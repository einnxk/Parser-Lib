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
package com.github.einnxk.properties.converter

import java.lang.reflect.Array
import java.lang.reflect.ParameterizedType

class ArrayConverter(private val internalConverter: PropertiesInternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val component = type!!.componentType
        val converter =
            internalConverter.getConverter(component)
        val length = Array.getLength(obj)
        val values = ArrayList<String>(length)

        for (i in 0 until length) {
            val value = Array.get(obj, i)
            values += converter?.toConfig(
                component,
                value,
                null
            )?.toString() ?: value.toString()
        }

        return values.joinToString(",")
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val component = type!!.componentType
        val converter =
            internalConverter.getConverter(component)
        val values =
            obj.toString()
                .split(",")
                .map { it.trim() }
        val array =
            Array.newInstance(
                component,
                values.size
            )

        for (i in values.indices) {
            val value =
                converter?.fromConfig(
                    component,
                    values[i],
                    null
                ) ?: values[i]
            Array.set(array, i, value)
        }

        return array
    }

    override fun supports(type: Class<*>): Boolean {
        return type.isArray
    }
}