package com.afjltd.tracking.model.requests

import com.google.gson.annotations.SerializedName

data class DistanceRequest(
    @SerializedName("locations") var location: ArrayList<LocationData> = arrayListOf()
)

data class LocationData(
    @SerializedName("latitude") var latitude: Double? = null,
    @SerializedName("longitude") var longitude: Double? = null
)