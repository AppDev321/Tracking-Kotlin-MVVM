package com.example.afjtracking.firebase

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
        val data =  message.data
        AFJUtils.writeLogs("notification: ${data}")

        if(data != null)
        {
            val type =   data["type"]
            message.data.let {
                NotificationUtils.showTextImageNotification(this, title =
                it["title"].toString() ,
                    msg =    it["body"].toString(),
                )
            }
        }

        else {
            message.notification?.let {
                NotificationUtils.showTextImageNotification(this, title =
                it.title!!,
                    msg =  it.body!!,
                )
            }
        }


    }


}
