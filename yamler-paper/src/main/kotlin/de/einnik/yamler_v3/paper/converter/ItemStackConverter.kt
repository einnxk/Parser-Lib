package de.einnik.yamler_v3.paper.converter

import de.einnik.yamler_v3.core.converter.Converter
import de.einnik.yamler_v3.core.converter.InternalConverter
import de.einnik.yamler_v3.core.section.ConfigSection
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus
import java.lang.reflect.ParameterizedType

/**
 * The ItemStackConverter is a part of the default paper provided converters. Here
 * we convert the ItemStack from Bukkit into the Config File and parse one from it.
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
@Deprecated(message = "Newer version peding")
@ApiStatus.ScheduledForRemoval(inVersion = "MC 26.1")
open class ItemStackConverter(private val internalConverter: InternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        val itemStack: ItemStack = obj as ItemStack
        val map: MutableMap<String, Any?> = mutableMapOf()
        val listConverter: Converter = internalConverter.getConverter(List::class.java)
            ?: throw IllegalStateException("Internal converter could not be found")

        map["type"] = itemStack.type
        map["amount"] = itemStack.amount
        map["durability"] = itemStack.durability
        map["enchants"] = itemStack.enchantments.let {
            listConverter.toConfig(List::class.java, it, null)
        }

        if (itemStack.itemMeta == null) {
            return map
        }

        val itemMeta = itemStack.itemMeta
        val metaMap: MutableMap<String, Any?> = mutableMapOf()
        metaMap["name"] = itemMeta.displayName
        metaMap["lore"] = itemMeta.lore.let {
            listConverter.toConfig(List::class.java, it, null)
        }

        map["meta"] = metaMap
        return map
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        val map = when (obj) {
            is Map<*, *> -> obj as Map<String, Any?>
            is ConfigSection -> obj.getRawMap() as Map<String, Any?>
            else -> return null
        }

        val material = map["type"] as Material
        val amount = (map["amount"] as Number).toInt()

        val itemStack = ItemStack(material).apply {
            this.amount = amount
            this.durability = (map["durability"] as? Number)?.toShort() ?: 0
        }

        val metaMap = map["meta"] as? Map<String, Any?>
        val listConverter = internalConverter.getConverter(List::class.java)
            ?: throw IllegalStateException("Internal converter could not be found")

        if (metaMap != null) {
            val itemMeta = itemStack.itemMeta ?: return itemStack

            val name = metaMap["name"] as? String
            if (name != null) {
                itemMeta.setDisplayName(name)
            }

            val lore = metaMap["lore"]
            if (lore != null) {
                itemMeta.lore = listConverter.fromConfig(
                    List::class.java,
                    lore,
                    null
                ) as? List<String>
            }

            itemStack.itemMeta = itemMeta
        }

        return itemStack
    }

    override fun supports(type: Class<*>): Boolean {
        return ItemStack::class.java.isAssignableFrom(type)
    }
}