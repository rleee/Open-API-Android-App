package com.codingwithmitch.openapi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.BaseActivity
import com.codingwithmitch.openapi.ui.main.MainActivity
import com.codingwithmitch.openapi.viewmodels.ViewModelProviderFactory
import javax.inject.Inject

class AuthActivity : BaseActivity() {

    @Inject
    lateinit var factory: ViewModelProviderFactory
    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        subscribeObserver()
    }

    private fun subscribeObserver() {
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
}