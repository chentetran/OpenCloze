package com.example.yap;

import static com.example.yap.AlarmHelpers.ACTION_ALARM;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;


public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver alarmActionReceiver;
    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, s) -> {
        Log.d("MainAcitivty", "sharedprefs changed");
        Intent intent = new Intent(getApplicationContext(), YapWidget.class);
        intent.setAction(YapWidget.ACTION_PREFERENCES_UPDATE);
        sendBroadcast(intent);
    };

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPrefs = getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);

        TextView exampleSentence = findViewById(R.id.exampleSentence);
        TextView exampleSentenceTranslation = findViewById(R.id.exampleSentenceTranslation);

        LinearLayout flashcard = findViewById(R.id.flashcard);
        SwitchMaterial showExampleSentenceSwitch = findViewById(R.id.showExampleSentenceSwitch);
        SwitchMaterial showExampleSentenceTranslationSwitch = findViewById(R.id.showExampleSentenceTranslationSwitch);
        Button newSentenceButton = findViewById(R.id.newSentenceButton);

        flashcard.setOnClickListener(view -> flipCard(sharedPrefs, view, exampleSentence,
                exampleSentenceTranslation, showExampleSentenceSwitch, showExampleSentenceTranslationSwitch ));

        setSwitchOnChangeListeners(exampleSentence,
               exampleSentenceTranslation, showExampleSentenceSwitch, showExampleSentenceTranslationSwitch);

        newSentenceButton.setOnClickListener(view -> {
            startFetchSentenceService();
        });

        initializeSentence();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void initializeSentence() {
        SharedPreferences sharedPrefs = getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
        String exampleSentenceString = sharedPrefs.getString("exampleSentence", "");
        String exampleSentenceTranslationString = sharedPrefs.getString("exampleSentenceTranslation", "");

        if (exampleSentenceString.isEmpty() || exampleSentenceTranslationString.isEmpty()) {
            startFetchSentenceService();
        }
    }

    private void registerAlarmActionReceiver() {
        alarmActionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateSentence();
            }
        };

        IntentFilter filter = new IntentFilter(ACTION_ALARM);
        registerReceiver(alarmActionReceiver, filter);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void startFetchSentenceService() {
        Intent startFetchSentenceServiceIntent = new Intent(this, FetchSentenceService.class);
        startService(startFetchSentenceServiceIntent);
        AlarmHelpers.setRepeatingAlarm(this);
    }

    public void updateSentence() {
        SharedPreferences sharedPrefs = getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
        String exampleSentenceString = sharedPrefs.getString("exampleSentence", "");
        String exampleSentenceTranslationString = sharedPrefs.getString("exampleSentenceTranslation", "");

        TextView exampleSentence = findViewById(R.id.exampleSentence);
        TextView exampleSentenceTranslation = findViewById(R.id.exampleSentenceTranslation);

        exampleSentence.setText(exampleSentenceString);
        exampleSentenceTranslation.setText(exampleSentenceTranslationString);
    }

    private void flipCard(SharedPreferences sharedPrefs, View flashcard, TextView exampleSentence, TextView exampleSentenceTranslation,
                          SwitchMaterial showExampleSentenceSwitch, SwitchMaterial showExampleSentenceTranslationSwitch) {
        boolean isCardFront = sharedPrefs.getBoolean("isCardFront", true);
        sharedPrefs.edit().putBoolean("isCardFront", !isCardFront).apply();
        applySettings();
    }

    private void setSwitchOnChangeListeners(TextView exampleSentence, TextView exampleSentenceTranslation,
                                         SwitchMaterial showExampleSentenceSwitch, SwitchMaterial showExampleSentenceTranslationSwitch) {
        showExampleSentenceSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            SharedPreferences sharedPrefs = getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
            String keyPrefix = sharedPrefs.getBoolean("isCardFront", true) ? "front:" : "back:";
            sharedPrefs.edit().putBoolean(keyPrefix + "showExampleSentence", b).apply();
            exampleSentence.setVisibility(b ? View.VISIBLE : View.GONE);
        });
        showExampleSentenceTranslationSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            SharedPreferences sharedPrefs = getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
            String keyPrefix = sharedPrefs.getBoolean("isCardFront", true) ? "front:" : "back:";
            sharedPrefs.edit().putBoolean(keyPrefix + "showExampleSentenceTranslation", b).apply();
            exampleSentenceTranslation.setVisibility(b ? View.VISIBLE : View.GONE);
        });
    }

    private void applySettings() {
        TextView exampleSentence = findViewById(R.id.exampleSentence);
        TextView exampleSentenceTranslation = findViewById(R.id.exampleSentenceTranslation);

        LinearLayout flashcard = findViewById(R.id.flashcard);
        SwitchMaterial showExampleSentenceSwitch = findViewById(R.id.showExampleSentenceSwitch);
        SwitchMaterial showExampleSentenceTranslationSwitch = findViewById(R.id.showExampleSentenceTranslationSwitch);
        SharedPreferences sharedPrefs = getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
        String keyPrefix = sharedPrefs.getBoolean("isCardFront", true) ? "front:" : "back:";

        if (sharedPrefs.getBoolean("isCardFront", true)) {
            flashcard.setBackground(getDrawable(R.drawable.layout_card_bg_lite));
            exampleSentence.setTextColor(getResources().getColor(R.color.black));
            exampleSentenceTranslation.setTextColor(getResources().getColor(R.color.black));
        } else {
            flashcard.setBackground(getDrawable(R.drawable.layout_card_bg));
            exampleSentence.setTextColor(getResources().getColor(R.color.white));
            exampleSentenceTranslation.setTextColor(getResources().getColor(R.color.white));
        }

        boolean showExampleSentenceSharedPref = sharedPrefs.getBoolean(keyPrefix + "showExampleSentence", true);
        showExampleSentenceSwitch.setChecked(showExampleSentenceSharedPref);
        exampleSentence.setVisibility(showExampleSentenceSharedPref ? View.VISIBLE : View.GONE);

        boolean showExampleSentenceTranslationSharedPref = sharedPrefs.getBoolean(keyPrefix + "showExampleSentenceTranslation", true);
        showExampleSentenceTranslationSwitch.setChecked(showExampleSentenceTranslationSharedPref);
        exampleSentenceTranslation.setVisibility(showExampleSentenceTranslationSharedPref ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onStart() {
        SharedPreferences sharedPrefs = getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
        super.onStart();
        updateSentence();
        applySettings();
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener);
        registerAlarmActionReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPrefs = getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener);
        unregisterReceiver(alarmActionReceiver);
    }
}