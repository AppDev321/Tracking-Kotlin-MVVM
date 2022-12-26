package com.hnh.webrtc_calling_lib

import android.util.Log
import com.hnh.webrtc_calling_lib.listners.SignalingClientListener
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.webrtc.IceCandidate


class SignalingClient(
    private val serverUrl: String,
    private val listener: SignalingClientListener
) : CoroutineScope {
    private val job = Job()
    override val coroutineContext = Dispatchers.IO + job
    private val client = OkHttpClient()

    private val signalingWebSocket = SignalingWebSocket(listener)


    init {

        val request = Request.Builder().url(serverUrl).build()
        client.newWebSocket(request, signalingWebSocket)

        //   connect()
    }


/*
private val sendChannel = ConflatedBroadcastChannel<String>()
    private fun connect() = launch {
        val sendData = sendChannel.send("Hi Connection form library")
        sendData.let {
            Log.e(this@SignalingClient.javaClass.simpleName, "Sending: $it")
        }
    }
*/

    fun sendMessage(textData: String) = launch {
        signalingWebSocket.sendSDP(textData)


       val msg = signalingWebSocket.messageHandler



    }

    fun sendIceCandidate(candidate: IceCandidate?) = runBlocking {

        val type = when {
            else -> "offerCandidate"
        }

        val candidateConstant = hashMapOf(
            "serverUrl" to candidate?.serverUrl,
            "sdpMid" to candidate?.sdpMid,
            "sdpMLineIndex" to candidate?.sdpMLineIndex,
            "sdpCandidate" to candidate?.sdp,
            "type" to type
        )
    }

    fun destroy() {
        client.dispatcher.executorService.shutdown()
        job.complete()
    }
}
