dependencies {
    api(project(":common"))

    implementation(libs.hocon)
    implementation(libs.hocon.jackson)
    implementation(libs.hocon.jackson.kotlin)
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