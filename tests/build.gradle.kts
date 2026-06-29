plugins {
    kotlin("jvm") version "2.3.21"
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    testImplementation(project(":common"))
    testImplementation(project(":json", configuration = "shadow"))
    testImplementation(project(":yamler-core", configuration = "shadow"))
    testImplementation(project(":properties", configuration = "shadow"))
    testImplementation(project(":hocon", configuration = "shadow"))
    testImplementation(project(":xml", configuration = "shadow"))
    testImplementation(project(":toml", configuration = "shadow"))
}

tasks.test {
    useJUnitPlatform()
}