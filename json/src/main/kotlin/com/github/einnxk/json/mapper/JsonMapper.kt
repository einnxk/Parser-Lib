package com.github.einnxk.json.mapper

import com.github.einnxk.common.annotations.*
import com.github.einnxk.common.annotations.validate.Range
import com.github.einnxk.common.annotations.validate.Required
import com.github.einnxk.common.enums.ConfigMode
import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.json.JsonConfig
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * In the JsonMapper we convert a class into a JsonObject or load
 * a JsonObject into a class using Gson.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
open class JsonMapper : BaseJsonMapper() {

    @Transient
    private val gson = GsonBuilder().setPrettyPrinting().create()

    @Throws(Exception::class)
    fun saveToMap(clazz: Class<Any>): JsonObject {
        val jsonObject = JsonObject()

        if (!clazz.superclass.equals(JsonConfig::class.java)) {
            val superObject = saveToMap(clazz.superclass)
            for ((key, value) in superObject.entrySet()) {
                jsonObject.add(key, value)
            }
        }

        for (field: Field in clazz.declaredFields) {
            if (doSkip(field)) continue

            var path = resolveFieldPath(field)

            if (Modifier.isPrivate(field.modifiers)) {
                field.isAccessible = true
            }

            val value = field.get(this)
            jsonObject.add(path, gson.toJsonTree(value))
        }

        return jsonObject
    }

    @Throws(Exception::class)
    fun loadMap(jsonObject: JsonObject, clazz: Class<Any>) {
        if (!clazz.superclass.equals(JsonConfig::class.java)) {
            loadMap(jsonObject, clazz.superclass)
        }

        for (field: Field in clazz.declaredFields) {
            if (doSkip(field)) continue

            var path = resolveFieldPath(field)

            if (Modifier.isPrivate(field.modifiers)) {
                field.isAccessible = true
            }

            val element: JsonElement? = jsonObject.get(path)

            if (element == null || element.isJsonNull) {
                validRequired(field)
                continue
            }

            field.set(this, gson.fromJson(element, field.genericType))

            validateRange(field)
            validRequired(field)
        }
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
    private fun validRequired(field: Field) {
        field.isAccessible = true

        if (!field.isAnnotationPresent(Required::class.java)) return

        val required = field.getAnnotation(Required::class.java).value
        if (!required) return

        field.get(this) ?: throw InvalidConfigurationException(
            "A field annotated with @Required is null: ${field.name}"
        )
    }

    @Throws(InvalidConfigurationException::class)
    private fun validateRange(field: Field) {
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