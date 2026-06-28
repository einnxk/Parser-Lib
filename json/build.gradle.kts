plugins {
    kotlin("jvm") version "2.3.21"
    id("com.gradleup.shadow") version "9.4.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(rootProject.libs.gson)

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.shadowJar {
    archiveClassifier.set("")

    relocate(
        "org.yaml.snakeyaml",
        "de.einnik.yamler_v3.libs.snakeyaml"
    )
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["shadowJar"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            artifactId = "Yamler-Core"
        }
    }
}

artifacts {
    add("shadow", tasks.shadowJar)
}

tasks.jar {
    enabled = false
}