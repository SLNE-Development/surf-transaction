plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    api(project(":surf-transaction-core:surf-transaction-core-common"))
    compileOnly("dev.slne.surf.cloud:surf-cloud-api-client-common:1.21.7+")
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.transaction.api.util.InternalTransactionApi")
    }
}