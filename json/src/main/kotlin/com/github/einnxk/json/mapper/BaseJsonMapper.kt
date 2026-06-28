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
package com.github.einnxk.json.mapper

import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.json.bootstrap.ConfigBase
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.*

/**
 * The bootstrap which handles all the writing and reading. We read the JSON file,
 * save comments as metadata and map fields to/from a JsonObject using Gson.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
open class BaseJsonMapper : ConfigBase() {

    @Transient
    private val gson = GsonBuilder().setPrettyPrinting().create()

    init {
        serializeConfigurationFromAnnotation()
    }

    protected fun loadFromJson(): JsonObject {
        return try {
            InputStreamReader(FileInputStream(configFile!!), Charsets.UTF_8).use { reader ->
                JsonParser.parseReader(reader).asJsonObject
            }
        } catch (e: IOException) {
            throw InvalidConfigurationException("Could not load JSON", e)
        }
    }

    protected fun saveToJson(jsonObject: JsonObject) {
        try {
            OutputStreamWriter(FileOutputStream(configFile!!), Charsets.UTF_8).use { writer ->
                writer.write(gson.toJson(jsonObject))
            }
        } catch (e: IOException) {
            throw InvalidConfigurationException("Could not save JSON", e)
        }
    }
}