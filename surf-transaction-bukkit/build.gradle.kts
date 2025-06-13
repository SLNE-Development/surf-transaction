plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

dependencies {
    api(project(":surf-transaction-core"))
    implementation(project(":surf-transaction-fallback"))
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.transaction.bukkit.BukkitMain")
    authors.add("Ammo")
}