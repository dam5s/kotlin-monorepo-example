package io.damo.kotlinmonorepo.grpcserversupport

import io.grpc.ManagedChannelBuilder
import io.grpc.health.v1.HealthCheckRequest
import io.grpc.health.v1.HealthCheckResponse.ServingStatus
import io.grpc.health.v1.HealthGrpc
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

class HealthServer {

    private var server: NettyApplicationEngine? = null

    fun start(healthServerPort: Int, grpcServicePort: Int) {
        val grpcChannel = ManagedChannelBuilder.forAddress("localhost", grpcServicePort)
            .usePlaintext()
            .build()

        server = embeddedServer(Netty, port = healthServerPort) {
            routing {
                get("/health") {
                    val healthStub = HealthGrpc.newBlockingStub(grpcChannel)
                    val healthResponse = healthStub.check(HealthCheckRequest.getDefaultInstance())
                    val isHealthy = healthResponse.status == ServingStatus.SERVING

                    if (isHealthy) {
                        call.respondText(text = "Serving", status = HttpStatusCode.OK)
                    } else {
                        call.respondText(text = "Not serving", status = HttpStatusCode.ServiceUnavailable)
                    }
                }
            }
        }.start(wait = false)
    }

    suspend fun url(path: String = ""): String {
        val connector = server?.resolvedConnectors()?.firstOrNull()
        val port = connector?.port
            ?: throw IllegalStateException("Could not read server port")

        return "http://localhost:${port}$path"
    }

    fun shutdown() {
        server?.stop(gracePeriodMillis = 10, timeoutMillis = 15)
    }
}
