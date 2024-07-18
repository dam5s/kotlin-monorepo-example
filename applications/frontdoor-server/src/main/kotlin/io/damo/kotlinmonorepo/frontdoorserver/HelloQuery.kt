package io.damo.kotlinmonorepo.frontdoorserver

import io.damo.kotlinmonorepo.helloservice.protocol.v2.HelloCustomerRequest
import io.damo.kotlinmonorepo.helloservice.protocol.v2.HelloRequest
import io.damo.kotlinmonorepo.helloservice.protocol.v2.HelloServiceGrpcKt
import com.expediagroup.graphql.server.operations.Query
import io.grpc.Channel

data class SayHelloResponse(
    val message: String,
)

class HelloQuery(private val helloChannel: Channel) : Query {

    suspend fun sayHello(
        name: String? = null,
        customerId: String? = null,
        customerEmail: String? = null,
    ): SayHelloResponse {
        val stub = HelloServiceGrpcKt.HelloServiceCoroutineStub(helloChannel)

        val response = when {
            name != null -> {
                val request = HelloRequest.newBuilder()
                    .apply { setName(name) }
                    .build()
                stub.hello(request)
            }
            customerId != null -> {
                val request = HelloCustomerRequest.newBuilder()
                    .apply { setCustomerId(customerId) }
                    .build()
                stub.helloCustomer(request)
            }
            customerEmail != null -> {
                val request = HelloCustomerRequest.newBuilder()
                    .apply { setCustomerEmail(customerEmail) }
                    .build()
                stub.helloCustomer(request)
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
