package com.example.afjtracking.websocket


import android.util.Log
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.websocket.listners.SignalingClientListener
import com.example.afjtracking.websocket.listners.SocketMessageListener
import com.example.afjtracking.websocket.model.MessageModel
import com.google.gson.Gson
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener


class SignalingWebSocket: WebSocketListener() {
    companion object {
        private val TAG = "SignalingWebSocket"

        private var INSTANCE: SignalingWebSocket? = null

        fun getInstance(): SignalingWebSocket {
            return INSTANCE ?: synchronized(this) {
                INSTANCE = SignalingWebSocket()
                return INSTANCE as SignalingWebSocket
            }
        }
    }


    private var signalingClientListener: SocketMessageListener? = null

    fun setSignalingClientListener(signalingClientListener: SocketMessageListener?) {
        this.signalingClientListener = signalingClientListener
    }

    var webSocket: WebSocket? = null
    private val gson = Gson()
    override fun onOpen(webSocket: WebSocket, response: Response) {
        this.webSocket = webSocket

        signalingClientListener?.onConnectionEstablished()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        signalingClientListener?.onNewMessageReceived(gson.fromJson(text, MessageModel::class.java))
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.e(TAG, "WebSocket: closed")
        super.onClosing(webSocket, code, reason)
        this.webSocket = null
        signalingClientListener?.onConnectionClosed()

    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        signalingClientListener?.onWebSocketFailure(t.message.toString())

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

