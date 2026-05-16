package com.example.nthabelengmolaoli2333784;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("TITLE");
        String message = intent.getStringExtra("MSG");

        NotificationHelper helper = new NotificationHelper(context);
        helper.sendInstantNotification(title, message);
    }
}
