plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":components:validation-support"))
    implementation(libs.bundles.grpc)
    implementation(libs.bundles.ktor)
    implementation(libs.logback)

    api(libs.slf4j.api)

    testImplementation(kotlin("test-junit"))
    testImplementation(libs.okhttp)
    testImplementation(libs.kotlinx.coroutines.core)
}
