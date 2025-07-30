import dev.slne.surf.surfapi.gradle.util.registerRequired

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

dependencies {
    api(project(":surf-transaction-core:surf-transaction-core-client"))
    compileOnly("dev.slne.surf.cloud:surf-cloud-api-client-paper:1.21.7+")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.transaction.paper.BukkitMain")
    bootstrapper("dev.slne.surf.transaction.paper.PaperBootstrap")
    authors.addAll("Ammo", "twisti")
    generateLibraryLoader(false)

    bootstrapDependencies {
        registerRequired("surf-cloud-bukkit")
    }
    serverDependencies {
        registerRequired("surf-cloud-bukkit")
    }
}