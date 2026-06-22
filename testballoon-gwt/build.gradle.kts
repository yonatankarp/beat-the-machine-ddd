plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.testballoon)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvm.get()))
    }
}

dependencies {
    api(libs.testballoon.core)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.bundles.unit.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.junit.platform") {
            useVersion(
                libs.versions.junit.platform.launcher
                    .get(),
            )
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
