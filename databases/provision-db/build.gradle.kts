tasks {
    create<Exec>("container") {
        workingDir("$projectDir/deployment")
        commandLine("docker", "build", "--platform=linux/amd64", "-q", "-t", "io.damo.kotlinmonorepo.provision-db", ".")
    }

    create("build") {
        dependsOn("container")
    }
}
