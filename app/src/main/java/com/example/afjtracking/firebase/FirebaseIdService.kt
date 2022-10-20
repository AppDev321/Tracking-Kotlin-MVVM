package com.example.afjtracking.firebase

import android.content.Intent
import com.example.afjtracking.broadcast.TrackingAppBroadcast
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.utils.NotificationUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseIdService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Constants.DEVICE_FCM_TOKEN = token
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        AFJUtils.writeLogs("notification: ${data}")

        if (data != null) {

           /* val intent = Intent()
            intent.action = Constants.NOTIFICATION_BROADCAST
            sendBroadcast(intent)*/
            Intent().also { intent ->
                intent.action= TrackingAppBroadcast.TrackingBroadCastObject.NOTIFICATION_BROADCAST
                intent.putExtra( TrackingAppBroadcast.TrackingBroadCastObject.intentData, TrackingAppBroadcast.TrackingBroadCastObject.firebaseEvent)
                sendBroadcast(intent)
            }

            val type = data["type"]!!
            when (type.uppercase()) {
                AFJUtils.NOTIFICATIONTYPE.TEXT.name,
                AFJUtils.NOTIFICATIONTYPE.EVENT.name
                ->  {
                    message.data.let {
                        NotificationUtils.showTextImageNotification(
                            this,
                            title =
                            it["title"].toString(),
                            msg = it["body"].toString(),
                        )
                    }
                }
                AFJUtils.NOTIFICATIONTYPE.IMAGE.name-> {

                    message.data.let {
                        NotificationUtils.showTextImageNotification(
                            this,
                            title =
                            it["title"].toString(),
                            msg = it["body"].toString(),
                            imageURL = it["image"].toString(),
                            isImageNotification = true
                        )
                    }
                }
                AFJUtils.NOTIFICATIONTYPE.LOCATION.name -> {
                    message.data.let {
                        NotificationUtils.showLocationNotification(
                            this,
                            title = it["title"].toString(),
                            msg = it["body"].toString(),
                            lat = it["lat"]!!.toDouble(),
                            lng = it["lng"]!!.toDouble(),
                        )
                    }
                }


            }

        } else {
            message.notification?.let {
                NotificationUtils.showTextImageNotification(
                    this,
                    title =
                    it.title!!,
                    msg = it.body!!,
                )
            }
        }


    }


}
