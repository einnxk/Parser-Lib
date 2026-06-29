[![Build](https://img.shields.io/github/actions/workflow/status/einnxk/Parser-Lib/ci.yml?logo=github)](https://github.com/einnxk/Parser-Lib/actions)
[![GitHub release](https://img.shields.io/github/v/release/einnxk/Parser-Lib?logo=github&color=blue)](https://github.com/einnxk/Parser-Lib/releases)

# Parser-Lib
Parser-Lib is a modern Kotlin/Java library which allows easy parsing of files. The following file types are
supported: 

- `yaml`
- `json`
- `properties`
- `toml`
- `xml`
- `hocon`
- `env`

> [!NOTE]
> The YAML parser is a hard-fork from [Cube-Space/Yamler](https://github.com/Cube-Space/Yamler) which is rewritten in modern Kotlin 25 with more features. 
> Special, Thanks! ♥️

## Developer Quick Start

### Adding Artifacts  & Repo via. Gradle
Start by adding the `jitpack` repository to your project with the artifacts you want to use. 
```kts
repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
}

dependencies {
    // common is required by all modules
    implementation("com.github.einnxk.parser:common:<version>")
    /* 
    Replace <module> with one of the available modules, currently available modules are
    yamler-core, yamler-paper, json, properties, toml, xml, hocon & env.
    Replace <version> with the latest version, at the current moment this is 4.0.0-SNAPSHOT
    */
    implementation("com.github.einnxk.parser:<module>:<version>")
}
```

### Define your Config
YamlConfig is an example if you want to parse YAML file, but all follow the pattern `<File-Suffix>Config`. 
In some Languages doesn't allow `.` these are automatically replaced.

The value you asign in your class is the default value which is created if no file or value is available. 
`static`, `final` `transient ` fields are excluded, if you want to parse static files anyways use `@PreserveStatic`.
````java
@Getter
@Setter
public class Example extends YamlConfig {
    
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

    @Required
    @Range(min = 26, max = 64)
    private Set<String> something = new ArrayList<>();
    
    @PreserveStatic
    private static String staticExample = "ExampleString";
}
````

### Init, load, save & reload your file
These methods are always the same, ignoring the file type you use. 
> [!CAUTION]
> All parent directories are created automatically 
````java
Example example = new Example(file);
// Create the File with default values if not exists, or load from the disk.         
example.init();
// load the file without creating it, if it doesn't exist
example.load();
// reload the file from the disk - Fields modified at the runtime in the class are dumped
example.reload();
// save the file with the field values from the class on the disk
example.save();
````

### Create your own Sections
Sections are used to define a collection of fields that are highlighted. Some file types makes this automatically,
like the JSON Parser.
````java

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstrcutor
public class ExampleSection extends ConfigSection {
    private String someField;
    private String otherField;
}
````
Now you can use this section as a filed in your configuration class. 
````java
@Getter
@Setter
public class Example extends YamlConfig {
    
    public Example(Path file) {
        this.setConfigFile(file.toFile());
        this.setConfigMode(ConfigMode.DEFAULT);
    }
    
    @Path("exmaple.section")
    private ExampleSection section = new ExampleSection("someFiled", "otherField");
}
````

### Create your own Converter 
Converters are used to serialize classes that you specially use in your project.

You need to return an `Object` at the method `toConfig(...)` and parse the object you returned back to its original type at the Method `fromConfig(...)`. 
At the ``supports(...)`` Method you need to describe what your classes your converter can convert. 
````kotlin
open class BlockConverter(private val internalConverter: InternalConverter) : Converter {

    override fun toConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val block: Block = obj as Block
        val locationConverter: Converter = internalConverter.getConverter(Location::class.java)
            ?: throw IllegalStateException("Could not find converter for ${obj.javaClass.canonicalName}")

        val saveMap: MutableMap<String, Any> = mutableMapOf()
        saveMap["type"] = block.type
        saveMap["location"] = locationConverter.toConfig(Location::class.java, block.location, null)!!

        return saveMap
    }

    override fun fromConfig(type: Class<*>?, obj: Any?, parameterizedType: ParameterizedType?): Any {
        val blockMap = (obj as ConfigSection).getRawMap() as MutableMap<String?, Any?>
        val locationMap = (blockMap["location"] as ConfigSection).getRawMap() as MutableMap<String?, Any?>

        val location = Location(
            Bukkit.getWorld(locationMap["world"] as String),
            locationMap["x"] as Double,
            locationMap["y"] as Double,
            locationMap["z"] as Double
        )
        val block = location.block

        block.type = blockMap["type"] as Material

        return block
    }

    override fun supports(type: Class<*>): Boolean {
        return Block::class.java.isAssignableFrom(type)
    }
}
````
Now you need to register that converter in whatever config class you want to use that type. 
```java
@Getter
@Setter
public class Example extends YamlConfig {
    
    public Example(Path file) {
        this.setConfigFile(file.toFile());
        this.setConfigMode(ConfigMode.DEFAULT);
        
        try {
            this.addConverter(BlockConverter.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Path("exmaple.block")
    private Block block = new Block();
}
```

## Build

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

## License
Parser-Lib is licensed under the Apache 2 license. Please see the [`LICENSE`](https://github.com/einnxk/parser-lib/blob/master/LICENSE) for more info.
 
