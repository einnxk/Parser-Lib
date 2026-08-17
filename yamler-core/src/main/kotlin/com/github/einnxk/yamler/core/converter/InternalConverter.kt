/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.einnxk.yamler.core.converter

import com.github.einnxk.common.annotations.PreserveStatic
import com.github.einnxk.yamler.core.YamlConfig
import com.github.einnxk.common.exception.InvalidConverterException
import com.github.einnxk.yamler.core.section.ConfigSection
import org.jetbrains.annotations.ApiStatus
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

/**
 * The manager class for [Converter]s which holds the default registered converters from the
 * `com.github.einnxk.yamler.core.converter.*` package and also is able to your converters using the
 * [addConverter] method.
 *
 * @author EinNik
 * @since 4.2.0
 *
 * @see Converter
 */
open class InternalConverter {

    private val converters: LinkedHashSet<Converter<*, *>> = linkedSetOf()

    init {
        try {
            addConverter(ConfigConverter::class.java)
            addConverter(SectionConverter::class.java)
            addConverter(ArrayConverter::class.java)
            addConverter(PrimitiveConverter::class.java)
            addConverter(MapConverter::class.java)
            addConverter(ListConverter::class.java)
            addConverter(SetConverter::class.java)
            addConverter(EnumConverter::class.java)
        } catch (e: Exception) {
            throw InvalidConverterException("Failed to init default converters: ", e)
        }
    }

    /**
     * Register a [Converter] class, which an instance is created of. No parameters, or only [InternalConverter]
     * constructors are allowed.
     *
     * @param converter The converter class that should be registered
     *
     * @throws InvalidConverterException when the converter does not have a matching constructor, or another
     * exception is thrown during class initialization
     */
    @Throws(InvalidConverterException::class)
    fun addConverter(converter: Class<out Converter<*, *>>) {
        try {
            val instance: Converter<*, *> = try {
                converter.getDeclaredConstructor(InternalConverter::class.java).newInstance(this)
            } catch (_: NoSuchMethodException) {
                converter.getDeclaredConstructor().newInstance()
            }

            converters.add(instance)
        } catch (e: Exception) {
            throw InvalidConverterException("Failed to init converter ${converter.name}: ", e)
        }
    }

    /**
     * Get a [Converter] from the registry which can convert a specified type
     *
     * @param type the class the convert should convert
     * @return a converter which a converts [type] or null
     */
    fun getConverter(type: Class<*>): Converter<*, *>? {
        return converters.firstOrNull { it.supports(type) }
    }

    @ApiStatus.Internal
    @Throws(Exception::class)
    fun fromConfig(config: YamlConfig, field: Field, root: ConfigSection, path: String) {
        val obj = field.get(config)
        val objConverter = obj?.let { getConverter(it.javaClass) }
        val fieldConverter = getConverter(field.type)
        @Suppress("UNCHECKED_CAST")
        val converter = (objConverter ?: fieldConverter) as Converter<Any?, Any?>?
        val isStatic = Modifier.isStatic(field.modifiers)
        if (isStatic) {
            val preserveStatic = field.getAnnotation(PreserveStatic::class.java)
            if (preserveStatic == null || !preserveStatic.value) {
                return
            }
        }

        val genericType = field.genericType as? ParameterizedType
        val rawValue = root.get(path) as Any?
        val value =
            if (converter != null) {
                val targetType =
                    if (objConverter != null)
                        obj.javaClass
                    else
                        field.type
                converter.fromConfig(targetType, rawValue, genericType)
            } else {
                rawValue
            }
        if (isStatic) {
            field.set(null, value)
        } else {
            field.set(config, value)
        }
    }

    @ApiStatus.Internal
    @Throws(Exception::class)
    fun toConfig(config: YamlConfig, field: Field, root: ConfigSection, path: String) {
        val obj = field.get(config)
        if (obj == null) {
            root.set(path, null)
            return
        }
        val objConverter = getConverter(obj.javaClass)
        val fieldConverter = getConverter(field.type)
        @Suppress("UNCHECKED_CAST")
        val converter = (objConverter ?: fieldConverter) as Converter<Any?, Any?>?
        val genericType = field.genericType as? ParameterizedType
        if (converter != null) {
            val targetType =
                if (objConverter != null)
                    obj.javaClass
                else
                    field.type
            root.set(path, converter.toConfig(targetType, obj, genericType)
            )
        } else {
            root.set(path, obj)
        }
    }
}