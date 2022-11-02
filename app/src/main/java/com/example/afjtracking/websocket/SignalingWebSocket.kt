package com.example.afjtracking.websocket


import android.util.Log
import com.example.afjtracking.websocket.listners.SignalingClientListener
import com.example.afjtracking.websocket.model.MessageModel
import com.example.afjtracking.websocket.model.WebSocketMessage
import com.google.gson.Gson
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener


class SignalingWebSocket(

    private val listener: SignalingClientListener
) : WebSocketListener() {
    companion object {
        private val TAG = "SignalingWebSocket"
    }

    var webSocket: WebSocket? = null
    private val gson = Gson()
    override fun onOpen(webSocket: WebSocket, response: Response) {
        this.webSocket = webSocket
        listener.onConnectionEstablished()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        listener.onNewMessageReceived(gson.fromJson(text, MessageModel::class.java))
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.e(TAG, "WebSocket: closed")
        super.onClosing(webSocket, code, reason)
        this.webSocket = null
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        Log.e(TAG, "WebSocket: Failure($t)")
    }

    fun close() {
        webSocket?.close(1000, null)
    }

    fun sendMessageToSocket(message: MessageModel) {
        try {
            Log.e(TAG, "sendMessageToSocket: ${Gson().toJson(message)}")
            webSocket?.send(Gson().toJson(message))
        } catch (e: Exception) {
            Log.e(TAG, "sendMessageToSocketException: $e")
        }
    }

}

