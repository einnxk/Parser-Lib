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
package com.github.einnxk.tests.parser

import com.github.einnxk.common.annotations.Path
import com.github.einnxk.common.annotations.SerializeOptions
import com.github.einnxk.common.annotations.validate.Range
import com.github.einnxk.common.annotations.validate.Required
import com.github.einnxk.common.enums.ConfigMode
import com.github.einnxk.common.interfaces.Config
import com.github.einnxk.json.JsonConfig
import com.github.einnxk.tests.AbstractConfigTests

class JsonConfigTest : AbstractConfigTests() {

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class SimpleJson : JsonConfig() {
        var name: String = "default"
        var port: Int = 8080
        var enabled: Boolean = true
    }

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class NestedJson : JsonConfig() {
        @Path("database.host") var host: String = "localhost"
        @Path("database.port") var dbPort: Int = 5432
    }

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class RequiredJson : JsonConfig() {
        @Required var requiredField: String? = null
    }

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class RangeJson : JsonConfig() {
        @Range(min = 1, max = 100) var value: Int = 50
    }

    override fun extension() = "json"
    override fun newSimpleConfig() = SimpleJson()
    override fun newNestedConfig() = NestedJson()
    override fun newRequiredConfig() = RequiredJson()
    override fun newRangeConfig() = RangeJson()

    override fun getConfigFile(config: Config) = (config as JsonConfig).configFile
    override fun setName(config: Config, value: String) { (config as SimpleJson).name = value }
    override fun getName(config: Config) = (config as SimpleJson).name
    override fun setPort(config: Config, value: Int) { (config as SimpleJson).port = value }
    override fun getPort(config: Config) = (config as SimpleJson).port
    override fun getEnabled(config: Config) = (config as SimpleJson).enabled
    override fun setHost(config: Config, value: String) { (config as NestedJson).host = value }
    override fun getHost(config: Config) = (config as NestedJson).host
    override fun setDbPort(config: Config, value: Int) { (config as NestedJson).dbPort = value }
    override fun getDbPort(config: Config) = (config as NestedJson).dbPort
    override fun setRangeValue(config: Config, value: Int) { (config as RangeJson).value = value }
}