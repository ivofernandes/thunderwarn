package com.thunderwarn.thunderwarn.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.thunderwarn.thunderwarn.common.Log;
import com.thunderwarn.thunderwarn.common.SharedResources;

import java.util.Calendar;

/**
 * When the alarm fires, this WakefulBroadcastReceiver receives the broadcast Intent 
 * and then starts the IntentService {@code NotificationSchedulingService} to do some work.
 */
public class NotificationAlarmReceiver extends WakefulBroadcastReceiver {

    private static String TAG = "NotificationAlarmReceiver";
    // The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager alarmMgr;
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent alarmIntent;
  
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            SharedResources.getInstance().init(context);

            Log.d(TAG, "intent received");

            // BEGIN_INCLUDE(alarm_onreceive)
            /*
             * If your receiver intent includes extras that need to be passed along to the
             * service, use setComponent() to indicate that the service should handle the
             * receiver's intent. For example:
             *
             * ComponentName comp = new ComponentName(context.getPackageName(),
             *      MyService.class.getName());
             *
             * // This intent passed in this call will include the wake lock extra as well as
             * // the receiver intent contents.
             * startWakefulService(context, (intent.setComponent(comp)));
             *
             * In this example, we simply create a new intent to deliver to the service.
             * This intent holds an extra identifying the wake lock.
             */
            Intent service = new Intent(context, NotificationSchedulingService.class);

            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, service);
            // END_INCLUDE(alarm_onreceive)
        }catch(Exception e){
            Log.e(TAG, "Error on alarm receiver " + e.getMessage(), e);
        }
    }

    // BEGIN_INCLUDE(set_alarm)
    /**
     * Sets a repeating alarm that runs once a day at approximately 8:30 a.m. When the
     * alarm fires, the app broadcasts an Intent to this WakefulBroadcastReceiver.
     */
    public void setAlarm() {

        Log.d(TAG, "start setting alarm");

        alarmMgr = (AlarmManager) SharedResources.getInstance().getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(SharedResources.getInstance().getContext(), NotificationAlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(SharedResources.getInstance().getContext(), 0, intent, 0);
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Set the alarm's trigger to the next 5 minutes
        calendar.add(Calendar.MINUTE, 5);

        Log.d(TAG, "setting alarm to the time: " + calendar.getTime());

        // Set the alarm to fire
        alarmMgr.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                alarmIntent);

        // Enable {@code NotificationBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(SharedResources.getInstance().getContext(), NotificationBootReceiver.class);
        PackageManager pm = SharedResources.getInstance().getContext().getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

    }
    // END_INCLUDE(set_alarm)

    /**
     * Cancels the alarm.
     * @param context
     */
    // BEGIN_INCLUDE(cancel_alarm)
    public void cancelAlarm(Context context) {
        // If the alarm has been set, cancel it.
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
        
        // Disable {@code NotificationBootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, NotificationBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
    // END_INCLUDE(cancel_alarm)

}
