package com.example.yap;

import static com.example.yap.YapWidget.ACTION_WIDGET_CLICK;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context context;
    private final ArrayList<Map<String, String>> itemList = new ArrayList<>();

    public WidgetRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;
        getSentencesFromSharedPrefs();
    }

    private void getSentencesFromSharedPrefs() {
        SharedPreferences sharedPrefs = context.getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
        String exampleSentence = sharedPrefs.getString("exampleSentence", "");
        String exampleTranslation = sharedPrefs.getString("exampleSentenceTranslation", "");

        Map<String, String> sentencesMap = new HashMap<>();
        sentencesMap.put("exampleSentence", exampleSentence);
        sentencesMap.put("exampleTranslation", exampleTranslation);
        itemList.add(sentencesMap);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        Log.d("data", "changed");
        itemList.clear();
        getSentencesFromSharedPrefs();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        Log.d("factory", "getCount() called, itemCount: " + itemList.size());
        return itemList.size();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews remoteViews;

        SharedPreferences sharedPrefs = context.getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
        boolean isCardFront = sharedPrefs.getBoolean("isCardFront", true);
        String keyPrefix = isCardFront ? "front:" : "back:";
        boolean showExampleSentence = sharedPrefs.getBoolean(keyPrefix + "showExampleSentence", true);
        boolean showSentenceTranslation = sharedPrefs.getBoolean(keyPrefix + "showExampleSentenceTranslation", true);

        Log.d("getviewAt ", keyPrefix);

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_list_item_example_sentence);
        remoteViews.setTextViewText(R.id.exampleSentence, itemList.get(i).get("exampleSentence"));
        remoteViews.setTextViewText(R.id.exampleSentenceTranslation, itemList.get(i).get("exampleTranslation"));

        remoteViews.setViewVisibility(R.id.exampleSentence, showExampleSentence ? View.VISIBLE : View.GONE);
        remoteViews.setViewVisibility(R.id.exampleSentenceTranslation, showSentenceTranslation ? View.VISIBLE : View.GONE);

        if (isCardFront) {
            remoteViews.setTextColor(R.id.exampleSentence, context.getResources().getColor(R.color.black));
            remoteViews.setTextColor(R.id.exampleSentenceTranslation, context.getResources().getColor(R.color.black));
        } else {
            remoteViews.setTextColor(R.id.exampleSentence, context.getResources().getColor(R.color.white));
            remoteViews.setTextColor(R.id.exampleSentenceTranslation, context.getResources().getColor(R.color.white));
        }

        Intent clickIntent = new Intent(context, YapWidget.class);
        clickIntent.setAction(ACTION_WIDGET_CLICK);
        remoteViews.setOnClickFillInIntent(R.id.exampleSentenceLayout, clickIntent);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
