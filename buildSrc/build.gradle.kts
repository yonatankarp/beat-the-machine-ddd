plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.openapitools:openapi-generator-gradle-plugin:${libs.versions.swagger.get()}")
}
