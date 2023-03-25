package com.example.opencloze;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

public class JsonHelpers {
    public static JSONArray getWordListJsonArray(Context context) {
        JSONArray json = new JSONArray();
        try  {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("hsk1json.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String jsonString = new String(buffer, "UTF-8");
            json = new JSONArray(jsonString);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public static int getWordListLength(Context context) {
        return getWordListJsonArray(context).length();
    }
}
