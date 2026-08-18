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

import java.lang.reflect.ParameterizedType

/**
 * The ListConverter is part of the default [Converter] library. This class
 * makes it able to convert a list into YAML and back into a List.
 *
 * @author EinNik
 * @since 3.0.0
 */
open class ListConverter(private val internalConverter: InternalConverter) : Converter<Any, Any> {

    override fun toConfig(type: Class<*>, obj: Any, parameterizedType: ParameterizedType?): Any {
        val values = obj as MutableList<*>
        val newList = ArrayList<Any?>()

        for (value in values) {
            if (value == null) {
                newList.add(null)
                continue
            }

            @Suppress("UNCHECKED_CAST")
            val converter = internalConverter.getConverter(value.javaClass) as? Converter<Any, Any>

            if (converter != null) {
                newList.add(converter.toConfig(value.javaClass, value, null))
            } else {
                newList.add(value)
            }
        }

        return newList
    }

    @Suppress("UNCHECKED_CAST")
    override fun fromConfig(type: Class<*>, obj: Any, parameterizedType: ParameterizedType?): Any {
        var newList: MutableList<Any?> = ArrayList()

        try {
            newList = type.getDeclaredConstructor().newInstance() as MutableList<Any?>
        } catch (_: Exception) {}

        val values = obj as MutableList<Any?>

        if (
            parameterizedType != null &&
            parameterizedType.actualTypeArguments[0] is Class<*>
        ) {
            val elementType =
                parameterizedType.actualTypeArguments[0] as Class<*>

            val converter = internalConverter.getConverter(elementType) as? Converter<Any, Any>

            if (converter != null) {
                for (value in values) {
                    if (value == null) {
                        newList.add(null)
                        continue
                    }
                    newList.add(converter.fromConfig(elementType, value, null))
                }
            } else {
                newList = values
            }
        } else {
            newList = values
        }

        return newList
    }

    override fun supports(type: Class<*>): Boolean {
        return List::class.java.isAssignableFrom(type)
    }
}