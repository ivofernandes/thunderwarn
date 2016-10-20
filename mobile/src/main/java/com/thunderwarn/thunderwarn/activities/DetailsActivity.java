package com.thunderwarn.thunderwarn.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import com.thunderwarn.thunderwarn.common.Log;

import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.thunderwarn.thunderwarn.OpenWeatherMap.processors.JsonDailyProcessor;
import com.thunderwarn.thunderwarn.OpenWeatherMap.processors.JsonProcessor;
import com.thunderwarn.thunderwarn.R;
import com.thunderwarn.thunderwarn.common.SharedResources;
import com.thunderwarn.thunderwarn.OpenWeatherMap.processors.Json3HoursProcessor;
import com.thunderwarn.thunderwarn.data.WeatherPrediction;
import com.thunderwarn.thunderwarn.forecastView.ExtrapolationFrom3htoDaily;
import com.thunderwarn.thunderwarn.manager.ForecastInterfaceManager;
import com.thunderwarn.thunderwarn.common.configuration.LayoutManager;
import com.thunderwarn.thunderwarn.common.i18n.UnitManager;
import com.thunderwarn.thunderwarn.manager.UserLocationManager;
import com.thunderwarn.thunderwarn.manager.WeatherConditionManager;

import org.json.JSONObject;

import java.util.Date;

/**
 * Activity shown when the user clicks in a forecast,
 * the objective is show more details about the weather
 */
public class DetailsActivity extends ActionBarActivity {

    // Constants
    public static final String TAG = "DetailsActivity";

    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_TYPE = "type";
    public static final String TYPE_3HOURS = "3_HOURS";
    public static final String TYPE_DAILY = "DAILY";

    // Fields
    private ForecastInterfaceManager interfaceManager = ForecastInterfaceManager.getInstance();
    private LayoutManager layoutManager = LayoutManager.getInstance();
    private UserLocationManager userLocationManager = UserLocationManager.getInstance();

    private LinearLayout panel;
    private JSONObject prediction;
    private JsonProcessor processor;

    private int color;
    private double ratioValue;
    private double rain;
    private double snow;
    private String type;
    private int hourInterval;
    private String dateString = "";
    private Date time = null;
    private WeatherPrediction weatherPrediction = null;
    private WeatherConditionManager.WeatherCondition weatherCondition = null;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if(userLocationManager.getLocationText() != null && !userLocationManager.getLocationText().equals("")) {
            setTitle(userLocationManager.getLocationText());
        }

        // Change colors
        RelativeLayout mainPanel = (RelativeLayout) findViewById(R.id.id3hoursDetailsMainPanel);
        mainPanel.setBackgroundColor(layoutManager.getBackgroundColor());

        this.panel = (LinearLayout) findViewById(R.id.id3hoursDetailsPanel);
        panel.setBackgroundColor(layoutManager.getBackgroundColor());

        // Generate panel
        Intent intent = getIntent();
        this.position = intent.getIntExtra(DetailsActivity.EXTRA_POSITION, -1);
        this.type = intent.getStringExtra(DetailsActivity.EXTRA_TYPE);

        if (this.position >= 0) {
            // 3 hours
            if (TYPE_3HOURS.equals(type)) {
                this.prediction = interfaceManager.get3HoursPrediction(position);
                this.processor = new Json3HoursProcessor();
                hourInterval = 3;
                render();
            } else if(TYPE_DAILY.equals(type)) {
                this.prediction = interfaceManager.getDailyPrediction(position);
                this.processor = new JsonDailyProcessor();
                hourInterval = 24;
                render();
            }
        }

        AdView mAdView = (AdView) findViewById(R.id.adImageView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    @Override
    protected void onResume() {
        super.onResume();

        final ScrollView idDetailsScrollPanel = (ScrollView) findViewById(R.id.idDetailsScrollPanel);
        (new Thread(new Runnable(){
            public void run(){
                idDetailsScrollPanel.fullScroll(View.FOCUS_UP);
            }
        })).start();

    }

    private void render() {

        init();
        
        overview();

        temperatureAndHumidity();

        wind();

        precipitation();

        details();

        //ratio();
    }

    private void init() {
        try {

            // Get the time of the prediction
            long timeJson = prediction.getLong("dt")* 1000;
            this.time = new Date(timeJson);

            this.dateString = SharedResources.getInstance().formatDate(time);

            if(this.hourInterval == 24){
                // Try to get a prediction based on the 3 hours prediction,
                // to be more accurate about min and max apparent temperature
                weatherPrediction =
                        ExtrapolationFrom3htoDaily.getInstance().predictionForDay(dateString,time);

                if(weatherPrediction != null){
                    weatherCondition = weatherPrediction.getWeatherCondition();
                }
            }

            this.color = LayoutManager.getInstance().getForegroundColor();

            this.rain = processor.getRain(prediction);
            this.snow = processor.getSnow(prediction);


        }catch(Exception e){
            Log.e(TAG,"Error getting time interval: " + e.getMessage(), e);
        }
    }


    private void overview() {

        // Time Interval
        try {
            String timeInterval = processor.timeInterval(prediction,position);

            panel.addView(titleText(R.string.time_interval));
            panel.addView(descriptionText(timeInterval));

        } catch (Exception e) {
            Log.e(TAG,"Error getting time interval: " + e.getMessage(), e);
        }

        // Weather Icon
        try {

            LinearLayout weatherImage = processor.getWeatherImage(prediction,rain
                    , this.snow, this.color, this.hourInterval, weatherCondition);

            String weatherDescription = processor.getWeatherDescription(prediction, rain,
                    this.snow, this.hourInterval, weatherCondition);

            panel.addView(weatherImage);
            panel.addView(descriptionText(weatherDescription));
        } catch (Exception e) {
            Log.e(TAG,"Error getting weather image: " + e.getMessage(), e);
        }
    }

    private void temperatureAndHumidity() {
        // Temperature
        try {
            String temperature = "";

            if(this.weatherPrediction != null){
                temperature = JsonProcessor.temperatureText(weatherPrediction.getTempMin(),
                        weatherPrediction.getTempMax());
            }else {
                temperature = processor.temperatureTextRetrieve(prediction, false, position,hourInterval);
            }
            panel.addView(titleText(R.string.temperature));
            panel.addView(descriptionText(temperature));
        } catch (Exception e) {
            Log.e(TAG,"Error getting temperature: " + e.getMessage(), e);
        }

        // Apparent Temperature
        try {
            String temperature = "";

            if(this.weatherPrediction != null){
                temperature = JsonProcessor.temperatureText(weatherPrediction.getApparentMin(),
                        weatherPrediction.getApparentMax());
            }else {
                temperature = processor.temperatureTextRetrieve(prediction, true, position, hourInterval);
            }

            panel.addView(titleText(R.string.apparent_temperature));
            panel.addView(descriptionText(temperature));
        } catch (Exception e) {
            Log.e(TAG,"Error getting apparent temperature: " + e.getMessage(), e);
        }

        // Humidity
        try {
            String humidity = processor.humidity(prediction) + "%";

            panel.addView(titleText(R.string.humidity));
            panel.addView(descriptionText(humidity));
        } catch (Exception e) {
            Log.e(TAG,"Error getting humidity: " + e.getMessage(), e);
        }
    }

    private void precipitation() {

        // Rain
        try {
            String rain = this.rain + UnitManager.getInstance().precipitation();

            panel.addView(titleText(R.string.rain));
            panel.addView(descriptionText(rain));
        } catch (Exception e) {
            Log.e(TAG,"Error getting rain: " + e.getMessage(), e);
        }


        // Snow
        try {
            String snow = this.snow + UnitManager.getInstance().precipitation();

            panel.addView(titleText(R.string.snow));
            panel.addView(descriptionText(snow));
        } catch (Exception e) {
            Log.e(TAG,"Error getting snow: " + e.getMessage(), e);
        }
    }

    private void wind() {

        Double windDegree = null;

        // Wind Speed
        try {
            windDegree = processor.windDegree(prediction);
            String windSpeedText = processor.windSpeed(prediction) + " " + UnitManager.getInstance().windSpeed();

            panel.addView(titleText(R.string.wind_speed));
            panel.addView(descriptionText(windSpeedText));
        } catch (Exception e) {
            Log.e(TAG,"Error getting wind speed: " + e.getMessage(), e);
        }

        // Wind Degree
        //TODO use coordinates for wind degree, the degree is too nerd lol
        if(windDegree != null && false) {
            String windDegreeText = windDegree + "º";

            panel.addView(titleText(R.string.wind_degree));
            panel.addView(descriptionText(windDegreeText));
        }


    }

    private void details() {

        // Cloudiness
        try {
            String cloudiness = processor.getCloudiness(prediction) + "%";

            panel.addView(titleText(R.string.cloudiness));
            panel.addView(descriptionText(cloudiness));
        } catch (Exception e) {
            Log.e(TAG,"Error getting apparent temperature: " + e.getMessage(), e);
        }
    }

    private View descriptionText(String text) {
        TextView textView = text();

        textView.setText(text);

        return textView;
    }

    private TextView titleText(int textId) {
        TextView textView = text();
        textView.setTextSize(16);
        textView.setTypeface(null, Typeface.BOLD);

        String text = SharedResources.getInstance().resolveString(textId);
        textView.setText(text);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)textView.getLayoutParams();

        if(params == null){
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        params.setMargins(0, 50, 0, 0);
        textView.setLayoutParams(params);

        return textView;
    }


    private TextView text() {
        TextView textView = new TextView(SharedResources.getInstance().getContext());
        textView.setTextColor(this.color);

        return textView;
    }

}
