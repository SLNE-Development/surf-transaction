import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

surfCoreApi {
    withCloudServer()
    migrationMainClass("dev.slne.surf.transaction.server.GenerateExposedMigrationScriptKt")
}

dependencies {
    api(project(":surf-transaction-core:surf-transaction-core-common"))
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.transaction.api.util.InternalTransactionApi")
    }
}

tasks.withType<ShadowJar> {
    destinationDirectory.set(rootProject.file("output"))
}