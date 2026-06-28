package de.einnik.yamler_v3.core.annotations

import org.jetbrains.annotations.NotNull

/**
 * An annotation defining the path to the field in a YAML file
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
*/
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Path(@NotNull val value: String)