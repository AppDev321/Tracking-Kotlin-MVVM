package com.afjltd.tracking.model.requests

import com.google.gson.annotations.SerializedName

data class SavedInspection(

    @SerializedName("vehicle_inspection_id" ) var vehicleInspectionId : String? = null,
    @SerializedName("check_no"              ) var checkNo             : String? = null,
    @SerializedName("name"                  ) var name                : String? = null,
    @SerializedName("type"                  ) var type                : String? = null,
    @SerializedName("code"                  ) var code                : String? = null,
    @SerializedName("comment"               ) var comment             : String? = null
)
