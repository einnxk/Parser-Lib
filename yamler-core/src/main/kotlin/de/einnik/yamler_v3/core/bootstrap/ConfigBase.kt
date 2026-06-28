package de.einnik.yamler_v3.core.bootstrap

import de.einnik.yamler_v3.core.annotations.PreserveStatic
import de.einnik.yamler_v3.core.annotations.SerializeOptions
import de.einnik.yamler_v3.core.converter.Converter
import de.einnik.yamler_v3.core.converter.InternalConverter
import de.einnik.yamler_v3.core.enums.ConfigMode
import de.einnik.yamler_v3.core.exception.InvalidConverterException
import org.jetbrains.annotations.NotNull
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.jvm.Throws

/**
 * The base for any configuration class, the class with extends the
 * YamlConfig later - Has this class as a transitive dependency
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
open class ConfigBase {

    @Transient
    open var configFile: File? = null
    @Transient
    open var configHeader: Array<String>? = null
    @Transient
    protected var configMode: ConfigMode = ConfigMode.DEFAULT
    @Transient
    protected var skipFailedObject: Boolean = false
    @Transient
    protected val internalConverter: InternalConverter = InternalConverter()

    /**
     * Add a custom converter for the parsing of the YAML File
     *
     * @param converter the custom converter we want to add for this YAML
     *                  file
     */
    @Throws(InvalidConverterException::class)
    fun addConverter(@NotNull converter: Class<out Converter>) {
        internalConverter.addConverter(converter)
    }

    /**
     * Internal method to check if the field should be skipped, because it is static, final
     * or the serialize annotation config allows that
     *
     * @param field the field we check
     *
     * @return returns if the field should be skipped
     */
    fun doSkip(@NotNull field: Field) : Boolean {
        if (Modifier.isTransient(field.modifiers) || Modifier.isFinal(field.modifiers)) {
            return true
        }

        if (Modifier.isStatic(field.modifiers)) {
            if (field.isAnnotationPresent(PreserveStatic::class.java)) {
                return true
            }

            val preserveStatic: PreserveStatic = field.getAnnotation(PreserveStatic::class.java)
            return !(preserveStatic.value)
        }

        return false
    }

    /**
     * An internal method that serializes the configurations from the class with are defined
     * by annotating the class with the SerializeOptions annotation
     */
    fun serializeConfigurationFromAnnotation() {
        if (!(javaClass.isAnnotationPresent(SerializeOptions::class.java))) {
            return
        }

        val options: SerializeOptions = javaClass.getAnnotation(SerializeOptions::class.java)
        configHeader = options.configHeader
        configMode = options.configMode
        skipFailedObject = options.skipFailedObjects
    }
}