import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule
import dev.slne.surf.surfapi.gradle.util.slneReleases

plugins {
    id("dev.slne.surf.surfapi.gradle.core")
    id("dev.slne.surf.microservice")
}

surfMicroservice {
    withRabbitModule(RabbitModule.CLIENT_API)
}

surfCoreApi {
    withSurfRedis()
}

publishing {
    repositories {
        slneReleases()
    }
}

dependencies {
    api(project(":surf-transaction-api"))
}
