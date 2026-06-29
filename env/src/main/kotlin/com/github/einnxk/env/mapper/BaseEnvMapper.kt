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
package com.github.einnxk.env.mapper

import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.env.bootstrap.ConfigBase
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.OutputStreamWriter

/**
 * The bootstrap which handles all the writing and reading of .env files.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
open class BaseEnvMapper : ConfigBase() {

    @Transient
    internal var strictLoad: Boolean = false

    @Transient
    private val envComments: MutableMap<String, MutableList<String>> = LinkedHashMap()

    init {
        serializeConfigurationFromAnnotation()
    }

    protected fun loadFromEnv(): Map<String, String> {
        return try {
            val result = LinkedHashMap<String, String>()
            BufferedReader(FileReader(configFile!!)).use { reader ->
                reader.lineSequence().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isBlank() || trimmed.startsWith("#")) return@forEach
                    val idx = trimmed.indexOf('=')
                    if (idx == -1) return@forEach
                    val key = trimmed.substring(0, idx).trim()
                    val value = trimmed.substring(idx + 1).trim()
                        .removeSurrounding("\"")
                        .removeSurrounding("'")
                    result[key] = value
                }
            }
            result
        } catch (e: IOException) {
            throw InvalidConfigurationException("Could not load .env", e)
        }
    }

    protected fun saveToEnv(values: Map<String, String>) {
        try {
            OutputStreamWriter(FileOutputStream(configFile!!), Charsets.UTF_8).use { writer ->
                configHeader?.let { header ->
                    header.forEach { writer.write("# $it\n") }
                    writer.write("\n")
                }

                values.forEach { (key, value) ->
                    envComments[key]?.forEach { comment ->
                        writer.write("# $comment\n")
                    }
                    writer.write("$key=$value\n")
                }
            }
        } catch (e: IOException) {
            throw InvalidConfigurationException("Could not save .env", e)
        }
    }

    protected fun addEnvComment(key: String, comment: String) {
        envComments.getOrPut(key) { mutableListOf() }.add(comment)
    }

    protected fun clearEnvComments() {
        envComments.clear()
    }
}