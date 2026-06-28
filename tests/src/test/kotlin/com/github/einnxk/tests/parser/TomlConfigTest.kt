package com.github.einnxk.tests.parser

import com.github.einnxk.common.annotations.Path
import com.github.einnxk.common.annotations.SerializeOptions
import com.github.einnxk.common.annotations.validate.Range
import com.github.einnxk.common.annotations.validate.Required
import com.github.einnxk.common.enums.ConfigMode
import com.github.einnxk.common.interfaces.Config
import com.github.einnxk.tests.AbstractConfigTests
import com.github.einnxk.toml.TomlConfig

class TomlConfigTest : AbstractConfigTests() {

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class SimpleToml : TomlConfig() {
        var name: String = "default"
        var port: Int = 8080
        var enabled: Boolean = true
    }

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class NestedToml : TomlConfig() {
        @Path("database.host") var host: String = "localhost"
        @Path("database.port") var dbPort: Int = 5432
    }

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class RequiredToml : TomlConfig() {
        @Required
        var requiredField: String? = null
    }

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class RangeToml : TomlConfig() {
        @Range(min = 1, max = 100) var value: Int = 50
    }

    override fun extension() = "toml"
    override fun newSimpleConfig() = SimpleToml()
    override fun newNestedConfig() = NestedToml()
    override fun newRequiredConfig() = RequiredToml()
    override fun newRangeConfig() = RangeToml()

    override fun getConfigFile(config: Config) = (config as TomlConfig).configFile
    override fun setName(config: Config, value: String) { (config as SimpleToml).name = value }
    override fun getName(config: Config) = (config as SimpleToml).name
    override fun setPort(config: Config, value: Int) { (config as SimpleToml).port = value }
    override fun getPort(config: Config) = (config as SimpleToml).port
    override fun getEnabled(config: Config) = (config as SimpleToml).enabled
    override fun setHost(config: Config, value: String) { (config as NestedToml).host = value }
    override fun getHost(config: Config) = (config as NestedToml).host
    override fun setDbPort(config: Config, value: Int) { (config as NestedToml).dbPort = value }
    override fun getDbPort(config: Config) = (config as NestedToml).dbPort
    override fun setRangeValue(config: Config, value: Int) { (config as RangeToml).value = value }
}