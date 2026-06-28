package com.github.einnxk.properties.converter

import java.lang.reflect.ParameterizedType

class SetConverter(internalConverter: PropertiesInternalConverter) : Converter {

    private val listConverter = ListConverter(internalConverter)

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        return listConverter.toConfig(
            MutableList::class.java,
            (obj as Set<*>).toMutableList(),
            parameterizedType
        )
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val list =
            listConverter.fromConfig(
                MutableList::class.java,
                obj,
                parameterizedType
            ) as MutableList<*>

        return LinkedHashSet(list)
    }

    override fun supports(type: Class<*>): Boolean {
        return Set::class.java.isAssignableFrom(type)
    }
}