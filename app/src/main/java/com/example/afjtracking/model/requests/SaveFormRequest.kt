package com.example.afjtracking.model.requests

import android.service.carrier.CarrierIdentifier
import com.example.afjtracking.model.responses.Form
import com.google.gson.annotations.SerializedName

data class SaveFormRequest(
    @SerializedName("form") var formData: List<Form> = arrayListOf(),
    @SerializedName("upload_id") var uploadID: String? = null,
    @SerializedName("request_name") var requestName: String? = null,
    @SerializedName("identifier") var identifier: String? = null,
    @SerializedName("device_detail") var deviceDetail: DeviceDetail?=null
)