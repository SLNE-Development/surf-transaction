plugins {
    `common-conventions`
}

dependencies {
    compileOnlyApi(libs.adventure.api)
    compileOnlyApi(libs.adventure.gson)
    compileOnlyApi(libs.fastutil)
}