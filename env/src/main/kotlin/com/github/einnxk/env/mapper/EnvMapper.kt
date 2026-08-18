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
package com.github.einnxk.env.mapper

import com.github.einnxk.common.annotations.Comment
import com.github.einnxk.common.annotations.Comments
import com.github.einnxk.common.annotations.Path
import com.github.einnxk.common.annotations.validate.Range
import com.github.einnxk.common.enums.ConfigMode
import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.env.EnvConfig
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * In the EnvMapper we convert a class into a flat key=value map or load
 * a flat key=value map into a class.
 *
 * Only primitives and Strings are supported - no nesting, no collections,
 * no custom classes.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
open class EnvMapper : BaseEnvMapper() {

    @Throws(Exception::class)
    fun saveToMap(clazz: Class<Any>): Map<String, String> {
        val result = LinkedHashMap<String, String>()

        if (!clazz.superclass.equals(EnvConfig::class.java)) {
            result.putAll(saveToMap(clazz.superclass))
        }

        clearEnvComments()

        for (field: Field in clazz.declaredFields) {
            if (doSkip(field)) continue

            val key = resolveKey(field)

            if (Modifier.isPrivate(field.modifiers)) {
                field.isAccessible = true
            }

            collectComments(field).forEach { addEnvComment(key, it) }

            val value = field.get(this) ?: continue
            assertSupportedType(field)
            result[key] = value.toString()
        }

        return result
    }

    @Throws(Exception::class)
    fun loadMap(values: Map<String, String>, clazz: Class<Any>) {
        if (!clazz.superclass.equals(EnvConfig::class.java)) {
            loadMap(values, clazz.superclass)
        }

        var needsSave = false

        for (field: Field in clazz.declaredFields) {
            if (doSkip(field)) continue

            val key = resolveKey(field)

            if (Modifier.isPrivate(field.modifiers)) {
                field.isAccessible = true
            }

            assertSupportedType(field)

            val raw = values[key]

            if (raw == null) {
                val defaultValue = field.get(this)
                if (defaultValue != null) {
                    needsSave = true
                }

                continue
            }

            field.set(this, convertValue(field, raw))

            validateRange(field)
        }

        if (needsSave) {
            @Suppress("UNCHECKED_CAST")
            saveToEnv(saveToMap(clazz))
        }
    }

    private fun convertValue(field: Field, raw: String): Any {
        return when (field.type) {
            String::class.java -> raw
            Int::class.java, Integer::class.java -> raw.toInt()
            Long::class.java, java.lang.Long::class.java -> raw.toLong()
            Double::class.java, java.lang.Double::class.java -> raw.toDouble()
            Float::class.java, java.lang.Float::class.java -> raw.toFloat()
            Boolean::class.java, java.lang.Boolean::class.java -> raw.toBoolean()
            Short::class.java, java.lang.Short::class.java -> raw.toShort()
            Byte::class.java, java.lang.Byte::class.java -> raw.toByte()
            else -> throw InvalidConfigurationException(
                "Unsupported type '${field.type.name}' for .env field '${field.name}'. " +
                        "Only primitives and String are supported."
            )
        }
    }

    private fun assertSupportedType(field: Field) {
        val supported = setOf(
            String::class.java,
            Int::class.java, Integer::class.java,
            Long::class.java, java.lang.Long::class.java,
            Double::class.java, java.lang.Double::class.java,
            Float::class.java, java.lang.Float::class.java,
            Boolean::class.java, java.lang.Boolean::class.java,
            Short::class.java, java.lang.Short::class.java,
            Byte::class.java, java.lang.Byte::class.java
        )

        if (field.type !in supported) {
            throw InvalidConfigurationException(
                "Unsupported type '${field.type.name}' for .env field '${field.name}'. " +
                        "Only primitives and String are supported."
            )
        }
    }

    private fun resolveKey(field: Field): String {
        if (field.isAnnotationPresent(Path::class.java)) {
            return field.getAnnotation(Path::class.java).value
        }

        return when (configMode) {
            ConfigMode.PATH_BY_UNDERSCORE -> field.name.replace("_", ".")
            ConfigMode.FIELD_IS_KEY -> field.name
            ConfigMode.DEFAULT -> field.name
        }
    }

    private fun collectComments(field: Field): List<String> {
        val comments = mutableListOf<String>()
        field.annotations.forEach { annotation ->
            when (annotation) {
                is Comment -> comments += annotation.value
                is Comments -> comments += annotation.value
            }
        }
        return comments
    }

    @Throws(InvalidConfigurationException::class)
    internal fun validateRange(field: Field) {
        field.isAccessible = true
        if (!field.isAnnotationPresent(Range::class.java)) return

        val range: Range = field.getAnnotation(Range::class.java)
        val min: Int = range.min
        val max: Int = range.max
        val value = field.get(this) ?: return

        when (value) {
            is Byte -> validateNumber(field.name, value.toLong(), min, max)
            is Short -> validateNumber(field.name, value.toLong(), min, max)
            is Int -> validateNumber(field.name, value.toLong(), min, max)
            is Long -> validateNumber(field.name, value, min, max)
            else -> throw InvalidConfigurationException(
                "@Range does not support type '${field.type.name}' for .env (field '${field.name}')"
            )
        }
    }

    @Throws(InvalidConfigurationException::class)
    private fun validateNumber(fieldName: String, value: Long, min: Int, max: Int) {
        if (value !in min.toLong()..max.toLong()) {
            throw InvalidConfigurationException(
                "Field '$fieldName' must be between $min - $max (currently: $value)"
            )
        }
    }
}