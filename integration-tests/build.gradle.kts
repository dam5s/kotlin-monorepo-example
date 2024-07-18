plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    testImplementation(kotlin("test-junit"))
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(project(":applications:hello-server"))
    testImplementation(project(":components:server-support"))
}

tasks {
    test {
        environment.putIfAbsent("COMMERCE_TOOLS_AUTH_URL", prop("commerceTools.authUrl"))
        environment.putIfAbsent("COMMERCE_TOOLS_API_URL", prop("commerceTools.apiUrl"))
        environment.putIfAbsent("COMMERCE_TOOLS_PROJECT_KEY", prop("commerceTools.projectKey"))
        environment.putIfAbsent("COMMERCE_TOOLS_SCOPES", prop("commerceTools.scopes"))
        environment.putIfAbsent("COMMERCE_TOOLS_CLIENT_ID", prop("commerceTools.clientId"))
        environment.putIfAbsent("COMMERCE_TOOLS_CLIENT_SECRET", prop("commerceTools.clientSecret"))
    }
}

fun Project.prop(name: String) =
    findProperty(name) as? String ?: throw GradleException("Expected to find $name in gradle.properties file")
