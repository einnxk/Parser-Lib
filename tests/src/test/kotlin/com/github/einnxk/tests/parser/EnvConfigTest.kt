package com.github.einnxk.tests.parser

import com.github.einnxk.common.annotations.Path
import com.github.einnxk.common.annotations.SerializeOptions
import com.github.einnxk.common.annotations.validate.Range
import com.github.einnxk.common.annotations.validate.Required
import com.github.einnxk.common.enums.ConfigMode
import com.github.einnxk.common.interfaces.Config
import com.github.einnxk.env.EnvConfig
import com.github.einnxk.tests.AbstractConfigTests

class EnvConfigTest : AbstractConfigTests() {

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class SimpleEnv : EnvConfig() {
        var name: String = "default"
        var port: Int = 8080
        var enabled: Boolean = true
    }

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class NestedEnv : EnvConfig() {
        @Path("DATABASE_HOST") var host: String = "localhost"
        @Path("DATABASE_PORT") var dbPort: Int = 5432
    }

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class RequiredEnv : EnvConfig() {
        @Required
        var requiredField: String? = null
    }

    @SerializeOptions(configMode = ConfigMode.FIELD_IS_KEY)
    class RangeEnv : EnvConfig() {
        @Range(min = 1, max = 100) var value: Int = 50
    }

    override fun extension() = "env"
    override fun newSimpleConfig() = SimpleEnv()
    override fun newNestedConfig() = NestedEnv()
    override fun newRequiredConfig() = RequiredEnv()
    override fun newRangeConfig() = RangeEnv()

    override fun getConfigFile(config: Config) = (config as EnvConfig).configFile
    override fun setName(config: Config, value: String) { (config as SimpleEnv).name = value }
    override fun getName(config: Config) = (config as SimpleEnv).name
    override fun setPort(config: Config, value: Int) { (config as SimpleEnv).port = value }
    override fun getPort(config: Config) = (config as SimpleEnv).port
    override fun getEnabled(config: Config) = (config as SimpleEnv).enabled
    override fun setHost(config: Config, value: String) { (config as NestedEnv).host = value }
    override fun getHost(config: Config) = (config as NestedEnv).host
    override fun setDbPort(config: Config, value: Int) { (config as NestedEnv).dbPort = value }
    override fun getDbPort(config: Config) = (config as NestedEnv).dbPort
    override fun setRangeValue(config: Config, value: Int) { (config as RangeEnv).value = value }
}