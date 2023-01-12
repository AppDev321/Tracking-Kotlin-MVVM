package com.example.afjtracking.callscreen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.example.afjtracking.utils.AFJUtils


/** CallkitIncomingPlugin */
class CallkitIncomingPlugin {

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var instance: CallkitIncomingPlugin? = null

         fun getInstance(): CallkitIncomingPlugin  {
            if(instance == null){
                instance = CallkitIncomingPlugin()
            }
            return instance!!
        }
        var eventHandler : EventListener? = null
        fun setEventCallListener(eventHandler : EventListener)
        {
            CallkitIncomingPlugin.eventHandler = eventHandler
        }
        fun sendEvent(event: String, body: Map<String, Any>) {
            eventHandler?.send(event, body)
        }




    }

    private var activity: Activity? = null
    private var context: Context? = null
    private var callNotificationManager: CallNotificationManager? = null





     fun showIncomingNotification(callName:String,isAudioCall:Boolean,context: Context?) {

         val param= hashMapOf<String,Any>(
             "id" to AFJUtils.getCurrentDateTime(),
             "nameCaller" to callName,
             "appName" to "AFJ App",
             "avatar" to "https://i.pravatar.cc/100",
             "handle" to "Incoming Call",
             "type" to if(isAudioCall) 0 else 1,
             "duration" to 1000 * 60,
             "textAccept" to "Accept",
             "textDecline" to "Decline",
             "textMissedCall" to "Missed call",
             "textCallback" to "Call back",
             "android" to hashMapOf<String,Any>(
                 "isCustomNotification" to true,
                 "isShowLogo" to false,
                 "isShowCallback" to false,
                 "isShowMissedCallNotification" to true,
                 "ringtonePath" to "system_ringtone_default",
                 "backgroundColor" to "#0955fa",
                 "backgroundUrl" to "https://i.pravatar.cc/500",
                 "actionColor" to "#4CAF50"

             ),
         )
         val data = Data(param)
        data.from = "notification"
        callNotificationManager?.showIncomingNotification(data.toBundle())
        //send BroadcastReceiver
        context?.sendBroadcast(
            CallIncomingBroadcastReceiver.getIntentIncoming(
                requireNotNull(context),
                data.toBundle()
            )
        )
    }

     fun showMissCallNotification(data: Data) {
        callNotificationManager?.showIncomingNotification(data.toBundle())
    }

     fun startCall(data: Data) {
        context?.sendBroadcast(
            CallIncomingBroadcastReceiver.getIntentStart(
                requireNotNull(context),
                data.toBundle()
            )
        )
    }

     fun endCall(data: Data) {
        context?.sendBroadcast(
            CallIncomingBroadcastReceiver.getIntentEnded(
                requireNotNull(context),
                data.toBundle()
            )
        )
    }

     fun endAllCalls() {
        val calls = getDataActiveCalls(context)
         AFJUtils.writeLogs("call status == ${calls}")

        calls.forEach {
            context?.sendBroadcast(
                CallIncomingBroadcastReceiver.getIntentEnded(
                    requireNotNull(context),
                    it.toBundle()
                )

            )

        }
        removeAllCalls(context)
    }


     fun onMethodCall( call: String) {
        try {
            when (call) {
                "showCallkitIncoming" -> {
                    val data = Data( HashMap<String, Any?>())
                   // data.from = "notification"
                    //send BroadcastReceiver
                    context?.sendBroadcast(
                        CallIncomingBroadcastReceiver.getIntentIncoming(
                            requireNotNull(context),
                            data.toBundle()
                        )
                    )

                }
                "showMissCallNotification" -> {
                    val data = Data( HashMap<String, Any?>())
                   // data.from = "notification"
                    callNotificationManager?.showMissCallNotification(data.toBundle())

                }
                "startCall" -> {
                    val data = Data( HashMap<String, Any?>())
                    context?.sendBroadcast(
                        CallIncomingBroadcastReceiver.getIntentStart(
                            requireNotNull(context),
                            data.toBundle()
                        )
                    )

                }
                "endCall" -> {
                    val data = Data( HashMap<String, Any?>())
                    context?.sendBroadcast(
                        CallIncomingBroadcastReceiver.getIntentEnded(
                            requireNotNull(context),
                            data.toBundle()
                        )
                    )

                }
                "endAllCalls" -> {
                    val calls = getDataActiveCalls(context)
                    calls.forEach {
                        if(it.isAccepted) {
                            context?.sendBroadcast(
                                CallIncomingBroadcastReceiver.getIntentEnded(
                                    requireNotNull(context),
                                    it.toBundle()
                                )
                            )
                        }else {
                            context?.sendBroadcast(
                                CallIncomingBroadcastReceiver.getIntentDecline(
                                    requireNotNull(context),
                                    it.toBundle()
                                )
                            )
                        }
                    }
                    removeAllCalls(context)

                }
                "activeCalls" -> {
                  //  result.success(getDataActiveCallsForFlutter(context))
                }
                "getDevicePushTokenVoIP" -> {
                 //   result.success("")
                }
            }
        } catch (error: Exception) {
         //   result.error("error", error.message, "")
        }
    }



}

    open class  EventListener : CallEventListener{
    override fun onListen() {
    }

    override fun send(event: String, body: Map<String, Any>) {
    }

    override fun onCancel(arguments: Any?) {
    }

}