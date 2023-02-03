package com.afjltd.tracking.model.requests

import com.afjltd.tracking.utils.AFJUtils
import com.google.gson.annotations.SerializedName

data class DailyInspectionListRequest
(

    //start * limit which is the logic of server end implemented
    @SerializedName("start" ) var start : Int? = 0,
    @SerializedName("limit" ) var limit : Int? = 0,
    @SerializedName("device_detail") var deviceDetail:DeviceDetail?= AFJUtils.getDeviceDetail()

)


