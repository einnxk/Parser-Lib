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
package com.github.einnxk.yamler.core

import com.github.einnxk.common.annotations.Comment
import com.github.einnxk.common.annotations.Comments
import com.github.einnxk.common.annotations.Path
import com.github.einnxk.common.enums.ConfigMode
import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.common.interfaces.Config
import com.github.einnxk.yamler.core.mapper.ConfigMapper
import com.github.einnxk.yamler.core.section.ConfigSection
import java.io.File
import java.io.IOException
import java.lang.reflect.Modifier
import java.util.*

/**
 * The core api class the config class needs to extends to implement
 * core file methods and get all the mappers as a transitive dependency
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
abstract class YamlConfig : ConfigMapper(),
    Config {

    override fun save() {
        if (configFile == null) {
            throw IllegalStateException("Config file can not be null")
        }

        if (root == null) {
            root = ConfigSection()
        }

        clearComments()
        internalSave(javaClass)
        saveToYaml()
    }

    override fun save(file: File) {
        configFile = file
        save()
    }

    override fun init() {
        if (configFile == null) {
            throw IllegalStateException("Config file can not be null")
        }

        if (!(configFile!!.exists())) {
            if (configFile!!.parentFile.exists()) {
                configFile!!.parentFile.mkdirs()
            }

            try {
                configFile!!.createNewFile()
                save()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            return
        }

        load()
    }

    override fun init(file: File) {
        configFile = file
        init()
    }

    override fun reload() {
        loadFromYaml()
        internalLoad(javaClass)
    }

    override fun load() {
        if (configFile == null) {
            throw IllegalStateException("Config file can not be null")
        }

        loadFromYaml()
        internalLoad(javaClass)
    }

    override fun load(file: File) {
        configFile = file
        load()
    }

    @Throws(InvalidConfigurationException::class)
    protected fun internalSave(clazz: Class<*>) {
        if (clazz.getSuperclass() != YamlConfig::class.java) {
            internalSave(clazz.getSuperclass())
        }

        for (field in clazz.declaredFields) {
            if (doSkip(field)) {
                continue
            }

            var path: String?

            when (configMode) {
                ConfigMode.PATH_BY_UNDERSCORE -> path = field.name.replace("_", ".")
                ConfigMode.FIELD_IS_KEY -> path = field.name
                ConfigMode.DEFAULT -> {
                    val fieldName = field.name
                    path = if (fieldName.contains("_")) {
                        field.name.replace("_", ".")
                    } else {
                        field.name
                    }
                }
            }

            val comments = ArrayList<String?>()
            for (annotation in field.annotations) {
                if (annotation is Comment) {
                    val comment: Comment = annotation
                    comments.add(comment.value)
                }

                if (annotation is Comments) {
                    val comment: Comments = annotation
                    comments.addAll(listOf(*comment.value))
                }
            }

            if (field.isAnnotationPresent(Path::class.java)) {
                val path1: Path = field.getAnnotation(Path::class.java)
                path = path1.value
            }

            if (comments.isNotEmpty()) {
                for (comment in comments) {
                    addComment(path!!, comment!!)
                }
            }

            if (Modifier.isPrivate(field.modifiers)) {
                field.setAccessible(true)
            }

            try {
                internalConverter.toConfig(this, field, root!!, path)
                internalConverter.fromConfig(this, field, root!!, path)
            } catch (e: Exception) {
                if (!(skipFailedObject)) {
                    throw RuntimeException("Could not save file, skipping...", e)
                }
            }
        }
    }

    @Throws(InvalidConfigurationException::class)
    protected fun internalLoad(clazz: Class<*>) {
        if (clazz.getSuperclass() != YamlConfig::class.java) {
            internalLoad(clazz.getSuperclass())
        }

        var save = false
        for (field in clazz.declaredFields) {
            if (doSkip(field)) {
                continue
            }

            var path = if (configMode == ConfigMode.PATH_BY_UNDERSCORE) field.name
                .replace("_".toRegex(), ".") else field.name

            if (field.isAnnotationPresent(Path::class.java)) {
                val path1: Path =
                    field.getAnnotation(Path::class.java)
                path = path1.value
            }

            if (Modifier.isPrivate(field.modifiers)) {
                field.setAccessible(true)
            }

            if (root!!.has(path!!)) {
                try {
                    internalConverter.fromConfig(this, field, root!!, path)
                } catch (e: java.lang.Exception) {
                    throw InvalidConfigurationException("Could not set field", e)
                }
            } else {
                try {
                    internalConverter.toConfig(this, field, root!!, path)
                   internalConverter.fromConfig(this, field, root!!, path)

                    save = true
                } catch (e: java.lang.Exception) {
                    if (!skipFailedObject) {
                        throw InvalidConfigurationException("Could not get field", e)
                    }
                }
            }
        }

        if (save) {
            saveToYaml()
        }
    }
}