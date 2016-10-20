package com.thunderwarn.thunderwarn.forecastView;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import com.thunderwarn.thunderwarn.common.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thunderwarn.thunderwarn.MainActivity;
import com.thunderwarn.thunderwarn.OpenWeatherMap.processors.JsonDailyProcessor;
import com.thunderwarn.thunderwarn.OpenWeatherMap.processors.JsonProcessor;
import com.thunderwarn.thunderwarn.R;
import com.thunderwarn.thunderwarn.activities.DetailsActivity;
import com.thunderwarn.thunderwarn.common.SharedResources;
import com.thunderwarn.thunderwarn.common.Weather;
import com.thunderwarn.thunderwarn.common.configuration.UserPreferencesManager;
import com.thunderwarn.thunderwarn.common.slider.SlidingItem;
import com.thunderwarn.thunderwarn.common.slider.SlidingViewPagerFragment;
import com.thunderwarn.thunderwarn.common.configuration.ConfigurationManager;
import com.thunderwarn.thunderwarn.common.configuration.LayoutManager;
import com.thunderwarn.thunderwarn.data.WeatherPrediction;
import com.thunderwarn.thunderwarn.manager.WeatherConditionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Ivo on 29-06-2015.
 */
public class SimpleForecastDailyUI extends SlidingItem {

    private static String TAG = "SimpleForecastDailyUI";
    private final SimpleForecastUI simpleForecastUI;
    private final SimpleForecast3HoursUI viewForecast3hours;

    private SharedResources sharedResources = SharedResources.getInstance();
    private LayoutManager layoutManager = LayoutManager.getInstance();
    private ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    private Weather weather = Weather.getInstance();

    // Json vars
    private JsonDailyProcessor processor = new JsonDailyProcessor();
    private JSONObject json;
    private JSONArray predictionsList = null;
    private boolean testingRatio = false;
    private int pagesNum;
    private int predictionShift;


    public SimpleForecastDailyUI(Context context, SimpleForecastUI simpleForecastUI,
                                 SimpleForecast3HoursUI viewForecast3hours) {
        super(context);
        this.simpleForecastUI = simpleForecastUI;
        this.viewForecast3hours = viewForecast3hours;
    }

    public void createViewFromJson(JSONObject json) throws JSONException {
        this.json = json;

        // add new components to the 3 hours prediction view
        this.predictionsList = json.getJSONArray("list");
        calculatePagesNumber();

        // Define the size
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(params);

        this.setOrientation(VERTICAL);
        this.setId(R.id.idDaily);

        FragmentTransaction transaction =
                sharedResources.getFragmentManager().beginTransaction();

        SlidingViewPagerFragment fragment = new SlidingViewPagerFragment();
        fragment.setPagesNum(this.pagesNum);
        fragment.setPosition(1);
        fragment.setForecastUI(this);
        transaction.replace(R.id.idDaily, fragment);
        transaction.commit();
    }

    private void calculatePagesNumber() throws JSONException {

        int size = this.predictionsList.length();
        this.predictionShift = 0;
        Date currentDate = new Date();

        for (int i=0 ; i<this.predictionsList.length() ; i++) {

            JSONObject prediction = predictionsList.getJSONObject(i);

            long timeJson = prediction.getLong("dt") * 1000;
            Date time = new Date(timeJson);

            // Add 24 hours to go the end of the prediction
            long newTime = time.getTime() + 24 * 1000 * 60 * 60;
            time.setTime(newTime);

            // If this prediction is valid stop the cycle
            Date firstPreditionTime =
                    ExtrapolationFrom3htoDaily.getInstance().getFirstPreditionTime();
            if(currentDate.before(time) && firstPreditionTime.before(time)){
                break;
            }else{
                this.predictionShift++;
                size--;
            }
        }

        double aux = size / simpleForecastUI.getPredictionsOnScreen();
        this.pagesNum = (int) aux;
    }

    @Override
    public View viewForPosition(ViewGroup container, int position) {
        int predictionNumber = position * simpleForecastUI.getPredictionsOnScreen();

        LinearLayout predictionGroupView = createPredictionDailyGroupView(this.predictionsList, predictionNumber);

        container.addView(predictionGroupView);
        return predictionGroupView;
    }

    private LinearLayout createPredictionDailyGroupView(JSONArray predictionsList, int cellNumber) {
        LinearLayout predictionGroupView = new LinearLayout(sharedResources.getContext());
        predictionGroupView.setOrientation(LinearLayout.HORIZONTAL);
        predictionGroupView.setBackgroundColor(layoutManager.getBackgroundColor());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        predictionGroupView.setLayoutParams(params);
        int i = cellNumber +  this.predictionShift;
        int screenLimit = i + simpleForecastUI.getPredictionsOnScreen();

        // Group n predictions in the screen at the same time
        for (; cellNumber<predictionsList.length() && i  < screenLimit ; i++){

            LinearLayout predictionView = new LinearLayout(sharedResources.getContext());

            try{
                JSONObject prediction = predictionsList.getJSONObject(i);

                // Get the time of the prediction
                long timeJson = prediction.getLong("dt")* 1000;
                Date time = new Date(timeJson);

                String dateString = sharedResources.formatDate(time);

                // Create the prediction view
                predictionView = createPredictionDaily(prediction, dateString, time, cellNumber);

                if(predictionView == null) {
                    continue;
                }
                // Create the onclick listener
                final int position = i;
                predictionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(sharedResources.getContext(), DetailsActivity.class);
                        intent.putExtra(DetailsActivity.EXTRA_POSITION, position);
                        intent.putExtra(DetailsActivity.EXTRA_TYPE, DetailsActivity.TYPE_DAILY);
                        MainActivity.getInstance().startActivity(intent);
                    }
                });
            }catch (JSONException e){
                Log.e(TAG, "Error parsing daily predictions", e);
            }

            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params2.weight = 1.0f;
            predictionView.setLayoutParams(params2);
            predictionGroupView.addView(predictionView);
        }

        return predictionGroupView;
    }

    public void createDailyFrom3h(JSONObject response) {

    }

    private LinearLayout createPredictionDaily(JSONObject prediction, String dateString, Date time, int predictionNumber) throws JSONException {

        // Try to get a prediction based on the 3 hours prediction,
        // to be more accurate about min and max apparent temperature
        WeatherPrediction weatherPrediction =
                ExtrapolationFrom3htoDaily.getInstance().predictionForDay(dateString,time);

        // Json structure
        JSONObject temp = prediction.getJSONObject("temp");

        // get fields
        double radiation = 1.3;

        double humidity = prediction.getDouble("humidity");
        int tempMax = (int) Math.round(temp.getDouble("max"));
        int tempMin = (int) Math.round(temp.getDouble("min"));
        double windSpeed = prediction.getDouble("speed");

        // precipitation
        double precipitation = 0;
        double rain = 0;
        double snow = 0;

        if(prediction.has("rain")){
            rain = prediction.getDouble("rain");
            precipitation += rain;
        }

        if(prediction.has("snow")){
            snow = prediction.getDouble("snow");
            precipitation += snow;
        }


        // weather ratio
        Double ratio = Double.valueOf(0);

        if(testingRatio){
            tempMin = (predictionNumber*5)-30;
            tempMax = (predictionNumber*5)-30;
        }

        String temperatureString = "";
        if(UserPreferencesManager.getInstance().isApparentTemperature() && !testingRatio) {

            if(weatherPrediction != null){
                tempMin = weatherPrediction.getApparentMin();
                tempMax = weatherPrediction.getApparentMax();
            }

            temperatureString = JsonProcessor.temperatureText(tempMin, tempMax);
            ratio = weather.ratio(tempMin, tempMax);
        }else{
            temperatureString = JsonProcessor.temperatureText(tempMin, tempMax);
            ratio = weather.ratio(tempMin, tempMax);
        }

        int color = layoutManager.colorForWeather(ratio);


        TextView temperatureView = this.simpleForecastUI.textView(color);
        temperatureView.setText(temperatureString);

        // Create UI objects
        TextView dateView = this.simpleForecastUI.textView(color);
        if (dateString != null) {
            dateView.setText(dateString);
        }

        WeatherConditionManager.WeatherCondition weatherCondition = null;

        if(weatherPrediction != null) {
            weatherCondition = weatherPrediction.getWeatherCondition();
        }

        LinearLayout iconView = processor.getWeatherImage(prediction, rain, snow, color, 24, weatherCondition);
        if(iconView != null) {
            iconView.setGravity(Gravity.CENTER);
        }else{
            Log.e(TAG,"can't get icon view for " +prediction);
            return null;
        }
        // Create the final layout
        LinearLayout predictionView = new LinearLayout(sharedResources.getContext());
        predictionView.setOrientation(LinearLayout.VERTICAL);
        predictionView.addView(new TextView(sharedResources.getContext()));
        predictionView.addView(dateView);

        predictionView.addView(iconView);

        predictionView.addView(temperatureView);

        return predictionView;
    }

    public JSONObject getPrediction(int position) {
        try {

            return this.predictionsList.getJSONObject(position);
        } catch (JSONException e) {
            Log.e(TAG, "Error getting daily predition for position " + position + ": " +e.getMessage());
            return null;
        }
    }
}
