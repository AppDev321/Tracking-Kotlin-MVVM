package com.example.afjtracking.firebase

import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings


object FirebaseConfig {

    private lateinit var mFirebaseRemoteConfig: FirebaseRemoteConfig

    val location_service_param = "LOCATION_SERVICE_TIME"
    val   data_query_limit = "FILE_QUERY_LIMIT"

    fun setTokenFirebase() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            Constants.DEVICE_FCM_TOKEN = task.result

            AFJUtils.writeLogs("${Constants.DEVICE_FCM_TOKEN}")

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
                    Constants.LOCATION_SERVICE_IN_SECONDS = (timeSeconds * 1000)

                    val queryData= getDataQueryLimit()
                    Constants.FILE_QUERY_LIMIT = queryData.toInt()


                }
            })
    }




}