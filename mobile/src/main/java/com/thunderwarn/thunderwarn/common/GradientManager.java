package com.thunderwarn.thunderwarn.common;

import android.graphics.Color;

/**
 * Created by ivofernandes on 22/10/15.
 */
public class GradientManager {

    // Basic input params
    private String colorStartHex;
    private String colorEndHex;


    // Calculated on init
    float colorStartHue;
    float colorStartSaturation;
    float colorStartValue ;

    float colorEndHue;
    float colorEndSaturation;
    float colorEndValue;

    public GradientManager(String colorStartHex, String colorEndHex) {
        this.colorStartHex = colorStartHex;
        this.colorEndHex = colorEndHex;
        
        init();
    }

    public void init(){

        // Only used for calculations, don't mistake for int color values for android
        Long colorStartLong_R = Long.parseLong(colorStartHex.substring(0,2),16);
        Long colorStartLong_G = Long.parseLong(colorStartHex.substring(2,4),16);
        Long colorStartLong_B = Long.parseLong(colorStartHex.substring(4,6),16);

        Long colorEndLong_R = Long.parseLong(colorEndHex.substring(0,2),16);
        Long colorEndLong_G = Long.parseLong(colorEndHex.substring(2,4),16);
        Long colorEndLong_B = Long.parseLong(colorEndHex.substring(4,6),16);

        // HSV
        float[] colorStartHsv = new float[3];
        Color.RGBToHSV(colorStartLong_R.intValue(), colorStartLong_G.intValue(),
                colorStartLong_B.intValue(), colorStartHsv);

        this.colorStartHue         = colorStartHsv[0];
        this.colorStartSaturation  = colorStartHsv[1];
        this.colorStartValue       = colorStartHsv[2];


        float[] colorEndHsv = new float[3];
        Color.RGBToHSV(colorEndLong_R.intValue(), colorEndLong_G.intValue(),
                colorEndLong_B.intValue(), colorEndHsv);

        this.colorEndHue           = colorEndHsv[0];
        this.colorEndSaturation    = colorEndHsv[1];
        this.colorEndValue         = colorEndHsv[2];
    }

    public int colorForRatio(double condition) {
        double valueChange = 0;
        double saturationChange = 0;

        if(condition == 0.5){
            valueChange = 0.3;
            saturationChange = -0.7;
        }

        if(condition == 0.3 || condition == 0.7){
            valueChange = 0.2;
        }

        if(condition == 1 || condition == 0){
            valueChange = -0.2;
        }

        condition -= 0.5;
        condition *= 1.2;
        condition += 0.5;

        condition = Math.max(0,condition);
        condition = Math.min(1,condition);

        float colorHue = (float) (colorStartHue * (1-condition) + colorEndHue  * condition);
        float colorSaturation = (float) (colorStartSaturation * (1-condition) + colorEndSaturation  * condition);
        float colorValue = (float) (colorStartValue * (1-condition) + colorEndValue  * condition);

        colorValue += valueChange;
        colorValue = Math.max(0,colorValue);
        colorValue = Math.min(1, colorValue);

        colorSaturation += saturationChange;
        colorSaturation = Math.max(0,colorSaturation);
        colorSaturation = Math.min(1,colorSaturation);

        float[] hsv = new float[]{colorHue, colorSaturation, colorValue};

        int result = Color.HSVToColor(hsv);
        return result;
    }


}
