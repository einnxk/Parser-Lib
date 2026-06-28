package com.github.einnxk.properties.converter

import java.lang.reflect.ParameterizedType

class EnumConverter(private val internalConverter: PropertiesInternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        return (obj as? Enum<*>)?.name
    }

    @Suppress("UNCHECKED_CAST")
    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        val clazz = type ?: return obj
        val value = obj?.toString() ?: return null

        return java.lang.Enum.valueOf(
            clazz as Class<out Enum<*>>,
            value
        )
    }

    override fun supports(type: Class<*>): Boolean {
        return type.isEnum
    }
}