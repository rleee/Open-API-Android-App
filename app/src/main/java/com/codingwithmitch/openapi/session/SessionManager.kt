package com.codingwithmitch.openapi.session

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.codingwithmitch.openapi.models.AuthTokenModel
import com.codingwithmitch.openapi.persistence.AuthTokenDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val application: Application
) {
    private val TAG = "DEBUG"

    private val _cachedToken = MutableLiveData<AuthTokenModel>()
    val cacheToken: LiveData<AuthTokenModel>
        get() = _cachedToken

    fun login(newToken: AuthTokenModel) {
        setValue(newToken)
    }

    fun logout() {
        GlobalScope.launch(Dispatchers.IO) {
            var errorMessage: String? = null
            try {
                // remove token from RoomDatabase
                _cachedToken.value!!.account_pk?.let {
                    authTokenDao.nullifyToken(it)
                }
            } catch (e: CancellationException) {
                errorMessage = e.message
            } catch (e: Exception) {
                errorMessage = "$errorMessage \n ${e.message}"
            } finally {
                errorMessage?.let {
                    Log.e(TAG, "logout: $it")
                }
                Log.d(TAG, "logout: finally...")
                setValue(null)
            }
        }
    }

    private fun setValue(newToken: AuthTokenModel?) {
        GlobalScope.launch(Dispatchers.Main) {
            if (_cachedToken.value != newToken) {
                _cachedToken.value = newToken
            }
        }
    }

    fun isConnectedToInternet(): Boolean {
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            return cm.activeNetworkInfo.isConnected
        } catch (e: Exception) {
            Log.e(TAG, "isConnectedToInternet: ${e.message}")
        }
        return false
    }
}