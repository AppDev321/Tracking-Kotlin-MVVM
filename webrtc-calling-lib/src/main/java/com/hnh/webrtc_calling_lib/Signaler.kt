package com.hnh.webrtc_calling_lib


import android.util.Log
import com.hnh.webrtc_calling_lib.listners.SignalingClientListener
import com.hnh.webrtc_calling_lib.model.*
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject


class SignalingWebSocket(
    private val listener: SignalingClientListener
    ) : WebSocketListener() {

    var webSocket: WebSocket? = null
    var messageHandler: ((ClientMessage) -> Unit)? = null

    override fun onOpen(webSocket: WebSocket, response: Response) {
        this.webSocket = webSocket
        listener.onConnectionEstablished()
    }


    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.e(TAG, "WebSocket: Got message $text")
        try {
            val json = JSONObject(text)
            val type = json.getString("type")
            val clientMessage =
                when (type) {
                    MessageType.SDPMessage.value ->
                        SDPMessage(json.getString("sdp"))
                    MessageType.ICEMessage.value ->
                        ICEMessage(
                            json.getInt("label"),
                            json.getString("id"),
                            json.getString("candidate")
                        )
                    MessageType.MatchMessage.value ->
                        MatchMessage(json.getString("match"), json.getBoolean("offer"))
                    MessageType.PeerLeft.value ->
                        PeerLeft()
                    else ->
                        null
                }

            if (clientMessage != null) this.messageHandler?.invoke(clientMessage)
        }
        catch (e:Exception)
        {
            Log.e(TAG, "WebSocket: Message Parsing issue")
        }
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

    private fun send(clientMessage: ClientMessage) {
        val json = JSONObject()
        json.put("type", clientMessage.type)
        when (clientMessage) {
            is SDPMessage -> {
                json.put("sdp", clientMessage.sdp)
            }
            is ICEMessage -> {
                json.put("candidate", clientMessage.candidate)
                json.put("id", clientMessage.id)
                json.put("label", clientMessage.label)
            }
            else -> {
                Log.e(
                    TAG,
                    "Message of type '${clientMessage.type.value}' can't be sent to the server"
                )
                return
            }
        }
        Log.e(TAG, "WebSocket: Sending $json")
        webSocket!!.send(json.toString())
    }

    fun close() {
        webSocket?.close(1000, null)
    }

    fun sendSDP(sdp: String) {
        send(SDPMessage(sdp))
    }

    fun sendCandidate(label: Int, id: String, candidate: String) {
        send(ICEMessage(label, id, candidate))
    }

    companion object {
        private val TAG = "SignalingWebSocket"
    }
}

