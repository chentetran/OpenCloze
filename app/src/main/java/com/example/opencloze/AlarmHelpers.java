package com.example.opencloze;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class AlarmHelpers {
    private static final int ALARM_INTENT_REQUEST_CODE = 1;
    private static final int REFRESH_INTERVAL = 24 * 60 * 60 * 1000; // 24 hrs in milliseconds
//    private static final int REFRESH_INTERVAL = 600000;

    public static void setRepeatingAlarm(Context context) {
        Intent alarmIntent = new Intent(context, OpenClozeWidget.class);
        alarmIntent.setAction(OpenClozeWidget.ACTION_ALARM);
        PendingIntent pendingAlarmIntent = PendingIntent.getBroadcast(context, ALARM_INTENT_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Create alarm that fires at midnight everyday
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), REFRESH_INTERVAL, pendingAlarmIntent);

        // TODO create separate  pendingIntent to send to the activity to update activity in real time
    }
}
