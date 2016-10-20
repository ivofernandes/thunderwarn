package com.thunderwarn.thunderwarn.common.configuration;

import android.content.res.Resources;

import com.thunderwarn.thunderwarn.common.Log;


import com.thunderwarn.thunderwarn.R;
import com.thunderwarn.thunderwarn.common.FileOperations;
import com.thunderwarn.thunderwarn.common.SharedResources;
import com.thunderwarn.thunderwarn.common.i18n.LanguageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by ivofernandes on 25/09/15.
 * Configurations that will be shared with other apps, web, etc...
 */
public class ConfigurationManager {

    //Singleton
    private static ConfigurationManager instance = new ConfigurationManager();

    public static ConfigurationManager getInstance() {
        return instance;
    }

    private ConfigurationManager(){}

    // Constants
    private final FileOperations fileOperations = FileOperations.getInstance();
    private final String TAG = "ConfigurationManager";

    private Map<String,Integer> fileConverterMap = new HashMap<>();

    // Icon converter
    private JSONObject jsonConverter;
    private JSONObject codesConverter;
    private JSONObject iconsConverter;

    public void loadIconConverter(InputStream iconConverterStream) {
        try {
            String jsonString = fileOperations.loadJSONFromAsset(iconConverterStream);

            jsonConverter = new JSONObject(jsonString);
            codesConverter = jsonConverter.getJSONObject("codes");
            iconsConverter = jsonConverter.getJSONObject("icons");

            initMaps();
        } catch (IOException e) {
            Log.e(TAG, "error loading icon converter file", e);
        } catch (JSONException e) {
            Log.e(TAG, "error parsing icon converter file", e);
        }
    }

    private void initMaps() {

        // icon converter
        fileConverterMap.put("", R.drawable.bb_blank);

        fileConverterMap.put("clear-sky-day", R.drawable.bb_clear_day);
        fileConverterMap.put("few-clouds-day", R.drawable.bb_few_clouds_day);
        // night
        fileConverterMap.put("clear-sky-night", R.drawable.bb_clear_night);
        fileConverterMap.put("few-clouds-night", R.drawable.bb_few_clouds_night);

        // codes converter
        fileConverterMap.put("thunderstorm", R.drawable.bb_thunderstorm);

        fileConverterMap.put("drizzle-light", R.drawable.bb_drizzle_light);
        fileConverterMap.put("drizzle-moderate", R.drawable.bb_drizzle_moderate);
        fileConverterMap.put("drizzle-heavy", R.drawable.bb_drizzle_heavy);

        fileConverterMap.put("rain-light", R.drawable.bb_rain_light);
        fileConverterMap.put("rain-moderate", R.drawable.bb_rain_moderate);
        fileConverterMap.put("rain-heavy", R.drawable.bb_rain_heavy);

        fileConverterMap.put("shower-light", R.drawable.bb_shower_light);
        fileConverterMap.put("shower-moderate", R.drawable.bb_shower_moderate);
        fileConverterMap.put("shower-heavy", R.drawable.bb_shower_heavy);

        fileConverterMap.put("snow-light", R.drawable.bb_snow_light);
        fileConverterMap.put("snow-moderate", R.drawable.bb_snow_moderate);
        fileConverterMap.put("snow-heavy", R.drawable.bb_snow_heavy);

        fileConverterMap.put("sleet-light", R.drawable.bb_sleet_light);
        fileConverterMap.put("sleet-moderate", R.drawable.bb_sleet_moderate);
        fileConverterMap.put("sleet-heavy", R.drawable.bb_sleet_heavy);
        fileConverterMap.put("rain-snow", R.drawable.bb_rain_snow);

        fileConverterMap.put("fog-moderate", R.drawable.bb_fog_moderate);
        fileConverterMap.put("fog-heavy", R.drawable.bb_fog_heavy);

        fileConverterMap.put("clouds", R.drawable.bb_clouds);

        fileConverterMap.put("extreme",R.drawable.bb_extreme);


    }


    public int iconForCode(String icon, String codeString) {

        String key = keyForCode(icon, codeString);

        return convertImageFile(key);
    }

    public String keyForCode(String icon, String codeString) {
        if(iconsConverter == null){
            Log.d(TAG,"initing icon converter from file");

            Resources resources = SharedResources.getInstance().getContext().getResources();
            InputStream iconConverterStream = resources.openRawResource(R.raw.icon_converter);
            ConfigurationManager.getInstance().loadIconConverter(iconConverterStream);

            Log.i(TAG, "icon converter readed from file");
        }

        if (codeString != null || icon != null) {

            // search icon converter for the code and return the image file name
            try {

                if(icon != null){
                    Iterator<String> iterator = iconsConverter.keys();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        JSONArray codesInJson = iconsConverter.getJSONArray(key);

                        for (int i = 0; i < codesInJson.length(); i++) {
                            String iconInJson = codesInJson.getString(i);

                            if (iconInJson != null) {
                                if (icon.equals(iconInJson)) {
                                    return key;
                                }
                            }
                        }

                    }
                }

                // If was not resolved by icon pass to code
                if(codeString != null) {
                    int code = Integer.parseInt(codeString);

                    Iterator<String> iterator = codesConverter.keys();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        JSONArray codesInJson = codesConverter.getJSONArray(key);

                        for (int i = 0; i < codesInJson.length(); i++) {
                            Integer codeInJson = codesInJson.getInt(i);

                            if (codeInJson != null) {
                                if (code == codeInJson.intValue()) {
                                    return key;
                                }
                            }
                        }

                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "error converting code: " + codeString, e);
                // don't use any icon
            }
        }

        return null;
    }

    private int convertImageFile(String key) {
        if (key == null) {
            return R.drawable.bb_blank;
        }
        else {
            Integer image = fileConverterMap.get(key);
            if(image != null){
                return image.intValue();
            }else {
                return R.drawable.bb_blank;
            }
        }
    }

    // Languages
    private JSONObject jsonLanguages;

    public void loadLanguages(InputStream languagesStream) {
        try {
            String jsonString = fileOperations.loadJSONFromAsset(languagesStream);

            jsonLanguages = new JSONObject(jsonString);
        } catch (IOException e) {
            Log.e(TAG, "error loading languages file", e);
        } catch (JSONException e) {
            Log.e(TAG, "error parsing languages file", e);
        }
    }

    public JSONObject getJsonLanguages() {
        Log.d(TAG, "get languages");
        if(this.jsonLanguages == null) {
            Log.d(TAG, "loading languages file");
            InputStream languagesStream = SharedResources.getInstance().getContext().getResources().openRawResource(R.raw.languages);
            ConfigurationManager.getInstance().loadLanguages(languagesStream);
            Log.i(TAG, "loaded languages file");
        }

        return jsonLanguages;
    }

    // Translation
    private JSONObject jsonTranslation;

    public void loadTranslation(InputStream translationStream) {
        try {
            String jsonString = fileOperations.loadJSONFromAsset(translationStream);

            jsonTranslation = new JSONObject(jsonString);
        } catch (IOException e) {
            Log.e(TAG, "error loading translation file", e);
        } catch (JSONException e) {
            Log.e(TAG, "error parsing translation file", e);
        }
    }

    public JSONObject getJsonTranslation()  {
        Log.d(TAG, "getJsonTranslation");

        String language = LanguageManager.getInstance().getLanguage();

        Log.d(TAG, "getJsonTranslation for language: " + language);

        JSONObject result = null;

        if(jsonTranslation == null){
            Log.d(TAG, "initing json translation");
            // translation
            Resources resources = SharedResources.getInstance().getContext().getResources();
            InputStream translationStream = resources.openRawResource(R.raw.translation);
            ConfigurationManager.getInstance().loadTranslation(translationStream);

            Log.i(TAG, "inited json translation");
        }

        if(jsonTranslation != null){
            try {
                result = jsonTranslation.getJSONObject(language);
            } catch (JSONException e) {
                Log.e(TAG, "Error getting translation for language " + language+ ": "+ e.getMessage(),e);
                return null;
            }
        }else{
            Log.e(TAG, "can't get json translation for language: " + language);
        }

        return result;
    }

    public boolean isMetric() {

        Locale locale = SharedResources.getInstance().getLocale();
        String country = locale.getCountry();

        if(country.equalsIgnoreCase("US")){
            //TODO to return false... need a different scale of colors
            // look the ration function  in the functionWeather.java.ratio(min,max)
            return true;
        }else{
            return true;

        }
    }

    // Other configurations


}
