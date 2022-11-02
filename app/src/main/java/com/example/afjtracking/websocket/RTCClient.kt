package com.example.afjtracking.websocket


import android.content.Context


import com.example.afjtracking.websocket.model.MessageModel
import com.example.afjtracking.websocket.model.MessageType

import org.webrtc.*


class RTCClient(
    context: Context,
    val signalingClient: SignalingClient,
    observer: PeerConnection.Observer,

    ) {

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
    }

    private val rootEglBase: EglBase = EglBase.create()

    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null
    val TAG = "RTCClient"

    var remoteSessionDescription: SessionDescription? = null


    init {
        initPeerConnectionFactory(context)
    }

  /*  private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer()
    )*/

    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:iphone-stun.strato-iphone.de:3478").createIceServer(),
        PeerConnection.IceServer("stun:openrelay.metered.ca:80"),
        PeerConnection.IceServer("turn:openrelay.metered.ca:80","openrelayproject","openrelayproject"),
        PeerConnection.IceServer("turn:openrelay.metered.ca:443","openrelayproject","openrelayproject"),
        PeerConnection.IceServer("turn:openrelay.metered.ca:443?transport=tcp","openrelayproject","openrelayproject"),

        )

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    private val videoCapturer by lazy { getVideoCapture(context) }

    private val audioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val peerConnection by lazy { buildPeerConnection(observer) }

    private fun initPeerConnectionFactory(context: Context) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory
            .builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    rootEglBase.eglBaseContext,
                    true,
                    true
                )
            )
            .setOptions(PeerConnectionFactory.Options().apply {
                disableEncryption = true
                disableNetworkMonitor = true
            })
            .createPeerConnectionFactory()
    }

    private fun buildPeerConnection(observer: PeerConnection.Observer) =
        peerConnectionFactory.createPeerConnection(
            iceServer,
            observer
        )

    private fun getVideoCapture(context: Context) =
        Camera2Enumerator(context).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }

    fun initSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(true)
        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    fun startLocalVideoCapture(localVideoOutput: SurfaceViewRenderer) {
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
        (videoCapturer as VideoCapturer).initialize(
            surfaceTextureHelper,
            localVideoOutput.context,
            localVideoSource.capturerObserver
        )
        videoCapturer.startCapture(320, 240, 60)
        localAudioTrack =
            peerConnectionFactory.createAudioTrack(LOCAL_TRACK_ID + "_audio", audioSource)
        localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)
        localVideoTrack?.addSink(localVideoOutput)
        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)
        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)
        peerConnection?.addStream(localStream)
    }

    private fun PeerConnection.call(
        sdpObserver: SdpObserver,
        targetId: String,
        currentUserId: String
    ) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                    }

                    override fun onSetSuccess() {
                        val offer = hashMapOf(
                            "sdp" to desc?.description,
                            "type" to desc?.type
                        )
                        val createOffer =
                            MessageModel(MessageType.CreateOffer.value, currentUserId, targetId, offer)
                        signalingClient.sendMessageToWebSocket(createOffer)


                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {

                    }

                    override fun onCreateFailure(p0: String?) {

                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }

            override fun onSetFailure(p0: String?) {

            }

            override fun onCreateFailure(p0: String?) {

            }
        }, constraints)
    }

    private fun PeerConnection.answer(
        sdpObserver: SdpObserver,
        targetId: String, currentUserId: String
    ) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        createAnswer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                    }
                    override fun onSetSuccess() {
                        val answer = hashMapOf(
                            "sdp" to desc?.description,
                            "type" to desc?.type
                        )
                        val answerCall = MessageModel(MessageType.AnswerCall.value, currentUserId, targetId, answer)

                        //Notifiy to WebSocket to answer call
                        signalingClient.sendMessageToWebSocket(answerCall)


                    }
                    override fun onCreateSuccess(p0: SessionDescription?) {
                    }
                    override fun onCreateFailure(p0: String?) {
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }
            override fun onCreateFailure(p0: String?) {
            }
        }, constraints)
    }

    fun call(sdpObserver: SdpObserver, targetId: String, currentUserId: String) =
        peerConnection?.call(sdpObserver, targetId, currentUserId)

    fun answer(sdpObserver: SdpObserver, targetId: String, currentUserId: String) =
        peerConnection?.answer(sdpObserver, targetId, currentUserId)

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        remoteSessionDescription = sessionDescription
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetFailure(p0: String?) {
            }
            override fun onSetSuccess() {
            }
            override fun onCreateSuccess(p0: SessionDescription?) {
            }
            override fun onCreateFailure(p0: String?) {
            }
        }, sessionDescription)
    }

    fun addIceCandidate(iceCandidate: IceCandidate?) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun endCall(currentUserId: String,targetId: String) {
      //peerConnection?.removeIceCandidates(iceCandidateArray.toTypedArray())
        val answerCall = MessageModel(MessageType.CallEnd.value, currentUserId, targetId, 0)
        signalingClient.sendMessageToWebSocket(answerCall)
       peerConnection?.close()
    }

    fun callClosed() {
        peerConnection?.close()
    }

    fun enableVideo(videoEnabled: Boolean) {
        if (localVideoTrack != null)
            localVideoTrack?.setEnabled(videoEnabled)
    }

    fun enableAudio(audioEnabled: Boolean) {
        if (localAudioTrack != null)
            localAudioTrack?.setEnabled(audioEnabled)
    }

    fun switchCamera() {
        videoCapturer.switchCamera(null)
    }
}