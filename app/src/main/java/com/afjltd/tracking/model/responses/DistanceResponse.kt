package com.afjltd.tracking.model.responses

import com.google.gson.annotations.SerializedName

data class DistanceResponse (

    @SerializedName("code"    ) var code    : Int?              = null,
    @SerializedName("message" ) var message : String?           = null,
    @SerializedName("data"    ) var data    : Calculation?      = Calculation(),
    @SerializedName("errors"  ) var errors  : ArrayList<Error> = arrayListOf()

)
data class Calculation (

    @SerializedName("distance"       ) var distance      : String? = null,
    @SerializedName("time"           ) var time          : String? = null,
    @SerializedName("distance_value" ) var distanceValue : Int?    = null

)
