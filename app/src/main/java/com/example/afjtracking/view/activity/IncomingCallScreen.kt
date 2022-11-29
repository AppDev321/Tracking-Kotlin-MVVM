package com.example.afjtracking.view.activity

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.example.afjtracking.R
import com.example.afjtracking.websocket.VideoCallActivity
import com.example.afjtracking.websocket.model.MessageModel


class IncomingCallScreen : BaseActivity() {

    companion object{
        const val intentData = "message_data"
    }

    var messageModel = MessageModel("test")

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    //   turnOnScreen()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }


        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val mp = MediaPlayer.create(this@IncomingCallScreen, notification)
        mp.start()
        if(intent.extras != null)
        {
            messageModel = intent.extras?.getSerializable(intentData) as MessageModel


        }
     showIncomingCallDialog(
                this@IncomingCallScreen,
                messageModel.callerName?: "Test Caller",
                positiveListener = {
                    mp.stop()
                    finish()

                    if(messageModel.data != null) {

                        val currentUserId = messageModel.sendTo
                        val targetUserID = messageModel.sendFrom
                        val intent = Intent(this, VideoCallActivity::class.java).apply {
                            putExtra(VideoCallActivity.currentUserID, "" + currentUserId)
                            putExtra(VideoCallActivity.targetUserID, "" + targetUserID)
                            putExtra(VideoCallActivity.messageIntentValue, messageModel)
                        }
                        startActivity(intent)
                    }
                    else
                    {
                        Toast.makeText(this,"No further action",Toast.LENGTH_LONG).show()
                    }

                },
                negativeListener = {
                    mp.stop()
                    finish()
                }
            )







    }

    fun showIncomingCallDialog(
        context: Activity?,
        title: String,
        positiveListener: (() -> Unit)? = null,
        negativeListener: (() -> Unit)? = null,
        canceledOnTouchOutside: Boolean = false
    ): AlertDialog? {
        if (context == null) return null

        lateinit var buttonAnswer: Button
        lateinit var buttonDecline: Button
        return AlertDialog.Builder(context).apply {

        }.create().apply {
            setCanceledOnTouchOutside(canceledOnTouchOutside)
            val view = context.layoutInflater.inflate(R.layout.custom_call_notification, null)
            buttonAnswer = view.findViewById<Button>(R.id.btnAnswer)
            buttonDecline = view.findViewById<Button>(R.id.btnDecline)
            val txtCallerName = view.findViewById<TextView>(R.id.callerName)
            txtCallerName.text = title
            setView(view)
            buttonAnswer.setOnClickListener {
                this.dismiss()
                positiveListener?.invoke()
            }
            buttonDecline.setOnClickListener {
                this.dismiss()
                negativeListener?.invoke()
            }

            show()
        }
    }


}