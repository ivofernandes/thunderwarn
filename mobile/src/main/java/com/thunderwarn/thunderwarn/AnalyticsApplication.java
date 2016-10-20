package com.thunderwarn.thunderwarn;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by ivofernandes on 28/11/15.
 */
public class AnalyticsApplication extends Application {

    public Tracker mTracker;

    public void startTracking(){

        // Init Analytics
        if(mTracker == null) {
            GoogleAnalytics ga = GoogleAnalytics.getInstance(this);

            mTracker = ga.newTracker(R.xml.track_app);

            ga.enableAutoActivityReports(this);

            ga.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
        }
    }

    public Tracker getTracker(){
        startTracking();

        return mTracker;
    }
}
