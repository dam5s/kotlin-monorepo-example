package testing.kotlinmonorepo.integrationtests

import io.damo.kotlinmonorepo.helloserver.customersapi.CommerceToolsCustomersApiGateway
import io.damo.kotlinmonorepo.helloserver.customersapi.CommerceToolsOptions
import io.damo.kotlinmonorepo.helloserver.customersapi.EcommerceCustomer
import io.damo.kotlinmonorepo.serversupport.requiredEnvironmentVariable
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class CommerceToolsCustomersApiGatewayTest {

    private val options = CommerceToolsOptions(
        authUrl = requiredEnvironmentVariable("COMMERCE_TOOLS_AUTH_URL"),
        apiUrl = requiredEnvironmentVariable("COMMERCE_TOOLS_API_URL"),
        projectKey = requiredEnvironmentVariable("COMMERCE_TOOLS_PROJECT_KEY"),
        clientId = requiredEnvironmentVariable("COMMERCE_TOOLS_CLIENT_ID"),
        clientSecret = requiredEnvironmentVariable("COMMERCE_TOOLS_CLIENT_SECRET"),
        scopes = requiredEnvironmentVariable("COMMERCE_TOOLS_SCOPES"),
    )

    private val gateway = CommerceToolsCustomersApiGateway(options)

    @Test
    fun `test tryFindCustomer`() = runBlocking {
        val customer = gateway.tryFindCustomer("0bff9fc8-a2b4-45fc-a68f-d703d0b2cf25")

        val expected = EcommerceCustomer(
            userId = "0bff9fc8-a2b4-45fc-a68f-d703d0b2cf25",
            email = "damien@initialcapacity.io",
            firstName = "Damien"
        )
        assertEquals(expected, customer)
    }

    @Test
    fun `test tryFindCustomerByEmail`() = runBlocking {
        val customer = gateway.tryFindCustomerByEmail("damien@initialcapacity.io")

        val expected = EcommerceCustomer(
            userId = "0bff9fc8-a2b4-45fc-a68f-d703d0b2cf25",
            email = "damien@initialcapacity.io",
            firstName = "Damien"
        )
        assertEquals(expected, customer)
    }
}
