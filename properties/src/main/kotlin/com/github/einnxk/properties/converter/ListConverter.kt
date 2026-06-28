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

import java.lang.reflect.ParameterizedType

class ListConverter(private val internalConverter: PropertiesInternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val list = obj as List<*>

        return list.joinToString(",") { value ->
            if (value == null) {
                ""
            } else {
                val converter = internalConverter.getConverter(value.javaClass)

                converter?.toConfig(
                    value.javaClass,
                    value,
                    null
                )?.toString() ?: value.toString()
            }
        }
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val result =
            try {
                type?.getDeclaredConstructor()?.newInstance() as MutableList<Any?>
            } catch (_: Exception) {
                mutableListOf()
            }
        val value = obj?.toString() ?: return result

        if (value.isBlank())
            return result

        val values = value.split(",")
        if (
            parameterizedType != null &&
            parameterizedType.actualTypeArguments[0] is Class<*>
        ) {

            val elementType =
                parameterizedType.actualTypeArguments[0] as Class<*>
            val converter =
                internalConverter.getConverter(elementType)

            for (entry in values) {
                result.add(
                    converter?.fromConfig(
                        elementType,
                        entry.trim(),
                        null
                    ) ?: entry.trim()
                )
            }

        } else {
            result.addAll(values.map { it.trim() })
        }

        return result
    }

    override fun supports(type: Class<*>): Boolean {
        return List::class.java.isAssignableFrom(type)
    }
}