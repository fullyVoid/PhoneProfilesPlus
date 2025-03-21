package sk.henrichg.phoneprofilesplus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Calendar;
import java.util.List;

class EventPreferencesSMS extends EventPreferences {

    //int _smsEvent;
    String _contacts;
    String _contactGroups;
    int _contactListType;
    boolean _permanentRun;
    int _duration;
    int _forSIMCard;

    long _startTime;
    int _fromSIMSlot;

    static final String PREF_EVENT_SMS_ENABLED = "eventSMSEnabled";
    //static final String PREF_EVENT_SMS_EVENT = "eventSMSEvent";
    static final String PREF_EVENT_SMS_CONTACTS = "eventSMSContacts";
    static final String PREF_EVENT_SMS_CONTACT_GROUPS = "eventSMSContactGroups";
    private static final String PREF_EVENT_SMS_CONTACT_LIST_TYPE = "eventSMSContactListType";
    private static final String PREF_EVENT_SMS_PERMANENT_RUN = "eventSMSPermanentRun";
    private static final String PREF_EVENT_SMS_DURATION = "eventSMSDuration";
    static final String PREF_EVENT_SMS_INSTALL_EXTENDER = "eventSMSInstallExtender";
    static final String PREF_EVENT_SMS_ACCESSIBILITY_SETTINGS = "eventSMSAccessibilitySettings";
    static final String PREF_EVENT_SMS_LAUNCH_EXTENDER = "eventSMSLaunchExtender";
    private static final String PREF_EVENT_SMS_FOR_SIM_CARD = "eventSMSForSimCard";

    static final String PREF_EVENT_SMS_ENABLED_NO_CHECK_SIM = "eventSMSEnabledEnabledNoCheckSim";

    private static final String PREF_EVENT_SMS_CATEGORY = "eventSMSCategoryRoot";

    //static final int SMS_EVENT_UNDEFINED = -1;
    //static final int SMS_EVENT_INCOMING = 0;
    //static final int SMS_EVENT_OUTGOING = 1;

    //static final int CONTACT_LIST_TYPE_WHITE_LIST = 0;
    //static final int CONTACT_LIST_TYPE_BLACK_LIST = 1;
    private static final int CONTACT_LIST_TYPE_NOT_USE = 2;

    EventPreferencesSMS(Event event,
                                    boolean enabled,
                                    //int smsEvent,
                                    String contacts,
                                    String contactGroups,
                                    int contactListType,
                                    boolean permanentRun,
                                    int duration,
                                    int forSIMCard)
    {
        super(event, enabled);

        //this._smsEvent = smsEvent;
        this._contacts = contacts;
        this._contactGroups = contactGroups;
        this._contactListType = contactListType;
        this._permanentRun = permanentRun;
        this._duration = duration;
        this._forSIMCard = forSIMCard;

        this._startTime = 0;
        this._fromSIMSlot = 0;
    }

    void copyPreferences(Event fromEvent)
    {
        this._enabled = fromEvent._eventPreferencesSMS._enabled;
        //this._smsEvent = fromEvent._eventPreferencesSMS._smsEvent;
        this._contacts = fromEvent._eventPreferencesSMS._contacts;
        this._contactGroups = fromEvent._eventPreferencesSMS._contactGroups;
        this._contactListType = fromEvent._eventPreferencesSMS._contactListType;
        this._permanentRun = fromEvent._eventPreferencesSMS._permanentRun;
        this._duration = fromEvent._eventPreferencesSMS._duration;
        this._forSIMCard = fromEvent._eventPreferencesSMS._forSIMCard;
        this.setSensorPassed(fromEvent._eventPreferencesSMS.getSensorPassed());

        this._startTime = 0;
        this._fromSIMSlot = 0;

    }

    void loadSharedPreferences(SharedPreferences preferences)
    {
        Editor editor = preferences.edit();
        editor.putBoolean(PREF_EVENT_SMS_ENABLED, _enabled);
        //editor.putString(PREF_EVENT_SMS_EVENT, String.valueOf(this._smsEvent));
        editor.putString(PREF_EVENT_SMS_CONTACTS, this._contacts);
        editor.putString(PREF_EVENT_SMS_CONTACT_GROUPS, this._contactGroups);
        editor.putString(PREF_EVENT_SMS_CONTACT_LIST_TYPE, String.valueOf(this._contactListType));
        editor.putBoolean(PREF_EVENT_SMS_PERMANENT_RUN, this._permanentRun);
        editor.putString(PREF_EVENT_SMS_DURATION, String.valueOf(this._duration));
        editor.putString(PREF_EVENT_SMS_FOR_SIM_CARD, String.valueOf(this._forSIMCard));
        editor.apply();
    }

    void saveSharedPreferences(SharedPreferences preferences)
    {
        this._enabled = preferences.getBoolean(PREF_EVENT_SMS_ENABLED, false);
        //this._smsEvent = Integer.parseInt(preferences.getString(PREF_EVENT_SMS_EVENT, "0"));
        this._contacts = preferences.getString(PREF_EVENT_SMS_CONTACTS, "");
        this._contactGroups = preferences.getString(PREF_EVENT_SMS_CONTACT_GROUPS, "");
        this._contactListType = Integer.parseInt(preferences.getString(PREF_EVENT_SMS_CONTACT_LIST_TYPE, "0"));
        this._permanentRun = preferences.getBoolean(PREF_EVENT_SMS_PERMANENT_RUN, false);
        this._duration = Integer.parseInt(preferences.getString(PREF_EVENT_SMS_DURATION, "5"));
        this._forSIMCard = Integer.parseInt(preferences.getString(PREF_EVENT_SMS_FOR_SIM_CARD, "0"));
    }

    String getPreferencesDescription(boolean addBullet, boolean addPassStatus, boolean disabled, Context context) {
        String descr = "";

        if (!this._enabled) {
            if (!addBullet)
                descr = context.getString(R.string.event_preference_sensor_sms_summary);
        } else {
            if (addBullet) {
                descr = descr + "<b>";
                descr = descr + getPassStatusString(context.getString(R.string.event_type_sms), addPassStatus, DatabaseHandler.ETYPE_SMS, context);
                descr = descr + "</b> ";
            }

            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_SMS_ENABLED, context);
            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context.getApplicationContext());
                if (extenderVersion == 0) {
                    descr = descr + context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + context.getString(R.string.preference_not_allowed_reason_not_extender_installed);
                } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST) {
                    descr = descr + context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context.getApplicationContext(), false, true
                        /*, "EventPreferencesSMS.getPreferencesDescription"*/)) {
                    descr = descr + context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + context.getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                } if (PPApplication.accessibilityServiceForPPPExtenderConnected == 0) {
                    descr = descr + context.getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + context.getString(R.string.preference_not_allowed_reason_state_of_accessibility_setting_for_extender_is_determined);
                } else {
                    //descr = descr + context.getString(R.string.pref_event_sms_event);
                    //String[] smsEvents = context.getResources().getStringArray(R.array.eventSMSEventsArray);
                    //descr = descr + ": " + smsEvents[tmp._smsEvent] + "; ";

                    descr = descr + context.getString(R.string.event_preferences_sms_contact_groups) + ": ";
                    descr = descr + "<b>" + getColorForChangedPreferenceValue(ContactGroupsMultiSelectDialogPreference.getSummary(_contactGroups, context), disabled, context) + "</b> • ";

                    descr = descr + context.getString(R.string.event_preferences_sms_contacts) + ": ";
                    descr = descr + "<b>" + getColorForChangedPreferenceValue(ContactsMultiSelectDialogPreference.getSummary(_contacts, false, context), disabled, context) + "</b> • ";

                    descr = descr + context.getString(R.string.pref_event_sms_contactListType);
                    String[] contactListTypes = context.getResources().getStringArray(R.array.eventSMSContactListTypeArray);
                    descr = descr + ": <b>" + getColorForChangedPreferenceValue(contactListTypes[this._contactListType], disabled, context) + "</b>";

                    if (Build.VERSION.SDK_INT >= 26) {
                        boolean hasSIMCard = false;
                        //if (Build.VERSION.SDK_INT >= 26) {
                            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            if (telephonyManager != null) {
                                int phoneCount = telephonyManager.getPhoneCount();
                                if (phoneCount > 1) {
                                    boolean simExists;
                                    boolean sim1Exists = GlobalUtils.hasSIMCard(context, 1);
                                    boolean sim2Exists = GlobalUtils.hasSIMCard(context, 2);

                                    simExists = sim1Exists;
                                    simExists = simExists && sim2Exists;
                                    hasSIMCard = simExists;
                                }
                            }
                        //}
                        if (hasSIMCard) {
                            descr = descr + " • " + context.getString(R.string.event_preferences_sms_forSimCard);
                            String[] forSimCard = context.getResources().getStringArray(R.array.eventSMSForSimCardArray);
                            descr = descr + ": <b>" + getColorForChangedPreferenceValue(forSimCard[this._forSIMCard], disabled, context) + "</b>";
                        }
                    }

                    if (this._permanentRun)
                        descr = descr + " • <b>" + getColorForChangedPreferenceValue(context.getString(R.string.pref_event_permanentRun), disabled, context) + "</b>";
                    else
                        descr = descr + " • " + context.getString(R.string.pref_event_duration) + ": <b>" + getColorForChangedPreferenceValue(StringFormatUtils.getDurationString(this._duration), disabled, context) + "</b>";
                }
            }
            else {
                descr = descr + context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context);
            }
        }

        return descr;
    }

    private void setSummary(PreferenceManager prefMng, String key, String value, Context context)
    {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (preferences == null)
            return;

        if (key.equals(PREF_EVENT_SMS_ENABLED)) {
            SwitchPreferenceCompat preference = prefMng.findPreference(key);
            if (preference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, preferences.getBoolean(key, false), false, false, false);
            }
        }

        if (/*key.equals(PREF_EVENT_SMS_EVENT) ||*/ key.equals(PREF_EVENT_SMS_CONTACT_LIST_TYPE))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(value);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
            }
        }
        if (key.equals(PREF_EVENT_SMS_PERMANENT_RUN)) {
            SwitchPreferenceCompat permanentRunPreference = prefMng.findPreference(key);
            if (permanentRunPreference != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(permanentRunPreference, true, preferences.getBoolean(key, false), false, false, false);
            }
            Preference preference = prefMng.findPreference(PREF_EVENT_SMS_DURATION);
            if (preference != null) {
                preference.setEnabled(value.equals("false"));
            }
        }
        if (key.equals(PREF_EVENT_SMS_DURATION)) {
            Preference preference = prefMng.findPreference(key);
            int delay;
            try {
                delay = Integer.parseInt(value);
            } catch (Exception e) {
                delay = 5;
            }
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, delay > 5, false, false, false);
        }

        boolean hasFeature = false;
        boolean hasSIMCard = false;
        if (Build.VERSION.SDK_INT >= 26) {
            if (key.equals(PREF_EVENT_SMS_FOR_SIM_CARD)) {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        hasFeature = true;
                        boolean simExists;
                        boolean sim1Exists = GlobalUtils.hasSIMCard(context, 1);
                        boolean sim2Exists = GlobalUtils.hasSIMCard(context, 2);

                        simExists = sim1Exists;
                        simExists = simExists && sim2Exists;
                        hasSIMCard = simExists;
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            int index = listPreference.findIndexOfValue(value);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);
                        }
                    }
                }
                if (!hasFeature) {
                    Preference preference = prefMng.findPreference(PREF_EVENT_SMS_FOR_SIM_CARD);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NO_HARDWARE;
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                }
                else if (!hasSIMCard) {
                    Preference preference = prefMng.findPreference(PREF_EVENT_SMS_FOR_SIM_CARD);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = new PreferenceAllowed();
                        preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                        preferenceAllowed.notAllowedReason = PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_TWO_SIM_CARDS;
                        preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    }
                }
            }
        }

        if (key.equals(PREF_EVENT_SMS_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0) {
                    String summary = context.getString(R.string.profile_preferences_PPPExtender_not_installed_summary) +
                            "\n\n" + context.getString(R.string.event_preferences_sms_PPPExtender_install_summary);
                    preference.setSummary(summary);
                }
                else {
                    String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(context);
                    String summary = context.getString(R.string.profile_preferences_PPPExtender_installed_summary) +
                            " " + extenderVersionName + " (" + extenderVersion + ")\n\n";
                    if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)
                        summary = summary + context.getString(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                    else
                        summary = summary + context.getString(R.string.event_preferences_applications_PPPExtender_upgrade_summary);
                    preference.setSummary(summary);
                }
            }
        }

        Event event = new Event();
        event.createEventPreferences();
        event._eventPreferencesSMS.saveSharedPreferences(prefMng.getSharedPreferences());
        boolean isRunnable = event._eventPreferencesSMS.isRunnable(context);
        boolean enabled = preferences.getBoolean(PREF_EVENT_SMS_ENABLED, false);
        Preference preference = prefMng.findPreference(PREF_EVENT_SMS_CONTACT_GROUPS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_SMS_CONTACT_GROUPS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable);
        }
        preference = prefMng.findPreference(PREF_EVENT_SMS_CONTACTS);
        if (preference != null) {
            boolean bold = !prefMng.getSharedPreferences().getString(PREF_EVENT_SMS_CONTACTS, "").isEmpty();
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, bold, false, true, !isRunnable);
        }
        preference = prefMng.findPreference(PREF_EVENT_SMS_CONTACT_LIST_TYPE);
        if (preference != null)
            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true, !isRunnable);

        int _isAccessibilityEnabled = event._eventPreferencesSMS.isAccessibilityServiceEnabled(context, false);
        boolean isAccessibilityEnabled = _isAccessibilityEnabled == 1;
        preference = prefMng.findPreference(PREF_EVENT_SMS_ACCESSIBILITY_SETTINGS);
        if (preference != null) {

            String summary;
            if (isAccessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1))
                summary = context.getString(R.string.accessibility_service_enabled);
            else {
                if (_isAccessibilityEnabled == -1) {
                    summary = context.getString(R.string.accessibility_service_not_used);
                    summary = summary + "\n\n" + context.getString(R.string.preference_not_used_extender_reason) + " " +
                            context.getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                } else {
                    summary = context.getString(R.string.accessibility_service_disabled);
                    summary = summary + "\n\n" + context.getString(R.string.event_preferences_sms_AccessibilitySettingsForExtender_summary);
                }
            }
            preference.setSummary(summary);

            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true, !isAccessibilityEnabled);
        }
    }

    void setSummary(PreferenceManager prefMng, String key, SharedPreferences preferences, Context context)
    {
        if (preferences == null)
            return;

        Preference preference = prefMng.findPreference(key);
        if (preference == null)
            return;

        if (key.equals(PREF_EVENT_SMS_ENABLED) ||
            key.equals(PREF_EVENT_SMS_PERMANENT_RUN)) {
            boolean value = preferences.getBoolean(key, false);
            setSummary(prefMng, key, value ? "true": "false", context);
        }
        if (/*key.equals(PREF_EVENT_SMS_EVENT) ||*/
            key.equals(PREF_EVENT_SMS_CONTACT_LIST_TYPE) ||
            key.equals(PREF_EVENT_SMS_CONTACTS) ||
            key.equals(PREF_EVENT_SMS_CONTACT_GROUPS) ||
            key.equals(PREF_EVENT_SMS_DURATION) ||
            key.equals(PREF_EVENT_SMS_INSTALL_EXTENDER) ||
            key.equals(PREF_EVENT_SMS_FOR_SIM_CARD))
        {
            setSummary(prefMng, key, preferences.getString(key, ""), context);
        }
    }

    void setAllSummary(PreferenceManager prefMng, SharedPreferences preferences, Context context)
    {
        setSummary(prefMng, PREF_EVENT_SMS_ENABLED, preferences, context);
        //setSummary(prefMng, PREF_EVENT_SMS_EVENT, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_CONTACT_LIST_TYPE, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_CONTACTS, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_CONTACT_GROUPS, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_PERMANENT_RUN, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_DURATION, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_INSTALL_EXTENDER, preferences, context);
        setSummary(prefMng, PREF_EVENT_SMS_FOR_SIM_CARD, preferences, context);
    }

    void setCategorySummary(PreferenceManager prefMng, /*String key,*/ SharedPreferences preferences, Context context) {
        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(PREF_EVENT_SMS_ENABLED_NO_CHECK_SIM, context);
        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            EventPreferencesSMS tmp = new EventPreferencesSMS(this._event, this._enabled, this._contacts, this._contactGroups, this._contactListType,
                                                                this._permanentRun, this._duration, this._forSIMCard);
            if (preferences != null)
                tmp.saveSharedPreferences(preferences);

            Preference preference = prefMng.findPreference(PREF_EVENT_SMS_CATEGORY);
            if (preference != null) {
                boolean enabled = tmp._enabled; //(preferences != null) && preferences.getBoolean(PREF_EVENT_SMS_ENABLED, false);
                boolean runnable = tmp.isRunnable(context) && (tmp.isAccessibilityServiceEnabled(context, false) == 1);
                boolean permissionGranted = true;
                if (enabled)
                    permissionGranted = Permissions.checkEventPermissions(context, null, preferences, EventsHandler.SENSOR_TYPE_SMS).size() == 0;
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, tmp._enabled, false, false, !(runnable && permissionGranted));
                if (enabled)
                    preference.setSummary(StringFormatUtils.fromHtml(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context), false, false, false, 0, 0, true));
                else
                    preference.setSummary(tmp.getPreferencesDescription(false, false, !preference.isEnabled(), context));
            }
        }
        else {
            Preference preference = prefMng.findPreference(PREF_EVENT_SMS_CATEGORY);
            if (preference != null) {
                preference.setSummary(context.getString(R.string.profile_preferences_device_not_allowed)+
                        ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                preference.setEnabled(false);
            }
        }
    }

    @Override
    boolean isRunnable(Context context)
    {

        boolean runnable = super.isRunnable(context);

        runnable = runnable && ((_contactListType == CONTACT_LIST_TYPE_NOT_USE) ||
                              (!(_contacts.isEmpty() && _contactGroups.isEmpty())));

        return runnable;
    }

    @Override
    int isAccessibilityServiceEnabled(Context context, boolean againCheckInDelay)
    {
        int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
        if (extenderVersion == 0)
            return -2;
        if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)
            return -1;
        if ((_event.getStatus() != Event.ESTATUS_STOP) && this._enabled && isRunnable(context)) {
            if (PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, againCheckInDelay, true
                        /*, "EventPreferencesSMS.isAccessibilityServiceEnabled"*/))
                return 1;
        } else
            return 1;
        return 0;
    }

    @Override
    void checkPreferences(PreferenceManager prefMng, boolean onlyCategory, Context context) {
        SharedPreferences preferences = prefMng.getSharedPreferences();
        if (!onlyCategory) {
            if (prefMng.findPreference(PREF_EVENT_SMS_ENABLED) != null) {
                final boolean accessibilityEnabled =
                        PPPExtenderBroadcastReceiver.isEnabled(context.getApplicationContext()/*, PPApplication.VERSION_CODE_EXTENDER_7_0*/, true, false
                                /*, "EventPreferencesSMS.checkPreferences"*/);

                boolean enabled = (preferences != null) && preferences.getBoolean(PREF_EVENT_SMS_ENABLED, false);
                Preference preference = prefMng.findPreference(PREF_EVENT_SMS_ACCESSIBILITY_SETTINGS);
                if (preference != null)
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, enabled, false, false, true, !accessibilityEnabled);

                if (Build.VERSION.SDK_INT >= 26) {
                    boolean showPreferences = false;
                    final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        int phoneCount = telephonyManager.getPhoneCount();
                        if (phoneCount > 1) {
                            boolean sim1Exists;
                            boolean sim2Exists;
                            sim1Exists = GlobalUtils.hasSIMCard(context, 1);
                            sim2Exists = GlobalUtils.hasSIMCard(context, 2);

                            showPreferences = true;
                            preference = prefMng.findPreference("eventSMSDualSIMInfo");
                            if (preference != null)
                                preference.setEnabled(enabled && sim1Exists && sim2Exists);
                            preference = prefMng.findPreference(PREF_EVENT_SMS_FOR_SIM_CARD);
                            if (preference != null)
                                preference.setEnabled(enabled && sim1Exists && sim2Exists);
                        } else {
                            preference = prefMng.findPreference("eventSMSDualSIMInfo");
                            if (preference != null)
                                preference.setEnabled(false);
                            preference = prefMng.findPreference(PREF_EVENT_SMS_FOR_SIM_CARD);
                            if (preference != null)
                                preference.setEnabled(false);
                        }
                    }
                    if (!showPreferences) {
                        preference = prefMng.findPreference("eventSMSDualSIMInfo");
                        if (preference != null)
                            preference.setVisible(false);
                        preference = prefMng.findPreference(PREF_EVENT_SMS_FOR_SIM_CARD);
                        if (preference != null)
                            preference.setVisible(false);
                    }
                }
                setSummary(prefMng, PREF_EVENT_SMS_ENABLED, preferences, context);
            }
        }
        setCategorySummary(prefMng, preferences, context);
    }

    private long computeAlarm()
    {
        Calendar calEndTime = Calendar.getInstance();

        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();

        calEndTime.setTimeInMillis((_startTime - gmtOffset) + (_duration * 1000L));
        //calEndTime.set(Calendar.SECOND, 0);
        //calEndTime.set(Calendar.MILLISECOND, 0);

        long alarmTime;
        alarmTime = calEndTime.getTimeInMillis();

        return alarmTime;
    }

    @Override
    void setSystemEventForStart(Context context)
    {
        // set alarm for state PAUSE

        // this alarm generates broadcast, that change state into RUNNING;
        // from broadcast will by called EventsHandler

        removeAlarm(context);
    }

    @Override
    void setSystemEventForPause(Context context)
    {
        // set alarm for state RUNNING

        // this alarm generates broadcast, that change state into PAUSE;
        // from broadcast will by called EventsHandler

        removeAlarm(context);

        if (!(isRunnable(context) && _enabled))
            return;

        setAlarm(computeAlarm(), context);
    }

    @Override
    void removeSystemEvent(Context context)
    {
        removeAlarm(context);
    }

    void removeAlarm(Context context)
    {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                //Intent intent = new Intent(context, SMSEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_SMS_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, SMSEventEndBroadcastReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
        //PPApplication.cancelWork(WorkerWithoutData.ELAPSED_ALARMS_SMS_EVENT_SENSOR_TAG_WORK+"_" + (int) _event._id);
    }

    private void setAlarm(long alarmTime, Context context)
    {
        if (!_permanentRun) {
            if (_startTime > 0) {
                //Intent intent = new Intent(context, SMSEventEndBroadcastReceiver.class);
                Intent intent = new Intent();
                intent.setAction(PhoneProfilesService.ACTION_SMS_EVENT_END_BROADCAST_RECEIVER);
                //intent.setClass(context, SMSEventEndBroadcastReceiver.class);

                //intent.putExtra(PPApplication.EXTRA_EVENT_ID, _event._id);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) _event._id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (ApplicationPreferences.applicationUseAlarmClock) {
                        Intent editorIntent = new Intent(context, EditorActivity.class);
                        editorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent infoPendingIntent = PendingIntent.getActivity(context, 1000, editorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(alarmTime + Event.EVENT_ALARM_TIME_SOFT_OFFSET, infoPendingIntent);
                        alarmManager.setAlarmClock(clockInfo, pendingIntent);
                    }
                    else {
                        //if (android.os.Build.VERSION.SDK_INT >= 23)
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                        //else //if (android.os.Build.VERSION.SDK_INT >= 19)
                        //    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                        //else
                        //    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime + Event.EVENT_ALARM_TIME_OFFSET, pendingIntent);
                    }
                }
            }
        }
    }

    void saveStartTime(DataWrapper dataWrapper, String phoneNumber, long startTime, int fromSIMSlot) {
        if (this._startTime == 0) {
            // alarm for end is not set

            if (Permissions.checkContacts(dataWrapper.context)) {
                boolean phoneNumberFound = false;

                if (this._contactListType != EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE) {
                    // find phone number in groups
                    String[] splits = this._contactGroups.split("\\|");
                    for (String split : splits) {
                    /*String[] projection = new String[]{ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID};
                    String selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=? AND "
                            + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
                    String[] selectionArgs = new String[]{split};
                    Cursor mCursor = dataWrapper.context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);
                    if (mCursor != null) {
                        while (mCursor.moveToNext()) {
                            String contactId = mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
                            String[] projection2 = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                            String selection2 = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?" + " and " +
                                    ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "=1";
                            String[] selection2Args = new String[]{contactId};
                            Cursor phones = dataWrapper.context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection2, selection2, selection2Args, null);
                            if (phones != null) {
                                while (phones.moveToNext()) {
                                    String _phoneNumber = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                        phoneNumberFound = true;
                                        break;
                                    }
                                }
                                phones.close();
                            }
                            if (phoneNumberFound)
                                break;
                        }
                        mCursor.close();
                    }*/

                        if (!split.isEmpty()) {
                            ContactsCache contactsCache = PPApplication.getContactsCache();
                            if (contactsCache == null)
                                return;

                            synchronized (PPApplication.contactsCacheMutex) {
                                List<Contact> contactList = contactsCache.getList(/*false*/);
                                if (contactList != null) {
                                    for (Contact contact : contactList) {
                                        if (contact.groups != null) {
                                            long groupId = contact.groups.indexOf(Long.valueOf(split));
                                            if (groupId != -1) {
                                                // group found in contact
                                                if (contact.phoneId != 0) {
                                                    String _phoneNumber = contact.phoneNumber;
                                                    if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                                        phoneNumberFound = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (phoneNumberFound)
                            break;
                    }

                    if (!phoneNumberFound) {
                        // find phone number in contacts
                        splits = this._contacts.split("\\|");
                        for (String split : splits) {
                            String[] splits2 = split.split("#");

                        /*// get phone number from contacts
                        String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER};
                        String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1' and " + ContactsContract.Contacts._ID + "=?";
                        String[] selectionArgs = new String[]{splits2[0]};
                        Cursor mCursor = dataWrapper.context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, selection, selectionArgs, null);
                        if (mCursor != null) {
                            while (mCursor.moveToNext()) {
                                String[] projection2 = new String[]{ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.NUMBER};
                                String selection2 = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?" + " and " + ContactsContract.CommonDataKinds.Phone._ID + "=?";
                                String[] selection2Args = new String[]{splits2[0], splits2[1]};
                                Cursor phones = dataWrapper.context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection2, selection2, selection2Args, null);
                                if (phones != null) {
                                    while (phones.moveToNext()) {
                                        String _phoneNumber = phones.getString(phones.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                        if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                            phoneNumberFound = true;
                                            break;
                                        }
                                    }
                                    phones.close();
                                }
                                if (phoneNumberFound)
                                    break;
                            }
                            mCursor.close();
                        }*/

                            if ((!split.isEmpty()) && (!splits2[0].isEmpty()) && (!splits2[1].isEmpty())) {
                                ContactsCache contactsCache = PPApplication.getContactsCache();
                                if (contactsCache == null)
                                    return;

                                synchronized (PPApplication.contactsCacheMutex) {
                                    List<Contact> contactList = contactsCache.getList(/*false*/);
                                    if (contactList != null) {
                                        for (Contact contact : contactList) {
                                            if (contact.phoneId != 0) {
                                                if ((contact.contactId == Long.parseLong(splits2[0])) && contact.phoneId == Long.parseLong(splits2[1])) {
                                                    String _phoneNumber = contact.phoneNumber;
                                                    if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                                        phoneNumberFound = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (phoneNumberFound)
                                break;
                        }
                    }

                    if (this._contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_BLACK_LIST)
                        phoneNumberFound = !phoneNumberFound;

                } else
                    phoneNumberFound = true;

                if (phoneNumberFound) {
                    this._startTime = startTime;// + (10 * 1000);
                    this._fromSIMSlot = fromSIMSlot;
                }
                else {
                    this._startTime = 0;
                    this._fromSIMSlot = 0;
                }

                DatabaseHandler.getInstance(dataWrapper.context).updateSMSStartTime(_event);

                if (phoneNumberFound) {
                    //if (_event.getStatus() == Event.ESTATUS_RUNNING)
                        setSystemEventForPause(dataWrapper.context);
                }
            } else {
                this._startTime = 0;
                this._fromSIMSlot = 0;
                DatabaseHandler.getInstance(dataWrapper.context).updateSMSStartTime(_event);
            }
        }
    }

    void doHandleEvent(EventsHandler eventsHandler/*, boolean forRestartEvents*/) {
        if (_enabled) {
            int oldSensorPassed = getSensorPassed();
            if ((Event.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, eventsHandler.context).allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
                // permissions are checked in EditorActivity.displayRedTextToPreferencesNotification()
                /*&& Permissions.checkEventSMSContacts(context, event, null)*/
                /* moved to Extender && Permissions.checkEventSMSBroadcast(context, event, null)*/) {
                // compute start time

                if (_startTime > 0) {
                    if ((Build.VERSION.SDK_INT < 26) || (_forSIMCard == 0) || (_forSIMCard == _fromSIMSlot)) {

                        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                        long startTime = _startTime - gmtOffset;

                        // compute end datetime
                        long endAlarmTime = computeAlarm();

                        Calendar now = Calendar.getInstance();
                        long nowAlarmTime = now.getTimeInMillis();

                        if (eventsHandler.sensorType == EventsHandler.SENSOR_TYPE_SMS)
                            eventsHandler.smsPassed = true;
                        else if (!_permanentRun) {
                            if (eventsHandler.sensorType == EventsHandler.SENSOR_TYPE_SMS_EVENT_END)
                                eventsHandler.smsPassed = false;
                            else
                                eventsHandler.smsPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                        } else {
                            eventsHandler.smsPassed = nowAlarmTime >= startTime;
                        }
                    }
                    else
                        eventsHandler.smsPassed = false;
                } else {
                    eventsHandler.smsPassed = false;
                }

                if (!eventsHandler.smsPassed) {
                    _startTime = 0;
                    _fromSIMSlot = 0;
                    DatabaseHandler.getInstance(eventsHandler.context).updateSMSStartTime(_event);
                }

                if (!eventsHandler.notAllowedSms) {
                    if (eventsHandler.smsPassed)
                        setSensorPassed(EventPreferences.SENSOR_PASSED_PASSED);
                    else
                        setSensorPassed(EventPreferences.SENSOR_PASSED_NOT_PASSED);
                }
            } else
                eventsHandler.notAllowedSms = true;
            int newSensorPassed = getSensorPassed() & (~EventPreferences.SENSOR_PASSED_WAITING);
            if (oldSensorPassed != newSensorPassed) {
                setSensorPassed(newSensorPassed);
                DatabaseHandler.getInstance(eventsHandler.context).updateEventSensorPassed(_event, DatabaseHandler.ETYPE_SMS);
            }
        }
    }

}
