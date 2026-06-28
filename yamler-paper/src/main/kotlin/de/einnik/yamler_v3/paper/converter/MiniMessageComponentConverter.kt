package de.einnik.yamler_v3.paper.converter

import de.einnik.yamler_v3.core.converter.Converter
import de.einnik.yamler_v3.core.converter.InternalConverter
import de.einnik.yamler_v3.core.section.ConfigSection
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import org.jetbrains.annotations.ApiStatus
import java.lang.reflect.ParameterizedType

/**
 * The MiniMessageComponentConverter is a part of the default paper provided
 * converters. Here we convert Components from Paper into the Config File
 * and parse one from it.
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
@ApiStatus.Experimental
open class MiniMessageComponentConverter(private val internalConverter: InternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val component: Component = obj as Component

        val saveMap: MutableMap<String, Any?> = mutableMapOf()
        saveMap["text-decoration"] =
            component.decorations().mapKeys { it.key.name }
                .mapValues { it.value.name }

        saveMap["content"] = MiniMessage.miniMessage().serialize(component)

        return saveMap
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val map: Map<*, *> = when (obj) {
            is Map<*, *> -> obj
            is ConfigSection -> obj.getRawMap()
            else -> throw IllegalStateException(
                "Expected Map or ConfigSection, got ${obj?.javaClass?.name}"
            )
        }

        val content = map["content"] as? String
            ?: throw IllegalStateException("Missing component content")

        val component = MiniMessage.miniMessage().deserialize(content)

        val decorationsRaw = map["text-decoration"] as? Map<*, *>

        if (decorationsRaw != null) {
            val converted = decorationsRaw.mapNotNull { (key, value) ->
                val decoration = key?.toString()?.let {
                    runCatching { TextDecoration.valueOf(it) }.getOrNull()
                }

                val state = when (value) {
                    is Boolean ->
                        if (value) TextDecoration.State.TRUE
                        else TextDecoration.State.FALSE

                    is String ->
                        runCatching {
                            TextDecoration.State.valueOf(value)
                        }.getOrNull()

                    else -> null
                }

                if (decoration != null && state != null) {
                    decoration to state
                } else {
                    null
                }
            }.toMap()

            return component.decorations(converted)
        }

        return component
    }

    override fun supports(type: Class<*>): Boolean {
        return Component::class.java == type
    }
}