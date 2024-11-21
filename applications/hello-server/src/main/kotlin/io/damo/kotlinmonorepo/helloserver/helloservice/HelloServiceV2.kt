package io.damo.kotlinmonorepo.helloserver.helloservice

import io.damo.kotlinmonorepo.helloserver.greetings.GreetingNameValidation
import io.damo.kotlinmonorepo.helloserver.greetings.GreetingsDataGateway
import io.damo.kotlinmonorepo.helloservice.protocol.v2.HelloReply
import io.damo.kotlinmonorepo.helloservice.protocol.v2.HelloRequest
import io.damo.kotlinmonorepo.helloservice.protocol.v2.HelloServiceGrpcKt
import io.damo.kotlinmonorepo.serversupport.validateOrThrow

class HelloServiceV2(private val greetings: GreetingsDataGateway) : HelloServiceGrpcKt.HelloServiceCoroutineImplBase() {

    override suspend fun hello(request: HelloRequest): HelloReply {
        val name = if (request.hasName()) request.name else "World"

        validateOrThrow(GreetingNameValidation.validate(name))

        val greeting = greetings.tryFind()

        return HelloReply.newBuilder()
            .setGreeting("$greeting $name")
            .build()
    }
}
