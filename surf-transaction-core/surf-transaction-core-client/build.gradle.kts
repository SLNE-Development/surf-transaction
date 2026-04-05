import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.surfapi.gradle.core")
    id("dev.slne.surf.microservice")
}

surfMicroservice {
    withRabbitModule(RabbitModule.CLIENT_API)
}

surfCoreApi {
    withSurfRedis()
}

dependencies {
    api(project(":surf-transaction-api"))
    api(project(":surf-transaction-core:surf-transaction-core-common"))
}
