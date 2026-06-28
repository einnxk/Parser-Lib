package de.einnik.yamler_v3.core.annotations

import de.einnik.yamler_v3.core.enums.ConfigMode

/**
 * An annotation that is applied to the YamlConfig class. Here
 * we define its configuration options with, like a header at the top
 * of the file, if the failed parsing of objects should be skipped
 * or the configMode
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class SerializeOptions(
    val configHeader: Array<String> = [],
    val skipFailedObjects: Boolean = false,
    val configMode: ConfigMode = ConfigMode.DEFAULT
)