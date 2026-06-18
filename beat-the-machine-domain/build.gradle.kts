plugins {
    id("beat-the-machine.spotless")
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.findByName("spotlessKotlin")?.dependsOn("compileKotlin")
tasks.findByName("spotlessKotlin")?.dependsOn("compileTestKotlin")
tasks.findByName("spotlessKotlin")?.dependsOn("test")
