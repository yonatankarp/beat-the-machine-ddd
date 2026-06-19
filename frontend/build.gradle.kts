plugins {
    alias(libs.plugins.node.gradle)
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

val buildWebApp by tasks.registering(com.github.gradle.node.npm.task.NpmTask::class) {
    dependsOn(tasks.named("npmInstall"))
    npmCommand.set(listOf("run", "build"))
    inputs.dir("src")
    inputs.file("package.json")
    inputs.file("vite.config.ts")
    outputs.dir(layout.projectDirectory.dir("dist"))
}
