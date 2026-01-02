plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

surfCoreApi {
    withSurfDatabaseR2dbc("1.0.0-SNAPSHOT", "dev.slne.surf.transaction.libs.db")
    withSurfRedis()
}

dependencies {
    api(project(":surf-transaction-api"))
}

kotlin {
    compilerOptions {
        optIn.add("dev.slne.surf.transaction.api.util.InternalTransactionApi")
    }
}