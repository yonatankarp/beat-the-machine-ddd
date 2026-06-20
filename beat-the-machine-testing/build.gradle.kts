plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.dependency.management)
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

dependencies {
    api(libs.testballoon.core)
    api(libs.spring.boot.starter.test) {
        exclude("org.mockito", "mockito-core")
    }
    api(libs.spring.boot.starter.webflux.test)
    api(libs.mockk)
    implementation(libs.kotlin.reflect)
}
