package com.thunderwarn.thunderwarn.common;

/**
 * Created by Ivo on 25-06-2015.
 */
public class Weather {

    // Constants
    public static double euler = 2.718281828459045235360287471352;

    // Singleton
    private static Weather instance = new Weather();

    private Weather(){};

    public static Weather getInstance(){
        return instance;
    }

    public double apparentTemperature(double temperature, double humidity, double windSpeed, double radiation) {

        double temps = (17.27 * temperature) / (237.7 + temperature);
        double waterVapourPressure = (humidity / 100) * 6.105 * Math.pow(euler,temps);
                //math.pow(euler, temps);
        double apparentTemperature = temperature + 0.348 * waterVapourPressure - 0.7 * windSpeed
                + 0.7 * (radiation / (windSpeed + 10)) - 4.25;


        // Limit the changes to 3 degrees
        if(apparentTemperature - temperature > 3){
            apparentTemperature = temperature + 3;
        }else if(apparentTemperature - temperature < -3){
            apparentTemperature = temperature - 3;
        }

        return apparentTemperature;
    }

    /**
     * Calculate a ratio between 0 and 1 when 0 is totally bad weather and 1 is totally good weather
     * @param apparentMin
     * @param apparentMax
     * @return
     */
    public Double ratio(int apparentMin, int apparentMax) {

        double [][] table = {
                {25 ,1},
                {20 ,0.9},
                {15 ,0.8},
                {10 ,0.7},
                {5  ,0.6},
                {0  ,0.5},
                {-5 ,0.4},
                {-10,0.3},
                {-15,0.2},
                {-20,0.1},
        };

        for(int i=0 ; i<table.length ; i++){
            double threshold = table[i][0];
            if(apparentMin > threshold){
                double result = table[i][1];
                return result;
            }
        }

        return Double.valueOf(0);
    }
}
