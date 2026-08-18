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
package com.github.einnxk.tests

import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.common.interfaces.Config
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

abstract class AbstractConfigTests {

    @TempDir
    lateinit var tempDir: File

    abstract fun extension(): String
    abstract fun newSimpleConfig(): Config
    abstract fun newNestedConfig(): Config
    abstract fun newRangeConfig(): Config

    abstract fun setName(config: Config, value: String)
    abstract fun getName(config: Config): String
    abstract fun setPort(config: Config, value: Int)
    abstract fun getPort(config: Config): Int
    abstract fun getEnabled(config: Config): Boolean
    abstract fun setHost(config: Config, value: String)
    abstract fun getHost(config: Config): String
    abstract fun setDbPort(config: Config, value: Int)
    abstract fun getDbPort(config: Config): Int
    abstract fun setRangeValue(config: Config, value: Int)
    abstract fun getConfigFile(config: Config): File?

    @Test
    fun `init creates file on disk`() {
        val c = newSimpleConfig().also { it.init(File(tempDir, "simple.${extension()}")) }
        assertThat(getConfigFile(c)).exists()
    }

    @Test fun `default values are written to file`() {
        val c = newSimpleConfig().also { it.init(File(tempDir, "defaults.${extension()}")) }
        assertThat(getName(c)).isEqualTo("default")
        assertThat(getPort(c)).isEqualTo(8080)
        assertThat(getEnabled(c)).isTrue()
    }

    @Test fun `modified values are saved and reloaded`() {
        val file = File(tempDir, "modified.${extension()}")
        val c = newSimpleConfig().also { it.init(file) }
        setName(c, "changed")
        setPort(c, 9090)
        c.save()

        val r = newSimpleConfig().also { it.init(file) }
        assertThat(getName(r)).isEqualTo("changed")
        assertThat(getPort(r)).isEqualTo(9090)
    }

    @Test fun `reload picks up changes`() {
        val c = newSimpleConfig().also { it.init(File(tempDir, "reload.${extension()}")) }
        setName(c, "updated")
        c.save()
        c.reload()
        assertThat(getName(c)).isEqualTo("updated")
    }

    @Test fun `nested path resolves correctly`() {
        val file = File(tempDir, "nested.${extension()}")
        val c = newNestedConfig().also { it.init(file) }
        setHost(c, "db.example.com")
        setDbPort(c, 3306)
        c.save()

        val r = newNestedConfig().also { it.init(file) }
        assertThat(getHost(r)).isEqualTo("db.example.com")
        assertThat(getDbPort(r)).isEqualTo(3306)
    }

    @Test fun `range throws when out of bounds`() {
        val file = File(tempDir, "range.${extension()}")
        val c = newRangeConfig().also { it.init(file) }
        setRangeValue(c, 200)
        c.save()
        assertThatThrownBy { c.reload() }
            .isInstanceOf(InvalidConfigurationException::class.java)
    }
}