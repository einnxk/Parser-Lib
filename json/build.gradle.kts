plugins {
    kotlin("jvm") version "2.3.21"
    id("com.gradleup.shadow") version "9.4.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(rootProject.libs.gson)
}

kotlin {
    jvmToolchain(25)
}

tasks.shadowJar {
    archiveClassifier.set("")

    relocate(
        "com.google.gson",
        "com.github.einnxk.libs.gson"
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