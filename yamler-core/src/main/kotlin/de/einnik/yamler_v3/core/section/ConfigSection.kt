package de.einnik.yamler_v3.core.section

/**
 * An ConfigSection is a model class representing the section of an
 * YAML file with their key value schema
 *
 * @author EinNik
 * @since 3.0.0-SNAPSHOT
 */
open class ConfigSection(private val fullPath: String = "") {

    protected val map: MutableMap<Any, Any?> = LinkedHashMap()

    constructor(root: ConfigSection, key: String) : this(
        if (root.fullPath.isNotEmpty()) "${root.fullPath}.$key" else key
    )

    fun create(path: String): ConfigSection {
        require(path.isNotEmpty()) {
            "Cannot create section at empty path"
        }

        val parts = path.split('.')
        var section = this

        for (part in parts.dropLast(1)) {
            section = section.getConfigSection(part)
                ?: section.create(part)
        }

        val key = parts.last()

        if (section === this) {
            return ConfigSection(this, key).also {
                map[key] = it
            }
        }

        return section.create(key)
    }

    private fun getConfigSection(node: String): ConfigSection? =
        map[node] as? ConfigSection

    fun set(path: String, value: Any?) {
        set(path, value, true)
    }

    fun set(
        path: String,
        value: Any?,
        searchForSubNodes: Boolean
    ) {
        require(path.isNotEmpty()) {
            "Cannot set a value at empty path"
        }

        var section = this
        val parts = path.split('.')

        if (searchForSubNodes) {
            for (part in parts.dropLast(1)) {
                section = section.getConfigSection(part)
                    ?: section.create(part)
            }
        }

        val key = parts.last()

        if (section === this) {
            if (value == null) {
                map.remove(key)
            } else {
                map[key] = value
            }
        } else {
            section.set(key, value, false)
        }
    }

    protected fun mapChildrenValues(
        output: MutableMap<Any, Any?>,
        section: ConfigSection?,
        deep: Boolean
    ) {
        section ?: return

        for ((key, value) in section.map) {
            if (value is ConfigSection) {
                val result = LinkedHashMap<Any, Any?>()
                output[key] = result

                if (deep) {
                    mapChildrenValues(result, value, true)
                }
            } else {
                output[key] = value
            }
        }
    }

    fun getValues(deep: Boolean): Map<Any, Any?> =
        LinkedHashMap<Any, Any?>().also {
            mapChildrenValues(it, this, deep)
        }

    fun remove(path: String) {
        set(path, null)
    }

    fun has(path: String): Boolean {
        require(path.isNotEmpty()) {
            "Cannot remove a Value at empty path"
        }

        val parts = path.split('.')
        var section = this

        for (part in parts.dropLast(1)) {
            section = section.getConfigSection(part)
                ?: return false
        }

        return parts.last() in section.map
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(path: String): T? {
        require(path.isNotEmpty()) {
            "Cannot remove a Value at empty path"
        }

        val parts = path.split('.')
        var section = this

        for (part in parts.dropLast(1)) {
            section = section.getConfigSection(part)
                ?: section.create(part)
        }

        return section.map[parts.last()] as T?
    }

    fun getRawMap(): Map<Any, Any?> = map

    companion object {

        fun convertFromMap(
            config: Map<*, *>
        ): ConfigSection =
            ConfigSection().apply {
                config.forEach { (key, value) ->
                    if (key != null) {
                        map[key] = value
                    }
                }
            }
    }
}