package com.codingwithmitch.openapi.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.util.*
import kotlinx.coroutines.*


abstract class NetworkBoundResource<ResponseObject, ViewStateType>
constructor(isNetworkAvailable: Boolean) {

    private val TAG = "AppDebug"
    protected val result = MediatorLiveData<DataState<ViewStateType>>()
    protected lateinit var job: CompletableJob
    protected lateinit var coroutineScope: CoroutineScope

    init {
        setJob(initNewJob())
        setValue(DataState.loading(true, null))

        if (isNetworkAvailable) {
            coroutineScope.launch {
                // simulate network delay
                delay(Constants.TESTING_NETWORK_DELAY)

                withContext(Dispatchers.Main) {
                    // make network call, why on main thread, because we will use mediator liveData
                    val apiResponse = createCall()
                    result.addSource(apiResponse) { response: GenericApiResponse<ResponseObject>? ->
                        result.removeSource(apiResponse)

                        coroutineScope.launch {
                            handleNetworkCall(response)
                        }
                    }
                }
                GlobalScope.launch(Dispatchers.IO) {
                    delay(Constants.NETWORK_TIMEOUT)
                    if (!job.isCompleted) {
                        Log.e(TAG, "NetworkBoundResource: Job network timeout")
                        job.cancel(CancellationException(ErrorHandling.UNABLE_TO_RESOLVE_HOST))
                    }
                }
            }
        } else {
            onErrorReturn(
                ErrorHandling.UNABLE_TODO_OPERATION_WO_INTERNET,
                shouldUseDialog = true,
                shouldUseToast = false
            )
        }
    }

    private suspend fun handleNetworkCall(genericApiResponse: GenericApiResponse<ResponseObject>?) {
        when (genericApiResponse) {
            is ApiSuccessResponse -> {
                handleApiSuccessResponse(genericApiResponse)
                Log.d(TAG, "handleNetworkCall: ApiSuccessResponse: ${genericApiResponse.body}")
            }
            is ApiErrorResponse -> {
                Log.e(TAG, "NetworkBoundResource: ${genericApiResponse.errorMessage}")
                onErrorReturn(
                    genericApiResponse.errorMessage,
                    shouldUseDialog = true,
                    shouldUseToast = false
                )
            }
            is ApiEmptyResponse -> {
                Log.e(TAG, "NetworkBoundResource: Request returned Nothing HTTP 204")
                onErrorReturn(
                    "Request returned Nothing HTTP 204",
                    shouldUseDialog = true,
                    shouldUseToast = false
                )
            }
        }
    }

    private fun setValue(dataState: DataState<ViewStateType>) {
        result.value = dataState
    }

    fun onCompleteJob(dataState: DataState<ViewStateType>) {
        GlobalScope.launch(Dispatchers.Main) {
            job.complete()
            setValue(dataState)
        }
    }

    fun onErrorReturn(errorMessage: String?, shouldUseDialog: Boolean, shouldUseToast: Boolean) {
        var msg = errorMessage
        var useDialog = shouldUseDialog
        var responseType: ResponseType = ResponseType.None()

        if (msg == null) {
            msg = ErrorHandling.ERROR_UNKNOWN
        } else if (ErrorHandling.isNetworkError(msg)) {
            msg = ErrorHandling.ERROR_CHECK_NETWORK_CONNECTION
            useDialog = false
        }

        if (shouldUseToast) responseType = ResponseType.Toast()
        if (useDialog) responseType = ResponseType.Dialog()

        onCompleteJob(
            DataState.error(
                response = Response(
                    message = msg,
                    responseType = responseType
                )
            )
        )
    }

    @OptIn(InternalCoroutinesApi::class)
    private fun initNewJob(): Job {
        Log.d(TAG, "initNewJob: called...")
        job = Job()
        job.invokeOnCompletion(
            onCancelling = true,
            invokeImmediately = true,
            handler = object : CompletionHandler {
                override fun invoke(cause: Throwable?) {
                    if (job.isCancelled) {
                        Log.e(TAG, "NetworkBoundResource: Job has been cancelled")
                        cause?.let {
                            onErrorReturn(
                                it.message,
                                shouldUseDialog = false,
                                shouldUseToast = true
                            )
                        } ?: onErrorReturn(
                            ErrorHandling.ERROR_UNKNOWN,
                            shouldUseDialog = false,
                            shouldUseToast = true
                        )
                    } else if (job.isCompleted) {
                        Log.i(TAG, "NetworkBoundResource: Job has been completed")
                        // Do nothing job has been handled
                    }
                }
            })
        // what is this? got note on Notable.md
        coroutineScope = CoroutineScope(Dispatchers.IO + job)
        return job
    }

    abstract suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<ResponseObject>)
    abstract fun createCall(): LiveData<GenericApiResponse<ResponseObject>>
    abstract fun setJob(job: Job)

    fun asLiveData() = result as LiveData<DataState<ViewStateType>>
}