package com.github.einnxk.xml.mapper

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.einnxk.common.exception.InvalidConfigurationException
import com.github.einnxk.xml.bootstrap.ConfigBase
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * The bootstrap which handles all the writing and reading of XML files.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
open class BaseXmlMapper : ConfigBase() {

    @Transient
    protected val xmlMapper: XmlMapper = XmlMapper().apply {
        registerModule(KotlinModule.Builder().build())
    }

    @Transient
    internal var strictLoad: Boolean = false

    init {
        serializeConfigurationFromAnnotation()
    }

    protected fun loadFromXml(): ObjectNode {
        return try {
            InputStreamReader(FileInputStream(configFile!!), Charsets.UTF_8).use { reader ->
                val content = reader.readText()
                if (content.isBlank()) {
                    xmlMapper.createObjectNode()
                } else {
                    xmlMapper.readValue(content, ObjectNode::class.java)
                }
            }
        } catch (e: IOException) {
            throw InvalidConfigurationException("Could not load XML", e)
        }
    }

    protected fun saveToXml(node: ObjectNode) {
        try {
            OutputStreamWriter(FileOutputStream(configFile!!), Charsets.UTF_8).use { writer ->
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                xmlMapper.writerWithDefaultPrettyPrinter()
                    .withRootName("config")
                    .writeValue(writer, node)
            }
        } catch (e: IOException) {
            throw InvalidConfigurationException("Could not save XML", e)
        }
    }
}