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
package com.github.einnxk.common.annotations

import org.jetbrains.annotations.ApiStatus

/**
 * An annotation that can be applied to fields in [com.github.einnxk.common.interfaces.Config]s,
 * which are then checked and overridden by an environment variable, which the defined details
 * from the annotation parameters.
 *
 * @author EinNik
 * @since 4.2.0
 *
 * @see com.github.einnxk.common.interfaces.Config
 */
@ApiStatus.Experimental
@ApiStatus.AvailableSince(value = "4.2")
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class EnvironmentOverride(

    /**
     * The env variable name, which doesn't need to be upper case.
     */
    val value: String,

    /**
     * Should an [IllegalStateException] be thrown, when the type from the env
     * variable can not be parsed into the type we except.
     */
    val throwIfWrongType: Boolean = false,

    /**
     * Should an [IllegalStateException] be thrown, when no value with that name
     * can be found on the machine.
     */
    val throwIfNull: Boolean = false
)