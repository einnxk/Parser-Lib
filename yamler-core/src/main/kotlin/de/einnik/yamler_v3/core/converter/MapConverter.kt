package de.einnik.yamler_v3.core.converter

import de.einnik.yamler_v3.core.section.ConfigSection
import java.lang.reflect.ParameterizedType

/**
 * The MapConverter is part of the default conversion library. This class
 * makes it able to convert a Map into YAML and back into a Map.
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
class MapConverter(private val internalConverter: InternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        val source = obj as? Map<*, *> ?: return obj
        val result = HashMap<Any?, Any?>()

        for ((key, value) in source) {
            if (value == null) {
                result[key] = null
                continue
            }

            val converter = internalConverter.getConverter(value.javaClass)

            result[key] = if (converter != null) {
                converter.toConfig(value.javaClass, value, null)
            } else {
                value
            }
        }

        return result
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        if (parameterizedType == null) return obj

        val map: MutableMap<Any, Any?> = try {
            val rawType = parameterizedType.rawType as Class<*>
            rawType.getDeclaredConstructor().newInstance() as MutableMap<Any, Any?>
        } catch (_: Exception) {
            HashMap()
        }

        val actualTypeArguments = parameterizedType.actualTypeArguments

        if (actualTypeArguments.size == 2) {
            val keyClass = actualTypeArguments[0] as Class<*>
            val currentSection = obj ?: HashMap<Any?, Any?>()

            val map1 = currentSection as? Map<*, *> ?: (currentSection as ConfigSection).getRawMap()

            for ((entryKey, entryValue) in map1) {
                if (entryKey == null) continue

                val key: Any = when (keyClass) {
                    Int::class.java if entryKey !is Int -> Integer.valueOf(entryKey.toString())
                    Short::class.java if entryKey !is Short -> java.lang.Short.valueOf(entryKey.toString())
                    Byte::class.java if entryKey !is Byte -> java.lang.Byte.valueOf(entryKey.toString())
                    Float::class.java if entryKey !is Float -> java.lang.Float.valueOf(entryKey.toString())
                    Double::class.java if entryKey !is Double -> java.lang.Double.valueOf(entryKey.toString())
                    else -> entryKey
                }

                val valueType = actualTypeArguments[1]
                val clazz: Class<*>
                val subParameterizedType: ParameterizedType?

                if (valueType is ParameterizedType) {
                    clazz = valueType.rawType as Class<*>
                    subParameterizedType = valueType
                } else {
                    clazz = valueType as Class<*>
                    subParameterizedType = null
                }

                val converter = internalConverter.getConverter(clazz)
                map[key] = if (converter != null) {
                    converter.fromConfig(clazz, entryValue, subParameterizedType)
                } else {
                    entryValue
                }
            }
        } else {
            val rawClass = parameterizedType.rawType as Class<*>
            val converter = internalConverter.getConverter(rawClass)

            if (converter != null) {
                return converter.fromConfig(rawClass, obj, null)
            }

            return obj as? Map<*, *> ?: (obj as ConfigSection).getRawMap()
        }

        return map
    }

    override fun supports(type: Class<*>): Boolean {
        return Map::class.java.isAssignableFrom(type)
    }
}