package com.example.afjtracking.model.requests

import com.example.afjtracking.model.responses.FuelForm
import com.example.afjtracking.model.responses.InspectionForm
import com.google.gson.annotations.SerializedName

data class SaveFormRequest (
    @SerializedName("fuel_form" ) var fuelForm : List<FuelForm> = arrayListOf(),
    @SerializedName("report_form" ) var reportForm : List<InspectionForm> = arrayListOf(),
    @SerializedName("upload_id" ) var uploadID : String?= null
    )