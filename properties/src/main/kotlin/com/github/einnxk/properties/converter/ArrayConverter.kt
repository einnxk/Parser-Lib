package com.github.einnxk.properties.converter

import java.lang.reflect.Array
import java.lang.reflect.ParameterizedType

class ArrayConverter(private val internalConverter: PropertiesInternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val component = type!!.componentType
        val converter =
            internalConverter.getConverter(component)
        val length = Array.getLength(obj)
        val values = ArrayList<String>(length)

        for (i in 0 until length) {
            val value = Array.get(obj, i)
            values += converter?.toConfig(
                component,
                value,
                null
            )?.toString() ?: value.toString()
        }

        return values.joinToString(",")
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val component = type!!.componentType
        val converter =
            internalConverter.getConverter(component)
        val values =
            obj.toString()
                .split(",")
                .map { it.trim() }
        val array =
            Array.newInstance(
                component,
                values.size
            )

        for (i in values.indices) {
            val value =
                converter?.fromConfig(
                    component,
                    values[i],
                    null
                ) ?: values[i]
            Array.set(array, i, value)
        }

        return array
    }

    override fun supports(type: Class<*>): Boolean {
        return type.isArray
    }
}