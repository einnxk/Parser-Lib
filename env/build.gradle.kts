dependencies {
    implementation(project(":common"))
}

tasks.shadowJar {
    archiveClassifier.set("")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["shadowJar"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            artifactId = "env"
        }
    }
}

artifacts {
    add("shadow", tasks.shadowJar)
}

tasks.jar {
    enabled = false
}