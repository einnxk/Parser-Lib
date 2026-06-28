package com.github.einnxk.common.annotations

/**
 * An annotation defining the name of the field in a file.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Path(val value: String)