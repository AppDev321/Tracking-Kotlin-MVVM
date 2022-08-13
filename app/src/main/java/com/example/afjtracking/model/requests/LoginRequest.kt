package com.example.afjtracking.model.requests

import android.util.Patterns
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LoginRequest(
    @field:Expose @field:SerializedName("email") var strEmailAddress: String,
    @field:Expose @field:SerializedName("password") var strPassword: String,
    @field:Expose @field:SerializedName("registration_number") var vrnNumber: String,
    @field:Expose @field:SerializedName("device_detail") var deviceDetail:DeviceDetail?= DeviceDetail()
) {

    val isEmailValid: Boolean
        get() = Patterns.EMAIL_ADDRESS.matcher(strEmailAddress).matches()
    val isPasswordLengthGreaterThan5: Boolean
        get() = strPassword.length > 3
}


class DeviceDetail(
    @SerializedName("brand") var brand: String? = null,
    @SerializedName("model") var model: String? = null,
    @SerializedName("android_ver") var androidVersion: String? = null,
    @SerializedName("device_id") var deviceID: String? = null,
    @SerializedName("mac_bt") var macBluetooth: String? = null,
    @SerializedName("imei") var imeiSim: String? = null
)