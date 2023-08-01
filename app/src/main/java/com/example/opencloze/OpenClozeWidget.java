package com.example.opencloze;

import static com.example.opencloze.AlarmHelpers.ACTION_ALARM;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implementation of App Widget functionality.
 */
public class OpenClozeWidget extends AppWidgetProvider {
    public static final String ACTION_WIDGET_CLICK = "ActionWidgetClick";
    public static final String ACTION_PREFERENCES_UPDATE = "ActionPreferencesUpdate";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.open_cloze_widget);
        SharedPreferences sharedPrefs = context.getSharedPreferences("com.example.opencloze", Context.MODE_PRIVATE);

        if (intent.getAction().equals(ACTION_WIDGET_CLICK)) {
            Log.d("onreceive", "click");
            boolean isCardFront = sharedPrefs.getBoolean("isCardFront", true);

            applySettings(context, views);

            sharedPrefs.edit().putBoolean("isCardFront", !isCardFront).apply();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.updateAppWidget(new ComponentName(context, OpenClozeWidget.class), views);
        }

        else if (intent.getAction().equals(ACTION_PREFERENCES_UPDATE)) {
            Log.d("onreceive", "preferences updated");
            applySettings(context, views);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.updateAppWidget(new ComponentName(context, OpenClozeWidget.class), views);
        }

        else if (intent.getAction().equals(ACTION_ALARM)) {
            Log.d("onreceive", "alarm");
            applySettings(context, views);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.updateAppWidget(new ComponentName(context, OpenClozeWidget.class), views);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.open_cloze_widget);

            applySettings(context, views);

            // Set click listener on widget
            Intent clickIntent = new Intent(context, OpenClozeWidget.class);
            clickIntent.setAction(ACTION_WIDGET_CLICK);
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_MUTABLE);
            views.setOnClickPendingIntent(R.id.widgetLayout, clickPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        // Start the AlarmManager countdown
        AlarmHelpers.setRepeatingAlarm(context);
    }

    private void applySettings(Context context, RemoteViews views) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("com.example.opencloze", Context.MODE_PRIVATE);
        String keyPrefix = sharedPrefs.getBoolean("isCardFront", true) ? "front:" : "back:";

        views.setTextViewText(R.id.exampleSentence, sharedPrefs.getString("exampleSentence", ""));
        views.setTextViewText(R.id.exampleSentenceTranslation, sharedPrefs.getString("exampleSentenceTranslation", ""));

        if (sharedPrefs.getBoolean("isCardFront", true)) {
            views.setInt(R.id.widgetLayout, "setBackgroundResource", R.drawable.layout_card_bg_lite);
            views.setTextColor(R.id.exampleSentence, context.getResources().getColor(R.color.black));
            views.setTextColor(R.id.exampleSentenceTranslation, context.getResources().getColor(R.color.black));
        } else {
            views.setInt(R.id.widgetLayout, "setBackgroundResource", R.drawable.layout_card_bg);
            views.setTextColor(R.id.exampleSentence, context.getResources().getColor(R.color.white));
            views.setTextColor(R.id.exampleSentenceTranslation, context.getResources().getColor(R.color.white));
        }

        boolean showExampleSentenceSharedPref = sharedPrefs.getBoolean(keyPrefix + "showExampleSentence", true);
        views.setViewVisibility(R.id.exampleSentence, showExampleSentenceSharedPref ? View.VISIBLE : View.GONE);

        boolean showExampleSentenceTranslationSharedPref = sharedPrefs.getBoolean(keyPrefix + "showExampleSentenceTranslation", true);
        views.setViewVisibility(R.id.exampleSentenceTranslation, showExampleSentenceTranslationSharedPref ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}