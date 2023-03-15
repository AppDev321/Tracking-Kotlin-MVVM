package com.afjltd.tracking.model.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

class GetRouteListResponse
    (
    @SerializedName("code") var code: Int? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: RouteData? = RouteData(),
    @SerializedName("errors") var errors: ArrayList<Error> = arrayListOf()

)

data class RouteData(

    @SerializedName("sheets") var sheets: ArrayList<Sheets> = arrayListOf()

)

@Parcelize
data class Sheets(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("role") var role: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("address") var address: String? = null,
    @SerializedName("latitude") var latitude: Double? = null,
    @SerializedName("longitude") var longitude: Double? = null,
    @SerializedName("sheet_arrival_time") var sheetArrivalTime: String? = null,
    @SerializedName("sheet_departure_time") var sheetDepartureTime: String? = null,
    @SerializedName("actual_arrival_time") var actualArrivalTime: String? = null,
    @SerializedName("actual_departure_time") var actualDepartureTime: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("notes") var notes: String? = null,
    @SerializedName("driver_note") var driverNote: String? = null,
    @SerializedName("action") var action: String? = null,
    @SerializedName("time") var time: String? = null,
    @SerializedName("label") var label: String? = null,
    @SerializedName("pick") var pick: Boolean? = null,
    @SerializedName("drop") var drop: Boolean? = null,
    @SerializedName("visibility") var visibility: Boolean? = null,
    @SerializedName("is_absent") var isChildAbsent: Int? = 0,
    @SerializedName("label_color") var labelColor:String? =null
): Parcelable

