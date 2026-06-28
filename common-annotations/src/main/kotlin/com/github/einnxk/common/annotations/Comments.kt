package com.github.einnxk.common.annotations

import org.jetbrains.annotations.NotNull

/**
 * An annotations that creates multiple lines of comments when the
 * file is created or updated.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Comments(@NotNull val value: Array<String>)