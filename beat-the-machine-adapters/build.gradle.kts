plugins {
    jacoco
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.openapi.contracts)
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

    implementation(platform(libs.spring.ai.bom))
    implementation(libs.bundles.spring.ai.starters)

    testImplementation(libs.bundles.unit.test)
    testImplementation(libs.bundles.spring.boot.test) {
        exclude("org.mockito", "mockito-core")
    }
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(testFixtures(project(":beat-the-machine-domain")))
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

tasks.jacocoTestReport {
    reports {
        xml.required = true
    }
    classDirectories.setFrom(
        classDirectories.files.map {
            fileTree(it) { exclude("**/openapi/**", "**/openapitools/**") }
        },
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.named("test"))
    classDirectories.setFrom(
        classDirectories.files.map {
            fileTree(it) { exclude("**/openapi/**", "**/openapitools/**") }
        },
    )
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

val copyWebApp by tasks.registering(Sync::class) {
    dependsOn(":beat-the-machine-frontend:buildWebApp")
    from(project(":beat-the-machine-frontend").layout.projectDirectory.dir("dist"))
    into(layout.buildDirectory.dir("generated/web/static"))
}

sourceSets.main {
    resources.srcDir(layout.buildDirectory.dir("generated/web"))
}

tasks.named("processResources") { dependsOn(copyWebApp) }

openapiContracts {
    directoryPath.set(
        rootProject.layout.projectDirectory
            .dir("docs/openapi")
            .asFile.absolutePath,
    )
    servers {
        register("BeatTheMachineApi") {
            spec.set("beat-the-machine-openapi.yaml")
            packageName.set("com.yonatankarp.beatthemachine.openapi.v1")
            modelPackageName.set("com.yonatankarp.beatthemachine.openapi.v1.models")
        }
    }
}
