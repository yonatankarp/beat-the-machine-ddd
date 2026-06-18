plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    implementation(project(":beat-the-machine-domain"))
    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
