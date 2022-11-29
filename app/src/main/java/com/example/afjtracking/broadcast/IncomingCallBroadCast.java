package com.example.afjtracking.broadcast;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import com.example.afjtracking.firebase.HeadsUpNotificationService;
import com.example.afjtracking.view.activity.IncomingCallScreen;
import com.example.afjtracking.view.activity.NavigationDrawerActivity;

public class IncomingCallBroadCast extends BroadcastReceiver {




    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("call service broadcalt","comongggg..........");

        /*Intent iclose = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(iclose);*/

        Intent callIntent =new  Intent(context, IncomingCallScreen.class);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP |Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(callIntent);

    }

}