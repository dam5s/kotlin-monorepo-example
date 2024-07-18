package testing.kotlinmonorepo.frontdoorserver

import io.damo.kotlinmonorepo.frontdoorserver.applicationServer
import io.damo.kotlinmonorepo.graphqltestsupport.GraphqlResponse
import io.damo.kotlinmonorepo.graphqltestsupport.graphqlQuery
import io.damo.kotlinmonorepo.helloserver.customersapi.buildTestEcommerceCustomer
import io.ktor.server.engine.ApplicationEngine
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HelloQueryTest {

    private val testHelloServer = TestHelloServer()
    private lateinit var server: ApplicationEngine

    @BeforeTest
    fun setup() {
        testHelloServer.start()
        server = applicationServer(port = 0, helloOptions = testHelloServer.connectionOptions())
        server.start(wait = false)
    }

    @AfterTest
    fun teardown() {
        server.stop(gracePeriodMillis = 10, timeoutMillis = 10)
        testHelloServer.shutdown()
    }

    private val sayHelloQuery = "query " +
        "(\$name: String, \$customerId: String, \$customerEmail: String) { " +
        "sayHello(name: \$name, customerId: \$customerId, customerEmail: \$customerEmail) { message }" +
        " }"

    @Test
    fun `test sayHello without argument`() = runBlocking {
        coEvery { testHelloServer.greetings.tryFind() } returns "Oh hai"

        val response = server.graphqlQuery(sayHelloQuery)

        val expectedResponse = GraphqlResponse(
            data = mapOf("sayHello" to mapOf("message" to "Oh hai World"))
        )
        assertEquals(expectedResponse, response)
    }

    @Test
    fun `test sayHello with name`() = runBlocking {
        coEvery { testHelloServer.greetings.tryFind() } returns "Bonjour"

        val response = server.graphqlQuery(sayHelloQuery, variables = mapOf("name" to "Damien"))

        val expectedResponse = GraphqlResponse(
            data = mapOf("sayHello" to mapOf("message" to "Bonjour Damien"))
        )
        assertEquals(expectedResponse, response)
    }

    @Test
    fun `test sayHello with customerId`() = runBlocking {
        val fred = buildTestEcommerceCustomer(firstName = "Fred")

        coEvery { testHelloServer.greetings.tryFind() } returns "Ola"
        coEvery { testHelloServer.customers.tryFindCustomer(fred.userId) } returns fred

        val response = server.graphqlQuery(sayHelloQuery, variables = mapOf("customerId" to fred.userId))

        val expectedResponse = GraphqlResponse(
            data = mapOf("sayHello" to mapOf("message" to "Ola Fred"))
        )
        assertEquals(expectedResponse, response)
    }

    @Test
    fun `test sayHello with customerEmail`() = runBlocking {
        val freddy = buildTestEcommerceCustomer(firstName = "Freddy")

        coEvery { testHelloServer.greetings.tryFind() } returns "Hallo"
        coEvery { testHelloServer.customers.tryFindCustomerByEmail(freddy.email) } returns freddy

        val response = server.graphqlQuery(sayHelloQuery, variables = mapOf("customerEmail" to freddy.email))

        val expectedResponse = GraphqlResponse(
            data = mapOf("sayHello" to mapOf("message" to "Hallo Freddy"))
        )
        assertEquals(expectedResponse, response)
    }
}
