import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    `java-library`

    id("xyz.jpenilla.run-velocity")
}

val libs = the<LibrariesForLibs>()

dependencies {
    compileOnlyApi(libs.velocity.api)
    annotationProcessor(libs.velocity.api)

    api(libs.commandapi.velocity)
    api(libs.commandapi.velocity.kotlin)
    api(libs.mccoroutine.velocity)
    api(libs.mccoroutine.velocity.core)
}

tasks.runVelocity {
    velocityVersion(libs.versions.velocity.server.get())

    downloadPlugins {
        url("https://s01.oss.sonatype.org/content/repositories/snapshots/dev/jorel/commandapi-velocity-plugin/9.6.0-SNAPSHOT/commandapi-velocity-plugin-9.6.0-20241023.203559-32.jar")
    }
}