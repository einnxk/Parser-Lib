dependencies {
    implementation(project(":common"))
    implementation(libs.gson)
}

tasks.shadowJar {
    archiveClassifier.set("")

    relocate("com.google.gson", "com.github.einnxk.libs.gson")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["shadowJar"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            artifactId = "json"
        }
    }
}

artifacts {
    add("shadow", tasks.shadowJar)
}

tasks.jar {
    enabled = false
}