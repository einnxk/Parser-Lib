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

import com.github.einnxk.properties.PropertiesConfig
import java.lang.reflect.ParameterizedType

class ConfigConverter(private val internalConverter: PropertiesInternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        return obj!!
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {

        val clazz = requireNotNull(type)

        val config = newInstance(clazz) as PropertiesConfig

        for (converter in internalConverter.customConverters()) {
            internalConverter.addConverter(converter)
        }

        return config
    }

    private fun newInstance(type: Class<*>): Any {
        val enclosingClass = type.enclosingClass

        return if (enclosingClass != null) {
            val parent = newInstance(enclosingClass)
            type.getDeclaredConstructor(enclosingClass)
                .newInstance(parent)

        } else {
            type.getDeclaredConstructor()
                .newInstance()
        }
    }

    override fun supports(type: Class<*>): Boolean {
        return PropertiesConfig::class.java.isAssignableFrom(type)
    }
}