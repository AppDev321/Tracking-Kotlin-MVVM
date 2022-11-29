package com.example.afjtracking.websocket.listners

import com.example.afjtracking.websocket.model.MessageModel
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription


interface SignalingClientListener {
    fun onConnectionEstablished()
    fun onNewMessageReceived(message:MessageModel)
    fun onCallEnded()
    fun onConnectionClosed()
    fun onWebSocketFailure(errorMessage :String)

}

open class SocketMessageListener: SignalingClientListener {
    override fun onConnectionEstablished() {
    }
    override fun onNewMessageReceived(message: MessageModel) {
    }
    override fun onCallEnded() {
    }
    override fun onConnectionClosed(){}
    override fun onWebSocketFailure(errorMessage :String) {

    }

}