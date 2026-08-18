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
package com.github.einnxk.properties.mapper

import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.properties.bootstrap.ConfigBase
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Properties

/**
 * The bootstrap which handles all the writing and reading. We read the Properties file,
 * save comments as metadata and map fields to/from a Properties.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
open class BaseConfigMapper : ConfigBase() {

    @Transient
    val properties: Properties = Properties()
    @Transient
    private val comments: MutableMap<String, MutableList<String>> = LinkedHashMap()

    init {
        serializeConfigurationFromAnnotation()
    }

    protected fun loadFromProperties() {
        properties.clear()

        try {
            InputStreamReader(
                FileInputStream(configFile!!),
                Charsets.UTF_8
            ).use { reader ->
                properties.load(reader)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Throws(InvalidConfigurationException::class)
    protected fun saveToProperties() {
        try {
            OutputStreamWriter(
                FileOutputStream(configFile!!),
                Charsets.UTF_8
            ).use { writer ->

                configHeader?.forEach {
                    writer.write("# $it\n")
                }

                if (!configHeader.isNullOrEmpty()) {
                    writer.write("\n")
                }

                val keys = properties.stringPropertyNames().sorted()

                for (key in keys) {

                    comments[key]?.forEach {
                        writer.write("# $it\n")
                    }

                    val value = properties.getProperty(key) ?: ""
                    writer.write("$key=$value\n")
                }
            }
        } catch (e: IOException) {
            throw InvalidConfigurationException("Could not save Properties", e)
        }
    }

    protected fun addComment(path: String, comment: String) {
        val list = comments.getOrPut(path) {
            mutableListOf()
        }
        list.add(comment)
    }
}