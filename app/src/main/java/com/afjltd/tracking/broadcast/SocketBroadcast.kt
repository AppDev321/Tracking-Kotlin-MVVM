package com.afjltd.tracking.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.afjltd.tracking.broadcast.SocketBroadcast.SocketBroadcast.FCM_INCOMING_CALL
import com.afjltd.tracking.broadcast.SocketBroadcast.SocketBroadcast.SOCKET_MESSAGE_RECEIVED
import com.afjltd.tracking.broadcast.SocketBroadcast.SocketBroadcast.intentValues
import com.afjltd.tracking.broadcast.TrackingAppBroadcast.TrackingBroadCastObject.intentData
import com.afjltd.tracking.callscreen.CallIncomingBroadcastReceiver
import com.afjltd.tracking.callscreen.CallkitIncomingPlugin
import com.afjltd.tracking.callscreen.EventListener
import com.afjltd.tracking.view.activity.IncomingCallScreen
import com.afjltd.tracking.websocket.VideoCallActivity
import com.afjltd.tracking.websocket.model.MessageModel
import com.afjltd.tracking.websocket.model.MessageType

class SocketBroadcast : BroadcastReceiver() {
    object SocketBroadcast {
        const val intentData = "data"
        const val intentValues = "data_values"
        const  val SOCKET_BROADCAST = "socket_broadcast_msg"
        const val SOCKET_MESSAGE_RECEIVED = "rtc_message"


        const val FCM_INCOMING_CALL = "incoming_call"
    }
    override fun onReceive(context: Context, p1: Intent) {
        val data = p1.extras?.getString(intentData)


        when (data) {
            SOCKET_MESSAGE_RECEIVED ->
            {
                val messageModel = p1.getSerializableExtra(intentValues) as MessageModel
                when (messageModel.type) {
                    MessageType.OfferReceived.value -> {
                        /*val intent = Intent(context, IncomingCallScreen::class.java).apply {
                            putExtra(IncomingCallScreen.intentData, messageModel)

                        }
                        context.startActivity(intent)*/

                        CallkitIncomingPlugin.getInstance().showIncomingNotification(messageModel.callerName.toString(),false,context)
                        CallkitIncomingPlugin.setEventCallListener(object: EventListener(){
                            override fun send(event: String, body: Map<String, Any>) {
                                if(event== CallIncomingBroadcastReceiver.ACTION_CALL_ACCEPT)
                                {
                                    val currentUserId = messageModel.sendTo
                                    val targetUserID = messageModel.sendFrom
                                    val intent = Intent(context, VideoCallActivity::class.java).apply {
                                        putExtra(VideoCallActivity.currentUserID, "" + currentUserId)
                                        putExtra(VideoCallActivity.targetUserID, "" + targetUserID)
                                        putExtra(VideoCallActivity.messageIntentValue, messageModel)
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        })

                    }
                    else ->
                    {

                    }

                }
            }
            FCM_INCOMING_CALL->
            {
                val intent = Intent(context, IncomingCallScreen::class.java)
                context.startActivity(intent)
            }
        }

    }


}