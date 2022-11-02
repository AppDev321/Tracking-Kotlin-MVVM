package com.example.afjtracking.websocket.listners

interface RTCViewListener {
   fun onEndCall()
   fun onMicClick(isMuted:Boolean)
   fun onVideoCameraClick(isDisableView :Boolean)
   fun onSpeakerClick(isEarPhone: Boolean)

}