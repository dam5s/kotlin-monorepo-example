plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.hikari)
    implementation(libs.kotlinx.coroutines.core)
}
