package testing.kotlinmonorepo.helloserver

import io.damo.kotlinmonorepo.greetingsserver.greetingsservice.GreetingsDataGateway
import io.damo.kotlinmonorepo.greetingsserver.greetingsservice.GreetingsService
import io.damo.kotlinmonorepo.grpcserversupport.ApplicationServer
import io.damo.kotlinmonorepo.grpcserversupport.ChannelConnectionOptions
import io.damo.kotlinmonorepo.grpcserversupport.ServerPorts
import io.mockk.coEvery
import io.mockk.mockk

class TestGreetingsServer {
    val greetings = mockk<GreetingsDataGateway>()

    private val service = GreetingsService(greetings)
    private val server = ApplicationServer.build(
        ports = ServerPorts.forTests,
        services = listOf(service),
    ).grpcServer

    fun start() {
        coEvery { greetings.tryFind() } returns null
        server.start()
    }

    fun connectionOptions() =
        ChannelConnectionOptions(host = "localhost", port = server.port)

    fun shutdown() {
        server.shutdown()
        server.awaitTermination()
    }
}
