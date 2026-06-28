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
 * makes it able to convert a set into YAML and back into a Set.
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
class SetConverter(private val internalConverter: InternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        val values = obj as Set<*>
        val newList = ArrayList<Any?>()

        for (value in values) {
            if (value == null) {
                newList.add(null)
                continue
            }

            val converter = internalConverter.getConverter(value.javaClass)

            if (converter != null) {
                newList.add(
                    converter.toConfig(
                        value.javaClass,
                        value,
                        null
                    )
                )
            } else {
                newList.add(value)
            }
        }

        return newList
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        @Suppress("UNCHECKED_CAST")
        val values = obj as List<Any?>

        var newSet: MutableSet<Any?> = HashSet()

        try {
            @Suppress("UNCHECKED_CAST")
            newSet = type?.getDeclaredConstructor()?.newInstance() as MutableSet<Any?>
        } catch (_: Exception) {
        }

        if (
            parameterizedType != null &&
            parameterizedType.actualTypeArguments[0] is Class<*>
        ) {
            val elementType =
                parameterizedType.actualTypeArguments[0] as Class<*>

            val converter = internalConverter.getConverter(elementType)

            if (converter != null) {
                for (value in values) {
                    newSet.add(
                        converter.fromConfig(
                            elementType,
                            value,
                            null
                        )
                    )
                }
            } else {
                newSet.addAll(values)
            }
        } else {
            newSet.addAll(values)
        }

        return newSet
    }

    override fun supports(type: Class<*>): Boolean {
        return Set::class.java.isAssignableFrom(type)
    }
}