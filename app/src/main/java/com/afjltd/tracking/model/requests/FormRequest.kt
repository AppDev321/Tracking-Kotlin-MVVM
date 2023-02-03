package com.afjltd.tracking.model.requests

import com.google.gson.annotations.SerializedName

data class FormRequest(
    @SerializedName("identifier") var formIdentifier: String? = null,
    @SerializedName("device_detail") var   deviceDetail: DeviceDetail? = null
)