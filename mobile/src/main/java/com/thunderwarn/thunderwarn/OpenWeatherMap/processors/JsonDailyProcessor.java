package com.thunderwarn.thunderwarn.OpenWeatherMap.processors;

import com.thunderwarn.thunderwarn.common.SharedResources;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by ivofernandes on 28/10/15.
 */
public class JsonDailyProcessor extends JsonProcessor{

    private int hourInterval = 24;

    @Override
    public String timeInterval(JSONObject prediction, int position) throws JSONException {

        Date timeStart = getTime(prediction);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timeStart);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        timeStart.setTime(calendar.getTimeInMillis());

        Date timeEnd = new Date(timeStart.getTime() + JsonProcessor.INTERVAL_24HOURS);

        String dateStringStart = SharedResources.getInstance().formatDateTime(timeStart);
        String dateStringEnd = SharedResources.getInstance().formatDateTime(timeEnd);
        String timeInteval = dateStringStart + " - " + dateStringEnd;

        return  timeInteval;
    }

    @Override
    public String temperatureTextRetrieve(JSONObject prediction, boolean apparentTemperature, int position, int hourInterval) throws JSONException {
        JSONObject main = prediction.getJSONObject("temp");

        // get fields
        double tempMax = main.getDouble("max");
        double tempMin = main.getDouble("min");

        if(apparentTemperature){
            double windSpeed = prediction.getDouble("speed");
            double radiation = 1.3;
            double humidity = prediction.getDouble("humidity");

            return JsonProcessor.getApparentTemperatureText(tempMin, tempMax, humidity,
                    windSpeed, radiation);
        }else{
            return JsonProcessor.temperatureText(tempMin, tempMax);
        }
    }

    @Override
    public double humidity(JSONObject prediction) throws JSONException {
        double humidity = prediction.getDouble("humidity");

        return humidity;
    }

    @Override
    public double getRain(JSONObject prediction) throws JSONException {

        if (prediction.has("rain")) {
            return prediction.getDouble("rain");
        }

        return 0;
    }

    @Override
    public double getSnow(JSONObject prediction) throws JSONException {
        if(prediction.has("snow")){
            return prediction.getDouble("snow");
        }
        return 0;
    }

    @Override
    public double windSpeed(JSONObject prediction) throws JSONException {
        double windSpeed  = prediction.getDouble("speed");
        return windSpeed;
    }

    @Override
    public double windDegree(JSONObject prediction) throws JSONException {
        double windDegree = prediction.getDouble("deg");
        return windDegree;
    }

    @Override
    public double getCloudiness(JSONObject prediction) throws JSONException {
        double cloudiness = prediction.getDouble("clouds");
        return cloudiness;
    }

    @Override
    public double getGoodWeatherRatio(JSONObject prediction, boolean apparentTemperature) throws JSONException {
        // Json structure
        JSONObject temp = prediction.getJSONObject("temp");

        // get fields
        double radiation = 1.3;

        double humidity = prediction.getDouble("humidity");
        int tempMax = (int) Math.round(temp.getDouble("max"));
        int tempMin = (int) Math.round(temp.getDouble("min"));
        double windSpeed = prediction.getDouble("speed");


        return super.calculateGoodWeatherRatio(tempMax,tempMin,humidity,windSpeed,radiation,apparentTemperature);
    }
}
