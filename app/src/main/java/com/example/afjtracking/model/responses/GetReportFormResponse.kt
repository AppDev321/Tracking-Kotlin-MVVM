package com.example.afjtracking.model.responses

import com.google.gson.annotations.SerializedName


data class GetReportFormResponse(

    @SerializedName("code") var code: Int? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: ReportData? = ReportData(),
    @SerializedName("errors") var errors: ArrayList<Error> = arrayListOf(),


 )



data class ReportData(

    @SerializedName("report_form"        ) var reportForm       : ArrayList<ReportForm> = arrayListOf(),
    @SerializedName("vehicle"            ) var vehicle          : Vehicle?              = Vehicle(),
    @SerializedName("image_required"     ) var imageRequired    : Boolean?              = null,
    @SerializedName("file_required"      ) var fileRequired     : Boolean?              = null,
    @SerializedName("image_upload_limit" ) var imageUploadLimit : Int?                  = null,
    @SerializedName("file_upload_limit"  ) var fileUploadLimit  : Int?                  = null,
    @SerializedName("request_name"       ) var requestName      : String?               = null


)

data class ReportForm (

    @SerializedName("input_no"   ) var inputNo   : Int?               = null,
    @SerializedName("title"      ) var title     : String?            = null,
    @SerializedName("field_name" ) var fieldName : String?            = null,
    @SerializedName("type"       ) var type      : String?            = null,
    @SerializedName("accept"     ) var accept    : String?            = null,
    @SerializedName("required"   ) var required  : Boolean?           = null,
    @SerializedName("comment"    ) var comment   : String?            = null,
    @SerializedName("value"      ) var value     : String?            = null,
    @SerializedName("options"    ) var options   : ArrayList<Options> = arrayListOf()

)


data class Options (

    @SerializedName("option"     ) var option    : Int?    = null,
    @SerializedName("title"      ) var title     : String? = null,
    @SerializedName("field_name" ) var fieldName : String? = null

)