package com.github.einnxk.properties.converter

import com.github.einnxk.common.annotations.PreserveStatic
import com.github.einnxk.common.exception.InvalidConverterException
import com.github.einnxk.properties.PropertiesConfig
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.util.*

/**
 * This is the manager class for the converters here we register custom
 * or default converter
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
open class PropertiesInternalConverter {

    private val converters = linkedSetOf<Converter>()
    private val customConverters = mutableListOf<Class<out Converter>>()

    init {
        try {
            addConverter(ConfigConverter::class.java)
            addConverter(ArrayConverter::class.java)
            addConverter(PrimitiveConverter::class.java)
            addConverter(ListConverter::class.java)
            addConverter(SetConverter::class.java)
            addConverter(EnumConverter::class.java)
        } catch (e: Exception) {
            throw InvalidConverterException(
                "Failed to initialize default converters.",
                e
            )
        }
    }

    fun addConverter(converter: Class<out Converter>) {
        try {
            val instance = converter
                .getDeclaredConstructor(PropertiesInternalConverter::class.java)
                .newInstance(this)

            converters.add(instance)
        } catch (e: Exception) {
            throw InvalidConverterException(
                "Failed to initialize converter ${converter.name}.",
                e
            )
        }
    }

    fun addCustomConverter(converter: Class<out Converter>) {
        addConverter(converter)
        customConverters += converter
    }

    fun customConverters(): List<Class<out Converter>> =
        customConverters.toList()

    fun getConverter(type: Class<*>): Converter? =
        converters.firstOrNull { it.supports(type) }

    @Throws(Exception::class)
    fun fromConfig(
        config: PropertiesConfig,
        field: Field,
        properties: Properties,
        path: String
    ) {

        val current = field.get(config)

        val objectConverter = current?.let {
            getConverter(it.javaClass)
        }

        val fieldConverter = getConverter(field.type)

        val converter = objectConverter ?: fieldConverter

        val isStatic = Modifier.isStatic(field.modifiers)

        if (isStatic) {
            val preserve = field.getAnnotation(PreserveStatic::class.java)

            if (preserve == null || !preserve.value) {
                return
            }
        }

        val rawValue = properties.getProperty(path) ?: return

        val genericType =
            field.genericType as? ParameterizedType

        val value =
            if (converter != null) {

                val targetType =
                    objectConverter?.let { current!!.javaClass }
                        ?: field.type

                converter.fromConfig(
                    targetType,
                    rawValue,
                    genericType
                )

            } else {
                rawValue
            }

        if (isStatic) {
            field.set(null, value)
        } else {
            field.set(config, value)
        }
    }

    @Throws(Exception::class)
    fun toConfig(
        config: PropertiesConfig,
        field: Field,
        properties: Properties,
        path: String
    ) {

        val value = field.get(config)

        if (value == null) {
            properties.remove(path)
            return
        }

        val objectConverter =
            getConverter(value.javaClass)

        val fieldConverter =
            getConverter(field.type)

        val converter =
            objectConverter ?: fieldConverter

        val genericType =
            field.genericType as? ParameterizedType

        val result =
            if (converter != null) {

                val targetType =
                    objectConverter?.let { value.javaClass }
                        ?: field.type

                converter.toConfig(
                    targetType,
                    value,
                    genericType
                )

            } else {
                value
            }

        if (result != null) {
            properties[path] = result.toString()
        }
    }
}