package de.einnik.yamler_v3.core.annotations.validate

/**
 * An annotation that validates the size of a numeric or the minimum
 * and maximum size of a collection and map
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Range(
    val min: Int = 0,
    val max: Int = Int.MAX_VALUE
)