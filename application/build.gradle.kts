plugins {
    id("beat-the-machine.code-metrics")
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(project(":domain"))
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.11")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
