import org.flywaydb.core.api.configuration.FluentConfiguration

private fun Task.migrate(url: String, user: String, password: String) {
    doFirst {
        FluentConfiguration()
            .dataSource(url, user, password)
            .locations("filesystem:${project.projectDir}/migrations")
            .load()
            .migrate()
    }
}

tasks {
    create("migrate") {
        migrate("jdbc:postgresql://localhost/greetings_dev", "monorepodev", "monorepodev")
        migrate("jdbc:postgresql://localhost/greetings_test", "monorepodev", "monorepodev")
    }

    create<Exec>("container") {
        commandLine("docker", "build", "--platform=linux/amd64", "-q", "-t", "io.damo.kotlinmonorepo.greetings-db", "-f", "deployment/Dockerfile", ".")
    }

    create("build") {
        dependsOn("container")
    }
}
