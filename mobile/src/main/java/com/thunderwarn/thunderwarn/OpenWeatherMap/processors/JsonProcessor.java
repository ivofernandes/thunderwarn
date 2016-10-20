package com.thunderwarn.thunderwarn.OpenWeatherMap.processors;

import com.thunderwarn.thunderwarn.common.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.thunderwarn.thunderwarn.common.SharedResources;
import com.thunderwarn.thunderwarn.common.Weather;
import com.thunderwarn.thunderwarn.common.configuration.ConfigurationManager;
import com.thunderwarn.thunderwarn.manager.WeatherConditionManager;
import com.thunderwarn.thunderwarn.manager.WeatherDataManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by ivofernandes on 28/10/15.
 */
public abstract class JsonProcessor {

    public static final String TAG = "JsonProcessor";
    public static final long INTERVAL_HOUR = 60 * 60 * 1000;
    public static final long INTERVAL_3HOURS = 3 * INTERVAL_HOUR;
    public static final long INTERVAL_24HOURS = 24 * INTERVAL_HOUR;

    public static Date getTime(JSONObject prediction) throws JSONException {
        long timeJson = prediction.getLong("dt")* 1000;
        Date time = new Date(timeJson);
        return time;
    }

    public static String getApparentTemperatureText(double tempMin, double tempMax, double humidity,
                                                    double windSpeed, double radiation) {

        int apparentMax = (int) Math.round(Weather.getInstance().apparentTemperature(tempMax, humidity, windSpeed, radiation));
        int apparentMin = (int) Math.round(Weather.getInstance().apparentTemperature(tempMin, humidity, windSpeed, radiation));

        return temperatureText(apparentMin, apparentMax);
    }

    public static String temperatureText(double tempMinParam, double tempMaxParam) {

        int tempMin = (int) Math.round(tempMinParam);
        int tempMax = (int) Math.round(tempMaxParam);

        String temperatureString = tempMax + "\u00ba | " + tempMin + "\u00ba";

        if(tempMax == tempMin){
            temperatureString = tempMax + "\u00ba";
        }

        return temperatureString;
    }

    public String getWeatherDescription(JSONObject prediction, double rain, double snow, int hourInterval, WeatherConditionManager.WeatherCondition weatherCondition) throws JSONException {
        String weatherDescription = "";
        JSONArray weatherArray = prediction.getJSONArray("weather");

        if(weatherArray != null && weatherArray.length() > 0) {
            try {
                JSONObject weatherObj = weatherArray.getJSONObject(0);

                String id = weatherObj.getString("id");

                if(weatherCondition == null) {
                    // Override weather id with rain formula
                    id = overrideWeatherId(id, rain);
                }else{
                    id = WeatherConditionManager.getInstance().getIdByWeatherCondition(weatherCondition);
                }

                weatherDescription = weatherObj.getString("description");

                // i18n and translation.json from ConfigurationManager
                JSONObject translationObject = ConfigurationManager.getInstance().getJsonTranslation();
                if(translationObject != null){
                    String newDescription = translationObject.getString(id);
                    if(newDescription != null){
                        weatherDescription = newDescription;
                    }
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing weather json for description: " + weatherArray, e);
            }
        }

        return weatherDescription;
    }

    public String getWeatherId(JSONObject prediction, double rain) throws JSONException {

        JSONArray weatherArray = prediction.getJSONArray("weather");

        if(weatherArray != null && weatherArray.length() > 0) {
            try {
                JSONObject weatherObj = weatherArray.getJSONObject(0);

                String id = weatherObj.getString("id");

                // Override weather id with rain formula
                id = overrideWeatherId(id,rain);

                return id;
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing weather json for image: " + weatherArray, e);
            }
        }

        return null;
    }

    public LinearLayout getWeatherImage(JSONObject prediction, double rain, double snow, int color,
                                        int hourInterval,
                                        WeatherConditionManager.WeatherCondition weatherCondition) throws JSONException {
        LinearLayout result = null;
        JSONArray weatherArray = prediction.getJSONArray("weather");

        if(weatherArray != null && weatherArray.length() > 0) {
            try {
                JSONObject weatherObj = weatherArray.getJSONObject(0);

                String id = getWeatherId(prediction, rain);

                String icon = weatherObj.getString("icon");

                int imageResource = ConfigurationManager.getInstance().iconForCode(icon, id);

                ImageView iconView = createImage(65);
                iconView.setImageResource(imageResource);
                iconView.setBackgroundColor(color);

                iconView.setScaleType(ImageView.ScaleType.FIT_XY);

                result = new LinearLayout(SharedResources.getInstance().getContext());
                result.addView(iconView, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing weather json for image: " + weatherArray, e);
            }
        }
        return result;
    }

    private String overrideWeatherId(String id, double rain) {
        /*
        "500":"light rain",
        "501":"moderate rain",
        "502":"heavy intensity rain",
        "503":"very heavy rain",
        "504":"extreme rain"
        */

        // If is a icon of rain
        if (isLightRainIcon(id)) {

            if(rain < WeatherConditionManager.LIGHT_RAIN_MIN) {
                return WeatherConditionManager.getInstance().getIdByWeatherCondition(
                        WeatherConditionManager.WeatherCondition.CLOUDS);
            }

        }

        return id;

    }

    protected boolean isLightRainIcon(String id){
        if(id.equals("500")){
            return true;
        }

        return false;
    }

    public static boolean isRainIcon(String id) {
        if(id.equals("500") || id.equals("501") || id.equals("502") || id.equals("503") || id.equals("504")){
            return true;
        }
        return false;
    }


    public static ImageView createImage(int size) {
        ImageView imageView = new ImageView(SharedResources.getInstance().getContext());

        imageView.setMaxHeight(size);
        imageView.setMaxWidth(size);
        imageView.setMinimumHeight(size);
        imageView.setMinimumWidth(size);

        return imageView;
    }


    public abstract String timeInterval(JSONObject prediction, int position) throws JSONException;

    public abstract String temperatureTextRetrieve(JSONObject prediction, boolean apparentTemperature, int position, int hourInterval) throws JSONException;

    public abstract double humidity(JSONObject prediction) throws JSONException;

    public abstract double getRain(JSONObject prediction) throws JSONException;

    public abstract double getSnow(JSONObject prediction) throws JSONException;

    public abstract double windDegree(JSONObject prediction) throws JSONException;

    public abstract double windSpeed(JSONObject prediction) throws JSONException;

    public abstract double getCloudiness(JSONObject prediction) throws JSONException;

    public abstract double getGoodWeatherRatio(JSONObject prediction, boolean apparentTemperature) throws JSONException;

    public double calculateGoodWeatherRatio(double tempMax, double tempMin, double humidity, double windSpeed, double radiation, boolean apparentTemperature) {

        int tempFinalMin = (int) tempMin;
        int tempFinalMax = (int) tempMax;
        Double ratio = null;

        if(apparentTemperature){
            tempFinalMax = (int) Math.round(Weather.getInstance().apparentTemperature(
                    tempMax, humidity, windSpeed, radiation));
            tempFinalMin = (int) Math.round(Weather.getInstance().apparentTemperature(
                    tempMin, humidity, windSpeed, radiation));
        }

        ratio = Weather.getInstance().ratio(tempFinalMin, tempFinalMax);

        if(ratio == null) {
            ratio = Double.valueOf(0);
        }

        return ratio;
    }
}
