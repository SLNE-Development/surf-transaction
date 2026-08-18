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
