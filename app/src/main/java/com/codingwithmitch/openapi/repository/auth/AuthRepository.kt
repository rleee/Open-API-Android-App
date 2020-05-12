package com.codingwithmitch.openapi.repository.auth

import androidx.lifecycle.LiveData
import com.codingwithmitch.openapi.api.auth.OpenApiAuthService
import com.codingwithmitch.openapi.api.auth.network_responses.LoginResponse
import com.codingwithmitch.openapi.api.auth.network_responses.RegistrationResponse
import com.codingwithmitch.openapi.models.AuthTokenModel
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.persistence.AuthTokenDao
import com.codingwithmitch.openapi.repository.NetworkBoundResource
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.auth.state.AuthViewState
import com.codingwithmitch.openapi.ui.auth.state.LoginFields
import com.codingwithmitch.openapi.ui.auth.state.RegistrationFields
import com.codingwithmitch.openapi.util.ApiSuccessResponse
import com.codingwithmitch.openapi.util.ErrorHandling
import com.codingwithmitch.openapi.util.GenericApiResponse
import kotlinx.coroutines.Job
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager
) {

    private var repositoryJob: Job? = null

    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {
        val loginFieldsError = LoginFields(email, password).isValidForLogin()
        if (loginFieldsError != LoginFields.LoginError.none()) {
            return returnErrorResponse(loginFieldsError, ResponseType.Dialog())
        }

        return object :
            NetworkBoundResource<LoginResponse, AuthViewState>(sessionManager.isConnectedToInternet()) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                // incorrect login credential count as 200 response from the server
                if (response.body.response == ErrorHandling.GENERIC_AUTH_ERROR) {
                    return onErrorReturn(
                        response.body.errorMessage, shouldUseDialog = true,
                        shouldUseToast = false
                    )
                }
                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthTokenModel(response.body.pk, response.body.response)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<LoginResponse>> {
                return openApiAuthService.login(email, password)
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

        }.asLiveData()
    }

    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<DataState<AuthViewState>> {

        val registrationFieldsError = RegistrationFields(email,username, password,confirmPassword).isValidForRegistration()
        if (registrationFieldsError != RegistrationFields.RegistrationError.none()) {
            return returnErrorResponse(registrationFieldsError, ResponseType.Dialog())
        }

        return object :
            NetworkBoundResource<RegistrationResponse, AuthViewState>(sessionManager.isConnectedToInternet()) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {
                // incorrect login credential count as 200 response from the server
                if (response.body.response == ErrorHandling.GENERIC_AUTH_ERROR) {
                    return onErrorReturn(
                        response.body.errorMessage, shouldUseDialog = true,
                        shouldUseToast = false
                    )
                }
                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthTokenModel(response.body.pk, response.body.response)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<RegistrationResponse>> {
                return openApiAuthService.register(email, username, password, confirmPassword)
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

        }.asLiveData()
    }

    private fun returnErrorResponse(
        errorMessage: String,
        responseType: ResponseType
    ): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.error(
                    Response(
                        errorMessage, responseType
                    )
                )
            }
        }
    }

    fun cancelActiveJob() {
        repositoryJob?.cancel()
    }
}