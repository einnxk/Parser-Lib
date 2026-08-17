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
import com.github.einnxk.yamler.core.section.ConfigSection
import org.bukkit.Bukkit
import org.bukkit.Location
import java.lang.reflect.ParameterizedType

/**
 * The LocationConverter is a part of the default paper provided converters. Here
 * we convert the Location from Bukkit into the Config File and parse one from it.
 *
 * @author EinNik
 * @since 3.0.0
 */
open class LocationConverter : Converter<Location, Any> {

    override fun toConfig(type: Class<*>, obj: Location, parameterizedType: ParameterizedType?): Any {
        val saveMap: MutableMap<String?, Any?> = HashMap()
        saveMap["world"] = obj.getWorld().name
        saveMap["x"] = obj.x
        saveMap["y"] = obj.y
        saveMap["z"] = obj.z
        saveMap["yaw"] = obj.yaw
        saveMap["pitch"] = obj.pitch

        return saveMap
    }

    @Suppress("UNCHECKED_CAST")
    override fun fromConfig(type: Class<*>, obj: Any, parameterizedType: ParameterizedType?): Location {
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