package com.example.yap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.Calendar;

public class AlarmHelpers {
    public static final String ACTION_ALARM = "ActionAlarm";
    public static final int START_FETCH_SENTENCE_INTENT_REQUEST_CODE = 20002;
//    private static final int REFRESH_INTERVAL = 24 * 60 * 60 * 1000; // 24 hrs in milliseconds
    private static final int REFRESH_INTERVAL = 600000; // 10 minutes
    private static boolean alarmUp = false;

    @RequiresApi(api = Build.VERSION_CODES.S)
    public static boolean isAlarmSet() {
        return alarmUp;
    };

    @RequiresApi(api = Build.VERSION_CODES.S)
    public static void setRepeatingAlarm(Context context) {
        Intent startFetchSentenceIntent = new Intent(context, FetchSentenceService.class);

        // Create alarm that fires at midnight everyday
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), REFRESH_INTERVAL, pendingStartFetchSentenceIntent);

        // Temporary, to see if it works every 10 minutes.
        long triggerAtMillis = System.currentTimeMillis() + REFRESH_INTERVAL;

        alarmUp = (PendingIntent.getService(context, START_FETCH_SENTENCE_INTENT_REQUEST_CODE, startFetchSentenceIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null);
        Log.d("alarmhelpers", "alarmup? " + alarmUp);

        if (!alarmUp) {
            PendingIntent pendingStartFetchSentenceIntent = PendingIntent.getService(context, START_FETCH_SENTENCE_INTENT_REQUEST_CODE, startFetchSentenceIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.setRepeating(AlarmManager.RTC, triggerAtMillis, REFRESH_INTERVAL, pendingStartFetchSentenceIntent);
            alarmUp = true;
        }

        Log.d("alarmhelpers", "alarmup2? " + alarmUp);
    }
}
