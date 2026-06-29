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
package com.github.einnxk.hocon.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.hocon.bootstrap.ConfigBase
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.ConfigValueFactory
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter

/**
 * The bootstrap which handles all the writing and reading of HOCON files.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
open class BaseHoconMapper : ConfigBase() {

    @Transient
    protected val objectMapper: ObjectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
    }

    @Transient
    internal var strictLoad: Boolean = false

    init {
        serializeConfigurationFromAnnotation()
    }

    protected fun loadFromHocon(): ObjectNode {
        return try {
            val config = ConfigFactory.parseFile(configFile!!)
            val json = config.root().render(
                ConfigRenderOptions.concise().setJson(true)
            )
            if (json.isBlank() || json == "{}") {
                objectMapper.createObjectNode()
            } else {
                objectMapper.readValue(json, ObjectNode::class.java)
            }
        } catch (e: IOException) {
            throw InvalidConfigurationException("Could not load HOCON", e)
        }
    }

    protected fun saveToHocon(node: ObjectNode) {
        try {
            @Suppress("UNCHECKED_CAST")
            val map = objectMapper.convertValue(node, Map::class.java) as Map<String, Any>
            val config = ConfigValueFactory.fromMap(map).toConfig()
            val rendered = config.root().render(
                ConfigRenderOptions.defaults()
                    .setComments(true)
                    .setOriginComments(false)
                    .setJson(false)
                    .setFormatted(true)
            )

            OutputStreamWriter(FileOutputStream(configFile!!), Charsets.UTF_8).use { writer ->
                configHeader?.let { header ->
                    header.forEach { writer.write("# $it\n") }
                    writer.write("\n")
                }
                writer.write(rendered)
            }
        } catch (e: IOException) {
            throw InvalidConfigurationException("Could not save HOCON", e)
        }
    }
}