package com.example.afjtracking.websocket.listners

import com.example.afjtracking.websocket.model.MessageModel
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription


interface SignalingClientListener {
    fun onConnectionEstablished()
    fun onNewMessageReceived(message:MessageModel)
    fun onCallEnded()

}