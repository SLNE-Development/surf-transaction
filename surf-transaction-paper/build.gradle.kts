plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

dependencies {
    api(projects.surfTransactionApi.surfTransactionApiPaper)
    api(projects.surfTransactionCore)
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.transaction.paper.PaperMain")
    authors.addAll("Ammo", "twisti")
    generateLibraryLoader(false)
    foliaSupported(true)
    withSurfRedis()
}