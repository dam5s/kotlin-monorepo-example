package testing.kotlinmonorepo.greetingsserver

import io.damo.kotlinmonorepo.databasesupport.DatabaseConnectionOptions
import io.damo.kotlinmonorepo.databasesupport.DatabaseGateway
import io.damo.kotlinmonorepo.databasesupport.createDataSource
import io.damo.kotlinmonorepo.greetingsserver.server
import io.damo.kotlinmonorepo.greetingsservice.protocol.GreetingsServiceGrpc
import io.damo.kotlinmonorepo.greetingsservice.protocol.TryFindRequest
import io.damo.kotlinmonorepo.grpcserversupport.ServerPorts
import io.damo.kotlinmonorepo.servertestsupport.GrpcServerTest
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GreetingsServiceTest : GrpcServerTest() {

    private val databaseOptions =
        DatabaseConnectionOptions(
            url = "jdbc:postgresql://localhost/greetings_test",
            username = "monorepodev",
            password = "monorepodev",
        )

    override fun server() =
        server(ports = ServerPorts.forTests, databaseOptions)

    private val db by lazy { DatabaseGateway(createDataSource(databaseOptions)) }
    private val stub by lazy { GreetingsServiceGrpc.newBlockingStub(channel) }

    @BeforeTest
    fun setup() = runBlocking {
        db.execute("truncate greetings")
    }

    @Test
    fun `test tryFindGreeting, without greeting in database`() = runBlocking {
        val response = stub.tryFindGreeting(TryFindRequest.getDefaultInstance())

        assertFalse(response.hasGreeting())
    }

    @Test
    fun `test tryFindGreeting, with a greeting in the database`() = runBlocking {
        db.execute("insert into greetings (text) values ('Bonjour')")

        val response = stub.tryFindGreeting(TryFindRequest.getDefaultInstance())

        assertTrue(response.hasGreeting())
        assertEquals("Bonjour", response.greeting.message)
    }
}
