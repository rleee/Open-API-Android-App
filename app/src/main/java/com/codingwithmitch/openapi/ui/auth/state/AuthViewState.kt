package com.codingwithmitch.openapi.ui.auth.state

import com.codingwithmitch.openapi.models.AuthTokenModel

data class AuthViewState(
    var registrationFields: RegistrationFields? = RegistrationFields(),
    var loginFields: LoginFields? = LoginFields(),
    var authToken: AuthTokenModel? = null
)

data class RegistrationFields(
    var registrationEmail: String? = null,
    var registrationUsername: String? = null,
    var registrationPassword: String? = null,
    var registrationConfirmPassword: String? = null
) {
    class RegistrationError {
        companion object {
            fun mustFillAllFields(): String = "All fields are required."
            fun passwordDoNotMatch(): String = "Password must match."
            fun none(): String = "None"
        }
    }

    fun isValidForRegistration(): String {
        if (registrationEmail.isNullOrEmpty() ||
            registrationUsername.isNullOrEmpty() ||
            registrationPassword.isNullOrEmpty() ||
            registrationConfirmPassword.isNullOrEmpty()
        ) {
            return RegistrationError.mustFillAllFields()
        }

        if (!registrationPassword.equals(registrationConfirmPassword)) {
            return RegistrationError.passwordDoNotMatch()
        }

        return RegistrationError.none()
    }
}

data class LoginFields(
    var login_email: String? = null,
    var login_password: String? = null
) {
    class LoginError {
        companion object {
            fun mustFillAllFields(): String = "You can't login without an email and password."
            fun none(): String = "None"
        }
    }

    fun isValidForLogin(): String {
        if (login_email.isNullOrEmpty() || login_password.isNullOrEmpty()) {
            return LoginError.mustFillAllFields()
        }
        return LoginError.none()
    }
}