plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    implementation(project(":surf-transaction-core:surf-transaction-core-common"))
    implementation(libs.surf.database)
}