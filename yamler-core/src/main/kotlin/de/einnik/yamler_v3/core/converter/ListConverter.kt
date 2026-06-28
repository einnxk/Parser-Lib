package de.einnik.yamler_v3.core.converter

import java.lang.reflect.ParameterizedType

/**
 * The MapConverter is part of the default conversion library. This class
 * makes it able to convert a list into YAML and back into a List.
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
class ListConverter(private val internalConverter: InternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        val values = obj as MutableList<*>
        val newList = ArrayList<Any?>()

        for (value in values) {
            if (value == null) {
                newList.add(null)
                continue
            }

            val converter = internalConverter.getConverter(value.javaClass)

            if (converter != null) {
                newList.add(
                    converter.toConfig(
                        value.javaClass,
                        value,
                        null
                    )
                )
            } else {
                newList.add(value)
            }
        }

        return newList
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any? {
        var newList: MutableList<Any?> = ArrayList()

        try {
            @Suppress("UNCHECKED_CAST")
            newList = type?.getDeclaredConstructor()?.newInstance() as MutableList<Any?>
        } catch (_: Exception) {
        }

        @Suppress("UNCHECKED_CAST")
        val values = obj as MutableList<Any?>

        if (
            parameterizedType != null &&
            parameterizedType.actualTypeArguments[0] is Class<*>
        ) {
            val elementType =
                parameterizedType.actualTypeArguments[0] as Class<*>

            val converter = internalConverter.getConverter(elementType)

            if (converter != null) {
                for (value in values) {
                    newList.add(
                        converter.fromConfig(
                            elementType,
                            value,
                            null
                        )
                    )
                }
            } else {
                newList = values
            }
        } else {
            newList = values
        }

        return newList
    }

    override fun supports(type: Class<*>): Boolean {
        return List::class.java.isAssignableFrom(type)
    }
}