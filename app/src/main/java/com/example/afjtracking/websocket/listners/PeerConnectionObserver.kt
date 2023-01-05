package com.example.afjtracking.websocket.listners

import com.example.afjtracking.utils.AFJUtils
import org.webrtc.*


open class PeerConnectionObserver : PeerConnection.Observer {
    override fun onIceCandidate(p0: IceCandidate?) {
    }

    override fun onDataChannel(p0: DataChannel?) {
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        AFJUtils.writeLogs("onIceConnectionReceivingChange =$p0")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        AFJUtils.writeLogs("onIceConnectionChange =$p0")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        AFJUtils.writeLogs("onIceGatheringChange =$p0")
    }

    override fun onAddStream(p0: MediaStream?) {

    }

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        AFJUtils.writeLogs("onSignalingChange =$p0")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
    }

    override fun onRemoveStream(p0: MediaStream?) {
    }

    override fun onRenegotiationNeeded() {

    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
    }
}