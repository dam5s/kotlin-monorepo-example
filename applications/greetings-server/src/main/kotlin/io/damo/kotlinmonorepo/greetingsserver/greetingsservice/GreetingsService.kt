package io.damo.kotlinmonorepo.greetingsserver.greetingsservice

import io.damo.kotlinmonorepo.greetingsservice.protocol.Greeting
import io.damo.kotlinmonorepo.greetingsservice.protocol.GreetingResponse
import io.damo.kotlinmonorepo.greetingsservice.protocol.GreetingsServiceGrpcKt
import io.damo.kotlinmonorepo.greetingsservice.protocol.TryFindRequest
import javax.sql.DataSource

class GreetingsService(private val greetings: GreetingsDataGateway) : GreetingsServiceGrpcKt.GreetingsServiceCoroutineImplBase() {

    constructor(dataSource: DataSource) : this(DbGreetingsDataGateway(dataSource))

    override suspend fun tryFindGreeting(request: TryFindRequest): GreetingResponse {
        val maybeGreeting = greetings.tryFind()
        val responseBuilder = GreetingResponse.newBuilder()

        if (maybeGreeting != null) {
            val greeting = Greeting.newBuilder()
                .setMessage(maybeGreeting.text)
                .build()

            responseBuilder.setGreeting(greeting)
        }

        return responseBuilder.build()
    }
}
