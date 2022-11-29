package com.example.afjtracking.firebase

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
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
        AFJUtils.writeLogs("notification: ${message.data}")
       // Toast.makeText(this," ${message.data} ",Toast.LENGTH_SHORT).show()


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
                AFJUtils.NOTIFICATIONTYPE.CALLING.name->
                {
                    /*Intent().also{
                        it.action = SocketBroadcast.SocketBroadcast.SOCKET_BROADCAST
                        it.putExtra( SocketBroadcast.SocketBroadcast.intentData,
                            SocketBroadcast.SocketBroadcast.FCM_INCOMING_CALL)
                        sendBroadcast(it)
                    }*/



                    val serviceIntent = Intent(
                        applicationContext,
                        HeadsUpNotificationService::class.java
                    )
                    val mBundle = Bundle()
                    mBundle.putString("inititator", "test")
                    mBundle.putString("call_type", "normal")
                    serviceIntent.putExtras(mBundle)
                    ContextCompat.startForegroundService(applicationContext, serviceIntent)

                    Thread.sleep(30000)
                    println("Notification After 30 sec")
                    applicationContext.stopService(
                        Intent(
                            applicationContext,
                            HeadsUpNotificationService::class.java
                        )
                    )
                    val istop = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                    applicationContext.sendBroadcast(istop)
                }
            }

        } else {
            message.notification?.let {
                NotificationUtils.showTextImageNotification(
                    this,
                    title ="Our test call",
                   // it.title!!,
                    msg = it.body!!,
                )
            }
        }


    }


   /* private fun initiateCallService(call: Call) {
        try {
            var callManager: CallHandler? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.e("initiateCallService: ",call.toString())
                callManager = CallHandler(applicationContext)
                callManager.init()
                callManager.startIncomingCall(call)
            }
        } catch (e: Exception) {
            Log.e("initiateCallError:","${e.message}" )
            Toast.makeText(applicationContext, "Unable to receive call due to " + e.message, Toast.LENGTH_LONG)
        }
    }*/


}
