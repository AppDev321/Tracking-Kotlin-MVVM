package com.example.afjtracking.service.location




import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service.START_NOT_STICKY
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.ContactsContract
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.stopForeground
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.afjtracking.AFJApplication
import com.example.afjtracking.R
import com.example.afjtracking.broadcast.TrackingAppBroadcast
import com.example.afjtracking.broadcast.TrackingAppBroadcast.TrackingBroadCastObject.EXTRA_LOCATION
import com.example.afjtracking.broadcast.TrackingAppBroadcast.TrackingBroadCastObject.NOTIFICATION_BROADCAST
import com.example.afjtracking.broadcast.TrackingAppBroadcast.TrackingBroadCastObject.intentData
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class ForegroundLocationService : LifecycleService() {
    private var configurationChange = false
    private var serviceRunningInForeground = false
    private val localBinder = LocalBinder()
    private lateinit var notificationManager: NotificationManager
    private var currentLocation: Location? = null
    lateinit var repository: LocationRepository
    private var locationFlow: Job? = null
    lateinit var context:Context
    private val trackingNotificationMsg = "Your location is being tracked"

    override fun onCreate() {
        super.onCreate()
        context = this
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        repository = LocationRepository(this)
        locationFlow = lifecycleScope.launch{
            repository.getLocations()
               .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest {
                    currentLocation = it
                    //AFJUtils.writeLogs("Location Emitted Received = ${it.toText()}")
                    val checkTrackingStatus = AFJUtils.getRequestingLocationUpdates(context)
                    if (checkTrackingStatus) {
                        Intent().also { intent ->
                            intent.action =
                                NOTIFICATION_BROADCAST
                            intent.putExtra(
                                intentData,
                                EXTRA_LOCATION
                            )
                            intent.putExtra(EXTRA_LOCATION, it)
                            sendBroadcast(intent)
                        }

                     //   if (serviceRunningInForeground) {
                            notificationManager.notify(
                                NOTIFICATION_ID,
                                generateNotification(trackingNotificationMsg)
                            )
                       // }
                    }
                    else
                    {
                            notificationManager.notify( NOTIFICATION_ID, generateNotification(null)  )
                    }
                }
        }
    }




    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val cancelLocationTrackingFromNotification =
            intent?.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)
        if (cancelLocationTrackingFromNotification == true) {
            unsubscribeToLocationUpdates()
            stopSelf()
        }
        return super.onStartCommand(intent, flags, START_NOT_STICKY)
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)

   stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        if (!configurationChange && AFJUtils.getRequestingLocationUpdates(this)) {

           // val notification = generateNotification(currentLocation)
            val notification = generateNotification(trackingNotificationMsg)
            startForeground(NOTIFICATION_ID, notification)
            serviceRunningInForeground = true
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    fun subscribeToLocationUpdates() {

       startService(Intent(applicationContext, ForegroundLocationService::class.java))

    }

    fun unsubscribeToLocationUpdates() {
        locationFlow?.cancel()
        AFJUtils.setRequestingLocationUpdates(this, false)
    }


    private fun generateNotification(location: String?): Notification {

        val mainNotificationText = location ?: "Tracking not enabled contact to admin"
        val titleText = getString(R.string.app_name)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        val launchActivityIntent = Intent(this, NavigationDrawerActivity::class.java)
        val cancelIntent = Intent(this, ForegroundLocationService::class.java)
        cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, true)
        val servicePendingIntent = PendingIntent.getService(
            this, 0, cancelIntent,  PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, launchActivityIntent,  PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        )
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setSmallIcon(R.drawable.ic_track_notificaiton)
           // .setDefaults(NotificationCompat.DEFAULT_ALL)
           // .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
           /* .addAction(
                R.drawable.ic_my_location, getString(R.string.launch_activity),
                activityPendingIntent
            )
           .addAction(
                R.drawable.ic_cancel,
                getString(R.string.remove_location_updates),
                servicePendingIntent
            )*/
            .build()
    }

    inner class LocalBinder : Binder() {
        internal val service: ForegroundLocationService
            get() = this@ForegroundLocationService
    }

    companion object {
        private const val PACKAGE_NAME = ContactsContract.Directory.PACKAGE_NAME
        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =  "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"
        private const val NOTIFICATION_ID = 12344
        private const val NOTIFICATION_CHANNEL_ID = "while_in_use_channel_01"


    }
}