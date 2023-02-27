package com.afjltd.tracking.model.responses

import com.google.gson.annotations.SerializedName

data class ApiVersionResponse(

    @SerializedName("code") var code: Int? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: APIData? = APIData(),
    @SerializedName("errors") var errors: ArrayList<Error> = arrayListOf()
)

data class APIData(
    @SerializedName("app_data" ) var appData : ArrayList<AppData> = arrayListOf()
)

data class AppData(
    @SerializedName("id"                ) var id              : Int?    = null,
    @SerializedName("name"              ) var name            : String? = null,
    @SerializedName("app_name"          ) var appName         : String? = null,
    @SerializedName("version"           ) var version         : String? = null,
    @SerializedName("download_url"      ) var downloadUrl     : String? = null,
    @SerializedName("download_url_type" ) var downloadUrlType : String? = null
)