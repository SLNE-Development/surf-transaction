plugins {
    id("dev.slne.surf.surfapi.gradle.velocity")
}

velocityPluginFile {
    main = "dev.slne.surf.transaction.velocity.VelocityMain"
    pluginDependencies {
        register("surf-cloud-velocity")
    }
}

dependencies {
    api(project(":surf-transaction-core:surf-transaction-core-client"))
    compileOnly("dev.slne.surf.cloud:surf-cloud-api-client-velocity:1.21.7+")
}