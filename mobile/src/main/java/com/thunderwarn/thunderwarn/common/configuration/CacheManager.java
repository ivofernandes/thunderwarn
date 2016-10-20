package com.thunderwarn.thunderwarn.common.configuration;

import android.content.Context;
import android.content.SharedPreferences;

import com.thunderwarn.thunderwarn.OpenWeatherMap.OpenWeatherRequest;
import com.thunderwarn.thunderwarn.common.Log;
import com.thunderwarn.thunderwarn.common.SharedResources;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ivofernandes on 29/10/15.
 */
public class CacheManager {

    private static final String TAG = "CacheManager";

    // Singleton
    private static final CacheManager instance = new CacheManager();

    private CacheManager(){}

    public static CacheManager getInstance(){
        return instance;
    }

    // Cache Constants weather data
    public static final String KEY_OPEN_WEATHER_MAP_3HOURS = "OPEN_WEATHER_MAP_3HOURS";
    public static final String KEY_OPEN_WEATHER_MAP_DAILY = "OPEN_WEATHER_MAP_DAILY";
    public static final String KEY_OPEN_WEATHER_MAP_CURRENT = "OPEN_WEATHER_MAP_CURRENT";

    public static final String SUFIX_DATE = "DATE";
    public static final String SUFIX_LOCATION = "LOCATION";
    public static final String SUFIX_DATA = "DATA";

    // Cache Constants Location
    public static final String KEY_GPS_DATE = "GPS_DATE";
    public static final String KEY_LOCATIONS = "LOCATIONS";
    public static final String LOCATION_DESCRIPTION = "LOCATION_DESCRIPTION";

    // Cache Constants Settings

    public static final String PREFERENCE_SETTINGS_NOTIFICATION = "SETTINGS_NOTIFICATION";
    public static final String PREFERENCE_SETTINGS_APPARENT_TEMPERATURE = "SETTINGS_APPARENT_TEMPERATURE";
    public static final String PREFERENCE_SETTINGS_USE_GPS = "SETTINGS_USE_GPS";

    public static final String PREFERENCE_LOCATION_USE_GPS = "SETTINGS_NOTIFICATION";

    // Other constants
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    // Fields
    private SharedPreferences sharedPreferences;


    // Static methods
    public static String getPreferenceFor(String requestType, String preferenceSufix) {
        String keyPrefix = null;
        if(requestType.equals(OpenWeatherRequest.REQUEST_DATA_3_HOURS)) {
            keyPrefix = CacheManager.KEY_OPEN_WEATHER_MAP_3HOURS;
        } else if(requestType.equals(OpenWeatherRequest.REQUEST_DATA_DAILY)) {
            keyPrefix = CacheManager.KEY_OPEN_WEATHER_MAP_DAILY;
        } else if(requestType.equals(OpenWeatherRequest.REQUEST_DATA_CURRENT)) {
            keyPrefix = CacheManager.KEY_OPEN_WEATHER_MAP_CURRENT;
        }

        return requestType + "_" + preferenceSufix;
    }

    // Methods
    public SharedPreferences getSharedPreferences() {
        if(sharedPreferences == null){

            this.sharedPreferences = SharedResources.getInstance().getContext().getSharedPreferences(
                    SharedResources.class.getName(), SharedResources.getInstance().getContext().MODE_PRIVATE);

            Log.d(TAG, "cache manager setted with shared preferences: " + sharedPreferences);
        }

        return sharedPreferences;
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key, String defaultValue) {
        return getSharedPreferences().getString(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return getSharedPreferences().getBoolean(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public Date getDate(String key,Date defaultValue){
        String dateString = getString(key, DATE_FORMAT.format(defaultValue));

        if(dateString != null){
            try {
                Date date = DATE_FORMAT.parse(dateString);
                return date;
            } catch (ParseException e) {
                Log.e(TAG,"Error parsing the date " + dateString + " from cache key " + key);
            }
        }

        return new Date();
    }

    public void putDate(String key, Date date){
        if(date != null){
            String dateString = DATE_FORMAT.format(date);
            putString(key,dateString);
        }
    }
}
