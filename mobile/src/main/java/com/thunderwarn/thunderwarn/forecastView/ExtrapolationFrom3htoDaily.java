package com.thunderwarn.thunderwarn.forecastView;

import com.thunderwarn.thunderwarn.common.Log;

import com.thunderwarn.thunderwarn.OpenWeatherMap.processors.Json3HoursProcessor;
import com.thunderwarn.thunderwarn.common.SharedResources;
import com.thunderwarn.thunderwarn.common.Weather;
import com.thunderwarn.thunderwarn.data.WeatherPrediction;
import com.thunderwarn.thunderwarn.manager.WeatherConditionManager;
import com.thunderwarn.thunderwarn.manager.WeatherDataManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by ivofernandes on 21/11/15.
 */
public class ExtrapolationFrom3htoDaily {
    // Singleton
    private static ExtrapolationFrom3htoDaily instance = new ExtrapolationFrom3htoDaily();

    private ExtrapolationFrom3htoDaily(){}

    public static ExtrapolationFrom3htoDaily getInstance() {
        return instance;
    }

    // Constants
    private static String TAG = "ExtrapolationFrom3htoDaily";
    private Weather weather = Weather.getInstance();
    private SharedResources sharedResources = SharedResources.getInstance();

    //Fields
    private HashMap<String,WeatherPrediction> cacheMinMax = new HashMap<String,WeatherPrediction>();
    private Date lastPredictionTime = null; // time of the last 3h prediction
    private Date firstPreditionTime = null;
    // Methods
    public void clear(){
        cacheMinMax.clear();

        lastPredictionTime = null;
        firstPreditionTime = null;
    }


    public void extrapolate3hours(JSONArray predictionsList, Json3HoursProcessor processor) throws JSONException {

        int size = predictionsList.length();
        Date currentDate = new Date();

        for (int i=0 ; i<predictionsList.length() ; i++){
            JSONObject prediction = predictionsList.getJSONObject(i);

            processPredition(prediction,processor,false);
        }

        Log.i(TAG, "lastPredictionTime: " + lastPredictionTime);
    }

    private void processPredition(JSONObject prediction, Json3HoursProcessor processor,
                                  boolean predictionForCurrentWeather) throws JSONException {

        long timeJson = prediction.getLong("dt")* 1000;
        Date time = new Date(timeJson);
        String dateString = sharedResources.formatDate(time);

        // Add 3 hours to go the end of the prediction
        long newTime = time.getTime() + 3 * 1000 * 60 * 60;
        time.setTime(newTime);

        // cache data
        JSONObject main = prediction.getJSONObject("main");
        JSONArray weatherArray = prediction.getJSONArray("weather");

        // get fields
        double tempMax = main.getDouble("temp_max");
        double tempMin = main.getDouble("temp_min");
        double temp = main.getDouble("temp");

        if(predictionForCurrentWeather){
            tempMax = temp;
            tempMin = temp;
        }

        double windSpeed = 0;
        double radiation = 1.3;
        double humidity = 0;
        try {
            JSONObject wind = prediction.getJSONObject("wind");
            windSpeed = wind.getDouble("speed");
            humidity = main.getDouble("humidity");
        }catch (Exception e){}

        int apparentMax = (int) Math.round(weather.apparentTemperature(tempMax, humidity, windSpeed, radiation));
        int apparentMin = (int) Math.round(weather.apparentTemperature(tempMin, humidity, windSpeed, radiation));


        JSONObject weatherObj = weatherArray.getJSONObject(0);
        String id = weatherObj.getString("id");

        double rain = processor.getRain(prediction);
        WeatherConditionManager.WeatherCondition rainIntensity = null;
        if(processor.isRainIcon(id)){
            rainIntensity = WeatherConditionManager.getInstance().rainIntensity(rain);
        }

        cacheData(dateString, apparentMin, apparentMax, rainIntensity, (int) tempMin, (int) tempMax);

        // Update bound times
        if(firstPreditionTime == null || predictionForCurrentWeather){
            firstPreditionTime = time;
        }

        if(lastPredictionTime == null || time.after(lastPredictionTime)) {
            lastPredictionTime = time;
        }

    }

    public void extrapolateCurrent(JSONObject jsonCurrent, Json3HoursProcessor processor) throws JSONException {
        processPredition(jsonCurrent,processor,true);
    }


    private void cacheData(String dateString, int apparentMin, int apparentMax,
                           WeatherConditionManager.WeatherCondition rainIntensity,
                           int tempMin, int tempMax) {
        WeatherPrediction weatherPrediction = cacheMinMax.remove(dateString);

        if(weatherPrediction == null){
            weatherPrediction = new WeatherPrediction(apparentMin,apparentMax,tempMin,tempMax);
        }

        if(apparentMin < weatherPrediction.getApparentMin()){
            weatherPrediction.setApparentMin(apparentMin);
        }

        if(apparentMax > weatherPrediction.getApparentMax()){
            weatherPrediction.setApparentMax(apparentMax);
        }

        if(tempMin < weatherPrediction.getTempMin()){
            weatherPrediction.setTempMin(tempMin);
        }

        if(tempMax > weatherPrediction.getTempMax()){
            weatherPrediction.setTempMax(tempMax);
        }

        WeatherConditionManager.WeatherCondition cachedWeatherCondition =
                weatherPrediction.getWeatherCondition();

        WeatherConditionManager.WeatherCondition weatherCondition =
                WeatherConditionManager.getInstance().moreImportantCondition(cachedWeatherCondition, rainIntensity);

        weatherPrediction.setWeatherCondition(weatherCondition);

        cacheMinMax.put(dateString, weatherPrediction);
    }

    /**
     * Get the daily data from 3 hours
     * @param dateString
     * @return
     */
    public WeatherPrediction predictionForDay(String dateString, Date date) {
        if(lastPredictionTime != null && date != null && dateString != null
                && WeatherDataManager.insideDateThreshold(date, lastPredictionTime, 60 * 60 * 24)){
            return cacheMinMax.get(dateString);
        }

        return null;
    }

    public Date getFirstPreditionTime() {
        return firstPreditionTime;
    }
}
