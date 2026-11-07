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

  private val types: Set<Class<*>> = listOf(
    Boolean::class,
    Char::class,
    Byte::class,
    Short::class,
    Int::class,
    Long::class,
    Float::class,
    Double::class
  ).flatMap { listOf(it.javaPrimitiveType!!, it.javaObjectType) }.toSet()

    override fun toConfig(type: Class<*>, obj: Any, parameterizedType: ParameterizedType?): Any {
        return obj
    }

    override fun fromConfig(type: Class<*>, obj: Any, parameterizedType: ParameterizedType?): Any {
      return when (type.kotlin.javaObjectType) {
        Byte::class.javaObjectType -> if (obj is Number) obj.toByte() else obj
        Short::class.javaObjectType -> if (obj is Number) obj.toShort() else obj
        Int::class.javaObjectType -> if (obj is Number) obj.toInt() else obj
        Long::class.javaObjectType -> if (obj is Number) obj.toLong() else obj
        Float::class.javaObjectType -> if (obj is Number) obj.toFloat() else obj
        Double::class.javaObjectType -> if (obj is Number) obj.toDouble() else obj

        Char::class.javaObjectType -> when (obj) {
          is Char -> obj
          is String -> if (obj.isNotEmpty()) obj.first() else obj
          is Number -> obj.toInt().toChar()
          else -> obj
        }

        else -> obj
      }
    }

    override fun supports(type: Class<*>): Boolean {
        return types.contains(type)
    }
}
