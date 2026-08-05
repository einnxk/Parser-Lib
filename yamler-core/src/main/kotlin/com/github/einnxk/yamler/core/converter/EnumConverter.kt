package com.github.einnxk.yamler.core.converter

import java.lang.reflect.ParameterizedType

/**
 * Part of the default converter library which automatically converts enums.
 *
 * @author EinNik
 * @since 4.1.0-SNAPSHOT
 */
open class EnumConverter : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        if (obj is Enum<*>) {
            return obj.name
        }

        return obj
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        if (type != null && type.isEnum && obj != null) {
            val name = obj.toString()
            @Suppress("UNCHECKED_CAST")
            val enumClass = type as Class<out Enum<*>>

            return java.lang.Enum.valueOf(enumClass, name)
        }

        return obj
    }

    override fun supports(type: Class<*>): Boolean {
        return type.isEnum
    }
}