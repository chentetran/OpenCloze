package com.example.yap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AlarmHelpers {
    public static final String ACTION_ALARM = "ActionAlarm";
    public static final int START_FETCH_SENTENCE_INTENT_REQUEST_CODE = 20002;
    private static boolean alarmUp = false;

    @RequiresApi(api = Build.VERSION_CODES.S)
    public static boolean isAlarmSet() {
        return alarmUp;
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static int getRefreshIntervalInMs(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
        String refreshIntervalKey = sharedPrefs.getString("refreshInterval", "1 hour");

        String[] keys = context.getResources().getStringArray(R.array.refresh_interval_options_keys);
        int[] values = context.getResources().getIntArray(R.array.refresh_interval_options_in_ms_values);

        Map<String, Integer> refreshIntervalKeyToIntMap = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            refreshIntervalKeyToIntMap.put(keys[i], values[i]);
        }

        return refreshIntervalKeyToIntMap.getOrDefault(refreshIntervalKey, 3600000);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public static void setRepeatingAlarm(Context context) {
        Intent startFetchSentenceIntent = new Intent(context, FetchSentenceService.class);

        // Create alarm that fires at midnight everyday
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int refreshInterval = getRefreshIntervalInMs(context);

        long triggerAtMillis = System.currentTimeMillis() + refreshInterval;

        alarmUp = (PendingIntent.getService(context, START_FETCH_SENTENCE_INTENT_REQUEST_CODE, startFetchSentenceIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null);

        if (!alarmUp) {
            PendingIntent pendingStartFetchSentenceIntent = PendingIntent.getService(context, START_FETCH_SENTENCE_INTENT_REQUEST_CODE, startFetchSentenceIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.setRepeating(AlarmManager.RTC, triggerAtMillis, refreshInterval, pendingStartFetchSentenceIntent);
            alarmUp = true;
        }
    }
}
