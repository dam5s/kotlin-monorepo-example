package io.damo.kotlinmonorepo.serversupport

import io.damo.kotlinmonorepo.validationsupport.ValidationResult
import io.grpc.Status
import io.grpc.StatusException

fun validateOrThrow(result: ValidationResult) {
    if (result is ValidationResult.Failure) {
        val status = Status.INVALID_ARGUMENT.withDescription(result.message)
        throw StatusException(status)
    }
}
