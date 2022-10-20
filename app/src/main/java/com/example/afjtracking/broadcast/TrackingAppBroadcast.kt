package com.example.afjtracking.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.provider.ContactsContract
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.afjtracking.broadcast.TrackingAppBroadcast.TrackingBroadCastObject.EXTRA_LOCATION
import com.example.afjtracking.broadcast.TrackingAppBroadcast.TrackingBroadCastObject.firebaseEvent
import com.example.afjtracking.broadcast.TrackingAppBroadcast.TrackingBroadCastObject.intentData
import com.example.afjtracking.broadcast.TrackingAppBroadcast.TrackingBroadCastObject.trackingSettingEvent
import com.example.afjtracking.model.requests.LocationApiRequest
import com.example.afjtracking.model.responses.VehicleDetail
import com.example.afjtracking.service.location.ForegroundLocationService
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.view.fragment.home.viewmodel.TrackingViewModel

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