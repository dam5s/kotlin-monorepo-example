package testing.kotlinmonorepo.helloserver

import io.damo.kotlinmonorepo.greetingsserver.greetingsservice.GreetingRecord
import io.damo.kotlinmonorepo.grpcserversupport.ApplicationServer
import io.damo.kotlinmonorepo.grpcserversupport.ServerPorts
import io.damo.kotlinmonorepo.helloserver.server
import io.damo.kotlinmonorepo.helloservice.protocol.v2.HelloCustomerRequest
import io.damo.kotlinmonorepo.helloservice.protocol.v2.HelloRequest
import io.damo.kotlinmonorepo.helloservice.protocol.v2.HelloServiceGrpcKt
import io.damo.kotlinmonorepo.servertestsupport.GrpcServerTest
import io.grpc.Status
import io.grpc.StatusException
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HelloServiceV2Test : GrpcServerTest() {

    private val testGreetingsServer = TestGreetingsServer()
    private val testCommerceToolsServer = TestCommerceToolsServer()

    override fun server(): ApplicationServer {
        testGreetingsServer.start()
        testCommerceToolsServer.start()

        return server(
            ports = ServerPorts.forTests,
            greetingsOptions = testGreetingsServer.connectionOptions(),
            commerceToolsOptions = testCommerceToolsServer.options(),
        )
    }

    private val stub by lazy { HelloServiceGrpcKt.HelloServiceCoroutineStub(channel) }

    @AfterTest
    fun teardown() {
        testCommerceToolsServer.shutdown()
        testGreetingsServer.shutdown()
    }

    @Test
    fun `test hello, without greeting available`() = runBlocking {
        val response = stub.hello(HelloRequest.newBuilder().setName("Damien").build())

        assertEquals("Hello Damien", response.greeting)
    }

    @Test
    fun `test hello, with greeting`() = runBlocking {
        coEvery { testGreetingsServer.greetings.tryFind() } returns GreetingRecord(id = "1", text = "Bonjour")

        val response = stub.hello(HelloRequest.newBuilder().setName("Damien").build())

        assertEquals("Bonjour Damien", response.greeting)
    }

    @Test
    fun `test hello, without name`() = runBlocking {
        val response = stub.hello(HelloRequest.newBuilder().build())

        assertEquals("Hello World", response.greeting)
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

    @Test
    fun `test helloCustomer, with customerId`() = runBlocking {
        testCommerceToolsServer.stubCustomer(firstName = "Fred", externalId = "urn:user:100")

        val request = HelloCustomerRequest.newBuilder()
            .setCustomerId("urn:user:100")
            .build()

        val response = stub.helloCustomer(request)

        assertEquals("Hello Fred", response.greeting)
    }

    @Test
    fun `test helloCustomer, with customerId, customer not found`() = runBlocking {
        val request = HelloCustomerRequest.newBuilder()
            .setCustomerId("urn:user:100")
            .build()

        val response = stub.helloCustomer(request)

        assertEquals("Hello Unknown customer", response.greeting)
    }

    @Test
    fun `test helloCustomer, with customerEmail`() = runBlocking {
        testCommerceToolsServer.stubCustomer(firstName = "Freddy", email = "fred@example.com")

        val request = HelloCustomerRequest.newBuilder()
            .setCustomerEmail("fred@example.com")
            .build()

        val response = stub.helloCustomer(request)

        assertEquals("Hello Freddy", response.greeting)
    }

    @Test
    fun `test helloCustomer, with customerEmail, customer not found`() = runBlocking {
        val request = HelloCustomerRequest.newBuilder()
            .setCustomerEmail("fred@example.com")
            .build()

        val response = stub.helloCustomer(request)

        assertEquals("Hello Unknown customer", response.greeting)
    }

    @Test
    fun `test helloCustomer, with neither`() = runBlocking {
        val request = HelloCustomerRequest.newBuilder()
            .build()

        val response = stub.helloCustomer(request)

        assertEquals("Hello Unknown customer", response.greeting)
    }
}
