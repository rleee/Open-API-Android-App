package com.codingwithmitch.openapi.ui.auth

import androidx.lifecycle.LiveData
import com.codingwithmitch.openapi.models.AuthTokenModel
import com.codingwithmitch.openapi.repository.auth.AuthRepository
import com.codingwithmitch.openapi.ui.BaseViewModel
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.auth.state.AuthStateEvent
import com.codingwithmitch.openapi.ui.auth.state.AuthViewState
import com.codingwithmitch.openapi.ui.auth.state.LoginFields
import com.codingwithmitch.openapi.ui.auth.state.RegistrationFields
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
    val authRepository: AuthRepository
) : BaseViewModel<AuthStateEvent, AuthViewState>() {

    fun setRegistrationFields(registrationFields: RegistrationFields) {
        val viewState = getCurrentViewStateOrNew()
        if (viewState.registrationFields == registrationFields) {
            return
        }
        viewState.registrationFields = registrationFields
        _viewState.value = viewState
    }

    fun setLoginFields(loginFields: LoginFields) {
        val viewState = getCurrentViewStateOrNew()
        if (viewState.loginFields == loginFields) {
            return
        }
        viewState.loginFields = loginFields
        _viewState.value = viewState
    }

    fun setAuthTokenFields(authToken: AuthTokenModel) {
        val viewState = getCurrentViewStateOrNew()
        if (viewState.authToken == authToken) {
            return
        }
        viewState.authToken = authToken
        _viewState.value = viewState
    }

    fun cancelActiveJobs() {
        authRepository.cancelActiveJob()
    }

    override fun onCleared() {
        authRepository.cancelActiveJob()
    }

    override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>> {
        return when (stateEvent) {
            is AuthStateEvent.LoginAttemptEvent -> {
                authRepository.attemptLogin(
                    stateEvent.email,
                    stateEvent.password
                )
            }
            is AuthStateEvent.RegisterAttempEvent -> {
                authRepository.attemptRegistration(
                    stateEvent.email,
                    stateEvent.username,
                    stateEvent.password,
                    stateEvent.confirmPassword
                )
            }
            is AuthStateEvent.CheckPreviousAuthEvent -> {
                authRepository.checkPreviousAuthUser()
            }
        }
    }

    override fun initNewViewState(): AuthViewState {
        return AuthViewState()
    }
}