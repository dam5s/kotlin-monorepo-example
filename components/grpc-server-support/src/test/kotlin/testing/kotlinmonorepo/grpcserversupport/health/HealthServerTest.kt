package testing.kotlinmonorepo.grpcserversupport.health

import io.damo.kotlinmonorepo.grpcserversupport.ApplicationServer
import io.damo.kotlinmonorepo.grpcserversupport.ServerPorts
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Duration
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HealthServerTest {

    private lateinit var server: ApplicationServer
    private val client = OkHttpClient()

    @BeforeTest
    fun setup() {
        server = ApplicationServer.build(
            ports = ServerPorts.forTests,
            services = emptyList(),
            healthCheckDelay = Duration.ZERO,
            healthCheckFrequency = Duration.ofMillis(100)
        )
        server.start()
    }

    @AfterTest
    fun teardown() {
        server.shutdown()
    }

    @Test
    fun `test health check, when default is healthy`() = runBlocking {
        val request = Request.Builder()
            .url(server.healthServer.url("/health"))
            .build()

        client.newCall(request).execute().use { response ->
            assertEquals(200, response.code)
        }
    }

    @Test
    fun `test health check, being unhealthy then healthy`() = runBlocking {
        var healthy = false

        server.addHealthCheck { healthy }

        waitFor("health endpoint to return 503") {
            val request = Request.Builder()
                .url(server.healthServer.url("/health"))
                .build()

            client.newCall(request).execute().use { response ->
                response.code == 503
            }
        }

        healthy = true

        waitFor("health endpoint to return 200") {
            val request = Request.Builder()
                .url(server.healthServer.url("/health"))
                .build()

            client.newCall(request).execute().use { response ->
                response.code == 200
            }
        }
    }
}

suspend fun waitFor(description: String = "predicate", predicate: suspend () -> Boolean) {
    var attempts = 0
    val maxAttempts = 10

    while (attempts <= maxAttempts) {
        if (predicate()) {
            return
        }
        attempts ++
        delay(50)
    }

    fail("Timed out waiting for $description")
}
