plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.graphql) apply false
}

buildscript {
    dependencies {
        classpath(libs.postgresql)
        classpath(libs.flyway.core)
        classpath(libs.flyway.postgresql)
    }
}
