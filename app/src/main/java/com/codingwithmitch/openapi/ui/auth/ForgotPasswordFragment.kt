package com.codingwithmitch.openapi.ui.auth

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient

import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.*
import com.codingwithmitch.openapi.util.Constants
import kotlinx.android.synthetic.main.fragment_forgot_password.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ForgotPasswordFragment : BaseAuthFragment() {

    lateinit var webView: WebView
    lateinit var stateChangeListener: DataStateChangeListener
    private val webInteractionCallback = object : WebAppInterface.OnWebInteractionCallback {
        override fun onSuccess(email: String) {
            Log.d(TAG, "ForgotPasswordFragment: onSuccess: a reset link will be sent to $email")
            onPasswordResetLinkSent()
        }

        override fun onError(errorMessage: String) {
            Log.e(TAG, "ForgotPasswordFragment: onError: $errorMessage")
            val dataState = DataState.error<Any>(
                response = Response(errorMessage, ResponseType.Dialog())
            )
            stateChangeListener.onDataStateChanged(dataState)
        }

        override fun onLoading(isLoading: Boolean) {
            Log.d(TAG, "ForgotPasswordFragment: isLoading... ")
            GlobalScope.launch(Dispatchers.Main) {
                val dataState = DataState.loading(isLoading, null)
                stateChangeListener.onDataStateChanged(dataState)
            }

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.e(
                TAG,
                "onAttach: ForgotPasswordFragment: $context must implement DataStateListener"
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "ForgotPasswordFragment: onViewCreated: ${viewModel.hashCode()}")

        webView = view.findViewById(R.id.webview)
        loadPasswordResetWebView()
    }

    private fun onPasswordResetLinkSent() {
        GlobalScope.launch(Dispatchers.Main) {
            parent_view.removeView(webView)
            webView.destroy()

            val animation = TranslateAnimation(
                password_reset_done_container.width.toFloat(),
                0F,
                0F,
                0F
            ).apply { duration = 500 }
            password_reset_done_container.apply {
                startAnimation(animation)
                visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadPasswordResetWebView() {
        stateChangeListener.onDataStateChanged(
            DataState.loading(
                isLoading = true,
                cachedData = null
            )
        )
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                stateChangeListener.onDataStateChanged(
                    DataState.loading(
                        isLoading = false,
                        cachedData = null
                    )
                )
            }
        }
        webView.loadUrl(Constants.PASSWORD_RESET_URL)
        webView.settings.javaScriptEnabled = true

        // "AndroidTextListener" MUST be the same as what implemented on the webPage / webView
        webview.addJavascriptInterface(
            WebAppInterface(webInteractionCallback),
            "AndroidTextListener"
        )
    }

    // this class will interact with webView
    class WebAppInterface(private val callBack: OnWebInteractionCallback) {

        private val TAG = "AppDebug"

        @JavascriptInterface
        fun onSuccess(email: String) {
            callBack.onSuccess(email)
        }

        @JavascriptInterface
        fun onError(errorMessage: String) {
            callBack.onError(errorMessage)
        }

        @JavascriptInterface
        fun onLoading(isLoading: Boolean) {
            callBack.onLoading((isLoading))
        }

        // this interface will interact with fragment
        interface OnWebInteractionCallback {
            fun onSuccess(email: String)
            fun onError(errorMessage: String)
            fun onLoading(isLoading: Boolean)
        }
    }
}
