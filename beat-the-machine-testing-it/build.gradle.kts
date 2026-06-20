plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.testballoon)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jvm.get()))
    }
}

extra["kotlin.version"] = libs.versions.kotlin.get()

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}")
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.junit.platform") useVersion("1.13.4")
        if (requested.group == "org.junit.jupiter") useVersion("5.13.4")
    }
}

dependencies {
    testImplementation(project(":beat-the-machine-testing"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
