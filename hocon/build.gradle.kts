plugins {
    kotlin("jvm") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(rootProject.libs.hocon)
    implementation(rootProject.libs.hocon.jackson)
    implementation(rootProject.libs.hocon.jackson.kotlin)
}

kotlin {
    jvmToolchain(25)
}

tasks.shadowJar {
    archiveClassifier.set("")

    relocate("com.fasterxml.jackson", "com.github.einnxk.libs.xml")
    relocate("com.typesafe.config", "com.github.einnxk.libs.typesafe")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["shadowJar"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            artifactId = "hocon"
        }
    }
}

artifacts {
    add("shadow", tasks.shadowJar)
}

tasks.jar {
    enabled = false
}