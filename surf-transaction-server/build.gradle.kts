import java.util.Properties
import kotlin.apply

plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    api(project(":surf-transaction-core:surf-transaction-core-common"))
    compileOnly("dev.slne.surf.cloud:surf-cloud-api-server:1.21.7+")
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.transaction.api.util.InternalTransactionApi")
    }
}

tasks {
    register<JavaExec>("generateExposedMigrationScript") {
        group = "migration"
        description = "Generate Exposed migration script"
        classpath = sourceSets.main.get().allJava
        mainClass.set("dev.slne.surf.transaction.server.GenerateExposedMigrationScriptKt")

        val propertiesFile = file("migration.properties")

        doFirst {
            if (!propertiesFile.exists()) {
                propertiesFile.parentFile.mkdirs()
                propertiesFile.writeText(
                    """
                # Migration database config
                migration.dbUrl=jdbc:mysql://localhost:3306/database
                migration.dbUser=
                migration.dbPassword=
                """.trimIndent()
                )
                throw GradleException("Created 'migration.properties' file. Please enter your credentials and run the task again.")
            }

            val migrationProperties = Properties().apply {
                load(propertiesFile.inputStream())
            }

            val requiredKeys = listOf("migration.dbUrl", "migration.dbUser", "migration.dbPassword")
            val missing =
                requiredKeys.filter { migrationProperties.getProperty(it).isNullOrBlank() }
            if (missing.isNotEmpty()) {
                throw GradleException("'migration.properties' is incomplete. Missing keys: ${missing.joinToString()}")
            }

            systemProperties(
                requiredKeys.associateWith { migrationProperties.getProperty(it) }
            )
        }
    }
}