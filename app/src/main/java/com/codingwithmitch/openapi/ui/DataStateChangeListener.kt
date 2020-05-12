package com.codingwithmitch.openapi.ui

interface DataStateChangeListener {

    fun onDataStateChanged(dataState: DataState<*>?)
}