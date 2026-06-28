package de.einnik.yamler_v3.paper.converter

import de.einnik.yamler_v3.core.converter.Converter
import de.einnik.yamler_v3.core.converter.InternalConverter
import de.einnik.yamler_v3.core.section.ConfigSection
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import java.lang.reflect.ParameterizedType

/**
 * The BlockConverter is a part of the default paper provided converters. Here
 * we convert the Block from Bukkit into the Config File and parse one from it.
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
open class BlockConverter(private val internalConverter: InternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val block: Block = obj as Block
        val locationConverter: Converter = internalConverter.getConverter(Location::class.java)
            ?: throw IllegalStateException("Could not find converter for ${obj.javaClass.canonicalName}")

        val saveMap: MutableMap<String, Any> = mutableMapOf()
        saveMap["type"] = block.type
        saveMap["location"] = locationConverter.toConfig(Location::class.java, block.location, null)!!

        return saveMap
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val blockMap = (obj as ConfigSection).getRawMap() as MutableMap<String?, Any?>
        val locationMap = (blockMap["location"] as ConfigSection).getRawMap() as MutableMap<String?, Any?>

        val location = Location(
            Bukkit.getWorld(locationMap["world"] as String),
            locationMap["x"] as Double,
            locationMap["y"] as Double,
            locationMap["z"] as Double
        )
        val block = location.block

        block.type = blockMap["type"] as Material

        return block
    }

    override fun supports(type: Class<*>): Boolean {
        return Block::class.java.isAssignableFrom(type)
    }
}