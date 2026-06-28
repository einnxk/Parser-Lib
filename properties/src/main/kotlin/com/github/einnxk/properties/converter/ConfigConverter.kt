package com.github.einnxk.properties.converter

import com.github.einnxk.properties.PropertiesConfig
import java.lang.reflect.ParameterizedType

class ConfigConverter(private val internalConverter: PropertiesInternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        return obj!!
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {

        val clazz = requireNotNull(type)

        val config = newInstance(clazz) as PropertiesConfig

        for (converter in internalConverter.customConverters()) {
            internalConverter.addConverter(converter)
        }

        return config
    }

    private fun newInstance(type: Class<*>): Any {
        val enclosingClass = type.enclosingClass

        return if (enclosingClass != null) {
            val parent = newInstance(enclosingClass)
            type.getDeclaredConstructor(enclosingClass)
                .newInstance(parent)

        } else {
            type.getDeclaredConstructor()
                .newInstance()
        }
    }

    override fun supports(type: Class<*>): Boolean {
        return PropertiesConfig::class.java.isAssignableFrom(type)
    }
}