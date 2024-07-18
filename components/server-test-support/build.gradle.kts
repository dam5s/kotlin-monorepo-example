plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    dependencies {
        implementation(kotlin("test-junit"))
        implementation(project(":components:database-support"))
        implementation(project(":components:grpc-server-support"))
        implementation(libs.grpc.api)
        implementation(libs.kotlinx.coroutines.core)
    }
}
