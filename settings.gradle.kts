pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.slne.surf.surfapi.gradle.settings") version "1.21.11+"
}

rootProject.name = "surf-transaction"

include("surf-transaction-api")
include("surf-transaction-core")
include("surf-transaction-core:surf-transaction-core-common")
include("surf-transaction-core:surf-transaction-core-client")
include("surf-transaction-microservice")
include("surf-transaction-paper")
include("surf-transaction-velocity")
