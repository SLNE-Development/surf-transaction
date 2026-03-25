plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

surfCoreApi {
    withSurfDatabaseR2dbc("1.3.0", "dev.slne.surf.transaction.libs.db")
}

dependencies {
    api(projects.surfTransactionApi)
}