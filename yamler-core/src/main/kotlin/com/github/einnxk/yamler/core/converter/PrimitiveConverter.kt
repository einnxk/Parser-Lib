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
 * The PrimitiveConverter is part of the default [Converter] library. This class
 * makes it able to convert primitive datatypes into YAML and back into a primitive
 * datatype.
 *
 * @author EinNik
 * @since 3.0.0
 */
open class PrimitiveConverter : Converter<Any, Any> {

    private val types: Set<String> = setOf(
        "boolean",
        "char",
        "byte",
        "short",
        "int",
        "float",
        "long",
        "double"
    )

    override fun toConfig(type: Class<*>, obj: Any, parameterizedType: ParameterizedType?): Any {
        return obj
    }

    override fun fromConfig(type: Class<*>, obj: Any, parameterizedType: ParameterizedType?): Any {
        return when (type) {
            java.lang.Short.TYPE -> when (obj) {
                is Short -> obj
                is Int -> obj.toShort()
                else -> obj
            }

            java.lang.Byte.TYPE -> when (obj) {
                is Byte -> obj
                is Int -> obj.toByte()
                else -> obj
            }

            java.lang.Float.TYPE -> when (obj) {
                is Float -> obj
                is Int -> obj.toFloat()
                is Double -> obj.toFloat()
                else -> obj
            }

            Character.TYPE -> when (obj) {
                is Char -> obj
                is String -> obj.first()
                else -> obj
            }

            else -> obj
        }
    }

    override fun supports(type: Class<*>): Boolean {
        return types.contains(type.name)
    }
}