plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "9.4.2" apply false
    id("com.diffplug.spotless") version "7.2.1"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

allprojects {
    group = "com.github.einnxk.parser"
    version = "3.0.11-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "com.diffplug.spotless")

    java {
        withSourcesJar()
        withJavadocJar()
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(
            listOf(
                "-Xlint:all",
                "-Xlint:-path"
            )
        )
    }

    repositories {
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        implementation(rootProject.libs.lombok)
        annotationProcessor(rootProject.libs.lombok)
        implementation(rootProject.libs.jetbrains)
        implementation(rootProject.libs.jspecify)

        // tests
        testImplementation(platform("org.junit:junit-bom:6.0.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("org.assertj:assertj-core:3.27.7")
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.processResources {
        val buildNumber = System.getenv("BUILD_NUMBER") ?: "0"

        inputs.property("version", project.version)
        inputs.property("name", project.name)
        inputs.property("buildNumber", buildNumber)

        filesMatching(listOf("**/*.yml", "**/*.properties")) {
            expand(
                "version" to project.version,
                "project.version" to project.version,
                "project.name" to project.name,
                "build.number" to buildNumber,
                "build" to mapOf(
                    "number" to buildNumber
                ),
                "project" to mapOf(
                    "version" to project.version,
                    "name" to project.name
                )
            )
        }
    }

    spotless {
        java {
            licenseHeaderFile(rootProject.file("config/license-header.txt"), "^(package|import|module) ")
        }
        kotlin {
            licenseHeaderFile(rootProject.file("config/license-header.txt"), "^(package|import|module) ")
        }
    }
}