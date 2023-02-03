package com.afjltd.tracking.model.requests

import com.afjltd.tracking.model.responses.SensorOrientationData
import com.afjltd.tracking.model.responses.WeeklyInspectionCheck
import com.google.gson.annotations.SerializedName

data class SavedWeeklyInspection(
    @SerializedName("inspection_id") var inspectionId: String? = null,
    @SerializedName("checks") var checks: ArrayList<WeeklyInspectionCheck> = arrayListOf(),
  //  @SerializedName("sensor_data") var sensorData: SensorData? = SensorData(),
    @SerializedName("sensor_data") var sensorData: List<SensorOrientationData>? = arrayListOf(),
    @SerializedName("time_spent") var inspectionTimeSpent:String? = null


)