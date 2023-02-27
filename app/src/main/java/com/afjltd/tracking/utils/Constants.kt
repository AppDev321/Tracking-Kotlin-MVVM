package com.afjltd.tracking.utils

import java.util.concurrent.TimeUnit

object Constants {

    var BASE_URL = "https://vmi808920.contaboserver.net/api/"
    var FAILED_API_TAG = BASE_URL.replace("https://", "").replace("/api/", "")

    var WEBSOCKET_URL = "ws://vmi808920.contaboserver.net:6001/video-call?token="
    var WEBSOCKET_APP_NAME = "TRACKING"

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

object ErrorCodes{
    const val errorMessage = "Oops! Something went wrong, Please contact to admin (Code:"
    private const val closeTag= ")"
    const val webRTCViewError = "0045$closeTag"
    const val deviceGettingError = "0012$closeTag"
    const val splashLoginButton = "0011$closeTag"
    const val authParsingIssue = "0025$closeTag"


    const val qrNotValid =  "QR Response is not same as per expected, Please contact admin"
    const val qrScanningIssue = "There is some issue in scanning QR Code, Please contact admin"
    const val receiptScanInfo = "Please rescan picture there is an issue in reading data"
    const val receiptScanMsg = "Please match value after scan receipt"

}