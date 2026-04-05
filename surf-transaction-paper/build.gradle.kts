import dev.slne.surf.surfapi.gradle.util.registerRequired

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

dependencies {
    api(projects.surfTransactionCore.surfTransactionCoreClient)
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.transaction.paper.PaperMain")
    authors.addAll("Ammo", "twisti")
    foliaSupported(true)

    serverDependencies {
        registerRequired("surf-rabbitmq-paper")
    }

    withSurfRedis()
}