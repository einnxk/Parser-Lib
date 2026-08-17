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

            artifactId = "properties"
        }
    }
}

artifacts {
    add("shadow", tasks.shadowJar)
}

tasks.jar {
    enabled = false
}