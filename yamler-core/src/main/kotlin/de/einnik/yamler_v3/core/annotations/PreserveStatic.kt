package de.einnik.yamler_v3.core.annotations

/**
 * An annotation that also maps static fields in a
 * config file.
 * <br>
 *
 * By default, static files are not mapped into the
 * YAML file
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class PreserveStatic(val value: Boolean = true)
