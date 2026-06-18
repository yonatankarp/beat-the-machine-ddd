plugins {
    id("jacoco")
    id("beat-the-machine.spotless")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    implementation(project(":beat-the-machine-application"))
    implementation(project(":beat-the-machine-domain"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.xerial:sqlite-jdbc")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("org.mockito:mockito-core")
    }
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-restclient")
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)
}

tasks.bootJar {
    archiveFileName.set("adapters.jar")
}

tasks {
    getByName<Jar>("jar") {
        enabled = false
    }

    build {
        finalizedBy(spotlessApply)
    }

    withType<Test> {
        useJUnitPlatform()
        finalizedBy(spotlessApply)
        finalizedBy(jacocoTestReport)
    }
}

tasks.findByName("spotlessKotlin")?.dependsOn("compileKotlin")
tasks.findByName("spotlessKotlin")?.dependsOn("compileTestKotlin")
tasks.findByName("spotlessKotlin")?.dependsOn("test")
tasks.findByName("spotlessKotlin")?.dependsOn("jacocoTestReport")
