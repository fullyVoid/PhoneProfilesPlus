package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

class IgnoreBatteryOptimizationNotification {

    //private static final String PREF_SHOW_IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ON_START = "show_ignore_battery_optimization_notification_on_start";

    static void showNotification(Context context, boolean useHandler) {
        final Context appContext = context.getApplicationContext();
        if (useHandler) {
            //PPApplication.startHandlerThread(/*"IgnoreBatteryOptimizationNotification.showNotification"*/);
            //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(
            //        context.getApplicationContext()) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                        PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=IgnoreBatteryOptimizationNotification.showNotification");

                //Context appContext= appContextWeakRef.get();
                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":IgnoreBatteryOptimizationNotification_showNotification");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

                        //if (show) {
                        PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        try {
                            if (pm != null) {
                                if (!pm.isIgnoringBatteryOptimizations(PPApplication.PACKAGE_NAME)) {
                                    //if (ApplicationPreferences.prefShowIgnoreBatteryOptimizationNotificationOnStart)
                                        showNotification(appContext,
                                                appContext.getString(R.string.ignore_battery_optimization_notification_title),
                                                appContext.getString(R.string.ignore_battery_optimization_notification_text));
                                }/* else {
                                    // show notification again
                                    setShowIgnoreBatteryOptimizationNotificationOnStart(appContext, true);
                                }*/
                            }
                        } catch (Exception ignore) {
                        }
                        //}

                    } catch (Exception e) {
//                            PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                //}
            }; //);
            PPApplication.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);

        }
        else {
            PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            try {
                if (pm != null) {
                    if (!pm.isIgnoringBatteryOptimizations(PPApplication.PACKAGE_NAME)) {
                        //if (ApplicationPreferences.prefShowIgnoreBatteryOptimizationNotificationOnStart)
                            showNotification(appContext,
                                    appContext.getString(R.string.ignore_battery_optimization_notification_title),
                                    appContext.getString(R.string.ignore_battery_optimization_notification_text));
                    }
                    /*else {
                        // show notification again
                        setShowIgnoreBatteryOptimizationNotificationOnStart(appContext, true);
                    }*/
                }
            } catch (Exception ignore) {
            }
        }
    }

    @SuppressLint("BatteryLife")
    static private void showNotification(Context context, String title, String text) {
        PPApplication.createExclamationNotificationChannel(context);
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(context, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(text) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));

        Intent intent;
        //PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        String packageName = PPApplication.PACKAGE_NAME;
        //if (pm.isIgnoringBatteryOptimizations(packageName)) {
//            intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        //}
        //else {
        //    DO NOT USE IT, CHANGE IS NOT DISPLAYED IN SYSTEM SETTINGS
        //    But in ONEPLUS it IS ONLY SOLUTION !!!
            intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
        //    if (!GlobalGUIRoutines.activityIntentExists(intent, context)) {
        //        intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        //    }
        //}

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        mBuilder.setOnlyAlertOnce(true);

        /*
        Intent disableIntent = new Intent(context, IgnoreBatteryOptimizationDisableActivity.class);
        PendingIntent pDisableIntent = PendingIntent.getActivity(context, 0, disableIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(
                //R.drawable.ic_action_exit_app,
                R.drawable.ic_empty,
                context.getString(R.string.ignore_battery_optimization_notification_disable_button),
                pDisableIntent);
        mBuilder.addAction(actionBuilder.build());
        */

        mBuilder.setWhen(0);

        mBuilder.setGroup(PPApplication.SYTEM_CONFIGURATION_ERRORS_NOTIFICATION_GROUP);

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
        try {
            mNotificationManager.notify(
                    PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_TAG,
                    PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
            //Log.e("IgnoreBatteryOptimizationNotification.showNotification", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    static void removeNotification(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.cancel(
                    PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_TAG,
                    PPApplication.IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ID);
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

/*
    static void getShowIgnoreBatteryOptimizationNotificationOnStart(Context context)
    {
        synchronized (PPApplication.applicationGlobalPreferencesMutex) {
            ApplicationPreferences.prefShowIgnoreBatteryOptimizationNotificationOnStart = ApplicationPreferences.
                    getSharedPreferences(context).getBoolean(PREF_SHOW_IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ON_START, true);
            //return prefRingerVolume;
        }
    }

    static void setShowIgnoreBatteryOptimizationNotificationOnStart(Context context, boolean show)
    {
        synchronized (PPApplication.applicationGlobalPreferencesMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putBoolean(PREF_SHOW_IGNORE_BATTERY_OPTIMIZATION_NOTIFICATION_ON_START, show);
            editor.apply();
            ApplicationPreferences.prefShowIgnoreBatteryOptimizationNotificationOnStart = show;
        }
    }
 */
}
