@file:OptIn(ExperimentalAbiValidation::class)

import dev.slne.surf.api.gradle.util.slneReleases
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("dev.slne.surf.api.gradle.core")
}

kotlin {
    abiValidation {
        filters {
            exclude {
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
