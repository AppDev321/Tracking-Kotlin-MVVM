package com.example.afjtracking.websocket.model

import java.io.Serializable

enum class MessageType(val value: String) {


    //From App to Web
    StartCall("is-client-ready"),
    CreateOffer("store_offer"),
    AnswerCall("send_answer"),
    SendIceCandidate("store_candidate"),
    RejectCall("offer_reject"),
    CallEnd("call_end"),

    //From Web to App

    CallResponse("call_response"),
    AnswerReceived("answer"),
    OfferReceived("offer_received"),
    CallReject("call_reject"),
    CallClosed("call_closed"),
    ICECandidate("ice_receive_candidate");


    override fun toString() = value
}


data class MessageModel(
    val type: String,
    val sendFrom: String? = null,
    val sendTo: String? = null,
    val data: Any = 0,
    val callerName: String? = null,
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


