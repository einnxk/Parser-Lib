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
package com.github.einnxk.common.interfaces

import com.github.einnxk.common.exception.InvalidConfigurationException
import org.jetbrains.annotations.NotNull
import java.io.File

/**
 * Config is the base of every file that is file type that can be parsed, here
 * we collect all the basic io methods the file should contain
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
interface Config {

    /**
     * Save the file overriding the variable in the ConfigBase
     */
    @Throws(InvalidConfigurationException::class)
    fun save()

    /**
     * Save the YAML file in a specified file, not the one overriding
     * the variable in the ConfigBase
     *
     * @param file the specified file which the class is saved in
     */
    @Throws(InvalidConfigurationException::class)
    fun save(@NotNull file: File)

    /**
     * Create or load the file on disk overriding the variable in
     * the ConfigBase
     */
    @Throws(InvalidConfigurationException::class)
    fun init()

    /**
     * Create or load the file on disk which is specified by the only
     * parameter
     *
     * @param file the specified file which the class is loaded or created
     *             from
     */
    @Throws(InvalidConfigurationException::class)
    fun init(@NotNull file: File)

    /**
     * Reload the file from the disk and re set the variables in the class
     * that is mapped
     */
    @Throws(InvalidConfigurationException::class)
    fun reload()

    /**
     * Load the file from the disk
     */
    @Throws(InvalidConfigurationException::class)
    fun load()

    /**
     * Load a specified file from the disk a map them into the configuration
     * class
     */
    @Throws(InvalidConfigurationException::class)
    fun load(@NotNull file: File)
}