plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

dependencies {
    api(project(":surf-transaction-core"))
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.cloud.bukkit.BukkitMain")
    bootstrapper("dev.slne.surf.cloud.bukkit.BukkitBootstrap")
    authors.add("Ammo")

    runServer {
        jvmArgs("-Dsurf.cloud.serverName=test-server01")
    }
}