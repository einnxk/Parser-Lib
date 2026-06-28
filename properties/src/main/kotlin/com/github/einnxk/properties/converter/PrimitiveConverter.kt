package com.github.einnxk.properties.converter

import java.lang.reflect.ParameterizedType
import java.util.*

/**
 * The MapConverter is part of the default conversion library. This class
 * makes it able to convert primitive datatypes into Properties and back into a primitive
 * datatype.
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
class PrimitiveConverter(private val internalConverter: PropertiesInternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        return obj?.toString()
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        val value = obj?.toString() ?: return null

        return when (type) {
            String::class.java -> value

            java.lang.Boolean::class.java,
            Boolean::class.javaPrimitiveType ->
                value.toBoolean()

            java.lang.Byte::class.java,
            Byte::class.javaPrimitiveType ->
                value.toByte()

            java.lang.Short::class.java,
            Short::class.javaPrimitiveType ->
                value.toShort()

            java.lang.Integer::class.java,
            Int::class.javaPrimitiveType ->
                value.toInt()

            java.lang.Long::class.java,
            Long::class.javaPrimitiveType ->
                value.toLong()

            java.lang.Float::class.java,
            Float::class.javaPrimitiveType ->
                value.toFloat()

            java.lang.Double::class.java,
            Double::class.javaPrimitiveType ->
                value.toDouble()

            java.lang.Character::class.java,
            Char::class.javaPrimitiveType ->
                value.first()

            UUID::class.java ->
                UUID.fromString(value)

            else -> obj
        }
    }

    override fun supports(type: Class<*>): Boolean {
        return type.isPrimitive
                || type == String::class.java
                || type == java.lang.Boolean::class.java
                || type == java.lang.Byte::class.java
                || type == java.lang.Short::class.java
                || type == Integer::class.java
                || type == java.lang.Long::class.java
                || type == java.lang.Float::class.java
                || type == java.lang.Double::class.java
                || type == Character::class.java
                || type == UUID::class.java
    }
}