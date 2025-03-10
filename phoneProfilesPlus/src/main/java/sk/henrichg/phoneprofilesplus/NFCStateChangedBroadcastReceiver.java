package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;

public class NFCStateChangedBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] NFCStateChangedBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(true, true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            final String action = intent.getAction();

            if ((action != null) && action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                final int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF);

                if ((state == NfcAdapter.STATE_ON) || (state == NfcAdapter.STATE_OFF)) {
                    final Context appContext = context.getApplicationContext();
                    PPExecutors.handleEvents(appContext, EventsHandler.SENSOR_TYPE_RADIO_SWITCH, "SENSOR_TYPE_RADIO_SWITCH", 0);
                    /*
                    PPApplication.startHandlerThreadBroadcast();
                    final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                    //__handler.post(new PPApplication.PPHandlerThreadRunnable(
                    //        context.getApplicationContext()) {
                    __handler.post(() -> {
//                            PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=NFCStateChangedBroadcastReceiver.onReceive");

                        //Context appContext= appContextWeakRef.get();
                        //if (appContext != null) {
                            PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                            PowerManager.WakeLock wakeLock = null;
                            try {
                                if (powerManager != null) {
                                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":NFCStateChangedBroadcastReceiver_onReceive");
                                    wakeLock.acquire(10 * 60 * 1000);
                                }

//                                PPApplication.logE("[EVENTS_HANDLER_CALL] NFCStateChangedBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_RADIO_SWITCH");
                                EventsHandler eventsHandler = new EventsHandler(appContext);
                                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RADIO_SWITCH);

                            } catch (Exception e) {
//                                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
                    });
                    */
                }
            }
        }
    }
}
