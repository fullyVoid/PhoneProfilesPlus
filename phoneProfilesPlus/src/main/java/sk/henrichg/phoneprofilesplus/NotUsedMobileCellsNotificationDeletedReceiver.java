package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.List;

// Delete button (X) or "clear all" in notification
public class NotUsedMobileCellsNotificationDeletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] NotUsedMobileCellsNotificationDeletedReceiver.onReceive", "xxx");

        if (intent != null) {
            final int mobileCellId = intent.getIntExtra(NotUsedMobileCellsDetectedActivity.EXTRA_MOBILE_CELL_ID, 0);
            if (mobileCellId != 0) {
                final Context appContext = context.getApplicationContext();
                //PPApplication.startHandlerThreadBroadcast(/*"NotUsedMobileCellsNotificationDeletedReceiver.onReceive"*/);
                //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                //__handler.post(new PPApplication.PPHandlerThreadRunnable(context.getApplicationContext()) {
                //__handler.post(() -> {
                Runnable runnable = () -> {
//                        PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=NotUsedMobileCellsNotificationDeletedReceiver.onReceive");

                    //Context appContext= appContextWeakRef.get();

                    //if (appContext != null) {
                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = null;
                        try {
                            if (powerManager != null) {
                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":NotUsedMobileCellsNotificationDeletedReceiver_onReceive");
                                wakeLock.acquire(10 * 60 * 1000);
                            }

                            DatabaseHandler db = DatabaseHandler.getInstance(appContext);

                            List<MobileCellsData> localCellsList = new ArrayList<>();
                            db.addMobileCellsToList(localCellsList, mobileCellId);
                            if (!localCellsList.isEmpty()) {
                                MobileCellsData cell = localCellsList.get(0);
                                db.deleteMobileCell(cell.cellId);
                            }

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
                PPApplication.createEventsHandlerExecutor();
                PPApplication.eventsHandlerExecutor.submit(runnable);
            }
        }

    }

}
