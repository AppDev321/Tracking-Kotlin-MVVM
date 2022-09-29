package com.example.afjtracking.firebase

import android.content.Context
import android.content.Intent
import com.example.afjtracking.broadcast.TrackingAppBroadcast
import com.example.afjtracking.model.responses.TrackingSettingFirebase
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings


object FirebaseConfig {

    private lateinit var mFirebaseRemoteConfig: FirebaseRemoteConfig

    val location_service_param = "LOCATION_SERVICE_TIME"
    val   data_query_limit = "FILE_QUERY_LIMIT"

    fun setTokenFirebase(context:Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            Constants.DEVICE_FCM_TOKEN = task.result

            AFJUtils.writeLogs("${Constants.DEVICE_FCM_TOKEN}")

        })


        val dbReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_TRACKING_SETTING)
       // dbReference.child(AFJUtils.getDeviceDetail().deviceID.toString()).setValue(TrackingSettingFirebase("123",false))
        dbReference.child(AFJUtils.getDeviceDetail().deviceID.toString())
            .addValueEventListener(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val trackingSetting = dataSnapshot.getValue(TrackingSettingFirebase::class.java)
                        if (trackingSetting == null) {
                            return
                        }

                      AFJUtils.setRequestingLocationUpdates(context, trackingSetting.tracking!!)
                        AFJUtils.writeLogs("Tracking status =${AFJUtils.requestingLocationUpdates(context)}")
                        Intent().also { intent ->
                            intent.action=Constants.NOTIFICATION_BROADCAST
                            intent.putExtra(TrackingAppBroadcast.BroadcastObect.intentData, TrackingAppBroadcast.BroadcastObect.trackingSettingEvent)
                            context.sendBroadcast(intent)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })


    }

    fun init() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(5)
            .build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        //  mFirebaseRemoteConfig.setDefaultsAsync(DEFAULT)//(com.example.afjtracking.R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetchAndActivate().addOnCanceledListener {
            AFJUtils.writeLogs("Remote config complete")
        }
    }


    fun getLocationServiceTimeValue(): Long = mFirebaseRemoteConfig.getLong(location_service_param)
    fun getDataQueryLimit(): Long = mFirebaseRemoteConfig.getLong(data_query_limit)
    fun fetchLocationServiceTime() {


        mFirebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(OnCompleteListener<Boolean?> { task ->
                if (task.isSuccessful) {
                    val timeSeconds = getLocationServiceTimeValue()
                    AFJUtils.writeLogs("LocationTime = $timeSeconds")
                    Constants.LOCATION_SERVICE_IN_SECONDS = (timeSeconds * 1000)

                    val queryData= getDataQueryLimit()
                    Constants.FILE_QUERY_LIMIT = queryData.toInt()


                }
            })
    }




}