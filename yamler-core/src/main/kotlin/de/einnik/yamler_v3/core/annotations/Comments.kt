package de.einnik.yamler_v3.core.annotations

import org.jetbrains.annotations.NotNull

/**
 *  * An annotations that creates multiple lines of comments when the
 *  file is created or updated
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Comments(@NotNull val value: Array<String>)