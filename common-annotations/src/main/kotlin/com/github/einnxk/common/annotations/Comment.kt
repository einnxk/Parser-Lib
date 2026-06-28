package com.github.einnxk.common.annotations

import org.jetbrains.annotations.NotNull

/**
 * An annotations that creates a comment when the file is created
 * or updated.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Comment(@NotNull val value: String = "")