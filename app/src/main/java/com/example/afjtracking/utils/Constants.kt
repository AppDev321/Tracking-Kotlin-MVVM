package com.example.afjtracking.utils

import java.util.concurrent.TimeUnit

object Constants {
  //  const val BASE_URL ="http://vmi808920.contaboserver.net/api/"
  //const val FAILED_API_TAG = "vmi808920.contaboserver.net"

    const val BASE_URL ="http://192.168.1.6/api/"
   const val FAILED_API_TAG = "192.168.1.6"

    const val WORKER_SERVICE_TIME: Long = 15
    val TIME_WORKER_SERVICE_UNIT = TimeUnit.MINUTES

    var FILE_QUERY_LIMIT = 5
    var API_RETRY_COUNT = 1
    var FILE_UPLOAD_UNIQUE_ID = "" + System.currentTimeMillis()


    var DEVICE_ID : String = ""
    var DEVICE_FCM_TOKEN =""

    //Location Service Time
    val SECONDS: Int = 30
  //  var LOCATION_SERVICE_IN_SECONDS = (1 * SECONDS * 1000).toLong()

    var LOCATION_SERVICE_IN_SECONDS :Long = 0
    //******************************

    const val dateFormat = "yyyy-MM-dd"
    const val dateTimeFromat = "$dateFormat hh:mm"
    const val dateTimeSecFromat = "$dateFormat hh:mm:ss"
    const val dateTime12HourFromat = "$dateFormat hh:mm aa"
    const val dateTimeSec12HourFromat = "$dateFormat hh:mm:ss aa"



    //Endpoints
    val DAILY_INSPECTION_CHECKS = "vehicles/daily-inspection/checks"
    val WEEKLY_INSPECTION_CHECKS = "vehicles/inspections"
    val LOCATION_API = "update-location"
    val FILE_UPLOAD_API = "upload"
    val DAILY_INSPECTION_LIST= "vehicles/daily-inspection/all"
    val REPORT_FORM= "vehicles/get-reporting-form"

    //Predefined Value of Nullable
    val NULL_DEFAULT_VALUE= "N/A"
}