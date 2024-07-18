package io.damo.kotlinmonorepo.helloserver

import io.damo.kotlinmonorepo.grpcserversupport.ApplicationServer
import io.damo.kotlinmonorepo.grpcserversupport.ChannelConnectionOptions
import io.damo.kotlinmonorepo.grpcserversupport.ServerPorts
import io.damo.kotlinmonorepo.grpcserversupport.createChannel
import io.damo.kotlinmonorepo.helloserver.customersapi.CommerceToolsCustomersApiGateway
import io.damo.kotlinmonorepo.helloserver.customersapi.CommerceToolsOptions
import io.damo.kotlinmonorepo.helloserver.greetings.GrpcGreetingsDataGateway
import io.damo.kotlinmonorepo.helloserver.helloservice.HelloService
import io.damo.kotlinmonorepo.helloserver.helloservice.HelloServiceV2
import io.damo.kotlinmonorepo.serversupport.LoggingConfig
import io.damo.kotlinmonorepo.serversupport.requiredEnvironmentVariable
import io.damo.kotlinmonorepo.serversupport.requiredIntEnvironmentVariable
import io.damo.kotlinmonorepo.serversupport.resolvePort

fun server(
    ports: ServerPorts,
    greetingsOptions: ChannelConnectionOptions,
    commerceToolsOptions: CommerceToolsOptions,
): ApplicationServer {
    val customersApi = CommerceToolsCustomersApiGateway(commerceToolsOptions)
    val greetingsChannel = createChannel(greetingsOptions)
    val greetingsDataGateway = GrpcGreetingsDataGateway(greetingsChannel)

    return ApplicationServer.build(
        ports = ports,
        services = listOf(
            HelloService(greetingsDataGateway),
            HelloServiceV2(greetingsDataGateway, customersApi),
        )
    )
}

fun main() {
    LoggingConfig.initialize()

    val server = server(
        ports = ServerPorts(
            server = resolvePort(name = "SERVER_PORT", default = 8082),
            health = resolvePort(name = "HEALTH_SERVER_PORT", default = 8092),
        ),
        greetingsOptions = ChannelConnectionOptions(
            host = requiredEnvironmentVariable("GREETINGS_HOST"),
            port = requiredIntEnvironmentVariable("GREETINGS_PORT"),
        ),
        commerceToolsOptions = CommerceToolsOptions(
            authUrl = requiredEnvironmentVariable("COMMERCE_TOOLS_AUTH_URL"),
            apiUrl = requiredEnvironmentVariable("COMMERCE_TOOLS_API_URL"),
            projectKey = requiredEnvironmentVariable("COMMERCE_TOOLS_PROJECT_KEY"),
            clientId = requiredEnvironmentVariable("COMMERCE_TOOLS_CLIENT_ID"),
            clientSecret = requiredEnvironmentVariable("COMMERCE_TOOLS_CLIENT_SECRET"),
            scopes = requiredEnvironmentVariable("COMMERCE_TOOLS_SCOPES"),
        )
    )
    server.startAndAwait()
}
