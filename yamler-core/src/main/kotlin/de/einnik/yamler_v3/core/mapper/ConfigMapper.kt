package de.einnik.yamler_v3.core.mapper

import de.einnik.yamler_v3.core.YamlConfig
import de.einnik.yamler_v3.core.annotations.Path
import de.einnik.yamler_v3.core.annotations.validate.Range
import de.einnik.yamler_v3.core.annotations.validate.Required
import de.einnik.yamler_v3.core.enums.ConfigMode
import de.einnik.yamler_v3.core.exception.InvalidConfigurationException
import de.einnik.yamler_v3.core.section.ConfigSection
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap

/**
 * In the configMapper we convert a class into a Map or load
 * a map into a class.
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
open class ConfigMapper : BaseConfigMapper() {

    @Throws(Exception::class)
    fun saveToMap(clazz: Class<Any>) : Map<String, Any> {
        val map: MutableMap<String, Any> = ConcurrentHashMap()

        if (!(clazz.superclass.equals(YamlConfig::class.java))) {
            val superClassMap: Map<String, Any> = saveToMap(clazz.superclass)
            map.putAll(superClassMap)
        }

        for (field: Field in clazz.declaredFields) {
            if (doSkip(field)) continue

            var path: String

            path = when (configMode) {
                ConfigMode.PATH_BY_UNDERSCORE ->
                    field.name.replace("_", ".")
                ConfigMode.FIELD_IS_KEY ->
                    field.name
                ConfigMode.DEFAULT ->
                    field.name.replace("_", ".")
            }

            if (field.isAnnotationPresent(Path::class.java)) run {
                val annotationPath: Path = field.getAnnotation(Path::class.java)
                path = annotationPath.value
            }

            if (Modifier.isPrivate(field.modifiers)) {
                field.isAccessible = true
            }

            map[path] = field.get(this)
        }

        val mapConverter = requireNotNull(
            internalConverter.getConverter(Map::class.java)
        ) { "No Map converter registered" }

        @Suppress("UNCHECKED_CAST")
        return mapConverter.toConfig(
            HashMap::class.java,
            map,
            null
        ) as Map<String, Any>
    }

    @Throws(Exception::class)
    fun loadMap(section: Map<Any, Any>, clazz: Class<Any>)  {
        if (!(clazz.superclass.equals(YamlConfig::class.java))) {
            loadMap(section, clazz.superclass)
        }

        for (field: Field in clazz.declaredFields) {
            if (doSkip(field)) continue

            var path = if (configMode == ConfigMode.PATH_BY_UNDERSCORE) field.name
                .replace("_".toRegex(), ".") else field.name

            if (field.isAnnotationPresent(Path::class.java)) run {
                val annotationPath: Path = field.getAnnotation(Path::class.java)
                path = annotationPath.value
            }

            if (Modifier.isPrivate(field.modifiers)) {
                field.isAccessible = true
            }

            internalConverter.fromConfig(this as YamlConfig, field, ConfigSection.convertFromMap(section), path)

            validateRange(field)
            validRequired(field)
        }
    }

    @Throws(InvalidConfigurationException::class)
    protected fun validRequired(field: Field) {
        field.isAccessible = true

        if (!(field.isAnnotationPresent(Required::class.java))) {
            return
        }

        val required = field.getAnnotation(Required::class.java).value
        if (!(required)) {
            return
        }

        field.get(this) ?: throw InvalidConfigurationException(
            "An field annotated with @Required is null: ${field.name}"
        )
    }

    @Throws(InvalidConfigurationException::class)
    protected fun validateRange(field: Field) {
        field.isAccessible = true

        if (!(field.isAnnotationPresent(Range::class.java))) {
            return
        }

        val range: Range = field.getAnnotation(Range::class.java)
        val min: Int = range.min
        val max: Int = range.max

        val value = field.get(this) ?: return

        when (value) {
            is Byte -> validateNumber(field.name, value.toLong(), min, max)
            is Short -> validateNumber(field.name, value.toLong(), min, max)
            is Int -> validateNumber(field.name, value.toLong(), min, max)
            is Long -> validateNumber(field.name, value, min, max)

            is Collection<*> -> validateSize(field.name, value.size, min, max)
            is Map<*, *> -> validateSize(field.name, value.size, min, max)

            is Array<*> -> validateSize(field.name, value.size, min, max)
            is ByteArray -> validateSize(field.name, value.size, min, max)
            is ShortArray -> validateSize(field.name, value.size, min, max)
            is IntArray -> validateSize(field.name, value.size, min, max)
            is LongArray -> validateSize(field.name, value.size, min, max)
            is FloatArray -> validateSize(field.name, value.size, min, max)
            is DoubleArray -> validateSize(field.name, value.size, min, max)
            is CharArray -> validateSize(field.name, value.size, min, max)
            is BooleanArray -> validateSize(field.name, value.size, min, max)

            else -> throw InvalidConfigurationException(
                "@Range does not support type '${field.type.name}' (field '${field.name}')"
            )
        }
    }

    @Throws(InvalidConfigurationException::class)
    private fun validateNumber(fieldName: String, value: Long, min: Int, max: Int) {
        if (value !in min.toLong()..max.toLong()) {
            throw InvalidConfigurationException(
                "Filed '$fieldName' must be between $min - $max (currently: $value)"
            )
        }
    }

    @Throws(InvalidConfigurationException::class)
    private fun validateSize(fieldName: String, size: Int, min: Int, max: Int) {
        if (size !in min..max) {
            throw InvalidConfigurationException(
                "Field '$fieldName' must have a size between $min - $max (currently: $size)"
            )
        }
    }
}