package com.thunderwarn.thunderwarn.scheduler;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;

import com.thunderwarn.thunderwarn.R;
import com.thunderwarn.thunderwarn.common.Log;

import com.thunderwarn.thunderwarn.MainActivity;
import com.thunderwarn.thunderwarn.OpenWeatherMap.processors.Json3HoursProcessor;
import com.thunderwarn.thunderwarn.common.SharedResources;
import com.thunderwarn.thunderwarn.common.configuration.CacheManager;
import com.thunderwarn.thunderwarn.common.configuration.ConfigurationManager;
import com.thunderwarn.thunderwarn.common.configuration.UserPreferencesManager;
import com.thunderwarn.thunderwarn.manager.WeatherConditionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ivofernandes on 18/10/15.
 */
public class SendNotificationManager {

    private static final String TAG = "SendNotificationManager";

    // Singleton
    private static SendNotificationManager instance = new SendNotificationManager();

    private SendNotificationManager(){}

    public static SendNotificationManager getInstance(){
        return instance;
    }

    public static final String LAST_NOTIFICATION = "LAST_NOTIFICATION";

    // Notification ID to allow for future updates

    // Vibration pattern
    private long[] mVibratePattern = { 0, 200};

    // Fields
    private SharedResources sharedResources = SharedResources.getInstance();

    private Json3HoursProcessor processor = new Json3HoursProcessor();

    private ConfigurationManager configurationManager = ConfigurationManager.getInstance();

    /**
     * Receives a 3h json and generate the notifications that it should produce
     * @param json3h
     */
    public void fireNotications(JSONObject json3h){

        Log.d(TAG, "fireNotications json " + json3h);

        try {
            Calendar currentDate = Calendar.getInstance();

            Log.d(TAG, "currentDate " + currentDate.getTime());

            // try notification for today,
            // for cases when the app was installed after 00:00 and before the user going to sleep
            boolean todayNotification = validateRain(json3h, currentDate, 0,
                    sharedResources.resolveString(R.string.notification_today_expect));

            // if today notification is not fired, then try for tomorrow
            if(!todayNotification) {
                validateRain(json3h, currentDate, 1, sharedResources.resolveString(R.string.notification_tomorrow_expect));
            }

            NotificationSchedulingService.getInstance().done();
        } catch (JSONException e) {
            Log.e(TAG,"error firing notification, reading json data: " + e.getMessage(),e);
            e.getStackTrace()[0].getClassName();

        }
    }

    /**
     *
     * @param json3h
     * @param currentDate
     * @param nextDay defines the day where will validate if rains:
     *          - 0, today
     *          - 1, tomorrow
     * @param whenExpect
     * @return if the notification was fired
     * @throws JSONException
     */
    private boolean validateRain(JSONObject json3h, Calendar currentDate,int nextDay, String whenExpect) throws JSONException {

        if(!json3h.has("list")){
            Log.e(TAG,"not found list in json: "+ json3h);
        }

        JSONArray list = json3h.getJSONArray("list");

        // precipitation tomorrow counter
        double precipitation = 0;
        WeatherConditionManager.WeatherCondition weatherNotification = WeatherConditionManager.WeatherCondition.CLOUDS;
        String idNotification = "";
        String weatherDescription = "";
        Calendar notificationTime = null;

        for (int i=0 ; i<list.length() ; i++){
            JSONObject prediction = list.getJSONObject(i);
            long timeJson = prediction.getLong("dt")* 1000;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(timeJson));

            int daysDiff = calendar.get(Calendar.DAY_OF_MONTH) - currentDate.get(Calendar.DAY_OF_MONTH);

            // Skip to today/tomorrow
            if(daysDiff < nextDay){
                continue;
            }

            // Stop when pass today/tomorrow
            else if(daysDiff > nextDay){
                break;
            }
            double rain = processor.getRain(prediction);
            String id = processor.getWeatherId(prediction,rain);
            WeatherConditionManager.WeatherCondition weatherCondition =
                    WeatherConditionManager.getInstance().getWeatherConditionById(id);

            // If the
            if(weatherCondition != null && !weatherNotification.equals(weatherCondition)) {

                weatherNotification = WeatherConditionManager.getInstance().moreImportantCondition(
                        weatherNotification, weatherCondition);

                if(weatherNotification.equals(weatherCondition)) {
                    idNotification = id;
                    weatherDescription = processor.getWeatherDescription(prediction, rain, 0, 3, weatherCondition);
                    notificationTime = calendar;
                }
            }

        }

        /*
        weatherNotification = WeatherConditionManager.WeatherCondition.RAIN_HEAVY;
        weatherDescription = "chuva teste";
        notificationTime = Calendar.getInstance();
        notificationTime.setTime(new Date());
        notificationTime.set(Calendar.DAY_OF_YEAR, notificationTime.get(Calendar.DAY_OF_YEAR)+1);
*/

        Log.d(TAG, "weatherNotification " + weatherNotification);

        return generateRainNotification(weatherNotification, idNotification, weatherDescription, notificationTime,whenExpect,nextDay);

    }

    private boolean generateRainNotification(WeatherConditionManager.WeatherCondition rain,
                                             String idNotification, String weatherDescription,
                                             Calendar notificationTime,
                                             String whenExpect, int nextDay) {
        boolean weatherNotification = !rain.equals(WeatherConditionManager.WeatherCondition.CLOUDS);

        if(weatherNotification) {
            Log.d(TAG,"fire notification if is notification time");
            // Validate if already fired a notification in this day
            if(isNotificationTime(notificationTime,nextDay)) {
                Log.d(TAG,"is notification time!!");
                // Generate the notification
                String title = sharedResources.resolveString(R.string.notification_title);

                String description = whenExpect + " " + weatherDescription;

                fireNotication(title, description);

                String dateString = CacheManager.DATE_FORMAT.format(notificationTime.getTime());

                SharedPreferences.Editor editor = CacheManager.getInstance().getSharedPreferences().edit();
                editor.putString(LAST_NOTIFICATION, dateString);
                editor.commit();

                return true;
            }
        }
        return false;
    }

    public boolean isNotificationTime(Calendar notificationTime, int nextDay){
        Log.d(TAG,"validate date for notification in " + nextDay + ": "
                + notificationTime.getTime());

        boolean result = true;

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());

        int notificationHour = SendNotificationManager.getInstance().getNotificationHour();
        int notificationMinute = SendNotificationManager.getInstance().getNotificationMinute();

        Calendar notificationDate = Calendar.getInstance();
        notificationDate.setTime(new Date());
        notificationDate.set(Calendar.HOUR_OF_DAY, notificationHour);
        notificationDate.set(Calendar.MINUTE, notificationMinute);

        // If isn't come yet the notification time return...
        Calendar startOfTheDay = Calendar.getInstance();
        //TODO user config to know when the notification will be fired?
        notificationDate.set(Calendar.HOUR_OF_DAY, 18);
        notificationDate.set(Calendar.MINUTE, 0);

        // Send notifications only if passed the notification time
        if (now.before(notificationDate) && nextDay > 0){
            return false;
        }

        Log.d(TAG,"current datetime is ok for notifications");

        // Validate if the notification already been sent
        String lastNotification =
                CacheManager.getInstance().getSharedPreferences().getString(LAST_NOTIFICATION, "");

        Log.d(TAG,"lastNotification: " + lastNotification);

        if (!lastNotification.equals("") && UserPreferencesManager.getInstance().isNotification()) {
            Date lastNotificationDate = null;
            try {
                lastNotificationDate = CacheManager.DATE_FORMAT.parse(lastNotification);

                Calendar lastNotificationCalendar = Calendar.getInstance();
                lastNotificationCalendar.setTime(lastNotificationDate);

                result = notificationTime.get(Calendar.DAY_OF_YEAR)
                        > lastNotificationCalendar.get(Calendar.DAY_OF_YEAR);

                Log.d(TAG,"isNotificationTime result: " + result);

            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date " + lastNotification + ": " + e.getMessage(), e);
            }
        }

        return result;
    }

    public void fireNotication(String title, String description) {
        Log.d(TAG,"notification: " + description);
        Intent notificationIntent = new Intent(sharedResources.getContext(), MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent = PendingIntent.getActivity(sharedResources.getContext(), 0,
                notificationIntent, 0);

        Notification.Builder notificationBuilder = new Notification.Builder(
                sharedResources.getContext())
                .setTicker(title)
                .setSmallIcon(R.drawable.notification_icon)
                .setAutoCancel(true)
                .setVibrate(mVibratePattern)
                .setContentTitle(title)
                .setContentIntent(intent)
                .setContentText(description);

        SharedResources.getInstance().getNotificationManager().notify(SharedResources.RAIN_NOTIFICATION_ID,
                notificationBuilder.build());

    }

    public int getNotificationHour(){
        return 18;
    }

    public int getNotificationMinute(){
        return 0;
    }
}
