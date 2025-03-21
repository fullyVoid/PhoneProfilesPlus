package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;

class WifiScanner {

    private final Context context;

    static final int WIFI_SCAN_DURATION = 25;      // 25 seconds for wifi scan

    //static boolean wifiEnabledForScan;

    private static final String PREF_FORCE_ONE_WIFI_SCAN = "forceOneWifiScanInt";

    static final int FORCE_ONE_SCAN_DISABLED = 0;
    static final int FORCE_ONE_SCAN_FROM_PREF_DIALOG = 3;

    //private static final String PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_WIFI = "show_enable_location_notification_wifi";

    WifiScanner(Context context) {
        this.context = context.getApplicationContext();
    }

    void doScan(boolean fromDialog) {
        synchronized (PPApplication.wifiScannerMutex) {
            if (!PPApplication.getApplicationStarted(true, true))
                // application is not started
                return;

            //DataWrapper dataWrapper;

            // check power save mode
            //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
            boolean isPowerSaveMode = GlobalUtils.isPowerSaveMode(context);
            int forceScan = ApplicationPreferences.prefForceOneWifiScan;
            if (isPowerSaveMode) {
                if (forceScan != FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                    if (ApplicationPreferences.applicationEventWifiScanInPowerSaveMode.equals("2")) {
                        // not scan wi-fi in power save mode
                        return;
                    }
                }
            }
            else {
                if (forceScan != FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                    if (ApplicationPreferences.applicationEventWifiScanInTimeMultiply.equals("2")) {
                        if (GlobalUtils.isNowTimeBetweenTimes(
                                ApplicationPreferences.applicationEventWifiScanInTimeMultiplyFrom,
                                ApplicationPreferences.applicationEventWifiScanInTimeMultiplyTo)) {
                            // not scan wi-fi in configured time
                            return;
                        }
                    }
                }
            }

            //PPApplication.startHandlerThreadPPScanners(/*"WifiScanner.doScan.1"*/);
            //final Handler wifiChangeHandler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());

            //synchronized (PPApplication.radioChangeStateMutex) {

                WifiScanWorker.fillWifiConfigurationList(context/*, false*/);

                boolean canScan = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED;
                if (canScan) {
                    if (!ApplicationPreferences.applicationEventWifiScanIgnoreHotspot) {
                        if (Build.VERSION.SDK_INT < 30)
                            canScan = !WifiApManager.isWifiAPEnabled(context);
                        else
                            //canScan = !CmdWifiAP.isEnabled(context);
                            canScan = !WifiApManager.isWifiAPEnabledA30(context);
                    }
                }

                if (canScan) {

                    //dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

                    // check if wifi scan events exists
                    //lock();
                    //boolean wifiEventsExists = DatabaseHandler.getInstance(context.getApplicationContext()).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false) > 0;
                    //unlock();
                    //int forceScan = ApplicationPreferences.prefForceOneWifiScan;
                    boolean scan; //(wifiEventsExists || (forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                    //if (scan) {
                    //    if (wifiEventsExists)
                            scan = isLocationEnabled(context/*, scannerType*/);
                    //}
                    if (!scan) {
                        // wifi scan events not exists
                        WifiScanWorker.cancelWork(context, fromDialog/*, null*/);
                    } else {
                        if (ApplicationPreferences.prefEventWifiEnabledForScan) {
                            // service restarted during scanning (prefEventWifiEnabledForScan is set to false at end of scan),
                            // disable wifi
                            //wifiChangeHandler.post(() -> {
                            Runnable runnable = () -> {
                                try {
//                                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=WifiScanner.doScan.1");
                                    if (WifiScanWorker.wifi == null)
                                        WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                    //lock();
                                    //if (Build.VERSION.SDK_INT >= 29)
                                    //    CmdWifi.setWifi(false);
                                    //else
                                    if (WifiScanWorker.wifi != null) {
                                        WifiScanWorker.wifi.setWifiEnabled(false);
                                    }
//                                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "END run - from=WifiScanner.doScan.1");
                                } catch (Exception e) {
                                    PPApplication.recordException(e);
                                }
                            }; //);
                            PPApplication.createScannersExecutor();
                            PPApplication.scannersExecutor.submit(runnable);
                            if (WifiScanWorker.wifi == null)
                                WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            waitForWifiDisabled(WifiScanWorker.wifi);
                            //PPApplication.sleep(1000);
                            //unlock();
                        }

                        //noinspection ConstantConditions
                        if (true /*canScanWifi(dataWrapper)*/) { // scan even if wifi is connected

                            WifiScanWorker.setScanRequest(context, false);
                            WifiScanWorker.setWaitForResults(context, false);
                            WifiScanWorker.setWifiEnabledForScan(context, false);

                            WifiScanWorker.unlock();

                            // start scan

                            //lock();

                            // enable wifi
                            int wifiState;
                            wifiState = enableWifi(WifiScanWorker.wifi/*, wifiChangeHandler*/);


                            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                                WifiScanWorker.startScan(context);
                            } else if (wifiState != WifiManager.WIFI_STATE_ENABLING) {
                                WifiScanWorker.setScanRequest(context, false);
                                WifiScanWorker.setWaitForResults(context, false);
                                setForceOneWifiScan(context, FORCE_ONE_SCAN_DISABLED);
                            }

                            if (ApplicationPreferences.prefEventWifiScanRequest ||
                                    ApplicationPreferences.prefEventWifiWaitForResult) {

                                // wait for scan end
                                waitForWifiScanEnd(/*context*/);


                                if (ApplicationPreferences.prefEventWifiWaitForResult) {
                                    if (ApplicationPreferences.prefForceOneWifiScan != WifiScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) // not start service for force scan
                                    {

                                        PPExecutors.handleEvents(context, EventsHandler.SENSOR_TYPE_WIFI_SCANNER, "SENSOR_TYPE_WIFI_SCANNER", 5);
                                        /*
                                        Data workData = new Data.Builder()
                                                .putInt(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_WIFI_SCANNER)
                                                .build();

                                        OneTimeWorkRequest worker =
                                                new OneTimeWorkRequest.Builder(MainWorker.class)
                                                        .addTag(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_SCANNER_WORK_TAG)
                                                        .setInputData(workData)
                                                        .setInitialDelay(5, TimeUnit.SECONDS)
                                                        //.keepResultsForAtLeast(PPApplication.WORK_PRUNE_DELAY_MINUTES, TimeUnit.MINUTES)
                                                        .build();
                                        try {
                                            if (PPApplication.getApplicationStarted(true)) {
                                                WorkManager workManager = PPApplication.getWorkManagerInstance();
                                                if (workManager != null) {

//                                                    //if (PPApplication.logEnabled()) {
//                                                    ListenableFuture<List<WorkInfo>> statuses;
//                                                    statuses = workManager.getWorkInfosForUniqueWork(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_SCANNER_WORK_TAG);
//                                                    try {
//                                                        List<WorkInfo> workInfoList = statuses.get();
//                                                    } catch (Exception ignored) {
//                                                    }
//                                                    //}

//                                                    PPApplication.logE("[WORKER_CALL] WifiScanner.doScan", "xxx");
                                                    //workManager.enqueue(worker);
                                                    workManager.enqueueUniqueWork(MainWorker.HANDLE_EVENTS_WIFI_SCANNER_FROM_SCANNER_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                                                }
                                            }
                                        } catch (Exception e) {
                                            PPApplication.recordException(e);
                                        }
                                        */

                                        /*PPApplication.startHandlerThread("WifiScanner.doScan");
                                        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                                                PowerManager.WakeLock wakeLock = null;
                                                try {
                                                    if (powerManager != null) {
                                                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiScanner_doScan");
                                                        wakeLock.acquire(10 * 60 * 1000);
                                                    }

                                                    // start events handler
                                                    EventsHandler eventsHandler = new EventsHandler(context);
                                                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_SCANNER);
                                                } finally {
                                                    if ((wakeLock != null) && wakeLock.isHeld()) {
                                                        try {
                                                            wakeLock.release();
                                                        } catch (Exception ignored) {}
                                                    }
                                                }
                                            }
                                        }, 5000);*/
                                        //PostDelayedBroadcastReceiver.setAlarmForHandleEvents(EventsHandler.SENSOR_TYPE_WIFI_SCANNER, 5, context);
                                    }
                                }
                            }

                            WifiScanWorker.unlock();
                            //unlock();
                        }
                    }

                    if (ApplicationPreferences.prefEventWifiEnabledForScan) {
                        //wifiChangeHandler.post(() -> {
                        Runnable runnable = () -> {
//                            PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=WifiScanner.doScan.2");

                            try {
                                if (WifiScanWorker.wifi == null)
                                    WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                //lock();
                                //if (Build.VERSION.SDK_INT >= 29)
                                //    CmdWifi.setWifi(false);
                                //else
                                if (WifiScanWorker.wifi != null) {
                                    WifiScanWorker.wifi.setWifiEnabled(false);
                                }
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }

//                            PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "END run - from=WifiScanner.doScan.1");
                        }; //);
                        PPApplication.createScannersExecutor();
                        PPApplication.scannersExecutor.submit(runnable);
                        //PPApplication.sleep(1000);
                        if (WifiScanWorker.wifi == null)
                            WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        waitForWifiDisabled(WifiScanWorker.wifi);
                        //unlock();
                    }
                }

                setForceOneWifiScan(context, FORCE_ONE_SCAN_DISABLED);
                WifiScanWorker.setWifiEnabledForScan(context, false);
                WifiScanWorker.setWaitForResults(context, false);
                WifiScanWorker.setScanRequest(context, false);

                WifiScanWorker.unlock();
                //unlock();

            //}

        }
    }

    static void getForceOneWifiScan(Context context)
    {
        synchronized (PPApplication.eventWifiSensorMutex) {
            ApplicationPreferences.prefForceOneWifiScan = ApplicationPreferences.
                    getSharedPreferences(context).getInt(PREF_FORCE_ONE_WIFI_SCAN, FORCE_ONE_SCAN_DISABLED);
        }
    }
    static void setForceOneWifiScan(Context context, int forceScan)
    {
        synchronized (PPApplication.eventWifiSensorMutex) {
            SharedPreferences.Editor editor = ApplicationPreferences.getEditor(context);
            editor.putInt(PREF_FORCE_ONE_WIFI_SCAN, forceScan);
            editor.apply();
            ApplicationPreferences.prefForceOneWifiScan = forceScan;
        }
    }

    /*
    private void lock() {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (wakeLock == null)
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ScanWakeLock");
        try {
            if (!wakeLock.isHeld())
                wakeLock.acquire(10 * 60 * 1000);
        } catch(Exception e) {
            Log.e("WifiScanner.lock", "Error getting Lock: ", e);
        }
    }

    private void unlock() {
        if ((wakeLock != null) && (wakeLock.isHeld())) {
            wakeLock.release();
        }
    }
    */

    private int enableWifi(WifiManager wifi/*, Handler wifiChangeHandler*/)
    {
        int wifiState = wifi.getWifiState();
        int forceScan = ApplicationPreferences.prefForceOneWifiScan;

        //if ((!dataWrapper.getIsManualProfileActivation()) || forceScan)
        //{
        if (wifiState != WifiManager.WIFI_STATE_ENABLING)
        {
            boolean isWifiEnabled = (wifiState == WifiManager.WIFI_STATE_ENABLED);
            boolean isScanAlwaysAvailable = false;
            if (forceScan != FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                // this must be disabled because scanning not working, when wifi is disabled after disabled WiFi AP
                // Tested and scanning working ;-)
                //if (android.os.Build.VERSION.SDK_INT >= 18)
                    isScanAlwaysAvailable = wifi.isScanAlwaysAvailable();
            }
            isWifiEnabled = isWifiEnabled || isScanAlwaysAvailable;
            if (!isWifiEnabled)
            {
                boolean applicationEventWifiScanIfWifiOff = ApplicationPreferences.applicationEventWifiScanIfWifiOff;
                if (applicationEventWifiScanIfWifiOff || (forceScan != FORCE_ONE_SCAN_DISABLED))
                {
                    //boolean wifiEventsExists = DatabaseHandler.getInstance(context).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false) > 0;
                    boolean scan = ((/*wifiEventsExists &&*/ applicationEventWifiScanIfWifiOff) ||
                            (forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                    if (scan)
                    {
                        WifiScanWorker.setWifiEnabledForScan(context, true);
                        WifiScanWorker.setScanRequest(context, true);
                        WifiScanWorker.lock(context);
                        final WifiManager _wifi = wifi;
                        //wifiChangeHandler.post(() -> {
                        Runnable runnable = () -> {
                            //if (PPApplication.logEnabled()) {
//                                    PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=WifiScanner.enableWifi");
                            //}

                            //if (Build.VERSION.SDK_INT >= 29)
                            //    CmdWifi.setWifi(true);
                            //else
                                _wifi.setWifiEnabled(true);

                        }; //);
                        PPApplication.createScannersExecutor();
                        PPApplication.scannersExecutor.submit(runnable);
                        return WifiManager.WIFI_STATE_ENABLING;
                    }
                }
            }
            else
            {
                // this is not needed, enableWifi() is called only from doScan and after when hotspot is disabled
                /*boolean isWifiAPEnabled = false;
                if (Build.VERSION.SDK_INT < 28) {
                    WifiApManager wifiApManager = null;
                    try {
                        wifiApManager = new WifiApManager(context);
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                    if (wifiApManager != null)
                        isWifiAPEnabled = wifiApManager.isWifiAPEnabled();
                }
                else
                    isWifiAPEnabled = CmdWifiAP.isEnabled();*/

                if (isScanAlwaysAvailable/*  && !isWifiAPEnabled*/) {
                    wifiState =  WifiManager.WIFI_STATE_ENABLED;
                }
                return wifiState;
            }
        }
        //}

        return wifiState;
    }

    private static void waitForWifiDisabled(WifiManager wifi) {
        long start = SystemClock.uptimeMillis();
        do {
            int wifiState = wifi.getWifiState();
            if (wifiState == WifiManager.WIFI_STATE_DISABLED)
                break;
            /*if (asyncTask != null)
            {
                if (asyncTask.isCancelled())
                    break;
            }*/

            GlobalUtils.sleep(200);
        } while (SystemClock.uptimeMillis() - start < 5 * 1000);
    }

    private static void waitForWifiScanEnd(/*Context context*//*, AsyncTask<Void, Integer, Void> asyncTask*/)
    {
        long start = SystemClock.uptimeMillis();
        do {
            if (!(ApplicationPreferences.prefEventWifiScanRequest ||
                    ApplicationPreferences.prefEventWifiWaitForResult)) {
                break;
            }
            /*if (asyncTask != null)
            {
                if (asyncTask.isCancelled())
                    break;
            }*/

            GlobalUtils.sleep(200);
        } while (SystemClock.uptimeMillis() - start < WIFI_SCAN_DURATION * 1000);
    }

    private static boolean isLocationEnabled(Context context/*, String scanType*/) {
        //if (Build.VERSION.SDK_INT >= 23) {
            // check for Location Settings

            /* isScanAlwaysAvailable() may be disabled for unknown reason :-(
            //boolean isScanAlwaysAvailable = true;
            if (WifiScanWorker.wifi == null)
                WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            int wifiState = WifiScanWorker.wifi.getWifiState();
            boolean isWifiEnabled = (wifiState == WifiManager.WIFI_STATE_ENABLED);
            isScanAlwaysAvailable = isWifiEnabled || WifiScanWorker.wifi.isScanAlwaysAvailable();
            */

            if (!GlobalUtils.isLocationEnabled(context)/* || (!isScanAlwaysAvailable)*/) {
                // Location settings are not properly set, show notification about it

                /*
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {

                    if (getShowEnableLocationNotification(context, scanType)) {
                        //Intent notificationIntent = new Intent(context, PhoneProfilesPrefsActivity.class);
                        Intent notificationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        String notificationText;
                        String notificationBigText;

                        notificationText = context.getString(R.string.phone_profiles_pref_category_wifi_scanning);
                        notificationBigText = context.getString(R.string.phone_profiles_pref_eventWiFiLocationSystemSettings_summary);

                        String nTitle = notificationText;
                        String nText = notificationBigText;
                        if (android.os.Build.VERSION.SDK_INT < 24) {
                            nTitle = context.getString(R.string.ppp_app_name);
                            nText = notificationText + ": " + notificationBigText;
                        }
                        PPApplication.createExclamationNotificationChannel(context);
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                                .setColor(ContextCompat.getColor(context, R.color.primary))
                                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                                .setContentTitle(nTitle) // title for notification
                                .setContentText(nText) // message for notification
                                .setAutoCancel(true); // clear notification after click
                        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(nText));

                        int requestCode;
                        //notificationIntent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "wifiScanningCategory");
                        requestCode = 1;
                        //notificationIntent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");

                        PendingIntent pi = PendingIntent.getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(pi);
                        mBuilder.setPriority(Notification.PRIORITY_MAX);
                        mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
                        mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (mNotificationManager != null) {
                            mNotificationManager.notify(LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_TAG,
                                                        PPApplication.LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_ID, mBuilder.build());
                        }

                        setShowEnableLocationNotification(context, false, scanType);
                    }
                }
                */

                return false;
            }
            else {
                /*NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(PPApplication.LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_ID);
                }
                setShowEnableLocationNotification(context, true, scanType);*/
                return true;
            }

        /*}
        else {
            //setShowEnableLocationNotification(context, true, scanType);
            return true;
        }*/
    }

}
