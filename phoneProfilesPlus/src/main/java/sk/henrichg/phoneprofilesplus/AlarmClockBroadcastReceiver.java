package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import java.util.Calendar;

public class AlarmClockBroadcastReceiver extends BroadcastReceiver {

    static final String EXTRA_ALARM_PACKAGE_NAME = "alarm_package_name";

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] AlarmClockBroadcastReceiver.onReceive", "xxx");
//        PPApplication.logE("[IN_BROADCAST_ALARM] AlarmClockBroadcastReceiver.onReceive", "xxx");

        if (!PPApplication.getApplicationStarted(true, true))
            return;

        Calendar now = Calendar.getInstance();
        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
        final long _time = now.getTimeInMillis() + gmtOffset;

        final String alarmPackageName = intent.getStringExtra(EXTRA_ALARM_PACKAGE_NAME);

        if (Event.getGlobalEventsRunning()) {
            final Context appContext = context.getApplicationContext();
            //PPApplication.startHandlerThreadBroadcast(/*"AlarmClockBroadcastReceiver.onReceive"*/);
            //final Handler __handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(context.getApplicationContext()) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
//                PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=AlarmClockBroadcastReceiver.onReceive");

                //Context appContext= appContextWeakRef.get();

                //if (appContext != null) {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":AlarmClockBroadcastReceiver_onReceive");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

//                        PPApplication.logE("[EVENTS_HANDLER_CALL] AlarmClockBroadcastReceiver.onReceive", "sensorType=SENSOR_TYPE_ALARM_CLOCK");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.setEventAlarmClockParameters(_time, alarmPackageName);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_ALARM_CLOCK);

                    } catch (Exception e) {
//                        PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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

/*

<receiver
            android:name=".AlarmClockBroadcastReceiver"
            android:enabled="false"
            android:exported="true"
            tools:ignore="ExportedReceiver">
            <intent-filter>

                <!--
                // Stock alarms
                // Nexus (?)
                -->
                <action android:name="com.android.deskclock.ALARM_ALERT"/>
                <!--
                <action android:name="com.android.deskclock.ALARM_DISMISS" />
                <action android:name="com.android.deskclock.ALARM_DONE" />
                <action android:name="com.android.deskclock.ALARM_SNOOZE" />
                -->
                <!-- // stock Android (?) -->
                <action android:name="com.android.alarmclock.ALARM_ALERT"/>
                <!--
                // Stock alarm Manufactures
                // Samsung
                -->
                <action android:name="com.samsung.sec.android.clockpackage.alarm.ALARM_ALERT"/>
                <!-- // HTC -->
                <action android:name="com.htc.android.worldclock.ALARM_ALERT"/>
                <action android:name="com.htc.android.ALARM_ALERT"/>
                <!-- // Sony -->
                <action android:name="com.sonyericsson.alarm.ALARM_ALERT"/>
                <!-- // ZTE -->
                <action android:name="zte.com.cn.alarmclock.ALARM_ALERT"/>
                <!-- // Motorola -->
                <action android:name="com.motorola.blur.alarmclock.ALARM_ALERT"/>
                <!-- // LG -->
                <action android:name="com.lge.clock.ALARM_ALERT"/>
                <!--
                // Third-party Alarms
                // Gentle Alarm
                -->
                <action android:name="com.mobitobi.android.gentlealarm.ALARM_INFO"/>
                <!-- // Sleep As Android -->
                <action android:name="com.urbandroid.sleep.alarmclock.ALARM_ALERT"/>
                <!-- // Alarmdroid (1.13.2) -->
                <action android:name="com.splunchy.android.alarmclock.ALARM_ALERT"/>
            </intent-filter>
        </receiver>

*/
