package io.damo.kotlinmonorepo.graphqltestsupport

import io.damo.kotlinmonorepo.serversupport.serverUrl
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.server.engine.ApplicationEngine
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

typealias JsonObject = Map<String, Any>

data class GraphqlResponse(
    val data: JsonObject? = null,
    val errors: List<JsonObject>? = null,
)

suspend fun ApplicationEngine.graphqlQuery(query: String, variables: JsonObject = emptyMap()): GraphqlResponse {
    val queryJson = objectMapper.writeValueAsString(
        mapOf(
            "query" to query,
            "variables" to variables,
        )
    )

    val request = Request.Builder()
        .url(serverUrl(path = "/graphql"))
        .post(queryJson.toRequestBody("application/json".toMediaTypeOrNull()))
        .build()

    client.newCall(request).execute().use { response ->
        if (response.code != 200) {
            throw IllegalStateException("Expected response to have status code 200, got ${response.code}")
        }

        return response.readGraphqlResponse()
    }
}

private fun Response.readGraphqlResponse(): GraphqlResponse =
    body
        ?.use { objectMapper.readValue<GraphqlResponse>(it.byteStream()) }
        ?: throw IllegalStateException("Could not read response body")

private val client = OkHttpClient()
private val objectMapper = jacksonObjectMapper()
