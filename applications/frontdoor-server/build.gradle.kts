plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.graphql)
}

kotlin {
    dependencies {
        implementation(libs.bundles.ktor)
        implementation(libs.bundles.grpc)
        implementation(libs.graphql.ktor.server)
        implementation(project(":components:grpc-server-support"))
        implementation(project(":components:server-support"))
        implementation(project(":protocols:hello-service"))

        testImplementation(kotlin("test-junit"))
        testImplementation(project(":components:graphql-test-support"))
        testImplementation(project(":applications:hello-server"))
        testImplementation(libs.mockk)
    }
}

graphql {
    schema {
        packages = listOf("io.damo.kotlinmonorepo.frontdoorserver")
    }
}

tasks {
    create<JavaExec>("run") {
        description = "Run the server"
        classpath = files(jar)
        environment["ENABLE_DEBUG_LOGS"] = "true"
        environment["HELLO_HOST"] = "localhost"
        environment["HELLO_PORT"] = "8082"
    }

    jar {
        manifest {
            attributes("Main-Class" to "io.damo.kotlinmonorepo.frontdoorserver.ServerKt")
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

        commandLine("docker", "build", "--platform=linux/amd64", "-q", "-t", "io.damo.kotlinmonorepo.frontdoor-server", "-f", "deployment/Dockerfile", ".")
    }
}
