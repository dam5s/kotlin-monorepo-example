package io.damo.kotlinmonorepo.validationsupport

sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Failure(val message: String) : ValidationResult()
}
