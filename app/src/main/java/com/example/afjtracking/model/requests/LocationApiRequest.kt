package com.example.afjtracking.model.requests

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LocationApiRequest {
    @SerializedName("vehicle_id")
    @Expose
    var vehicleID: String? = null


    @SerializedName("altitude")
    @Expose
    var altitude: String? = null

    @SerializedName("heading")
    @Expose
    var heading: String? = null

    @SerializedName("latitude")
    @Expose
    var latitude: String? = null

    @SerializedName("accuracy")
    @Expose
    var accuracy: String? = null

    @SerializedName("speed_accuracy")
    @Expose
    var speedAccuracy: String? = null

    @SerializedName("time")
    @Expose
    var time: String? = null

    @SerializedName("is_mocked")
    @Expose
    var isMocked: Boolean? = null

    @SerializedName("speed")
    @Expose
    var speed: String? = null

    @SerializedName("longitude")
    @Expose
    var longitude: String? = null
}