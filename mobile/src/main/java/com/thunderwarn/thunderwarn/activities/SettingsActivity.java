package com.thunderwarn.thunderwarn.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.thunderwarn.thunderwarn.R;
import com.thunderwarn.thunderwarn.common.SharedResources;
import com.thunderwarn.thunderwarn.common.configuration.LayoutManager;
import com.thunderwarn.thunderwarn.common.configuration.UserPreferencesManager;

import org.w3c.dom.Text;

public class SettingsActivity extends ActionBarActivity {

    private LayoutManager layoutManager = LayoutManager.getInstance();
    private UserPreferencesManager userPreferencesManager = UserPreferencesManager.getInstance();

    private LinearLayout panel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedResources.getInstance().setContext(getApplicationContext());

        setContentView(R.layout.activity_settings);

        // Change panel colors
        RelativeLayout mainPanel = (RelativeLayout) findViewById(R.id.SettingsMainPanel);
        mainPanel.setBackgroundColor(layoutManager.getBackgroundColor());

        this.panel = (LinearLayout) findViewById(R.id.settingsPanel);
        panel.setBackgroundColor(layoutManager.getBackgroundColor());


        TextView notificationsText = (TextView) findViewById(R.id.notificationsText);
        notificationsText.setTextColor(layoutManager.getForegroundColor());

        TextView apparentTemperatureText = (TextView) findViewById(R.id.apparentTemperatureText);
        apparentTemperatureText.setTextColor(layoutManager.getForegroundColor());


        // Notification
        Switch switchNotification = (Switch) findViewById(R.id.switchNotification);
        switchNotification.setTextColor(layoutManager.getForegroundColor());
        switchNotification.setChecked(userPreferencesManager.isNotification());
        switchNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UserPreferencesManager.getInstance().setNotification(isChecked);
            }
        });

        // Notification time
        //http://developer.android.com/guide/topics/ui/controls/pickers.html

        // Apparent temperature
        Switch apparentTemperatureSwitch = (Switch) findViewById(R.id.switchApparentTemperature);
        apparentTemperatureSwitch.setTextColor(layoutManager.getForegroundColor());
        apparentTemperatureSwitch.setChecked(userPreferencesManager.isApparentTemperature());
        apparentTemperatureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UserPreferencesManager.getInstance().setApparentTemperature(isChecked);
            }
        });


        //
    }
}
