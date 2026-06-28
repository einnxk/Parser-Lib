package com.github.einnxk.properties.converter

import java.lang.reflect.ParameterizedType

class ListConverter(private val internalConverter: PropertiesInternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val list = obj as List<*>

        return list.joinToString(",") { value ->
            if (value == null) {
                ""
            } else {
                val converter = internalConverter.getConverter(value.javaClass)

                converter?.toConfig(
                    value.javaClass,
                    value,
                    null
                )?.toString() ?: value.toString()
            }
        }
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val result =
            try {
                type?.getDeclaredConstructor()?.newInstance() as MutableList<Any?>
            } catch (_: Exception) {
                mutableListOf()
            }
        val value = obj?.toString() ?: return result

        if (value.isBlank())
            return result

        val values = value.split(",")
        if (
            parameterizedType != null &&
            parameterizedType.actualTypeArguments[0] is Class<*>
        ) {

            val elementType =
                parameterizedType.actualTypeArguments[0] as Class<*>
            val converter =
                internalConverter.getConverter(elementType)

            for (entry in values) {
                result.add(
                    converter?.fromConfig(
                        elementType,
                        entry.trim(),
                        null
                    ) ?: entry.trim()
                )
            }

        } else {
            result.addAll(values.map { it.trim() })
        }

        return result
    }

    override fun supports(type: Class<*>): Boolean {
        return List::class.java.isAssignableFrom(type)
    }
}