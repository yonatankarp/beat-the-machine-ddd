plugins {
    id("beat-the-machine.code-metrics")
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
    testImplementation("io.mockk:mockk:1.14.11")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.findByName("spotlessKotlin")?.dependsOn("compileKotlin")
tasks.findByName("spotlessKotlin")?.dependsOn("compileTestKotlin")
tasks.findByName("spotlessKotlin")?.dependsOn("test")
