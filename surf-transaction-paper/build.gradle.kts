import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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
    foliaSupported(true)

    withCloudClientPaper()
}

tasks.withType<ShadowJar> {
    destinationDirectory.set(rootProject.file("output"))
}