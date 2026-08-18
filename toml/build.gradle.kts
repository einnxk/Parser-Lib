dependencies {
    api(project(":common"))

    implementation(libs.toml)
    implementation(libs.toml.kotlin)
}

tasks.shadowJar {
    archiveClassifier.set("")

    relocate("com.fasterxml.jackson", "com.github.einnxk.libs.toml")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["shadowJar"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            artifactId = "toml"
        }
    }
}

artifacts {
    add("shadow", tasks.shadowJar)
}

tasks.jar {
    enabled = false
}