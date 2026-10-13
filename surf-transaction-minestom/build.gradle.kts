import dev.slne.surf.api.gradle.util.slneReleases
import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.api.gradle.minestom")
    id("dev.slne.surf.microservice")
}

surfMinestomApi {
    withCoreMinestom()
    withSurfRedis()
}

repositories {
    mavenLocal()
}

minestomPluginFile {
    id = "surf-transaction-minestom"
    main = "dev.slne.surf.transaction.minestom.TransactionMinestomPlugin"
    authors = listOf("Ammo", "twisti")

    pluginDependencies {
        register("surf-rabbitmq-minestom")
        register("surf-redis-minestom")
    }
}

surfMicroservice {
    withRabbitModule(RabbitModule.CLIENT_API)
}

dependencies {
    api(projects.surfTransactionCore.surfTransactionCoreClient)
}

publishing {
    repositories {
        slneReleases()
    }
}
