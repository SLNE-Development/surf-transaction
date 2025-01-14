plugins {
    `common-conventions`
}

dependencies {
    api(project(":surf-transaction-api"))

    compileOnly(libs.coroutines)
}