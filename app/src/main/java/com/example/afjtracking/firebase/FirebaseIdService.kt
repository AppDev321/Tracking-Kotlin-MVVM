package com.example.afjtracking.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.text.HtmlCompat
import com.example.afjtracking.R
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseIdService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Constants.DEVICE_FCM_TOKEN = token
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data =  message.data
        AFJUtils.writeLogs("notificaiton: ${data}")

        if(data != null)
        {
            val type =   data["type"]

            val intent = Intent(this,
                NavigationDrawerActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notificationId = 12910
            val channelId = "channel-01"
            val channelName = "Channel Name"
            val importance =  NotificationManager.IMPORTANCE_HIGH

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val mChannel = NotificationChannel(channelId, channelName, importance)
                notificationManager.createNotificationChannel(mChannel)
            }

            val mBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                .setContentText (HtmlCompat.fromHtml(message.notification!!.title.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY))
                .setContentTitle(HtmlCompat.fromHtml(message.notification!!.body.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY))

                //.setContentTitle(HtmlCompat.fromHtml(data["title"].toString(), HtmlCompat.FROM_HTML_MODE_LEGACY))
               // .setContentText (HtmlCompat.fromHtml(data["body"].toString(), HtmlCompat.FROM_HTML_MODE_LEGACY))
                .setSound(notificationSound)
                .setAutoCancel(true)

            val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(this)
            stackBuilder.addNextIntent(intent)
            val resultPendingIntent: PendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            mBuilder.setContentIntent(resultPendingIntent)
            notificationManager.notify(notificationId, mBuilder.build())
        }


        else {
            message.notification?.let {

                //Message Services handle notification
                val notification = NotificationCompat.Builder(this)
                    .setContentTitle(HtmlCompat.fromHtml(it.title!!, HtmlCompat.FROM_HTML_MODE_LEGACY))
                    .setContentText(HtmlCompat.fromHtml(it.body!!, HtmlCompat.FROM_HTML_MODE_LEGACY))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build()
                val manager = NotificationManagerCompat.from(applicationContext)
                manager.notify(/*notification id*/0, notification)
            }
        }


    }


}
