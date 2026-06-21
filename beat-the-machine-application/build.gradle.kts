plugins {
    jacoco
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.testballoon)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvm.get()))
    }
}

dependencies {
    implementation(project(":beat-the-machine-domain"))
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(project(":beat-the-machine-domain")))
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
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.named("test"))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
