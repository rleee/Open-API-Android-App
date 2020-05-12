package com.codingwithmitch.openapi.repository.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import com.codingwithmitch.openapi.api.auth.OpenApiAuthService
import com.codingwithmitch.openapi.api.auth.network_responses.LoginResponse
import com.codingwithmitch.openapi.api.auth.network_responses.RegistrationResponse
import com.codingwithmitch.openapi.models.AccountPropertiesModel
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
import com.codingwithmitch.openapi.util.*
import kotlinx.coroutines.Job
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    val sharedPreferencesEditor: SharedPreferences.Editor
) {

    private var repositoryJob: Job? = null

    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {
        val loginFieldsError = LoginFields(email, password).isValidForLogin()
        if (loginFieldsError != LoginFields.LoginError.none()) {
            return returnErrorResponse(loginFieldsError, ResponseType.Dialog())
        }

        return object :
            NetworkBoundResource<LoginResponse, AuthViewState>(
                sessionManager.isConnectedToInternet(),
                true
            ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                // incorrect login credential count as 200 response from the server
                if (response.body.response == ErrorHandling.GENERIC_AUTH_ERROR) {
                    return onErrorReturn(
                        response.body.errorMessage, shouldUseDialog = true,
                        shouldUseToast = false
                    )
                }

                // don't care about the result. Just insert if it doesn't exists b/c foreign key relationship
                // with AuthToken table
                accountPropertiesDao.insertAndIgnore(
                    AccountPropertiesModel(
                        response.body.pk,
                        response.body.email,
                        ""
                    )
                )

                // will return -1 if failed
                val result = authTokenDao.insert(
                    AuthTokenModel(
                        response.body.pk,
                        response.body.token
                    )
                )

                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(ErrorHandling.ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                        )
                    )
                }

                // save authenticated user to sharedpreferences (email)
                saveAuthenticatedUserToPrefs(email)

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

            override suspend fun createCacheRequestAndReturn() {
                TODO("Not yet implemented")
            }

        }.asLiveData()
    }

    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<DataState<AuthViewState>> {

        val registrationFieldsError =
            RegistrationFields(email, username, password, confirmPassword).isValidForRegistration()
        if (registrationFieldsError != RegistrationFields.RegistrationError.none()) {
            return returnErrorResponse(registrationFieldsError, ResponseType.Dialog())
        }

        return object :
            NetworkBoundResource<RegistrationResponse, AuthViewState>(
                sessionManager.isConnectedToInternet(),
                true
            ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {
                // incorrect login credential count as 200 response from the server
                if (response.body.response == ErrorHandling.GENERIC_AUTH_ERROR) {
                    return onErrorReturn(
                        response.body.errorMessage, shouldUseDialog = true,
                        shouldUseToast = false
                    )
                }

                // don't care about the result. Just insert if it doesn't exists b/c foreign key relationship
                // with AuthToken table
                accountPropertiesDao.insertAndIgnore(
                    AccountPropertiesModel(
                        response.body.pk,
                        response.body.email,
                        ""
                    )
                )

                // will return -1 if failed
                val result = authTokenDao.insert(
                    AuthTokenModel(
                        response.body.pk,
                        response.body.token
                    )
                )

                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(ErrorHandling.ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                        )
                    )
                }

                // save authenticated user to sharedpreferences (email)
                saveAuthenticatedUserToPrefs(email)

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

            override suspend fun createCacheRequestAndReturn() {
                TODO("Not yet implemented")
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

    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.data(
                    data = null,
                    response = Response(
                        SuccessHandling.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None()
                    )
                )
            }
        }
    }

    fun cancelActiveJob() {
        repositoryJob?.cancel()
    }

    private fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPreferencesEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPreferencesEditor.apply()
    }

    fun checkPreviousAuthUser(): LiveData<DataState<AuthViewState>> {
        val previousAuthUserEmail: String? =
            sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)
        if (previousAuthUserEmail.isNullOrBlank()) {
            return returnNoTokenFound()
        }

        return object : NetworkBoundResource<Void, AuthViewState>(
            sessionManager.isConnectedToInternet(),
            false
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {
                // not used in this case, because this is not network request
            }

            override suspend fun createCacheRequestAndReturn() {
                accountPropertiesDao.searchByEmail(previousAuthUserEmail)
                    .let { accountPropertiesModel: AccountPropertiesModel? ->
                        accountPropertiesModel?.let {
                            if (it.pk > -1) {
                                authTokenDao.searchByPk(it.pk)
                                    .let { authTokenModel: AuthTokenModel? ->
                                        if (authTokenModel != null) {
                                            onCompleteJob(
                                                DataState.data(
                                                    data = AuthViewState(
                                                        authToken = authTokenModel
                                                    )
                                                )
                                            )
                                            return
                                        }
                                    }
                            }
                        }
                        onCompleteJob(
                            DataState.data(
                                data = null,
                                response = Response(
                                    SuccessHandling.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                    ResponseType.None()
                                )
                            )
                        )
                    }
            }

            override fun createCall(): LiveData<GenericApiResponse<Void>> {
                // not used in this case, because this is not network request
                return AbsentLiveData.create()
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

        }.asLiveData()
    }
}