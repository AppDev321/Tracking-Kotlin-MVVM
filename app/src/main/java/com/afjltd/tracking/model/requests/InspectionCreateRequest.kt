package com.afjltd.tracking.model.requests

import com.google.gson.annotations.SerializedName

data class InspectionCreateRequest
    (
    @SerializedName("vehicle_id") var vehicleId: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("date") var date: String? = null,
    @SerializedName("odometer_reading") var odoMeterReading: String? = null
)