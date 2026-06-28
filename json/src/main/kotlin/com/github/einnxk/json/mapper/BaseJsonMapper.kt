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

    @Transient
    private val comments: MutableMap<String, MutableList<String>> = LinkedHashMap()

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
                configHeader?.let { header ->
                    for (line in header) {
                        writer.write("// $line\n")
                    }
                    writer.write("\n")
                }

                writer.write(gson.toJson(jsonObject))
            }
        } catch (e: IOException) {
            throw InvalidConfigurationException("Could not save JSON", e)
        }
    }

    fun addComment(key: String, value: String) {
        comments.getOrPut(key) { mutableListOf() }.add(value)
    }

    fun clearComments() {
        comments.clear()
    }

    protected fun getComments(): Map<String, MutableList<String>> = comments
}