plugins {
    id("dev.slne.surf.api.gradle.velocity")
}

surfVelocityApi {
    withSurfRedis()
}

velocityPluginFile {
    main = "dev.slne.surf.transaction.velocity.VelocityMain"
}

dependencies {
    api(projects.surfTransactionCore.surfTransactionCoreClient)
}