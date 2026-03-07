enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "surf-transaction"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include("surf-transaction-api:surf-transaction-api")
include("surf-transaction-api:surf-transaction-api-paper")

include("surf-transaction-core")
include("surf-transaction-paper")
include("surf-transaction-velocity")