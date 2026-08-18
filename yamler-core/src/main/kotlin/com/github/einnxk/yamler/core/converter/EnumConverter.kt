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
 * Part of the default yamler [Converter] library. Which automatically converts Enums through
 * their name.
 *
 * @author EinNik
 * @since 4.1.0
 *
 * @see Converter
 */
open class EnumConverter : Converter<Enum<*>, String> {

    override fun toConfig(type: Class<*>, obj: Enum<*>, parameterizedType: ParameterizedType?): String {
        return obj.name
    }

    @Suppress("UNCHECKED_CAST")
    override fun fromConfig(type: Class<*>, obj: String, parameterizedType: ParameterizedType?) : Enum<*> {
        val enumClass = type as Class<out Enum<*>>

        return java.lang.Enum.valueOf(enumClass, obj)
    }

    override fun supports(type: Class<*>): Boolean {
        return type.isEnum
    }
}