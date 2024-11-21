package io.damo.kotlinmonorepo.frontdoorserver

import io.damo.kotlinmonorepo.grpcserversupport.ChannelConnectionOptions
import io.damo.kotlinmonorepo.grpcserversupport.createChannel
import io.damo.kotlinmonorepo.serversupport.LoggingConfig
import io.damo.kotlinmonorepo.serversupport.requiredEnvironmentVariable
import io.damo.kotlinmonorepo.serversupport.requiredIntEnvironmentVariable
import io.damo.kotlinmonorepo.serversupport.resolvePort
import com.expediagroup.graphql.server.ktor.GraphQL
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphQLSDLRoute
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.Routing

private typealias ApplicationModule = (Application.() -> Unit)

private fun injectAppModule(helloOptions: ChannelConnectionOptions): ApplicationModule {
    val helloChannel = createChannel(helloOptions)

    return {
        install(GraphQL) {
            schema {
                packages = listOf("io.damo.kotlinmonorepo.frontdoorserver")
                queries = listOf(
                    HelloQuery(helloChannel),
                )
            }
        }

        install(Routing) {
            graphQLPostRoute()
            graphQLSDLRoute()
        }
    }
}

fun applicationServer(port: Int, helloOptions: ChannelConnectionOptions): ApplicationEngine =
    embeddedServer(Netty, port = port, module = injectAppModule(helloOptions))

fun main() {
    LoggingConfig.initialize()

    applicationServer(
        port = resolvePort(name = "PORT", default = 8080),
        helloOptions = ChannelConnectionOptions(
            host = requiredEnvironmentVariable("HELLO_HOST"),
            port = requiredIntEnvironmentVariable("HELLO_PORT"),
        ),
    ).start(wait = true)
}
