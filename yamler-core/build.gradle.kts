dependencies {
    implementation(project(":common"))

    implementation(libs.snake.yml)
}

tasks.shadowJar {
    archiveClassifier.set("")

    relocate("org.yaml.snakeyaml", "com.github.einnxk.libs.snakeyaml")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["shadowJar"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            artifactId = "yamler-core"
        }
    }
}

artifacts {
    add("shadow", tasks.shadowJar)
}

tasks.jar {
    enabled = false
}