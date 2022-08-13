package com.example.afjtracking.model.requests

import com.google.gson.annotations.SerializedName

data class DailyInspectionListRequest
    (
    @SerializedName("start" ) var start : Int? = 0,
    @SerializedName("limit" ) var limit : Int? = 0
)