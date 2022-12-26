package com.hnh.webrtc_calling_lib.model


enum class MessageType(val value: String)
{
    SDPMessage("sdp"),
    ICEMessage("ice"),
    MatchMessage("match"),
    PeerLeft("peer-left");

    override fun toString() = value
}

open class ClientMessage(val type: MessageType)

data class SDPMessage(val sdp: String) : ClientMessage(MessageType.SDPMessage)
data class ICEMessage(val label: Int, val id: String, val candidate: String) : ClientMessage(MessageType.ICEMessage)
data class MatchMessage(val match: String, val offer: Boolean) : ClientMessage(MessageType.MatchMessage)
class PeerLeft : ClientMessage(MessageType.PeerLeft)
