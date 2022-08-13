package com.example.afjtracking.model.requests

import com.google.gson.annotations.SerializedName

data class WeeklyVehicleInspectionRequest
    (
    @SerializedName("vehicle_id") var vehicleId: String? = null,

)