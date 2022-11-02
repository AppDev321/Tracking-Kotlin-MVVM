package com.example.afjtracking.websocket.model

enum class MessageType(val value: String) {


    //From App to Web
    StartCall("is-client-ready"),
    CreateOffer("create_offer"),
    AnswerCall("create_answer"),
    SendIceCandidate("send_ice_candidate"),
    RejectCall("offer_reject"),
    CallEnd("call_end"),

    //From Web to App

    CallResponse("call_response"),
    AnswerReceived("answer_received"),
    OfferReceived("offer_received"),
    CallReject("call_reject"),
    CallClosed("call_closed"),
    ICECandidate("ice_candidate_received");



    override fun toString() = value
}


data class MessageModel(
    val type: String,
    val name: String? = null,
    val sendTo: String? = null,
    val data: Any = 0,
    val callerName :String?=null,
)



data class IceCandidateModel(
    val sdpMid: String,
    val sdpMLineIndex: Double,
    val sdpCandidate: String
)



data class WebSocketMessage(
    val type: String,
    val name: String? = null,
    val sendTo: String? = null,
    val data: Any? = null
)
