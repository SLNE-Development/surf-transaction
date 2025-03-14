plugins {
    id("dev.slne.surf.surfapi.gradle.velocity")
}

velocityPluginFile {
    main = "dev.slne.surf.transaction.velocity.VelocityMain"
}


dependencies {
    api(project(":surf-transaction-core"))
    api(project(":surf-transaction-fallback"))
}