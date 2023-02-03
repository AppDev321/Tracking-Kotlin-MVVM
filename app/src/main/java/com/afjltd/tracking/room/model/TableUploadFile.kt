package com.afjltd.tracking.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upload_file")
data class TableUploadFile(
    @ColumnInfo(name = "file_type")
    var fileType: String,
    @ColumnInfo(name = "type")
    var type: String,
    @ColumnInfo(name = "extension")
    var extention: String,
    @ColumnInfo(name = "file_path")
    var filePath: String,
    @ColumnInfo(name = "upload_id")
    var uploadID: String,
    @ColumnInfo(name = "field_name")
    var fieldName: String,
    @ColumnInfo(name = "upload_status")
    var uploadStatus: Int =0,
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

    @ColumnInfo(name="file_size")
    var fileSize: String = "",
    @ColumnInfo(name = "error_posted_server")
    var errorPosted: String="",
    ) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var Id: Int? = null

}