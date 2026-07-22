import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.api.gradle.core")
    id("dev.slne.surf.microservice")
}

repositories {
    mavenLocal()
}

surfCoreApi {
    withSurfDatabaseR2dbc("2.3.1", "dev.slne.surf.transaction.libs.db")
}

surfMicroservice {
    withRabbitModule(RabbitModule.SERVER_API, true)
    withMicroserviceApi()
}

dependencies {
    implementation(projects.surfTransactionCore.surfTransactionCoreCommon)
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.1")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib:2.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    testImplementation("dev.slne.surf.api:surf-api-core:+")
    testRuntimeOnly("net.kyori:adventure-api:5.1.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.1.1")
}

tasks.test {
    useJUnitPlatform()
}