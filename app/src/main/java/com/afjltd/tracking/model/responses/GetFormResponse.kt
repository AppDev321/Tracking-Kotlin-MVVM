package com.afjltd.tracking.model.responses

import com.google.gson.annotations.SerializedName


data class GetFormResponse(

    @SerializedName("code") var code: Int? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: FormData? = FormData(),
    @SerializedName("errors") var errors: ArrayList<Error> = arrayListOf(),


    )


data class FormData(

    @SerializedName("form") var formList: ArrayList<Form> = arrayListOf(),
    @SerializedName("vehicle") var vehicle: Vehicle? = Vehicle(),
    @SerializedName("image_required") var imageRequired: Boolean? = null,
    @SerializedName("file_required") var fileRequired: Boolean? = null,
    @SerializedName("image_upload_limit") var imageUploadLimit: Int? = null,
    @SerializedName("file_upload_limit") var fileUploadLimit: Int? = null,
    @SerializedName("request_name") var requestName: String? = null,
    @SerializedName("form_name") var formName: String? = null

)
