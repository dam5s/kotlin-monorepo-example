plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    dependencies {
        implementation(project(":components:database-support"))
        implementation(project(":components:server-support"))
        implementation(project(":components:grpc-server-support"))
        implementation(project(":protocols:greetings-service"))

        runtimeOnly(libs.postgresql)
        implementation(libs.bundles.grpc)

        testImplementation(kotlin("test-junit"))
        testImplementation(project(":components:server-test-support"))
    }
}

tasks {
    create<JavaExec>("run") {
        description = "Run the server"
        classpath = files(jar)
        environment["ENABLE_DEBUG_LOGS"] = "true"
        environment["DATABASE_URL"] = "jdbc:postgresql://localhost/greetings_dev"
        environment["DATABASE_USERNAME"] = "monorepodev"
        environment["DATABASE_PASSWORD"] = "monorepodev"
    }

    jar {
        manifest {
            attributes("Main-Class" to "io.damo.kotlinmonorepo.greetingsserver.ServerKt")
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

        commandLine("docker", "build", "--platform=linux/amd64", "-q", "-t", "io.damo.kotlinmonorepo.greetings-server", "-f", "deployment/Dockerfile", ".")
    }
}
