package com.example.afjtracking.model.requests

import com.example.afjtracking.model.responses.SensorData
import com.example.afjtracking.model.responses.WeeklyInspectionCheck
import com.example.afjtracking.utils.TimerListener
import com.google.gson.annotations.SerializedName

data class SavedWeeklyInspection(
    @SerializedName("inspection_id") var inspectionId: String? = null,
    @SerializedName("checks") var checks: ArrayList<WeeklyInspectionCheck> = arrayListOf(),
    @SerializedName("sensor_data") var sensorData: SensorData? = SensorData(),
    @SerializedName("time_spent") var inspectionTimeSpent:String? = null


)