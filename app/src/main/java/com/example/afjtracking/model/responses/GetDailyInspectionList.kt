package com.example.afjtracking.model.responses

import com.google.gson.annotations.SerializedName

class GetDailyInspectionList
    (
    @SerializedName("code") var code: Int? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: InspectionDataList? = InspectionDataList(),
    @SerializedName("errors") var errors: ArrayList<Error> = arrayListOf()

)
data class InspectionDataList (

    @SerializedName("vehicle" ) var vehicle : Vehicle? = Vehicle(),
    @SerializedName("start"   ) var start   : Int?     = null,
    @SerializedName("limit"   ) var limit   : Int?     = null,
    @SerializedName("inspections") var inspections : ArrayList<Inspections> = arrayListOf(),

)


data class Inspections (

    @SerializedName("id"           ) var id          : Int?    = null,
    @SerializedName("employee_id"  ) var employeeId  : Int?    = null,
    @SerializedName("vehicle_id"   ) var vehicleId   : Int?    = null,
    @SerializedName("date"         ) var date        : String? = null,
    @SerializedName("vehicle_type" ) var vehicleType : String? = null,
    @SerializedName("status"       ) var status      : String? = null,
    @SerializedName("is_read"      ) var isRead      : Int?    = null,
    @SerializedName("input_status" ) var inputStatus : String? = null,
    @SerializedName("check_status" ) var checkStatus : String? = null,
    @SerializedName("upload_id"    ) var uploadId    : String? = null,
    @SerializedName("source"       ) var source      : String? = null,
    @SerializedName("created_at"   ) var createdAt   : String? = null,
    @SerializedName("updated_at"   ) var updatedAt   : String? = null,
    @SerializedName("count") var count: String? = null,

)