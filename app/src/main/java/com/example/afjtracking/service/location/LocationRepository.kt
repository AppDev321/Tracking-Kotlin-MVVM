package com.example.afjtracking.service.location

import android.content.Context
import com.example.afjtracking.AFJApplication
import kotlinx.coroutines.flow.StateFlow

class LocationRepository  (
    private val context:Context

) {

    private var sharedLocationManager: LocationManager = LocationManager(  context,
        (context.applicationContext as AFJApplication).applicationScope
    )

    val isTrackingStatusEnabled: StateFlow<Boolean> = sharedLocationManager.isTrackingStatusEnabled
    fun getLocations() = sharedLocationManager.locationFlow()

}

