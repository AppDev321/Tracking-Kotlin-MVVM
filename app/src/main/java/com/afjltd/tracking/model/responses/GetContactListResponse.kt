package com.afjltd.tracking.model.responses

import com.google.gson.annotations.SerializedName

class GetContactListResponse
    (
    @SerializedName("code") var code: Int? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: ContactListData? = ContactListData(),
    @SerializedName("errors") var errors: ArrayList<Error> = arrayListOf()

)
data class ContactListData (

    @SerializedName("contacts") var contactUserList : ArrayList<User> = arrayListOf(),

)

