package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class ActionForExternalApplicationActivity extends AppCompatActivity {

    private DataWrapper dataWrapper;

    private String action;

    private String profileName = "";
    private String eventName = "";
    private long profile_id = 0;
    private long event_id = 0;

    // !!! do NOT change this actions, these are for Tasker !!!!
    static final String ACTION_ACTIVATE_PROFILE = PPApplication.PACKAGE_NAME + ".ACTION_ACTIVATE_PROFILE";
    private static final String ACTION_RESTART_EVENTS = PPApplication.PACKAGE_NAME + ".ACTION_RESTART_EVENTS";
    private static final String ACTION_ENABLE_RUN_FOR_EVENT = PPApplication.PACKAGE_NAME + ".ACTION_ENABLE_RUN_FOR_EVENT";
    private static final String ACTION_PAUSE_EVENT = PPApplication.PACKAGE_NAME + ".ACTION_PAUSE_EVENT";
    private static final String ACTION_STOP_EVENT = PPApplication.PACKAGE_NAME + ".ACTION_STOP_EVENT";

    static final String EXTRA_EVENT_NAME = "event_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        Intent intent = getIntent();

        action = intent.getAction();

        dataWrapper = new DataWrapper(getApplicationContext(), false, 0, false, 0, 0, 0f);

        if (action != null) {
            if (action.equals(ACTION_ACTIVATE_PROFILE)) {
                profileName = intent.getStringExtra(ActivateProfileFromExternalApplicationActivity.EXTRA_PROFILE_NAME);
                if (profileName != null) {
                    profileName = profileName.trim();

                    if (!profileName.isEmpty()) {
                        //dataWrapper.fillProfileList(false, false);
                        profile_id = dataWrapper.getProfileIdByName(profileName, true);
                        /*for (Profile profile : this.dataWrapper.profileList) {
                            if (profile._name.trim().equals(profileName)) {
                                profile_id = profile._id;
                                break;
                            }
                        }*/
                    }
                }
            } else if (!action.equals(ACTION_RESTART_EVENTS)) {
                eventName = intent.getStringExtra(ActionForExternalApplicationActivity.EXTRA_EVENT_NAME);
                if (eventName != null) {
                    eventName = eventName.trim();

                    if (!eventName.isEmpty()) {
                        event_id = dataWrapper.getEventIdByName(eventName, true);
                        /*dataWrapper.fillEventList();
                        for (Event event : dataWrapper.eventList) {
                            if (event._name.trim().equals(eventName)) {
                                event_id = event._id;
                                break;
                            }
                        }*/
                    }
                }
            }
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (action != null) {
            boolean serviceStarted = GlobalUtils.isServiceRunning(getApplicationContext(), PhoneProfilesService.class, false);
            if (!serviceStarted) {
                AutostartPermissionNotification.showNotification(getApplicationContext(), true);

                PPApplication.setApplicationStarted(getApplicationContext(), true);
                Intent serviceIntent = new Intent(getApplicationContext(), PhoneProfilesService.class);
                //serviceIntent.putExtra(PhoneProfilesService.EXTRA_DEACTIVATE_PROFILE, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_ACTIVATE_PROFILES, false);
                serviceIntent.putExtra(PPApplication.EXTRA_APPLICATION_START, true);
                serviceIntent.putExtra(PPApplication.EXTRA_DEVICE_BOOT, false);
                serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_PACKAGE_REPLACE, false);
                boolean extraDataOk;
                if (action.equals(ACTION_ACTIVATE_PROFILE)) {
                    extraDataOk = profile_id != 0;
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_TYPE,
                            PhoneProfilesService.START_FOR_EXTERNAL_APP_PROFILE);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_VALUE, profileName);
                }
                else
                if (!action.equals(ACTION_RESTART_EVENTS)) {
                    extraDataOk = event_id != 0;
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_TYPE,
                            PhoneProfilesService.START_FOR_EXTERNAL_APP_EVENT);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_DATA_VALUE, eventName);
                }
                else
                    extraDataOk = true;
                if (extraDataOk) {
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APPLICATION, true);
                    serviceIntent.putExtra(PhoneProfilesService.EXTRA_START_FOR_EXTERNAL_APP_ACTION, action);
                }
//                PPApplication.logE("[START_PP_SERVICE] ActionForExternalApplicationActivity.onStart", "xxx");
                PPApplication.startPPService(this, serviceIntent);
                finish();
                return;
            }

            switch (action) {
                case ACTION_ACTIVATE_PROFILE:
                    PPApplication.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_PROFILE_ACTIVATION,
                            null, profileName, "");

                    if (profile_id != 0) {
                        Profile profile = dataWrapper.getProfileById(profile_id, false, false, false);
                        if (profile != null) {
                            //if (Permissions.grantProfilePermissions(getApplicationContext(), profile, false, true,
                            //        /*false, false, 0,*/ PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, true, false)) {
                            if (!DataWrapperStatic.displayPreferencesErrorNotification(profile, null, true, getApplicationContext())) {
                                dataWrapper.activateProfileFromMainThread(profile, false, PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this, false);
                            } else
                                dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                        }
                        else
                            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    } else {
                        showNotification(getString(R.string.action_for_external_application_notification_title),
                                getString(R.string.action_for_external_application_notification_no_profile_text));

                        dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    }
                    break;
                case ACTION_RESTART_EVENTS:
                    PPApplication.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_RESTART_EVENTS,
                            null, null, "");

                    dataWrapper.restartEventsWithRescan(true, true, true, false, true, true);
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    break;
                case ACTION_ENABLE_RUN_FOR_EVENT:
                    PPApplication.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_ENABLE_RUN_FOR_EVENT,
                            eventName, null, "");

                    if (event_id != 0) {
                        final Event event = dataWrapper.getEventById(event_id);
                        if (event != null) {
                            if (event.getStatus() != Event.ESTATUS_RUNNING) {
                                final Context appContext = getApplicationContext();
                                //PPApplication.startHandlerThread(/*"ActionForExternalApplicationActivity.onStart.1"*/);
                                //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
                                //__handler.post(new PPHandlerThreadRunnable(
                                //        getApplicationContext(), dataWrapper, event) {
                                //__handler.post(() -> {
                                Runnable runnable = () -> {
//                                        PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=ActionForExternalApplicationActivity.onStart.1");

                                    //Context appContext= appContextWeakRef.get();
                                    //DataWrapper dataWrapper = dataWrapperWeakRef.get();
                                    //Event event = eventWeakRef.get();

                                    //if ((appContext != null) && (dataWrapper != null) && (event != null)) {
                                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                        PowerManager.WakeLock wakeLock = null;
                                        try {
                                            if (powerManager != null) {
                                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActionForExternalApplicationActivity_ACTION_ENABLE_RUN_FOR_EVENT");
                                                wakeLock.acquire(10 * 60 * 1000);
                                            }

                                            synchronized (PPApplication.eventsHandlerMutex) {
                                                event.pauseEvent(dataWrapper, true, false,
                                                        false, true, null, false, false, true);
                                            }
                                            //_dataWrapper.restartEvents(false, true, true, true, false);
                                          dataWrapper.restartEventsWithRescan(true, false, false, false, true, true);

                                        } catch (Exception e) {
//                                            PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
                        }
                        else
                            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    } else {
                        showNotification(getString(R.string.action_for_external_application_notification_title),
                                getString(R.string.action_for_external_application_notification_no_event_text));

                    }
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    break;
                case ACTION_PAUSE_EVENT:
                    PPApplication.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_PAUSE_EVENT,
                            eventName, null, "");

                    if (event_id != 0) {
                        final Event event = dataWrapper.getEventById(event_id);
                        if (event != null) {
                            if (event.getStatus() == Event.ESTATUS_RUNNING) {
                                final Context appContext = getApplicationContext();
                                //PPApplication.startHandlerThread(/*"ActionForExternalApplicationActivity.onStart.11"*/);
                                //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
                                //__handler.post(new PPHandlerThreadRunnable(
                                //        getApplicationContext(), dataWrapper, event) {
                                //__handler.post(() -> {
                                Runnable runnable = () -> {
//                                        PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=ActionForExternalApplicationActivity.onStart.11");

                                    //Context appContext= appContextWeakRef.get();
                                    //DataWrapper dataWrapper = dataWrapperWeakRef.get();
                                    //Event event = eventWeakRef.get();

                                    //if ((appContext != null) && (dataWrapper != null) && (event != null)) {
                                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                        PowerManager.WakeLock wakeLock = null;
                                        try {
                                            if (powerManager != null) {
                                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActionForExternalApplicationActivity_ACTION_PAUSE_EVENT");
                                                wakeLock.acquire(10 * 60 * 1000);
                                            }

                                            synchronized (PPApplication.eventsHandlerMutex) {
                                                event.pauseEvent(dataWrapper, true, false,
                                                        false, true, null, true, false, true);
                                            }

                                        } catch (Exception e) {
//                                            PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
                        }
                        else
                            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    } else {
                        showNotification(getString(R.string.action_for_external_application_notification_title),
                                getString(R.string.action_for_external_application_notification_no_event_text));

                    }
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    break;
                case ACTION_STOP_EVENT:
                    PPApplication.addActivityLog(getApplicationContext(), PPApplication.ALTYPE_ACTION_FROM_EXTERNAL_APP_STOP_EVENT,
                            eventName, null, "");

                    if (event_id != 0) {
                        final Event event = dataWrapper.getEventById(event_id);
                        if (event != null) {
                            if (event.getStatus() != Event.ESTATUS_STOP) {
                                final Context appContext = getApplicationContext();
                                //PPApplication.startHandlerThread(/*"ActionForExternalApplicationActivity.onStart.2"*/);
                                //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
                                //__handler.post(new PPHandlerThreadRunnable(
                                //        getApplicationContext(), dataWrapper, event) {
                                //__handler.post(() -> {
                                Runnable runnable = () -> {
//                                        PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=ActionForExternalApplicationActivity.onStart.2");

                                    //Context appContext= appContextWeakRef.get();
                                    //DataWrapper dataWrapper = dataWrapperWeakRef.get();
                                    //Event event = eventWeakRef.get();

                                    //if ((appContext != null) && (dataWrapper != null) && (event != null)) {
                                        PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                                        PowerManager.WakeLock wakeLock = null;
                                        try {
                                            if (powerManager != null) {
                                                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":ActionForExternalApplicationActivity_ACTION_STOP_EVENT");
                                                wakeLock.acquire(10 * 60 * 1000);
                                            }

                                            synchronized (PPApplication.eventsHandlerMutex) {
                                                event.stopEvent(dataWrapper, true, false,
                                                        true, true, true); // activate return profile
                                            }
                                            //_dataWrapper.restartEvents(false, true, true, true, false);
                                            dataWrapper.restartEventsWithRescan(true, false, false, false, true, true);

                                        } catch (Exception e) {
//                                            PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", Log.getStackTraceString(e));
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
                        }
                        else
                            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    } else {
                        showNotification(getString(R.string.action_for_external_application_notification_title),
                                getString(R.string.action_for_external_application_notification_no_event_text));

                    }
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    break;
                default:
                    showNotification(getString(R.string.action_for_external_application_notification_title),
                            getString(R.string.action_for_external_application_notification_bad_action));
                    dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
                    break;
            }
        }
        else {
            showNotification(getString(R.string.action_for_external_application_notification_title),
                    getString(R.string.action_for_external_application_notification_no_action));
            dataWrapper.finishActivity(PPApplication.STARTUP_SOURCE_EXTERNAL_APP, false, this);
        }
    }

    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_PROFILE) {
            if (data != null) {
                long profileId = data.getLongExtra(PPApplication.EXTRA_PROFILE_ID, 0);
                int startupSource = data.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, 0);
                boolean mergedProfile = data.getBooleanExtra(Permissions.EXTRA_MERGED_PROFILE, false);
                boolean activateProfile = data.getBooleanExtra(Permissions.EXTRA_ACTIVATE_PROFILE, false);

                if (activateProfile) {
                    Profile profile = dataWrapper.getProfileById(profileId, false, false, mergedProfile);
                    dataWrapper.activateProfileFromMainThread(profile, mergedProfile, startupSource, this);
                }
            }
        }
    }
    */

    private void showNotification(String title, String text) {
        PPApplication.createExclamationNotificationChannel(getApplicationContext());
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(getApplicationContext(), PPApplication.EXCLAMATION_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(this, R.color.notificationDecorationColor))
                .setSmallIcon(R.drawable.ic_exclamation_notify) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(text) // message for notification
                .setAutoCancel(true); // clear notification after click
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        /*Intent intent = new Intent(context, ImportantInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);*/
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
            mBuilder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        //}

        mBuilder.setGroup(PPApplication.ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_GROUP);

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
        try {
            mNotificationManager.notify(
                    PPApplication.ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_TAG,
                    PPApplication.ACTION_FOR_EXTERNAL_APPLICATION_NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
            //Log.e("ActionForExternalApplicationActivity.showNotification", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

/*    private static abstract class PPHandlerThreadRunnable implements Runnable {

        final WeakReference<Context> appContextWeakRef;
        final WeakReference<DataWrapper> dataWrapperWeakRef;
        final WeakReference<Event> eventWeakRef;

        PPHandlerThreadRunnable(Context appContext,
                                       DataWrapper dataWrapper,
                                       Event event) {
            this.appContextWeakRef = new WeakReference<>(appContext);
            this.dataWrapperWeakRef = new WeakReference<>(dataWrapper);
            this.eventWeakRef = new WeakReference<>(event);
        }

    }*/

}
