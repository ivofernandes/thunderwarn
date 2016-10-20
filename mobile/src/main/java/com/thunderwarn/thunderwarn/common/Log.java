package com.thunderwarn.thunderwarn.common;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.widget.Toast;

import com.thunderwarn.thunderwarn.MainActivity;
import com.thunderwarn.thunderwarn.R;
import com.thunderwarn.thunderwarn.activities.LogActivity;
import com.thunderwarn.thunderwarn.common.configuration.CacheManager;
import com.thunderwarn.thunderwarn.manager.WeatherDataManager;
import com.thunderwarn.thunderwarn.scheduler.SendNotificationManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ivofernandes on 17/12/15.
 */
public class Log {
    private static final String TAG = "Log";
    private static final String ERROR_LOG_CACHE = "ERROR_LOG";
    private static final String LOG_CACHE = "LOG";

    private static List<Date> clickCounter = new ArrayList<Date>();
    private static boolean LOG = true;
    private static boolean errorVisualLog = false;
    private static boolean canEnableLog = true;
    private static boolean log;
    private static ArrayList<String> allLogs = new ArrayList<>();

    public static void i(String tag, String string) {
        try {
            if (LOG){
                android.util.Log.i(tag, string);
                allLogs.add("I " + tag + " " + string);
            }
        }catch(Exception ex){
            // If can't log, don't break the app...
        }
    }

    public static void d(String tag, String string) {
        try {
            if (LOG){
                android.util.Log.d(tag, string);
                allLogs.add("D " + tag + " " + string);
            }
        }catch(Exception ex){
            // If can't log, don't break the app...
        }
    }

    public static void v(String tag, String string) {
        try {
            if (LOG){
                android.util.Log.v(tag, string);
            }
        }catch(Exception ex){
            // If can't log, don't break the app...
        }
    }

    public static void w(String tag, String string) {
        try {
            if (LOG){
                android.util.Log.w(tag, string);
                allLogs.add("W " + tag + " " + string);
            }
        }catch(Exception ex){
            // If can't log, don't break the app...
        }
    }

    public static void e(String tag, String string, Exception e) {
        try {
            if (LOG) {
                if (e == null) {
                    android.util.Log.e(tag, string);
                    allLogs.add("E " + tag + " " + string);
                } else {
                    android.util.Log.e(tag, string, e);
                    String stack = "";
                    if (e != null) {
                        stack = e.getMessage();

                        if (e.getStackTrace() != null) {
                            if (e.getStackTrace().length > 0) {
                                StackTraceElement traceLine = e.getStackTrace()[0];
                                stack += traceLine.toString();
                            }
                        }
                    }
                    allLogs.add("E " + tag + " " + string + " " + stack);
                }
                if (errorVisualLog && MainActivity.getInstance().getApplicationContext() != null) {

                    // create intent that show the error log
                    Intent notificationIntent = new Intent(MainActivity.getInstance().getApplicationContext(), LogActivity.class);
                    notificationIntent.putExtra("logList", "all");

                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    PendingIntent intent = PendingIntent.getActivity(SharedResources.getInstance().getContext(), 0,
                            notificationIntent, 0);

                    Notification.Builder notificationBuilder = new Notification.Builder(
                            SharedResources.getInstance().getContext())
                            .setTicker("Error")
                            .setSmallIcon(R.drawable.notification_icon)
                            .setAutoCancel(true)
                            .setContentTitle("Error")
                            .setContentIntent(intent)
                            .setContentText(string);

                    SharedResources.getInstance().getNotificationManager().notify(SharedResources.LOG_NOTIFICATION_ID,
                            notificationBuilder.build());
                }
            }
        }catch(Exception ex){
            // If can't log an exception, don't break the app...
        }
    }

    public static void e(String tag, String string) {
        e(tag, string, null);
    }

    public static void clickToLog() {
        clickCounter.add(new Date());

        // keep only the clicks in the last 5 minutes
        boolean toRemove = true;
        while(toRemove) {
            Date firstDate = clickCounter.get(0);

            toRemove = !WeatherDataManager.insideDateThreshold(firstDate, 5);

            if(toRemove){
                clickCounter.remove(0);
            }
        }

        // If had 10 clicks in last 5 minutes activate logs for errors
        if(clickCounter.size() == 10){
            setLog(true,true);
        }
        // If had 20 clicks in last 5 minutes disable logs
        else if (clickCounter.size() == 20){
            setLog(false,false);
            clickCounter.clear();
            if(SharedResources.getInstance().getContext() != null && canEnableLog) {
                Toast.makeText(SharedResources.getInstance().getContext(), "Disable log", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void setLog(boolean log,boolean errorVisualLog) {
        if(Log.canEnableLog) {

            Log.LOG = log;
            Log.errorVisualLog = errorVisualLog;

            if (log && errorVisualLog) {
                if (SharedResources.getInstance().getContext() != null) {
                    Toast.makeText(SharedResources.getInstance().getContext(), "Log enabled",
                            Toast.LENGTH_SHORT).show();
                }
            }

            CacheManager.getInstance().putBoolean(ERROR_LOG_CACHE, getErrorVisualLog());
        }
    }

    public static void initLog(){
        if(Log.canEnableLog) {
            boolean errorLog = CacheManager.getInstance().getBoolean(ERROR_LOG_CACHE, false);
            boolean log = errorLog;

            // For tests
            //log = true;
            Log.d(TAG, "initing log errorLog: " + errorLog);

            setLog(log, errorLog);
        }
    }


    public static boolean getErrorVisualLog(){
        return errorVisualLog;
    }

    public static List<String> allLogs() {

        while(allLogs.size() > 100){
            allLogs.remove(0);
        }

        return allLogs;
    }
}
