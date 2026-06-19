plugins {
    jacoco
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvm.get()))
    }
}

dependencies {
    implementation(project(":beat-the-machine-application"))
    implementation(project(":beat-the-machine-domain"))

    implementation(libs.bundles.spring.boot.starters)
    implementation(libs.sqlite.jdbc)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.bundles.kotlinx.coroutines)

    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.bundles.spring.boot.test) {
        exclude("org.mockito", "mockito-core")
    }
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.bootJar {
    archiveFileName.set("adapters.jar")
}

tasks {
    getByName<Jar>("jar") {
        enabled = false
    }

    withType<Test> {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }
}
