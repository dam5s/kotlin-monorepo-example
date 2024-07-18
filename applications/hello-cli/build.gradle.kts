plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    dependencies {
        implementation(project(":protocols:hello-service"))
        implementation(project(":components:database-support"))
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.bundles.grpc)

        runtimeOnly(libs.postgresql)
    }
}

tasks {
    create<JavaExec>("run") {
        description = "Run the app"
        classpath = files(jar)
        environment["HELLO_SERVER_HOST"] = "localhost"
        environment["HELLO_SERVER_PORT"] = "8082"
        environment["GREETINGS_DB_URL"] = "jdbc:postgresql://localhost/greetings_dev"
        environment["GREETINGS_DB_USERNAME"] = "monorepodev"
        environment["GREETINGS_DB_PASSWORD"] = "monorepodev"
    }

    jar {
        manifest {
            attributes("Main-Class" to "io.damo.kotlinmonorepo.hellocli.AppKt")
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

        commandLine("docker", "build", "--platform=linux/amd64", "-q", "-t", "io.damo.kotlinmonorepo.hello-cli", "-f", "deployment/Dockerfile", ".")
    }
}
