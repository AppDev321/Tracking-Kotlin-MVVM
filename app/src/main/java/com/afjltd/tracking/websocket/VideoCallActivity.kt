package com.afjltd.tracking.websocket

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.afjltd.tracking.model.responses.QRFirebaseUser
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.Constants
import com.afjltd.tracking.utils.CustomDialog
import com.afjltd.tracking.websocket.listners.RTCViewListener
import com.afjltd.tracking.websocket.model.MessageModel
import com.afjltd.tracking.R
import com.permissionx.guolindev.PermissionX
import org.webrtc.SurfaceViewRenderer


class VideoCallActivity : AppCompatActivity() {


    private lateinit var switch_camera_button: ImageView
    private lateinit var audio_output_button: ImageView
    private lateinit var video_button: ImageView
    private lateinit var mic_button: ImageView
    private lateinit var end_call_button: ImageView
    private lateinit var local_view: SurfaceViewRenderer
    private lateinit var remote_view: SurfaceViewRenderer
    private lateinit var remote_view_loading: ProgressBar
    private lateinit var container_progress_bar:LinearLayout
    private lateinit var txt_calling_status:TextView
    private lateinit var container_remote_top: View
    private lateinit var rtcView: WebRtcView
    private lateinit var targetUserId: String
    private lateinit var currentUserId: String


    var messageIntent: MessageModel? = null


    companion object {
        const val messageIntentValue = "message_model_intent"
        const val currentUserID = "currentUser"
         var targetUserID = "targetUserId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        targetUserId = intent.extras?.getString(targetUserID).toString()
        currentUserId = intent.extras?.getString(currentUserID).toString()



        if (intent.extras?.getSerializable(messageIntentValue) != null) {
            messageIntent = intent.extras?.getSerializable(messageIntentValue) as MessageModel
            targetUserID = messageIntent?.sendFrom.toString()
        }

        switch_camera_button = findViewById(R.id.switch_camera_button)
        audio_output_button = findViewById(R.id.audio_output_button)
        video_button = findViewById(R.id.video_button)
        mic_button = findViewById(R.id.mic_button)
        end_call_button = findViewById(R.id.end_call_button)
        local_view = findViewById(R.id.local_view)
        remote_view = findViewById(R.id.remote_view)
        remote_view_loading = findViewById(R.id.remote_view_loading)
        container_progress_bar = findViewById(R.id.container_progress_bar)
        txt_calling_status = findViewById(R.id.txt_calling_status)
        container_remote_top =findViewById(R.id.container_remote_top)



        PermissionX.init(this)
            .permissions(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            ).request { allGranted, _, _ ->
                if (allGranted) {
                    createVideoCallView()
                } else {
                    Toast.makeText(this, "you should accept all permissions", Toast.LENGTH_LONG)
                        .show()
                }
            }



    }

    private fun createVideoCallView() {

        var url =
            Constants.WEBSOCKET_URL + AFJUtils.getDeviceDetail().deviceID +"&device=${Constants.WEBSOCKET_APP_NAME}"

        rtcView = WebRtcView(
            this,
            currentUserId,
            targetUserId,
            messageIntent,
            url,
            switch_camera_button,
            audio_output_button,
            video_button,
            mic_button,
            end_call_button,
            local_view,
            remote_view,
            remote_view_loading,
            container_progress_bar,
            txt_calling_status,
            container_remote_top,
            object : RTCViewListener {
                override fun onEndCall() {
                    finish()
                }

                override fun onMicClick(isMuted: Boolean) {
                    if (!isMuted) {
                        mic_button.setImageResource(R.drawable.ic_baseline_mic_24)
                    } else {
                        mic_button.setImageResource(R.drawable.ic_baseline_mic_off_24)
                    }
                }

                override fun onVideoCameraClick(isEnableLocalView: Boolean) {
                    if (isEnableLocalView) {
                        video_button.setImageResource(R.drawable.ic_baseline_videocam_24)
                    } else {
                        video_button.setImageResource(R.drawable.ic_baseline_videocam_off_24)
                    }

                }

                override fun onSpeakerClick(isEarPhone: Boolean) {
                    if (isEarPhone) {
                        audio_output_button.setImageResource(R.drawable.ic_baseline_hearing_24)
                    } else {
                        audio_output_button.setImageResource(R.drawable.ic_baseline_speaker_up_24)
                    }
                }

                override fun showDialogMessage(msg: String) {
                    runOnUiThread{
                    CustomDialog().showSimpleAlertMsg(
                        context = this@VideoCallActivity,
                        message = msg,
                        textPositive = "Close",
                    positiveListener = {
                        finish()
                    })
                }
                }
            }

        )
    }


    override fun onDestroy() {

        rtcView.onDestroy()
        super.onDestroy()
    }
}