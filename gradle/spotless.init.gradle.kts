// Spotless configuration applied as a Gradle init script, NOT as a project plugin.
//
// This mirrors exactly what CI injects (yonatankarp/github-actions linters.yml runs
// `./gradlew --init-script <this> spotlessCheck`). It is vendored here verbatim so the
// local pre-commit hook (.githooks/pre-commit) enforces the SAME ktlint rules as CI,
// while the project build stays free of the Spotless plugin ("CI owns Spotless").
//
// Source of truth, re-sync if CI changes:
//   https://raw.githubusercontent.com/yonatankarp/github-actions/v2/config/spotless/spotless.gradle.kts
initscript {
    repositories {
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.diffplug.spotless:spotless-plugin-gradle:8.6.0")
    }
}

allprojects {
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        plugins.apply(com.diffplug.gradle.spotless.SpotlessPlugin::class.java)

        extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension>("spotless") {
            kotlin {
                target("**/*.kt")
                ktlint()
            }

            kotlinGradle {
                target("**/*.gradle.kts")
                ktlint()
            }
        }
    }
}
