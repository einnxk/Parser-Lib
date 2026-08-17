plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.6.1"
    id("com.diffplug.spotless") version "8.9.0"
    kotlin("jvm") version "2.4.10"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

allprojects {
    group = "com.github.einnxk.parser"
    version = "4.1.2-SNAPSHOT"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    java {
        withSourcesJar()
        withJavadocJar()

        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
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

    dependencies {
        implementation(rootProject.libs.lombok)
        annotationProcessor(rootProject.libs.lombok)
        implementation(rootProject.libs.jetbrains)
        implementation(rootProject.libs.jspecify)

        // tests
        testImplementation(platform("org.junit:junit-bom:6.1.2"))
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
    }
}