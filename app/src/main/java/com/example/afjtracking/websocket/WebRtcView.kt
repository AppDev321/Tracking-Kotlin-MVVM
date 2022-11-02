package com.example.afjtracking.websocket

import android.app.Activity
import android.view.SurfaceView
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.isGone
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.utils.CustomDialog
import com.example.afjtracking.websocket.listners.AppSdpObserver
import com.example.afjtracking.websocket.listners.PeerConnectionObserver
import com.example.afjtracking.websocket.listners.RTCViewListener
import com.example.afjtracking.websocket.listners.SignalingClientListener
import com.example.afjtracking.websocket.model.IceCandidateModel
import com.example.afjtracking.websocket.model.MessageModel
import com.example.afjtracking.websocket.model.MessageType
import com.example.afjtracking.websocket.utils.RTCAudioManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer


class WebRtcView(
    private val currentUserId: String,
    private val targetUserId: String,
    private val context: Activity,
    private val webSocketURL: String,
    private val switch_camera_button: ImageView,
    private val audio_output_button: ImageView,
    private val video_button: ImageView,
    private val mic_button: ImageView,
    private val end_call_button: ImageView,
    private val localView: SurfaceView,
    private val remoteView: SurfaceView,
    private val remote_view_loading: ProgressBar,
    private val listener: RTCViewListener,
) {


    private lateinit var rtcClient: RTCClient
    private lateinit var signallingClient: SignalingClient
    private val audioManager by lazy { RTCAudioManager.create(context) }

    private var isMute = false
    private var isLocalVideoEnable = true
    private var inSpeakerMode = true
    private val local_view: SurfaceViewRenderer = localView as SurfaceViewRenderer
    private val remote_view: SurfaceViewRenderer = remoteView as SurfaceViewRenderer
    private val sdpObserver = object : AppSdpObserver() {

    }

    init {
        initializeRTCClient()
        audioManager.selectAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
        switch_camera_button.setOnClickListener {
            rtcClient.switchCamera()
        }

        audio_output_button.setOnClickListener {
            inSpeakerMode = !inSpeakerMode
            if (inSpeakerMode) {
                audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
            } else {
                audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
            }
            listener.onSpeakerClick(inSpeakerMode)
        }

        video_button.setOnClickListener {
            isLocalVideoEnable = !isLocalVideoEnable
            rtcClient.enableVideo(isLocalVideoEnable)
            listener.onVideoCameraClick(isLocalVideoEnable)
        }


        mic_button.setOnClickListener {
            isMute = !isMute
            rtcClient.enableAudio(isMute)
            listener.onMicClick(isMute)

            rtcClient.call(sdpObserver, targetUserId, currentUserId)
        }
        end_call_button.setOnClickListener {
            rtcClient.endCall(currentUserId, targetUserId)
            remote_view.isGone = false
            Constants.isCallEnded = true
            listener.onEndCall()

        }
    }


    private fun initializeRTCClient() {

        signallingClient = SignalingClient(
            webSocketURL,
            createSignallingClientListener()
        )

        rtcClient = RTCClient(
            context,
            signallingClient,
            object : PeerConnectionObserver() {
                override fun onIceCandidate(p0: IceCandidate?) {
                    super.onIceCandidate(p0)
                    rtcClient.addIceCandidate(p0)

                    val candidate = hashMapOf(
                        "sdpMid" to p0?.sdpMid,
                        "sdpMLineIndex" to p0?.sdpMLineIndex,
                        "sdpCandidate" to p0?.sdp
                    )

                    val iceCandidate = MessageModel(
                        MessageType.SendIceCandidate.value,
                        currentUserId,
                        targetUserId,
                        candidate
                    )
                    signallingClient.sendMessageToWebSocket(iceCandidate)

                }

                override fun onAddStream(p0: MediaStream?) {
                    super.onAddStream(p0)
                    p0?.videoTracks?.get(0)?.addSink(remote_view)
                }

            }
        )

        rtcClient.initSurfaceView(remote_view)
        rtcClient.initSurfaceView(local_view)
        rtcClient.startLocalVideoCapture(local_view)

    }

    private fun createSignallingClientListener() = object : SignalingClientListener {
        override fun onConnectionEstablished() {
            end_call_button.isClickable = true
            AFJUtils.writeLogs("Connection Established")

            val message = MessageModel(
                type = MessageType.StartCall.value,
                name = currentUserId,
                sendTo = targetUserId,
            )
            signallingClient.sendMessageToWebSocket(message)

        }

        override fun onNewMessageReceived(messageModel: MessageModel) {
            //  AFJUtils.writeLogs("Got New Message= $messageModel")


            try {
                when (messageModel.type) {
                    MessageType.CallResponse.value -> {
                        //Check if target user is active send offer him
                        if (messageModel.data == true) {
                            // rtcClient.call(sdpObserver, targetUserId,currentUserId)
                        } else {
                            showToastMessage("${messageModel.callerName} is currently offline")
                        }

                    }

                    MessageType.OfferReceived.value -> {
                        context.runOnUiThread {
                            CustomDialog().showIncomingCallDialog(
                                context,
                                messageModel.callerName.toString(),
                                positiveListener = {
                                    val session = SessionDescription(
                                        SessionDescription.Type.OFFER,
                                        messageModel.data.toString()
                                    )
                                    rtcClient.onRemoteSessionReceived(session)
                                    Constants.isIntiatedNow = false
                                    rtcClient.answer(sdpObserver, targetUserId, currentUserId)
                                    remote_view_loading.isGone = true
                                },
                                negativeListener = {
                                    val rejectOffer =
                                        MessageModel(
                                            MessageType.RejectCall.value,
                                            currentUserId,
                                            targetUserId,
                                            0
                                        )
                                    signallingClient.sendMessageToWebSocket(rejectOffer)
                                    //listener.onEndCall()
                                }
                            )
                        }
                    }

                    MessageType.CallReject.value -> {

                        showToastMessage("${messageModel.data}")
                    }
                    MessageType.CallClosed.value -> {

                        showToastMessage("${messageModel.data}")
                        rtcClient.callClosed()
                        listener.onEndCall()

                    }
                    MessageType.AnswerReceived.value -> {

                        val session = SessionDescription(
                            SessionDescription.Type.ANSWER,
                            messageModel.data.toString()
                        )
                        rtcClient.onRemoteSessionReceived(session)
                        Constants.isIntiatedNow = false
                        context.runOnUiThread {
                            remote_view_loading.isGone = true
                        }

                    }
                    MessageType.ICECandidate.value -> {

                        try {
                            val gson = Gson()
                            val receivingCandidate = gson.fromJson(
                                gson.toJson(messageModel.data),
                                IceCandidateModel::class.java
                            )
                            rtcClient.addIceCandidate(
                                IceCandidate(
                                    receivingCandidate.sdpMid,
                                    Math.toIntExact(receivingCandidate.sdpMLineIndex.toLong()),
                                    receivingCandidate.sdpCandidate
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                    else -> {

                    }
                }
            } catch (e: Exception) {
                AFJUtils.writeLogs("WebSocket: Message Parsing issue $e")
            }
        }


        override fun onCallEnded() {
            if (!Constants.isCallEnded) {
                Constants.isCallEnded = true
                rtcClient.endCall(currentUserId, targetUserId)
                listener.onEndCall()

            }
        }
    }


    fun onDestroy() {
        signallingClient.destroy()
        rtcClient.endCall(currentUserId, targetUserId)

    }

    fun showToastMessage(msg: String) {
        context.runOnUiThread {
            Snackbar.make(
                mic_button,
                msg,
                Snackbar.LENGTH_SHORT
            ).show()
        }


    }


}