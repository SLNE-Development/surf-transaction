import dev.slne.surf.surfapi.gradle.util.slnePublic
import dev.slne.surf.surfapi.gradle.util.slneReleases

buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
    }
    dependencies {
        classpath("dev.slne.surf:surf-api-gradle-plugin:1.21.7+")
    }
}

plugins {
    java
}


allprojects {
    group = "dev.slne.surf"
    version = findProperty("version") as String
}

subprojects {
    apply(plugin = "java")
    repositories {
        slnePublic()
        slneReleases()
    }
    dependencies {
        implementation(platform("dev.slne.surf.cloud:surf-cloud-bom:1.21.7+"))
    }
}