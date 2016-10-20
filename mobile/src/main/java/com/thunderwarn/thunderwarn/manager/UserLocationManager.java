package com.thunderwarn.thunderwarn.manager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

import com.thunderwarn.thunderwarn.common.Log;

import com.thunderwarn.thunderwarn.MainActivity;
import com.thunderwarn.thunderwarn.R;
import com.thunderwarn.thunderwarn.common.SharedResources;
import com.thunderwarn.thunderwarn.common.configuration.CacheManager;
import com.thunderwarn.thunderwarn.common.configuration.LayoutManager;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ivo on 20-06-2015.
 */
public class UserLocationManager {

    //Singleton
    private static UserLocationManager instance = new UserLocationManager();

    public static UserLocationManager getInstance() {
        return instance;
    }

    private UserLocationManager(){}

    // Enums
    private enum UserLocationState{
        NOT_INITED,
        GETTING_LOCATION_FROM_GPS,
        GETTING_WEATHER_DATA,
        LOCATION_RESOLVED,
        LOCATION_RESOLVED_WITHOUT_GEOCODE
    };

    // Constants
    private static final long ONE_MIN = 1000 * 60;
    private static final long TWO_MIN = ONE_MIN * 2;
    private static final long FIVE_MIN = ONE_MIN * 5;

    private static final int GPS_TIMEOUT = 5 * 1000;
    private static final int GPS_ERROR_TIMEOUT = 30 * 1000;
    private static final int TIMEOUT_GPS_CACHE = 50;
    private static final String TAG = "UserLocationManager";
    private static final float MIN_ACCURACY_DISTANCE = 1000.0f; // min metters of precision in GPS
    private static final float MIN_ACCURACY_DISTANCE_AFTER_TIMEOUT = 5000.0f; // min metters of precision in GPS
    private static final float MAX_ACCURACY_DISTANCE = 10000.0f; // min metters of precision in GPS
    private static final int TIMEOUT_ACCURACY_DISTANCE = 10; // seconds timeout

    private static final long MEASURE_TIME = 1000 * 30;
    private static final long POLLING_FREQ = 1000 * 1;
    private static final float MIN_LAST_READ_ACCURACY = 500.0f;
    private static final float MIN_DISTANCE = 0;
    // Fields
    private double latitude;
    private double longitude;

    private double latitudeInCache;
    private double longitudeInCache;

    private String locationText = "";
    private String locationDescription = null;

    private boolean locationUpdated = false;
    // Fields
    private float accuracyDistance = MIN_ACCURACY_DISTANCE;

    private UserLocationState state = UserLocationState.NOT_INITED;

    private SharedResources sharedResources = SharedResources.getInstance();

    // Current best location estimate
    private Location mBestReading;

    // Reference to the LocationManager and LocationListener
    private android.location.LocationManager mLocationManager;
    private LocationListener mLocationListener;

    // Managers
    private WeatherDataManager weatherDataManager = WeatherDataManager.getInstance();

    /**
     * Get the location of the user
     * @param wheaterRequestType
     * @param gotLocationFromCache
     */
    public void calculateUserLocation(WeatherDataManager.WeatherRequestType wheaterRequestType,
                                      boolean gotLocationFromCache) {

        this.locationUpdated = false;

        try {
            if (MainActivity.getInstance() != null) {
                if(MainActivity.getInstance().getTitle() == null) {
                    MainActivity.getInstance().setTitle(R.string.loading_gps);
                }
            }
        }catch(Exception e){
            Log.e(TAG,"Can't change title of main activity but will continue calculating location",e);
        }

        SharedPreferences sharedPreferences = CacheManager.getInstance().getSharedPreferences();
        Set<String> locations = sharedPreferences.getStringSet(
                CacheManager.KEY_LOCATIONS, new HashSet<String>());

        state = UserLocationState.GETTING_LOCATION_FROM_GPS;

        loadGPSCoordinates(gotLocationFromCache, wheaterRequestType);
    }

    // UI Elements

    public void setLocationManager(LocationManager mLocationManager) {
        this.mLocationManager = mLocationManager;
    }




    // Actions
    public void onResume() {

        // Determine whether initial reading is
        // "good enough". If not, register for
        // further location updates

        if (null == mBestReading
                || mBestReading.getAccuracy() > MIN_LAST_READ_ACCURACY
                || mBestReading.getTime() < System.currentTimeMillis()
                - TWO_MIN) {

            // Register for network location updates
            if (null != mLocationManager
                    .getProvider(LocationManager.NETWORK_PROVIDER)) {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, POLLING_FREQ,
                        MIN_DISTANCE, mLocationListener);
            }

            // Register for GPS location updates
            if (null != mLocationManager
                    .getProvider(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, POLLING_FREQ,
                        MIN_DISTANCE, mLocationListener);
            }

            // Schedule a runnable to unregister location listeners
            Executors.newScheduledThreadPool(1).schedule(new Runnable() {

                @Override
                public void run() {
                    if(!locationUpdated){
                        mLocationManager.removeUpdates(mLocationListener);
                    }
                }
            }, MEASURE_TIME, TimeUnit.MILLISECONDS);
        }
    }
    private void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstance());
        builder.setMessage(R.string.enableGPSMessage)
                .setCancelable(false)
                .setPositiveButton(R.string.enableGPS, new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        MainActivity.getInstance().startActivity(
                                new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    private Handler customHandler = new Handler();
    /**
     * Start the loading for GPS Coordinates
     * @param gotLocationFromCache can get location from cache?
     * @param wheaterRequestType type of request: update views or notification
     */
    public void loadGPSCoordinates(final boolean gotLocationFromCache, final WeatherDataManager.WeatherRequestType wheaterRequestType) {

        // If have no GPS coordinates on cache, neither GPS enabled, show an alert to enable GPS
        if ( !sharedResources.isGpsEnabled()
                && !gotLocationFromCache) {
            buildAlertMessageNoGps();
        }

        // Create Timeout! if had location in cache, use it
        // If not,drop the needed accuracy
        customHandler.postDelayed(new Runnable() {
            public void run() {

            }
        }, GPS_TIMEOUT);


        // Try get location from GPS
        final Date startGettingGPS = new Date();
        Log.d(TAG, "loadGPSCoordinates START >");

        mLocationListener = new LocationListener() {
            // Called back when location changes
            public void onLocationChanged(Location location) {

                // Determine whether new location is better than current best estimate
                if (null == mBestReading
                        || location.getAccuracy() < mBestReading.getAccuracy()) {

                    // Update best estimate
                    mBestReading = location;

                    tryGpsReading(gotLocationFromCache, wheaterRequestType);
                }
            }


            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
                // NA
                Log.d(TAG,"onStatusChanged");
            }

            public void onProviderEnabled(String provider) {
                // NA
                Log.d(TAG,"onProviderEnabled");
            }

            public void onProviderDisabled(String provider) {
                Log.d(TAG,"onProviderDisabled");
            }
        };


        UserLocationManager.getInstance().onResume();

        Log.d(TAG, "loadGPSCoordinates END <");
    }

    /**
     * Try to use the best GPS reading, if have enough accuracy
     * @param locationFromCache got a location on cache
     * @param wheaterRequestType
     * @return
     */
    private boolean tryGpsReading(boolean locationFromCache, WeatherDataManager.WeatherRequestType wheaterRequestType) {

        if(mBestReading == null){
            return false;
        }

        float accuracy = mBestReading.getAccuracy();
        // Validate that the accuracy is enough
        if (mBestReading.hasAccuracy() && accuracy < accuracyDistance) {

            if(accuracy < MIN_ACCURACY_DISTANCE) {
                mLocationManager.removeUpdates(mLocationListener);
                this.locationUpdated = true;
            }

            // Update the gps cache date
            CacheManager.getInstance().putDate(CacheManager.KEY_GPS_DATE,new Date());

            // If the location stored on cache is close enough just use it
            if(locationFromCache){
                double distance = distance(getLatitude(), getLongitude(),
                        mBestReading.getLatitude(), mBestReading.getLongitude());

                if(distance > 0.5){
                    // Update display
                    updateLocation(mBestReading.getLatitude(), mBestReading.getLongitude(), wheaterRequestType);
                }
            }
            // If the location was changed
            else {
                // Store the location in user preferences
                SharedPreferences sharedPreferences = CacheManager.getInstance().getSharedPreferences();
                Set<String> locations = sharedPreferences.getStringSet(
                        CacheManager.KEY_LOCATIONS, new HashSet<String>());

                String locationString = mBestReading.getLatitude() + "|" + mBestReading.getLongitude();
                locations.add(locationString);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putStringSet(CacheManager.KEY_LOCATIONS, locations);
                editor.commit();


                // Update display
                if (!locationFromCache) {
                    updateLocation(mBestReading.getLatitude(), mBestReading.getLongitude(), wheaterRequestType);
                }
            }
            return true;
        }else{
            return false;
        }
    }


    public void updateLocation(double latitude, double longitude, WeatherDataManager.WeatherRequestType wheaterRequestType) {
        this.latitude = latitude;
        this.longitude = longitude;

        Log.v(TAG, "updateLocation START > [" + UserLocationManager.this.latitudeInCache + " , "
            + UserLocationManager.this.longitudeInCache + "]");

        state = UserLocationState.GETTING_WEATHER_DATA;

        // Request openweathermap for forecasts
        weatherDataManager.requestAllData(latitude, longitude, wheaterRequestType);

        //TODO LOC retrieve for more than one location

        Log.v(TAG, "updateLocation END <");
    }

    private Map<String,String> geocode = new HashMap<>();

    public static String getLocationName(double latitude, double longitude, Locale locale){
        String result = null;

        // Use geocode to insert a location?
        Geocoder geocoder = new Geocoder(SharedResources.getInstance().getContext());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 10);
            for (int i=0 ; i<addresses.size(); i++) {
                Address address = addresses.get(i);

                if(address.getLocality() != null) {
                    result = address.getLocality();
                }else{
                    Log.w(TAG,"Don't get any locality for coordinates "+ latitude + " | " + longitude);
                }

                if(address.getSubAdminArea() != null){
                    result = address.getSubAdminArea();
                }else{
                    Log.w(TAG,"Don't get any sub admin area for coordinates "+ latitude + " | " + longitude);
                }

            }
            if(addresses.size() == 0){
               Log.w(TAG,"Don't get any results for coordinates "+ latitude + " | " + longitude);
            }

        } catch (IOException e) {
            Log.e(TAG,"Error getting data from Geocoder", e);
        }

        Log.i(TAG,"Geocoder resolved for coordinates "+ latitude + " | " + longitude + " is "
                + result);

        return result;
    }

    public void commitUpdateLocation(double latitude, double longitude) {

        locationDescription = getLocationName(latitude,longitude,Locale.getDefault());

        if(locationDescription != null && !locationDescription.equals("")) {
            this.locationText = locationDescription;
            state = UserLocationState.LOCATION_RESOLVED;

            // cache geocoder
            CacheManager.getInstance().putString(CacheManager.LOCATION_DESCRIPTION, locationDescription);
        }else{
            NumberFormat formatter = NumberFormat.getNumberInstance();
            formatter.setMaximumFractionDigits(5);
            this.locationDescription = formatter.format(latitude) + "N "
                    + formatter.format(longitude) + "W";

            // use cache geocoder
            locationDescription = CacheManager.getInstance().getString(
                    CacheManager.LOCATION_DESCRIPTION, locationDescription);
            state = UserLocationState.LOCATION_RESOLVED_WITHOUT_GEOCODE;
        }

        try {
            // If is not in the flow of notification, update the location title
            if (MainActivity.getInstance() != null) {
                MainActivity.getInstance().setAppTitle(locationDescription);
            }
        }catch(Exception e ){
            Log.e(TAG,"Error updating app title, but will continue...",e);
        }
    }

    /**
     * Method called when the weather data is retrieved from cache
     */
    public void dataRetrievedFromCache() {
        // Ensure that the geocode is resolved
        if(!state.equals(UserLocationState.LOCATION_RESOLVED)){
            SharedPreferences sharedPreferences = CacheManager.getInstance().getSharedPreferences();

            // If the main activity is not null, set from cache, if is null probably the call come from notifications after reboot
            if(MainActivity.getInstance() != null) {
                String currentLocationText = MainActivity.getInstance().getTitle().toString();

                String newLocationText = sharedPreferences.getString(
                        CacheManager.LOCATION_DESCRIPTION, currentLocationText);

                this.locationText = newLocationText;
                try {
                    MainActivity.getInstance().setAppTitle(newLocationText);
                }catch (Exception e){
                    Log.e(TAG,"Error setting main activity title with cached location, but will continue...",e);
                }
            }

        }
    }

    /**
     * Method called when the application can't find a location manager
     */
    public void noLocationManager() {
        Log.w(TAG, "Can't find a location manager");
    }


    public void getUserLocation(WeatherDataManager.WeatherRequestType wheaterRequestType) {

        Date date = CacheManager.getInstance().getDate(CacheManager.KEY_GPS_DATE, new Date(0));
        Set<String> locations = CacheManager.getInstance().getSharedPreferences().getStringSet(
                CacheManager.KEY_LOCATIONS, new HashSet<String>());

        boolean validGPSCache = insideDateThreshold(date, TIMEOUT_GPS_CACHE);
        boolean gotLocation = locations != null && !locations.isEmpty();
        boolean noInternetAccess = !sharedResources.haveInternetAccess();
        boolean noGps = !sharedResources.isGpsEnabled();

        if(gotLocation){
                state = UserLocationState.GETTING_LOCATION_FROM_GPS;

                String firstLocation = locations.iterator().next();
                String [] tab = firstLocation.split("\\|");

                double latitude = Double.parseDouble(tab[0]);
                double longitude = Double.parseDouble(tab[1]);

                this.latitudeInCache = latitude;
                this.longitudeInCache = longitude;

                updateLocation(latitude, longitude, wheaterRequestType);

                calculateUserLocation(wheaterRequestType, true);

        }else {
            // If the return was not called get location from GPS
            calculateUserLocation(wheaterRequestType,false);
        }

    }


    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLocationText() {
        return locationText;
    }


    /**
     * Calculate distance between two points in Kilometers
     *
     * Formula from: https://www.geodatasource.com/developers/java
     * @param lat1 latitude  of location 1
     * @param lon1 longitude of location 1
     * @param lat2 latitude  of location 2
     * @param lon2 longitude of location 2
     * @return distance between location 1 and location 2 in Kilometers
     */
    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts decimal degrees to radians						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::	This function converts radians to decimal degrees						 :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
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

}
