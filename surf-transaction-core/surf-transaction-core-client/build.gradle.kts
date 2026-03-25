plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

surfCoreApi {
    withSurfRedis()
}

dependencies {
    api(projects.surfTransactionCore.surfTransactionCoreCommon)
}