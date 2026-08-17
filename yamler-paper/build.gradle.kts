dependencies {
    implementation(project(":yamler-core", configuration = "shadow"))

    compileOnly(libs.paper)
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifactId = "yamler-paper"
        }
    }
}