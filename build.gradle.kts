plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "9.4.2" apply false
}

tasks.withType<Test> {
    useJUnitPlatform()
}

allprojects {
    group = "com.github.einnxk"
    version = "3.0.11-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

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
    }

    tasks.test {
        useTestNG()
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
}