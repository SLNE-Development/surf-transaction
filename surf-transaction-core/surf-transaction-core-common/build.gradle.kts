plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

surfCoreApi {
    withCloudCommon()
}

dependencies {
    api(project(":surf-transaction-api"))
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.transaction.api.util.InternalTransactionApi")
    }
}