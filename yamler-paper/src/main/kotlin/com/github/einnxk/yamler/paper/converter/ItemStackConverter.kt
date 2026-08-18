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
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus
import java.lang.reflect.ParameterizedType

/**
 * The ItemStackConverter is a part of the default paper provided converters. Here
 * we convert the ItemStack from Bukkit into the Config File and parse one from it.
 *
 * @author EinNik
 * @since 3.0.0
 */
@ApiStatus.Experimental
@ApiStatus.AvailableSince("4.2.0")
open class ItemStackConverter(private val internalConverter: InternalConverter) : Converter<ItemStack, Any> {

    override fun toConfig(type: Class<*>, obj: ItemStack, parameterizedType: ParameterizedType?): Any {
        val map: MutableMap<String, Any?> = mutableMapOf()

        @Suppress("UNCHECKED_CAST")
        val listConverter = internalConverter.getConverter(List::class.java) as? Converter<Any, Any>
            ?: throw IllegalStateException("Internal converter could not be found")

        @Suppress("UNCHECKED_CAST")
        val componentConverter = internalConverter.getConverter(Component::class.java) as? Converter<Component, Any>
            ?: throw IllegalStateException("No converter found for ${Component::class.java.canonicalName}")

        map["type"] = obj.type
        map["amount"] = obj.amount
        map["durability"] = obj.durability
        map["enchants"] = listConverter.toConfig(List::class.java, obj.enchantments, null)

        val itemMeta = obj.itemMeta ?: return map
        val metaMap: MutableMap<String, Any?> = mutableMapOf()

        val displayName = itemMeta.displayName()
        if (displayName != null) {
            metaMap["name"] = componentConverter.toConfig(Component::class.java, displayName, null)
        }

        val lore = itemMeta.lore()
        if (lore != null) {
            metaMap["lore"] = lore.map { line ->
                componentConverter.toConfig(Component::class.java, line, null)
            }
        }

        map["meta"] = metaMap
        return map
    }

    @Suppress("UNCHECKED_CAST")
    override fun fromConfig(type: Class<*>, obj: Any, parameterizedType: ParameterizedType?): ItemStack {
        val map = when (obj) {
            is Map<*, *> -> obj as Map<String, Any?>
            is ConfigSection -> obj.getRawMap() as Map<String, Any?>
            else -> throw IllegalArgumentException("Cannot convert ${obj.javaClass} into an ItemStack")
        }

        val material = map["type"] as Material
        val amount = (map["amount"] as Number).toInt()

        val itemStack = ItemStack(material).apply {
            this.amount = amount
            this.durability = (map["durability"] as? Number)?.toShort() ?: 0
        }

        val metaMap = when (val rawMeta = map["meta"]) {
            is Map<*, *> -> rawMeta as Map<String, Any?>
            is ConfigSection -> rawMeta.getRawMap() as Map<String, Any?>
            else -> null
        }

        if (metaMap != null) {
            val itemMeta = itemStack.itemMeta ?: return itemStack

            val componentConverter = internalConverter.getConverter(Component::class.java) as? Converter<Component, Any>
                ?: throw IllegalStateException("No converter found for ${Component::class.java.canonicalName}")

            val name = metaMap["name"]
            if (name != null) {
                itemMeta.displayName(componentConverter.fromConfig(Component::class.java, name, null))
            }

            val lore = metaMap["lore"] as? List<*>
            if (lore != null) {
                itemMeta.lore(lore.mapNotNull { line ->
                    line?.let { componentConverter.fromConfig(Component::class.java, it, null) }
                })
            }

            itemStack.itemMeta = itemMeta
        }

        return itemStack
    }

    override fun supports(type: Class<*>): Boolean {
        return ItemStack::class.java.isAssignableFrom(type)
    }
}