plugins {
    id("jacoco")
    id("beat-the-machine.kotlin-conventions") apply true
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.springboot.dependency.management) apply false
    alias(libs.plugins.springboot) apply false
    alias(libs.plugins.openapi.generator) apply false
}

repositories {
    mavenCentral()
}

tasks.named("pitest") {
    enabled = false
}
