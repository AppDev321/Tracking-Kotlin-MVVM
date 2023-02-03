package com.afjltd.tracking.model.requests

import com.google.gson.annotations.SerializedName

data class WeeklyVehicleInspectionRequest
    (

    //start * limit which is the logic of server end implemented
    @SerializedName("start" ) var start : Int? = 0,
    @SerializedName("limit" ) var limit : Int? = 0,
    @SerializedName("vehicle_id") var vehicleId: String? = null,
    @SerializedName("device_detail") var deviceDetail:DeviceDetail?= DeviceDetail()

)