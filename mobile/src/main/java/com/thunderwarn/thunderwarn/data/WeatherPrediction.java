package com.thunderwarn.thunderwarn.data;

import com.thunderwarn.thunderwarn.manager.WeatherConditionManager;

/**
 * Created by ivofernandes on 14/11/15.
 */
public class WeatherPrediction {

    private int apparentMin;
    private int apparentMax;
    private int tempMin;
    private int tempMax;

    private WeatherConditionManager.WeatherCondition weatherCondition = null;

    public WeatherPrediction(int apparentMin, int apparentMax,int tempMin, int tempMax) {
        this.apparentMin = apparentMin;
        this.apparentMax = apparentMax;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
    }

    public int getApparentMin() {
        return apparentMin;
    }

    public void setApparentMin(int apparentMin) {
        this.apparentMin = apparentMin;
    }

    public int getApparentMax() {
        return apparentMax;
    }

    public void setApparentMax(int apparentMax) {
        this.apparentMax = apparentMax;
    }

    public WeatherConditionManager.WeatherCondition getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(WeatherConditionManager.WeatherCondition weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public int getTempMin() {
        return tempMin;
    }

    public void setTempMin(int tempMin) {
        this.tempMin = tempMin;
    }

    public int getTempMax() {
        return tempMax;
    }

    public void setTempMax(int tempMax) {
        this.tempMax = tempMax;
    }
}
