import dev.slne.surf.surfapi.gradle.util.slneReleases

plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    compileOnly("dev.slne.surf.cloud:surf-cloud-api-common:1.21.7+")
}

publishing {
    repositories {
        slneReleases()
    }
}