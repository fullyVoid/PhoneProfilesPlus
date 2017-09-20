package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import java.util.List;

public class EventsService extends WakefulIntentService {
    private Context context;
    private DataWrapper dataWrapper;
    private String broadcastReceiverType;

    //public static boolean restartAtEndOfEvent = false;

    private int callEventType;
    private static int oldRingerMode;
    private static int oldSystemRingerMode;
    private static int oldZenMode;
    private static String oldRingtone;
    //private static String oldNotificationTone;
    private static int oldSystemRingerVolume;

    static final String EXTRA_BROADCAST_RECEIVER_TYPE = "broadcast_receiver_type";
    static final String EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED = "event_notification_posted_removed";
    static final String EXTRA_EVENT_SMS_PHONE_NUMBER = "event_sms_phone_number";
    static final String EXTRA_EVENT_SMS_DATE = "event_sms_date";
    static final String EXTRA_SIMULATE_RINGING_CALL = "simulate_ringing_call";
    static final String EXTRA_OLD_RINGER_MODE = "old_ringer_mode";
    static final String EXTRA_OLD_SYSTEM_RINGER_MODE = "old_system_ringer_mode";
    static final String EXTRA_OLD_ZEN_MODE = "old_zen_mode";
    static final String EXTRA_OLD_RINGTONE = "old_ringtone";
    static final String EXTRA_EVENT_NFC_DATE = "event_nfc_date";
    static final String EXTRA_EVENT_NFC_TAG_NAME = "event_nfc_tag_name";
    static final String EXTRA_EVENT_RADIO_SWITCH_TYPE = "event_radio_switch_type";
    static final String EXTRA_EVENT_RADIO_SWITCH_STATE = "event_radio_switch_state";
    //static final String EXTRA_SIMULATE_NOTIFICATION_TONE = "simulate_notification_tone";
    //static final String EXTRA_OLD_NOTIFICATION_TONE = "old_notification_tone";
    static final String EXTRA_OLD_SYSTEM_RINGER_VOLUME = "old_system_ringer_volume";

    static final String SENSOR_TYPE_RADIO_SWITCH = "radioSwitch";
    static final String SENSOR_TYPE_RESTART_EVENTS = "restartEvents";
    static final String SENSOR_TYPE_START_EVENTS_SERVICE = "startEventsService";
    static final String SENSOR_TYPE_PHONE_CALL = "phoneCall";
    static final String SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED = "calendarProviderChanged";
    static final String SENSOR_TYPE_SEARCH_CALENDAR_EVENTS = "searchCalendarEvents";
    static final String SENSOR_TYPE_SMS = "sms";
    static final String SENSOR_TYPE_NOTIFICATION = "notification";
    static final String SENSOR_TYPE_NFC_TAG = "nfcTag";
    static final String SENSOR_TYPE_EVENT_DELAY_START = "eventDelayStart";
    static final String SENSOR_TYPE_EVENT_DELAY_END = "eventDelayEnd";
    static final String SENSOR_TYPE_BATTERY = "battery";
    static final String SENSOR_TYPE_BLUETOOTH_CONNECTION = "bluetoothConnection";
    static final String SENSOR_TYPE_BLUETOOTH_STATE = "bluetoothState";
    static final String SENSOR_TYPE_DOCK_CONNECTION = "dockConnection";
    static final String SENSOR_TYPE_CALENDAR = "calendar";
    static final String SENSOR_TYPE_TIME = "time";
    static final String SENSOR_TYPE_APPLICATION = "application";
    static final String SENSOR_TYPE_HEADSET_CONNECTION = "headsetConnection";
    static final String SENSOR_TYPE_NOTIFICATION_EVENT_END = "notificationEventEnd";
    static final String SENSOR_TYPE_SMS_EVENT_END = "smsEventEnd";
    static final String SENSOR_TYPE_WIFI_CONNECTION = "wifiConnection";
    static final String SENSOR_TYPE_WIFI_STATE = "wifiState";
    static final String SENSOR_TYPE_POWER_SAVE_MODE = "powerSaveMode";
    static final String SENSOR_TYPE_GEOFENCES_SCANNER = "geofenceScanner";
    static final String SENSOR_TYPE_LOCATION_MODE = "locationMode";
    static final String SENSOR_TYPE_DEVICE_ORIENTATION = "deviceOrientation";
    static final String SENSOR_TYPE_PHONE_STATE = "phoneState";
    static final String SENSOR_TYPE_NFC_EVENT_END = "nfcEventEnd";
    static final String SENSOR_TYPE_WIFI_SCANNER = "wifiScanner";
    static final String SENSOR_TYPE_BLUETOOTH_SCANNER = "bluetoothScanner";
    static final String SENSOR_TYPE_SCREEN = "screen";
    static final String SENSOR_TYPE_DEVICE_IDLE_MODE = "deviceIdleMode";

    //public static ArrayList<Profile> mergedProfiles = null;
    //public static Profile oldActivatedProfile = null;

    //public static final String BROADCAST_RECEIVER_TYPE_NO_BROADCAST_RECEIVER = "noBroadcastReceiver";

    public EventsService() {
        super("EventsService");

        // if enabled is true, onStartCommand(Intent, int, int) will return START_REDELIVER_INTENT,
        // so if this process dies before onHandleIntent(Intent) returns, the process will be restarted
        // and the intent redelivered. If multiple Intents have been sent, only the most recent one
        // is guaranteed to be redelivered.
        // -- but restarted service has intent == null??
        setIntentRedelivery(true);
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        CallsCounter.logCounter(getApplicationContext(), "EventsService.doWakefulWork", "EventsService_doWakefulWork");

        if (intent == null) {
            PPApplication.logE("#### EventsService.onHandleIntent", "intent=null");
            return;
        }

        context = getApplicationContext();

        if (!PPApplication.getApplicationStarted(context, true))
            // application is not started
            return;

        PPApplication.logE("#### EventsService.onHandleIntent", "-- start --------------------------------");

        broadcastReceiverType = intent.getStringExtra(EXTRA_BROADCAST_RECEIVER_TYPE);
        PPApplication.logE("#### EventsService.onHandleIntent", "broadcastReceiverType=" + broadcastReceiverType);

        //restartAtEndOfEvent = false;

        // disabled for firstStartEvents
        //if (!PPApplication.getApplicationStarted(context))
        // application is not started
        //	return;

        //PPApplication.setApplicationStarted(context, true);

        dataWrapper = new DataWrapper(context, true, false, 0);

        ApplicationPreferences.getSharedPreferences(context);
        callEventType = ApplicationPreferences.preferences.getInt(PhoneCallService.PREF_EVENT_CALL_EVENT_TYPE, PhoneCallService.CALL_EVENT_UNDEFINED);

        oldRingerMode = ActivateProfileHelper.getRingerMode(context);
        oldZenMode = ActivateProfileHelper.getZenMode(context);
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        oldSystemRingerMode = audioManager.getRingerMode();
        oldSystemRingerVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);

        try {
            Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
            if (uri != null)
                oldRingtone = uri.toString();
            else
                oldRingtone = "";
        } catch (SecurityException e) {
            Permissions.grantPlayRingtoneNotificationPermissions(context, true, false);
            oldRingtone = "";
        } catch (Exception e) {
            oldRingtone = "";
        }

        /*
        try {
            Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION);
            if (uri != null)
                oldNotificationTone = uri.toString();
            else
                oldNotificationTone = "";
        } catch (SecurityException e) {
            Permissions.grantPlayRingtoneNotificationPermissions(context, true, false);
            oldNotificationTone = "";
        } catch (Exception e) {
            oldNotificationTone = "";
        }
        */

        if (PhoneProfilesService.instance != null) {
            // start of GeofenceScanner
            if (!PhoneProfilesService.isGeofenceScannerStarted())
                PPApplication.startGeofenceScanner(context);
            // start of CellTowerScanner
            if (!PhoneProfilesService.isPhoneStateStarted()) {
                PPApplication.logE("EventsService.onHandleIntent", "startPhoneStateScanner");
                //PPApplication.sendMessageToService(this, PhoneProfilesService.MSG_START_PHONE_STATE_SCANNER);
                PPApplication.startPhoneStateScanner(context);
            }
        }

        if (!Event.getGlobalEventsRunning(context)) {
            // events are globally stopped

            doEndService(/*intent*/);
            dataWrapper.invalidateDataWrapper();

            return;
        }

        // start orientation listener only when events exists
        if (PhoneProfilesService.instance != null) {
            if (!PhoneProfilesService.isOrientationScannerStarted()) {
                if (dataWrapper.getDatabaseHandler().getTypeEventsCount(DatabaseHandler.ETYPE_ORIENTATION) > 0)
                    PPApplication.startOrientationScanner(context);
            }
        }

        if (!eventsExists(broadcastReceiverType)) {
            // events not exists

            doEndService(/*intent*/);
            dataWrapper.invalidateDataWrapper();

            PPApplication.logE("@@@ EventsService.onHandleIntent", "-- end: not events found --------------------------------");

            return;
        }

        dataWrapper.getActivateProfileHelper().initialize(dataWrapper, context);

        PPApplication.logE("%%%% EventsService.onHandleIntent","broadcastReceiverType="+broadcastReceiverType);

        List<Event> eventList = dataWrapper.getEventList();

        boolean isRestart = broadcastReceiverType.equals(EventsService.SENSOR_TYPE_RESTART_EVENTS);

        boolean interactive = (!isRestart) || intent.getBooleanExtra(DataWrapper.EXTRA_INTERACTIVE, false);

        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED) ||
                broadcastReceiverType.equals(EventsService.SENSOR_TYPE_SEARCH_CALENDAR_EVENTS)) {
            // search for calendar events
            PPApplication.logE("[CALENDAR] EventsService.onHandleIntent", "search for calendar events");
            for (Event _event : eventList) {
                if ((_event._eventPreferencesCalendar._enabled) && (_event.getStatus() != Event.ESTATUS_STOP)) {
                    PPApplication.logE("[CALENDAR] EventsService.onHandleIntent", "event._id=" + _event._id);
                    _event._eventPreferencesCalendar.saveStartEndTime(dataWrapper);
                }
            }
        }

        // "push events"
        if (isRestart) {
            // for restart events, set startTime to 0
            for (Event _event : eventList) {
                _event._eventPreferencesSMS._startTime = 0;
                dataWrapper.getDatabaseHandler().updateSMSStartTime(_event);
                _event._eventPreferencesNotification._startTime = 0;
                dataWrapper.getDatabaseHandler().updateNotificationStartTime(_event);
                _event._eventPreferencesNFC._startTime = 0;
                dataWrapper.getDatabaseHandler().updateNFCStartTime(_event);
            }
        }
        else {
            // for no-restart events, stet startTime to actual time
            if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_SMS)) {
                // search for sms events, save start time
                PPApplication.logE("EventsService.onHandleIntent", "search for sms events");
                for (Event _event : eventList) {
                    if (_event.getStatus() != Event.ESTATUS_STOP) {
                        if (_event._eventPreferencesSMS._enabled) {
                            PPApplication.logE("EventsService.onHandleIntent", "event._id=" + _event._id);
                            _event._eventPreferencesSMS.saveStartTime(dataWrapper,
                                    intent.getStringExtra(EXTRA_EVENT_SMS_PHONE_NUMBER),
                                    intent.getLongExtra(EXTRA_EVENT_SMS_DATE, 0));
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_NOTIFICATION)) {
                    // search for notification events, save start time
                    PPApplication.logE("EventsService.onHandleIntent", "search for notification events");
                    for (Event _event : eventList) {
                        if (_event.getStatus() != Event.ESTATUS_STOP) {
                            if (_event._eventPreferencesNotification._enabled) {
                                PPApplication.logE("EventsService.onHandleIntent", "event._id=" + _event._id);
                        /*_event._eventPreferencesNotification.saveStartTime(dataWrapper,
                                intent.getStringExtra(PPApplication.EXTRA_EVENT_NOTIFICATION_PACKAGE_NAME),
                                intent.getLongExtra(PPApplication.EXTRA_EVENT_NOTIFICATION_TIME, 0));*/
                                if (intent.getStringExtra(EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED).equals("posted"))
                                    _event._eventPreferencesNotification.saveStartTime(dataWrapper);

                            }
                        }
                    }
                }
            }
            if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_NFC_TAG)) {
                // search for nfc events, save start time
                PPApplication.logE("EventsService.onHandleIntent", "search for nfc events");
                for (Event _event : eventList) {
                    if (_event.getStatus() != Event.ESTATUS_STOP) {
                        if (_event._eventPreferencesNFC._enabled) {
                            PPApplication.logE("EventsService.onHandleIntent", "event._id=" + _event._id);
                            _event._eventPreferencesNFC.saveStartTime(dataWrapper,
                                    intent.getStringExtra(EXTRA_EVENT_NFC_TAG_NAME),
                                    intent.getLongExtra(EXTRA_EVENT_NFC_DATE, 0));
                        }
                    }
                }
            }
        }

        boolean forDelayStartAlarm = broadcastReceiverType.equals(EventsService.SENSOR_TYPE_EVENT_DELAY_START);
        boolean forDelayEndAlarm = broadcastReceiverType.equals(EventsService.SENSOR_TYPE_EVENT_DELAY_END);

        //PPApplication.logE("@@@ EventsService.onHandleIntent","isRestart="+isRestart);
        PPApplication.logE("@@@ EventsService.onHandleIntent","forDelayStartAlarm="+forDelayStartAlarm);
        PPApplication.logE("@@@ EventsService.onHandleIntent","forDelayEndAlarm="+forDelayEndAlarm);

        // no refresh notification and widgets
        ActivateProfileHelper.lockRefresh = true;

        Profile mergedProfile = DataWrapper.getNonInitializedProfile("", "", 0);
        //mergedProfiles = new ArrayList<>();

        //Profile activatedProfile0 = null;

        int runningEventCount0 = 0;

        if (isRestart)
        {
            PPApplication.logE("$$$ EventsService.onHandleIntent","restart events");

            //oldActivatedProfile = null;

            // 1. pause events
            dataWrapper.sortEventsByStartOrderDesc();
            for (Event _event : eventList)
            {
                PPApplication.logE("EventsService.onHandleIntent","state PAUSE");
                PPApplication.logE("EventsService.onHandleIntent","event._id="+_event._id);
                PPApplication.logE("EventsService.onHandleIntent","event.getStatus()="+_event.getStatus());

                if (_event.getStatus() != Event.ESTATUS_STOP)
                    // len pauzuj eventy
                    // pauzuj aj ked uz je zapauznuty
                    dataWrapper.doEventService(_event, true, true, interactive, forDelayStartAlarm, forDelayEndAlarm, true, mergedProfile, broadcastReceiverType);
            }

            // get running events count
            List<EventTimeline> _etl = dataWrapper.getEventTimelineList();
            runningEventCount0 = _etl.size();

            // 2. start events
            dataWrapper.sortEventsByStartOrderAsc();
            for (Event _event : eventList)
            {
                PPApplication.logE("EventsService.onHandleIntent","state RUNNING");
                PPApplication.logE("EventsService.onHandleIntent","event._id="+_event._id);
                PPApplication.logE("EventsService.onHandleIntent","event.getStatus()="+_event.getStatus());

                if (_event.getStatus() != Event.ESTATUS_STOP)
                    // len spustaj eventy
                    // spustaj vsetky
                    dataWrapper.doEventService(_event, false, true, interactive, forDelayStartAlarm, forDelayEndAlarm, true, mergedProfile, broadcastReceiverType);
            }
        }
        else
        {
            PPApplication.logE("$$$ EventsService.onHandleIntent","NO restart events");

            //oldActivatedProfile = dataWrapper.getActivatedProfile();

            //activatedProfile0 = dataWrapper.getActivatedProfileFromDB();

            //1. pause events
            dataWrapper.sortEventsByStartOrderDesc();
            for (Event _event : eventList)
            {
                PPApplication.logE("EventsService.onHandleIntent","state PAUSE");
                PPApplication.logE("EventsService.onHandleIntent","event._id="+_event._id);
                PPApplication.logE("EventsService.onHandleIntent","event.getStatus()="+_event.getStatus());

                if (_event.getStatus() != Event.ESTATUS_STOP)
                    // len pauzuj eventy
                    // pauzuj len ak este nie je zapauznuty
                    //noinspection ConstantConditions
                    dataWrapper.doEventService(_event, true, false, interactive, forDelayStartAlarm, forDelayEndAlarm, false, mergedProfile, broadcastReceiverType);
            }

            // get running events count
            List<EventTimeline> _etl = dataWrapper.getEventTimelineList();
            runningEventCount0 = _etl.size();

            //2. start events
            dataWrapper.sortEventsByStartOrderAsc();
            for (Event _event : eventList)
            {
                PPApplication.logE("EventsService.onHandleIntent","state RUNNING");
                PPApplication.logE("EventsService.onHandleIntent","event._id="+_event._id);
                PPApplication.logE("EventsService.onHandleIntent","event.getStatus()="+_event.getStatus());

                if (_event.getStatus() != Event.ESTATUS_STOP)
                    // len spustaj eventy
                    // spustaj len ak este nebezi - musi to takto byt, lebo inac to bude furt menit veci v mobile
                    // napr. ked hlasitosti zmenene manualne tlacitkami.
                    //noinspection ConstantConditions
                    dataWrapper.doEventService(_event, false, false, interactive, forDelayStartAlarm, forDelayEndAlarm, true, mergedProfile, broadcastReceiverType);
            }
        }

        ActivateProfileHelper.lockRefresh = false;

        if (mergedProfile._id == 0)
            PPApplication.logE("$$$ EventsService.profile for activation","no profile for activation");
        else
            PPApplication.logE("$$$ EventsService.profile for activation","profileName="+mergedProfile._name);

        //if ((!restartAtEndOfEvent) || isRestart) {
        //    // No any paused events has "Restart events" at end of event

            //////////////////
            //// when no events are running or manual activation,
            //// activate background profile when no profile is activated

            // get running events count
            List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList();
            int runningEventCountE = eventTimelineList.size();

            Profile activatedProfile = dataWrapper.getActivatedProfileFromDB();

            if (!dataWrapper.getIsManualProfileActivation()) {
                PPApplication.logE("$$$ EventsService.onHandleIntent", "active profile is NOT activated manually");
                PPApplication.logE("$$$ EventsService.onHandleIntent", "runningEventCountE=" + runningEventCountE);
                // no manual profile activation
                if (runningEventCountE == 0) {
                    PPApplication.logE("$$$ EventsService.onHandleIntent", "no events running");
                    // no events running
                    long profileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(context));
                    if (profileId != Profile.PROFILE_NO_ACTIVATE) {
                        PPApplication.logE("$$$ EventsService.onHandleIntent", "default profile is set");
                        long activatedProfileId = 0;
                        if (activatedProfile != null)
                            activatedProfileId = activatedProfile._id;
                        if ((activatedProfileId != profileId) || isRestart) {
                            mergedProfile.mergeProfiles(profileId, dataWrapper);
                            PPApplication.logE("$$$ EventsService.onHandleIntent", "activated default profile");
                        }
                    }
                /*else
                if (activatedProfile == null)
                {
                    mergedProfile.mergeProfiles(0, dataWrapper);
                    PPApplication.logE("### EventsService.onHandleIntent", "not activated profile");
                }*/
                }
            } else {
                PPApplication.logE("$$$ EventsService.onHandleIntent", "active profile is activated manually");
                // manual profile activation
                long profileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(context));
                if (profileId != Profile.PROFILE_NO_ACTIVATE) {
                    if (activatedProfile == null) {
                        // if not profile activated, activate Default profile
                        mergedProfile.mergeProfiles(profileId, dataWrapper);
                        PPApplication.logE("$$$ EventsService.onHandleIntent", "not activated profile");
                    }
                }
            }
            ////////////////

            String eventNotificationSound = "";
            boolean eventNotificationVibrate = false;

            if ((!isRestart) && (runningEventCountE > runningEventCount0)) {
                // only when not restart events and running events is increased, play event notification sound

                EventTimeline eventTimeline = eventTimelineList.get(runningEventCountE - 1);
                Event event = dataWrapper.getEventById(eventTimeline._fkEvent);
                if (event != null) {
                    eventNotificationSound = event._notificationSound;
                    eventNotificationVibrate = event._notificationVibrate;
                }
            }

            PPApplication.logE("$$$ EventsService.onHandleIntent", "mergedProfile=" + mergedProfile);

            PPApplication.logE("$$$ EventsService.onHandleIntent", "mergedProfile._id=" + mergedProfile._id);
            if (mergedProfile._id != 0) {
                // activate merged profile
                PPApplication.logE("$$$ EventsService.onHandleIntent", "profileName=" + mergedProfile._name);
                PPApplication.logE("$$$ EventsService.onHandleIntent", "profileId=" + mergedProfile._id);
                PPApplication.logE("$$$ EventsService.onHandleIntent", "profile._deviceRunApplicationPackageName=" + mergedProfile._deviceRunApplicationPackageName);
                PPApplication.logE("$$$ EventsService.onHandleIntent", "interactive=" + interactive);
                dataWrapper.getDatabaseHandler().saveMergedProfile(mergedProfile);
                dataWrapper.activateProfileFromEvent(mergedProfile._id, interactive, false, true);

                if (PhoneProfilesService.instance != null)
                    PhoneProfilesService.instance.playEventNotificationSound(eventNotificationSound, eventNotificationVibrate);

                // wait for profile activation
                //try { Thread.sleep(500); } catch (InterruptedException e) { }
                //SystemClock.sleep(500);
                PPApplication.sleep(500);
            } else {
                /*long prId0 = 0;
                long prId = 0;
                if (activatedProfile0 != null) prId0 = activatedProfile0._id;
                if (activatedProfile != null) prId = activatedProfile._id;
                if ((prId0 != prId) || (prId == 0))*/
                dataWrapper.updateNotificationAndWidgets(activatedProfile);

                if (PhoneProfilesService.instance != null)
                    PhoneProfilesService.instance.playEventNotificationSound(eventNotificationSound, eventNotificationVibrate);

            }

        //}

        //restartAtEndOfEvent = false;

        doEndService(/*intent*/);

        // refresh GUI
        /*Intent refreshIntent = new Intent();
        refreshIntent.setAction(RefreshGUIBroadcastReceiver.INTENT_REFRESH_GUI);
        context.sendBroadcast(refreshIntent);*/
        LocalBroadcastManager.getInstance(context).registerReceiver(PPApplication.refreshGUIBroadcastReceiver, new IntentFilter("RefreshGUIBroadcastReceiver"));
        Intent refreshIntent = new Intent("RefreshGUIBroadcastReceiver");
        LocalBroadcastManager.getInstance(context).sendBroadcast(refreshIntent);


        dataWrapper.invalidateDataWrapper();

        PPApplication.logE("@@@ EventsService.onHandleIntent","-- end --------------------------------");

    }

    private boolean eventsExists(String broadcastReceiverType) {
        int eventType = 0;
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_BATTERY))
            eventType = DatabaseHandler.ETYPE_BATTERY;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_BLUETOOTH_CONNECTION))
            eventType = DatabaseHandler.ETYPE_BLUETOOTHCONNECTED;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_BLUETOOTH_SCANNER))
            eventType = DatabaseHandler.ETYPE_BLUETOOTHINFRONT;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_BLUETOOTH_STATE))
            eventType = DatabaseHandler.ETYPE_BLUETOOTHCONNECTED;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_CALENDAR_PROVIDER_CHANGED))
            eventType = DatabaseHandler.ETYPE_CALENDAR;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_DOCK_CONNECTION))
            eventType = DatabaseHandler.ETYPE_PERIPHERAL;
        else
        /*if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_EVENT_DELAY_START))
            eventType = DatabaseHandler.ETYPE_????;
        else*/
        /*if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_EVENT_DELAY_END))
            eventType = DatabaseHandler.ETYPE_????;
        else*/
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_CALENDAR))
            eventType = DatabaseHandler.ETYPE_CALENDAR;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_TIME))
            eventType = DatabaseHandler.ETYPE_TIME;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_APPLICATION))
            eventType = DatabaseHandler.ETYPE_APPLICATION;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_HEADSET_CONNECTION))
            eventType = DatabaseHandler.ETYPE_PERIPHERAL;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_NOTIFICATION))
            eventType = DatabaseHandler.ETYPE_NOTIFICATION;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_NOTIFICATION_EVENT_END))
            eventType = DatabaseHandler.ETYPE_NOTIFICATION;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_PHONE_CALL))
            eventType = DatabaseHandler.ETYPE_CALL;
        else
        /*if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_RESTART_EVENTS))
            eventType = DatabaseHandler.ETYPE_???;
        else*/
        // call doEventService for all screen on/off changes
        /*if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_SCREEN))
            eventType = DatabaseHandler.ETYPE_SCREEN;
        else*/
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_SEARCH_CALENDAR_EVENTS))
            eventType = DatabaseHandler.ETYPE_CALENDAR;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_SMS))
            eventType = DatabaseHandler.ETYPE_SMS;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_SMS_EVENT_END))
            eventType = DatabaseHandler.ETYPE_SMS;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_WIFI_CONNECTION))
            eventType = DatabaseHandler.ETYPE_WIFICONNECTED;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_WIFI_SCANNER))
            eventType = DatabaseHandler.ETYPE_WIFIINFRONT;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_WIFI_STATE))
            eventType = DatabaseHandler.ETYPE_WIFICONNECTED;
        else
        /*if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_DEVICE_IDLE_MODE))
            eventType = DatabaseHandler.ETYPE_????;
        else*/
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_POWER_SAVE_MODE))
            eventType = DatabaseHandler.ETYPE_BATTERY;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_GEOFENCES_SCANNER))
            eventType = DatabaseHandler.ETYPE_LOCATION;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_LOCATION_MODE))
            eventType = DatabaseHandler.ETYPE_LOCATION;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_DEVICE_ORIENTATION))
            eventType = DatabaseHandler.ETYPE_ORIENTATION;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_PHONE_STATE))
            eventType = DatabaseHandler.ETYPE_MOBILE_CELLS;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_NFC_TAG))
            eventType = DatabaseHandler.ETYPE_NFC;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_NFC_EVENT_END))
            eventType = DatabaseHandler.ETYPE_NFC;
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_RADIO_SWITCH))
            eventType = DatabaseHandler.ETYPE_RADIO_SWITCH;

        if (eventType > 0)
            return dataWrapper.getDatabaseHandler().getTypeEventsCount(eventType) > 0;
        else
            return true;
    }

    private void doEndService(/*Intent intent*/) {
        PPApplication.logE("EventsService.doEndService","broadcastReceiverType="+broadcastReceiverType);
        PPApplication.logE("EventsService.doEndService","callEventType="+callEventType);

        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_PHONE_CALL)) {

            if (!PhoneCallService.linkUnlinkExecuted) {
                // no profile is activated from EventsService
                // link, unlink volumes for activated profile
                boolean linkUnlink = false;
                if (callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_RINGING)
                    linkUnlink = true;
                if (callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_ENDED)
                    linkUnlink = true;
                if (linkUnlink) {
                    Profile profile = dataWrapper.getActivatedProfile();
                    profile = Profile.getMappedProfile(profile, context);
                    if (profile != null) {
                        PPApplication.logE("EventsService.doEndService", "callEventType=" + callEventType);
                        try {
                            Intent volumeServiceIntent = new Intent(context, ExecuteVolumeProfilePrefsService.class);
                            volumeServiceIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
                            volumeServiceIntent.putExtra(ActivateProfileHelper.EXTRA_MERGED_PROFILE, false);
                            volumeServiceIntent.putExtra(ActivateProfileHelper.EXTRA_FOR_PROFILE_ACTIVATION, false);
                            volumeServiceIntent.putExtra(ActivateProfileHelper.EXTRA_STARTED_FROM_BROADCAST, false);
                            WakefulIntentService.sendWakefulWork(context, volumeServiceIntent);
                            // wait for link/unlink
                            //try { Thread.sleep(1000); } catch (InterruptedException e) { }
                            //SystemClock.sleep(1000);
                            PPApplication.sleep(1000);
                        } catch (Exception ignored) {}
                    }
                }
            } else
                PhoneCallService.linkUnlinkExecuted = false;

            if ((android.os.Build.VERSION.SDK_INT >= 21) && (callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_RINGING)) {
                // start PhoneProfilesService for ringing call simulation
                try {
                    Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
                    lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                    lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
                    lIntent.putExtra(EXTRA_SIMULATE_RINGING_CALL, true);
                    lIntent.putExtra(EXTRA_OLD_RINGER_MODE, oldRingerMode);
                    lIntent.putExtra(EXTRA_OLD_SYSTEM_RINGER_MODE, oldSystemRingerMode);
                    lIntent.putExtra(EXTRA_OLD_ZEN_MODE, oldZenMode);
                    lIntent.putExtra(EXTRA_OLD_RINGTONE, oldRingtone);
                    lIntent.putExtra(EXTRA_OLD_SYSTEM_RINGER_VOLUME, oldSystemRingerVolume);
                    //TODO Android O
                    //if (Build.VERSION.SDK_INT < 26)
                        context.startService(lIntent);
                    //else
                    //    context.startForegroundService(lIntent);
                } catch (Exception ignored) {}
            }

            if (!PhoneCallService.speakerphoneOnExecuted) {
                // no profile is activated from EventsService
                // set speakerphone ON for activated profile
                if ((callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_ANSWERED) ||
                        (callEventType == PhoneCallService.CALL_EVENT_OUTGOING_CALL_ANSWERED)) {
                    Profile profile = dataWrapper.getActivatedProfile();
                    profile = Profile.getMappedProfile(profile, context);
                    PhoneCallService.setSpeakerphoneOn(profile, context);
                }
            } else
                PhoneCallService.speakerphoneOnExecuted = false;

            if ((callEventType == PhoneCallService.CALL_EVENT_INCOMING_CALL_ENDED) ||
                (callEventType == PhoneCallService.CALL_EVENT_OUTGOING_CALL_ENDED)) {
                ApplicationPreferences.getSharedPreferences(context);
                SharedPreferences.Editor editor = ApplicationPreferences.preferences.edit();
                editor.putInt(PhoneCallService.PREF_EVENT_CALL_EVENT_TYPE, PhoneCallService.CALL_EVENT_UNDEFINED);
                editor.putString(PhoneCallService.PREF_EVENT_CALL_PHONE_NUMBER, "");
                editor.apply();
            }
        }
        /*else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_SMS)) {
            // start PhoneProfilesService for notification tone simulation
            Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
            lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
            lIntent.putExtra(EXTRA_SIMULATE_NOTIFICATION_TONE, true);
            lIntent.putExtra(EXTRA_OLD_RINGER_MODE, oldRingerMode);
            lIntent.putExtra(EXTRA_OLD_SYSTEM_RINGER_MODE, oldSystemRingerMode);
            lIntent.putExtra(EXTRA_OLD_ZEN_MODE, oldZenMode);
            lIntent.putExtra(EXTRA_OLD_NOTIFICATION_TONE, oldNotificationTone);
            context.startService(lIntent);
        }
        else
        if (broadcastReceiverType.equals(EventsService.SENSOR_TYPE_NOTIFICATION)) {
            if ((android.os.Build.VERSION.SDK_INT >= 21) && intent.getStringExtra(EXTRA_EVENT_NOTIFICATION_POSTED_REMOVED).equals("posted")) {
                // start PhoneProfilesService for notification tone simulation
                Intent lIntent = new Intent(context.getApplicationContext(), PhoneProfilesService.class);
                lIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
                lIntent.putExtra(PhoneProfilesService.EXTRA_START_ON_BOOT, false);
                lIntent.putExtra(EXTRA_SIMULATE_NOTIFICATION_TONE, true);
                lIntent.putExtra(EXTRA_OLD_RINGER_MODE, oldRingerMode);
                lIntent.putExtra(EXTRA_OLD_SYSTEM_RINGER_MODE, oldSystemRingerMode);
                lIntent.putExtra(EXTRA_OLD_ZEN_MODE, oldZenMode);
                lIntent.putExtra(EXTRA_OLD_NOTIFICATION_TONE, oldNotificationTone);
                context.startService(lIntent);
            }
        }*/
    }

}
