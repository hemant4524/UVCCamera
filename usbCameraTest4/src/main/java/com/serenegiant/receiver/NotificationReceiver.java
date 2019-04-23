package com.serenegiant.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.serenegiant.constants.AppConstant;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (AppConstant.START_ACTION.equals(action)) {
            Toast.makeText(context, "YES CALLED", Toast.LENGTH_SHORT).show();
        } else if (AppConstant.STOP_ACTION.equals(action)) {
            Toast.makeText(context, "STOP CALLED", Toast.LENGTH_SHORT).show();
        }
    }
}