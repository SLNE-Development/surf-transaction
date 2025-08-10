rootProject.name = "surf-transaction"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include("surf-transaction-api")
include("surf-transaction-paper")
include("surf-transaction-velocity")
include("surf-transaction-core:surf-transaction-core-common")
include("surf-transaction-core:surf-transaction-core-client")
include("surf-transaction-server")