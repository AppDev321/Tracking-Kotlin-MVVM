package com.example.afjtracking.model.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UploadFileAPiResponse {
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

    inner class Data {
        @SerializedName("token")
        @Expose
        var token: String? = null

        @SerializedName("path")
        @Expose
        var path: String? = null

        @SerializedName("complete_url")
        @Expose
        var completeUrl: String? = null

    }
}