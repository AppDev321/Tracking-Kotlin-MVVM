package com.example.afjtracking.websocket

import android.os.Handler
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.websocket.listners.SocketMessageListener
import com.example.afjtracking.websocket.model.MessageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request


class SignalingClient : CoroutineScope {


    private val job = Job()
    override val coroutineContext = Dispatchers.IO + job

    private val signalingWebSocket = SignalingWebSocket.getInstance()


    companion object {
        private var INSTANCE: SignalingClient? = null
        private val client = OkHttpClient()
        private var listener: SocketMessageListener? = null
        private var serverUrl: String = ""
        fun getInstance(listener: SocketMessageListener?, serverUrl: String): SignalingClient {
            this.listener = listener
            this.serverUrl = serverUrl
            return INSTANCE ?: synchronized(this) {

                INSTANCE = SignalingClient()
                return INSTANCE as SignalingClient
            }
        }
    }

    init {
        AFJUtils.writeLogs("socket connect = $serverUrl")
        val request = Request.Builder().url(serverUrl).build()
        client.newWebSocket(request, signalingWebSocket)
        signalingWebSocket.setSignalingClientListener(object : SocketMessageListener() {
            override fun onNewMessageReceived(message: MessageModel) {

                if (listener != null) {

                    listener?.onNewMessageReceived(message)
                }
            }

            override fun onConnectionClosed() {
                if (listener != null) {

                        listener?.onConnectionClosed()


                }
            }

            override fun onWebSocketFailure(errorMessage: String) {
                    if (listener != null) {
                        listener?.onWebSocketFailure(errorMessage)
                    }
            }
        })
    }




   /* fun registerSignalListener(listener: SocketMessageListener) {
        signalingWebSocket.setSignalingClientListener(object : SocketMessageListener() {
            override fun onNewMessageReceived(message: MessageModel) {
                if (listener != null) {
                    listener?.onNewMessageReceived(message)
                }
            }
        })
    }*/

    fun sendMessageToWebSocket(message: MessageModel) = launch {
        signalingWebSocket.sendMessageToSocket(message)
    }


    fun destroy() {


        INSTANCE = null
      //  client.dispatcher.executorService.shutdown()
        job.complete()
        signalingWebSocket.close()
    }
}
