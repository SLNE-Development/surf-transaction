import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.surfapi.gradle.core")
    id("dev.slne.surf.microservice")
}

surfCoreApi {
    withSurfDatabaseR2dbc("1.3.0", "dev.slne.surf.transaction.libs.db")
    withSurfRedis()
}

surfMicroservice {
    withRabbitModule(RabbitModule.SERVER_API)
    withMicroserviceApi()
}

dependencies {
    implementation(project(":surf-transaction-core:surf-transaction-core-common"))
}
