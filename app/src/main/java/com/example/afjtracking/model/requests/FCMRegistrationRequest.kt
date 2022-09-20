package com.example.afjtracking.model.requests

import com.google.gson.annotations.SerializedName

data class FCMRegistrationRequest
    (
    @SerializedName("device_type") var deviceType: String? = "android",
    @SerializedName("fcm_token") var fcmToken: String? = null,
    @SerializedName("device_id") var vehicleDeviceId: String?=null,
    @SerializedName("type") var qrType: String? = "ATTENDANCE", //TRACKING_APP_LOGIN
    @SerializedName("vehicle_id") var vehicle_id :Int?= null
)