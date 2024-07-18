package io.damo.kotlinmonorepo.servertestsupport

import io.damo.kotlinmonorepo.grpcserversupport.ApplicationServer
import io.damo.kotlinmonorepo.grpcserversupport.createChannel
import io.grpc.ManagedChannel
import io.grpc.Server
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class GrpcServerTest {

    private lateinit var server: Server
    protected lateinit var channel: ManagedChannel

    abstract fun server(): ApplicationServer

    @BeforeTest
    fun serverSetup() = runBlocking {
        server = server().grpcServer
        server.start()
        channel = createChannel("localhost", server.port)
    }

    @AfterTest
    fun serverTeardown() {
        channel.shutdown()
        server.shutdown()
        server.awaitTermination()
    }
}
