/*
 * Copyright 2026-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 */

package com.github.einnxk.properties.converter

import java.lang.reflect.ParameterizedType

/**
 * Base interface for all Properties converters.
 *
 * Every converter is responsible for converting an object
 * to a representation which can be stored inside a Properties
 * file and restoring it afterward.
 */
interface Converter {

    /**
     * Converts an object into a representation that can be
     * written into a Properties file.
     */
    @Throws(Exception::class)
    fun toConfig(
        type: Class<*>?,
        obj: Any?,
        parameterizedType: ParameterizedType?
    ): Any?

    /**
     * Restores an object from the value stored in the
     * Properties file.
     */
    @Throws(Exception::class)
    fun fromConfig(
        type: Class<*>?,
        obj: Any?,
        parameterizedType: ParameterizedType?
    ): Any?

    /**
     * Whether this converter supports the given type.
     */
    fun supports(type: Class<*>): Boolean
}