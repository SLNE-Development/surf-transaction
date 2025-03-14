rootProject.name = "surf-transaction"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

val projects: List<Pair<String, String>> = listOf(
    "surf-transaction-api" to "SurfTransactionApi",
    "surf-transaction-core" to "SurfTransactionCore",
    "surf-transaction-bukkit" to "SurfTransactionBukkit",
    "surf-transaction-velocity" to "SurfTransactionVelocity",
    "surf-transaction-fallback" to "SurfTransactionFallback"
)

projects.forEach { (path, _) ->
    include(path)
}
