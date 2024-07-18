package io.damo.kotlinmonorepo.greetingsserver

import io.damo.kotlinmonorepo.databasesupport.DatabaseConnectionOptions
import io.damo.kotlinmonorepo.databasesupport.createDataSource
import io.damo.kotlinmonorepo.greetingsserver.greetingsservice.GreetingsService
import io.damo.kotlinmonorepo.grpcserversupport.ApplicationServer
import io.damo.kotlinmonorepo.grpcserversupport.DatabaseHealthCheck
import io.damo.kotlinmonorepo.grpcserversupport.ServerPorts
import io.damo.kotlinmonorepo.serversupport.LoggingConfig
import io.damo.kotlinmonorepo.serversupport.optionalEnvironmentVariable
import io.damo.kotlinmonorepo.serversupport.requiredEnvironmentVariable
import io.damo.kotlinmonorepo.serversupport.resolvePort

fun server(ports: ServerPorts, databaseOptions: DatabaseConnectionOptions): ApplicationServer {
    val dataSource = createDataSource(databaseOptions)
    val greetingsService = GreetingsService(dataSource)

    val server = ApplicationServer.build(
        ports = ports,
        services = listOf(greetingsService),
    )
    server.addHealthCheck(DatabaseHealthCheck(dataSource))

    return server
}

fun main() {
    LoggingConfig.initialize()

    val server = server(
        ports = ServerPorts(
            server = resolvePort(name = "SERVER_PORT", default = 8081),
            health = resolvePort(name = "HEALTH_SERVER_PORT", default = 8091),
        ),
        databaseOptions = DatabaseConnectionOptions(
            url = requiredEnvironmentVariable("DATABASE_URL"),
            username = optionalEnvironmentVariable("DATABASE_USERNAME"),
            password = optionalEnvironmentVariable("DATABASE_PASSWORD"),
        )
    )
    server.startAndAwait()
}
