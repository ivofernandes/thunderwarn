package com.thunderwarn.thunderwarn.manager;

import com.thunderwarn.thunderwarn.common.Log;

import com.thunderwarn.thunderwarn.common.configuration.ConfigurationManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ivofernandes on 02/11/15.
 */
public class WeatherConditionManager {

    // Singleton
    private static WeatherConditionManager instance = new WeatherConditionManager();

    private WeatherConditionManager(){
        init();
    }

    public static WeatherConditionManager getInstance() {
        return instance;
    }

    // Fields

    public enum WeatherCondition {
        CLOUDS,
        RAIN_LIGHT, RAIN_MODERATE, RAIN_HEAVY,
        THUNDERSTORM, EXTREME,
        SLEET_MODERATE, DRIZZLE_LIGHT, DRIZZLE_MODERATE, DRIZZLE_HEAVY,
        SNOW_LIGHT, SNOW_MODERATE, SNOW_HEAVY
    };

    private Map<WeatherCondition,String> weatherConditionToId = new HashMap<>();
    private Map<WeatherCondition,Integer> weatherConditionWeight = new HashMap<>();
    private Map<String,WeatherCondition> keyToWeatherCondition = new HashMap<>();

    private static String TAG = "WeatherConditionManager";

    private void init() {
        weatherToIds();
        weatherConditionValues();
        keyToWeatherConditions();
    }

    private void keyToWeatherConditions(){
        keyToWeatherCondition.put("clouds", WeatherCondition.CLOUDS);

        keyToWeatherCondition.put("snow-light", WeatherCondition.SNOW_LIGHT);
        keyToWeatherCondition.put("snow-moderate", WeatherCondition.SNOW_MODERATE);
        keyToWeatherCondition.put("snow-heavy", WeatherCondition.SNOW_HEAVY);

        keyToWeatherCondition.put("drizzle-light", WeatherCondition.DRIZZLE_LIGHT);
        keyToWeatherCondition.put("drizzle-moderate", WeatherCondition.DRIZZLE_MODERATE);
        keyToWeatherCondition.put("drizzle-heavy", WeatherCondition.DRIZZLE_HEAVY);

        keyToWeatherCondition.put("rain-light", WeatherCondition.RAIN_LIGHT);
        keyToWeatherCondition.put("rain-moderate", WeatherCondition.RAIN_MODERATE);
        keyToWeatherCondition.put("rain-heavy", WeatherCondition.RAIN_HEAVY);

        keyToWeatherCondition.put("thunderstorm", WeatherCondition.THUNDERSTORM);
        keyToWeatherCondition.put("extreme", WeatherCondition.EXTREME);
        keyToWeatherCondition.put("sleet-moderate", WeatherCondition.SLEET_MODERATE);

    }

    private void weatherToIds() {
        weatherConditionToId.put(WeatherCondition.CLOUDS, "801");
        weatherConditionToId.put(WeatherCondition.RAIN_LIGHT, "500");
        weatherConditionToId.put(WeatherCondition.RAIN_MODERATE, "501");
        weatherConditionToId.put(WeatherCondition.RAIN_HEAVY, "502");
    }

    private void weatherConditionValues() {
        weatherConditionWeight.put(WeatherCondition.CLOUDS, 1000);

        weatherConditionWeight.put(WeatherCondition.SNOW_LIGHT, 1040);
        weatherConditionWeight.put(WeatherCondition.SNOW_MODERATE, 1041);
        weatherConditionWeight.put(WeatherCondition.SNOW_HEAVY, 1042);

        weatherConditionWeight.put(WeatherCondition.DRIZZLE_LIGHT, 1050);
        weatherConditionWeight.put(WeatherCondition.DRIZZLE_MODERATE, 1051);
        weatherConditionWeight.put(WeatherCondition.DRIZZLE_HEAVY, 1052);

        weatherConditionWeight.put(WeatherCondition.RAIN_LIGHT, 1100);
        weatherConditionWeight.put(WeatherCondition.RAIN_MODERATE, 1200);
        weatherConditionWeight.put(WeatherCondition.RAIN_HEAVY, 1300);

        weatherConditionWeight.put(WeatherCondition.THUNDERSTORM, 2000);
        weatherConditionWeight.put(WeatherCondition.SLEET_MODERATE, 3000);
        weatherConditionWeight.put(WeatherCondition.EXTREME, 5000);
    }

    /**
     * Rain fields
     * http://wiki.sandaysoft.com/a/Rain_measurement
     Very light rain	precipitation rate is < 0.25 mm/hour
     Light rain	        precipitation rate is between 0.25mm/hour and 1.0mm/hour
     Moderate rain	    precipitation rate is between 1.0 mm/hour and 4.0 mm/hour
     Heavy rain	        precipitation rate is between 4.0 mm/hour and 16.0 mm/hour
     Very heavy rain   	precipitation rate is between 16.0 mm/hour and 50 mm/hour
     Extreme rain	    precipitation rate is > 50.0 mm/hour
     */

    public static final float LIGHT_RAIN_MIN = (float) 0.10;
    private static final float LIGHT_RAIN_MAX = (float) 1;

    private static final float MODERATE_RAIN_MIN = (float) 1;
    private static final float MODERATE_RAIN_MAX = (float) 4;

    private static final float HEAVY_RAIN_MIN = (float) 4;

    public WeatherCondition rainIntensity(double rain){

        if(rain >= LIGHT_RAIN_MIN && rain < LIGHT_RAIN_MAX){
            return WeatherCondition.RAIN_LIGHT;
        }else if(rain >= MODERATE_RAIN_MIN && rain < MODERATE_RAIN_MAX){
            return WeatherCondition.RAIN_MODERATE;
        }else if(rain >= HEAVY_RAIN_MIN){
            return WeatherCondition.RAIN_HEAVY;
        }

        return WeatherCondition.CLOUDS;
    }


    public WeatherCondition moreImportantCondition(WeatherCondition weatherCondition1, WeatherCondition weatherCondition2) {
        if(weatherCondition1 == null){
            return weatherCondition2;
        }else if(weatherCondition2 == null){
            return weatherCondition1;
        }

        Integer value1 = weatherConditionWeight.get(weatherCondition1);
        Integer value2 = weatherConditionWeight.get(weatherCondition2);

        if(value1 != null && value2 != null){
            if(value1.intValue() > value2.intValue()){
                return weatherCondition1;
            }else{
                return weatherCondition2;
            }
        }
        // If have no weights
        else{
            if(weatherCondition1 == null){
                return weatherCondition2;
            }else if(weatherCondition2 == null){
                return weatherCondition1;
            }else{
                return null;
            }
        }

    }

    public String getIdForRain(double rain) {
        WeatherCondition rainIntensity = rainIntensity(rain);

        return getIdByWeatherCondition(rainIntensity);
    }


    public String descriptionFor(WeatherCondition rain) {

        JSONObject translationObject = ConfigurationManager.getInstance().getJsonTranslation();
        String id = weatherConditionToId.get(rain);

        if(translationObject != null && id != null){
            try {
                String newDescription = translationObject.getString(id);
                return newDescription;
            } catch (JSONException e) {
                Log.e(TAG, "error getting description for rain intensity " +rain + ": "
                        + e.getMessage() ,e);
            }
        }

        return "";
    }

    public String getIdByWeatherCondition(WeatherCondition weatherCondition){
        return weatherConditionToId.get(weatherCondition);
    }

    public WeatherCondition getWeatherConditionById(String id){

        String key = ConfigurationManager.getInstance().keyForCode(null,id);

        WeatherCondition weatherCondition = keyToWeatherCondition.get(key);

        return weatherCondition;
    }
}
