plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

dependencies {
    api(project(":surf-transaction-paper:surf-transaction-paper-api"))
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.transaction.paper.PaperMain")
    authors.addAll("Ammo", "twisti")
    generateLibraryLoader(false)
    foliaSupported(true)
    withSurfRedis()
}