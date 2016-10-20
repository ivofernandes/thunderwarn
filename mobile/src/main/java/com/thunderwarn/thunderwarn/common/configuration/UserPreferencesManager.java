package com.thunderwarn.thunderwarn.common.configuration;

import com.thunderwarn.thunderwarn.common.Log;

/**
 * Created by ivofernandes on 27/07/15.
 */
public class UserPreferencesManager {

    // Singleton
    private static UserPreferencesManager instance = new UserPreferencesManager();

    private UserPreferencesManager(){}

    public static UserPreferencesManager getInstance(){
        return instance;
    }

    // Fields
    private boolean apparentTemperature = true;
    private boolean notification = true;
    private boolean useGps = true;

    /**
     * Load data from local cache
     */
    public void init() {
        this.notification = CacheManager.getInstance().getBoolean(
                CacheManager.PREFERENCE_SETTINGS_NOTIFICATION, true);

        this.apparentTemperature = CacheManager.getInstance().getBoolean(
                CacheManager.PREFERENCE_SETTINGS_APPARENT_TEMPERATURE, true);

        this.useGps = CacheManager.getInstance().getBoolean(
                CacheManager.PREFERENCE_SETTINGS_USE_GPS, true);
    }

    public boolean isApparentTemperature() {
        return apparentTemperature;
    }

    public boolean isNotification() {
        return notification;
    }

    public boolean isUseGps() {
        return useGps;
    }

    public void setApparentTemperature(boolean apparentTemperature) {
        this.apparentTemperature = apparentTemperature;

        CacheManager.getInstance().putBoolean(CacheManager.PREFERENCE_SETTINGS_APPARENT_TEMPERATURE,
                this.apparentTemperature);
    }

    public void setNotification(boolean notification) {
        this.notification = notification;

        CacheManager.getInstance().putBoolean(CacheManager.PREFERENCE_SETTINGS_NOTIFICATION,
                this.notification);

        Log.clickToLog();
    }

    public void setUseGps(boolean useGps) {
        this.useGps = useGps;

        CacheManager.getInstance().putBoolean(CacheManager.PREFERENCE_SETTINGS_USE_GPS,
                this.useGps);
    }
}
