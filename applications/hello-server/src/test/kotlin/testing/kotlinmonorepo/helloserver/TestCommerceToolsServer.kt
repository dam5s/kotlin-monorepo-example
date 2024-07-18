package testing.kotlinmonorepo.helloserver

import io.damo.kotlinmonorepo.helloserver.customersapi.CommerceToolsOptions
import com.commercetools.api.models.customer.Customer
import io.vrap.rmf.base.client.utils.json.JsonUtils
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.net.URLEncoder

class TestCommerceToolsServer {

    private val dispatcher = TestCommerceToolsServiceDispatcher()
    private val server = MockWebServer()

    fun options() = CommerceToolsOptions(
        authUrl = server.url("/auth").toString(),
        apiUrl = server.url("/api").toString(),
        projectKey = "testing-project-key",
        clientId = "testing-id",
        clientSecret = "testing-secret",
        scopes = "testing-scopes",
    )

    fun start() {
        server.dispatcher = dispatcher
        server.start(port = 0)
    }

    fun shutdown() {
        server.shutdown()
    }

    fun stubCustomer(firstName: String, externalId: String = "", email: String = "") {
        dispatcher.stubbedCustomer = Customer.builder()
            .firstName(firstName)
            .externalId(externalId)
            .email(email)
            .buildUnchecked()
    }
}

private class TestCommerceToolsServiceDispatcher : Dispatcher() {

    var stubbedCustomer: Customer? = null

    override fun dispatch(request: RecordedRequest): MockResponse {
        println("Received request at url ${request.requestUrl}")

        val path = request.path ?: ""

        return when {
            path.startsWith("/api") -> dispatchApiCall(request)
            path.startsWith("/auth") -> dispatchAuthCall(request)
            else -> MockResponse().setResponseCode(503)
        }
    }

    private fun encode(value: String?): String =
        URLEncoder.encode("$value", Charsets.UTF_8)

    private fun whereStringEquals(name: String, value: String?): String =
        encode("$name=\"${value}\"")

    private fun dispatchApiCall(request: RecordedRequest): MockResponse {
        val externalIdWhere = whereStringEquals("externalId", stubbedCustomer?.externalId)
        val emailWhere = whereStringEquals("email", stubbedCustomer?.email)

        val customer = stubbedCustomer
        val path = request.path ?: ""

        return when {
            (customer != null && path == "/api/testing-project-key/customers?where=$externalIdWhere") ||
            (customer != null && path =="/api/testing-project-key/customers?where=$emailWhere") -> {
                val body = mapOf(
                    "results" to listOf(
                        mapOf(
                            "firstName" to customer.firstName,
                            "externalId" to customer.externalId,
                            "email" to customer.email,
                        )
                    ),
                    "limit" to 1,
                    "offset" to 0,
                    "count" to 1,
                ).toJson()

                MockResponse().setBody(body)
            }

            customer == null && path.startsWith("/api/testing-project-key/customers") -> {
                MockResponse().setResponseCode(404)
            }

            else -> {
                MockResponse().setResponseCode(503)
            }
        }
    }

    private fun dispatchAuthCall(request: RecordedRequest): MockResponse {
        if (request.method != "POST") {
            return MockResponse().setResponseCode(503)
        }

        return MockResponse()
            .setResponseCode(200)
            .setBody(
                mapOf(
                    "access_token" to "MTQ0NjJkZmQ5OTM2NDE1ZTZjNGZmZjI3",
                    "token_type" to "Bearer",
                    "expires_in" to 3600,
                    "refresh_token" to "IwOGYzYTlmM2YxOTQ5MGE3YmNmMDFkNTVk",
                    "scope" to "testing-scopes"
                ).toJson()
            )
    }
}

private fun Any.toJson() =
    JsonUtils.getConfiguredObjectMapper().writeValueAsString(this)
