package com.thunderwarn.thunderwarn.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.thunderwarn.thunderwarn.MainActivity;
import com.thunderwarn.thunderwarn.R;
import com.thunderwarn.thunderwarn.common.Log;
import com.thunderwarn.thunderwarn.common.SharedResources;

/**
 * Created by ivofernandes on 30/10/15.
 */
public class MenuActivity extends ActionBarActivity {

    private SharedResources sharedResources = SharedResources.getInstance();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if(Log.getErrorVisualLog()){
            MenuItem menuItem = menu.add("Log");
            Intent intent = new Intent(MainActivity.getInstance(), LogActivity.class);
            intent.putExtra("logList","all");
            menuItem.setIntent(intent);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.getInstance(), SettingsActivity.class);
            MainActivity.getInstance().startActivity(intent);
            return true;
        }

        if (id == R.id.action_help) {
            Intent intent = new Intent(MainActivity.getInstance(), HelpActivity.class);
            MainActivity.getInstance().startActivity(intent);
            return true;
        }

        //TODO Location change
        // Remember that the objective of the app is too avoid user to get wet,
        // and forget the clothes outside at the rain,
        // so maybe the change of the location must be a temporary and then return to GPS
        /*
        if( id == R.id.action_change_location){
            Intent intent = new Intent(MainActivity.getInstance(), ChangeLocationActivity.class);
            MainActivity.getInstance().startActivity(intent);

        }
        */



        return super.onOptionsItemSelected(item);
    }
}
