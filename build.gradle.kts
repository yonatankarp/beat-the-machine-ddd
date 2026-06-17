plugins {
    id("com.diffplug.spotless") version "8.7.0" apply false
    id("org.springframework.boot") version "4.1.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    val kotlinVersion = "2.4.0"
    id("org.jetbrains.kotlin.jvm") version kotlinVersion apply false
    id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion apply false
}

subprojects {
    repositories {
        mavenCentral()
    }
}
