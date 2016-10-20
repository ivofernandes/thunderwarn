package com.thunderwarn.thunderwarn.common;

import android.content.SharedPreferences;
import com.thunderwarn.thunderwarn.common.Log;

import com.thunderwarn.thunderwarn.common.configuration.CacheManager;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ivofernandes on 29/11/15.
 */
public class Testing {

    // Singleton
    private static Testing instance = new Testing();
    private Testing(){}
    public static Testing getInstance(){return instance;}

    // Constants
    private static String TAG = "Testing";
    private static boolean testing = false;

    // Fields

    // update data
    public void updateData() {
        if(!testing) {
            return;
        }

        Set<String> list = CacheManager.getInstance().getSharedPreferences().getStringSet("updateData", new HashSet<String>());
        list.add(SharedResources.getInstance().formatDateTime(new Date()));

        SharedPreferences.Editor editor = CacheManager.getInstance().getSharedPreferences().edit();
        editor.putStringSet("updateData", list);
        editor.commit();

    }

    // update location

    public void updateLocation(double latitude, double longitude) {

        if(!testing) {
            return;
        }

        Set<String> list = CacheManager.getInstance().getSharedPreferences().getStringSet("updateLocation", new HashSet<String>());

        String log = SharedResources.getInstance().formatDateTime(new Date())
                + " " + latitude+","+longitude;

        list.add(log);

        SharedPreferences.Editor editor = CacheManager.getInstance().getSharedPreferences().edit();
        editor.putStringSet("updateLocation", list);
        editor.commit();
    }

    // Get testing data

    public void getTestingData(){
        Set<String> updateData = CacheManager.getInstance().getSharedPreferences().getStringSet("updateData", new HashSet<String>());
        Set<String> updateLocation = CacheManager.getInstance().getSharedPreferences().getStringSet("updateLocation", new HashSet<String>());
        Log.i(TAG,"updateData: " + updateData);
        Log.i(TAG,"updateLocation: " + updateLocation);
    }
}
