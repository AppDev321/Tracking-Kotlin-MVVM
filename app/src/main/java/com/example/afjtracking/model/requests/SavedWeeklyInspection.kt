package com.example.afjtracking.model.requests

import com.example.afjtracking.model.responses.WeeklyInspectionCheck
import com.google.gson.annotations.SerializedName

data class SavedWeeklyInspection (
    @SerializedName("inspection_id")  var inspectionId:String ? = null,
    @SerializedName("checks"  ) var checks   : ArrayList<WeeklyInspectionCheck>  = arrayListOf()

)