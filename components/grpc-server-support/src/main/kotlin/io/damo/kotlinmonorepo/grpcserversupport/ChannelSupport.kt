package io.damo.kotlinmonorepo.grpcserversupport

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

data class ChannelConnectionOptions(val host: String, val port: Int)

fun createChannel(options: ChannelConnectionOptions): ManagedChannel =
    createChannel(options.host, options.port)

fun createChannel(host: String, port: Int): ManagedChannel =
    ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build()
