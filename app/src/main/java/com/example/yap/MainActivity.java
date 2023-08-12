package com.example.yap;

import static com.example.yap.AlarmHelpers.ACTION_ALARM;
import static com.example.yap.AlarmHelpers.isAlarmSet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver alarmActionReceiver;
    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, s) -> {
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

        initializeSentence();

        flashcard.setOnClickListener(view -> flipCard(sharedPrefs, view, exampleSentence,
                exampleSentenceTranslation, showExampleSentenceSwitch, showExampleSentenceTranslationSwitch ));

        setSwitchOnChangeListeners(exampleSentence,
               exampleSentenceTranslation, showExampleSentenceSwitch, showExampleSentenceTranslationSwitch);

        newSentenceButton.setOnClickListener(view -> {
            startFetchSentenceService();
        });

        populateSpinners();
    }

    private void populateSpinners() {
        SharedPreferences sharedPrefs = getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
        String targetLangSetting = sharedPrefs.getString("targetLang", "");
        String nativeLangSetting = sharedPrefs.getString("nativeLang", "");
        String cefrLvl = sharedPrefs.getString("cefrLevel", "A1");
        String refreshInterval = sharedPrefs.getString("refreshInterval", "");

        Spinner targetLanguageSpinner = findViewById(R.id.targetLanguageSpinner);
        ArrayAdapter<CharSequence> targetLangsAdapter = ArrayAdapter.createFromResource(this, R.array.target_language_options, android.R.layout.simple_spinner_dropdown_item);
        targetLangsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        targetLanguageSpinner.setAdapter(targetLangsAdapter);
        targetLanguageSpinner.setSelection(targetLangsAdapter.getPosition(targetLangSetting));
        targetLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                sharedPrefs.edit().putString("targetLang", selectedItem).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        Spinner nativeLanguageSpinner = findViewById(R.id.nativeLanguageSpinner);
        ArrayAdapter<CharSequence> nativeLangsAdapter = ArrayAdapter.createFromResource(this, R.array.native_language_options, android.R.layout.simple_spinner_dropdown_item);
        nativeLangsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nativeLanguageSpinner.setAdapter(nativeLangsAdapter);
        nativeLanguageSpinner.setSelection(nativeLangsAdapter.getPosition(nativeLangSetting));
        nativeLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                sharedPrefs.edit().putString("nativeLang", selectedItem).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        Spinner cefrLvlSpinner = findViewById(R.id.cefrLevelSpinner);
        ArrayAdapter<CharSequence> cefrLvlAdapter = ArrayAdapter.createFromResource(this, R.array.cefr_level_options, android.R.layout.simple_spinner_dropdown_item);
        cefrLvlAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cefrLvlSpinner.setAdapter(cefrLvlAdapter);
        cefrLvlSpinner.setSelection(cefrLvlAdapter.getPosition(cefrLvl));
        cefrLvlSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                sharedPrefs.edit().putString("cefrLevel", selectedItem).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        Context mContext = this;
        Spinner refreshIntervalSpinner = findViewById(R.id.refreshIntervalSpinner);
        ArrayAdapter<CharSequence> refreshIntervalAdapter = ArrayAdapter.createFromResource(this, R.array.refresh_interval_options_keys, android.R.layout.simple_spinner_dropdown_item);
        refreshIntervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        refreshIntervalSpinner.setAdapter(refreshIntervalAdapter);
        refreshIntervalSpinner.setSelection(refreshIntervalAdapter.getPosition(refreshInterval));
        refreshIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                sharedPrefs.edit().putString("refreshInterval", selectedItem).apply();
                AlarmHelpers.setRepeatingAlarm(mContext);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void initializeSentence() {
        SharedPreferences sharedPrefs = getSharedPreferences("com.example.yap", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        String exampleSentenceString = sharedPrefs.getString("exampleSentence", "");
        String exampleSentenceTranslationString = sharedPrefs.getString("exampleSentenceTranslation", "");
        String targetLangSetting = sharedPrefs.getString("targetLang", "");
        String nativeLangSetting = sharedPrefs.getString("nativeLang", "");
        String refreshInterval = sharedPrefs.getString("refreshInterval", "");


        if (targetLangSetting.isEmpty() ) {
            editor.putString("targetLang", "Spanish");
        }
        if (nativeLangSetting.isEmpty()) {
            editor.putString("nativeLang", "English");
        }
        if (refreshInterval.isEmpty()) {
            editor.putString("refreshInterval", "1 hour");
        }
        editor.apply();


        if (!isAlarmSet() || exampleSentenceString.isEmpty() || exampleSentenceTranslationString.isEmpty()) {
            startFetchSentenceService();
        }
    }

    private void registerAlarmActionReceiver() {
        alarmActionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("MainActivity", "received alarm action");
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