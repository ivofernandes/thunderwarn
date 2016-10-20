package com.thunderwarn.thunderwarn.common;

import android.app.NotificationManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.thunderwarn.thunderwarn.OpenWeatherMap.OpenWeatherRequest;
import com.thunderwarn.thunderwarn.R;
import com.thunderwarn.thunderwarn.common.configuration.CacheManager;
import com.thunderwarn.thunderwarn.common.configuration.ConfigurationManager;
import com.thunderwarn.thunderwarn.common.configuration.UserPreferencesManager;
import com.thunderwarn.thunderwarn.manager.UserLocationManager;
import com.thunderwarn.thunderwarn.scheduler.NotificationAlarmReceiver;
import com.thunderwarn.thunderwarn.scheduler.SendNotificationManager;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Ivo on 21-06-2015.
 */
public class SharedResources {

    // Singleton
    private static SharedResources instance = new SharedResources();
    private Resources resources;
    private NotificationManager notificationManager;
    private LocationManager locationManager;

    private SharedResources(){};

    // Public constants
    public static final int RAIN_NOTIFICATION_ID = 1;
    public static final int LOG_NOTIFICATION_ID = 2;

    // Constants
    private static final String TAG = "SharedResources";

    // Fields
    private Context context;

    private boolean inited = false;
    private Locale locale;

    private DateFormat dateFormat;
    private DateFormat timeFormat;
    private DateFormat lastUpdateFormat;

    private FragmentManager fragmentManager;
    private NotificationAlarmReceiver alarm = new NotificationAlarmReceiver();



    public static SharedResources getInstance(){
        return instance;
    }

    // methods for resources

    public void init(Context context){
        Log.d(TAG, "start initing shared resources");

        setContext(context);
        Log.initLog();

        Log.d(TAG, "shared resources - set locale");
        Locale locale = SharedResources.getInstance().getContext().getResources().getConfiguration().locale;
        setLocale(locale);

        Log.d(TAG, "shared resources - init user preferences");
        UserPreferencesManager.getInstance().init();

        Log.d(TAG, "shared resources - set resources");
        setResources(SharedResources.getInstance().getContext().getResources());

        Log.d(TAG, "shared resources - init notification manager");
        initNotificationManager();

        Log.d(TAG, "shared resources - init location manager");
        initLocationManager();

        inited = true;

        Log.d(TAG, "shared resources inited");
    }

    private void initLocationManager() {
        // Acquire reference to the LocationManager
        LocationManager mLocationManager = getLocationManager();

    }

    private void initNotificationManager() {

        // Init notication manager
        this.notificationManager = (NotificationManager)
                SharedResources.getInstance().getContext().getSystemService(
                        SharedResources.getInstance().getContext().NOTIFICATION_SERVICE);

        alarm.setAlarm();
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;

        // Generate fields that depend on the locale
        dateFormat = new SimpleDateFormat("EEE dd",locale);
        timeFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT,locale);

    }

    public Locale getLocale() {
        return locale;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public DateFormat getTimeFormat() {
        return timeFormat;
    }


    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public String resolveString(int code) {
        return resources.getString(code);
    }

    public String formatDate(Date date) {
        String dateString = dateFormat.format(date);

        //dateString = dateString.toUpperCase();
        return dateString;
    }

    public String formatTime(Date time) {
        String timeString = timeFormat.format(time);
        return timeString;
    }


    public String formatDateTime(Date date) {
        String dateString = dateFormat.format(date);
        String timeString = timeFormat.format(date);
        return dateString + " " + timeString;
    }

    public boolean haveInternetAccess(){
        final ConnectivityManager connMgr = (ConnectivityManager)
                SharedResources.getInstance().getContext().getSystemService(
                        SharedResources.getInstance().getContext().CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnectedOrConnecting ()) {
            return true;
        } else if (mobile.isConnectedOrConnecting ()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean usingInternetDataPlanAccess(){
        final ConnectivityManager connMgr = (ConnectivityManager)
                SharedResources.getInstance().getContext().getSystemService(
                        SharedResources.getInstance().getContext().CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnectedOrConnecting ()) {
            return false;
        } else if (mobile.isConnectedOrConnecting ()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isGpsEnabled() {
        LocationManager mLocationManager = getLocationManager();
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public LocationManager getLocationManager() {
        LocationManager mLocationManager;
        if (null == (mLocationManager = (LocationManager) SharedResources.getInstance().getContext().getSystemService(
                SharedResources.getInstance().getContext().LOCATION_SERVICE))) {
            UserLocationManager.getInstance().noLocationManager();
        }
        UserLocationManager.getInstance().setLocationManager(mLocationManager);

        return mLocationManager;
    }
}
