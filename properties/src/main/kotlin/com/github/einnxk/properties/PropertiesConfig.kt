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
package com.github.einnxk.properties

import com.github.einnxk.common.annotations.Comment
import com.github.einnxk.common.annotations.Comments
import com.github.einnxk.common.annotations.Path
import com.github.einnxk.common.annotations.validate.Required
import com.github.einnxk.common.enums.ConfigMode
import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.common.interfaces.Config
import com.github.einnxk.properties.mapper.PropertiesConfigMapper
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * JsonConfig is the abstract base for any Properties-backed configuration class.
 * Extend this class and define your fields - they will be automatically
 * mapped to and from a Properties file on disk.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
abstract class PropertiesConfig : PropertiesConfigMapper(), Config {

    private var strictLoad = false

    override fun save() {
        if (configFile == null) {
            throw IllegalStateException("Config file can not be null")
        }

        try {
            properties.clear()

            internalSave(javaClass)

            saveToProperties()
        } catch (e: Exception) {
            throw RuntimeException("Could not save properties config", e)
        }
    }

    override fun save(file: File) {
        configFile = file
        save()
    }

    override fun init() {
        if (configFile == null) {
            throw IllegalStateException("Config file can not be null")
        }

        if (!configFile!!.exists()) {
            configFile!!.parentFile?.mkdirs()
            configFile!!.createNewFile()
        }

        loadFromProperties()
        internalLoad(javaClass)
    }

    override fun init(file: File) {
        configFile = file
        init()
    }

    override fun reload() {
        loadFromProperties()
        internalLoad(javaClass)
    }

    override fun load() {
        if (configFile == null) {
            throw IllegalStateException("Config file can not be null")
        }

        strictLoad = true
        loadFromProperties()
        internalLoad(javaClass)
        strictLoad = false
    }

    override fun load(file: File) {
        configFile = file
        load()
    }

    @Throws(Exception::class)
    protected fun internalSave(clazz: Class<*>) {
        if (clazz.superclass != null &&
            clazz.superclass != PropertiesConfig::class.java
        ) {
            internalSave(clazz.superclass)
        }

        for (field in clazz.declaredFields) {
            if (doSkip(field)) continue
            val path = resolvePath(field)
            val comments = collectComments(field)

            if (field.isAnnotationPresent(Path::class.java)) {
                val annotation = field.getAnnotation(Path::class.java)
                comments.clear()
                addComment(path, annotation.value)
            }

            comments.forEach {
                addComment(path, it)
            }

            if (Modifier.isPrivate(field.modifiers)) {
                field.isAccessible = true
            }

            try {
                internalConverter.toConfig(
                    this,
                    field,
                    properties,
                    path
                )

            } catch (e: Exception) {
                if (!skipFailedObject) {
                    throw RuntimeException("Could not save field '${field.name}'", e)
                }
            }
        }
    }

    @Throws(InvalidConfigurationException::class)
    protected fun internalLoad(clazz: Class<*>) {
        if (clazz.superclass != null &&
            clazz.superclass != PropertiesConfig::class.java
        ) {
            internalLoad(clazz.superclass)
        }

        var needsSave = false
        for (field in clazz.declaredFields) {
            if (doSkip(field)) continue
            val path = resolvePath(field)
            if (Modifier.isPrivate(field.modifiers)) {
                field.isAccessible = true
            }
            val exists = properties.containsKey(path)

            if (exists) {
                try {
                    internalConverter.fromConfig(
                        this,
                        field,
                        properties,
                        path
                    )

                    validateRange(field)
                    if (strictLoad) {
                        validateRequired(field)
                    }
                } catch (e: Exception) {
                    throw InvalidConfigurationException(
                        "Could not load field '${field.name}'",
                        e
                    )
                }
            } else {
                if (strictLoad) {
                    val required = field.getAnnotation(Required::class.java)
                    if (required != null && required.value) {
                        throw InvalidConfigurationException(
                            "Required field '${field.name}' is missing in properties"
                        )
                    }
                }

                try {
                    internalConverter.toConfig(
                        this,
                        field,
                        properties,
                        path
                    )

                    internalConverter.fromConfig(
                        this,
                        field,
                        properties,
                        path
                    )

                    validateRange(field)
                    needsSave = true
                } catch (e: Exception) {
                    if (!skipFailedObject) {
                        throw InvalidConfigurationException(
                            "Could not initialize field '${field.name}'",
                            e
                        )
                    }
                }
            }
        }

        if (needsSave) {
            saveToProperties()
        }
    }

    private fun resolvePath(field: Field): String {
        var path =
            if (configMode == ConfigMode.PATH_BY_UNDERSCORE)
                field.name.replace("_", ".")
            else
                field.name

        if (field.isAnnotationPresent(Path::class.java)) {
            path = field.getAnnotation(Path::class.java).value
        }

        return path
    }

    private fun collectComments(field: Field): MutableList<String> {
        val comments = mutableListOf<String>()

        field.annotations.forEach { annotation ->
            when (annotation) {
                is Comment -> comments += annotation.value
                is Comments -> comments += annotation.value
            }
        }

        return comments
    }
}