package com.codingwithmitch.openapi.ui

import android.util.Log
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.util.displayErrorDialog
import com.codingwithmitch.openapi.util.displaySuccessDialog
import com.codingwithmitch.openapi.util.displayToast
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity(), DataStateChangeListener {

    val TAG: String = "AppDebug"

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onDataStateChanged(dataState: DataState<*>?) {

        dataState?.let {
            GlobalScope.launch(Dispatchers.Main) {
                displayProgressBar(it.loading.isLoading)

                it.error?.let { event: Event<StateError> ->
                    handleStateError(event)
                }
                it.data?.let {
                    it.response?.let { event: Event<Response> ->
                        handleStateResponse(event)
                    }
                }
            }
        }
    }

    private fun handleStateError(errorEvent: Event<StateError>) {
        errorEvent.getContentIfNotHandled()?.let { stateError: StateError ->
            when (stateError.response.responseType) {
                is ResponseType.Toast -> {
                    stateError.response.message?.let { message: String ->
                        displayToast(message)
                    }
                }
                is ResponseType.Dialog -> {
                    stateError.response.message?.let { message: String ->
                        displayErrorDialog(message)
                    }
                }
                is ResponseType.None -> {
                    Log.e(TAG, "handleStateError: ${stateError.response.message}")
                }
            }
        }
    }

    private fun handleStateResponse(event: Event<Response>) {
        event.getContentIfNotHandled()?.let { response: Response ->
            when (response.responseType) {
                is ResponseType.Toast -> {
                    response.message?.let { message: String ->
                        displayToast(message)
                    }
                }
                is ResponseType.Dialog -> {
                    response.message?.let { message: String ->
                        displaySuccessDialog(message)
                    }
                }
                is ResponseType.None -> {
                    Log.e(TAG, "handleStateResponse: ${response.message}")
                }
            }
        }
    }

    abstract fun displayProgressBar(boolean: Boolean)
}

