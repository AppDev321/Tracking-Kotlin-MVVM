package com.example.afjtracking.utils

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationUtils {
    companion object {
         fun showNotification(
            context: Context,
            Message: String,
            name: String,
            Information: String
        ) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val NOTIFICATION_CHANNEL_ID = "my_channel_id_01"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Stock Market",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationChannel.description = "Channel description"
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                notificationManager.createNotificationChannel(notificationChannel)
            }
            val notificationBuilder = NotificationCompat.Builder(
                context, NOTIFICATION_CHANNEL_ID
            )
            val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            notificationBuilder.setAutoCancel(false)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_menu_add)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(Message)
                .setContentText(name)
                .setContentInfo(Information)
            notificationManager.notify( /*notification id*/1, notificationBuilder.build())
        }
    }
}