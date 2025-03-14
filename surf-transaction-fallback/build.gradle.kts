plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    api(project(":surf-transaction-core"))

    api(libs.surf.database)
}