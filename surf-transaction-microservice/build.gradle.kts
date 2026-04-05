import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.api.gradle.core")
    id("dev.slne.surf.microservice")
}

surfCoreApi {
    withSurfDatabaseR2dbc("1.4.0", "dev.slne.surf.transaction.libs.db")
}

surfMicroservice {
    withRabbitModule(RabbitModule.SERVER_API, true)
    withMicroserviceApi()
}

dependencies {
    implementation(projects.surfTransactionCore.surfTransactionCoreCommon)
}