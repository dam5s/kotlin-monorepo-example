package testing.kotlinmonorepo.frontdoorserver

import io.damo.kotlinmonorepo.grpcserversupport.ApplicationServer
import io.damo.kotlinmonorepo.grpcserversupport.ChannelConnectionOptions
import io.damo.kotlinmonorepo.grpcserversupport.ServerPorts
import io.damo.kotlinmonorepo.helloserver.greetings.GreetingsDataGateway
import io.damo.kotlinmonorepo.helloserver.helloservice.HelloServiceV2
import io.mockk.mockk

class TestHelloServer {
    val greetings = mockk<GreetingsDataGateway>()

    private val service = HelloServiceV2(greetings)
    private val server = ApplicationServer.build(
        ports = ServerPorts.forTests,
        services = listOf(service),
    ).grpcServer

    fun start() {
        server.start()
    }

    fun connectionOptions() =
        ChannelConnectionOptions(host = "localhost", port = server.port)

    fun shutdown() {
        server.shutdown()
        server.awaitTermination()
    }
}
