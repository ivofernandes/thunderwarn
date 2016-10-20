package com.thunderwarn.thunderwarn.common.configuration;

import android.graphics.Color;

import com.thunderwarn.thunderwarn.common.GradientManager;

/**
 * Created by Ivo on 26-06-2015.
 */
public class LayoutManager {


    // Singleton
    private static LayoutManager instance = new LayoutManager();

    private LayoutManager(){}

    public static LayoutManager getInstance(){
        return instance;
    }


    private static final String badWeatherHex =  "b41fde";
    private static final String goodWeatherHex = "de1f1f";

    private GradientManager gradientManager = new GradientManager(badWeatherHex,goodWeatherHex);

    // Getters for colors
    public int getBackgroundColor(){
        return Color.parseColor("#000000");
    }

    public int getSmoothBackgroundColor(){
        return Color.parseColor("#000000");
    }

    public int getSmoothForegroundColor(){
        return Color.parseColor("#a8a8a8");
    }

    public int getForegroundColor(){
        return Color.parseColor("#f9f9f9");
    }

    /**
     * Calculate the color for the weather
     * @param condition 0 is totally bad weather, 1 is totally good weather
     * @return
     */
    public int colorForWeather(double condition){
        int result = this.gradientManager.colorForRatio(condition);

        return result;
    }

}
