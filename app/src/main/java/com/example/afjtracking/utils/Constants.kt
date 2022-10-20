package com.example.afjtracking.utils

import java.util.concurrent.TimeUnit

object Constants {

    // const val BASE_URL ="http://vmi808920.contaboserver.net/api/"
    // const val FAILED_API_TAG = "vmi808920.contaboserver.net"

    const val BASE_URL = "http://afjdev.hnhtechpk.com/api/"
    const val FAILED_API_TAG = "afjdev.hnhtechpk.com"

    const val FIREBASE_QR_TABLE = "qr_table"
    const val FIREBASE_TRACKING_SETTING = "tracking_setting"

    const val WORKER_SERVICE_TIME: Long = 15
    val TIME_WORKER_SERVICE_UNIT = TimeUnit.MINUTES

    var FILE_QUERY_LIMIT = 5
    var API_RETRY_COUNT = 1
    var FILE_UPLOAD_UNIQUE_ID = "" + System.currentTimeMillis()


    var DEVICE_ID: String = ""
    var DEVICE_FCM_TOKEN = ""

    //Location Service Time
    const val SECONDS: Int = 30


    var LOCATION_SERVICE_IN_SECONDS: Long = 1//10 * 1000
    //******************************

    const val dateFormat = "yyyy-MM-dd"
    const val dateTimeFromat = "$dateFormat hh:mm"
    const val dateTimeSecFromat = "$dateFormat hh:mm:ss"
    const val dateTime12HourFromat = "$dateFormat hh:mm aa"
    const val dateTimeSec12HourFromat = "$dateFormat hh:mm:ss aa"


    //Endpoints
    const val DAILY_INSPECTION_CHECKS = "vehicles/daily-inspection/checks"
    const val WEEKLY_INSPECTION_CHECKS = "vehicles/inspections"
    const val LOCATION_API = "update-location"
    const val FILE_UPLOAD_API = "upload"
    const val DAILY_INSPECTION_LIST = "vehicles/daily-inspection/all"
    const val REPORT_FORM = "vehicles/get-reporting-form"

    //Predefined Value of Nullable
    const val NULL_DEFAULT_VALUE = "N/A"


}