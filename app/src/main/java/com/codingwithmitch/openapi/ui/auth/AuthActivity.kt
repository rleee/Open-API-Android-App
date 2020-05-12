package com.codingwithmitch.openapi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.*
import com.codingwithmitch.openapi.ui.auth.state.AuthViewState
import com.codingwithmitch.openapi.ui.main.MainActivity
import com.codingwithmitch.openapi.viewmodels.ViewModelProviderFactory
import javax.inject.Inject

class AuthActivity : BaseActivity(), NavController.OnDestinationChangedListener {

    @Inject
    lateinit var factory: ViewModelProviderFactory
    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        findNavController(R.id.auth_nav_host_fragment).addOnDestinationChangedListener(this)

        subscribeObserver()
    }

    private fun subscribeObserver() {
        viewModel.dataState.observe(this, Observer { dataState: DataState<AuthViewState> ->
            dataState.data?.let { data: Data<AuthViewState> ->
                data.data?.let { event: Event<AuthViewState> ->
                    event.getContentIfNotHandled()?.let { authViewState: AuthViewState ->
                        authViewState.authToken?.let {
                            Log.d(TAG, "AuthActivity: DataState: $it")
                            viewModel.setAuthTokenFields(it)
                        }
                    }
                }
                data.response?.let { event: Event<Response> ->
                    event.getContentIfNotHandled()?.let { response: Response ->
                        when (response.responseType) {
                            is ResponseType.Dialog -> {
                                // inflate dialog
                            }
                            is ResponseType.Toast -> {
                                // show toast
                            }
                            is ResponseType.None -> {
                                Log.e(TAG, "AuthActivity: DataState: $response.message")
                            }
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(this, Observer { authViewState ->
            authViewState.authToken?.let {
                sessionManager.login(it)
            }
        })

        sessionManager.cacheToken.observe(this, Observer { authToken ->
            Log.d(TAG, "MainActivity: subscribeObserver: AuthToken: $authToken")
            if (authToken != null && authToken.account_pk != -1 && authToken.token != null) {
                navMainActivity()
            }
        })
    }

    private fun navMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        viewModel.cancelActiveJobs()
    }
}