package io.damo.kotlinmonorepo.helloserver.customersapi

interface CustomersApiGateway {
    suspend fun tryFindCustomer(userId: String): EcommerceCustomer?
    suspend fun tryFindCustomerByEmail(email: String): EcommerceCustomer?
}

data class EcommerceCustomer(
    val userId: String,
    val email: String,
    val firstName: String?,
)

fun buildTestEcommerceCustomer(
    userId: String = "urn:user:1",
    email: String = "fred@example.com",
    firstName: String? = "Fred",
) = EcommerceCustomer(userId, email, firstName)
