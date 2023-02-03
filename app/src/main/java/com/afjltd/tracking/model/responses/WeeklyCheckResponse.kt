package com.afjltd.tracking.model.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class WeeklyCheckResponse {
    @SerializedName("code")
    @Expose
    var code: Int? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("data")
    @Expose
    var data: Data? = null

    @SerializedName("errors")
    @Expose
    var errors: List<Error>? = null
    var exceptionMsg = ""

    inner class Error {
        @SerializedName("message")
        @Expose
        var message: String? = null
    }

}

