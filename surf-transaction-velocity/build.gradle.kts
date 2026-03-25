import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.surfapi.gradle.velocity")
    id("dev.slne.surf.microservice")
}

surfVelocityApi {
    withSurfRedis()
    withCoreVelocity()
}

surfMicroservice {
    withRabbitModule(RabbitModule.CLIENT_API)
}

velocityPluginFile {
    main = "dev.slne.surf.transaction.velocity.VelocityMain"
}

dependencies {
    api(project(":surf-transaction-core:surf-transaction-core-client"))
}