package io.damo.kotlinmonorepo.grpcserversupport

import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.health.v1.HealthCheckResponse.ServingStatus
import io.grpc.protobuf.services.HealthStatusManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.time.delay
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.coroutines.CoroutineContext

data class ServerPorts(
    val server: Int,
    val health: Int,
) {
    companion object {
        val forTests = ServerPorts(0, 0)
    }
}

class ApplicationServer private constructor(
    val grpcServer: Server,
    val healthServer: HealthServer,
    private val healthServerPort: Int,
    private val health: HealthStatusManager,
    private val healthCheckDelay: Duration,
    private val healthCheckFrequency: Duration,
): CoroutineScope {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val healthChecks = mutableListOf<HealthCheck>()
    private var shuttingDown = false

    companion object {
        fun build(
            ports: ServerPorts,
            services: List<BindableService>,
            healthCheckDelay: Duration = Duration.ofSeconds(5),
            healthCheckFrequency: Duration = Duration.ofSeconds(30),
        ): ApplicationServer {
            val serverBuilder = ServerBuilder.forPort(ports.server)
            val health = HealthStatusManager()

            services.forEach { serverBuilder.addService(it) }
            serverBuilder.addService(health.healthService)

            return ApplicationServer(
                grpcServer = serverBuilder.build(),
                healthServer = HealthServer(),
                healthServerPort = ports.health,
                health,
                healthCheckDelay,
                healthCheckFrequency,
            )
        }
    }

    override val coroutineContext: CoroutineContext = Job()

    fun addHealthCheck(function: () -> Boolean) {
        addHealthCheck(FunctionHealthCheck(function))
    }

    fun addHealthCheck(healthCheck: HealthCheck) {
        healthChecks.add(healthCheck)
    }

    fun start() {
        grpcServer.start()
        healthServer.start(
            healthServerPort = healthServerPort,
            grpcServicePort = grpcServer.port,
        )
        startHealthChecking()
        logger.info("Server started on port ${grpcServer.port}.")
    }

    fun shutdown() {
        shuttingDown = true
        health.enterTerminalState()

        grpcServer.shutdown()
        grpcServer.awaitTermination()

        healthServer.shutdown()
    }

    fun startAndAwait() {
        Runtime.getRuntime().addShutdownHook(Thread {
            shutdown()
        })

        start()
        grpcServer.awaitTermination()
    }

    private fun healthCheckFlow(): Flow<Unit> =
        flow {
            delay(healthCheckDelay)
            while (!shuttingDown) {
                val healthy = healthChecks.all { it.run() }
                val status = if (healthy) ServingStatus.SERVING else ServingStatus.NOT_SERVING

                health.setStatus("", status)

                delay(healthCheckFrequency)
            }
        }

    private fun startHealthChecking() {
        health.setStatus("", ServingStatus.SERVING)
        healthCheckFlow().launchIn(this)
    }
}
