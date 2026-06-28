package de.einnik.yamler_v3.core.annotations.validate

/**
 * An annotation marking a field as required with cannot be skipped
 * while parsing the file
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Required(val value: Boolean = true)