plugins {
    id("beat-the-machine.kotlin-conventions")
    `java-test-fixtures`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.assertions.core)
}
