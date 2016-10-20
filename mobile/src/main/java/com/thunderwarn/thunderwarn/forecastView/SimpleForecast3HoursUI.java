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

import com.thunderwarn.thunderwarn.OpenWeatherMap.processors.Json3HoursProcessor;
import com.thunderwarn.thunderwarn.activities.DetailsActivity;
import com.thunderwarn.thunderwarn.MainActivity;
import com.thunderwarn.thunderwarn.R;
import com.thunderwarn.thunderwarn.common.SharedResources;
import com.thunderwarn.thunderwarn.common.Weather;
import com.thunderwarn.thunderwarn.common.configuration.UserPreferencesManager;
import com.thunderwarn.thunderwarn.common.slider.SlidingItem;
import com.thunderwarn.thunderwarn.common.slider.SlidingViewPagerFragment;
import com.thunderwarn.thunderwarn.common.configuration.ConfigurationManager;
import com.thunderwarn.thunderwarn.common.configuration.LayoutManager;
import com.thunderwarn.thunderwarn.manager.WeatherDataManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Ivo on 27-06-2015.
 */
public class SimpleForecast3HoursUI extends SlidingItem {

    private static String TAG = "SimpleForecast3HoursUI";
    private final SimpleForecastUI simpleForecastUI;

    private SharedResources sharedResources = SharedResources.getInstance();
    private LayoutManager layoutManager = LayoutManager.getInstance();
    private ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    private Weather weather = Weather.getInstance();

    private int pagesNum;

    // Cache for 3 hours data

    // 3 hours vars
    private JSONObject json3Hours = null;
    private JSONArray predictionsList = null;

    private String lastDateString = null;

    private boolean debug = false;

    // Current data
    private JSONObject jsonCurrent = null;
    private LinearLayout firstPredictionView = null;
    private int predictionShift;
    private Json3HoursProcessor processor = new Json3HoursProcessor();
    private Date lastUpdate = null;


    public SimpleForecast3HoursUI(Context context, SimpleForecastUI simpleForecastUI) {
        super(context);
        this.simpleForecastUI = simpleForecastUI;
    }

    public void reset() {
        json3Hours = null;
        jsonCurrent = null;
        ExtrapolationFrom3htoDaily.getInstance().clear();
    }

    /**
     * Init the sliding fragment
     * @param json 3 hours json
     * @param lastUpdate date when the json was crawled from internet
     * @throws JSONException
     */
    public void create3hViewFromJson(JSONObject json, Date lastUpdate) throws JSONException {
        Log.d(TAG,"create 3h view");
        this.json3Hours = json;
        this.lastUpdate = lastUpdate;

        // add new components to the 3 hours prediction view
        this.predictionsList = json.getJSONArray("list");
        ExtrapolationFrom3htoDaily.getInstance().extrapolate3hours(predictionsList, processor);
        calculatePagesNumber();

        // Define the size
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setLayoutParams(params);

        this.setOrientation(VERTICAL);
        this.setId(R.id.id3hours);

        FragmentTransaction transaction =
                sharedResources.getFragmentManager().beginTransaction();

        SlidingViewPagerFragment fragment = new SlidingViewPagerFragment();
        fragment.setPagesNum(this.pagesNum);
        fragment.setForecastUI(this);
        transaction.replace(R.id.id3hours, fragment);
        transaction.commit();
    }

    @Override
    public View viewForPosition(ViewGroup container,int position) {
        Log.d(TAG,"3h fragment for position " + position);
        MainActivity.getInstance().loadingComplete();

        // show last update if already passed 90 minutes from the data date
        int minutes = 90;
        if(!WeatherDataManager.insideDateThreshold(this.lastUpdate, minutes)) {
            String dateString = sharedResources.formatDate(this.lastUpdate) + " "
                    + sharedResources.getTimeFormat().format(this.lastUpdate);
            MainActivity.getInstance().setLastUpdate(dateString,layoutManager.getForegroundColor());
        }else{
            MainActivity.getInstance().setLastUpdate(null,layoutManager.getForegroundColor());
        }

        // Generate the view with forecasts
        int i = position * simpleForecastUI.getPredictionsOnScreen();

        LinearLayout predictionGroupView = createPrediction3HoursGroupView(this.predictionsList, i);

        container.addView(predictionGroupView);
        return predictionGroupView;

    }

    private void calculatePagesNumber() throws JSONException {

        int size = this.predictionsList.length();
        this.predictionShift = 0;
        Date currentDate = new Date();

        for (int i=0 ; i<this.predictionsList.length() ; i++){

            JSONObject prediction = predictionsList.getJSONObject(i);

            long timeJson = prediction.getLong("dt")* 1000;
            Date time = new Date(timeJson);

            // Add 3 hours to go the end of the prediction
            long newTime = time.getTime() + 3 * 1000 * 60 * 60;
            time.setTime(newTime);

            // If this prediction is valid stop the cycle
            if(currentDate.before(time)){
                break;
            }else{
                this.predictionShift++;
                size--;
            }
        }

        double aux = size / simpleForecastUI.getPredictionsOnScreen();
        this.pagesNum = (int) aux; // Truncate to avoid have a view half filled

    }



    /**
     * Creates the prediction for each slide of the sliding fragment
     * @param predictionsList list of predictions in json
     * @param cellNumber number of the slider bullet
     * @return the layout with 3 predictions
     */
    private LinearLayout createPrediction3HoursGroupView(JSONArray predictionsList, int cellNumber) {

        LinearLayout predictionGroupView = new LinearLayout(sharedResources.getContext());
        predictionGroupView.setOrientation(LinearLayout.HORIZONTAL);
        predictionGroupView.setBackgroundColor(layoutManager.getBackgroundColor());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        predictionGroupView.setLayoutParams(params);


        // Shows the number of predictions that should be presented on screen if fit in the number of cells
        int i = cellNumber +  this.predictionShift;
        int screenLimit = i + simpleForecastUI.getPredictionsOnScreen();
        for (; i  < screenLimit && i<=predictionsList.length() ; i++){
            try {
                LinearLayout predictionView = new LinearLayout(sharedResources.getContext());
                if (i == 0 && this.predictionShift == 0) {

                    this.firstPredictionView = predictionView;

                    if(jsonCurrent != null) {
                        prediction(this.jsonCurrent, predictionView, true);
                        ExtrapolationFrom3htoDaily.getInstance().extrapolateCurrent(jsonCurrent, processor);
                    }
                } else {
                    int index = i - 1;
                    if(this.predictionShift == 0){
                        index = i;
                    }

                    JSONObject prediction = predictionsList.getJSONObject(index);
                    prediction(prediction, predictionView, false);
                }
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params2.weight = 1.0f;
                predictionView.setLayoutParams(params2);

                // Create the onclick listener for 3hours
                final int position = i;
                predictionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(sharedResources.getContext(), DetailsActivity.class);
                        intent.putExtra(DetailsActivity.EXTRA_POSITION, position);
                        intent.putExtra(DetailsActivity.EXTRA_TYPE, DetailsActivity.TYPE_3HOURS);
                        MainActivity.getInstance().startActivity(intent);
                    }
                });

                predictionGroupView.addView(predictionView);

            }catch (JSONException e){
                Log.e(TAG,"error generating view for cell "+ i,e);
            }
        }
        this.lastDateString = null;
        return predictionGroupView;
    }

    private void prediction(JSONObject prediction, LinearLayout predictionView, boolean currentWeatherPrediction) throws JSONException {

        // Get the time of the prediction
        Date time = processor.getTime(prediction);

        String dateString = sharedResources.formatDate(time);

        // If already exists a last date in the group and is the same as this date
        if(this.lastDateString != null && this.lastDateString.equals(dateString)){
            //Don't need to print the date again
            dateString = null;
        }else{
            this.lastDateString = dateString;
        }

        // Create the prediction view
        createPrediction3Hours(prediction, dateString, time, predictionView);
    }

    /**
     * Show an individual prediction for a 3 hours interval
     * @param prediction json data
     * @param dateString date of the prediction formatted
     * @param time date of the prediction
     * @param predictionView view to insert the prediction
     * @throws JSONException
     */
    private void createPrediction3Hours(JSONObject prediction, String dateString, Date time,
                                        LinearLayout predictionView) throws JSONException {

        // Json structure
        JSONObject main = prediction.getJSONObject("main");
        JSONObject wind = prediction.getJSONObject("wind");

        // get fields
        double radiation = 1.3;
        double tempMaxSource = main.getDouble("temp_max");
        double tempMinSource = main.getDouble("temp_min");
        double humidity = main.getDouble("humidity");
        int tempMax = (int) Math.round(main.getDouble("temp_max"));
        int tempMin = (int) Math.round(main.getDouble("temp_min"));
        double temp = main.getDouble("temp");

        tempMax = (int) Math.round(temp);
        tempMin = (int) Math.round(temp);
        tempMaxSource = temp;
        tempMinSource = temp;

        double windSpeed = wind.getDouble("speed");

        // precipitation
        double precipitation = 0;
        double rain = 0;
        double snow = 0;

        try {
            rain = processor.getRain(prediction);
            precipitation += rain;
        }catch(Exception e){
            Log.e(TAG,"Error getting rain values in json: "+ prediction.toString(),e);
        }

        try {
            snow = processor.getSnow(prediction);
            precipitation += snow;
        }catch(Exception e){
            Log.e(TAG,"Error getting snow values in json: "+ prediction.toString(),e);
        }

        // Use formulas
        if(UserPreferencesManager.getInstance().isApparentTemperature()){
            tempMax = (int) Math.round(weather.apparentTemperature(tempMaxSource, humidity, windSpeed, radiation));
            tempMin = (int) Math.round(weather.apparentTemperature(tempMinSource, humidity, windSpeed, radiation));
        }

        // weather ratio
        Double ratio = weather.ratio(tempMin, tempMax);
        int color = layoutManager.colorForWeather(ratio);

        String timeString = sharedResources.formatTime(time);

        // Create UI objects
        TextView dateView = this.simpleForecastUI.textView(color);
        if(dateString != null) {
            dateView.setText(dateString);
        }

        TextView timeView = this.simpleForecastUI.textView(color);
        timeView.setText(timeString);

        TextView temperatureView = this.simpleForecastUI.textView(color);
        temperatureView.setText(Json3HoursProcessor.temperatureText(tempMin, tempMax));

        LinearLayout iconView = processor.getWeatherImage(prediction, rain, snow, color, 3, null);

        // create the final layout
        predictionView.setOrientation(LinearLayout.VERTICAL);
        predictionView.addView(dateView);
        predictionView.addView(timeView);
        if(iconView != null) {
            iconView.setGravity(Gravity.CENTER);
            predictionView.addView(iconView);
        }

        if(debug) {
            TextView precipitationView = this.simpleForecastUI.textView(color);
            precipitationView.setText(precipitation + "mm");

            TextView ratioView = this.simpleForecastUI.textView(color);
            ratioView.setText((int) (ratio * 100) + " R");

            TextView codesView = this.simpleForecastUI.textView(color);
            codesView.setText(SimpleForecastUI.weatherCodes(prediction));

            predictionView.addView(precipitationView);
            predictionView.addView(ratioView);
            predictionView.addView(codesView);

        }

        predictionView.addView(temperatureView);

    }

    public void createCurrentViewFromJson(JSONObject jsonCurrent) {
        this.jsonCurrent = jsonCurrent;

        if(this.firstPredictionView != null){
            Log.i(TAG,"update current prediction view");

            try {
                prediction(this.jsonCurrent,this.firstPredictionView,true);

                ExtrapolationFrom3htoDaily.getInstance().extrapolateCurrent(jsonCurrent, processor);
            } catch (JSONException e) {
                Log.e(TAG, "error updating current prediction view",e);
            }
        }

    }

    public JSONObject getJson3Hours() {
        return json3Hours;
    }

    public JSONObject getPrediction(int position){
        if(predictionShift == 0 && position == 0){
            return jsonCurrent;
        }else {
            try {
                if(predictionShift > 0){
                    position--;
                }
                return this.predictionsList.getJSONObject(position);
            } catch (JSONException e) {
                Log.e(TAG, "Error getting predition for position " + position + ": " +e.getMessage());
                return null;
            }
        }
    }

    public JSONObject getJsonCurrent() {
        return jsonCurrent;
    }

}