package testing.kotlinmonorepo.helloserver

import io.damo.kotlinmonorepo.greetingsserver.greetingsservice.GreetingRecord
import io.damo.kotlinmonorepo.grpcserversupport.ApplicationServer
import io.damo.kotlinmonorepo.grpcserversupport.ServerPorts
import io.damo.kotlinmonorepo.helloserver.server
import io.damo.kotlinmonorepo.helloservice.protocol.HelloRequest
import io.damo.kotlinmonorepo.helloservice.protocol.HelloServiceGrpcKt
import io.damo.kotlinmonorepo.servertestsupport.GrpcServerTest
import io.grpc.Status
import io.grpc.StatusException
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HelloServiceTest : GrpcServerTest() {

    private val testGreetingsServer = TestGreetingsServer()

    override fun server(): ApplicationServer {
        testGreetingsServer.start()

        return server(
            ports = ServerPorts.forTests,
            greetingsOptions = testGreetingsServer.connectionOptions(),
        )
    }

    private val stub by lazy { HelloServiceGrpcKt.HelloServiceCoroutineStub(channel) }

    @AfterTest
    fun teardown() {
        testGreetingsServer.shutdown()
    }

    @Test
    fun `test hello, without greeting available`() = runBlocking {
        coEvery { testGreetingsServer.greetings.tryFind() } returns null

        val response = stub.hello(HelloRequest.newBuilder().setName("Damien").build())

        assertEquals("Hello Damien", response.message)
    }

    @Test
    fun `test hello, with greeting in database`() = runBlocking {
        coEvery { testGreetingsServer.greetings.tryFind() } returns GreetingRecord(id = "1", text = "Bonjour")

        val response = stub.hello(HelloRequest.newBuilder().setName("Damien").build())

        assertEquals("Bonjour Damien", response.message)
    }

    @Test
    fun `test hello, when name is too short`() = runBlocking {
        try {
            stub.hello(HelloRequest.newBuilder().setName("Yo").build())
            fail("Expected an error")
        } catch (e: StatusException) {
            assertEquals(Status.INVALID_ARGUMENT.code, e.status.code)
        }
    }
}
