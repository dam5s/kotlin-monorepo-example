plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    dependencies {
        implementation(project(":components:database-support"))
        implementation(project(":components:server-support"))
        implementation(project(":components:grpc-server-support"))
        implementation(project(":components:validation-support"))

        implementation(project(":protocols:hello-service"))
        implementation(project(":protocols:greetings-service"))

        implementation(libs.bundles.grpc)
        implementation(libs.bundles.commercetools)

        testImplementation(kotlin("test-junit"))
        testImplementation(libs.kotlinx.coroutines.core)
        testImplementation(project(":components:server-test-support"))
        testImplementation(project(":applications:greetings-server"))
        testImplementation(libs.okhttp.mockwebserver)
        testImplementation(libs.mockk)
    }
}

tasks {
    create<JavaExec>("run") {
        description = "Run the server"
        classpath = files(jar)
        environment["ENABLE_DEBUG_LOGS"] = "true"
        environment["GREETINGS_HOST"] = "localhost"
        environment["GREETINGS_PORT"] = "8081"

        environment["COMMERCE_TOOLS_AUTH_URL"] = prop("commerceTools.authUrl")
        environment["COMMERCE_TOOLS_API_URL"] = prop("commerceTools.apiUrl")
        environment["COMMERCE_TOOLS_PROJECT_KEY"] = prop("commerceTools.projectKey")
        environment["COMMERCE_TOOLS_SCOPES"] = prop("commerceTools.scopes")
        environment["COMMERCE_TOOLS_CLIENT_ID"] = prop("commerceTools.clientId")
        environment["COMMERCE_TOOLS_CLIENT_SECRET"] = prop("commerceTools.clientSecret")
    }

    jar {
        manifest {
            attributes("Main-Class" to "io.damo.kotlinmonorepo.helloserver.ServerKt")
        }

        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from({
            configurations.runtimeClasspath.get()
                .filter { it.name.endsWith("jar") }
                .map(::zipTree)
        })
    }

    create<Exec>("container") {
        mustRunAfter("assemble", "check")
        findByName("build")?.dependsOn(this)

        commandLine("docker", "build", "--platform=linux/amd64", "-q", "-t", "io.damo.kotlinmonorepo.hello-server", "-f", "deployment/Dockerfile", ".")
    }
}

fun Project.prop(name: String) =
    findProperty(name) as? String ?: throw GradleException("Expected to find $name in gradle.properties file")
