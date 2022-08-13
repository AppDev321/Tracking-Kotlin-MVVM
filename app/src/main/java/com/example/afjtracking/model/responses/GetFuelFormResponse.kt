package com.example.afjtracking.model.responses

import com.google.gson.annotations.SerializedName


data class GetFuelFormResponse(

    @SerializedName("code") var code: Int? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: FuelData? = FuelData(),
    @SerializedName("errors") var errors: ArrayList<Error> = arrayListOf(),


 )



data class FuelData(

    @SerializedName("fuel_form" ) var fuelForm : ArrayList<FuelForm> = arrayListOf(),
    @SerializedName("vehicle"   ) var vehicle  : Vehicle?            = Vehicle()

)

data class FuelForm (

    @SerializedName("input_no"   ) var inputNo   : Int?     = null,
    @SerializedName("title"      ) var title     : String?  = null,
    @SerializedName("field_name" ) var fieldName : String?  = null,
    @SerializedName("type"       ) var type      : String?  = null,
    @SerializedName("accept"     ) var accept    : String?  = null,
    @SerializedName("required"   ) var required  : Boolean? = null,
    @SerializedName("comment"    ) var comment   : String?  = null,
    @SerializedName("value"      ) var value     : String?  = null

)