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
 * The ArrayConverter is part of the default [Converter] library. This class
 * makes it able to convert arrays into YAML and back into an Array.
 *
 * @author EinNik
 * @since 3.0.0
 */
open class ArrayConverter(private val internalConverter: InternalConverter) : Converter<Any, Any> {

    override fun toConfig(type: Class<*>, obj: Any, parameterizedType: ParameterizedType?): Any {
        val singleType = type.componentType ?: return obj

        @Suppress("UNCHECKED_CAST")
        val converter = internalConverter.getConverter(singleType) as? Converter<Any, Any>
            ?: return obj

        val length = java.lang.reflect.Array.getLength(obj)
        val result = arrayOfNulls<Any?>(length)

        for (i in 0 until length) {
            val element = java.lang.reflect.Array.get(obj, i)
            result[i] = if (element != null) {
                converter.toConfig(singleType, element, parameterizedType)
            } else {
                null
            }
        }

        return result
    }

    override fun fromConfig(type: Class<*>, obj: Any, parameterizedType: ParameterizedType?): Any {
        val singleType = type.componentType ?: return obj

        val values: List<Any?> =
            obj as? List<*> ?: (obj as Array<*>).toList()

        val result = java.lang.reflect.Array.newInstance(
            singleType,
            values.size
        )

        @Suppress("UNCHECKED_CAST")
        val converter = internalConverter.getConverter(singleType) as? Converter<Any, Any>
            ?: return values.toTypedArray()

        for (i in values.indices) {
            val value = values[i]
            val converted = if (value != null) {
                converter.fromConfig(singleType, value, parameterizedType)
            } else {
                null
            }
            java.lang.reflect.Array.set(result, i, converted)
        }

        return result
    }

    override fun supports(type: Class<*>): Boolean {
        return type.isArray
    }
}