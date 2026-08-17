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
package com.github.einnxk.yamler.paper.converter

import com.github.einnxk.yamler.core.converter.Converter
import com.github.einnxk.yamler.core.converter.InternalConverter
import com.github.einnxk.yamler.core.section.ConfigSection
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.jetbrains.annotations.ApiStatus
import java.lang.reflect.ParameterizedType
import kotlin.collections.get

/**
 * The MiniMessageComponentConverter is a part of the default paper provided
 * converters. Here we convert Components from Paper into the Config File
 * and parse one from it.
 *
 * @author EinNik
 * @since 3.0.0
 */
open class MiniMessageComponentConverter : Converter<Component, Map<*, *>> {

    override fun toConfig(type: Class<*>, obj: Component, parameterizedType: ParameterizedType?): Map<*, *> {
        val component: Component = obj as Component

        val saveMap: MutableMap<String, Any> = mutableMapOf()
        saveMap["text-decoration"] =
            component.decorations().mapKeys { it.key.name }
                .mapValues { it.value.name }

        saveMap["content"] = MiniMessage.miniMessage().serialize(component)

        return saveMap
    }

    override fun fromConfig(type: Class<*>, obj: Map<*, *>, parameterizedType: ParameterizedType?): Component {
        val map: Map<*, *> = when (obj) {
            else -> obj
        }

        val content = map["content"] as? String
            ?: throw IllegalStateException("Missing component content")

        val component = MiniMessage.miniMessage().deserialize(content)

        val decorationsRaw = map["text-decoration"] as? Map<*, *>

        if (decorationsRaw != null) {
            val converted = decorationsRaw.mapNotNull { (key, value) ->
                val decoration = key?.toString()?.let {
                    runCatching { TextDecoration.valueOf(it) }.getOrNull()
                }

                val state = when (value) {
                    is Boolean ->
                        if (value) TextDecoration.State.TRUE
                        else TextDecoration.State.FALSE

                    is String ->
                        runCatching {
                            TextDecoration.State.valueOf(value)
                        }.getOrNull()

                    else -> null
                }

                if (decoration != null && state != null) {
                    decoration to state
                } else {
                    null
                }
            }.toMap()

            return component.decorations(converted)
        }

        return component
    }

    override fun supports(type: Class<*>): Boolean {
        return Component::class.java == type
    }
}