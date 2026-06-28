package com.github.einnxk.common.annotations

/**
 * An annotation that also maps static fields in a
 * config file.
 * <br>
 *
 * By default, static files are not mapped into the
 * file.
 *
 * @author EinNik
 * @since 4.0.0-SNAPSHOT
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class PreserveStatic(val value: Boolean = true)