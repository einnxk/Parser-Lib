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
package com.github.einnxk.json.bootstrap

import com.github.einnxk.common.annotations.PreserveStatic
import com.github.einnxk.common.annotations.SerializeOptions
import com.github.einnxk.common.enums.ConfigMode
import org.jetbrains.annotations.NotNull
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * The base for any configuration class, the class with extends the
 * Config later - Has this class as a transitive dependency
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
open class ConfigBase {

    @Transient
    open var configFile: File? = null
    @Transient
    open var configHeader: Array<String>? = null
    @Transient
    protected var configMode: ConfigMode = ConfigMode.DEFAULT
    @Transient
    protected var skipFailedObject: Boolean = false

    fun doSkip(@NotNull field: Field) : Boolean {
        if (Modifier.isTransient(field.modifiers) || Modifier.isFinal(field.modifiers)) {
            return true
        }

        if (Modifier.isStatic(field.modifiers)) {
            if (field.isAnnotationPresent(PreserveStatic::class.java)) {
                return true
            }

            val preserveStatic: PreserveStatic = field.getAnnotation(PreserveStatic::class.java)
            return !(preserveStatic.value)
        }

        return false
    }

    fun serializeConfigurationFromAnnotation() {
        if (!(javaClass.isAnnotationPresent(SerializeOptions::class.java))) {
            return
        }

        val options: SerializeOptions = javaClass.getAnnotation(SerializeOptions::class.java)
        configHeader = options.configHeader
        configMode = options.configMode
        skipFailedObject = options.skipFailedObjects
    }
}