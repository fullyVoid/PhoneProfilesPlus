package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class LocationScannerSwitchGPSBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] LocationScannerSwitchGPSBroadcastReceiver.onReceive", "xxx");
//        PPApplication.logE("[IN_BROADCAST_ALARM] LocationScannerSwitchGPSBroadcastReceiver.onReceive", "xxx");

        final Context appContext = context.getApplicationContext();

        doWork(appContext);
    }

    static void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(_context, LocationScannerSwitchGPSBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_LOCATION_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER);
                //intent.setClass(context, LocationScannerSwitchGPSBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        PPApplication.cancelWork(MainWorker.LOCATION_SCANNER_SWITCH_GPS_TAG_WORK, false);
    }

    static void setAlarm(Context context)
    {
        removeAlarm(context);

        int interval = 25; // seconds
        if (ApplicationPreferences.applicationEventLocationUpdateInterval > 1)
            interval = (ApplicationPreferences.applicationEventLocationUpdateInterval * 60) / LocationScanner.INTERVAL_DIVIDE_VALUE; // interval is in minutes
        int delay = interval + 10; // interval from settings + 10 seconds;

        if (!LocationScanner.useGPS)
            delay = 30 * 60;  // 30 minutes with GPS OFF

        if (!PPApplication.isIgnoreBatteryOptimizationEnabled(context)) {
            if (ApplicationPreferences.applicationUseAlarmClock) {
                //Intent intent = new Intent(_context, LocationScannerSwitchGPSBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_LOCATION_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER);
                //intent.setClass(context, LocationScannerSwitchGPSBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, delay);
                    long alarmTime = now.getTimeInMillis();

                    Intent editorIntent = new Intent(context, EditorActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                }
            } else {
            /*int keepResultsDelay = delay * 5;
            if (keepResultsDelay < PPApplication.WORK_PRUNE_DELAY)
                keepResultsDelay = PPApplication.WORK_PRUNE_DELAY;*/
                OneTimeWorkRequest worker =
                        new OneTimeWorkRequest.Builder(MainWorker.class)
                                .addTag(MainWorker.LOCATION_SCANNER_SWITCH_GPS_TAG_WORK)
                                .setInitialDelay(delay, TimeUnit.SECONDS)
                                .build();
                try {
                    if (PPApplication.getApplicationStarted(true, true)) {
                        WorkManager workManager = PPApplication.getWorkManagerInstance();
                        if (workManager != null) {
//                        //if (PPApplication.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(MainWorker.LOCATION_SCANNER_SWITCH_GPS_TAG_WORK);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                        } catch (Exception ignored) {
//                        }
//                        //}

//                            PPApplication.logE("[WORKER_CALL] LocationScannerSwitchGPSBroadcastReceiver.setAlarm", "xxx");
                            workManager.enqueueUniqueWork(MainWorker.LOCATION_SCANNER_SWITCH_GPS_TAG_WORK, ExistingWorkPolicy.REPLACE/*KEEP*/, worker);
                        }
                    }
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        }
        else {

            //Intent intent = new Intent(_context, LocationScannerSwitchGPSBroadcastReceiver.class);
            Intent intent = new Intent();
            intent.setAction(PhoneProfilesService.ACTION_LOCATION_SCANNER_SWITCH_GPS_BROADCAST_RECEIVER);
            //intent.setClass(context, LocationScannerSwitchGPSBroadcastReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (ApplicationPreferences.applicationUseAlarmClock) {
                    Calendar now = Calendar.getInstance();
                    now.add(Calendar.SECOND, delay);
                    long alarmTime = now.getTimeInMillis();

                    Intent editorIntent = new Intent(context, EditorActivity.class);
                    editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime, infoPendingIntent);
                    alarmManager.setAlarmClock(clockInfo, pendingIntent);
                } else {

                    long alarmTime = SystemClock.elapsedRealtime() + delay * 1000L;

                    //if (android.os.Build.VERSION.SDK_INT >= 23)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    //else //if (android.os.Build.VERSION.SDK_INT >= 19)
                    //    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                    //else
                    //    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime, pendingIntent);
                }
            }
        }
    }

    static void doWork(final Context appContext) {
        //PPApplication.startHandlerThreadPPScanners(/*"BootUpReceiver.onReceive2"*/);
        //final Handler __handler2 = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
        //__handler2.post(new PPApplication.PPHandlerThreadRunnable(
        //        appContext) {
        //__handler2.post(() -> {
        Runnable runnable = () -> {
//                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=LocationScannerSwitchGPSBroadcastReceiver.doWork");

            //Context appContext= appContextWeakRef.get();
            //if (appContext != null) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":LocationScannerSwitchGPSBroadcastReceiver_doWork");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    synchronized (PPApplication.locationScannerMutex) {
                        if ((PhoneProfilesService.getInstance() != null) && (PPApplication.locationScanner != null)) {

                            if (LocationScanner.mUpdatesStarted) {
//                              if (LocationScanner.useGPS) {
//                                  if (PPApplication.googlePlayServiceAvailable) {
//                                      locationScanner.flushLocations();
//                                      PPApplication.sleep(5000);
//                                  }
//                              }

                                PPApplication.locationScanner.stopLocationUpdates();

                                GlobalUtils.sleep(1000);

                                if (ApplicationPreferences.applicationEventLocationUseGPS &&
                                        (!CheckOnlineStatusBroadcastReceiver.isOnline(appContext)))
                                    // force useGPS
                                    LocationScanner.useGPS = true;
                                else {
                                    boolean useGPS = LocationScanner.useGPS;
                                    LocationScanner.useGPS = !useGPS;
                                }

                                // this also calls LocationScannerSwitchGPSBroadcastReceiver.setAlarm()
                                String provider = PPApplication.locationScanner.startLocationUpdates();
                                PPApplication.locationScanner.updateTransitionsByLastKnownLocation(provider);
                            }
                        }
                    }

                } catch (Exception e) {
//                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
        PPApplication.createScannersExecutor();
        PPApplication.scannersExecutor.submit(runnable);
    }

}
