package com.afjltd.tracking.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_result")
data class TableAPIData (
    @ColumnInfo(name = "api_name") //end point
    var apiName: String,
    @ColumnInfo(name = "api_get_response")
    var apiGetResponse: String,
    @ColumnInfo(name = "api_post_response")
    var apiPostResponse: String,

    @ColumnInfo(name = "api_request")
    var apiRequest: String,
    @ColumnInfo(name = "api_post_data")
    var apiPostData: String,
    @ColumnInfo(name = "api_status")
    var apiStatus: Int =0,
    @ColumnInfo(name = "api_error")
    var apiError: String,
    @ColumnInfo(name="last_time_api_error")
    var lastTimeApiError: String = "",
    @ColumnInfo(name = "api_retry_count")
    var apiRetryCount: Int,
    @ColumnInfo(name = "api_request_time")
    var apiRequestTime: String,
    @ColumnInfo(name = "api_response_time")
    var apiResponseTime: String,
    @ColumnInfo(name = "error_posted_server")
    var errorPosted: String="",
    ) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var Id: Int? = null

}