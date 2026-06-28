package de.einnik.yamler_v3.core.mapper

import de.einnik.yamler_v3.core.bootstrap.ConfigBase
import de.einnik.yamler_v3.core.exception.InvalidConfigurationException
import de.einnik.yamler_v3.core.section.ConfigSection
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor
import org.yaml.snakeyaml.representer.Representer
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.Charset

/**
 * The bootstrap which handles all the writing and reading. We read the YAML file
 * save comments and create ConfigSections and later also saves the ConfigSection
 * and writes them into the YAML File
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
open class BaseConfigMapper : ConfigBase() {

    @Transient
    private val yaml: Yaml
    @Transient
    protected var root: ConfigSection? = null
    @Transient
    private val comments: MutableMap<String, MutableList<String>> = LinkedHashMap()
    @Transient
    private val representer: Representer

    init {
        var dumperOptions = DumperOptions()
        dumperOptions.indent = 2
        dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK

        representer = Representer(dumperOptions)

        val loaderOptions = LoaderOptions()

        yaml = Yaml(
            CustomClassLoaderConstructor(
                BaseConfigMapper::class.java.classLoader,
                loaderOptions
            ),
            representer,
            dumperOptions,
            loaderOptions
        )

        serializeConfigurationFromAnnotation()
    }

    protected fun loadFromYaml() {
        root = ConfigSection()

        try {
            InputStreamReader(FileInputStream(configFile!!), Charset.defaultCharset()).use { fileReader ->
                val `object` = yaml.load<Any?>(fileReader)
                if (`object` != null) {
                    convertMapsToSections(`object` as MutableMap<*, *>, root)
                }
            }
        } catch (e: IOException) {
            throw InvalidConfigurationException("Could not load YAML", e)
        }
    }

    private fun convertMapsToSections(input: MutableMap<*, *>?, section: ConfigSection?) {
        if (input == null) {
            return
        }

        for (entry in input.entries) {
            val key: String = entry.key.toString()
            val value: Any? = entry.value

            if (value is MutableMap<*, *>) {
                convertMapsToSections(value, section?.create(key))
            } else {
                section?.set(key, value, false)
            }
        }
    }

    @Throws(InvalidConfigurationException::class)
    protected fun saveToYaml() {
        try {
            OutputStreamWriter(
                FileOutputStream(configFile!!),
                Charsets.UTF_8
            ).use { fileWriter ->

                configHeader?.let { header ->
                    for (line in header) {
                        fileWriter.write("# $line\n")
                    }
                    fileWriter.write("\n")
                }

                var depth = 0
                var keyChain = arrayListOf<String>()

                val yamlString = yaml.dump(root?.getValues(true))

                val writeLines = buildString {
                    for (line in yamlString.split("\n")) {

                        if (line.startsWith(" ".repeat(depth))) {
                            keyChain.add(line.split(":")[0].trim())
                            depth += 2
                        } else {
                            if (line.startsWith(" ".repeat(depth - 2))) {
                                if (keyChain.isNotEmpty()) {
                                    keyChain.removeAt(keyChain.size - 1)
                                }
                            } else {

                                var spaces = 0
                                for (char in line) {
                                    if (char == ' ') {
                                        spaces++
                                    } else {
                                        break
                                    }
                                }

                                depth = spaces

                                if (spaces == 0) {
                                    keyChain = arrayListOf()
                                    depth = 2
                                } else {
                                    val temp = arrayListOf<String>()

                                    for ((index, _) in (0 until spaces step 2).withIndex()) {
                                        temp.add(keyChain[index])
                                    }

                                    keyChain = temp
                                    depth += 2
                                }
                            }

                            keyChain.add(line.split(":")[0].trim())
                        }

                        val search = if (keyChain.isNotEmpty()) {
                            keyChain.joinToString(".")
                        } else {
                            ""
                        }

                        comments[search]?.forEach { comment ->
                            append(" ".repeat(depth - 2))
                            append("# ")
                            append(comment)
                            append('\n')
                        }

                        append(line)
                        append('\n')
                    }
                }

                fileWriter.write(writeLines)
            }
        } catch (e: IOException) {
            throw InvalidConfigurationException("Could not save YML", e)
        }
    }

    private fun join(list: MutableList<String?>, conjunction: String?): String {
        val sb = StringBuilder()
        var first = true
        for (item in list) {
            if (first) {
                first = false
            } else {
                sb.append(conjunction)
            }
            sb.append(item)
        }

        return sb.toString()
    }

    fun addComment(key: String, value: String) {
        if (!(comments.contains(key))) {
            comments[key] = mutableListOf(value)
        }
    }

    fun clearComments() {
        comments.clear()
    }
}