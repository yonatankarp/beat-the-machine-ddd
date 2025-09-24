plugins {
    id("beat-the-machine.kotlin-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.assertions.core)
}
