package com.example.yap;

import static com.example.yap.AlarmHelpers.ACTION_ALARM;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FetchSentenceService extends Service {
    public FetchSentenceService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("fetch sentence service", "try");

        String url = "https://yap-backend-production.up.railway.app/";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        Log.d("fetch sentence service", response);
                        JSONObject responseJson = new JSONObject(response);
                        String exampleSentenceString = responseJson.getString("target_lang");
                        String exampleSentenceTranslationString = responseJson.getString("native_lang");

                        SharedPreferences sharedPrefs = getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.putString("exampleSentence", exampleSentenceString);
                        editor.putString("exampleSentenceTranslation", exampleSentenceTranslationString);
                        editor.apply();

                        Intent alarmWidgetIntent = new Intent(this, YapWidget.class);
                        alarmWidgetIntent.setAction(ACTION_ALARM);
                        sendBroadcast(alarmWidgetIntent);

//                        Intent alarmActivityIntent = new Intent(this, MainActivityAlarmBroadcastReceiver.class);
//                        alarmActivityIntent.setAction(ACTION_ALARM);
//                        sendBroadcast(alarmActivityIntent);

                        Intent alarmIntent = new Intent(ACTION_ALARM);
                        sendBroadcast(alarmIntent);

                        stopSelf();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        stopSelf();
                    }
                },
                error -> Log.d("real error", error.getMessage())
        ) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                SharedPreferences sharedPrefs = getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
                String targetLang = sharedPrefs.getString("targetLang", "");
                String nativeLang = sharedPrefs.getString("nativeLang", "");
                String cefrLevel = sharedPrefs.getString("cefrLevel", "A1");

                Map<String, String> params = new HashMap<>();
                params.put("cefr_level", cefrLevel);
                params.put("target_language", targetLang);
                params.put("native_language", nativeLang);
                return params;
            }
        };

        requestQueue.add(stringRequest);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}