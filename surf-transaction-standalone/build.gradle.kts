plugins {
    `common-conventions`
    `spring-conventions`
}

dependencies {
    api(project(":surf-transaction-core"))
    compileOnly(libs.gson)
}