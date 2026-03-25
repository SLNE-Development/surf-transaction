import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
    id("dev.slne.surf.microservice")
}

surfMicroservice {
    withRabbitModule(RabbitModule.CLIENT_API)
}

dependencies {
    api(project(":surf-transaction-core:surf-transaction-core-client"))
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.transaction.paper.PaperMain")
    authors.addAll("Ammo", "twisti")
    generateLibraryLoader(false)
    foliaSupported(true)
    withSurfRedis()
    withCorePaper()
}