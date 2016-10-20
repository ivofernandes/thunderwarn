package com.thunderwarn.thunderwarn.manager;

import android.content.SharedPreferences;
import android.os.Handler;

import com.thunderwarn.thunderwarn.OpenWeatherMap.OpenWeatherRequest;
import com.thunderwarn.thunderwarn.common.Log;
import com.thunderwarn.thunderwarn.common.SharedResources;
import com.thunderwarn.thunderwarn.common.configuration.CacheManager;
import com.thunderwarn.thunderwarn.common.configuration.ConfigurationManager;
import com.thunderwarn.thunderwarn.scheduler.NotificationSchedulingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ivo on 20-06-2015.
 *
 * Class that manage how the data is requested, received, and distributed to interface manager
 */
public class WeatherDataManager {

    // Singleton
    private static WeatherDataManager instance = new WeatherDataManager();

    private WeatherDataManager() {}

    public static WeatherDataManager getInstance() {
        return instance;
    }

    // Enums
    public enum WeatherRequestType {
        UPDATE_VIEWS, // Request called by the user, refresh all views
        NOTIFICATION, // Request to send a notifications

    };

    private enum RequestState{
        REQUESTING,
        DONE
    }

    // Constants
    private final ForecastInterfaceManager forecastInterfaceManager = ForecastInterfaceManager.getInstance();
    private final SharedResources sharedResources = SharedResources.getInstance();
    private final OpenWeatherRequest openWeatherRequest = OpenWeatherRequest.getInstance();
    private static final int REQUESTS_TIMEOUT = 60 * 1000;

    // Fields
    private Map<String,String> options = new HashMap<String,String>();
    private RequestState state = RequestState.DONE;
    private WeatherRequestType weatherRequestType;
    private double latitude;
    private double longitude;

    private JSONObject jsonCurrent = null;
    private JSONObject json3h = null;
    private JSONObject jsonDaily = null;
    private boolean step3h = false;
    private boolean stepDaily = false;
    private Date lastUpdate = null;
    private int processCurrentAnd3hCount = 0;
    private Handler customHandler  = new Handler();;

    // Actions
    public void requestAllData(double latitude, double longitude,WeatherRequestType weatherRequestType) {

        // Reset vars
        this.state = RequestState.REQUESTING;
        this.jsonCurrent = null;
        this.json3h = null;
        jsonDaily = null;
        step3h = false;
        stepDaily = false;
        this.processCurrentAnd3hCount = 0;

        if(weatherRequestType.equals(WeatherDataManager.WeatherRequestType.UPDATE_VIEWS)) {
            forecastInterfaceManager.reset();
        }

        // Set global params
        this.weatherRequestType = weatherRequestType;
        this.latitude = latitude;
        this.longitude = longitude;

        //TODO Create timeout for responses that put the state DONE

        customHandler.postDelayed(new Runnable() {
            public void run() {
                state = RequestState.DONE;
            }
        }, REQUESTS_TIMEOUT);

        //TODO responses only work if still in state response
        //TODO requests when the state is already requesting will need to wait...

        // Make the request
        if(options.isEmpty()) {
            addLocaleOption(options);
        }

        openWeatherRequest.request_current_3hours_daily(latitude,longitude, options,weatherRequestType);
    }

    private void addLocaleOption(Map<String,String> options){
        //TODO metric
        //&units=metric
        if(true || ConfigurationManager.getInstance().isMetric()){
            options.put("units", "metric");
        }
    }


    public void response(String response, JSONObject json, String requestType,
                         WeatherRequestType weatherRequestType, boolean dataFromCache,
                         Date lastUpdateDate) throws JSONException {

        this.lastUpdate = lastUpdateDate;

        // Store the jsons
        if(requestType.equals(OpenWeatherRequest.REQUEST_DATA_3_HOURS)) {
            this.json3h = json;
        } else if(requestType.equals(OpenWeatherRequest.REQUEST_DATA_DAILY)) {
            this.jsonDaily = json;
        } else if(requestType.equals(OpenWeatherRequest.REQUEST_DATA_CURRENT)) {
            this.jsonCurrent = json;
        }

        // cache the data
        if(!dataFromCache) {
            cacheData(response, requestType,latitude,longitude);
        }

        // Try process the requests
        processRequests(weatherRequestType);

        // Update the wake up control
        if(this.weatherRequestType.equals(WeatherDataManager.WeatherRequestType.NOTIFICATION)){
            NotificationSchedulingService.getInstance().updated(requestType);
        }
    }

    /**
     * Insert data in android local cache
     * @param response
     * @param requestType
     * @param latitude
     * @param longitude
     */
    private void cacheData(String response, String requestType, double latitude, double longitude) {

        String keyDate =
                CacheManager.getPreferenceFor(requestType, CacheManager.SUFIX_DATE);
        String keyLocation =
                CacheManager.getPreferenceFor(requestType, CacheManager.SUFIX_LOCATION);
        String keyData =
                CacheManager.getPreferenceFor(requestType, CacheManager.SUFIX_DATA);

        SharedPreferences sharedPreferences = CacheManager.getInstance().getSharedPreferences();

        String locationString = latitude + "|" + longitude;
        String dateString = CacheManager.DATE_FORMAT.format(new Date());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(keyDate, dateString);
        editor.putString(keyLocation, locationString);
        editor.putString(keyData, response);

        editor.commit();

    }

    private void processRequests(WeatherRequestType weatherRequestType) throws JSONException {
        this.processCurrentAnd3hCount++;

        // If got all the data go ahead
        if(this.jsonCurrent != null && this.json3h != null){
            // Process the current request

            forecastInterfaceManager.receives3HourForecast(this.jsonCurrent,this.json3h, latitude, longitude, weatherRequestType, lastUpdate);

            this.step3h = true;

            if(this.jsonDaily != null) {
                processDaily(weatherRequestType);
            }
        }

        if(this.jsonDaily != null){
            if(step3h) {
                processDaily(weatherRequestType);
            }

            this.stepDaily = true;
        }

        if(step3h && stepDaily){
            this.state = RequestState.DONE;
        }

    }

    private void processDaily(WeatherRequestType weatherRequestType) throws JSONException {

        forecastInterfaceManager.receivesDailyForecast(this.jsonDaily, weatherRequestType);
    }

    /**
     * Verify if the current date plus the minutes is before the date passed by param
     * @param dateParam
     * @param minutes
     * @return
     */
    public static boolean insideDateThreshold(Date dateParam, int minutes) {
        Date date = new Date();

        int second = 1000;
        int minute = 60 * second;

        long threshold = minute * minutes;
        date.setTime(date.getTime() - threshold);

        boolean result = date.before(dateParam);
        return result;
    }

    /**
     * Verify if the current date plus the minutes is before the date passed by param
     * @param date1
     * @param minutes
     * @return
     */
    public static boolean insideDateThreshold(Date date1, Date date2,int minutes) {

        int second = 1000;
        int minute = 60 * second;

        long threshold = minute * minutes;
        date2.setTime(date2.getTime() - threshold);

        boolean result = date2.before(date1);
        return result;
    }

}