rootProject.name = "surf-transaction"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val projects: List<Pair<String, String>> = listOf(
    "surf-transaction-api" to "SurfTransactionApi",
    "surf-transaction-bukkit" to "SurfTransactionBukkit",
    "surf-transaction-velocity" to "SurfTransactionVelocity",
//    "surf-transaction-fallback" to "SurfTransactionFallback"
)

projects.forEach { (path, _) ->
    include(path)
}

include("surf-transaction-core:surf-transaction-core-common")
include("surf-transaction-core:surf-transaction-core-client")
include("surf-transaction-server")