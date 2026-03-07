@file:OptIn(ExperimentalAbiValidation::class)

import dev.slne.surf.surfapi.gradle.util.slneReleases
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-raw")
}

dependencies {
    api(projects.surfTransactionApi.surfTransactionApi)
}

kotlin {
    abiValidation {
        enabled = true
        filters {
            excluded {
                annotatedWith.add("dev.slne.surf.transaction.api.util.InternalTransactionApi")
            }
        }
    }
}

publishing {
    repositories {
        slneReleases()
    }
}