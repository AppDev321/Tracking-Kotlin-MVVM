package com.afjltd.tracking.model.requests

import com.afjltd.tracking.model.responses.Form
import com.google.gson.annotations.SerializedName

data class SaveFormRequest(
    @SerializedName("form") var formData: List<Form> = arrayListOf(),
    @SerializedName("upload_id") var uploadID: String? = null,
    @SerializedName("request_name") var requestName: String? = null,
    @SerializedName("identifier") var identifier: String? = null,
    @SerializedName("device_detail") var deviceDetail: DeviceDetail?=null
)