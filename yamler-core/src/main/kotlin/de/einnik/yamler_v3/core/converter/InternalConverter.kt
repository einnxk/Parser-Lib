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
package de.einnik.yamler_v3.core.converter

import de.einnik.yamler_v3.core.YamlConfig
import de.einnik.yamler_v3.core.annotations.PreserveStatic
import de.einnik.yamler_v3.core.exception.InvalidConverterException
import de.einnik.yamler_v3.core.section.ConfigSection
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

/**
 * This is the manager class for the converters here we register custom
 * or default converter
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
open class InternalConverter {

    private val converters: LinkedHashSet<Converter> = linkedSetOf()
    private val customConverters: MutableList<Class<out Converter>> = mutableListOf()

    init {
        try {
            addConverter(ConfigConverter::class.java)
            addConverter(SectionConverter::class.java)
            addConverter(ArrayConverter::class.java)
            addConverter(PrimitiveConverter::class.java)
            addConverter(MapConverter::class.java)
            addConverter(ListConverter::class.java)
            addConverter(SetConverter::class.java)
        } catch (e: Exception) {
            throw InvalidConverterException("Failed to init default converters: ", e)
        }
    }

    fun addConverter(converter: Class<out Converter>) {
        try {
            val instance: Converter = converter.getDeclaredConstructor(InternalConverter::class.java).newInstance(this)
            converters.add(instance)
        } catch (e: Exception) {
            throw InvalidConverterException("Failed to init converter ${converter.name}: ", e)
        }
    }

    fun getConverter(type: Class<*>): Converter? {
        return converters.firstOrNull { it.supports(type) }
    }

    fun customConverters(): List<Class<out Converter>> {
        return customConverters.toList()
    }

    fun addCustomConverter(converter: Class<out Converter>) {
        addConverter(converter)
        customConverters.add(converter)
    }

    @Throws(Exception::class)
    fun fromConfig(config: YamlConfig, field: Field, root: ConfigSection, path: String) {
        val obj = field.get(config)

        val objConverter = obj?.let { getConverter(it.javaClass) }
        val fieldConverter = getConverter(field.type)
        val converter = objConverter ?: fieldConverter

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
    fun toConfig(config: YamlConfig, field: Field, root: ConfigSection, path: String) {
        val obj = field.get(config)

        if (obj == null) {
            root.set(path, null)
            return
        }

        val objConverter = getConverter(obj.javaClass)
        val fieldConverter = getConverter(field.type)
        val converter = objConverter ?: fieldConverter

        val genericType = field.genericType as? ParameterizedType

        if (converter != null) {
            val targetType =
                if (objConverter != null)
                    obj.javaClass
                else
                    field.type

            root.set(
                path,
                converter.toConfig(
                    targetType,
                    obj,
                    genericType
                )
            )
        } else {
            root.set(path, obj)
        }
    }
}