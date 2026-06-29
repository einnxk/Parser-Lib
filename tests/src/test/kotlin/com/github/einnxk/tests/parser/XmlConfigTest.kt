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
import com.github.einnxk.tests.AbstractConfigTests
import com.github.einnxk.xml.XmlConfig

class XmlConfigTest : AbstractConfigTests() {

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class SimpleXml : XmlConfig() {
        var name: String = "default"
        var port: Int = 8080
        var enabled: Boolean = true
    }

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class NestedXml : XmlConfig() {
        @Path("database.host") var host: String = "localhost"
        @Path("database.port") var dbPort: Int = 5432
    }

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class RequiredXml : XmlConfig() {
        @Required
        var requiredField: String? = null
    }

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class RangeXml : XmlConfig() {
        @Range(min = 1, max = 100) var value: Int = 50
    }

    override fun extension() = "xml"
    override fun newSimpleConfig() = SimpleXml()
    override fun newNestedConfig() = NestedXml()
    override fun newRequiredConfig() = RequiredXml()
    override fun newRangeConfig() = RangeXml()

    override fun getConfigFile(config: Config) = (config as XmlConfig).configFile
    override fun setName(config: Config, value: String) { (config as SimpleXml).name = value }
    override fun getName(config: Config) = (config as SimpleXml).name
    override fun setPort(config: Config, value: Int) { (config as SimpleXml).port = value }
    override fun getPort(config: Config) = (config as SimpleXml).port
    override fun getEnabled(config: Config) = (config as SimpleXml).enabled
    override fun setHost(config: Config, value: String) { (config as NestedXml).host = value }
    override fun getHost(config: Config) = (config as NestedXml).host
    override fun setDbPort(config: Config, value: Int) { (config as NestedXml).dbPort = value }
    override fun getDbPort(config: Config) = (config as NestedXml).dbPort
    override fun setRangeValue(config: Config, value: Int) { (config as RangeXml).value = value }
}