package io.damo.kotlinmonorepo.hellocli

import io.damo.kotlinmonorepo.databasesupport.DatabaseConnectionOptions
import io.damo.kotlinmonorepo.databasesupport.DatabaseGateway
import io.damo.kotlinmonorepo.databasesupport.createDataSource
import io.damo.kotlinmonorepo.helloservice.protocol.HelloRequest
import io.damo.kotlinmonorepo.helloservice.protocol.HelloServiceGrpc
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.runBlocking

private fun requiredEnvironmentVariable(name: String): String =
    System.getenv()[name] ?: throw RuntimeException("missing configuration: $name")

private fun requiredIntEnvironmentVariable(name: String): Int =
    try {
        requiredEnvironmentVariable(name).toInt()
    } catch (e: NumberFormatException) {
        throw RuntimeException("invalid value Int value in $name environment variable")
    }

private fun buildHelloServerStub(host: String, port: Int): HelloServiceGrpc.HelloServiceBlockingStub =
    ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build()
        .let { HelloServiceGrpc.newBlockingStub(it) }

private fun buildDatabaseGateway(options: DatabaseConnectionOptions): DatabaseGateway =
    DatabaseGateway(createDataSource(options))

fun main() = runBlocking {
    val helloServerStub = buildHelloServerStub(
        host = requiredEnvironmentVariable("HELLO_SERVER_HOST"),
        port = requiredIntEnvironmentVariable("HELLO_SERVER_PORT"),
    )

    val dbOptions = DatabaseConnectionOptions(
        url = requiredEnvironmentVariable("GREETINGS_DB_URL"),
        username = requiredEnvironmentVariable("GREETINGS_DB_USERNAME"),
        password = requiredEnvironmentVariable("GREETINGS_DB_PASSWORD"),
    )

    val db = buildDatabaseGateway(dbOptions)

    println("Truncating greetings")
    db.execute("truncate greetings")

    println("Querying hello server with name Johnny")
    val johnnyResponse = helloServerStub.hello(HelloRequest.newBuilder().setName("Johnny").build())
    println(johnnyResponse.message)

    println("Inserting greeting")
    db.execute("insert into greetings (text) values ('Bonjour')")

    println("Querying hello server with name Damien")
    val damienResponse = helloServerStub.hello(HelloRequest.newBuilder().setName("Damien").build())
    println(damienResponse.message)
}
