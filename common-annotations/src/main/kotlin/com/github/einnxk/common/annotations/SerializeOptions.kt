package com.github.einnxk.common.annotations

/**
 * An annotation that is applied to the config class. Here
 * we define its configuration options with, like a header at the top
 * of the file, if the failed parsing of objects should be skipped.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class SerializeOptions(
    val configHeader: Array<String> = [],
    val skipFailedObjects: Boolean = false,
)