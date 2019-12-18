package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

class WifiBluetoothScanner {

    private final Context context;

    static final int wifiScanDuration = 25;      // 25 seconds for wifi scan
    static final int classicBTScanDuration = 25; // 25 seconds for classic bluetooth scan

    //static boolean wifiEnabledForScan;
    //static boolean bluetoothEnabledForScan;
    static List<BluetoothDeviceData> tmpBluetoothScanResults = null;
    static boolean bluetoothDiscoveryStarted = false;
    static BluetoothLeScanner bluetoothLEScanner = null;
    //static BluetoothLEScanCallback18 bluetoothLEScanCallback18 = null;
    //static BluetoothLEScanCallback21 bluetoothLEScanCallback21 = null;

    static final String SCANNER_TYPE_WIFI = "wifi";
    static final String SCANNER_TYPE_BLUETOOTH = "bluetooth";

    private static final String PREF_FORCE_ONE_BLUETOOTH_SCAN = "forceOneBluetoothScanInt";
    private static final String PREF_FORCE_ONE_LE_BLUETOOTH_SCAN = "forceOneLEBluetoothScanInt";
    private static final String PREF_FORCE_ONE_WIFI_SCAN = "forceOneWifiScanInt";

    static final int FORCE_ONE_SCAN_DISABLED = 0;
    static final int FORCE_ONE_SCAN_FROM_PREF_DIALOG = 3;

    //private static final String PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_WIFI = "show_enable_location_notification_wifi";
    //private static final String PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_BLUETOOTH = "show_enable_location_notification_bluetooth";

    public WifiBluetoothScanner(Context context) {
        this.context = context;
    }

    void doScan(String scannerType) {
        synchronized (PPApplication.wifiBluetoothscannerMutex) {
            CallsCounter.logCounter(context, "WifiBluetoothScanner.doScan", "Scanner_doScan");

            if (!PPApplication.getApplicationStarted(context, true))
                // application is not started
                return;

            PPApplication.logE("%%%% WifiBluetoothScanner.doScan", "-- START ------------");

            DataWrapper dataWrapper;

            PPApplication.logE("%%%% WifiBluetoothScanner.doScan", "scannerType=" + scannerType);

            // for Airplane mode ON, no scan
            //if (android.os.Build.VERSION.SDK_INT >= 17) {
                if (Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0) {
                    PPApplication.logE("%%%% WifiBluetoothScanner.doScan", "-- END - airplane mode ON -------");
                    return;
                }
            /*} else {
                if (Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0) {
                    PPApplication.logE("%%%% WifiBluetoothScanner.doScan", "-- END - airplane mode ON -------");
                    return;
                }
            }*/

            // check power save mode
            //boolean isPowerSaveMode = PPApplication.isPowerSaveMode;
            boolean isPowerSaveMode = DataWrapper.isPowerSaveMode(context);
            if (isPowerSaveMode) {
                int forceScan = getForceOneWifiScan(context);
                if (forceScan != FORCE_ONE_SCAN_FROM_PREF_DIALOG) {
                    if (scannerType.equals(SCANNER_TYPE_WIFI) &&
                            ApplicationPreferences.applicationEventWifiScanInPowerSaveMode(context).equals("2")) {
                        // not scan wi-fi in power save mode
                        PPApplication.logE("%%%% WifiBluetoothScanner.doScan", "-- END - power save mode ON -------");
                        return;
                    }
                    if (scannerType.equals(SCANNER_TYPE_BLUETOOTH) &&
                            ApplicationPreferences.applicationEventBluetoothScanInPowerSaveMode(context).equals("2")) {
                        // not scan bluetooth in power save mode
                        PPApplication.logE("%%%% WifiBluetoothScanner.doScan", "-- END - power save mode ON -------");
                        return;
                    }
                }
            }

            PPApplication.startHandlerThread("WifiBluetoothScanner.doScan.1");
            final Handler wifiBluetoothChangeHandler = new Handler(PPApplication.handlerThread.getLooper());

            PPApplication.logE("$$$ WifiBluetoothScanner.doScan", "before synchronized block - scannerType=" + scannerType);

            //synchronized (PPApplication.radioChangeStateMutex) {

                PPApplication.logE("$$$ WifiBluetoothScanner.doScan", "in synchronized block - start - scannerType=" + scannerType);

                if (scannerType.equals(SCANNER_TYPE_WIFI)) {
                    PPApplication.logE("$$$W WifiBluetoothScanner.doScan", "start wifi scan");

                    WifiScanWorker.fillWifiConfigurationList(context);

                    boolean canScan = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED;
                    if (canScan) {
                        if (Build.VERSION.SDK_INT < 28)
                            canScan = !WifiApManager.isWifiAPEnabled(context);
                        else
                            canScan = !CmdWifiAP.isEnabled();
                        PPApplication.logE("$$$W WifiBluetoothScanner.doScan", "canScan=" + canScan);
                        PPApplication.logE("$$$W WifiBluetoothScanner.doScan", "isWifiAPEnabled=" + !canScan);
                    }

                    if (canScan) {

                        dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

                        // check if wifi scan events exists
                        //lock();
                        boolean wifiEventsExists = DatabaseHandler.getInstance(context.getApplicationContext()).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false) > 0;
                        //unlock();
                        int forceScan = getForceOneWifiScan(context);
                        boolean scan = (wifiEventsExists || (forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                        if (scan) {
                            if (wifiEventsExists)
                                scan = isLocationEnabled(context/*, scannerType*/);
                        }
                        PPApplication.logE("$$$W WifiBluetoothScanner.doScan", "wifiEventsExists=" + wifiEventsExists);
                        PPApplication.logE("$$$W WifiBluetoothScanner.doScan", "forceScan=" + forceScan);
                        if (!scan) {
                            // wifi scan events not exists
                            PPApplication.logE("$$$W WifiBluetoothScanner.doScan", "alarms removed");
                            WifiScanWorker.cancelWork(context, true, null);
                        } else {
                            PPApplication.logE("$$$W WifiBluetoothScanner.doScan", "can scan");

                            if (WifiScanWorker.wifi == null)
                                WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                            if (WifiScanWorker.getWifiEnabledForScan(context)) {
                                // service restarted during scanning, disable wifi
                                PPApplication.logE("$$$W WifiBluetoothScanner.doScan", "disable wifi - service restarted");
                                wifiBluetoothChangeHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=WifiBluetoothScanner.doScan.1");
                                        //lock();
                                        PPApplication.logE("#### setWifiEnabled", "from WifiBluetoothScanner.doScan 1");
                                        if (Build.VERSION.SDK_INT >= 26)
                                            CmdWifi.setWifi(false);
                                        else
                                            WifiScanWorker.wifi.setWifiEnabled(false);
                                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiBluetoothScanner.doScan.1");
                                    }
                                });
                                //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                                //SystemClock.sleep(1000);
                                PPApplication.sleep(1000);
                                //unlock();
                            }

                            //noinspection ConstantConditions,ConstantIfStatement
                            if (true /*canScanWifi(dataWrapper)*/) { // scan even if wifi is connected

                                PPApplication.logE("$$$W WifiBluetoothScanner.doScan", "scan started");

                                WifiScanWorker.setScanRequest(context, false);
                                WifiScanWorker.setWaitForResults(context, false);
                                WifiScanWorker.setWifiEnabledForScan(context, false);

                                WifiScanWorker.unlock();

                                // start scan

                                //lock();

                                // enable wifi
                                int wifiState;
                                wifiState = enableWifi(dataWrapper, WifiScanWorker.wifi, wifiBluetoothChangeHandler);

                                if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                                    PPApplication.logE("$$$W WifiBluetoothScanner.doScan", "startScan");
                                    WifiScanWorker.startScan(context);
                                } else if (wifiState != WifiManager.WIFI_STATE_ENABLING) {
                                    WifiScanWorker.setScanRequest(context, false);
                                    WifiScanWorker.setWaitForResults(context, false);
                                    setForceOneWifiScan(context, FORCE_ONE_SCAN_DISABLED);
                                }

                                if (WifiScanWorker.getScanRequest(context) ||
                                        WifiScanWorker.getWaitForResults(context)) {
                                    PPApplication.logE("$$$W WifiBluetoothScanner.doScan", "waiting for scan end");

                                    // wait for scan end
                                    waitForWifiScanEnd(context);

                                    PPApplication.logE("$$$W WifiBluetoothScanner.doScan", "scan ended");

                                    if (WifiScanWorker.getWaitForResults(context)) {
                                        PPApplication.logE("$$$W WifiBluetoothScanner.doScan", "no data received from scanner");
                                        if (getForceOneWifiScan(context) != WifiBluetoothScanner.FORCE_ONE_SCAN_FROM_PREF_DIALOG) // not start service for force scan
                                        {
                                            Data workData = new Data.Builder()
                                                    .putString(PhoneProfilesService.EXTRA_DELAYED_WORK, DelayedWorksWorker.DELAYED_WORK_HANDLE_EVENTS)
                                                    .putString(PhoneProfilesService.EXTRA_SENSOR_TYPE, EventsHandler.SENSOR_TYPE_WIFI_SCANNER)
                                                    .build();

                                            OneTimeWorkRequest worker =
                                                    new OneTimeWorkRequest.Builder(DelayedWorksWorker.class)
                                                            .setInputData(workData)
                                                            .setInitialDelay(5, TimeUnit.SECONDS)
                                                            .build();
                                            try {
                                                WorkManager workManager = WorkManager.getInstance(context);
                                                workManager.enqueueUniqueWork("handleEventsWifiScannerFromScannerWork", ExistingWorkPolicy.REPLACE, worker);
                                            } catch (Exception ignored) {}

                                            /*PPApplication.startHandlerThread("WifiBluetoothScanner.doScan");
                                            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                                                    PowerManager.WakeLock wakeLock = null;
                                                    try {
                                                        if (powerManager != null) {
                                                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":WifiBluetoothScanner_doScan");
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

                        wifiBluetoothChangeHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                PPApplication.logE("PPApplication.startHandlerThread", "START run - from=WifiBluetoothScanner.doScan.1");

                                if (WifiScanWorker.getWifiEnabledForScan(context)) {
                                    PPApplication.logE("$$$W WifiBluetoothScanner.doScan", "disable wifi");
                                    //lock();
                                    PPApplication.logE("#### setWifiEnabled", "from WifiBluetoothScanner.doScan 2");
                                    if (Build.VERSION.SDK_INT >= 26)
                                        CmdWifi.setWifi(false);
                                    else
                                        WifiScanWorker.wifi.setWifiEnabled(false);
                                } else
                                    PPApplication.logE("$$$W WifiBluetoothScanner.doScan", "keep enabled wifi");

                                PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiBluetoothScanner.doScan.1");
                            }
                        });
                        //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                        //SystemClock.sleep(1000);
                        PPApplication.sleep(1000);
                        //unlock();
                    }

                    setForceOneWifiScan(context, FORCE_ONE_SCAN_DISABLED);
                    WifiScanWorker.setWaitForResults(context, false);
                    WifiScanWorker.setScanRequest(context, false);

                    WifiScanWorker.unlock();
                    //unlock();

                } else if (scannerType.equals(SCANNER_TYPE_BLUETOOTH)) {

                    if (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {

                        PPApplication.logE("$$$B WifiBluetoothScanner.doScan", "start bt scan");

                        dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);

                        // check if bluetooth scan events exists
                        //lock();
                        boolean bluetoothEventsExists = DatabaseHandler.getInstance(context.getApplicationContext()).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY, false) > 0;
                        int forceScan = getForceOneBluetoothScan(dataWrapper.context);
                        int forceScanLE = getForceOneLEBluetoothScan(context);
                        boolean classicDevicesScan = true; //DatabaseHandler.getInstance(context.getApplicationContext()).getBluetoothDevicesTypeCount(EventPreferencesBluetooth.DTYPE_CLASSIC, forceScanLE) > 0;
                        boolean leDevicesScan = bluetoothLESupported(context);
                        /*if (bluetoothLESupported(context))
                            leDevicesScan = DatabaseHandler.getInstance(context.getApplicationContext()).getBluetoothDevicesTypeCount(EventPreferencesBluetooth.DTYPE_LE, forceScanLE) > 0;
                        else
                            leDevicesScan = false;*/
                        //noinspection ConstantConditions
                        PPApplication.logE("$$$B WifiBluetoothScanner.doScan", "classicDevicesScan="+classicDevicesScan);
                        PPApplication.logE("$$$B WifiBluetoothScanner.doScan", "leDevicesScan="+leDevicesScan);

                        //unlock();
                        boolean scan = (bluetoothEventsExists ||
                                       (forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG) ||
                                       (forceScanLE == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                        if (scan) {
                            if (leDevicesScan)
                                leDevicesScan = isLocationEnabled(context/*, scannerType*/);
                        }
                        if (!scan) {
                            // bluetooth scan events not exists
                            PPApplication.logE("$$$B WifiBluetoothScanner.doScan", "no bt scan events");
                            BluetoothScanWorker.cancelWork(context, true, null);
                        } else {
                            PPApplication.logE("$$$B WifiBluetoothScanner.doScan", "scan=true");

                            if (BluetoothScanWorker.bluetooth == null)
                                BluetoothScanWorker.bluetooth = BluetoothAdapter.getDefaultAdapter(); //BluetoothScanWorker.getBluetoothAdapter(context);

                            if (BluetoothScanWorker.bluetooth != null) {
                                if (BluetoothScanWorker.getBluetoothEnabledForScan(context)) {
                                    // service restarted during scanning, disable Bluetooth
                                    PPApplication.logE("$$$B WifiBluetoothScanner.doScan", "disable BT - service restarted");
                                    wifiBluetoothChangeHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            PPApplication.logE("PPApplication.startHandlerThread", "START run - from=WifiBluetoothScanner.doScan.1");
                                            if (Permissions.checkBluetoothForEMUI(context)) {
                                                //lock();
                                                if (Build.VERSION.SDK_INT >= 26)
                                                    CmdBluetooth.setBluetooth(false);
                                                else
                                                    BluetoothScanWorker.bluetooth.disable();
                                            }
                                            PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiBluetoothScanner.doScan.1");
                                        }
                                    });
                                    //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                                    //SystemClock.sleep(1000);
                                    PPApplication.sleep(1000);
                                    //unlock();
                                }

                                //noinspection ConstantConditions,ConstantIfStatement
                                if (true /*canScanBluetooth(dataWrapper)*/) {  // scan even if bluetooth is connected
                                    BluetoothScanWorker.setScanRequest(context, false);
                                    BluetoothScanWorker.setLEScanRequest(context, false);
                                    BluetoothScanWorker.setWaitForResults(context, false);
                                    BluetoothScanWorker.setWaitForLEResults(context, false);
                                    BluetoothScanWorker.setBluetoothEnabledForScan(context, false);
                                    BluetoothScanWorker.setScanKilled(context, false);

                                    int bluetoothState;

                                    //noinspection ConstantConditions
                                    if (classicDevicesScan) {
                                        ///////// Classic BT scan

                                        PPApplication.logE("$$$BCL WifiBluetoothScanner.doScan", "classic devices scan");

                                        //lock();

                                        // enable bluetooth
                                        bluetoothState = enableBluetooth(dataWrapper,
                                                BluetoothScanWorker.bluetooth,
                                                wifiBluetoothChangeHandler,
                                                false);
                                        PPApplication.logE("$$$BCL WifiBluetoothScanner.doScan", "bluetoothState=" + bluetoothState);

                                        if (bluetoothState == BluetoothAdapter.STATE_ON) {
                                            PPApplication.logE("$$$BCL WifiBluetoothScanner.doScan", "start classic scan");
                                            BluetoothScanWorker.startCLScan(context);
                                        } else if (bluetoothState != BluetoothAdapter.STATE_TURNING_ON) {
                                            BluetoothScanWorker.setScanRequest(context, false);
                                            BluetoothScanWorker.setWaitForResults(context, false);
                                            setForceOneBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                                        }

                                        if ((BluetoothScanWorker.getScanRequest(context)) ||
                                                (BluetoothScanWorker.getWaitForResults(context))) {
                                            PPApplication.logE("$$$BCL WifiBluetoothScanner.doScan", "waiting for classic scan end");

                                            // wait for scan end
                                            waitForBluetoothCLScanEnd(context);

                                            PPApplication.logE("$$$BCL WifiBluetoothScanner.doScan", "classic scan ended");
                                        }

                                        //unlock();

                                        //setForceOneBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                                        //BluetoothScanWorker.setWaitForResults(context, false);
                                        //BluetoothScanWorker.setLEScanRequest(context, false);

                                        ///////// Classic BT scan - end
                                    }

                                    if (leDevicesScan && !BluetoothScanWorker.getScanKilled(context)) {
                                        ///////// LE BT scan

                                        PPApplication.logE("$$$BLE WifiBluetoothScanner.doScan", "LE devices scan");

                                    /*if (android.os.Build.VERSION.SDK_INT < 21)
                                        // for old BT LE scan must by acquired lock
                                        lock();*/

                                        // enable bluetooth
                                        bluetoothState = enableBluetooth(dataWrapper,
                                                BluetoothScanWorker.bluetooth,
                                                wifiBluetoothChangeHandler,
                                                true);

                                        if (bluetoothState == BluetoothAdapter.STATE_ON) {
                                            PPApplication.logE("$$$BLE WifiBluetoothScanner.doScan", "start LE scan");
                                            BluetoothScanWorker.startLEScan(context);
                                        } else if (bluetoothState != BluetoothAdapter.STATE_TURNING_ON) {
                                            BluetoothScanWorker.setLEScanRequest(context, false);
                                            BluetoothScanWorker.setWaitForLEResults(context, false);
                                            setForceOneLEBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                                        }

                                        if ((BluetoothScanWorker.getLEScanRequest(context)) ||
                                                (BluetoothScanWorker.getWaitForLEResults(context))) {
                                            PPApplication.logE("$$$BLE WifiBluetoothScanner.doScan", "waiting for LE scan end");

                                            // wait for scan end
                                            waitForLEBluetoothScanEnd(context);

                                            PPApplication.logE("$$$BLE WifiBluetoothScanner.doScan", "LE scan ended");

                                            // send broadcast for start EventsHandler
                                            /*Intent btLEIntent = new Intent(context, BluetoothLEScanBroadcastReceiver.class);
                                            sendBroadcast(btLEIntent);*/
                                            Intent btLEIntent = new Intent(PPApplication.PACKAGE_NAME + ".BluetoothLEScanBroadcastReceiver");
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(btLEIntent);
                                        }

                                        //unlock();

                                        //setForceOneLEBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                                        //BluetoothScanWorker.setWaitForLEResults(context, false);
                                        //BluetoothScanWorker.setLEScanRequest(context, false);

                                        ///////// LE BT scan - end
                                    }
                                }

                                wifiBluetoothChangeHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        PPApplication.logE("PPApplication.startHandlerThread", "START run - from=WifiBluetoothScanner.doScan.1");

                                        if (BluetoothScanWorker.getBluetoothEnabledForScan(context)) {
                                            PPApplication.logE("$$$B WifiBluetoothScanner.doScan", "disable bluetooth");
                                            if (Permissions.checkBluetoothForEMUI(context)) {
                                                //lock();
                                                if (Build.VERSION.SDK_INT >= 26)
                                                    CmdBluetooth.setBluetooth(false);
                                                else
                                                    BluetoothScanWorker.bluetooth.disable();
                                            }
                                        } else
                                            PPApplication.logE("$$$B WifiBluetoothScanner.doScan", "keep enabled bluetooth");

                                        PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiBluetoothScanner.doScan.1");
                                    }
                                });
                                //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                                //SystemClock.sleep(1000);
                                PPApplication.sleep(1000);
                                //unlock();
                            }
                        }
                    }

                    setForceOneBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                    setForceOneLEBluetoothScan(context, FORCE_ONE_SCAN_DISABLED);
                    BluetoothScanWorker.setWaitForResults(context, false);
                    BluetoothScanWorker.setWaitForLEResults(context, false);
                    BluetoothScanWorker.setScanRequest(context, false);
                    BluetoothScanWorker.setLEScanRequest(context, false);
                    BluetoothScanWorker.setScanKilled(context, false);

                    //unlock();
                }

                PPApplication.logE("$$$ WifiBluetoothScanner.doScan", "in synchronized block - end - scannerType=" + scannerType);

            //}

            PPApplication.logE("$$$ WifiBluetoothScanner.doScan", "after synchronized block - scannerType=" + scannerType);

            PPApplication.logE("%%%% WifiBluetoothScanner.doScan", "-- END ------------");
        }
    }

    static int getForceOneWifiScan(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_FORCE_ONE_WIFI_SCAN, FORCE_ONE_SCAN_DISABLED);
    }

    static void setForceOneWifiScan(Context context, int forceScan)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_FORCE_ONE_WIFI_SCAN, forceScan);
        editor.apply();
    }

    static int getForceOneBluetoothScan(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_FORCE_ONE_BLUETOOTH_SCAN, FORCE_ONE_SCAN_DISABLED);
    }

    static void setForceOneBluetoothScan(Context context, int forceScan)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_FORCE_ONE_BLUETOOTH_SCAN, forceScan);
        editor.apply();
    }

    static int getForceOneLEBluetoothScan(Context context)
    {
        ApplicationPreferences.getSharedPreferences(context);
        return ApplicationPreferences.preferences.getInt(PREF_FORCE_ONE_LE_BLUETOOTH_SCAN, FORCE_ONE_SCAN_DISABLED);
    }

    static void setForceOneLEBluetoothScan(Context context, int forceScan)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        editor.putInt(PREF_FORCE_ONE_LE_BLUETOOTH_SCAN, forceScan);
        editor.apply();
    }

    /*
    private void lock() {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (wakeLock == null)
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ScanWakeLock");
        try {
            if (!wakeLock.isHeld())
                wakeLock.acquire(10 * 60 * 1000);
            PPApplication.logE("$$$ WifiBluetoothScanner.lock","xxx");
        } catch(Exception e) {
            Log.e("WifiBluetoothScanner.lock", "Error getting Lock: ", e);
            PPApplication.logE("$$$ WifiBluetoothScanner.lock", "Error getting Lock: " + e.getMessage());
        }
    }

    private void unlock() {
        if ((wakeLock != null) && (wakeLock.isHeld())) {
            PPApplication.logE("$$$ WifiBluetoothScanner.unlock","xxx");
            wakeLock.release();
        }
    }
    */

    @SuppressLint("NewApi")
    private int enableWifi(DataWrapper dataWrapper, WifiManager wifi, Handler wifiBluetoothChangeHandler)
    {
        PPApplication.logE("@@@ WifiBluetoothScanner.enableWifi","xxx");

        int wifiState = wifi.getWifiState();
        int forceScan = getForceOneWifiScan(dataWrapper.context);

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
            PPApplication.logE("@@@ WifiBluetoothScanner.enableWifi","isScanAlwaysAvailable="+isScanAlwaysAvailable);
            isWifiEnabled = isWifiEnabled || isScanAlwaysAvailable;
            if (!isWifiEnabled)
            {
                boolean applicationEventWifiScanIfWifiOff = ApplicationPreferences.applicationEventWifiScanIfWifiOff(dataWrapper.context);
                if (applicationEventWifiScanIfWifiOff || (forceScan != FORCE_ONE_SCAN_DISABLED))
                {
                    boolean wifiEventsExists = DatabaseHandler.getInstance(dataWrapper.context).getTypeEventsCount(DatabaseHandler.ETYPE_WIFI_NEARBY, false) > 0;
                    boolean scan = ((wifiEventsExists && applicationEventWifiScanIfWifiOff) ||
                            (forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                    if (scan)
                    {
                        WifiScanWorker.setWifiEnabledForScan(context, true);
                        WifiScanWorker.setScanRequest(dataWrapper.context, true);
                        WifiScanWorker.lock(dataWrapper.context);
                        final WifiManager _wifi = wifi;
                        PPApplication.logE("[HANDLER] WifiBluetoothScanner.enableWifi", "before start handler");
                        wifiBluetoothChangeHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                PPApplication.logE("PPApplication.startHandlerThread", "START run - from=WifiBluetoothScanner.doScan.1");

                                PPApplication.logE("$$$ WifiBluetoothScanner.enableWifi", "before enable wifi");
                                PPApplication.logE("[HANDLER] WifiBluetoothScanner.enableWifi", "before enable wifi");
                                PPApplication.logE("#### setWifiEnabled", "from WifiBluetoothScanner.enableWifi");
                                if (Build.VERSION.SDK_INT >= 26)
                                    CmdWifi.setWifi(true);
                                else
                                    _wifi.setWifiEnabled(true);
                                PPApplication.logE("$$$ WifiBluetoothScanner.enableWifi", "after enable wifi");

                                PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiBluetoothScanner.doScan.1");
                            }
                        });
                        PPApplication.logE("@@@ WifiBluetoothScanner.enableWifi","set enabled");
                        return WifiManager.WIFI_STATE_ENABLING;
                    }
                }
            }
            else
            {
                boolean isWifiAPEnabled = false;
                if (Build.VERSION.SDK_INT < 28) {
                    WifiApManager wifiApManager = null;
                    try {
                        wifiApManager = new WifiApManager(context);
                    } catch (Exception ignored) {
                    }
                    if (wifiApManager != null)
                        isWifiAPEnabled = wifiApManager.isWifiAPEnabled();
                }
                else
                    isWifiAPEnabled = CmdWifiAP.isEnabled();

                if (isScanAlwaysAvailable  && !isWifiAPEnabled) {
                    PPApplication.logE("@@@ WifiBluetoothScanner.enableWifi", "scan always available");
                    wifiState =  WifiManager.WIFI_STATE_ENABLED;
                }
                return wifiState;
            }
        }
        //}

        return wifiState;
    }

    @SuppressLint("NewApi")
    private int enableBluetooth(DataWrapper dataWrapper,
                                BluetoothAdapter bluetooth,
                                Handler wifiBluetoothChangeHandler,
                                boolean forLE)
    {
        PPApplication.logE("$$$B WifiBluetoothScanner.enableBluetooth","xxx");

        int bluetoothState = bluetooth.getState();
        int forceScan;
        if (!forLE)
            forceScan = getForceOneBluetoothScan(dataWrapper.context);
        else
            forceScan = getForceOneLEBluetoothScan(dataWrapper.context);

        //if ((!dataWrapper.getIsManualProfileActivation()) || forceScan)
        //{
        boolean isBluetoothEnabled = bluetoothState == BluetoothAdapter.STATE_ON;
        if (!isBluetoothEnabled)
        {
            boolean applicationEventBluetoothScanIfBluetoothOff = ApplicationPreferences.applicationEventBluetoothScanIfBluetoothOff(dataWrapper.context);
            if (applicationEventBluetoothScanIfBluetoothOff || (forceScan != FORCE_ONE_SCAN_DISABLED))
            {
                boolean bluetoothEventsExists = DatabaseHandler.getInstance(dataWrapper.context).getTypeEventsCount(DatabaseHandler.ETYPE_BLUETOOTH_NEARBY, false) > 0;
                boolean scan = ((bluetoothEventsExists && applicationEventBluetoothScanIfBluetoothOff) ||
                        (forceScan == FORCE_ONE_SCAN_FROM_PREF_DIALOG));
                if (scan)
                {
                    PPApplication.logE("$$$B WifiBluetoothScanner.enableBluetooth","set enabled");
                    BluetoothScanWorker.setBluetoothEnabledForScan(context, true);
                    if (!forLE)
                        BluetoothScanWorker.setScanRequest(dataWrapper.context, true);
                    else
                        BluetoothScanWorker.setLEScanRequest(dataWrapper.context, true);
                    final BluetoothAdapter _bluetooth = bluetooth;
                    wifiBluetoothChangeHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            PPApplication.logE("PPApplication.startHandlerThread", "START run - from=WifiBluetoothScanner.doScan.1");

                            if (Permissions.checkBluetoothForEMUI(context)) {
                                //lock(); // lock is required for enabling bluetooth
                                if (Build.VERSION.SDK_INT >= 26)
                                    CmdBluetooth.setBluetooth(true);
                                else
                                    _bluetooth.enable();
                            }

                            PPApplication.logE("PPApplication.startHandlerThread", "END run - from=WifiBluetoothScanner.doScan.1");
                        }
                    });
                    return BluetoothAdapter.STATE_TURNING_ON;
                }
            }
        }
        else
        {
            PPApplication.logE("$$$B WifiBluetoothScanner.enableBluetooth","already enabled");
            return bluetoothState;
        }
        //}

        return bluetoothState;
    }

    private static void waitForWifiScanEnd(Context context/*, AsyncTask<Void, Integer, Void> asyncTask*/)
    {
        long start = SystemClock.uptimeMillis();
        do {
            if (!((WifiScanWorker.getScanRequest(context)) ||
                    (WifiScanWorker.getWaitForResults(context)))) {
                break;
            }
            /*if (asyncTask != null)
            {
                if (asyncTask.isCancelled())
                    break;
            }*/

            //try { Thread.sleep(100); } catch (InterruptedException e) { }
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < wifiScanDuration * 1000);
    }

    private static void waitForBluetoothCLScanEnd(Context context)
    {
        long start = SystemClock.uptimeMillis();
        do {
            if (!((BluetoothScanWorker.getScanRequest(context)) ||
                    (BluetoothScanWorker.getWaitForResults(context))))
                break;
            //try { Thread.sleep(100); } catch (InterruptedException e) { }
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < classicBTScanDuration * 1000);

        BluetoothScanWorker.finishCLScan(context);
        BluetoothScanWorker.stopCLScan();
    }

    private static void waitForLEBluetoothScanEnd(Context context)
    {
        if (bluetoothLESupported(context)) {
            int applicationEventBluetoothLEScanDuration = ApplicationPreferences.applicationEventBluetoothLEScanDuration(context);
            long start = SystemClock.uptimeMillis();
            do {
                if (!((BluetoothScanWorker.getLEScanRequest(context)) ||
                        (BluetoothScanWorker.getWaitForLEResults(context))))
                    break;

                //try { Thread.sleep(100); } catch (InterruptedException e) { }
                SystemClock.sleep(100);
            } while (SystemClock.uptimeMillis() - start < applicationEventBluetoothLEScanDuration * 1000);
            BluetoothScanWorker.finishLEScan(context);
            BluetoothScanWorker.stopLEScan(context);


            // wait for ScanCallback.onBatchScanResults after stop scan
            start = SystemClock.uptimeMillis();
            do {
                //try { Thread.sleep(100); } catch (InterruptedException e) { }
                SystemClock.sleep(100);
            } while (SystemClock.uptimeMillis() - start < 10 * 1000);
            // save ScanCallback.onBatchScanResults
            BluetoothScanWorker.finishLEScan(context);

        }
    }

    /*
    static void waitForForceOneBluetoothScanEnd(Context context, AsyncTask<Void, Integer, Void> asyncTask) {
        long start = SystemClock.uptimeMillis();
        do {
            if (getForceOneBluetoothScan(context) == FORCE_ONE_SCAN_DISABLED)
                break;
            if (asyncTask != null)
            {
                if (asyncTask.isCancelled())
                    break;
            }

            //try { Thread.sleep(100); } catch (InterruptedException e) { }
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < classicBTScanDuration * 1000);
        BluetoothScanWorker.finishCLScan(context);
        BluetoothScanWorker.stopCLScan(context);

        if (asyncTask != null)
        {
            if (asyncTask.isCancelled())
                return;
        }

        int applicationEventBluetoothLEScanDuration = ApplicationPreferences.applicationEventBluetoothLEScanDuration(context);
        start = SystemClock.uptimeMillis();
        do {
            if (getForceOneLEBluetoothScan(context) == FORCE_ONE_SCAN_DISABLED)
                break;
            if (asyncTask != null) {
                if (asyncTask.isCancelled())
                    break;
            }

            //try { Thread.sleep(100); } catch (InterruptedException e) { }
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < applicationEventBluetoothLEScanDuration * 1000);
        BluetoothScanWorker.finishLEScan(context);
        BluetoothScanWorker.stopLEScan(context);

        // wait for ScanCallback.onBatchScanResults after stop scan
        start = SystemClock.uptimeMillis();
        do {
            //try { Thread.sleep(100); } catch (InterruptedException e) { }
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() - start < 10 * 1000);
        // save ScanCallback.onBatchScanResults
        BluetoothScanWorker.finishLEScan(context);
    }
    */

    @SuppressLint("InlinedApi")
    static boolean bluetoothLESupported(Context context) {
        return (/*(android.os.Build.VERSION.SDK_INT >= 18) &&*/
                PPApplication.hasSystemFeature(context, PackageManager.FEATURE_BLUETOOTH_LE));
    }

    private static boolean isLocationEnabled(Context context/*, String scanType*/) {
        if (Build.VERSION.SDK_INT >= 23) {
            // check for Location Settings

            /* isScanAlwaysAvailable() may be disabled for unknown reason :-(
            //boolean isScanAlwaysAvailable = true;
            if (scanType.equals(SCANNER_TYPE_WIFI)) {
                if (WifiScanWorker.wifi == null)
                    WifiScanWorker.wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                int wifiState = WifiScanWorker.wifi.getWifiState();
                boolean isWifiEnabled = (wifiState == WifiManager.WIFI_STATE_ENABLED);
                isScanAlwaysAvailable = isWifiEnabled || WifiScanWorker.wifi.isScanAlwaysAvailable();
            }
            */

            //noinspection RedundantIfStatement
            if (!PhoneProfilesService.isLocationEnabled(context)/* || (!isScanAlwaysAvailable)*/) {
                // Location settings are not properly set, show notification about it

                /*
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, context)) {

                    if (getShowEnableLocationNotification(context, scanType)) {
                        //Intent notificationIntent = new Intent(context, PhoneProfilesPreferencesActivity.class);
                        Intent notificationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        String notificationText;
                        String notificationBigText;

                        if (scanType.equals(SCANNER_TYPE_WIFI)) {
                            notificationText = context.getString(R.string.phone_profiles_pref_category_wifi_scanning);
                            notificationBigText = context.getString(R.string.phone_profiles_pref_eventWiFiLocationSystemSettings_summary);
                        } else {
                            notificationText = context.getString(R.string.phone_profiles_pref_category_bluetooth_scanning);
                            notificationBigText = context.getString(R.string.phone_profiles_pref_eventBluetoothLocationSystemSettings_summary);
                        }

                        String nTitle = notificationText;
                        String nText = notificationBigText;
                        if (android.os.Build.VERSION.SDK_INT < 24) {
                            nTitle = context.getString(R.string.app_name);
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
                        if (scanType.equals(SCANNER_TYPE_WIFI)) {
                            //notificationIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "wifiScanningCategory");
                            requestCode = 1;
                        } else {
                            //notificationIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO, "bluetoothScanningCategory");
                            requestCode = 2;
                        }
                        //notificationIntent.putExtra(PhoneProfilesPreferencesActivity.EXTRA_SCROLL_TO_TYPE, "screen");

                        PendingIntent pi = PendingIntent.getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(pi);
                        mBuilder.setPriority(Notification.PRIORITY_MAX);
                        mBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
                        mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        if (mNotificationManager != null) {
                            if (scanType.equals(SCANNER_TYPE_WIFI))
                                mNotificationManager.notify(PPApplication.LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_ID, mBuilder.build());
                            else
                                mNotificationManager.notify(PPApplication.LOCATION_SETTINGS_FOR_BLUETOOTH_SCANNING_NOTIFICATION_ID, mBuilder.build());
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
                    if (scanType.equals(SCANNER_TYPE_WIFI))
                        notificationManager.cancel(PPApplication.LOCATION_SETTINGS_FOR_WIFI_SCANNING_NOTIFICATION_ID);
                    else
                        notificationManager.cancel(PPApplication.LOCATION_SETTINGS_FOR_BLUETOOTH_SCANNING_NOTIFICATION_ID);
                }
                setShowEnableLocationNotification(context, true, scanType);*/
                return true;
            }

        }
        else {
            //setShowEnableLocationNotification(context, true, scanType);
            return true;
        }
    }

    /*
    private static boolean getShowEnableLocationNotification(Context context, String type)
    {
        ApplicationPreferences.getSharedPreferences(context);
        switch (type) {
            case SCANNER_TYPE_WIFI:
                return ApplicationPreferences.preferences.getBoolean(PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_WIFI, true);
            case SCANNER_TYPE_BLUETOOTH:
                return ApplicationPreferences.preferences.getBoolean(PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_BLUETOOTH, true);
            default:
                return false;
        }
    }

    static void setShowEnableLocationNotification(Context context, boolean show, String type)
    {
        ApplicationPreferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
        switch (type) {
            case SCANNER_TYPE_WIFI:
                editor.putBoolean(PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_WIFI, show);
                break;
            case SCANNER_TYPE_BLUETOOTH:
                editor.putBoolean(PREF_SHOW_ENABLE_LOCATION_NOTIFICATION_BLUETOOTH, show);
                break;
        }
        editor.apply();
    }
    */

}
