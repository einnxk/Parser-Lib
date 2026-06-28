package de.einnik.yamler_v3.paper.converter

import de.einnik.yamler_v3.core.converter.Converter
import de.einnik.yamler_v3.core.converter.InternalConverter
import de.einnik.yamler_v3.core.section.ConfigSection
import org.bukkit.Bukkit
import org.bukkit.Location
import java.lang.reflect.ParameterizedType

/**
 * The LocationConverter is a part of the default paper provided converters. Here
 * we convert the Location from Bukkit into the Config File and parse one from it.
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
open class LocationConverter(private val internalConverter: InternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        val location = obj as Location
        val saveMap: MutableMap<String?, Any?> = HashMap()
        saveMap["world"] = location.getWorld().name
        saveMap["x"] = location.x
        saveMap["y"] = location.y
        saveMap["z"] = location.z
        saveMap["yaw"] = location.yaw
        saveMap["pitch"] = location.pitch

        return saveMap
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        val locationMap: MutableMap<String?, Any?>?
        if (obj is MutableMap<*, *>) {
            locationMap = obj as MutableMap<String?, Any?>?
        } else {
            locationMap = (obj as ConfigSection).getRawMap() as MutableMap<String?, Any?>
        }

        val yaw: Float
        if (locationMap!!["yaw"] is Double) {
            val dYaw = locationMap["yaw"] as Double
            yaw = dYaw.toFloat()
        } else {
            yaw = locationMap["yaw"] as Float
        }

        val pitch: Float
        if (locationMap["pitch"] is Double) {
            val dPitch = locationMap["pitch"] as Double
            pitch = dPitch.toFloat()
        } else {
            pitch = locationMap["pitch"] as Float
        }

        return Location(
            Bukkit.getWorld((locationMap["world"] as String?)!!),
            (locationMap["x"] as Double?)!!,
            (locationMap["y"] as Double?)!!,
            (locationMap["z"] as Double?)!!,
            yaw,
            pitch
        )
    }

    override fun supports(type: Class<*>): Boolean {
        return Location::class.java.isAssignableFrom(type)
    }
}