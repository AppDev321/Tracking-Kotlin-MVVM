package com.example.afjtracking.model.requests

import com.example.afjtracking.utils.AFJUtils
import com.google.gson.annotations.SerializedName

data class DailyInspectionListRequest
    (
    @SerializedName("start" ) var start : Int? = 0,
    @SerializedName("limit" ) var limit : Int? = 0,
    @SerializedName("device_detail") var deviceDetail:DeviceDetail?= AFJUtils.getDeviceDetail()
)