package com.codingwithmitch.openapi.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.BaseActivity
import com.codingwithmitch.openapi.ui.auth.AuthActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        subscribeObserver()

        // will remove later, only for testing to logout
        tool_bar.setOnClickListener {
            sessionManager.logout()
        }
    }

    override fun displayProgressBar(boolean: Boolean) {
        if (boolean) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility = View.GONE
        }
    }

    private fun subscribeObserver() {
        sessionManager.cacheToken.observe(this, Observer { authToken ->
            Log.d(TAG, "MainActivity: subscribeObserver: AuthToken: $authToken")
            if (authToken == null || authToken.account_pk == -1 || authToken.token == null) {
                navAuthActivity()
            }
        })
    }

    private fun navAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }
}