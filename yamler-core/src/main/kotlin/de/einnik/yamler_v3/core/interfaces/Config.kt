package de.einnik.yamler_v3.core.interfaces

import de.einnik.yamler_v3.core.exception.InvalidConfigurationException
import org.jetbrains.annotations.NotNull
import java.io.File

/**
 * Config is the base of every file that is file type that can be parsed, here
 * we collect all the basic io methods the file should contain
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
interface Config {

    /**
     * Save the file overriding the variable in the ConfigBase
     */
    @Throws(InvalidConfigurationException::class)
    fun save()

    /**
     * Save the YAML file in a specified file, not the one overriding
     * the variable in the ConfigBase
     *
     * @param file the specified file which the class is saved in
     */
    @Throws(InvalidConfigurationException::class)
    fun save(@NotNull file: File)

    /**
     * Create or load the file on disk overriding the variable in
     * the ConfigBase
     */
    @Throws(InvalidConfigurationException::class)
    fun init()

    /**
     * Create or load the file on disk which is specified by the only
     * parameter
     *
     * @param file the specified file which the class is loaded or created
     *             from
     */
    @Throws(InvalidConfigurationException::class)
    fun init(@NotNull file: File)

    /**
     * Reload the file from the disk and re set the variables in the class
     * that is mapped
     */
    @Throws(InvalidConfigurationException::class)
    fun reload()

    /**
     * Load the file from the disk
     */
    @Throws(InvalidConfigurationException::class)
    fun load()

    /**
     * Load a specified file from the disk a map them into the configuration
     * class
     */
    @Throws(InvalidConfigurationException::class)
    fun load(@NotNull file: File)
}