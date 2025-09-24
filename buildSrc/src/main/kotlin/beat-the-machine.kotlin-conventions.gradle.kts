import com.saveourtool.diktat.plugin.gradle.tasks.DiktatCheckTask
import com.saveourtool.diktat.plugin.gradle.tasks.DiktatFixTask
import org.gradle.kotlin.dsl.dependencies
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("jacoco")
    id("io.gitlab.arturbosch.detekt")
    id("com.saveourtool.diktat")
    id("com.diffplug.spotless")
    id("info.solidsoft.pitest")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.12"
}

spotless {
    kotlin {
        target("src/**/*.kt")
        targetExclude("**/build/**", "**/.gradle/**")
        ktlint("1.5.0")
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**", "**/.gradle/**")
        ktlint("1.5.0")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = true
}

val diktatConfig = file("$rootDir/config/diktat/diktat-analysis.yml")

tasks.withType<DiktatCheckTask>().configureEach {
    configFile.set(diktatConfig)
}

tasks.withType<DiktatFixTask>().configureEach {
    configFile.set(diktatConfig)
}

diktat {
    inputs {
        include("src/**/*.kt")
        exclude("**/build/**", "**/.gradle/**")
    }
    reporters {
        plain()
        html {
            output =
                file("${layout.buildDirectory.get()}/reports/diktat/diktat.html")
        }
    }
    debug = false
    ignoreFailures = false
    diktatConfigFile = project.rootProject.file("diktat/diktat.yml")
}

tasks.withType<JacocoReport>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

val jacocoCoverageVerification by tasks.registering(JacocoCoverageVerification::class) {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                minimum = "0.97".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "BRANCH"
                minimum = "0.95".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(jacocoCoverageVerification)
    dependsOn("spotlessCheck")
    dependsOn("detekt")
    dependsOn("diktatCheck")
    dependsOn("pitest")
}

pitest {
    junit5PluginVersion = "1.2.3"
    targetClasses = listOf("com.yonatankarp.beatthemachine.*")
    avoidCallsTo = listOf("kotlin.jvm.internal")

    threads = 4
    outputFormats = listOf("XML", "HTML")
    timestampedReports = false
}
