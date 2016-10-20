package com.thunderwarn.thunderwarn.forecastView;

import com.thunderwarn.thunderwarn.common.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thunderwarn.thunderwarn.common.configuration.ConfigurationManager;
import com.thunderwarn.thunderwarn.common.SharedResources;
import com.thunderwarn.thunderwarn.common.Weather;
import com.thunderwarn.thunderwarn.common.configuration.LayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Ivo on 20-06-2015.
 */
public class SimpleForecastUI {

    private static final String TAG = "SimpleForecastUI";

    public int predictionsOnScreen = 3;

    private SharedResources sharedResources = SharedResources.getInstance();
    private LayoutManager layoutManager = LayoutManager.getInstance();
    private ConfigurationManager configurationManager = ConfigurationManager.getInstance();

    private LinearLayout forecastView;
    private boolean cleared3HourView = false;

    private Weather weather = Weather.getInstance();

    // Sub views
    private SimpleForecast3HoursUI viewForecast3hours = null;
    private SimpleForecastDailyUI viewForecastDaily = null;


    public SimpleForecastUI(){}

    public void init(){
        if(this.viewForecast3hours == null) {

            // 3 hours view
            this.viewForecast3hours = new SimpleForecast3HoursUI(sharedResources.getContext(), this);
            //this.forecastView.addView(this.viewForecast3hours);

            // daily view
            this.viewForecastDaily = new SimpleForecastDailyUI(sharedResources.getContext(), this, this.viewForecast3hours);
            //this.forecastView.addView(this.viewForecastDaily);

        }
    }

    public void setForecastView(LinearLayout forecastPanel) {
        this.forecastView = forecastPanel;
    }


    /**
     * Generate the whole 3hours forecast
     * @param response data with 3hours forecast
     * @throws JSONException
     */
    public void show3HoursForecast(JSONObject response, Date lastUpdate) throws JSONException {

        init();
        // clear before adding new components
        clear3HourView();

        // Add new components
        viewForecast3hours.create3hViewFromJson(response, lastUpdate);
        this.viewForecastDaily.createDailyFrom3h(response);
    }

    /**
     * Generate the whole daily forecast
     * @param response data with daily forecast
     * @throws JSONException
     */
    public void showDailyForecast(JSONObject response) throws JSONException {

        init();

        // Add new components
        this.viewForecastDaily.createViewFromJson(response);
    }

    public void showCurrentForecast(JSONObject response) throws JSONException {
        init();

        // clear before adding new components
        clear3HourView();

        // Add new components
        this.viewForecast3hours.createCurrentViewFromJson(response);
    }

    private void clear3HourView() {
        if(!cleared3HourView) {
            this.viewForecast3hours.removeAllViews();

            // clear also daily before adding new components
            this.viewForecastDaily.removeAllViews();
            cleared3HourView = true;
        }
    }


    public int getPredictionsOnScreen() {
        return predictionsOnScreen;
    }

    public static String weatherCodes(JSONObject prediction) throws JSONException {

        JSONArray weatherArray = prediction.getJSONArray("weather");

        if(weatherArray != null && weatherArray.length() > 0) {
            try {
                JSONObject weatherObj = weatherArray.getJSONObject(0);

                String id = weatherObj.getString("id");
                String icon = weatherObj.getString("icon");

                return id + " | "+ icon;
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing weather json: " + weatherArray, e);
            }
        }
        return "";
    }

    public void reset() {
        if(viewForecast3hours != null){
            viewForecast3hours.reset();
        }
    }

    public JSONObject getJson3Hours() {
        return viewForecast3hours.getJson3Hours();
    }

    public JSONObject getJsonCurrent() {
        return viewForecast3hours.getJsonCurrent();
    }

    public JSONObject get3HoursPrediction(int position){
        return viewForecast3hours.getPrediction(position);
    }

    public TextView textView(int color) {
        TextView textView = new TextView(sharedResources.getContext());
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(color);

        return textView;
    }

    public JSONObject getDailyPrediction(int position) {
        return viewForecastDaily.getPrediction(position);
    }
}