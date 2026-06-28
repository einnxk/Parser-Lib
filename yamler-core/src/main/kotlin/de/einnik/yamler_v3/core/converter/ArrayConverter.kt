package de.einnik.yamler_v3.core.converter

import java.lang.reflect.ParameterizedType

/**
 * The MapConverter is part of the default conversion library. This class
 * makes it able to convert arrays into YAML and back into an Array.
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
class ArrayConverter(private val internalConverter: InternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        val singleType = type?.componentType ?: return obj

        val converter = internalConverter.getConverter(singleType)
            ?: return obj

        val length = java.lang.reflect.Array.getLength(obj)
        val result = arrayOfNulls<Any?>(length)

        for (i in 0 until length) {
            result[i] = converter.toConfig(
                singleType,
                java.lang.reflect.Array.get(obj, i),
                parameterizedType
            )
        }

        return result
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        val singleType = type?.componentType ?: return obj

        val values: List<Any?> =
            obj as? List<*> ?: (obj as Array<*>).toList()

        val result = java.lang.reflect.Array.newInstance(
            singleType,
            values.size
        )

        val converter = internalConverter.getConverter(singleType) ?: return values.toTypedArray()

        for (i in values.indices) {
            java.lang.reflect.Array.set(
                result,
                i,
                converter.fromConfig(
                    singleType,
                    values[i],
                    parameterizedType
                )
            )
        }

        return result
    }

    override fun supports(type: Class<*>): Boolean {
        return type.isArray
    }
}