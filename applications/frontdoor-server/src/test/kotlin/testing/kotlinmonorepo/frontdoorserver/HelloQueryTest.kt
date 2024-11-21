package testing.kotlinmonorepo.frontdoorserver

import io.damo.kotlinmonorepo.frontdoorserver.applicationServer
import io.damo.kotlinmonorepo.graphqltestsupport.GraphqlResponse
import io.damo.kotlinmonorepo.graphqltestsupport.graphqlQuery
import io.ktor.server.engine.*
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

    private val sayHelloQuery = "query (\$name: String) { sayHello(name: \$name) { message } }"

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
}
