package com.example.afjtracking.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.afjtracking.broadcast.TrackingAppBroadcast.BroadcastObect.firebaseEvent
import com.example.afjtracking.broadcast.TrackingAppBroadcast.BroadcastObect.intentData
import com.example.afjtracking.broadcast.TrackingAppBroadcast.BroadcastObect.trackingSettingEvent
import com.example.afjtracking.utils.AFJUtils

abstract class TrackingAppBroadcast : BroadcastReceiver() {
    object BroadcastObect {
        val intentData = "data"
        val firebaseEvent = "firebase_notification"
        val trackingSettingEvent = "tracking_setting_action"
    }


    override fun onReceive(context: Context?, p1: Intent?) {
        val data = p1?.extras?.getString(intentData)
        AFJUtils.writeLogs(data.toString())
        when (data) {
            firebaseEvent -> {
                refreshNotificationCount()
            }
            trackingSettingEvent -> {
                trackingSetting()
            }
        }

    }

    open fun refreshNotificationCount() {}
    open fun trackingSetting() {}

}