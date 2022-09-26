/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.afjtracking.service.location

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.*
import android.provider.ContactsContract.Directory.PACKAGE_NAME
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.afjtracking.R
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.AFJUtils.getLocationText
import com.example.afjtracking.utils.AFJUtils.getLocationTitle
import com.example.afjtracking.utils.AFJUtils.requestingLocationUpdates
import com.example.afjtracking.utils.AFJUtils.setRequestingLocationUpdates
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.google.android.gms.location.*

class LocationUpdatesService : Service() {

    companion object{
        val ACTION_BROADCAST = PACKAGE_NAME + ".broadcast"
        val EXTRA_LOCATION = PACKAGE_NAME + ".location"
    }


    private  var PACKAGE_NAME =""

    private val TAG = LocationUpdatesService::class.java.simpleName
    private  val CHANNEL_ID = "channel_map"

    private  val EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification"
    //private  val UPDATE_INTERVAL_IN_MILLISECONDS = (10000 * 1).toLong()

    private  var UPDATE_INTERVAL_IN_MILLISECONDS = Constants.LOCATION_SERVICE_IN_SECONDS
    private  val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
        UPDATE_INTERVAL_IN_MILLISECONDS / 2

    private  val NOTIFICATION_ID = 12211



    private val mBinder: IBinder = LocalBinder()
    private var mChangingConfiguration = false
    private var mNotificationManager: NotificationManager? = null
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null
    private var mServiceHandler: Handler? = null
    private var mLocation: Location? = null
    override fun onCreate() {

        PACKAGE_NAME = this.packageName
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult.lastLocation)
            }
        }

        createLocationRequest()

        lastLocation

        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.app_name)
            val mChannel =
                NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
              mNotificationManager!!.createNotificationChannel(mChannel)
        }

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val startedFromNotification = intent.getBooleanExtra(
            EXTRA_STARTED_FROM_NOTIFICATION,
            false
        )
          if (startedFromNotification) {
            removeLocationUpdates()
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mChangingConfiguration = true
    }

    override fun onBind(intent: Intent): IBinder? {


        stopForeground(true)
        mChangingConfiguration = false
        return mBinder
    }

    override fun onRebind(intent: Intent) {


        stopForeground(true)
        mChangingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {


        if (!mChangingConfiguration && requestingLocationUpdates(this)) {
            /*
            // TODO(developer). If targeting O, use the following code.
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                mNotificationManager.startServiceInForeground(new Intent(this,
                        LocationUpdatesService.class), NOTIFICATION_ID, getNotification());
            } else {
                startForeground(NOTIFICATION_ID, getNotification());
            }
             */
            startForeground(NOTIFICATION_ID, notification)
        }
        return true // Ensures onRebind() is called when a client re-binds.
    }

    override fun onDestroy() {
        mServiceHandler!!.removeCallbacksAndMessages(null)
    }


    fun requestLocationUpdates() {
        setRequestingLocationUpdates(this, true)
        startService(Intent(applicationContext, LocationUpdatesService::class.java))
        try {
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback, Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            setRequestingLocationUpdates(this, false)
         }
    }

    fun removeLocationUpdates() {

        try {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
            setRequestingLocationUpdates(this, false)
            stopSelf()
        } catch (unlikely: SecurityException) {
            setRequestingLocationUpdates(this, true)

        }
    }

    private val notification: Notification
        private get() {
            val intent = Intent(this, LocationUpdatesService::class.java)
            val text: CharSequence = getLocationText(mLocation)
            intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)
            val servicePendingIntent = PendingIntent.getService(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val activityPendingIntent = PendingIntent.getActivity(
                this, 0,
                Intent(this, NavigationDrawerActivity::class.java), 0 or PendingIntent.FLAG_IMMUTABLE
            )
            val builder = NotificationCompat.Builder(this)
                .addAction(
                    R.drawable.ic_launch, getString(R.string.launch_activity),
                    activityPendingIntent
                )
                .addAction(
                    R.drawable.ic_cancel, getString(R.string.remove_location_updates),
                    servicePendingIntent
                )
                .setContentText(text)
                .setContentTitle(getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_track_notificaiton)
                .setTicker(text)
                .setSilent(true)
                .setWhen(System.currentTimeMillis())

            // Set the Channel ID for Android O.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setNotificationSilent()
                builder.setChannelId(CHANNEL_ID) // Channel ID
            }
            return builder.build()
        }


    private val lastLocation: Unit
        private get() {
            try {
                mFusedLocationClient!!.lastLocation
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result != null) {
                            mLocation = task.result
                        } else {
                            Log.w(TAG, "Failed to get location.")
                        }
                    }
            } catch (unlikely: SecurityException) {
                Log.e(TAG, "Lost location permission.$unlikely")
            }
        }

    private fun onNewLocation(location: Location) {

        mLocation = location
        UPDATE_INTERVAL_IN_MILLISECONDS = Constants.LOCATION_SERVICE_IN_SECONDS
       // AFJUtils.writeLogs("time value= $UPDATE_INTERVAL_IN_MILLISECONDS")
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_LOCATION, location)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager!!.notify(NOTIFICATION_ID, notification)
       }
    }

    private fun createLocationRequest() {


        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest!!.priority =
            LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    inner class LocalBinder : Binder() {
        val service: LocationUpdatesService
            get() = this@LocationUpdatesService
    }

    fun serviceIsRunningInForeground(context: Context): Boolean {
        val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (javaClass.name == service.service.className) {
                AFJUtils.writeLogs("Service= ${javaClass.name} ")
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }


}

