plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "surf-transaction"

val projects: List<Pair<String, String>> = listOf(
    "surf-transaction-api" to "SurfTransactionApi",
    "surf-transaction-core" to "SurfTransactionCore",
    "surf-transaction-bukkit" to "SurfTransactionBukkit",
    "surf-transaction-velocity" to "SurfTransactionVelocity",
    "surf-transaction-standalone" to "SurfTransactionStandalone"
)

projects.forEach { (path, _) ->
    include(path)
}
