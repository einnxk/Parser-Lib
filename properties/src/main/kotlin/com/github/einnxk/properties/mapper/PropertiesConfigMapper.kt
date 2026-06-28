package com.github.einnxk.properties.mapper

import com.github.einnxk.common.annotations.Path
import com.github.einnxk.common.annotations.validate.Range
import com.github.einnxk.common.annotations.validate.Required
import com.github.einnxk.common.enums.ConfigMode
import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.properties.PropertiesConfig
import com.github.einnxk.properties.converter.PropertiesInternalConverter
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.Properties

/**
 * In the configMapper we convert a class into a Map or load
 * a map into a class.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
open class PropertiesConfigMapper : BaseConfigMapper() {

    @Transient
    protected val internalConverter = PropertiesInternalConverter()

    @Throws(Exception::class)
    fun saveToProperties(clazz: Class<Any>) {
        val props = this.properties

        writeClassToProperties(this, clazz, props)

        saveToProperties()
    }

    private fun writeClassToProperties(instance: Any, clazz: Class<*>, props: Properties) {

        if (clazz.superclass != null &&
            clazz.superclass != PropertiesConfig::class.java
        ) {
            writeClassToProperties(instance, clazz.superclass, props)
        }

        for (field in clazz.declaredFields) {
            if (doSkip(field)) continue

            var path = resolvePath(field)

            if (Modifier.isPrivate(field.modifiers)) {
                field.isAccessible = true
            }

            internalConverter.toConfig(
                instance as PropertiesConfig,
                field,
                props,
                path
            )
        }
    }

    @Throws(Exception::class)
    fun loadFromProperties(clazz: Class<Any>) {
        loadFromProperties()
        val props = this.properties

        readClassFromProperties(this, clazz, props)
    }

    private fun readClassFromProperties(instance: Any, clazz: Class<*>, props: Properties) {

        if (clazz.superclass != null &&
            clazz.superclass != PropertiesConfig::class.java
        ) {
            readClassFromProperties(instance, clazz.superclass, props)
        }

        for (field in clazz.declaredFields) {
            if (doSkip(field)) continue

            val path = resolvePath(field)

            if (Modifier.isPrivate(field.modifiers)) {
                field.isAccessible = true
            }

            internalConverter.fromConfig(
                instance as PropertiesConfig,
                field,
                props,
                path
            )

            validateRange(field)
            validateRequired(field)
        }
    }

    private fun resolvePath(field: Field): String {
        var path =
            if (configMode == ConfigMode.PATH_BY_UNDERSCORE)
                field.name.replace("_", ".")
            else
                field.name

        if (field.isAnnotationPresent(Path::class.java)) {
            path = field.getAnnotation(Path::class.java).value
        }

        return path
    }

    @Throws(InvalidConfigurationException::class)
    protected fun validateRequired(field: Field) {
        field.isAccessible = true

        val annotation = field.getAnnotation(Required::class.java)
            ?: return

        if (!annotation.value) return

        if (field.get(this) == null) {
            throw InvalidConfigurationException(
                "Field annotated with @Required is null: ${field.name}"
            )
        }
    }

    @Throws(InvalidConfigurationException::class)
    protected fun validateRange(field: Field) {
        field.isAccessible = true

        val range = field.getAnnotation(Range::class.java)
            ?: return

        val min = range.min
        val max = range.max

        val value = field.get(this) ?: return

        when (value) {
            is Byte, is Short, is Int, is Long ->
                validateNumber(field.name, (value as Number).toLong(), min, max)

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

    private fun validateNumber(fieldName: String, value: Long, min: Int, max: Int) {
        if (value !in min.toLong()..max.toLong()) {
            throw InvalidConfigurationException(
                "Field '$fieldName' must be between $min - $max (currently: $value)"
            )
        }
    }

    private fun validateSize(fieldName: String, size: Int, min: Int, max: Int) {
        if (size !in min..max) {
            throw InvalidConfigurationException(
                "Field '$fieldName' must have size between $min - $max (currently: $size)"
            )
        }
    }
}