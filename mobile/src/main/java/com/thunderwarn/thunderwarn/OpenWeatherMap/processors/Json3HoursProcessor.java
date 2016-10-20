package com.thunderwarn.thunderwarn.OpenWeatherMap.processors;

import com.thunderwarn.thunderwarn.common.SharedResources;
import com.thunderwarn.thunderwarn.common.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by ivofernandes on 28/10/15.
 */
public class Json3HoursProcessor extends JsonProcessor{

    private Weather weather = Weather.getInstance();
    private int hourInterval = 3;

    public String timeInterval(JSONObject prediction, int position) throws JSONException {

        Date timeStart = getTime(prediction);
        Date timeEnd = new Date(timeStart.getTime() + JsonProcessor.INTERVAL_3HOURS);

        String dateStringStart = SharedResources.getInstance().formatDateTime(timeStart);
        String dateStringEnd = SharedResources.getInstance().formatDateTime(timeEnd);
        String timeInteval = dateStringStart + " - " + dateStringEnd;

        return  timeInteval;
    }

    public String temperatureTextRetrieve(JSONObject prediction, boolean apparentTemperature,
                                          int position, int hourInterval) throws JSONException {
        JSONObject main = prediction.getJSONObject("main");
        JSONArray weatherArray = prediction.getJSONArray("weather");

        // get fields
        double tempMax = main.getDouble("temp_max");
        double tempMin = main.getDouble("temp_min");

        if(position == 0 || hourInterval == 3){
            double temp = main.getDouble("temp");
            tempMin = temp;
            tempMax = temp;
        }

        if(apparentTemperature){
            JSONObject wind = prediction.getJSONObject("wind");
            double windSpeed = wind.getDouble("speed");
            double radiation = 1.3;
            double humidity = main.getDouble("humidity");

            return JsonProcessor.getApparentTemperatureText(tempMin, tempMax, humidity,
                    windSpeed, radiation);
        }else{
            return JsonProcessor.temperatureText(tempMin, tempMax);
        }
    }

    @Override
    public double getRain(JSONObject prediction) throws JSONException {

        if (prediction.has("rain")) {
            JSONObject rainJS = prediction.getJSONObject("rain");
            if (rainJS.has("3h")) {
                return rainJS.getDouble("3h");
            }
        }

        return 0;
    }

    @Override
    public double getSnow(JSONObject prediction) throws JSONException {
        if(prediction.has("snow")){
            JSONObject snowJS = prediction.getJSONObject("snow");
            if (snowJS.has("3h")) {
                return prediction.getDouble("snow");
            }
        }
        return 0;
    }

    @Override
    public double humidity(JSONObject prediction) throws JSONException {

        JSONObject main = prediction.getJSONObject("main");
        double humidity = main.getDouble("humidity");

        return humidity;
    }

    @Override
    public double windSpeed(JSONObject prediction) throws JSONException {
        JSONObject wind = prediction.getJSONObject("wind");
        double windSpeed = wind.getDouble("speed");
        return windSpeed;
    }

    @Override
    public double windDegree(JSONObject prediction) throws JSONException {
        JSONObject wind = prediction.getJSONObject("wind");
        double windDegree = wind.getDouble("deg");
        return windDegree;
    }

    @Override
    public double getCloudiness(JSONObject prediction) throws JSONException {
        JSONObject clouds = prediction.getJSONObject("clouds");
        double cloudiness = clouds.getDouble("all");
        return cloudiness;
    }

    @Override
    public double getGoodWeatherRatio(JSONObject prediction, boolean apparentTemperature) throws JSONException {

        JSONObject main = prediction.getJSONObject("main");
        JSONArray weatherArray = prediction.getJSONArray("weather");

        // get fields
        double tempMax = main.getDouble("temp_max");
        double tempMin = main.getDouble("temp_min");
        JSONObject wind = prediction.getJSONObject("wind");
        double windSpeed = wind.getDouble("speed");
        double radiation = 1.3;
        double humidity = main.getDouble("humidity");

        return super.calculateGoodWeatherRatio(tempMax, tempMin, humidity, windSpeed, radiation, apparentTemperature);
    }
}
