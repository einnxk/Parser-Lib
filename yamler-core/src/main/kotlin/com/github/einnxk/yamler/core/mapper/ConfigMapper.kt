/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.einnxk.yamler.core.mapper

import com.github.einnxk.common.annotations.EnvironmentOverride
import com.github.einnxk.common.annotations.Path
import com.github.einnxk.common.annotations.validate.Range
import com.github.einnxk.common.enums.ConfigMode
import com.github.einnxk.yamler.core.YamlConfig
import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.yamler.core.converter.Converter
import com.github.einnxk.yamler.core.section.ConfigSection
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

/**
 * In the configMapper we convert a class into a Map or load
 * a map into a class.
 *
 * @author EinNik
 * @since 3.0.0
 */
@Suppress("UNCHECKED_CAST")
open class ConfigMapper : BaseConfigMapper() {

    @Throws(Exception::class)
    fun saveToMap(clazz: Class<Any>) : Map<String, Any> {
        val map: MutableMap<String, Any> = ConcurrentHashMap()

        if (!(clazz.superclass.equals(YamlConfig::class.java))) {
            val superClassMap: Map<String, Any> = saveToMap(clazz.superclass)
            map.putAll(superClassMap)
        }

        for (field: Field in clazz.declaredFields) {
            if (doSkip(field)) continue

            var path: String

            path = when (configMode) {
                ConfigMode.PATH_BY_UNDERSCORE ->
                    field.name.replace("_", ".")
                ConfigMode.FIELD_IS_KEY ->
                    field.name
                ConfigMode.DEFAULT ->
                    field.name.replace("_", ".")
            }

            if (field.isAnnotationPresent(Path::class.java)) run {
                val annotationPath: Path = field.getAnnotation(Path::class.java)
                path = annotationPath.value
            }

            if (Modifier.isPrivate(field.modifiers)) {
                field.isAccessible = true
            }

            map[path] = field.get(this)
        }

        val mapConverter = requireNotNull(internalConverter.getConverter(Map::class.java)) {
            "No Map converter registered" } as Converter<Any, Any>
        return mapConverter.toConfig(HashMap::class.java, map, null) as Map<String, Any>
    }

    @Throws(Exception::class)
    fun loadMap(section: Map<Any, Any>, clazz: Class<Any>)  {
        if (!(clazz.superclass.equals(YamlConfig::class.java))) {
            loadMap(section, clazz.superclass)
        }

        for (field: Field in clazz.declaredFields) {
            if (doSkip(field)) continue

            var path = if (configMode == ConfigMode.PATH_BY_UNDERSCORE) field.name
                .replace("_".toRegex(), ".") else field.name

            if (field.isAnnotationPresent(Path::class.java)) run {
                val annotationPath: Path = field.getAnnotation(Path::class.java)
                path = annotationPath.value
            }

            if (Modifier.isPrivate(field.modifiers)) {
                field.isAccessible = true
            }

            val envOverride = field.getAnnotation(EnvironmentOverride::class.java)
            if (envOverride != null && applyEnvironmentOverride(field, envOverride)) {
                validateRange(field)
                continue
            }

            internalConverter.fromConfig(this as YamlConfig, field, ConfigSection.convertFromMap(section), path)
            validateRange(field)
        }
    }

    @Throws(IllegalStateException::class)
    private fun applyEnvironmentOverride(field: Field, annotation: EnvironmentOverride): Boolean {
        val rawValue = System.getenv(annotation.value)

        if (rawValue == null) {
            if (annotation.throwIfNull) {
                throw IllegalStateException(
                    "Environment variable '${annotation.value}' for field '${field.name}' is not set"
                )
            }
            return false
        }

        val convertedValue = parseEnvironmentValue(rawValue, field.type)

        if (convertedValue == null) {
            if (annotation.throwIfWrongType) {
                throw IllegalStateException(
                    "Environment variable '${annotation.value}' with value '$rawValue' could not be parsed " +
                            "into type '${field.type.name}' for field '${field.name}'"
                )
            }
            return false
        }

        field.set(this, convertedValue)
        return true
    }

    private fun parseEnvironmentValue(rawValue: String, type: Class<*>): Any? {
        return when {
            type == String::class.java -> rawValue
            type == Int::class.java || type == java.lang.Integer::class.java -> rawValue.toIntOrNull()
            type == Long::class.java || type == java.lang.Long::class.java -> rawValue.toLongOrNull()
            type == Short::class.java || type == java.lang.Short::class.java -> rawValue.toShortOrNull()
            type == Byte::class.java || type == java.lang.Byte::class.java -> rawValue.toByteOrNull()
            type == Double::class.java || type == java.lang.Double::class.java -> rawValue.toDoubleOrNull()
            type == Float::class.java || type == java.lang.Float::class.java -> rawValue.toFloatOrNull()
            type == Boolean::class.java || type == java.lang.Boolean::class.java -> rawValue.toBooleanStrictOrNull()
            type.isEnum -> {
                @Suppress("UNCHECKED_CAST")
                val enumType = type as Class<out Enum<*>>
                enumType.enumConstants.firstOrNull { it.name.equals(rawValue, ignoreCase = true) }
            }
            else -> null
        }
    }

    @Throws(InvalidConfigurationException::class)
    protected fun validateRange(field: Field) {
        field.isAccessible = true

        if (!(field.isAnnotationPresent(Range::class.java))) {
            return
        }

        val range: Range = field.getAnnotation(Range::class.java)
        val min: Int = range.min
        val max: Int = range.max

        val value = field.get(this) ?: return

        when (value) {
            is Byte -> validateNumber(field.name, value.toLong(), min, max)
            is Short -> validateNumber(field.name, value.toLong(), min, max)
            is Int -> validateNumber(field.name, value.toLong(), min, max)
            is Long -> validateNumber(field.name, value, min, max)

            is Collection<*> -> validateSize(field.name, value.size, min, max)
            is Map<*, *> -> validateSize(field.name, value.size, min, max)

            is Array<*> -> validateSize(field.name, value.size, min, max)
            is ByteArray -> validateSize(field.name, value.size, min, max)
            is ShortArray -> validateSize(field.name, value.size, min, max)
            is IntArray -> validateSize(field.name, value.size, min, max)
            is LongArray -> validateSize(field.name, value.size, min, max)
            is FloatArray -> validateSize(field.name, value.size, min, max)
            is DoubleArray -> validateSize(field.name, value.size, min, max)
            is CharArray -> validateSize(field.name, value.size, min, max)
            is BooleanArray -> validateSize(field.name, value.size, min, max)

            else -> throw InvalidConfigurationException(
                "@Range does not support type '${field.type.name}' (field '${field.name}')"
            )
        }
    }

    @Throws(InvalidConfigurationException::class)
    private fun validateNumber(fieldName: String, value: Long, min: Int, max: Int) {
        if (value !in min.toLong()..max.toLong()) {
            throw InvalidConfigurationException(
                "Filed '$fieldName' must be between $min - $max (currently: $value)"
            )
        }
    }

    @Throws(InvalidConfigurationException::class)
    private fun validateSize(fieldName: String, size: Int, min: Int, max: Int) {
        if (size !in min..max) {
            throw InvalidConfigurationException(
                "Field '$fieldName' must have a size between $min - $max (currently: $size)"
            )
        }
    }
}