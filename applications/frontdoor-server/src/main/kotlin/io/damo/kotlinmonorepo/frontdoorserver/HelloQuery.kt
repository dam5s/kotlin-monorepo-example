package io.damo.kotlinmonorepo.frontdoorserver

import com.expediagroup.graphql.server.operations.Query
import io.damo.kotlinmonorepo.helloservice.protocol.v2.HelloRequest
import io.damo.kotlinmonorepo.helloservice.protocol.v2.HelloServiceGrpcKt
import io.grpc.Channel

data class SayHelloResponse(
    val message: String,
)

class HelloQuery(private val helloChannel: Channel) : Query {

    suspend fun sayHello(name: String? = null): SayHelloResponse {
        val stub = HelloServiceGrpcKt.HelloServiceCoroutineStub(helloChannel)

        val response = when {
            name != null -> {
                val request = HelloRequest.newBuilder()
                    .apply { setName(name) }
                    .build()
                stub.hello(request)
            }
            else -> {
                val request = HelloRequest.newBuilder()
                    .build()
                stub.hello(request)
            }
        }

        return SayHelloResponse(response.greeting)
    }
}
