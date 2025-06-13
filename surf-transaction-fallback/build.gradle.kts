plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    implementation(project(":surf-transaction-core"))
    implementation(libs.surf.database)
}