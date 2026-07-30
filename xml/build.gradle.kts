plugins {
    kotlin("jvm") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(rootProject.libs.xml)
    implementation(rootProject.libs.xml.kotlin)
}

kotlin {
    jvmToolchain(25)
}

tasks.shadowJar {
    archiveClassifier.set("")

    relocate(
        "com.fasterxml.jackson",
        "com.github.einnxk.libs.xml"
    )
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["shadowJar"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            artifactId = "xml"
        }
    }
}

artifacts {
    add("shadow", tasks.shadowJar)
}

tasks.jar {
    enabled = false
}