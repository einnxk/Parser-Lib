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

import com.github.einnxk.yamler.core.section.ConfigSection
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

/**
 * The SectionConverter is part of the default [Converter] library. This class
 * makes it able to convert whole [ConfigSection]'s into YAML and back into an [ConfigSection].
 *
 * @author EinNik
 * @since 4.2.9
 */
open class SectionConverter(private val internalConverter: InternalConverter) : Converter<Any, Any> {

  override fun toConfig(type: Class<*>, obj: Any, parameterizedType: ParameterizedType?): Any {
    val result = LinkedHashMap<Any, Any?>()

    for (field in getAllFields(obj.javaClass)) {
      if (isExcluded(field)) continue

      field.isAccessible = true
      val value = field.get(obj) ?: continue

      val converter = resolveConverter(field.type, value.javaClass)
      result[field.name] = converter?.toConfig(field.type, value, field.genericType as? ParameterizedType) ?: value
    }

    return result
  }

  @Suppress("UNCHECKED_CAST")
  override fun fromConfig(type: Class<*>, obj: Any, parameterizedType: ParameterizedType?): Any {
    val instance = type.getDeclaredConstructor().newInstance() as ConfigSection

    val rawMap: Map<Any, Any?> = when (obj) {
      is ConfigSection -> obj.getRawMap()
      is Map<*, *> -> obj as Map<Any, Any?>
      else -> return instance
    }

    val fieldsByName = getAllFields(type).associateBy { it.name }

    for ((key, value) in rawMap) {
      if (value == null) continue

      val field = fieldsByName[key.toString()] ?: continue
      if (isExcluded(field)) continue

      field.isAccessible = true
      val converter = resolveConverter(field.type, value.javaClass)
      val converted = converter?.fromConfig(field.type, value, field.genericType as? ParameterizedType) ?: value
      field.set(instance, converted)
    }

    return instance
  }

  override fun supports(type: Class<*>): Boolean {
    return ConfigSection::class.java.isAssignableFrom(type) && type != ConfigSection::class.java
  }

  @Suppress("UNCHECKED_CAST")
  private fun resolveConverter(declaredType: Class<*>, runtimeType: Class<*>): Converter<Any, Any>? {
    val converter = internalConverter.getConverter(declaredType) ?: internalConverter.getConverter(runtimeType)
    return converter as? Converter<Any, Any>
  }

  private fun isExcluded(field: Field): Boolean {
    val modifiers = field.modifiers
    return Modifier.isTransient(modifiers) || Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)
  }

  private fun getAllFields(clazz: Class<*>): List<Field> {
    val fields = mutableListOf<Field>()
    var current: Class<*>? = clazz

    while (current != null && current != ConfigSection::class.java && current != Any::class.java) {
      fields.addAll(current.declaredFields)
      current = current.superclass
    }

    return fields
  }
}
