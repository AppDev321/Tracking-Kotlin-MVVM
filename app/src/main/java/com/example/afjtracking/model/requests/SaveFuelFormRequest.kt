package com.example.afjtracking.model.requests

import com.example.afjtracking.model.responses.FuelForm
import com.google.gson.annotations.SerializedName

data class SaveFuelFormRequest (
    @SerializedName("fuel_form" ) var fuelForm : List<FuelForm> = arrayListOf(),

    )