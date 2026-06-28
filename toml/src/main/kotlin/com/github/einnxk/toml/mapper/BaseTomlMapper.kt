package com.github.einnxk.toml.mapper

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.toml.bootstrap.ConfigBase
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * The bootstrap which handles all the writing and reading of TOML files.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
open class BaseTomlMapper : ConfigBase() {

    @Transient
    protected val tomlMapper: TomlMapper = TomlMapper()

    init {
        serializeConfigurationFromAnnotation()
    }

    protected fun loadFromToml(): ObjectNode {
        return try {
            InputStreamReader(FileInputStream(configFile!!), Charsets.UTF_8).use { reader ->
                val node = tomlMapper.readTree(reader)
                if (node == null || node.isNull || node.isMissingNode) {
                    tomlMapper.createObjectNode()
                } else {
                    node as ObjectNode
                }
            }
        } catch (e: IOException) {
            throw InvalidConfigurationException("Could not load TOML", e)
        }
    }

    protected fun saveToToml(node: ObjectNode) {
        try {
            OutputStreamWriter(FileOutputStream(configFile!!), Charsets.UTF_8).use { writer ->
                tomlMapper.writerWithDefaultPrettyPrinter().writeValue(writer, node)
            }
        } catch (e: IOException) {
            throw InvalidConfigurationException("Could not save TOML", e)
        }
    }
}