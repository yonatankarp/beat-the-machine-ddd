import com.github.gradle.node.npm.task.NpmTask

plugins {
    base
    alias(libs.plugins.node.gradle)
    alias(libs.plugins.openapi.generator)
}

node {
    version.set(
        (project.extensions.getByName("versionCatalogs") as org.gradle.api.artifacts.VersionCatalogsExtension)
            .named("libs")
            .findVersion("node")
            .get()
            .requiredVersion
    )
    download.set(true)
    nodeProjectDir.set(layout.projectDirectory)
}

openApiGenerate {
    generatorName.set("typescript-fetch")
    inputSpec.set(
        rootProject.layout.projectDirectory
            .file("docs/openapi/beat-the-machine-openapi.yaml")
            .asFile.absolutePath,
    )
    outputDir.set(layout.projectDirectory.dir("src/generated").asFile.absolutePath)
    configOptions.set(
        mapOf(
            "supportsES6" to "true",
            "withInterfaces" to "true",
            "typescriptThreePlus" to "true",
        ),
    )
    cleanupOutput.set(true)
}

tasks.named("npmInstall") { dependsOn(tasks.named("openApiGenerate")) }

val buildWebApp = tasks.register<NpmTask>("buildWebApp") {
    dependsOn(tasks.named("npmInstall"))
    npmCommand.set(listOf("run", "build"))
    inputs.dir("src")
    inputs.file("package.json")
    inputs.file("vite.config.ts")
    outputs.dir(layout.projectDirectory.dir("dist"))
}

val test = tasks.register<NpmTask>("test") {
    dependsOn(tasks.named("npmInstall"))
    npmCommand.set(listOf("run", "test"))
    inputs.dir("src")
    inputs.file("package.json")
    inputs.file("tsconfig.json")
    inputs.file("vite.config.ts")
    outputs.upToDateWhen { false }
}

tasks.named("check") {
    dependsOn(test)
}
