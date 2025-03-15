import dev.slne.surf.surfapi.gradle.util.slneReleases

buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
    }
    dependencies {
        classpath("dev.slne.surf:surf-api-gradle-plugin:1.21.4+")
    }
}


allprojects {
    group = "dev.slne.surf"
    version = findProperty("version") as String
}

subprojects {
    afterEvaluate {
        configure<PublishingExtension> {
            repositories {
                slneReleases()
            }
        }
    }
}