package com.example.afjtracking.utils


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.afjtracking.R
import com.example.afjtracking.view.activity.NavigationDrawerActivity


class NotificationUtils {

    companion object {

        fun showTextImageNotification(
            context: Context,
            title: String,
            smallDesc: String = "",
            msg: String,
            isImageNotification: Boolean = false,
            imageURL: String = "",
        ) {


            val CHANNEL_ID = "channel_text"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Text Channel"
                val channelDescription = "Text Channel Description"
                val importance = NotificationManager.IMPORTANCE_DEFAULT

                val channel = NotificationChannel(CHANNEL_ID, name, importance)
                channel.apply {
                    description = channelDescription
                }

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            notificationBuilder.setAutoCancel(false)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_track_notificaiton)
                .setSound(notificationSound).priority = Notification.PRIORITY_MAX
            if (msg.length > 200) {
                notificationBuilder.setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(msg)
                        .setBigContentTitle(title)
                        .setSummaryText(smallDesc)
                )
            } else {
                notificationBuilder.setContentTitle(title)
                    .setContentText(msg)
                    .setContentInfo(msg)

            }

            if (isImageNotification) {
                Glide.with(context)
                    .asBitmap()
                    .load(imageURL)
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {

                            notificationBuilder.setLargeIcon(resource)
                            notificationBuilder.setStyle(
                                NotificationCompat.BigPictureStyle().bigPicture(resource)
                            )
                            val notificationId = 1
                            with(NotificationManagerCompat.from(context)) {
                                notify(notificationId, notificationBuilder.build())
                            }

                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }

                    })


            } else {
                val notificationId = 1
                with(NotificationManagerCompat.from(context)) {
                    notify(notificationId, notificationBuilder.build())
                }
            }


        }

        fun showLocationNotification(
            context: Context,
            title: String,
            msg: String,
            lat: Double,
            lng: Double

        ) {
            val CHANNEL_ID = "channel_loc"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Location Channel"
                val channelDescription = "Location Channel Description"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance)
                channel.apply {
                    description = channelDescription
                }

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
            val detailsIntent = Intent(context, NavigationDrawerActivity::class.java)
            detailsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            detailsIntent.putExtra("EXTRA_DETAILS_ID", 42)
            val detailsPendingIntent = PendingIntent.getActivity(
                context,
                0,
                detailsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val uri = "http://maps.google.com/maps?daddr=" + lat + "," + lng
            val mapClassIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            mapClassIntent.setPackage("com.google.android.apps.maps")
            val mapPendingIntent = PendingIntent.getActivity(
                context,
                0,
                mapClassIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            notificationBuilder.setAutoCancel(false)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.location)
                .setContentTitle(title)
                .setContentText(msg)
                .setSound(notificationSound)
                .setContentInfo(msg)
                .addAction(R.drawable.ic_menu_compass, "Details", detailsPendingIntent)
                .addAction(R.drawable.ic_menu_direction, "Show Map", mapPendingIntent)
            val notificationId = 22
            // notificationManager.notify( /*notification id*/1, notificationBuilder.build())
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, notificationBuilder.build())
            }
        }
    }


}