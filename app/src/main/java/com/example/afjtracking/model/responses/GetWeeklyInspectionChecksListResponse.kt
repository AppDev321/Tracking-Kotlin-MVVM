package com.example.afjtracking.model.responses

import com.example.afjtracking.model.requests.SavedInspection
import com.google.gson.annotations.SerializedName


data class GetWeeklyInspectionChecksListResponse(

    @SerializedName("code") var code: Int? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: WeeklyInspectionCheckData? = WeeklyInspectionCheckData(),
    @SerializedName("errors") var errors: ArrayList<Error> = arrayListOf(),


    )


data class WeeklyInspectionCheckData (

    @SerializedName("vehicle"     ) var vehicle    : Vehicle?           = Vehicle(),
    @SerializedName("total_count" ) var totalCount : Int?               = null,
    @SerializedName("inspection"  ) var inspection : Inspection?        = Inspection(),
    @SerializedName("options"     ) var options    : ArrayList<RadioCheckOption> = arrayListOf(),
    @SerializedName("checks"      ) var checks     : ArrayList<WeeklyInspectionCheck>  = arrayListOf()

)

data class WeeklyInspectionCheck (

    @SerializedName("id"                ) var id               : Int?              = null,
    @SerializedName("check_no"          ) var checkNo          : Int?              = null,
    @SerializedName("name"              ) var name             : String?           = null,
    @SerializedName("type"              ) var type             : String?           = null,
    @SerializedName("created_at"        ) var createdAt        : String?           = null,
    @SerializedName("updated_at"        ) var updatedAt        : String?           = null,
    @SerializedName("im_ref"            ) var imRef            : String?           = null,
    @SerializedName("saved_inspections" ) var savedInspections : ArrayList<SavedInspection> = arrayListOf()

)

data class RadioCheckOption (

    @SerializedName("id"    ) var id    : String? = null,
    @SerializedName("value" ) var value : String? = null

)




