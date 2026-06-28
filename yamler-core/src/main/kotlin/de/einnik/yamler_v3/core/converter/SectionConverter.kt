package de.einnik.yamler_v3.core.converter

import de.einnik.yamler_v3.core.section.ConfigSection
import java.lang.reflect.ParameterizedType

/**
 * The SectionConverter is part of the default conversion library. This class
 * makes it able to convert whole ConfigSection's into YAML and back into an ConfigSection.
 *
 * @author EinNik
 * @since 3.0.10-SNAPSHOT
 */
open class SectionConverter(private val internalConverter: InternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        if (obj == null) return null

        val result = LinkedHashMap<Any, Any?>()
        val fields = getAllFields(obj.javaClass)

        for (field in fields) {
            if (java.lang.reflect.Modifier.isTransient(field.modifiers) ||
                java.lang.reflect.Modifier.isStatic(field.modifiers) ||
                java.lang.reflect.Modifier.isFinal(field.modifiers)) continue

            field.isAccessible = true
            val value = field.get(obj) ?: continue

            val converter = internalConverter.getConverter(value.javaClass)
            result[field.name] = converter?.toConfig(value.javaClass, value, null) ?: value
        }

        return result
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        if (type == null) return obj

        val instance = type.getDeclaredConstructor().newInstance() as ConfigSection

        val rawMap: Map<Any, Any?> = when (obj) {
            is ConfigSection -> obj.getRawMap()
            is Map<*, *> -> obj as Map<Any, Any?>
            else -> return instance
        }

        for ((key, value) in rawMap) {
            if (value == null) continue

            val fieldList = getAllFields(type)
            val matchingField = fieldList.firstOrNull { it.name == key.toString() }

            if (matchingField != null) {
                matchingField.isAccessible = true
                val converter = internalConverter.getConverter(matchingField.type)
                val converted = converter?.fromConfig(matchingField.type, value, null) ?: value
                matchingField.set(instance, converted)
            }
        }

        return instance
    }

    private fun getAllFields(clazz: Class<*>): List<java.lang.reflect.Field> {
        val fields = mutableListOf<java.lang.reflect.Field>()
        var current: Class<*>? = clazz
        while (current != null && current != ConfigSection::class.java && current != Any::class.java) {
            fields.addAll(current.declaredFields)
            current = current.superclass
        }
        return fields
    }

    override fun supports(type: Class<*>): Boolean {
        return ConfigSection::class.java.isAssignableFrom(type) && type != ConfigSection::class.java
    }
}