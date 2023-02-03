package com.afjltd.tracking.model.requests

import com.google.gson.annotations.SerializedName

data class SingleInspectionRequest
    (
    @SerializedName("vehicle_inspection_id") var vehicleInspectionId: String? = null,
    @SerializedName("vehicle_daily_inspection_id") var dailyInspectionId: String? = null,
)