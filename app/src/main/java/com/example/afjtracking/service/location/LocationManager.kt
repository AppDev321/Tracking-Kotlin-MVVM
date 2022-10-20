package com.example.afjtracking.service.location


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Looper
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.utils.MonitorConnectivity

import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit

class LocationManager (
    private val context: Context,
    externalScope: CoroutineScope
) {

    private val _trackingStatus: MutableStateFlow<Boolean> =
        MutableStateFlow(false)


    val isTrackingStatusEnabled: StateFlow<Boolean>
        get() = _trackingStatus




    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Stores parameters for requests to the FusedLocationProviderApi.
    private val locationRequest = LocationRequest.create().apply {
      val duration:Long =  5// Constants.LOCATION_SERVICE_IN_SECONDS //1

        interval = TimeUnit.SECONDS.toMillis(duration)
        fastestInterval = TimeUnit.SECONDS.toMillis(duration)
        maxWaitTime = TimeUnit.SECONDS.toMillis(duration)
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    @SuppressLint("MissingPermission")
    private val _locationUpdates = callbackFlow<Location> {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result
                     result.lastLocation?.let {
                     //   AFJUtils.writeLogs("New Location Received = ${it.toText()}")

                         trySend(it)
                }
            }
        }

        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            !context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) close()


        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        ).addOnFailureListener { e ->
            close(e) // in case of exception, close the Flow
        }

        awaitClose {


            fusedLocationClient.removeLocationUpdates(callback) // clean up when Flow collection ends
        }
    }.shareIn(
        externalScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )


    fun locationFlow(): Flow<Location> {
        return _locationUpdates
    }


}