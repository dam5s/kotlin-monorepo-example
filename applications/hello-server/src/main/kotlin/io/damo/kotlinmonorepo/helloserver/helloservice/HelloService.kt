package io.damo.kotlinmonorepo.helloserver.helloservice

import io.damo.kotlinmonorepo.helloserver.greetings.GreetingNameValidation
import io.damo.kotlinmonorepo.helloserver.greetings.GreetingsDataGateway
import io.damo.kotlinmonorepo.helloservice.protocol.HelloReply
import io.damo.kotlinmonorepo.helloservice.protocol.HelloRequest
import io.damo.kotlinmonorepo.helloservice.protocol.HelloServiceGrpcKt
import io.damo.kotlinmonorepo.serversupport.validateOrThrow

class HelloService(private val greetings: GreetingsDataGateway) : HelloServiceGrpcKt.HelloServiceCoroutineImplBase() {

    override suspend fun hello(request: HelloRequest): HelloReply {
        val name = request.name

        validateOrThrow(GreetingNameValidation.validate(name))

        val greeting = greetings.tryFind()

        return HelloReply.newBuilder()
            .setMessage("$greeting $name")
            .build()
    }
}
