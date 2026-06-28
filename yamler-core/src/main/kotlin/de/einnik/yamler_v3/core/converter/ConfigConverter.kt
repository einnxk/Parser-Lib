package de.einnik.yamler_v3.core.converter

import de.einnik.yamler_v3.core.YamlConfig
import de.einnik.yamler_v3.core.section.ConfigSection
import java.lang.reflect.ParameterizedType

class ConfigConverter(private val internalConverter: InternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        return obj as? Map<*, *> ?: (obj as YamlConfig).saveToMap(obj.javaClass)
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val clazz = type ?: error("type is null")
        val yamlConfig = newInstance(clazz) as YamlConfig

        for (converterClass in internalConverter.customConverters()) {
            yamlConfig.addConverter(converterClass)
        }

        val map: Map<Any, Any> = if (obj is Map<*, *>) {
            obj as Map<Any, Any>
        } else {
            (obj as ConfigSection).getRawMap() as Map<Any, Any>
        }

        yamlConfig.loadMap(map, clazz as Class<Any>)

        return yamlConfig
    }

    fun newInstance(type: Class<*>): Any {
        val enclosingClass = type.enclosingClass

        return if (enclosingClass != null) {
            val enclosingInstance = newInstance(enclosingClass)

            type.getConstructor(enclosingClass)
                .newInstance(enclosingInstance)
        } else {
            type.getDeclaredConstructor()
                .newInstance()
        }
    }

    override fun supports(type: Class<*>): Boolean {
        return YamlConfig::class.java.isAssignableFrom(type)
    }
}