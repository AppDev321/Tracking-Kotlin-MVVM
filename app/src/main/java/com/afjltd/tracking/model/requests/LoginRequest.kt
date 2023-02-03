package com.afjltd.tracking.model.requests

import android.util.Patterns
import com.afjltd.tracking.model.responses.Sheets
import com.google.gson.annotations.SerializedName

class LoginRequest(
    @SerializedName("email") var strEmailAddress: String?=null,
    @SerializedName("password") var strPassword: String?=null,
    @SerializedName("registration_number") var vrnNumber: String?=null,
    @SerializedName("device_detail") var deviceDetail:DeviceDetail?= DeviceDetail(),
    @SerializedName("device_id") var deviceID:String?= null,
    @SerializedName("notification_id") var notificatonID:Int?= null,
    @SerializedName("sheet") var routeSheet:Sheets?=null,
    @SerializedName("longitude")   var longitude: String? = null,
    @SerializedName("latitude")  var latitude: String? = null

) {

    val isEmailValid: Boolean
        get() = Patterns.EMAIL_ADDRESS.matcher(strEmailAddress).matches()
    val isPasswordLengthGreaterThan5: Boolean
        get() = strPassword!!.length > 3
}


class DeviceDetail(
    @SerializedName("brand") var brand: String? = null,
    @SerializedName("model") var model: String? = null,
    @SerializedName("android_ver") var androidVersion: String? = null,
    @SerializedName("device_id") var deviceID: String? = null,
    @SerializedName("mac_bt") var macBluetooth: String? = null,
    @SerializedName("imei") var imeiSim: String? = null
)