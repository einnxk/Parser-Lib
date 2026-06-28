package com.github.einnxk.toml

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.common.interfaces.Config
import com.github.einnxk.toml.mapper.TomlMapper
import org.jetbrains.annotations.NotNull
import java.io.File

/**
 * TomlConfig is the abstract base for any TOML-backed configuration class.
 * Extend this class and define your fields - they will be automatically
 * mapped to and from a TOML file on disk.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
abstract class TomlConfig : TomlMapper(), Config {

    @Throws(InvalidConfigurationException::class)
    override fun save() {
        requireNotNull(configFile) { "configFile is not set" }
        try {
            @Suppress("UNCHECKED_CAST")
            val node = saveToMap(javaClass as Class<Any>)
            saveToToml(node)
        } catch (e: Exception) {
            throw InvalidConfigurationException("Could not save TOML config", e)
        }
    }

    @Throws(InvalidConfigurationException::class)
    override fun save(@NotNull file: File) {
        configFile = file
        save()
    }

    @Throws(InvalidConfigurationException::class)
    override fun init() {
        requireNotNull(configFile) { "configFile is not set" }
        if (!configFile!!.exists()) {
            configFile!!.parentFile?.mkdirs()
            configFile!!.createNewFile()
            save()
        } else {
            load()
        }
    }

    @Throws(InvalidConfigurationException::class)
    override fun init(@NotNull file: File) {
        configFile = file
        init()
    }

    @Throws(InvalidConfigurationException::class)
    override fun reload() {
        requireNotNull(configFile) { "configFile is not set" }
        load()
    }

    @Throws(InvalidConfigurationException::class)
    override fun load() {
        requireNotNull(configFile) { "configFile is not set" }
        try {
            val node: ObjectNode = loadFromToml()
            @Suppress("UNCHECKED_CAST")
            loadMap(node, javaClass as Class<Any>)
        } catch (e: InvalidConfigurationException) {
            throw e
        } catch (e: Exception) {
            throw InvalidConfigurationException("Could not load TOML config", e)
        }
    }

    @Throws(InvalidConfigurationException::class)
    override fun load(@NotNull file: File) {
        configFile = file
        load()
    }
}