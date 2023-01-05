package com.example.afjtracking.websocket.model

import java.io.Serializable

enum class MessageType(val value: String) {


    //From App to Web
    StartCall("is-client-ready"),
    JoinCall("join-call"),
    CreateOffer("store-offer"),
    AnswerCall("send-answer"),
    SendIceCandidate("store-candidate"),
    SendCandidate("send-candidate"),
    RejectCall("offer-reject"),
    CallEnd("end-call"),

    //From Web to App

    CallResponse("client-status"),
    IncomingCall("incoming-call"),
    AnswerReceived("answer-received"),
    OfferReceived("offer-received"),
    CallReject("call-reject"),
    CallClosed("end-call"),
    ICECandidate("candidate"),
    CallAlreadyAnswered("call-already-answered");


    override fun toString() = value
}


data class MessageModel(
    val type: String,
    val sendFrom: String? = null,
    val sendTo: String? = null,
    val data: Any = 0,
    val callerName: String? = null,
    val offer_connection_id:String?=null,
    val callType: String? = "video" //audio,video
):Serializable


data class IceCandidateModel(
    val sdpMid: String,
    val sdpMLineIndex: Double,
    val candidate: String
)


data class WebSocketMessage(
    val type: String,
    val name: String? = null,
    val sendTo: String? = null,
    val data: Any? = null
)


