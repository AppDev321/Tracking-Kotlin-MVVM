package com.afjltd.tracking.model.responses

import com.google.gson.annotations.SerializedName


data class NotificationDataResponse(

    @SerializedName("code"    ) var code    : Int?              = null,
    @SerializedName("message" ) var message : String?           = null,
    @SerializedName("data"    ) var data    : DataNotification?             = DataNotification(),
    @SerializedName("errors"  ) var errors  : List<Error> = arrayListOf()

)
data class NotificationData (

    @SerializedName("body"     ) var body     : String? = null,
    @SerializedName("type"     ) var type     : String? = null,
    @SerializedName("title"    ) var title    : String? = null,
    @SerializedName("activity" ) var activity : String? = null,


)

data class Notifications (

    @SerializedName("id"                ) var id               : Int?              = null,
    @SerializedName("is_read"           ) var isRead           : Int?              = null,
    @SerializedName("type"              ) var type             : String?           = null,
    @SerializedName("severity"          ) var severity         : String?           = null,
    @SerializedName("notification_data" ) var notificationData : NotificationData? = NotificationData(),
    @SerializedName("created_at"        ) var createdAt        : String?           = null

)



data class DataNotification (

    @SerializedName("message"       ) var message       : String?                  = null,
    @SerializedName("notifications" ) var notifications : List<Notifications> = arrayListOf()

)