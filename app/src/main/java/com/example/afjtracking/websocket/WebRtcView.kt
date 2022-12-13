package com.example.afjtracking.websocket

import android.app.Activity
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.SurfaceView
import android.view.View
import android.widget.*
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.utils.CustomDialog
import com.example.afjtracking.websocket.listners.AppSdpObserver
import com.example.afjtracking.websocket.listners.PeerConnectionObserver
import com.example.afjtracking.websocket.listners.RTCViewListener
import com.example.afjtracking.websocket.listners.SocketMessageListener
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
    private val context: Activity,
    private val currentUserId: String,
    private val targetUserId: String,
    private val incomingCallMessageModel: MessageModel?,
    private val webSocketURL: String,
    private val switch_camera_button: ImageView,
    private val audio_output_button: ImageView,
    private val video_button: ImageView,
    private val mic_button: ImageView,
    private val end_call_button: ImageView,
    private val localView: SurfaceView,
    private val remoteView: SurfaceView,
    private val remote_view_loading: ProgressBar,
    private val containerProgressBar: LinearLayout,
    private val txtCallingStatus: TextView,
    private val containerTopRemoteView: View,
    private val listener: RTCViewListener

) {


    private lateinit var rtcClient: RTCClient
    private lateinit var signallingClient: SignalingClient
    private val audioManager by lazy { RTCAudioManager.create(context) }

    private var isMute = false
    private var isLocalVideoEnable = true
    private var inEarMode = false
    private val local_view: SurfaceViewRenderer = localView as SurfaceViewRenderer
    private val remote_view: SurfaceViewRenderer = remoteView as SurfaceViewRenderer
    private var toneGenerator: ToneGenerator? = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60)

    private val sdpObserver = object : AppSdpObserver() {

    }

    init {
        initializeRTCClient()
        audioManager.selectAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
        switch_camera_button.setOnClickListener {
            rtcClient.switchCamera()
        }

        audio_output_button.setOnClickListener {
            inEarMode = !inEarMode
            if (inEarMode) {
                audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
            } else {
                audioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
            }
            listener.onSpeakerClick(inEarMode)
        }

        video_button.setOnClickListener {
            isLocalVideoEnable = !isLocalVideoEnable
            rtcClient.enableVideo(isLocalVideoEnable)
            listener.onVideoCameraClick(isLocalVideoEnable)
        }


        mic_button.setOnClickListener {
            isMute = !isMute
            rtcClient.enableAudio(!isMute)
            listener.onMicClick(isMute)


        }
        end_call_button.setOnClickListener {
            // rtcClient.endCall(currentUserId, targetUserId)
            //   remote_view.visibility =View.GONE
            rtcClient.endCall(currentUserId, targetUserId)
            Constants.isCallEnded = true
            listener.onEndCall()

        }
    }


    private fun initializeRTCClient() {
        signallingClient = SignalingClient.getInstance(
            createSignallingClientListener(),
            webSocketURL
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
                        "candidate" to p0?.sdp
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
                    stopDTMFTone()
                    context.runOnUiThread {
                        containerProgressBar.visibility = View.GONE
                        containerTopRemoteView.visibility = View.GONE
                    }

                    p0?.videoTracks?.get(0)?.addSink(remote_view)
                }

            }
        )

        rtcClient.initSurfaceView(remote_view)
        rtcClient.initSurfaceView(local_view)
        rtcClient.startLocalVideoCapture(local_view)


        //*******************************Incoming Calling Object Checking *********************************////////////////////////////

        if (incomingCallMessageModel != null) {
            if (incomingCallMessageModel.type.contains(MessageType.OfferReceived.value)) {
                val customSessionClass = Gson().fromJson(
                    Gson().toJson(incomingCallMessageModel.data).toString(),
                    CustomSessionClass::class.java
                )
                val session = SessionDescription(
                    SessionDescription.Type.OFFER,
                    customSessionClass.sdp
                )

                rtcClient.onRemoteSessionReceived(session)
                Constants.isIntiatedNow = false
                rtcClient.answer(
                    sdpObserver,
                    targetUserId,
                    currentUserId,
                    incomingCallMessageModel.offer_connection_id.toString()
                )
                context.runOnUiThread {
                    containerTopRemoteView.visibility = View.VISIBLE
                    containerProgressBar.visibility = View.VISIBLE
                }

            }
        } else {
            end_call_button.isClickable = true
            context.runOnUiThread {
                txtCallingStatus.text = "Calling"
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE)
                containerProgressBar.visibility = View.VISIBLE
            }
            val message = MessageModel(
                type = MessageType.StartCall.value,
                sendFrom = currentUserId,
                sendTo = targetUserId,
            )
            signallingClient.sendMessageToWebSocket(message)


        }
//****************************************************************////////////////////////////


    }

    private fun createSignallingClientListener() = object : SocketMessageListener() {
        override fun onWebSocketFailure(errorMessage: String) {
            super.onWebSocketFailure(errorMessage)
            context.runOnUiThread{
                Toast.makeText(context,"Socket Connection Issue: $errorMessage",Toast.LENGTH_LONG).show()
            }

        }
        override fun onConnectionEstablished() {
        }

        override fun onNewMessageReceived(messageModel: MessageModel) {

            AFJUtils.writeLogs("Got New Message WebRTCView = $messageModel")



            try {
                when (messageModel.type) {
                    MessageType.CallResponse.value -> {
                        //Check if target user is active send offer him

                        val userCallStatus = Gson().fromJson(
                            Gson().toJson(messageModel.data).toString(),
                            UserCallingStatus::class.java
                        )


                        if (userCallStatus.is_online) {
                            if (userCallStatus.is_busy) {
                                /*showToastMessage("User is busy on another call")
                                context.runOnUiThread {
                                    txtCallingStatus.text = "User is on another call"
                                }*/
                                listener.showDialogMessage("User is busy on another call")
                                Constants.isCallEnded = true
                                //  listener.onEndCall()
                                onDestroy()


                            } else {
                                context.runOnUiThread {
                                    txtCallingStatus.text = "Ringing"
                                }

                                rtcClient.call(sdpObserver, targetUserId, currentUserId)
                            }
                        } else {

                            listener.showDialogMessage("User is not available")
                            Constants.isCallEnded = true
                            //listener.onEndCall()
                            onDestroy()
                        }
                    }
                    MessageType.OfferReceived.value -> {

                        context.runOnUiThread {
                            val customSessionClass = Gson().fromJson(
                                Gson().toJson(messageModel.data).toString(),
                                CustomSessionClass::class.java
                            )
                            CustomDialog().showIncomingCallDialog(
                                context,
                                messageModel.callerName.toString(),
                                positiveListener = {
                                    val session = SessionDescription(
                                        SessionDescription.Type.OFFER,
                                        customSessionClass.sdp

                                    )
                                    rtcClient.onRemoteSessionReceived(session)
                                    Constants.isIntiatedNow = false
                                    rtcClient.answer(
                                        sdpObserver,
                                        targetUserId,
                                        currentUserId,
                                        messageModel.offer_connection_id.toString()
                                    )


                                    context.runOnUiThread {
                                        containerProgressBar.visibility = View.GONE
                                    }

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
                        stopDTMFTone()
                        showToastMessage("${messageModel.data}")
                    }
                    MessageType.CallClosed.value -> {
                        showToastMessage("${messageModel.data}")
                        rtcClient.callClosed()
                        listener.onEndCall()

                    }
                    MessageType.AnswerReceived.value -> {

                        val customSessionClass = Gson().fromJson(
                            Gson().toJson(messageModel.data).toString(),
                            CustomSessionClass::class.java
                        )
                        val session = SessionDescription(
                            SessionDescription.Type.ANSWER,
                            customSessionClass.sdp
                        )

                        rtcClient.onRemoteSessionReceived(session)
                        Constants.isIntiatedNow = false
                        context.runOnUiThread {
                            containerProgressBar.visibility = View.GONE
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
                                    receivingCandidate.candidate
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
                AFJUtils.writeLogs("WebSocket: Message Parsing issue WebRTCView== $e")
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
        rtcClient.endCall(currentUserId, targetUserId)
        stopDTMFTone()
    }

    fun stopDTMFTone() {
        if (toneGenerator != null) {
            toneGenerator?.stopTone()
            toneGenerator?.release()
            toneGenerator = null
        }
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

data class CustomSessionClass(val type: String, val sdp: String)
data class UserCallingStatus(val is_online: Boolean, val is_busy: Boolean)
