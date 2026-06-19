rootProject.name = "beat-the-machine"
include(
    "beat-the-machine-domain",
    "beat-the-machine-application",
    "beat-the-machine-adapters",
    "beat-the-machine-web"
)
project(":beat-the-machine-web").projectDir = file("frontend")
