package com.afjltd.tracking.websocket

import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.websocket.listners.SocketMessageListener
import com.afjltd.tracking.websocket.model.MessageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.schedule


class SignalingClient : CoroutineScope {


    private val job = Job()
    override val coroutineContext = Dispatchers.IO + job
    private val signalingWebSocket = SignalingWebSocket.getInstance()


    //For auto reconnecting
    var config: Config = Config()
    private val isConnected = AtomicBoolean(false)
    private val isConnecting = AtomicBoolean(false)
    val status: Status
        get() = if (isConnected.get()) Status.CONNECTED else if (isConnecting.get()) Status.CONNECTING else Status.DISCONNECT
    var onConnectStatusChangeListener: ((status: Status) -> Unit)? = null
    private var timer: Timer? = null

    //*********************************************************************





    companion object {
        private var INSTANCE: SignalingClient? = null
        private val client =OkHttpClient()
        private var listener: SocketMessageListener? = null
        private var serverUrl: String = ""
        fun getInstance(
            listener: SocketMessageListener?,
            serverUrl: String,
        ): SignalingClient {
            this.listener = listener
            this.serverUrl = serverUrl
            return INSTANCE ?: synchronized(this) {
                INSTANCE = SignalingClient()
                return INSTANCE as SignalingClient
            }
        }
    }




    private val webSocketListener = object : SocketMessageListener() {
        override fun onConnectionEstablished() {
            isConnected.compareAndSet(false, true)
            isConnecting.compareAndSet(true, false)
            onConnectStatusChangeListener?.invoke(status)

            AFJUtils.writeLogs("Connection now Established")

            synchronized(this) {
                timer?.cancel()
                timer = null
            }

        }


        override fun onNewMessageReceived(message: MessageModel) {
            if (listener != null) {
                listener?.onNewMessageReceived(message)
            }
        }


        override fun onConnectionClosed() {
            if (listener != null) {
                isConnected.compareAndSet(true, false)
                onConnectStatusChangeListener?.invoke(status)
                doReconnect()
                listener?.onConnectionClosed()
            }
        }

        override fun onWebSocketFailure(errorMessage: String) {
            if (listener != null) {
                listener?.onWebSocketFailure(errorMessage)
                isConnected.compareAndSet(true, false)
                onConnectStatusChangeListener?.invoke(status)
                doReconnect()

            }
        }
    }




    init {
        AFJUtils.writeLogs("socket connect = $serverUrl")
        onConnectStatusChangeListener?.invoke(status)
        val request = Request.Builder().url(serverUrl).build()
        client.newWebSocket(request, signalingWebSocket)
        signalingWebSocket.setSignalingClientListener(webSocketListener)
        onConnectStatusChangeListener = {
            AFJUtils.writeLogs("Socket Status = $it")
        }
    }


    fun sendMessageToWebSocket(message: MessageModel) = launch {
        signalingWebSocket.sendMessageToSocket(message)
    }


    fun destroy() {


        INSTANCE = null
        //  client.dispatcher.executorService.shutdown()
        job.complete()
        signalingWebSocket.close()
    }


    private fun doReconnect() {
        if (!config.isAllowReconnect) {
            return
        }
        if (isConnected.get() || isConnecting.get()) {
            return
        }
        isConnecting.compareAndSet(false, true)
        onConnectStatusChangeListener?.invoke(status)
        synchronized(this) {
            if (timer == null) {
                timer = Timer()
            }
            timer?.schedule(config.reconnectInterval) {
                    destroy()
                    INSTANCE ?: synchronized(this) {
                        INSTANCE = SignalingClient()
                    }
            }


        }
    }
}


data class Config(
    val isAllowReconnect: Boolean = true,
    val reconnectCount: Int = Int.MAX_VALUE,
    val reconnectInterval: Long = 8000
)

enum class Status {
    CONNECTING, CONNECTED, DISCONNECT
}