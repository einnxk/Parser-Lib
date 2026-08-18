[![Build](https://img.shields.io/github/actions/workflow/status/einnxk/Parser-Lib/ci.yml?logo=github)](https://github.com/einnxk/Parser-Lib/actions)
[![GitHub release](https://img.shields.io/github/v/release/einnxk/Parser-Lib?logo=github&color=blue)](https://github.com/einnxk/Parser-Lib/releases)
[![JitPack](https://jitpack.io/v/einnxk/Parser-Lib.svg)](https://jitpack.io/#einnxk/Parser-Lib)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

# Parser-Lib
**Parser-Lib** is a modern, lightweight Kotlin and Java library designed for effortless file parsing and configuration management. Write your configuration logic once and easily switch between multiple file formats without changing your application code. The following file types are supported: 

- `yaml`
- `json`
- `properties`
- `toml`
- `xml`
- `hocon`
- `env`

### It is: 
- **Annotation Driven** - A modern, mostly annotation driven library 
- **Extensible** - Easily implement `Converters` for nested classes yourself
- **Unified for multiple file types** - Easily switch between file types 
- **Native Minecraft support** - The optional paper module provides parsers, for `Locations, Blocks, Components & ItemStacks`
- **Free & Open Source** - Free and open source forever

## 📦 Installation & Dependency Setup
Add the JitPack repository and the specific modules you need to your `build.gradle.kts`:
```kts
repositories {
    maven("https://jitpack.io")
}

dependencies {
    // replace the module with the one you need
    implementation("com.github.einnxk.parser-lib:yamler-core:4.2.0")
}
```

## ⚡ Quick Start Guide
### 1. Define your configuration class
All configuration classes extend `<Format>Config` (e.g., YamlConfig, JsonConfig, TomlConfig). Fields are automatically assigned default values if the configuration file or value does not exist.

`static, final, and transient` fields are excluded by default. Use `@PreserveStatic` if you wish to parse static fields.
````java
@Getter
@Setter
public final class Example extends YamlConfig {

    public Example(Path file) {
        this.setConfigFile(file.toFile());
        this.setConfigMode(ConfigMode.DEFAULT);
    }

    @Comments({
            "Here you can write very important comments in most of",
            "the languages supported!"
    })

    @Path("example.enabled")
    private boolean enabled = false;

    @Range(min = 26, max = 64)
    private Set<String> something = new ArrayList<>();

    @EnvironmentOverride(name = "REDIS_HOST", throwIfWrongType = true, throwIfNull = true)
    private String datasourcePassword = "password";

    @PreserveStatic
    private static String staticExample = "ExampleString";
}
````

### 2. Init, load, save & reload your file
Managing configuration lifecycle is consistent across all file formats:
````java
ExampleConfig example = new ExampleConfig(filePath);

// Create the file with default values if missing, or load it from disk
example.init();

// Load the file without creating it if missing
example.load();

// Reload values from disk (overwrites runtime modifications in memory)
example.reload();

// Save field values from memory back to disk
example.save();
````

### 3. Create your own Sections
Sections define collections of fields grouped under a common path.
> [!CAUTION]
> Sections do not replace converters, nested custom classes still need a Converter registered in the Config parent class. 
````java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class ExampleSection extends ConfigSection {
    private String someField;
    private String otherField;
}
````

Usage within your main configuration class:
````java
@Getter
@Setter
public final class ExampleConfig extends YamlConfig {

    public ExampleConfig(Path file) {
        this.setConfigFile(file.toFile());
        this.setConfigMode(ConfigMode.DEFAULT);
    }

    @Path("example.section")
    private ExampleSection section = new ExampleSection("someField", "otherField");
}
````

### 4. Create your own Converter
Converters serialize custom or third-party classes (like Bukkit's `Block` or `Location`). With the statically typed `Converter<T, R>` interface, `T` represents your custom target type and `R` represents the serialized configuration output (usually `Object` or `Map`).

Implement the `Converter<T, R>` interface:
* `toConfig(Class<?>, T obj, ParameterizedType)`: Converts your strongly-typed object `obj` into a serializable structure.
* `fromConfig(Class<?>, R obj, ParameterizedType)`: Deserializes the configuration data back into your typed object `T`.
* `supports(Class<?>)`: Specifies which classes this converter can handle.
```java
public class BlockConverter implements Converter<Block, Object> {

    private final InternalConverter internalConverter;

    // this constructor is optional
    // no args constructors are also supported
    public BlockConverter(InternalConverter internalConverter) {
        this.internalConverter = internalConverter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object toConfig(Class<?> type, Block obj, ParameterizedType parameterizedType) {
        Converter<Object, Object> locationConverter = (Converter<Object, Object>) internalConverter.getConverter(Location.class);
        if (locationConverter == null) {
            throw new IllegalStateException("Could not find converter for " + Location.class.getCanonicalName());
        }

        Map<String, Object> saveMap = new HashMap<>();
        saveMap.put("type", obj.getType());
        saveMap.put("location", locationConverter.toConfig(Location.class, obj.getLocation(), null));

        return saveMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Block fromConfig(Class<?> type, Object obj, ParameterizedType parameterizedType) {
        ConfigSection section = (ConfigSection) obj;
        Map<String, Object> blockMap = (Map<String, Object>) section.getRawMap();

        ConfigSection locationSection = (ConfigSection) blockMap.get("location");
        Map<String, Object> locationMap = (Map<String, Object>) locationSection.getRawMap();

        Location location = new Location(
            Bukkit.getWorld((String) locationMap.get("world")),
            (Double) locationMap.get("x"),
            (Double) locationMap.get("y"),
            (Double) locationMap.get("z")
        );

        Block block = location.getBlock();
        block.setType((Material) blockMap.get("type"));

        return block;
    }

    @Override
    public boolean supports(Class<?> type) {
        return Block.class.isAssignableFrom(type);
    }
}
```

Register the converter inside your config constructor:
```java
@Getter
@Setter
public final class ExampleConfig extends YamlConfig {

    public ExampleConfig(Path file) {
        this.setConfigFile(file.toFile());
        this.setConfigMode(ConfigMode.DEFAULT);

        try {
            this.addConverter(BlockConverter.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register BlockConverter", e);
        }
    }

    @Path("example.block")
    private Block block = new Block();
}
```

## ⚙️ Build

Parser-Lib uses Gradle to handle dependencies & building. <br />

#### Requirements:
* Java 25 JDK or newer
* Git
* Gradlew installed

#### Compiling from source
```sh
git clone https://github.com/einnxk/parser-lib.git
cd Parser-Lib/
./gradlew clean build
```

## 📄 License
Parser-Lib is licensed under the Apache 2 license. Please see the [`LICENSE`](https://github.com/einnxk/parser-lib/blob/master/LICENSE) for more info.

> [!NOTE]
> Special Thanks: The YAML parser is a hard-fork derived from [Cube-Space/Yamler](https://github.com/Cube-Space/Yamler), rewritten and modernized in Kotlin with enhanced feature support. ♥️
