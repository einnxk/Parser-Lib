package com.github.einnxk.env

import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.common.interfaces.Config
import com.github.einnxk.env.mapper.EnvMapper
import org.jetbrains.annotations.NotNull
import java.io.File

/**
 * EnvConfig is the abstract base for any .env-backed configuration class.
 * Extend this class and define your fields - only primitives and String
 * are supported.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
abstract class EnvConfig : EnvMapper(), Config {

    @Throws(InvalidConfigurationException::class)
    override fun save() {
        requireNotNull(configFile) { "configFile is not set" }
        try {
            @Suppress("UNCHECKED_CAST")
            saveToEnv(saveToMap(javaClass as Class<Any>))
        } catch (e: Exception) {
            throw InvalidConfigurationException("Could not save .env config", e)
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
        }
        @Suppress("UNCHECKED_CAST")
        loadMap(loadFromEnv(), javaClass as Class<Any>)
    }

    @Throws(InvalidConfigurationException::class)
    override fun init(@NotNull file: File) {
        configFile = file
        init()
    }

    @Throws(InvalidConfigurationException::class)
    override fun reload() {
        @Suppress("UNCHECKED_CAST")
        loadMap(loadFromEnv(), javaClass as Class<Any>)
    }

    @Throws(InvalidConfigurationException::class)
    override fun load() {
        requireNotNull(configFile) { "configFile is not set" }
        strictLoad = true
        try {
            @Suppress("UNCHECKED_CAST")
            loadMap(loadFromEnv(), javaClass as Class<Any>)
        } finally {
            strictLoad = false
        }
    }

    @Throws(InvalidConfigurationException::class)
    override fun load(@NotNull file: File) {
        configFile = file
        load()
    }
}