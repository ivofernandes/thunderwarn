package com.thunderwarn.thunderwarn.activities;

import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thunderwarn.thunderwarn.OpenWeatherMap.processors.JsonProcessor;
import com.thunderwarn.thunderwarn.R;
import com.thunderwarn.thunderwarn.common.Log;
import com.thunderwarn.thunderwarn.common.SharedResources;
import com.thunderwarn.thunderwarn.common.configuration.LayoutManager;

public class HelpActivity extends ActionBarActivity {

    private SharedResources sharedResources = SharedResources.getInstance();
    private LayoutManager layoutManager = LayoutManager.getInstance();
    private LinearLayout panel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // Change colors
        RelativeLayout mainPanel = (RelativeLayout) findViewById(R.id.helpMainPanel);
        mainPanel.setBackgroundColor(layoutManager.getBackgroundColor());

        this.panel = (LinearLayout) findViewById(R.id.helpPanel);
        panel.setBackgroundColor(layoutManager.getBackgroundColor());

        TextView aboutThunderwarn = (TextView) findViewById(R.id.aboutThunderwarn);
        aboutThunderwarn.setTextColor(layoutManager.getForegroundColor());

        TextView aboutThunderwarnDescription = (TextView) findViewById(R.id.aboutThunderwarnDescription);
        aboutThunderwarnDescription.setTextColor(layoutManager.getForegroundColor());

        TextView colorMeaning = (TextView) findViewById(R.id.colorMeaning);
        colorMeaning.setTextColor(layoutManager.getForegroundColor());


        TextView colorMeaningDescription = (TextView) findViewById(R.id.colorMeaningDescription);
        colorMeaningDescription.setTextColor(layoutManager.getForegroundColor());

        LinearLayout colorMeaningDescriptionPanel = (LinearLayout) findViewById(R.id.colorMeaningDescriptionPanel);
        colorMeaningDescriptionPanel.setBackgroundColor(layoutManager.getBackgroundColor());

        TextView apparentTemperature = (TextView) findViewById(R.id.apparentTemperature);
        apparentTemperature.setTextColor(layoutManager.getForegroundColor());

        TextView apparentTemperatureDescription = (TextView) findViewById(R.id.apparentTemperatureDescription);
        apparentTemperatureDescription.setTextColor(layoutManager.getForegroundColor());

        // Generate panel
        createColorMeaningDescription(colorMeaningDescriptionPanel);


    }

    private void createColorMeaningDescription(LinearLayout colorMeaningDescriptionPanel) {


        String [] texts = {
                " <-20º",
                " [-20º ... -15º]",
                " [-15º ... -10º]",
                " [-10º ...  -5º]",
                " [-5º  ...   0º]",
                " [0º   ...  +5º]",
                " [+5º  ... +10º]",
                " [+10º ... +15º]",
                " [+15º ... +20º]",
                " [+20º ... +25º]",
                " >+25º",};

        for(int i=10 ; i>=0 ; i--) {
            ImageView color = JsonProcessor.createImage(50);
            double ratio = (double)i / 10;
            color.setBackgroundColor(layoutManager.colorForWeather(ratio));

            TextView textView = new TextView(sharedResources.getContext());
            textView.setText(texts[i]);
            textView.setTypeface(Typeface.MONOSPACE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) textView.getLayoutParams();
            if(params == null){
                params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
            }

            params.setMargins(20, 0, 0, 0);

            LinearLayout colorSample = new LinearLayout(sharedResources.getContext());
            colorSample.setOrientation(LinearLayout.HORIZONTAL);
            colorSample.addView(color);
            colorSample.addView(textView);

            colorMeaningDescriptionPanel.addView(colorSample);
        }
    }
}
