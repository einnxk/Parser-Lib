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
 * The base class, which allows fields in the [com.github.einnxk.yamler.core.YamlConfig] to be
 * converted into the file and back from it.
 *
 * Yamler contains a large library of default converters which allows to convert `Arrays`, `Enums`,
 * `Lists`, `Maps`, `Primitive Types`, `ConfigSections`, `Sets`.
 *
 * @param T the type of the class we want to convert
 * @param R the return type we converted into. As an example in most default converters this
 *          is set to `Map<String, Object>`
 * @author EinNik
 * @since 4.2.0
 *
 * @see ArrayConverter
 * @see ConfigConverter
 * @see EnumConverter
 * @see ListConverter
 * @see MapConverter
 * @see PrimitiveConverter
 * @see SectionConverter
 * @see SetConverter
 */
interface Converter<T, R> {

    /**
     * This method is called when the [com.github.einnxk.yamler.core.YamlConfig] is saved. In this method the
     * type should be converted into [R] from the interface declaration.
     *
     * @param type the type of the field
     * @param obj the object [T] from the interface declaration
     * @param parameterizedType the type of the method or else null
     *
     * @return the finished converted return type [R] from the interface declaration
     * @throws Exception when an error occurs during the insertion into the [com.github.einnxk.yamler.core.YamlConfig]
     */
    @Throws(Exception::class)
    fun toConfig(type: Class<*>, obj: T, parameterizedType: ParameterizedType?): R

    /**
     * This method is called when a field from the [com.github.einnxk.yamler.core.YamlConfig] should parsed. In this method
     * the return type from the [toConfig] method is parsed into the [T] from the interface declaration.
     *
     * @param type the type of the field
     * @param obj the object [R] from the interface declaration
     * @param parameterizedType the type of the method or else null
     *
     * @return the finished converted return type [T] from the interface declaration
     * @throws Exception when an error occurs during the retrieve into the [com.github.einnxk.yamler.core.YamlConfig]
     */
    @Throws(Exception::class)
    fun fromConfig(type: Class<*>, obj: R, parameterizedType: ParameterizedType?): T
}