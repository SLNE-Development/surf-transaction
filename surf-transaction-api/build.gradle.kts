@file:OptIn(ExperimentalAbiValidation::class)

import dev.slne.surf.surfapi.gradle.util.slneReleases
import jdk.jfr.internal.JVM.exclude
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

kotlin {
    abiValidation {
        enabled = true
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