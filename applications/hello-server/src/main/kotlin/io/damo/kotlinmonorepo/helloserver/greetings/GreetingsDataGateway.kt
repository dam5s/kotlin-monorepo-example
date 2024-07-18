package io.damo.kotlinmonorepo.helloserver.greetings

import io.damo.kotlinmonorepo.greetingsservice.protocol.GreetingsServiceGrpcKt
import io.damo.kotlinmonorepo.greetingsservice.protocol.TryFindRequest
import io.grpc.Channel

interface GreetingsDataGateway {
    suspend fun tryFind(): String
}

class GrpcGreetingsDataGateway(private val greetingsChannel: Channel): GreetingsDataGateway {

    override suspend fun tryFind(): String {
        val stub = GreetingsServiceGrpcKt.GreetingsServiceCoroutineStub(greetingsChannel)
        val response = stub.tryFindGreeting(TryFindRequest.getDefaultInstance())

        if (response.hasGreeting()) {
            return response.greeting.message
        }

        return "Hello"
    }
}
