plugins {
    kotlin("jvm") version "2.3.21"
}

kotlin {
    jvmToolchain(25)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "common"
        }
    }
}