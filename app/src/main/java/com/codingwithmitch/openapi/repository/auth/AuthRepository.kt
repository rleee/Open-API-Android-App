package com.codingwithmitch.openapi.repository.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.codingwithmitch.openapi.api.auth.OpenApiAuthService
import com.codingwithmitch.openapi.api.auth.network_responses.LoginResponse
import com.codingwithmitch.openapi.api.auth.network_responses.RegistrationResponse
import com.codingwithmitch.openapi.models.AuthTokenModel
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.persistence.AuthTokenDao
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.auth.state.AuthViewState
import com.codingwithmitch.openapi.util.*
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager
) {

    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {
        return openApiAuthService.login(email, password)
            .switchMap { genericApiResponse: GenericApiResponse<LoginResponse> ->
                object : LiveData<DataState<AuthViewState>>() {
                    override fun onActive() {
                        super.onActive()

                        return when (genericApiResponse) {
                            is ApiSuccessResponse -> {
                                value = DataState.data(
                                    data = AuthViewState(
                                        authToken = AuthTokenModel(
                                            genericApiResponse.body.pk,
                                            genericApiResponse.body.token
                                        )
                                    ),
                                    response = null
                                )
                            }
                            is ApiErrorResponse -> {
                                value = DataState.error(
                                    Response(
                                        message = genericApiResponse.errorMessage,
                                        responseType = ResponseType.Dialog()
                                    )
                                )
                            }
                            is ApiEmptyResponse -> {
                                value = DataState.error(
                                    Response(
                                        message = ErrorHandling.ERROR_UNKNOWN,
                                        responseType = ResponseType.Dialog()
                                    )
                                )
                            }
                        }
                    }
                }
            }
    }

    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<DataState<AuthViewState>> {
        return openApiAuthService.register(email, username, password, confirmPassword)
            .switchMap { genericApiResponse: GenericApiResponse<RegistrationResponse> ->
                object : LiveData<DataState<AuthViewState>>() {
                    override fun onActive() {
                        super.onActive()

                        return when (genericApiResponse) {
                            is ApiSuccessResponse -> {
                                value = DataState.data(
                                    data = AuthViewState(
                                        authToken = AuthTokenModel(
                                            genericApiResponse.body.pk,
                                            genericApiResponse.body.token
                                        )
                                    ),
                                    response = null
                                )
                            }
                            is ApiErrorResponse -> {
                                value = DataState.error(
                                    Response(
                                        message = genericApiResponse.errorMessage,
                                        responseType = ResponseType.Dialog()
                                    )
                                )
                            }
                            is ApiEmptyResponse -> {
                                value = DataState.error(
                                    Response(
                                        message = ErrorHandling.ERROR_UNKNOWN,
                                        responseType = ResponseType.Dialog()
                                    )
                                )
                            }
                        }
                    }
                }
            }
    }
}