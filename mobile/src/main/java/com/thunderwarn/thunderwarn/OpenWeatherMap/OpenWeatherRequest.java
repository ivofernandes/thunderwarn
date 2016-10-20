package com.thunderwarn.thunderwarn.OpenWeatherMap;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.appdatasearch.GetRecentContextCall;
import com.thunderwarn.thunderwarn.common.Log;

import android.widget.Toast;

import com.thunderwarn.thunderwarn.MainActivity;
import com.thunderwarn.thunderwarn.R;
import com.thunderwarn.thunderwarn.common.SharedResources;
import com.thunderwarn.thunderwarn.common.configuration.CacheManager;
import com.thunderwarn.thunderwarn.common.i18n.LanguageManager;
import com.thunderwarn.thunderwarn.manager.UserLocationManager;
import com.thunderwarn.thunderwarn.manager.WeatherDataManager;
import com.thunderwarn.thunderwarn.scheduler.NotificationSchedulingService;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Ivo on 20-06-2015.
 *
 * http://api.openweathermap.org/data/2.5/forecast?appid=0f3f0b0574edaf1267d5439472b77891&lat=38.714&lon=-9.14
 */
public class OpenWeatherRequest {

    //Singleton
    public static OpenWeatherRequest instance = new OpenWeatherRequest();

    public static OpenWeatherRequest getInstance(){
        return instance;
    }

    private OpenWeatherRequest(){}


    // Constants
    public static final String TAG = "OpenWeatherRequest";
    public static final String REQUEST_DATA_3_HOURS = "forecast";
    public static final String REQUEST_DATA_DAILY = "forecast/daily";
    public static final String REQUEST_DATA_CURRENT = "weather";

    // Minutes to cache timeout
    private static final int CACHE_TIMEOUT = 60;
    private static final int CACHE_TIMEOUT_WITHOUT_INTERNET = 10 * 24 * 60;

    private static String OPEN_WEATHER_MAP_KEY = "0f3f0b0574edaf1267d5439472b77891";
    private static String BASE_URL = "http://api.openweathermap.org/data/2.5/";

    private static final int MAX_RETRY = 3;

    // Generic requestAllData vars
    private Map<String,Integer> requestTypes = new HashMap<String,Integer>();

    // Vars
    private Map<String, String> options;

    private SharedResources sharedResources = SharedResources.getInstance();


    /**
     * Make the requests to get the weather data
     */
    public void request_current_3hours_daily(double latitude, double longitude,Map<String,String> options,
                                             WeatherDataManager.WeatherRequestType weatherRequestType){
        this.options = options;

        // Reset the variables
        requestTypes.put(REQUEST_DATA_CURRENT, 0);
        requestTypes.put(REQUEST_DATA_3_HOURS, 0);
        requestTypes.put(REQUEST_DATA_DAILY, 0);

        // Request 3 hours data
        Log.i(TAG, "Request 3 hours data");
        request(REQUEST_DATA_3_HOURS, false, weatherRequestType, latitude, longitude);

        // Request current data
        Log.i(TAG,"Request current data");
        request(REQUEST_DATA_CURRENT, false, weatherRequestType,latitude, longitude);

        // Request daily data
        Log.i(TAG,"Request daily data");
        request(REQUEST_DATA_DAILY, false, weatherRequestType,latitude, longitude);
    }


    /**
     * Request data for 3 hours interval
     */
    private void request(String requestDataType, boolean retry,
                         WeatherDataManager.WeatherRequestType weatherRequestType,
                         double latitude, double longitude) {

        // If don't have internet access, try to get data from cache
        if(!sharedResources.haveInternetAccess()){
            Log.d(TAG,requestDataType + " request without internet access");
            boolean retrieved = tryRetrieveFromCache(requestDataType, CACHE_TIMEOUT_WITHOUT_INTERNET, weatherRequestType, latitude, longitude);
            Log.d(TAG, requestDataType + " retrieved from cache: " +retrieved);

            if(!retrieved){
                if(weatherRequestType.equals(WeatherDataManager.WeatherRequestType.UPDATE_VIEWS)) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstance());
                    builder.setMessage(R.string.enableWifiMessage)
                            .setCancelable(false)
                            .setPositiveButton(R.string.enableWifi, new DialogInterface.OnClickListener() {
                                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    MainActivity.getInstance().startActivity(
                                            new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                                }
                            });

                    final AlertDialog alert = builder.create();
                    alert.show();
                }else{
                    NotificationSchedulingService.getInstance().noInternetError();
                }
            }

        }
        // If have internet access try the normal request cycle
        else {
            requestTryCicle(requestDataType, retry, weatherRequestType, latitude, longitude);
        }
    }

    /**
     * Control the retries, and try to get the data first from cache, and then from internet connection
     * @param requestDataType
     * @param retry
     * @param weatherRequestType
     * @param latitude
     * @param longitude
     */
    private void requestTryCicle(String requestDataType, boolean retry,
                                 WeatherDataManager.WeatherRequestType weatherRequestType,
                                 double latitude, double longitude) {

        // If is retrying to get the data, increment the retry control var
        Integer tries = requestTypes.get(requestDataType);
        tries++;
        requestTypes.put(requestDataType, tries);

        Log.d(TAG, requestDataType + " " + tries + " tries");

        // If not reached the max number of retries
        if (tries <= MAX_RETRY) {

            // If is not a retry, first try get the data from cache
            if (!retry) {
                int cache = calculateCache(weatherRequestType);
                boolean retrieved = tryRetrieveFromCache(requestDataType, cache, weatherRequestType,
                        latitude,longitude);
                Log.d(TAG, requestDataType + " retrieved from cache : " + retrieved);
                if (!retrieved) {
                    makeRequest(requestDataType, weatherRequestType,latitude,longitude,tries);
                }
            }

            // If is a retry, try now by internet
            else {
                makeRequest(requestDataType, weatherRequestType,latitude,longitude,tries);
            }
        }
        // If reached the max number of retries
        else {
            if (!requestDataType.equals(REQUEST_DATA_CURRENT)) {
                // Try to get data from cache one time, and if fails just stop
                if (tries <= MAX_RETRY + 1) {
                    tryRetrieveFromCache(requestDataType, CACHE_TIMEOUT_WITHOUT_INTERNET, weatherRequestType, latitude, longitude);
                }
                // If we can't get data in any way...
                else if (requestDataType.equals(REQUEST_DATA_3_HOURS)) {
                    if (weatherRequestType.equals(WeatherDataManager.WeatherRequestType.UPDATE_VIEWS)) {
                        String noInternetError = sharedResources.resolveString(R.string.error_requesting_data);
                        Toast.makeText(sharedResources.getContext(), noInternetError, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private int calculateCache(WeatherDataManager.WeatherRequestType requestType) {
        return CACHE_TIMEOUT;
    }

    private void makeRequest(String requestType, WeatherDataManager.WeatherRequestType weatherRequestType, double latitude, double longitude, Integer tries){

        if(requestType != null) {
            String url = urlForForecast(latitude, longitude, options, requestType,tries);

            RequestTask requestTask = new RequestTask(requestType, this,weatherRequestType,latitude,longitude);

            requestTask.execute(url);

       }
    }

    public void response(String response, String requestType,
                         WeatherDataManager.WeatherRequestType weatherRequestType,
                         boolean dataFromCache, Date lastUpdateDate, double latitude, double longitude){

        Log.v(TAG, requestType + " weatherRequestType: " + weatherRequestType + " response: " + response);

        if(response == null){
            request(requestType, true, weatherRequestType, latitude, longitude); // retry
        }

        // 3 Hours request_current_3hours_daily
        if(requestType.equals(OpenWeatherRequest.REQUEST_DATA_3_HOURS)
                || requestType.equals(OpenWeatherRequest.REQUEST_DATA_DAILY)
                || requestType.equals(OpenWeatherRequest.REQUEST_DATA_CURRENT)){
            try {
                JSONObject json = new JSONObject(response);
                if(validResponse(json,response)) {
                    WeatherDataManager.getInstance().response(response, json, requestType,
                            weatherRequestType, dataFromCache, lastUpdateDate);
                }else{
                    request(requestType, true, weatherRequestType, latitude, longitude); // retry
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting data " + weatherRequestType + "for type " + requestType
                        + ", data: " + e.getMessage(),e);
                request(requestType, true, weatherRequestType, latitude, longitude); // retry
            }
        }

        else{
            Log.e(TAG, "Unknown requestAllData type: " + requestType);
        }
    }


    /**
     * Try get data from cache
     * @param requestType
     * - REQUEST_DATA_3_HOURS
     * - REQUEST_DATA_DAILY
     * - REQUEST_DATA_CURRENT
     * @param cacheTimeout
     * @param latitude
     *@param longitude @return true if data was retrieved from cache
     */
    private boolean tryRetrieveFromCache(String requestType, int cacheTimeout, WeatherDataManager.WeatherRequestType weatherRequestType, double latitude, double longitude) {
        try {

            // First of all, validate if the cache remains valid
            SharedPreferences sharedPreferences = CacheManager.getInstance().getSharedPreferences();

            String keyDate =
                    CacheManager.getPreferenceFor(requestType, CacheManager.SUFIX_DATE);

            String dateString = sharedPreferences.getString(keyDate, "");

            if(dateString.equals("")) return false;

            Date date = CacheManager.DATE_FORMAT.parse(dateString);

            boolean validData = WeatherDataManager.insideDateThreshold(date, cacheTimeout);

            if(!validData) return false;

            // If is valid, get the data
            String keyLocation =
                    CacheManager.getPreferenceFor(requestType, CacheManager.SUFIX_LOCATION);
            String keyData =
                    CacheManager.getPreferenceFor(requestType, CacheManager.SUFIX_DATA);

            String key = null;

            //TODO LOC retrieve for more than one location
            String locationString = sharedPreferences.getString(keyLocation, "");

            String dataString = sharedPreferences.getString(keyData, "");

            // pass info to response to alert that this data already comes from a cache
            // and so, don't refresh the cache with this data as it is not updated
            boolean dataFromCache = true;
            response(dataString,requestType, weatherRequestType, dataFromCache, date, latitude, longitude);

            UserLocationManager.getInstance().dataRetrievedFromCache();

            return true;
        }catch (Exception e){
            Log.e(TAG, "Error retrieving data from Cache", e);
            return false;
        }
    }

    private boolean validResponse(JSONObject response, String jsonString) {
        if(response == null){
            return false;
        }

        if(jsonString.contains("Not found city")){
            return false;
        }

        return true;
    }

    /**
     *  @param latitude
     * @param longitude
     * @param options
     * @param requestType defines if the request_current_3hours_daily is one of the following types:
     ** REQUEST_DATA_3_HOURS
     *** http://api.openweathermap.org/data/2.5/forecast?appid=0f3f0b0574edaf1267d5439472b77891&lat=38.71685333333333&lon=-9.162623333333332&units=metric&lang=pt
     ** REQUEST_DATA_DAILY
     * @param tries
     *
     */
    private String urlForForecast(double latitude, double longitude, Map<String, String> options, String requestType, Integer tries) {

        String language = LanguageManager.getInstance().getLanguage();

        String url = BASE_URL + requestType + "?appid=" + OPEN_WEATHER_MAP_KEY
                + paramsForLocation(latitude, longitude,tries);

        for (Map.Entry<String,String> entry : options.entrySet()){
            url += "&" + entry.getKey() + "=" + entry.getValue();
        }

        // calculate the number of pages to show
        if(requestType.equals(REQUEST_DATA_DAILY)){
            url += "&cnt=16";
        }

        url += "&lang=" + language;

        return url;
    }

    /**
     * Get the location based on the coordinates and the number of tries,
     * because if is a retry will use geocode to get the city and
     * @param latitude
     * @param longitude
     * @param tries
     * @return
     */
    private String paramsForLocation(double latitude, double longitude, Integer tries) {

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(7);
        String locationQuery = "&lat=" + nf.format(latitude) + "&lon=" + nf.format(longitude);

        // If can't get by coordinates, try using location name
        if(tries > -1) {
            String locationName = UserLocationManager.getLocationName(latitude,longitude, Locale.US);
            Log.i(TAG, "locale name for location: " + locationName);
            if(locationName != null){
                locationQuery = "&q=" + locationName;

                try{
                    if(MainActivity.getInstance() != null) {
                        MainActivity.getInstance().setAppTitle(locationName);
                    }
                }catch (Exception e){}
            }
        }

        return locationQuery;
    }

}
