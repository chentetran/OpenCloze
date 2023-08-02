package com.example.yap;

import static com.example.yap.AlarmHelpers.ACTION_ALARM;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;

public class YapWidget extends AppWidgetProvider {
    public static final String ACTION_WIDGET_CLICK = "ActionWidgetClick";
    public static final String ACTION_PREFERENCES_UPDATE = "ActionPreferencesUpdate";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.yap_widget);
        SharedPreferences sharedPrefs = context.getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);

        if (intent.getAction().equals(ACTION_WIDGET_CLICK)) {
            Log.d("onreceive", "click");
            boolean isCardFront = sharedPrefs.getBoolean("isCardFront", true);

            applySettings(context, views);

            sharedPrefs.edit().putBoolean("isCardFront", !isCardFront).apply();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            appWidgetManager.updateAppWidget(new ComponentName(context, YapWidget.class), views);
        }

        else if (intent.getAction().equals(ACTION_PREFERENCES_UPDATE)) {
            Log.d("onreceive", "preferences updated");
            applySettings(context, views);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.updateAppWidget(new ComponentName(context, YapWidget.class), views);
        }

        else if (intent.getAction().equals(ACTION_ALARM)) {
            Log.d("onreceive", "alarm");
            applySettings(context, views);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.updateAppWidget(new ComponentName(context, YapWidget.class), views);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.yap_widget);

            applySettings(context, views);

            Intent onItemClickIntent = new Intent(context, YapWidget.class);
            onItemClickIntent.setAction(ACTION_WIDGET_CLICK);
            PendingIntent onItemClickPendingIntent = PendingIntent.getBroadcast(
                    context,
                    99999,
                    onItemClickIntent,
                    PendingIntent.FLAG_IMMUTABLE
            );
            views.setPendingIntentTemplate(R.id.listView, onItemClickPendingIntent);

            // Load sentences as listView
            Intent widgetRemoveViewsServiceIntent = new Intent(context, WidgetRemoteViewsService.class);
            views.setRemoteAdapter(R.id.listView, widgetRemoveViewsServiceIntent);




//            // Set click listener on widget
//            Intent clickIntent = new Intent(context, YapWidget.class);
//            clickIntent.setAction(ACTION_WIDGET_CLICK);
//            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context, 10001, clickIntent, PendingIntent.FLAG_MUTABLE);
//            views.setOnClickPendingIntent(R.id.widgetLayout, clickPendingIntent);


            appWidgetManager.updateAppWidget(appWidgetId, views);
//            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listView);
        }

        // Start the AlarmManager countdown
        AlarmHelpers.setRepeatingAlarm(context);
    }
    


    private void applySettings(Context context, RemoteViews views) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
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
        SharedPreferences sharedPrefs = context.getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
        String exampleSentenceString = sharedPrefs.getString("exampleSentence", "");
        String exampleSentenceTranslationString = sharedPrefs.getString("exampleSentenceTranslation", "");

        if (exampleSentenceString.isEmpty() || exampleSentenceTranslationString.isEmpty()) {
            Log.d("here", "here");
            Intent serviceIntent = new Intent(context, FetchSentenceService.class);
            context.startService(serviceIntent);
        }
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}