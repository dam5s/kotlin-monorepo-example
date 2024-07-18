package io.damo.kotlinmonorepo.helloserver.customersapi

import com.commercetools.api.client.ByProjectKeyCustomersGet
import com.commercetools.api.client.ProjectApiRoot
import com.commercetools.api.defaultconfig.ApiRootBuilder
import io.vrap.rmf.base.client.DeserializationException
import io.vrap.rmf.base.client.RequestCommand
import io.vrap.rmf.base.client.oauth2.ClientCredentialsBuilder
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import java.io.IOException

data class CommerceToolsOptions(
    val authUrl: String,
    val apiUrl: String,
    val projectKey: String,
    val clientId: String,
    val clientSecret: String,
    val scopes: String,
) {
    companion object {
        val none = CommerceToolsOptions("", "", "", "", "", "")
    }
}

class CommerceToolsCustomersApiGateway(options: CommerceToolsOptions) : CustomersApiGateway {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val api = options.buildApiRoot()

    override suspend fun tryFindCustomer(userId: String): EcommerceCustomer? {
        val request = api
            .customers()
            .get()
            .withWhere("externalId=\"$userId\"")

        return tryFindCustomer(request)
    }

    override suspend fun tryFindCustomerByEmail(email: String): EcommerceCustomer? {
        val request = api
            .customers()
            .get()
            .withWhere("email=\"$email\"")

        return tryFindCustomer(request)
    }

    private suspend fun tryFindCustomer(request: ByProjectKeyCustomersGet): EcommerceCustomer? {
        val response = request.executeSafely()

        if (response?.count != 1L) {
            logger.info("Customer was not found")
            return null
        }

        val customer = response.results[0]

        return EcommerceCustomer(
            userId = customer.externalId,
            firstName = customer.firstName,
            email = customer.email,
        )
    }

    private suspend fun <T> RequestCommand<T>.executeSafely(): T? {
        try {
            val response = execute().await()
            if (response.body is T) {
                return response.body
            }
        } catch (e: DeserializationException) {
            logger.error("There was an error in CommerceTools SDK trying to deserialize JSON", e)
        } catch (e: IOException) {
            logger.error("There was a connection error in CommerceTools SDK trying to execute the request", e)
        } catch (e: Exception) {
            logger.error("There was an unexpected error in CommerceTools SDK trying to execute the request", e)
        }

        return null
    }
}

private fun CommerceToolsOptions.buildApiRoot(): ProjectApiRoot =
    ApiRootBuilder.of()
        .defaultClient(apiUrl)
        .withClientCredentialsFlow(
            ClientCredentialsBuilder()
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .withScopes(scopes)
                .build(),
            "${authUrl}/oauth/token",
        )
        .build(projectKey)
