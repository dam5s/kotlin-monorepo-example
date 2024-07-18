package io.damo.kotlinmonorepo.helloserver.greetings

import io.damo.kotlinmonorepo.validationsupport.ValidationResult

object GreetingNameValidation {
    fun validate(name: String): ValidationResult {
        if (name.length < 3) {
            return ValidationResult.Failure("Name should have at least 3 characters")
        }

        return ValidationResult.Success
    }
}
