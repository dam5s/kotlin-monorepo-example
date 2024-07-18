plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":components:validation-support"))
    implementation(libs.bundles.grpc)
    implementation(libs.bundles.ktor)
    implementation(libs.slf4j.api)

    api(libs.kotlinx.coroutines.core)

    testImplementation(kotlin("test-junit"))
    testImplementation(libs.okhttp)
}
