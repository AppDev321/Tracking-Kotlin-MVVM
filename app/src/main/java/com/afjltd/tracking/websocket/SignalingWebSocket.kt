package com.afjltd.tracking.websocket


import android.util.Log
import com.afjltd.tracking.websocket.listners.SocketMessageListener
import com.afjltd.tracking.websocket.model.MessageModel
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
        Log.d(TAG, "Receiving Msg from Server: ${text}")
        signalingClientListener?.onNewMessageReceived(gson.fromJson(text, MessageModel::class.java))
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.e(TAG, "WebSocket: try closing...")
        super.onClosing(webSocket, code, reason)


    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        Log.e(TAG, "WebSocket: closed")
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

