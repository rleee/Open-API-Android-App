package com.codingwithmitch.openapi.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer

import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.util.ApiEmptyResponse
import com.codingwithmitch.openapi.util.ApiErrorResponse
import com.codingwithmitch.openapi.util.ApiSuccessResponse

class RegisterFragment : BaseAuthFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "RegisterFragment: onViewCreated: ${viewModel.hashCode()}")

        viewModel.testRegister().observe(
            viewLifecycleOwner,
            Observer {
                when(it) {
                    is ApiSuccessResponse -> {
                        Log.d(TAG, "onViewCreated: REGISTER RESPONSE: ${it.body}")
                    }
                    is ApiEmptyResponse -> {
                        Log.d(TAG, "onViewCreated: REGISTER RESPONSE: Empty Response")
                    }
                    is ApiErrorResponse -> {
                        Log.d(TAG, "onViewCreated: REGISTER RESPONSE: ${it.errorMessage}")
                    }
                }
            }
        )
    }

}
