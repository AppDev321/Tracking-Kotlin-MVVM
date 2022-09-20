package com.example.afjtracking.model.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LocationResponse {
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
    var errors: List<Error> = arrayListOf()
    var exceptionMsg = ""

    inner class Error {
        @SerializedName("message")
        @Expose
        var message: String? = null
    }

    inner class Data {
        @SerializedName("message")
        @Expose
        var message: String? = null

        //*************
        @SerializedName("id")
        @Expose
        var id: String?=null

        @SerializedName("vehicle_id")
        @Expose
        var vehicleId: String?=null

        @SerializedName("isCompleted" )
        var isCompleted : Boolean? = null


        @SerializedName("code" )
        var attendanceCode : String? = null

        @SerializedName("expires_after" )
        var expireCodeSecond : Int? = null

        @SerializedName("count")
        var notificationCount :Int?= null

        //*********************
    }
}