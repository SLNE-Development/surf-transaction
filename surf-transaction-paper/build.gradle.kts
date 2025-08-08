import dev.slne.surf.surfapi.gradle.util.registerRequired

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

dependencies {
    api(project(":surf-transaction-core:surf-transaction-core-client"))
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.transaction.paper.PaperMain")
    bootstrapper("dev.slne.surf.transaction.paper.PaperBootstrap")
    authors.addAll("Ammo", "twisti")
    generateLibraryLoader(false)

    withCloudClientPaper()
    
    bootstrapDependencies {
        registerRequired("surf-cloud-bukkit")
    }
    serverDependencies {
        registerRequired("surf-cloud-bukkit")
    }
}