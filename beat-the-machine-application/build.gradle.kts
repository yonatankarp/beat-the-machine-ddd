plugins {
    id("beat-the-machine.kotlin-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":beat-the-machine-domain"))

    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.assertions.core)
}
