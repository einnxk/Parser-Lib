package de.einnik.yamler_v3.core.exception

/**
 * An Exception that is thrown when the file is missing required fields or
 * these fields contains a type we didn't expect there
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
class InvalidConfigurationException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)