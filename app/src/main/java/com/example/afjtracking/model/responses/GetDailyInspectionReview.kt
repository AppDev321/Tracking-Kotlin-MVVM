package com.example.afjtracking.model.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


data class GetDailyInspectionReview(

    @SerializedName("code") var code: Int? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: InspectionReviewData? = InspectionReviewData(),
    @SerializedName("errors") var errors: ArrayList<Error> = arrayListOf(),


    )



@Parcelize
data class InspectionReviewData(

    @SerializedName("check") var checks: ArrayList<Checks> = arrayListOf(),
    @SerializedName("vehicle") var vehicle: Vehicle? = Vehicle(),
   @SerializedName("inspection") var inspection: Inspection? = Inspection(),

) : Parcelable

