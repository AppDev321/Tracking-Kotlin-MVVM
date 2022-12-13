package com.example.afjtracking.websocket


import android.content.Context
import android.util.Base64
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.websocket.model.MessageModel
import com.example.afjtracking.websocket.model.MessageType
import org.webrtc.*
import java.time.Instant.now
import java.time.LocalDate.now
import java.time.LocalDateTime
import java.time.LocalTime.now
import java.util.*
import java.util.Base64.getEncoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


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


    /*  private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:iphone-stun.strato-iphone.de:3478")
            .createIceServer(),
        PeerConnection.IceServer("stun:openrelay.metered.ca:80"),
        PeerConnection.IceServer(
            "stun:openrelay.metered.ca:443"
        ),
        PeerConnection.IceServer(
            "stun:stun.l.google.com:19302"
        ),

        )
*/
    var secret = "8f60119eb0b51b511bf4b0fb7838e36c"
    private fun hmacSha1( value:String,  key:String):ByteArray
    {

        val signingKey = SecretKeySpec(key.toByteArray(), "HmacSHA1")
        val mac: Mac = Mac.getInstance("HmacSHA1")
        mac.init(signingKey)
        val rawHmac = mac.doFinal(value.toByteArray())
        val hexBytes: ByteArray = Base64.encode(rawHmac,Base64.DEFAULT)
           return hexBytes
    }

   // private var uuID= UUID.randomUUID().toString()
    var uuID = ((System.currentTimeMillis() / 1000) + (12 * 3600)).toString()
    private val iceServer = listOf(

//1670963170
        PeerConnection.IceServer.builder("stun:vmi808920.contaboserver.net:3479")
            .createIceServer(),

        PeerConnection
            .IceServer
            .builder("turn:vmi808920.contaboserver.net:3479")
            .setUsername(uuID)
            .setPassword( String(hmacSha1(uuID,secret), Charsets.UTF_8))
            .createIceServer(),

        )






    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    private val videoCapturer by lazy { getVideoCapture(context) }

    private val audioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val peerConnection by lazy {
        buildPeerConnection(observer)
    }

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
                disableEncryption = false
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
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            /*  mandatory.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
              mandatory.add(MediaConstraints.KeyValuePair("RtpDataChannels", "true"))*/
        }

        createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                    }

                    override fun onSetSuccess() {
                        val offer = hashMapOf(
                            "sdp" to desc?.description,
                            "type" to desc?.type.toString().lowercase()
                        )
                        val createOffer =
                            MessageModel(
                                MessageType.CreateOffer.value,
                                currentUserId,
                                targetId,
                                offer
                            )
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
        targetId: String, currentUserId: String, offerConnectionID: String
    ) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            /* mandatory.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
             mandatory.add(MediaConstraints.KeyValuePair("RtpDataChannels", "true"))*/
        }
        createAnswer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {

                        AFJUtils.writeLogs("set answer failure : ${p0}")
                    }

                    override fun onSetSuccess() {
                        val answer = hashMapOf(
                            "sdp" to desc?.description,
                            "type" to desc?.type.toString().lowercase()
                        )
                        val answerCall = MessageModel(
                            type = MessageType.AnswerCall.value,
                            sendFrom = currentUserId,
                            sendTo = targetId,
                            data = answer,
                            offer_connection_id = offerConnectionID

                        )

                        //Notifiy to WebSocket to answer call
                        signalingClient.sendMessageToWebSocket(answerCall)


                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        AFJUtils.writeLogs("create answer successss : ${p0}")
                    }

                    override fun onCreateFailure(p0: String?) {
                        AFJUtils.writeLogs("create answer failure 111 : ${p0}")
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }

            override fun onCreateFailure(p0: String?) {
                AFJUtils.writeLogs("create answer failure : ${p0}")
            }
        }, constraints)
    }

    fun call(sdpObserver: SdpObserver, targetId: String, currentUserId: String) =
        peerConnection?.call(sdpObserver, targetId, currentUserId)

    fun answer(sdpObserver: SdpObserver, targetId: String, currentUserId: String,offerConnectionID: String) =
        peerConnection?.answer(sdpObserver, targetId, currentUserId,offerConnectionID)

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        remoteSessionDescription = sessionDescription
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetFailure(p0: String?) {
                AFJUtils.writeLogs("create remotedescription failure 111 : ${p0}")
            }

            override fun onSetSuccess() {
                AFJUtils.writeLogs("create remotedescription sucess")
            }

            override fun onCreateSuccess(p0: SessionDescription?) {
                AFJUtils.writeLogs("create remotedescription created : ${p0}")
            }

            override fun onCreateFailure(p0: String?) {
                AFJUtils.writeLogs("create remotedescription failure  : ${p0}")
            }
        }, sessionDescription)
    }

    fun addIceCandidate(iceCandidate: IceCandidate?) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun endCall(currentUserId: String, targetId: String) {
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