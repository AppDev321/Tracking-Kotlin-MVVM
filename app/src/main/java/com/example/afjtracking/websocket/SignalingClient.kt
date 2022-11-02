package com.example.afjtracking.websocket

import android.util.Log
import com.example.afjtracking.websocket.SignalingWebSocket
import com.example.afjtracking.websocket.listners.SignalingClientListener
import com.example.afjtracking.websocket.model.MessageModel
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.webrtc.IceCandidate


class SignalingClient(
    serverUrl: String,
    listener: SignalingClientListener
) : CoroutineScope {
    private val job = Job()
    override val coroutineContext = Dispatchers.IO + job
    private val client = OkHttpClient()
    private val signalingWebSocket = SignalingWebSocket(listener)


    init {
        val request = Request.Builder().url(serverUrl).build()
        client.newWebSocket(request, signalingWebSocket)
    }


    fun sendMessageToWebSocket(message: MessageModel) = launch {
        signalingWebSocket.sendMessageToSocket(message)
    }


    fun destroy() {
        signalingWebSocket.close()
        client.dispatcher.executorService.shutdown()
        job.complete()
    }
}
