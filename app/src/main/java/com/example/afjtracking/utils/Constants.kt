package com.example.afjtracking.utils

import java.util.concurrent.TimeUnit

object Constants {

   const val BASE_URL ="http://vmi808920.contaboserver.net/api/"
   const val FAILED_API_TAG = "vmi808920.contrabass.net"

    //const val BASE_URL = "http://afjdev.hnhtechpk.com/api/"
    //const val FAILED_API_TAG = "afjdev.hnhtechpk.com"

  // const val BASE_URL = "http://192.168.18.69:8000/api/"
 // const val FAILED_API_TAG = "192.168.18.69:8000"


  val WEBSOCKET_URL = "ws://vmi808920.contaboserver.net:6001/video-call?token="
 // val WEBSOCKET_URL = "ws://192.168.18.69:6001/video-call?token="

    var isCallEnded: Boolean = false
    var isIntiatedNow: Boolean = true
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


  const val ACTION_END_CALL = "ACTION_END_CALL"
  const val ACTION_REJECTED_CALL = "ACTION_REJECTED_CALL"
  const val ACTION_HIDE_CALL = "ACTION_HIDE_CALL"
  const val ACTION_SHOW_INCOMING_CALL = "ACTION_SHOW_INCOMING_CALL"
  const val HIDE_NOTIFICATION_INCOMING_CALL = "HIDE_NOTIFICATION_INCOMING_CALL"
  const val ACTION_PRESS_ANSWER_CALL = "ACTION_PRESS_ANSWER_CALL"
  const val ACTION_PRESS_DECLINE_CALL = "ACTION_PRESS_DECLINE_CALL"
  const val ACTION_START_ACTIVITY = "ACTION_START_ACTIVITY"

  //event press answer/decline call
  const val RNNotificationAnswerAction = "RNNotificationAnswerAction"
  const val RNNotificationEndCallAction = "RNNotificationEndCallAction"
  const val onPressNotification = "onPressNotification"

}