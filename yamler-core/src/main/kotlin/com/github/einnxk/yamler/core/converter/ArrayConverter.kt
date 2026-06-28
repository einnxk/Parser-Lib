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
 * The MapConverter is part of the default conversion library. This class
 * makes it able to convert arrays into YAML and back into an Array.
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
class ArrayConverter(private val internalConverter: InternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        val singleType = type?.componentType ?: return obj

        val converter = internalConverter.getConverter(singleType)
            ?: return obj

        val length = java.lang.reflect.Array.getLength(obj)
        val result = arrayOfNulls<Any?>(length)

        for (i in 0 until length) {
            result[i] = converter.toConfig(
                singleType,
                java.lang.reflect.Array.get(obj, i),
                parameterizedType
            )
        }

        return result
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        val singleType = type?.componentType ?: return obj

        val values: List<Any?> =
            obj as? List<*> ?: (obj as Array<*>).toList()

        val result = java.lang.reflect.Array.newInstance(
            singleType,
            values.size
        )

        val converter = internalConverter.getConverter(singleType) ?: return values.toTypedArray()

        for (i in values.indices) {
            java.lang.reflect.Array.set(
                result,
                i,
                converter.fromConfig(
                    singleType,
                    values[i],
                    parameterizedType
                )
            )
        }

        return result
    }

    override fun supports(type: Class<*>): Boolean {
        return type.isArray
    }
}