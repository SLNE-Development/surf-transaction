import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("dev.slne.surf.surfapi.gradle.velocity")
}

surfVelocityApi {
    withCloudClientVelocity()
}

velocityPluginFile {
    main = "dev.slne.surf.transaction.velocity.VelocityMain"
    pluginDependencies {
        register("surf-cloud-velocity")
    }
}

dependencies {
    api(project(":surf-transaction-core:surf-transaction-core-client"))
}

tasks.withType<ShadowJar> {
    destinationDirectory.set(rootProject.file("output"))
}