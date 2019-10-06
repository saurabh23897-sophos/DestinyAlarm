package com.example.destinyalarm;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import static androidx.legacy.content.WakefulBroadcastReceiver.startWakefulService;
import static com.example.destinyalarm.Utils.Constants.ALARM_MESSAGE;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, ALARM_MESSAGE, Toast.LENGTH_SHORT).show();

        //Stop sound service to play sound for alarm
        context.startService(new Intent(context, AlarmSoundService.class));

        //This will send a notification message and show notification in notification tray
        ComponentName comp = new ComponentName(context.getPackageName(),
                AlarmNotificationService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
    }
}