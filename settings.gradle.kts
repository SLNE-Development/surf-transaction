pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://reposilite.slne.dev/releases")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.slne.surf.api.gradle.settings") version "+"
}

rootProject.name = "surf-transaction"

include("surf-transaction-api")
include("surf-transaction-core:surf-transaction-core-common")
include("surf-transaction-core:surf-transaction-core-client")
include("surf-transaction-paper")
include("surf-transaction-minestom")
include("surf-transaction-velocity")

include("surf-transaction-microservice")