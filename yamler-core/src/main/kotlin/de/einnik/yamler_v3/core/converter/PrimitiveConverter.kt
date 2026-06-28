package de.einnik.yamler_v3.core.converter

import java.lang.reflect.ParameterizedType

/**
 * The MapConverter is part of the default conversion library. This class
 * makes it able to convert primitive datatypes into YAML and back into a primitive
 * datatype.
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
class PrimitiveConverter(private val internalConverter: InternalConverter) : Converter {

    private val types: Set<String> = setOf(
        "boolean",
        "char",
        "byte",
        "short",
        "int",
        "float",
        "long",
        "double"
    )

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        return obj
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        return when (type) {
            java.lang.Short.TYPE -> when (obj) {
                is Short -> obj
                is Int -> obj.toShort()
                else -> obj
            }

            java.lang.Byte.TYPE -> when (obj) {
                is Byte -> obj
                is Int -> obj.toByte()
                else -> obj
            }

            java.lang.Float.TYPE -> when (obj) {
                is Float -> obj
                is Int -> obj.toFloat()
                is Double -> obj.toFloat()
                else -> obj
            }

            Character.TYPE -> when (obj) {
                is Char -> obj
                is String -> obj.first()
                else -> obj
            }

            else -> obj
        }
    }

    override fun supports(type: Class<*>): Boolean {
        return types.contains(type.name)
    }
}