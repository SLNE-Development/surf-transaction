import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.surfapi.gradle.core")
    id("dev.slne.surf.microservice")
}

surfCoreApi {
    withSurfRedis()
    withSurfDatabaseR2dbc("1.3.0", "dev.slne.surf.transaction.libs.db")
}

surfMicroservice {
    withRabbitModule(RabbitModule.SERVER_API, true)
    withMicroserviceApi()
}

dependencies {
    implementation(projects.surfTransactionCore.surfTransactionCoreCommon)
}