package com.afjltd.tracking.websocket.listners

import com.afjltd.tracking.websocket.model.MessageModel


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