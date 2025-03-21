package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class WifiScanBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] WifiScanBroadcastReceiver.onReceive","xxx");

        if (intent == null)
            return;

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true, true))
            // application is not started
            return;

        if (ApplicationPreferences.prefForceOneWifiScan != WifiScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
            if (!ApplicationPreferences.applicationEventWifiEnableScanning)
                // scanning is disabled
                return;
        }

        if (intent.getAction() != null) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                // WifiScanBroadcastReceiver

                boolean resultsUpdated; // = false;
                if (intent.hasExtra(WifiManager.EXTRA_RESULTS_UPDATED)) {
//                    PPApplication.logE("[IN_BROADCAST] WifiScanBroadcastReceiver.onReceive", "EXTRA_RESULTS_UPDATED exists");
                    resultsUpdated = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, true);
                    WifiScanWorker.fillScanResults(appContext);
//                    PPApplication.logE("[IN_BROADCAST] WifiScanBroadcastReceiver.onReceive","fillScanResults - end");
                }
                else {
//                    PPApplication.logE("[IN_BROADCAST] WifiScanBroadcastReceiver.onReceive", "EXTRA_RESULTS_UPDATED NOT exists");
                    resultsUpdated = true;
                }

                if (!resultsUpdated)
                    return;

                final int forceOneScan = ApplicationPreferences.prefForceOneWifiScan;

                if (Event.getGlobalEventsRunning() || (forceOneScan == WifiScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG)) {

                    if (ApplicationPreferences.prefEventWifiWaitForResult) {

//                        PPApplication.logE("[IN_BROADCAST] WifiScanBroadcastReceiver.onReceive", "start Worker");

                        WifiScanWorker.setWaitForResults(appContext, false);
                        WifiScanner.setForceOneWifiScan(appContext, WifiScanner.FORCE_ONE_SCAN_DISABLED);

                        if (forceOneScan != WifiScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) // not start service for force scan
                        {

                            PPExecutors.handleEvents(appContext, EventsHandler.SENSOR_TYPE_WIFI_SCANNER, "SENSOR_TYPE_WIFI_SCANNER", 5);
                            /*
                            Data workData = new Data.Builder()
                                    .putInt(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_WIFI_SCANNER)
                                    .build();

                            OneTimeWorkRequest worker =
                                    new OneTimeWorkRequest.Builder(MainWorker.class)
                                            .addTag(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_RECEIVER_WORK_TAG)
                                            .setInputData(workData)
                                            .setInitialDelay(5, TimeUnit.SECONDS)
                                            //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                            .build();
                            try {
                                if (PPApplication.getApplicationStarted(true)) {
                                    WorkManager workManager = PPApplication.getWorkManagerInstance();
                                    if (workManager != null) {

//                                        //if (PPApplication.logEnabled()) {
//                                        ListenableFuture<List<WorkInfo>> statuses;
//                                        statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_RECEIVER_WORK_TAG);
//                                        try {
//                                            List<WorkInfo> workInfoList = statuses.get();
//                                        } catch (Exception ignored) {
//                                        }
//                                        //}

//                                        PPApplication.logE("[WORKER_CALL] WifiScanBroadcastReceiver.onReceive", "xxx");
                                        //workManager.enqueue(worker);
                                        workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_RECEIVER_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                                    }
                                }
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                            */

                        }

//                        PPApplication.logE("[IN_BROADCAST] WifiScanBroadcastReceiver.onReceive", "end start Worker");

/*
                        PPApplication.startHandlerThreadBroadcast();
                        final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                PowerManager.WakeLock wakeLock = null;
                                try {
                                    if (powerManager != null) {
                                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiScanBroadcastReceiver_onReceive_1");
                                        wakeLock.acquire(10 * 60 * 1000);
                                    }

                                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=WifiScanBroadcastReceiver.onReceive.1");

                                    if (WifiScanWorker.wifi == null)
                                        WifiScanWorker.wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);

                                    WifiScanWorker.setWaitForResults(appContext, false);
                                    WifiScanner.setForceOneWifiScan(appContext, WifiScanner.FORCE_ONE_SCAN_DISABLED);

                                    if (forceOneScan != WifiScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) // not start service for force scan
                                    {

                                        Data workData = new Data.Builder()
                                                .putString(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_WIFI_SCANNER)
                                                .build();

                                        OneTimeWorkRequest worker =
                                                new OneTimeWorkRequest.Builder(MainWorker.class)
                                                        .addTag(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_RECEIVER_WORK_TAG)
                                                        .setInputData(workData)
                                                        .setInitialDelay(5, TimeUnit.SECONDS)
                                                        //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                                        .build();
                                        try {
                                            if (PPApplication.getApplicationStarted(true)) {
                                                WorkManager workManager = PPApplication.getWorkManagerInstance();
                                                if (workManager != null) {

//                                                //if (PPApplication.logEnabled()) {
//                                                ListenableFuture<List<WorkInfo>> statuses;
//                                                statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_RECEIVER_WORK_TAG);
//                                                try {
//                                                    List<WorkInfo> workInfoList = statuses.get();
//                                                } catch (Exception ignored) {
//                                                }
//                                                //}

                                                    //workManager.enqueue(worker);
                                                    workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_RECEIVER_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                                                }
                                            }
                                        } catch (Exception e) {
                                            PPApplication.recordException(e);
                                        }

                                    }
                                } finally {
                                    if ((wakeLock != null) && wakeLock.isHeld()) {
                                        try {
                                            wakeLock.release();
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                            }
                        });
*/
                    }
                }
            }
        }
    }

}
