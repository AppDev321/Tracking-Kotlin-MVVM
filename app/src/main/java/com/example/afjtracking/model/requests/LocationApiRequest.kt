package com.example.afjtracking.model.requests

import com.example.afjtracking.utils.AFJUtils
import com.google.gson.annotations.SerializedName

class LocationApiRequest {

    @SerializedName("device_detail")
    var deviceDetail: DeviceDetail? = AFJUtils.getDeviceDetail()

    @SerializedName("vehicle_id")
    var vehicleID: String? = null


    @SerializedName("altitude")
    var altitude: String? = null

    @SerializedName("heading")
    var heading: String? = null

    @SerializedName("latitude")
    var latitude: String? = null

    @SerializedName("accuracy")
    var accuracy: String? = null

    @SerializedName("speed_accuracy")
    var speedAccuracy: String? = null

    @SerializedName("time")
    var time: String? = null

    @SerializedName("is_mocked")
    var isMocked: Boolean? = null

    @SerializedName("speed")
    var speed: String? = null

    @SerializedName("longitude")
    var longitude: String? = null
}