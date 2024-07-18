plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":components:server-support"))

    implementation(libs.bundles.ktor)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.core)
}
