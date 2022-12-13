package com.example.afjtracking.callscreen

interface CallEventListener {
    fun onListen()
    fun send(event: String, body: Map<String, Any>)
    fun onCancel(arguments: Any?)
}