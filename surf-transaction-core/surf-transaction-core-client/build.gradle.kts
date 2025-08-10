plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

surfCoreApi {
    withCloudClientCommon()
}

dependencies {
    api(project(":surf-transaction-core:surf-transaction-core-common"))
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.transaction.api.util.InternalTransactionApi")
    }
}