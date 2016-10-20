package com.thunderwarn.thunderwarn.manager;

import android.widget.LinearLayout;

import com.thunderwarn.thunderwarn.common.configuration.LayoutManager;
import com.thunderwarn.thunderwarn.forecastView.SimpleForecastUI;
import com.thunderwarn.thunderwarn.scheduler.SendNotificationManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Ivo on 20-06-2015.
 */
public class ForecastInterfaceManager {

    // Singleton
    private static ForecastInterfaceManager instance = new ForecastInterfaceManager();

    private ForecastInterfaceManager(){}

    public static ForecastInterfaceManager getInstance() {
        return instance;
    }

    // Constants
    private static String TAG = "ForecastInterfaceManager";

    // Fields
    private SendNotificationManager sendNotificationManager = SendNotificationManager.getInstance();

    private LayoutManager layoutManager = LayoutManager.getInstance();

    // Forecast UI types
    private SimpleForecastUI simpleForecastUI = new SimpleForecastUI();

    // Forecast panel
    private LinearLayout forecastPanel;
    //
    private Date lastUpdate = null;


    // Getters/Setters
    public void setForecastView(LinearLayout forecastView) {
        this.forecastPanel = forecastView;
        this.forecastPanel.setBackgroundColor(layoutManager.getBackgroundColor());
        // When receives the panel, pass it to the child panels
        simpleForecastUI.setForecastView(forecastView);
    }
    
    // Receivers
    public void receives3HourForecast(JSONObject forecastCurrent, JSONObject forecast3h,
                                      double latitude, double longitude,
                                      WeatherDataManager.WeatherRequestType weatherRequestType,
                                      Date lastUpdate) throws JSONException {
        this.lastUpdate = lastUpdate;

        UserLocationManager.getInstance().commitUpdateLocation(latitude, longitude);

        if(weatherRequestType.equals(WeatherDataManager.WeatherRequestType.UPDATE_VIEWS)) {
            simpleForecastUI.show3HoursForecast(forecast3h, this.lastUpdate);
            simpleForecastUI.showCurrentForecast(forecastCurrent);
        }else if(weatherRequestType.equals(WeatherDataManager.WeatherRequestType.NOTIFICATION)) {
            sendNotificationManager.fireNotications(forecast3h);
        }
    }

    public void receivesDailyForecast(JSONObject response, WeatherDataManager.WeatherRequestType weatherRequestType) throws JSONException {
        if(weatherRequestType.equals(WeatherDataManager.WeatherRequestType.UPDATE_VIEWS)) {
            simpleForecastUI.showDailyForecast(response);
        }
    }

    public void reset() {
        simpleForecastUI.reset();
    }


    public JSONObject get3HoursPrediction(int position){
        return simpleForecastUI.get3HoursPrediction(position);
    }

    public JSONObject getDailyPrediction(int position) {
        return simpleForecastUI.getDailyPrediction(position);
    }
}
