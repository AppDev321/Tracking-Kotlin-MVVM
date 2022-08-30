package com.example.afjtracking.model.requests

import com.google.gson.annotations.SerializedName

data class FCMRegistrationRequest
    (
    @SerializedName("device_type") var deviceId: String? = "android",
    @SerializedName("fcm_token") var fcmToken: String? = null,
    @SerializedName("device_id") var vehicleDeviceId: String?=null

)