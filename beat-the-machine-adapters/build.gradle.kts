plugins {
    jacoco
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.testballoon)
    alias(libs.plugins.openapi.contracts)
}

extra["kotlin.version"] = libs.versions.kotlin.get()

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
    testImplementation(libs.bundles.spring.boot.test)
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
