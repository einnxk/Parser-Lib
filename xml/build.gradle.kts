dependencies {
    api(project(":common"))

    implementation(libs.xml)
    implementation(libs.xml.kotlin)
}

tasks.shadowJar {
    archiveClassifier.set("")

    relocate("com.fasterxml.jackson", "com.github.einnxk.libs.xml")
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