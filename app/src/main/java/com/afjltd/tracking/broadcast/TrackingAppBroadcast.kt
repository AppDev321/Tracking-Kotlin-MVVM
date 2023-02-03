package com.afjltd.tracking.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.provider.ContactsContract
import com.afjltd.tracking.broadcast.TrackingAppBroadcast.TrackingBroadCastObject.EXTRA_LOCATION
import com.afjltd.tracking.broadcast.TrackingAppBroadcast.TrackingBroadCastObject.firebaseEvent
import com.afjltd.tracking.broadcast.TrackingAppBroadcast.TrackingBroadCastObject.intentData
import com.afjltd.tracking.broadcast.TrackingAppBroadcast.TrackingBroadCastObject.trackingSettingEvent

abstract class TrackingAppBroadcast : BroadcastReceiver() {
    object TrackingBroadCastObject {
        private const val PACKAGE_NAME = ContactsContract.Directory.PACKAGE_NAME
        const val intentData = "data"
        const val firebaseEvent = "firebase_notification"
        const val trackingSettingEvent = "tracking_setting_action"
        const val EXTRA_LOCATION = "$PACKAGE_NAME.location"
        const  val NOTIFICATION_BROADCAST = "notification_boradcast"
    }


    override fun onReceive(context: Context, p1: Intent) {
        val data = p1.extras?.getString(intentData)
     //   AFJUtils.writeLogs(data.toString())
        when (data) {
            firebaseEvent -> {
                refreshNotificationCount()
            }
            trackingSettingEvent -> {
                trackingSetting()
            }
            EXTRA_LOCATION -> {
                    val location = p1.getParcelableExtra<Location>(EXTRA_LOCATION)
                    onLocationReceived(location)
            }


        }

    }
    open fun onLocationReceived(location:Location?){}
    open fun refreshNotificationCount() {}
    open fun trackingSetting() {}


}