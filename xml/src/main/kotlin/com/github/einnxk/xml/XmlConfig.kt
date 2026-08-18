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
package com.github.einnxk.xml

import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.common.interfaces.Config
import com.github.einnxk.xml.mapper.XmlMapper
import java.io.File

/**
 * XmlConfig is the abstract base for any XML-backed configuration class.
 * Extend this class and define your fields - they will be automatically
 * mapped to and from an XML file on disk.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
@Suppress("UNCHECKED_CAST")
abstract class XmlConfig : XmlMapper(), Config {

    @Throws(InvalidConfigurationException::class)
    override fun save() {
        requireNotNull(configFile) { "configFile is not set" }
        try {
            val node = saveToMap(javaClass as Class<Any>)
            saveToXml(node)
        } catch (e: Exception) {
            throw InvalidConfigurationException("Could not save XML config", e)
        }
    }

    @Throws(InvalidConfigurationException::class)
    override fun save(file: File) {
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
        loadMap(loadFromXml(), javaClass as Class<Any>)
    }

    @Throws(InvalidConfigurationException::class)
    override fun init(file: File) {
        configFile = file
        init()
    }

    @Throws(InvalidConfigurationException::class)
    override fun reload() {
        loadMap(loadFromXml(), javaClass as Class<Any>)
    }

    @Throws(InvalidConfigurationException::class)
    override fun load() {
        requireNotNull(configFile) { "configFile is not set" }
        strictLoad = true
        try {
            loadMap(loadFromXml(), javaClass as Class<Any>)
        } finally {
            strictLoad = false
        }
    }

    @Throws(InvalidConfigurationException::class)
    override fun load(file: File) {
        configFile = file
        load()
    }
}