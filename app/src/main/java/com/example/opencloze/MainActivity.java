package com.example.opencloze;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.opencloze.JsonHelpers;


public class MainActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPrefs = getSharedPreferences("com.example.opencloze", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        sharedPrefs.registerOnSharedPreferenceChangeListener((sharedPreferences, s) -> {
            Intent intent = new Intent(getApplicationContext(), OpenClozeWidget.class);
            intent.setAction(OpenClozeWidget.ACTION_PREFERENCES_UPDATE);
            sendBroadcast(intent);
        });

        TextView vocabWord = findViewById(R.id.vocabWord);
        TextView vocabRomanization = findViewById(R.id.vocabRomanization);
        TextView vocabDefinition = findViewById(R.id.vocabDefinition);
        TextView exampleSentence = findViewById(R.id.exampleSentence);
        TextView exampleSentenceTranslation = findViewById(R.id.exampleSentenceTranslation);
        TextView plecoLink = findViewById(R.id.plecoLink);
        ConstraintLayout showPlecoLinkSwitchLine = findViewById(R.id.showPlecoLinkSwitchLine);
        ConstraintLayout plecoLinkLayout = findViewById(R.id.plecoLinkLayout);
        LinearLayout flashcard = findViewById(R.id.flashcard);
        SwitchMaterial showVocabWordSwitch = findViewById(R.id.showVocabWordSwitch);
        SwitchMaterial showRomanizationSwitch = findViewById(R.id.showRomanizationSwitch);
        SwitchMaterial showVocabDefinitionSwitch = findViewById(R.id.showVocabDefinitionSwitch);
        SwitchMaterial showExampleSentenceSwitch = findViewById(R.id.showExampleSentenceSwitch);
        SwitchMaterial showExampleSentenceTranslationSwitch = findViewById(R.id.showExampleSentenceTranslationSwitch);
        SwitchMaterial showPlecoLinkSwitch = findViewById(R.id.showPlecoLinkSwitch);
        Button nextButton = findViewById(R.id.nextButton);
        Button prevButton = findViewById(R.id.previousButton);

        JSONArray wordListJsonArray = JsonHelpers.getWordListJsonArray(this);

        setupPlecoLink(sharedPrefs, wordListJsonArray);

        initializeSettings(sharedPrefs, flashcard, vocabWord, vocabRomanization, vocabDefinition, exampleSentence,
                exampleSentenceTranslation, showVocabWordSwitch, showRomanizationSwitch, showVocabDefinitionSwitch,
                showExampleSentenceSwitch, showExampleSentenceTranslationSwitch);

        updateVocabWord(sharedPrefs, wordListJsonArray, vocabWord, vocabRomanization, vocabDefinition, exampleSentence, exampleSentenceTranslation);

        flashcard.setOnClickListener(view -> flipCard(sharedPrefs, view, vocabWord, vocabRomanization, vocabDefinition, exampleSentence,
                exampleSentenceTranslation, plecoLinkLayout, showVocabWordSwitch, showRomanizationSwitch, showVocabDefinitionSwitch,
                showExampleSentenceSwitch, showExampleSentenceTranslationSwitch, showPlecoLinkSwitch));

        setAllOnChangeListeners(sharedPrefs, vocabWord, vocabRomanization, vocabDefinition, exampleSentence,
               exampleSentenceTranslation, plecoLinkLayout, showVocabWordSwitch, showRomanizationSwitch, showVocabDefinitionSwitch,
               showExampleSentenceSwitch, showExampleSentenceTranslationSwitch, showPlecoLinkSwitch);

        int wordListLength = JsonHelpers.getWordListLength(this);

        nextButton.setOnClickListener(view -> {
            int currentVocabWordId = sharedPrefs.getInt("currentVocabWordId", 0);
            editor.putInt("currentVocabWordId", Math.floorMod(currentVocabWordId + 1, wordListLength)).commit();
            updateVocabWord(sharedPrefs, wordListJsonArray, vocabWord, vocabRomanization, vocabDefinition, exampleSentence, exampleSentenceTranslation);
        });

        prevButton.setOnClickListener(view -> {
            int currentVocabWordId = sharedPrefs.getInt("currentVocabWordId", 0);
            editor.putInt("currentVocabWordId", Math.floorMod(currentVocabWordId - 1, wordListLength)).commit();
            updateVocabWord(sharedPrefs, wordListJsonArray, vocabWord, vocabRomanization, vocabDefinition, exampleSentence, exampleSentenceTranslation);
        });

        // Start the timer from this Activity
        AlarmHelpers.setRepeatingAlarm(this);
    }

    private void setupPlecoLink(SharedPreferences prefs, JSONArray wordListJsonArray) {
        TextView plecoLink = findViewById(R.id.plecoLink);
        String text = "Pleco";
        SpannableString spannableString = new SpannableString(text);
        UnderlineSpan underlineSpan = new UnderlineSpan();
        spannableString.setSpan(underlineSpan, 0, text.length(), 0);
        plecoLink.setText(spannableString);

        ConstraintLayout plecoLinkLayout = findViewById(R.id.plecoLinkLayout);
        plecoLinkLayout.setOnClickListener(view -> {
            String currentVocabWord = "";
            try {
                int currentVocabWordId = prefs.getInt("currentVocabWordId", 0);
                JSONObject vocabJsonObj = wordListJsonArray.getJSONObject(currentVocabWordId);
                currentVocabWord = vocabJsonObj.getString("simplified");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String finalCurrentVocabWord = currentVocabWord;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("plecoapi://x-callback-url/s?mode=df&sec=dict&hw=" + finalCurrentVocabWord));
            intent.setPackage("com.pleco.chinesesystem");
            startActivity(intent);
        });
    }


    private void updateVocabWord(SharedPreferences sharedPrefs, JSONArray wordListJsonArray, TextView vocabWord, TextView vocabRomanization, TextView vocabDefinition, TextView exampleSentence, TextView exampleSentenceTranslation ) {
        try {
            // Get current vocab word index from shared prefs
            int currentVocabWordId = sharedPrefs.getInt("currentVocabWordId", 0);

            JSONObject vocabJsonObj = wordListJsonArray.getJSONObject(currentVocabWordId);

            vocabWord.setText(vocabJsonObj.getString("simplified")); // TODO swap this out with traditional according to sharedpreferences
            vocabRomanization.setText(vocabJsonObj.getString("pinyin"));
            vocabDefinition.setText(vocabJsonObj.getString("definition"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void flipCard(SharedPreferences sharedPrefs, View flashcard, TextView vocabWord, TextView vocabRomanization,
                          TextView vocabDefinition, TextView exampleSentence, TextView exampleSentenceTranslation, ConstraintLayout plecoLinkLayout,
                          SwitchMaterial showVocabWordSwitch, SwitchMaterial showRomanizationSwitch,
                          SwitchMaterial showVocabDefinitionSwitch, SwitchMaterial showExampleSentenceSwitch,
                          SwitchMaterial showExampleSentenceTranslationSwitch, SwitchMaterial showPlecoLinkSwitch) {
        boolean isCardFront = sharedPrefs.getBoolean("isCardFront", true);
        sharedPrefs.edit().putBoolean("isCardFront", !isCardFront).apply();

        // Need to remove onChangeListeners before calling initializeSettings again, or else the setChecked calls will mess up SharedPrefs
        removeAllOnChangeListeners(showVocabWordSwitch, showRomanizationSwitch, showVocabDefinitionSwitch,
                showExampleSentenceSwitch, showExampleSentenceTranslationSwitch, showPlecoLinkSwitch);
        initializeSettings(sharedPrefs, flashcard, vocabWord, vocabRomanization, vocabDefinition, exampleSentence,
                exampleSentenceTranslation, showVocabWordSwitch, showRomanizationSwitch, showVocabDefinitionSwitch,
                showExampleSentenceSwitch, showExampleSentenceTranslationSwitch);
        // Re-set all the onChangeListeners
        setAllOnChangeListeners(sharedPrefs, vocabWord, vocabRomanization, vocabDefinition, exampleSentence,
                exampleSentenceTranslation, plecoLinkLayout, showVocabWordSwitch, showRomanizationSwitch, showVocabDefinitionSwitch,
                showExampleSentenceSwitch, showExampleSentenceTranslationSwitch, showPlecoLinkSwitch);
    }

    private void removeAllOnChangeListeners(SwitchMaterial showVocabWordSwitch, SwitchMaterial showRomanizationSwitch,
                                            SwitchMaterial showVocabDefinitionSwitch, SwitchMaterial showExampleSentenceSwitch,
                                            SwitchMaterial showExampleSentenceTranslationSwitch, SwitchMaterial showPlecoLinkSwitch) {
        showVocabWordSwitch.setOnCheckedChangeListener(null);
        showRomanizationSwitch.setOnCheckedChangeListener(null);
        showVocabDefinitionSwitch.setOnCheckedChangeListener(null);
        showExampleSentenceSwitch.setOnCheckedChangeListener(null);
        showExampleSentenceTranslationSwitch.setOnCheckedChangeListener(null);
        showPlecoLinkSwitch.setOnCheckedChangeListener(null);
    }

    private void setAllOnChangeListeners(SharedPreferences sharedPrefs, TextView vocabWord, TextView vocabRomanization,
                                          TextView vocabDefinition, TextView exampleSentence, TextView exampleSentenceTranslation, ConstraintLayout plecoLinkLayout,
                                          SwitchMaterial showVocabWordSwitch, SwitchMaterial showRomanizationSwitch,
                                          SwitchMaterial showVocabDefinitionSwitch, SwitchMaterial showExampleSentenceSwitch,
                                          SwitchMaterial showExampleSentenceTranslationSwitch, SwitchMaterial showPlecoLinkSwitch) {
        String keyPrefix = sharedPrefs.getBoolean("isCardFront", true) ? "front:" : "back:";

        showVocabWordSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            SharedPreferences sharedPrefs1 = getSharedPreferences("com.example.opencloze", Context.MODE_PRIVATE);
            sharedPrefs1.edit().putBoolean(keyPrefix + "showVocabWord", b).apply();
            vocabWord.setVisibility(b ? View.VISIBLE : View.GONE);
        });
        showRomanizationSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            sharedPrefs.edit().putBoolean(keyPrefix + "showRomanization", b).apply();
            vocabRomanization.setVisibility(b ? View.VISIBLE : View.GONE);
        });
        showVocabDefinitionSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            sharedPrefs.edit().putBoolean(keyPrefix + "showVocabDefinition", b).apply();
            vocabDefinition.setVisibility(b ? View.VISIBLE : View.GONE);
        });
        showExampleSentenceSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            sharedPrefs.edit().putBoolean(keyPrefix + "showExampleSentence", b).apply();
            exampleSentence.setVisibility(b ? View.VISIBLE : View.GONE);
        });
        showExampleSentenceTranslationSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            sharedPrefs.edit().putBoolean(keyPrefix + "showExampleSentenceTranslation", b).apply();
            exampleSentenceTranslation.setVisibility(b ? View.VISIBLE : View.GONE);
        });
        showPlecoLinkSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            sharedPrefs.edit().putBoolean(keyPrefix + "showPlecoLink", b).apply();
            plecoLinkLayout.setVisibility(b ? View.VISIBLE : View.GONE);
        });
    }

    private void initializeSettings(SharedPreferences sharedPrefs, View flashcard, TextView vocabWord, TextView vocabRomanization,
                                     TextView vocabDefinition, TextView exampleSentence, TextView exampleSentenceTranslation,
                                     SwitchMaterial showVocabWordSwitch, SwitchMaterial showRomanizationSwitch,
                                     SwitchMaterial showVocabDefinitionSwitch, SwitchMaterial showExampleSentenceSwitch,
                                     SwitchMaterial showExampleSentenceTranslationSwitch) {
        String keyPrefix = sharedPrefs.getBoolean("isCardFront", true) ? "front:" : "back:";

        if (sharedPrefs.getBoolean("isCardFront", true)) {
            flashcard.setBackground(getDrawable(R.drawable.layout_card_bg_lite));
            vocabWord.setTextColor(getResources().getColor(R.color.black));
            vocabRomanization.setTextColor(getResources().getColor(R.color.black));
            vocabDefinition.setTextColor(getResources().getColor(R.color.black));
            exampleSentence.setTextColor(getResources().getColor(R.color.black));
            exampleSentenceTranslation.setTextColor(getResources().getColor(R.color.black));
        } else {
            flashcard.setBackground(getDrawable(R.drawable.layout_card_bg));
            vocabWord.setTextColor(getResources().getColor(R.color.white));
            vocabRomanization.setTextColor(getResources().getColor(R.color.white));
            vocabDefinition.setTextColor(getResources().getColor(R.color.white));
            exampleSentence.setTextColor(getResources().getColor(R.color.white));
            exampleSentenceTranslation.setTextColor(getResources().getColor(R.color.white));
        }

        boolean showVocabWordSharedPref = sharedPrefs.getBoolean(keyPrefix + "showVocabWord", true);
        showVocabWordSwitch.setChecked(showVocabWordSharedPref);
        vocabWord.setVisibility(showVocabWordSharedPref ? View.VISIBLE : View.GONE);

        boolean showRomanizationSharedPref = sharedPrefs.getBoolean(keyPrefix + "showRomanization", true);
        showRomanizationSwitch.setChecked(showRomanizationSharedPref);
        vocabRomanization.setVisibility(showRomanizationSharedPref ? View.VISIBLE : View.GONE);

        boolean showVocabDefinitionSharedPref = sharedPrefs.getBoolean(keyPrefix + "showVocabDefinition", true);
        showVocabDefinitionSwitch.setChecked(showVocabDefinitionSharedPref);
        vocabDefinition.setVisibility(showVocabDefinitionSharedPref ? View.VISIBLE : View.GONE);

        // TODO Temporary, while we don't have example sentences
        exampleSentence.setVisibility(View.GONE);
        exampleSentenceTranslation.setVisibility(View.GONE);

//        boolean showExampleSentenceSharedPref = sharedPrefs.getBoolean(keyPrefix + "showExampleSentence", true);
//        showExampleSentenceSwitch.setChecked(showExampleSentenceSharedPref);
//        exampleSentence.setVisibility(showExampleSentenceSharedPref ? View.VISIBLE : View.GONE);
//
//        boolean showExampleSentenceTranslationSharedPref = sharedPrefs.getBoolean(keyPrefix + "showExampleSentenceTranslation", true);
//        showExampleSentenceTranslationSwitch.setChecked(showExampleSentenceTranslationSharedPref);
//        exampleSentenceTranslation.setVisibility(showExampleSentenceTranslationSharedPref ? View.VISIBLE : View.GONE);
    }

    private void restoreDefaultSettings(SharedPreferences sharedPrefs) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
//        editor.putBoolean()
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPrefs = getSharedPreferences("com.example.opencloze", Context.MODE_PRIVATE);
        JSONArray wordListJsonArray = JsonHelpers.getWordListJsonArray(this);
        TextView vocabWord = findViewById(R.id.vocabWord);
        TextView vocabRomanization = findViewById(R.id.vocabRomanization);
        TextView vocabDefinition = findViewById(R.id.vocabDefinition);
        TextView exampleSentence = findViewById(R.id.exampleSentence);
        TextView exampleSentenceTranslation = findViewById(R.id.exampleSentenceTranslation);

        updateVocabWord(sharedPrefs, wordListJsonArray, vocabWord, vocabRomanization, vocabDefinition, exampleSentence, exampleSentenceTranslation);
    }
}