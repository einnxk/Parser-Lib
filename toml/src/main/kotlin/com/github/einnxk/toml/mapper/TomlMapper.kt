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
package com.github.einnxk.toml.mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.einnxk.common.annotations.Path
import com.github.einnxk.common.annotations.validate.Range
import com.github.einnxk.common.annotations.validate.Required
import com.github.einnxk.common.enums.ConfigMode
import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.toml.TomlConfig
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * In the TomlMapper we convert a class into an ObjectNode or load
 * an ObjectNode into a class using Jackson TOML.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
open class TomlMapper : BaseTomlMapper() {

    @Throws(Exception::class)
    fun saveToMap(clazz: Class<Any>): ObjectNode {
        val node = tomlMapper.createObjectNode()

        if (!clazz.superclass.equals(TomlConfig::class.java)) {
            val superNode = saveToMap(clazz.superclass)
            superNode.properties().forEach { (key, value) -> node.set<JsonNode>(key, value) }
        }

        for (field: Field in clazz.declaredFields) {
            if (doSkip(field)) continue

            val path = resolveFieldPath(field)

            if (Modifier.isPrivate(field.modifiers)) {
                field.isAccessible = true
            }

            val value = field.get(this)
            if (value != null) {
                setNestedValue(node, path, tomlMapper.valueToTree(value))
            }
        }

        return node
    }

    @Throws(Exception::class)
    fun loadMap(node: ObjectNode, clazz: Class<Any>) {
        if (!clazz.superclass.equals(TomlConfig::class.java)) {
            loadMap(node, clazz.superclass)
        }

        var needsSave = false

        for (field: Field in clazz.declaredFields) {
            if (doSkip(field)) continue

            val path = resolveFieldPath(field)

            if (Modifier.isPrivate(field.modifiers)) {
                field.isAccessible = true
            }

            val element: JsonNode? = getNestedValue(node, path)

            if (element == null || element.isNull || element.isMissingNode) {
                if (strictLoad) {
                    val required = field.getAnnotation(Required::class.java)
                    if (required != null && required.value) {
                        throw InvalidConfigurationException(
                            "Required field '${field.name}' is missing in TOML"
                        )
                    }
                }

                val defaultValue = field.get(this)
                if (defaultValue != null) {
                    setNestedValue(node, path, tomlMapper.valueToTree(defaultValue))
                    needsSave = true
                }

                continue
            }

            field.set(this, tomlMapper.convertValue(element, field.type))

            validateRange(field)
            if (strictLoad) validRequired(field)
        }

        if (needsSave) {
            saveToToml(node)
        }
    }

    private fun setNestedValue(node: ObjectNode, path: String, value: JsonNode) {
        val keys = path.split(".")
        var current = node

        for (i in 0 until keys.size - 1) {
            val key = keys[i]
            if (!current.has(key) || !current.get(key).isObject) {
                current.set<JsonNode>(key, tomlMapper.createObjectNode())
            }
            current = current.get(key) as ObjectNode
        }

        current.set<JsonNode>(keys.last(), value)
    }

    private fun getNestedValue(node: ObjectNode, path: String): JsonNode? {
        val keys = path.split(".")
        var current: JsonNode = node

        for (key in keys) {
            if (!current.isObject) return null
            current = current.get(key) ?: return null
        }

        return current
    }

    private fun resolveFieldPath(field: Field): String {
        if (field.isAnnotationPresent(Path::class.java)) {
            return field.getAnnotation(Path::class.java).value
        }

        return when (configMode) {
            ConfigMode.PATH_BY_UNDERSCORE -> field.name.replace("_", ".")
            ConfigMode.FIELD_IS_KEY -> field.name
            ConfigMode.DEFAULT -> field.name.replace("_", ".")
        }
    }

    @Throws(InvalidConfigurationException::class)
    internal fun validRequired(field: Field) {
        field.isAccessible = true
        if (!field.isAnnotationPresent(Required::class.java)) return
        val required = field.getAnnotation(Required::class.java).value
        if (!required) return
        field.get(this) ?: throw InvalidConfigurationException(
            "A field annotated with @Required is null: ${field.name}"
        )
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
                "Field '$fieldName' must be between $min - $max (currently: $value)"
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