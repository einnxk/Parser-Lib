package de.einnik.yamler_v3.core.annotations

import org.jetbrains.annotations.NotNull

/**
 * An annotations that creates a comment when the file is created
 * or updated
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Comment(@NotNull val value: String = "")