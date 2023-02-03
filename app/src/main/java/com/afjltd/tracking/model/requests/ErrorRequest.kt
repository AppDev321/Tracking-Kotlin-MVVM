package com.afjltd.tracking.model.requests

import com.google.gson.annotations.SerializedName

data class ErrorRequest
    (
    @SerializedName("device_id") var deviceId: String? = null,
    @SerializedName("endpoint") var endpoint: String? = null,
    @SerializedName("error") var error: String? = null,
    @SerializedName("retries") var retries: String? = null
)