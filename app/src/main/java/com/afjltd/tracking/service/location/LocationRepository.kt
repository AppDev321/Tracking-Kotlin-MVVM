package com.afjltd.tracking.service.location

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

class LocationRepository  (
    private val context:Context

) {

    private var sharedLocationManager: LocationManager = LocationManager(  context,
        (context.applicationContext as com.afjltd.tracking.AFJApplication).applicationScope
    )

    val isTrackingStatusEnabled: StateFlow<Boolean> = sharedLocationManager.isTrackingStatusEnabled
    fun getLocations() = sharedLocationManager.locationFlow()

}

