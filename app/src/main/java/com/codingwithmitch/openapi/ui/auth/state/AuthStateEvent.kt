package com.codingwithmitch.openapi.ui.auth.state

sealed class AuthStateEvent {
    data class LoginAttemptEvent(
        val email: String,
        val password: String
    ) : AuthStateEvent()

    data class RegisterAttempEvent(
        val email: String,
        val username: String,
        val password: String,
        val confirmPassword: String
    ) : AuthStateEvent()

    class CheckPreviousAuthEvent : AuthStateEvent()
}