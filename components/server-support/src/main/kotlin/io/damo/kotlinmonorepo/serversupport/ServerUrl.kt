package io.damo.kotlinmonorepo.serversupport

import io.ktor.server.engine.ApplicationEngine

suspend fun ApplicationEngine.serverUrl(path: String = ""): String {
    val connector = resolvedConnectors().firstOrNull()
        ?: throw IllegalStateException("Could not read server connector information")

    val scheme = connector.type.name.lowercase()
    val host = connector.host
    val port = connector.port

    return "$scheme://$host:$port$path"
}
