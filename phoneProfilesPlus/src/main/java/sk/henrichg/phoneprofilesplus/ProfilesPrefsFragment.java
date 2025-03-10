package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
//import me.drakeet.support.toast.ToastCompat;

public class ProfilesPrefsFragment extends PreferenceFragmentCompat
                                    implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceManager prefMng;
    private SharedPreferences preferences;

    private boolean nestedFragment = false;

    //private static final String PREF_NOTIFICATION_ACCESS = "prf_pref_volumeNotificationsAccessSettings";
    private static final int RESULT_NOTIFICATION_ACCESS_SETTINGS = 2980;
    private static final int RESULT_UNLINK_VOLUMES_APP_PREFERENCES = 2981;
    private static final int RESULT_ACCESSIBILITY_SETTINGS = 2983;
    private static final int RESULT_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON_SETTINGS = 2984;
    private static final int RESULT_ASSISTANT_SETTINGS = 2985;

    private static final String PREF_VOLUME_NOTIFICATION_VOLUME0 = "prf_pref_volumeNotificationVolume0";

    private static final String PRF_GRANT_PERMISSIONS = "prf_pref_grantPermissions";
    private static final String PRF_GRANT_ROOT = "prf_pref_grantRoot";
    private static final String PRF_GRANT_G1_PREFERENCES = "prf_pref_grantG1Permissions";
    private static final String PRF_NOT_ENABLED_ACCESSIBILITY_SERVICE = "prf_pref_notEnabledAccessibilityService";
    private static final String PRF_NOT_INSTALLED_PPPPS = "prf_pref_notInstammedPPPPS";

    private static final String PREF_FORCE_STOP_APPLICATIONS_CATEGORY = "prf_pref_forceStopApplicationsCategoryRoot";
    private static final String PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER = "prf_pref_deviceForceStopApplicationInstallExtender";
    private static final String PREF_FORCE_STOP_APPLICATIONS_ACCESSIBILITY_SETTINGS = "prf_pref_deviceForceStopApplicationAccessibilitySettings";
    //private static final String PREF_INSTALL_SILENT_TONE = "prf_pref_soundInstallSilentTone";
    private static final String PREF_LOCK_DEVICE_CATEGORY = "prf_pref_lockDeviceCategoryRoot";
    private static final String PREF_LOCK_DEVICE_INSTALL_EXTENDER = "prf_pref_lockDeviceInstallExtender";
    private static final String PREF_LOCK_DEVICE_ACCESSIBILITY_SETTINGS = "prf_pref_lockDeviceAccessibilitySettings";
    private static final String PREF_FORCE_STOP_APPLICATIONS_LAUNCH_EXTENDER = "prf_pref_deviceForceStopApplicationLaunchExtender";
    private static final String PREF_LOCK_DEVICE_LAUNCH_EXTENDER = "prf_pref_lockDeviceLaunchExtender";
    private static final String PREF_NOTIFICATION_ACCESS_ENABLED = "prf_pref_notificationAccessEnable";
    private static final String PREF_NOTIFICATION_LED_INFO = "prf_pref_notificationLedInfo";
    private static final String PREF_ALWAYS_ON_DISPLAY_INFO = "prf_pref_alwaysOnDisplayInfo";
    private static final String PREF_PROFILE_DEVICE_RADIOS_DUAL_SIM_SUPPORT_CATEGORY_ROOT = "prf_pref_deviceRadiosDualSIMSupportCategoryRoot";
    private static final String PREF_PROFILE_SOUNDS_DUAL_SIM_SUPPORT_CATEGORY_ROOT = "prf_pref_soundsDualSIMSupportCategoryRoot";
    private static final String PREF_DEVICE_WALLPAPER_CATEGORY = "prf_pref_deviceWallpaperCategoryRoot";
    private static final String PREF_PROFILE_DEVICE_RUN_APPLICATION_MIUI_PERMISSIONS = "prf_pref_deviceRunApplicationMIUIPermissions";
    private static final String PREF_PROFILE_DEVICE_BRIGHTNESS_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON = "prf_pref_deviceBrightness_forceSetBrightnessAtScreenOn";
    private static final String PREF_PROFILE_DEVICE_AIRPLANE_MODE_ASSISTANT_SETTINGS = "prf_pref_deviceAirplaneMode_assistantSettings";
    private static final String PREF_SCREEN_DARK_MODE_INFO = "prf_pref_screenDarkModeInfo";
    private static final String PREF_PROFILE_AIRPLANE_MODE_RADIOS_INFO = "prf_pref_deviceAirplaneModeRadiosInfo";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // is required for to not call onCreate and onDestroy on orientation change
        //noinspection deprecation
        setRetainInstance(true);

        nestedFragment = !(this instanceof ProfilesPrefsActivity.ProfilesPrefsRoot);

        initPreferenceFragment(/*savedInstanceState*/);

        updateAllSummary();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //initPreferenceFragment();
    }

    @NonNull
    @Override
    public RecyclerView onCreateRecyclerView (@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, Bundle state) {
        final RecyclerView view = super.onCreateRecyclerView(inflater, parent, state);
        view.setItemAnimator(null);
        view.setLayoutAnimation(null);

        // do not use this, because this generates exception on orientation change:
        // java.lang.NullPointerException: Attempt to invoke virtual method 'android.widget.ScrollBarDrawable
        // android.widget.ScrollBarDrawable.mutate()' on a null object reference
        //view.setScrollbarFadingEnabled(false);

        return view;
    }

    @Override
    public void onDisplayPreferenceDialog(@NonNull Preference preference)
    {
        PreferenceDialogFragmentCompat dialogFragment = null;

        if (preference instanceof PPListPreference)
        {
            ((PPListPreference)preference).fragment = new PPListPreferenceFragment();
            dialogFragment = ((PPListPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof PPMultiSelectListPreference)
        {
            ((PPMultiSelectListPreference)preference).fragment = new PPMultiSelectListPreferenceFragment();
            dialogFragment = ((PPMultiSelectListPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof DurationDialogPreference)
        {
            ((DurationDialogPreference)preference).fragment = new DurationDialogPreferenceFragment();
            dialogFragment = ((DurationDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof TimeDialogPreference)
        {
            ((TimeDialogPreference)preference).fragment = new TimeDialogPreferenceFragment();
            dialogFragment = ((TimeDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof RingtonePreference)
        {
            ((RingtonePreference)preference).fragment = new RingtonePreferenceFragment();
            dialogFragment = ((RingtonePreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof InfoDialogPreference)
        {
            ((InfoDialogPreference)preference).fragment = new InfoDialogPreferenceFragment();
            dialogFragment = ((InfoDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ProfileIconPreference)
        {
            ((ProfileIconPreference)preference).fragment = new ProfileIconPreferenceFragment();
            dialogFragment = ((ProfileIconPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof VolumeDialogPreference)
        {
            ((VolumeDialogPreference)preference).fragment = new VolumeDialogPreferenceFragment();
            dialogFragment = ((VolumeDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof NotificationVolume0DialogPreference)
        {
            ((NotificationVolume0DialogPreference)preference).fragment = new NotificationVolume0DialogPreferenceFragment();
            dialogFragment = ((NotificationVolume0DialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ConnectToSSIDDialogPreference)
        {
            ((ConnectToSSIDDialogPreference)preference).fragment = new ConnectToSSIDDialogPreferenceFragment();
            dialogFragment = ((ConnectToSSIDDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof BrightnessDialogPreference)
        {
            ((BrightnessDialogPreference)preference).fragment = new BrightnessDialogPreferenceFragment();
            dialogFragment = ((BrightnessDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof RunApplicationsDialogPreference)
        {
            ((RunApplicationsDialogPreference)preference).fragment = new RunApplicationsDialogPreferenceFragment();
            dialogFragment = ((RunApplicationsDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ApplicationsMultiSelectDialogPreference)
        {
            ((ApplicationsMultiSelectDialogPreference)preference).fragment = new ApplicationsMultiSelectDialogPreferenceFragment();
            dialogFragment = ((ApplicationsMultiSelectDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ProfilePreference) {
            ((ProfilePreference) preference).fragment = new ProfilePreferenceFragment();
            dialogFragment = ((ProfilePreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof GenerateNotificationDialogPreference)
        {
            ((GenerateNotificationDialogPreference)preference).fragment = new GenerateNotificationDialogPreferenceFragment();
            dialogFragment = ((GenerateNotificationDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ConfiguredProfilePreferencesDialogPreference)
        {
            ((ConfiguredProfilePreferencesDialogPreference)preference).fragment = new ConfiguredProfilePreferencesDialogPreferenceFragment();
            if (getActivity() != null)
                ((ConfiguredProfilePreferencesDialogPreference)preference).profile_id = ((ProfilesPrefsActivity)getActivity()).profile_id;
            else
                ((ConfiguredProfilePreferencesDialogPreference)preference).profile_id = 0;
            dialogFragment = ((ConfiguredProfilePreferencesDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof DefaultSIMDialogPreference)
        {
            ((DefaultSIMDialogPreference)preference).fragment = new DefaultSIMDialogPreferenceFragment();
            dialogFragment = ((DefaultSIMDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof LiveWallpapersDialogPreference)
        {
            ((LiveWallpapersDialogPreference)preference).fragment = new LiveWallpapersDialogPreferenceFragment();
            dialogFragment = ((LiveWallpapersDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof VPNDialogPreference)
        {
            ((VPNDialogPreference)preference).fragment = new VPNDialogPreferenceFragment();
            dialogFragment = ((VPNDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof VibrationIntensityPreference)
        {
            ((VibrationIntensityPreference)preference).fragment = new VibrationIntensityPreferenceFragment();
            dialogFragment = ((VibrationIntensityPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }

        if (dialogFragment != null)
        {
            if ((getActivity() != null) && (!getActivity().isFinishing())) {
                FragmentManager fragmentManager = getParentFragmentManager();//getFragmentManager();
                //if (fragmentManager != null) {
                //noinspection deprecation
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(fragmentManager, PPApplication.PACKAGE_NAME + ".ProfilesPrefsActivity.DIALOG");
                //}
            }
        }
        else
        {
            super.onDisplayPreferenceDialog(preference);
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() == null)
            return;

        final Context context = getActivity().getBaseContext();

        // must be used handler for rewrite toolbar title/subtitle
        final ProfilesPrefsFragment fragment = this;
        Handler handler = new Handler(getActivity().getMainLooper());
        handler.postDelayed(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ProfilesPrefsFragment.onActivityCreated");
            if (getActivity() == null)
                return;

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            final String profileName = preferences.getString(Profile.PREF_PROFILE_NAME, "");
            Toolbar toolbar = getActivity().findViewById(R.id.activity_preferences_toolbar);
            if (nestedFragment) {
                toolbar.setTitle(fragment.getPreferenceScreen().getTitle());
                toolbar.setSubtitle(getString(R.string.profile_string_0) + ": " + profileName);
            } else {
                toolbar.setTitle(getString(R.string.title_activity_profile_preferences));
                toolbar.setSubtitle(getString(R.string.profile_string_0) + ": " + profileName);
            }

        }, 200);

        setDivider(null); // this remove dividers for categories

        /*
        if (savedInstanceState != null) {
            //startupSource = savedInstanceState.getInt("startupSource", PPApplication.PREFERENCES_STARTUP_SOURCE_ACTIVITY);
        }
        */

        setCategorySummary("prf_pref_activationDurationCategoryRoot", context);
        setCategorySummary("prf_pref_soundProfileCategoryRoot", context);
        setCategorySummary("prf_pref_volumeCategoryRoot", context);
        setCategorySummary("prf_pref_soundsCategoryRoot", context);
        setCategorySummary("prf_pref_touchEffectsCategoryRoot", context);
        setCategorySummary("prf_pref_vibrationIntensityCategoryRoot", context);
        setCategorySummary("prf_pref_radiosCategoryRoot", context);
        setCategorySummary("prf_pref_screenCategoryRoot", context);
        setCategorySummary("prf_pref_ledAccessoriesCategoryRoot", context);
        setCategorySummary("prf_pref_othersCategoryRoot", context);
        setCategorySummary("prf_pref_applicationCategoryRoot", context);
        setCategorySummary(PREF_FORCE_STOP_APPLICATIONS_CATEGORY, context);
        setCategorySummary(PREF_LOCK_DEVICE_CATEGORY, context);
        setCategorySummary(PREF_PROFILE_DEVICE_RADIOS_DUAL_SIM_SUPPORT_CATEGORY_ROOT, context);
        setCategorySummary(PREF_PROFILE_SOUNDS_DUAL_SIM_SUPPORT_CATEGORY_ROOT, context);
        setCategorySummary(PREF_DEVICE_WALLPAPER_CATEGORY, context);

        setRedTextToPreferences();

        //if (android.os.Build.VERSION.SDK_INT >= 21)
        //{
        PPListPreference ringerModePreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_RINGER_MODE);
            /*if (ringerModePreference.findIndexOfValue("5") < 0) {
                // add zen mode option to preference Ringer mode
                CharSequence[] entries = ringerModePreference.getEntries();
                CharSequence[] entryValues = ringerModePreference.getEntryValues();

                CharSequence[] newEntries = new CharSequence[entries.length + 1];
                CharSequence[] newEntryValues = new CharSequence[entries.length + 1];

                for (int i = 0; i < entries.length; i++) {
                    newEntries[i] = entries[i];
                    newEntryValues[i] = entryValues[i];
                }

                newEntries[entries.length] = context.getString(R.string.array_pref_ringerModeArray_ZenMode);
                newEntryValues[entries.length] = "5";

                ringerModePreference.setEntries(newEntries);
                ringerModePreference.setEntryValues(newEntryValues);
                ringerModePreference.setValue(Integer.toString(profile._volumeRingerMode));
                setSummary(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, profile._volumeRingerMode);
            }
            */

            /*final boolean canEnableZenMode =
                    (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                     (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                    );*/
        //final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context.getApplicationContext());

        /*ListPreference zenModePreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
        if (zenModePreference != null) {
            String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "");
            zenModePreference.setEnabled((value.equals("5")) && canEnableZenMode);
        }*/

        if (ringerModePreference != null) {
            CharSequence[] entries;
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if ((vibrator != null) && vibrator.hasVibrator()) {
                entries = ringerModePreference.getEntries();
                if (!entries[1].toString().contains(getString(R.string.array_pref_soundModeArray_ZenModeM_Off)))
                    entries[1] = entries[1] + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_Off) + ")";
                if (!entries[2].toString().contains(getString(R.string.array_pref_soundModeArray_ZenModeM_Off)))
                    entries[2] = entries[2] + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_Off) + ")";
                if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                        (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI)) {
                    if (!entries[3].toString().contains(getString(R.string.array_pref_soundModeArray_ZenModeM_Off)))
                        entries[3] = entries[3] + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_Off) + ")";
                }
                else {
                    if (!entries[3].toString().contains(getString(R.string.array_pref_soundModeArray_ZenModeM_On)))
                        entries[3] = entries[3] + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_On) + ")";
                }
            }
            else {
                ringerModePreference.setEntries(R.array.soundModeNotVibratorArray);
                ringerModePreference.setEntryValues(R.array.soundModeNotVibratorValues);
                entries = ringerModePreference.getEntries();
                if (!entries[1].toString().contains(getString(R.string.array_pref_soundModeArray_ZenModeM_Off)))
                    entries[1] = entries[1] + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_Off) + ")";
                if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                        (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI)) {
                    if (!entries[2].toString().contains(getString(R.string.array_pref_soundModeArray_ZenModeM_Off)))
                        entries[2] = entries[2] + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_Off) + ")";
                }
                else {
                    if (!entries[2].toString().contains(getString(R.string.array_pref_soundModeArray_ZenModeM_On)))
                        entries[2] = entries[2] + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_On) + ")";
                }
            }
            ringerModePreference.setEntries(entries);
            setSummary(Profile.PREF_PROFILE_VOLUME_RINGER_MODE);

            PPListPreference zenModePreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
            if (zenModePreference != null) {
                if (!((vibrator != null) && vibrator.hasVibrator())) {
                    zenModePreference.setEntries(R.array.zenModeNotVibratorArray);
                    zenModePreference.setEntryValues(R.array.zenModeNotVibratorValues);
                }
            }

            ringerModePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                String sNewValue = (String) newValue;
                int iNewValue;
                if (sNewValue.isEmpty())
                    iNewValue = 0;
                else
                    iNewValue = Integer.parseInt(sNewValue);

                /*final boolean canEnableZenMode =
                        (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                                (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                        );*/
                final boolean canEnableZenMode1 = ActivateProfileHelper.canChangeZenMode(context.getApplicationContext());

                PPListPreference _zenModePreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
                if (_zenModePreference != null) {
                    _zenModePreference.setEnabled((iNewValue == 5) && canEnableZenMode1);

                    GlobalGUIRoutines.setPreferenceTitleStyleX(_zenModePreference, true, false, false, false, false);

                    Preference zenModePreferenceInfo = prefMng.findPreference("prf_pref_volumeZenModeInfo");
                    if (zenModePreferenceInfo != null) {
                        zenModePreferenceInfo.setEnabled(_zenModePreference.isEnabled());
                    }
                }

                return true;
            });
        }
        /*}
        else
        {
            // remove zen mode preferences from preferences screen
            // for Android version < 5.0 this is not supported
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null)
            {
                PreferenceScreen preferenceCategory = (PreferenceScreen) findPreference("prf_pref_soundProfileCategory");
                preferenceCategory.removePreference(preference);
            }
        }*/
        if (Build.VERSION.SDK_INT != 23) {
            Preference preference = prefMng.findPreference("prf_pref_volumeVibrateWhenRingingRootInfo");
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("prf_pref_soundProfileCategory");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }
        if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) {
            PPListPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null)
            {
                preference.setTitle("(S)(R) "+getString(R.string.profile_preferences_vibrateWhenRinging));
                preference.setDialogTitle("(S)(R) "+getString(R.string.profile_preferences_vibrateWhenRinging));
                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, "");
                setSummary(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, value);
            }
        }

        PPListPreference vibrateNotificationsPreference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS);
        if (vibrateNotificationsPreference != null)
        {
            vibrateNotificationsPreference.setTitle("(S)(R) "+getString(R.string.profile_preferences_vibrateNotifications));
            vibrateNotificationsPreference.setDialogTitle("(S)(R) "+getString(R.string.profile_preferences_vibrateNotifications));
            String value = preferences.getString(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, "");
            setSummary(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, value);
        }

        PreferenceAllowed _preferenceAllowed = new PreferenceAllowed();
        PreferenceAllowed.isProfileCategoryAllowed_PREF_PROFILE_VIBRATION_INTENSITY(_preferenceAllowed, context);
        if (_preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            VibrationIntensityPreference vibrationIntensityPreference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING);
            if (vibrationIntensityPreference != null) {
                vibrationIntensityPreference.setTitle("(S)(R) " + getString(R.string.profile_preferences_vibrationIntensityRinging));
                vibrationIntensityPreference.setDialogTitle("(S)(R) " + getString(R.string.profile_preferences_vibrationIntensityRinging));
                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING, "-1|1");
                setSummary(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING, value);
            }
            vibrationIntensityPreference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS);
            if (vibrationIntensityPreference != null) {
                vibrationIntensityPreference.setTitle("(S)(R) " + getString(R.string.profile_preferences_vibrationIntensityNotificatiions));
                vibrationIntensityPreference.setDialogTitle("(S)(R) " + getString(R.string.profile_preferences_vibrationIntensityNotificatiions));
                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS, "-1|1");
                setSummary(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS, value);
            }
            vibrationIntensityPreference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION);
            if (vibrationIntensityPreference != null) {
                vibrationIntensityPreference.setTitle("(S)(R) " + getString(R.string.profile_preferences_vibrationIntensityTouchInteraction));
                vibrationIntensityPreference.setDialogTitle("(S)(R) " + getString(R.string.profile_preferences_vibrationIntensityTouchInteraction));
                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, "-1|1");
                setSummary(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, value);
            }
        }

        if (android.os.Build.VERSION.SDK_INT < 26) {
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
            if (preference != null)
            {
                preference.setTitle("(R) "+getString(R.string.profile_preferences_deviceAirplaneMode));
                String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, "");
                setSummary(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, value);
            }
        }
        /*if (android.os.Build.VERSION.SDK_INT >= 26) {
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WIFI_AP);
            if (preference != null)
            {
                preference.setTitle("(R) "+getString(R.string.profile_preferences_deviceWiFiAP));
                String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP, "");
                setSummary(Profile.PREF_PROFILE_DEVICE_WIFI_AP, value);
            }
        }*/
        if (PPApplication.HAS_FEATURE_TELEPHONY)
        {
            fillDeviceNetworkTypePreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, context);

            if (Build.VERSION.SDK_INT >= 26) {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    int phoneCount = telephonyManager.getPhoneCount();
                    if (phoneCount > 1) {
                        fillDeviceNetworkTypePreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, context);
                        fillDeviceNetworkTypePreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, context);
                    }
                }
            }
        }

        DurationDialogPreference durationPreference = prefMng.findPreference(Profile.PREF_PROFILE_DURATION);
        if (durationPreference != null)
        {
            durationPreference.setTitle(context.getString(R.string.profile_preferences_duration));
            durationPreference.setDialogTitle(context.getString(R.string.profile_preferences_duration));
            String value = preferences.getString(Profile.PREF_PROFILE_DURATION, "");
            setSummary(Profile.PREF_PROFILE_DURATION, value);
        }

        Preference preference;

        preference = prefMng.findPreference(Profile.PREF_PROFILE_ASK_FOR_DURATION);
        if (preference != null) {
            preference.setTitle("[M] " + getString(R.string.profile_preferences_askForDuration));
        }

        preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);
        if (preference != null) {
            preference.setOnPreferenceClickListener(preference1 -> {
                // start preferences activity for default profile
                if (getActivity() != null) {
                    Intent intent = new Intent(getActivity().getBaseContext(), PhoneProfilesPrefsActivity.class);
                    intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "categorySystemRoot");
                    //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    //noinspection deprecation
                    getActivity().startActivityForResult(intent, RESULT_UNLINK_VOLUMES_APP_PREFERENCES);
                }
                return false;
            });
        }

        InfoDialogPreference infoDialogPreference = prefMng.findPreference("prf_pref_preferenceTypesInfo");
        if (infoDialogPreference != null) {
            infoDialogPreference.setInfoText(
                    /*
                    "• " + getString(R.string.important_info_profile_install_pppps)+"\n\n"+
                    // [0=tab of Important info, 1=fragment (0=System, 1=Profiles, 2=Events), TextView from Important info]
                    "<II0 [0,1,"+R.id.activity_info_notification_profile_pppps_howTo_1+"]>"+
                    getString(R.string.profile_preferences_types_G1_show_info)+ " \u21D2"+
                    "<II0/>"+
                    "\n\n"+

                    "• " + getString(R.string.important_info_profile_grant)+"\n\n"+
                    // [0=tab of Important info, 1=fragment (0=System, 1=Profiles, 2=Events), TextView from Important info]
                    "<II1 [0,1,"+R.id.activity_info_notification_profile_grant_1_howTo_1+"]>"+
                    getString(R.string.profile_preferences_types_G1_show_info)+ " \u21D2"+
                    "<II1/>"+
                    "\n\n"+

                    "• " + getString(R.string.important_info_profile_root)+"\n\n"+
                    //"• " + getString(R.string.important_info_profile_settings)+"\n\n"+
                    "• " + getString(R.string.important_info_profile_interactive)
                    */

                    "<ul>" +
                            "<li>" + getString(R.string.important_info_profile_install_pppps) + "<br><br>" +
                            "<a href='" + InfoDialogPreference.ACTIVITY_IMPORTANT_INFO_PROFILES + "__" +
                            R.id.activity_info_notification_profile_pppps_howTo_1 + "'>" +
                            getString(R.string.profile_preferences_types_G1_show_info) + " &#8658;</a><br><br>" +
                            "</li>" +
                            "<li>" + getString(R.string.important_info_profile_grant) + "<br><br>" +
                            "<a href='" + InfoDialogPreference.ACTIVITY_IMPORTANT_INFO_PROFILES + "__" +
                            R.id.activity_info_notification_profile_grant_1_howTo_1 + "'>" +
                            getString(R.string.profile_preferences_types_G1_show_info) + " &#8658;</a><br><br>" +
                            "</li>" +
                            "<li>" + getString(R.string.important_info_profile_root) + "<br><br>" +
                            "</li>" +
                            "<li>" + getString(R.string.important_info_profile_interactive) +
                            "</li>" +
                            /*
                            "<li>" + "<a href=mailto:henrich.gron@gmail.com>E-mail to Henrisko &#8658;</a><br>" +
                            "</li>" +
                            "<li>" + "<a href=" + PPApplication.HELP_WIFI_SCAN_THROTTLING + "> Show web aboyt wi-fi throttling &#8658;</a>" +
                            */
                            "</ul>"
            );
            infoDialogPreference.setIsHtml(true);
        }

        Preference showInActivatorPreference = prefMng.findPreference(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR);
        if (showInActivatorPreference != null) {
            showInActivatorPreference.setTitle(/*"[A] " + */getString(R.string.profile_preferences_showInActivator));
            boolean value = preferences.getBoolean(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, false);
            setSummary(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR, value);
        }

        Preference extenderPreference = prefMng.findPreference(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(preference12 -> {
                installExtender();
                return false;
            });
        }
        Preference accessibilityPreference = prefMng.findPreference(PREF_FORCE_STOP_APPLICATIONS_ACCESSIBILITY_SETTINGS);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(preference13 -> {
                enableExtender();
                return false;
            });
        }

        /*
        boolean toneInstalled = TonesHandler.isToneInstalled(TonesHandler.TONE_ID, getActivity().getApplicationContext());
        if (!toneInstalled) {
            Preference installTonePreference = prefMng.findPreference(PREF_INSTALL_SILENT_TONE);
            if (installTonePreference != null) {
                installTonePreference.setSummary(R.string.profile_preferences_installSilentTone_summary);
                installTonePreference.setEnabled(true);
                installTonePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (!TonesHandler.isToneInstalled(TonesHandler.TONE_ID, context.getApplicationContext()))
                            TonesHandler.installTone(TonesHandler.TONE_ID, TonesHandler.TONE_NAME, context.getApplicationContext());
                        else {
                            Toast msg = ToastCompat.makeText(context.getApplicationContext(),
                                    context.getString(R.string.profile_preferences_installSilentTone_installed_summary),
                                    Toast.LENGTH_SHORT);
                            msg.show();
                        }
                        return false;
                    }
                });
            }
        }
        else {
            Preference installTonePreference = prefMng.findPreference(PREF_INSTALL_SILENT_TONE);
            if (installTonePreference != null) {
                installTonePreference.setSummary(R.string.profile_preferences_installSilentTone_installed_summary);
                installTonePreference.setEnabled(false);
            }
        }
        */

        extenderPreference = prefMng.findPreference(PREF_LOCK_DEVICE_INSTALL_EXTENDER);
        if (extenderPreference != null) {
            //extenderPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            extenderPreference.setOnPreferenceClickListener(preference14 -> {
                installExtender();
                return false;
            });
        }
        accessibilityPreference = prefMng.findPreference(PREF_LOCK_DEVICE_ACCESSIBILITY_SETTINGS);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(preference15 -> {
                enableExtender();
                return false;
            });
        }
        accessibilityPreference = prefMng.findPreference(PREF_FORCE_STOP_APPLICATIONS_LAUNCH_EXTENDER);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(preference16 -> {
                if (PPPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_LATEST) {
                    PackageManager packageManager = context.getPackageManager();
                    Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.PACKAGE_NAME_EXTENDER);
                    if (intent != null) {
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                }
                else {
                    if (getActivity() != null) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                getString(R.string.event_preferences_applications_LaunchExtender_title),
                                getString(R.string.event_preferences_extender_not_installed),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                getActivity()
                        );

                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                }
                return false;
            });
        }
        accessibilityPreference = prefMng.findPreference(PREF_LOCK_DEVICE_LAUNCH_EXTENDER);
        if (accessibilityPreference != null) {
            //accessibilityPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            accessibilityPreference.setOnPreferenceClickListener(preference17 -> {
                if (PPPExtenderBroadcastReceiver.isExtenderInstalled(context) >= PPApplication.VERSION_CODE_EXTENDER_LATEST) {
                    PackageManager packageManager = context.getPackageManager();
                    Intent intent = packageManager.getLaunchIntentForPackage(PPApplication.PACKAGE_NAME_EXTENDER);
                    if (intent != null) {
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                }
                else {
                    if (getActivity() != null) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                getString(R.string.event_preferences_applications_LaunchExtender_title),
                                getString(R.string.event_preferences_extender_not_installed),
                                getString(android.R.string.ok),
                                null,
                                null, null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                true, true,
                                false, false,
                                true,
                                getActivity()
                        );

                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                }
                return false;
            });
        }

        if (Build.VERSION.SDK_INT >= 29) {
            //if (Build.VERSION.SDK_INT < 30) {
                preference = findPreference("prf_pref_deviceWiFiAPInfo");
                if (preference != null) {
                    preference.setSummary(getString(R.string.profile_preferences_deviceWiFiAPInfo_summary) +
                        "\n" + getString(R.string.profile_preferences_deviceWiFiAPInfo2_summary) +
                        "\n" + getString(R.string.profile_preferences_deviceWiFiAPInfo_2_summary));
                }
            //}
            preference = findPreference("prf_pref_deviceCloseAllApplicationsInfo");
            if (preference != null) {
                preference.setSummary(getString(R.string.profile_preferences_deviceCloseAllApplicationsInfo_summary) + "\n" + getString(R.string.profile_preferences_deviceWiFiAPInfo2_summary));
            }
        }

        preference = findPreference(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND);
        if (preference != null) {
            preference.setSummary(getString(R.string.profile_preferences_volumeMuteSound_summary)+". "+
                    getString(R.string.profile_preferences_volumeMuteSound_summary_2));
        }

        preference = findPreference(PREF_NOTIFICATION_LED_INFO);
        if (preference != null) {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_NOTIFICATION_LED, null, preferences, true, context);
            preference.setEnabled((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                    ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
        }
        preference = findPreference(PREF_ALWAYS_ON_DISPLAY_INFO);
        if (preference != null) {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY, null, preferences, true, context);
            preference.setEnabled((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                    ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
        }
        preference = findPreference(PREF_SCREEN_DARK_MODE_INFO);
        if (preference != null) {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SCREEN_DARK_MODE, null, preferences, true, context);
            preference.setEnabled((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                    ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                     (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
        }

        if (Build.VERSION.SDK_INT >= 26) {
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                if (telephonyManager.getPhoneCount() > 1) {

                    preference = findPreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowedSIM1 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, null, preferences, true, context);
                        preference.setEnabled((preferenceAllowedSIM1.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                            ((preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                             (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                             (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                             (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                    }
                    preference = findPreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowedSIM2 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, null, preferences, true, context);
                        preference.setEnabled((preferenceAllowedSIM2.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                ((preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                    }

                    preference = findPreference(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowedSIM1 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, null, preferences, true, context);
                        preference.setEnabled((preferenceAllowedSIM1.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                ((preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                    }
                    preference = findPreference(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowedSIM2 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, null, preferences, true, context);
                        preference.setEnabled((preferenceAllowedSIM2.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                ((preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                    }

                    preference = findPreference(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS);
                    if (preference != null) {
                        PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, null, preferences, true, context);
                        preference.setEnabled((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                 (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                 (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                 (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                    }

                    PPListPreference listPreference = findPreference(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1);
                    if (listPreference != null) {
                        PreferenceAllowed preferenceAllowedSIM1 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, null, preferences, true, context);

                        listPreference.setTitle("(R) "+ getString(R.string.profile_preferences_deviceOnOff_SIM1));
                        listPreference.setDialogTitle("(R) "+getString(R.string.profile_preferences_deviceOnOff_SIM1));
                        String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, "");
                        setSummary(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, value);

                        listPreference.setEnabled((preferenceAllowedSIM1.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                ((preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                 (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                    }
                    listPreference = findPreference(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2);
                    if (listPreference != null) {
                        PreferenceAllowed preferenceAllowedSIM2 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, null, preferences, true, context);

                        listPreference.setTitle("(R) "+ getString(R.string.profile_preferences_deviceOnOff_SIM2));
                        listPreference.setDialogTitle("(R) "+getString(R.string.profile_preferences_deviceOnOff_SIM2));
                        String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, "");
                        setSummary(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, value);

                        listPreference.setEnabled((preferenceAllowedSIM2.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                ((preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                 (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                    }

                    if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                            (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                            (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI)) {
                        preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1);
                        if (preference != null) {
                            PreferenceAllowed preferenceAllowedSIM1 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, null, preferences, true, context);
                            preference.setEnabled((preferenceAllowedSIM1.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                    ((preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                     (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                     (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                     (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                        }

                        preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1);
                        if (preference != null) {
                            PreferenceAllowed preferenceAllowedSIM1 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1, null, preferences, true, context);
                            preference.setEnabled((preferenceAllowedSIM1.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                    ((preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                     (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                     (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                     (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                            disableDependedPref(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1);
                        }

                        preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2);
                        if (preference != null) {
                            PreferenceAllowed preferenceAllowedSIM2 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, null, preferences, true, context);
                            preference.setEnabled((preferenceAllowedSIM2.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                    ((preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                     (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                     (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                     (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                        }

                        preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2);
                        if (preference != null) {
                            PreferenceAllowed preferenceAllowedSIM2 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2, null, preferences, true, context);
                            preference.setEnabled((preferenceAllowedSIM2.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                    ((preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                     (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                     (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                     (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                            disableDependedPref(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2);
                        }

                        listPreference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1);
                        if (listPreference != null) {
                            PreferenceAllowed preferenceAllowedSIM1 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, null, preferences, true, context);

                            listPreference.setTitle("(S)(R) "+getString(R.string.profile_preferences_soundNotificationChangeSIM1));
                            listPreference.setDialogTitle("(S)(R) "+getString(R.string.profile_preferences_soundNotificationChangeSIM1));
                            String value = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, "");
                            setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, value);

                            listPreference.setEnabled((preferenceAllowedSIM1.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                    ((preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                     (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                     (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                     (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                        }

                        RingtonePreference ringtonePreference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1);
                        if (ringtonePreference != null) {
                            PreferenceAllowed preferenceAllowedSIM1 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1, null, preferences, true, context);

                            ringtonePreference.setTitle("(S)(R) "+getString(R.string.profile_preferences_soundNotificationSIM1));
                            ringtonePreference.setDialogTitle("(S)(R) "+getString(R.string.profile_preferences_soundNotificationSIM1));
                            String value = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1, "");
                            setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1, value);

                            ringtonePreference.setEnabled((preferenceAllowedSIM1.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                    ((preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                     (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                     (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                     (preferenceAllowedSIM1.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                            disableDependedPref(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1);
                        }

                        listPreference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2);
                        if (listPreference != null) {
                            PreferenceAllowed preferenceAllowedSIM2 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, null, preferences, true, context);

                            if (PPApplication.deviceIsHuawei && (PPApplication.romIsEMUI)) {
                                listPreference.setTitle("(R) " + getString(R.string.profile_preferences_soundNotificationChangeSIM2));
                                listPreference.setDialogTitle("(R) " + getString(R.string.profile_preferences_soundNotificationChangeSIM2));
                            } else {
                                listPreference.setTitle("(S)(R) " + getString(R.string.profile_preferences_soundNotificationChangeSIM2));
                                listPreference.setDialogTitle("(S)(R) " + getString(R.string.profile_preferences_soundNotificationChangeSIM2));
                            }
                            String value = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, "");
                            setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, value);

                            listPreference.setEnabled((preferenceAllowedSIM2.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                    ((preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                     (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                     (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                     (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                        }

                        ringtonePreference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2);
                        if (ringtonePreference != null) {
                            PreferenceAllowed preferenceAllowedSIM2 = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2, null, preferences, true, context);

                            if (PPApplication.deviceIsHuawei && (PPApplication.romIsEMUI)) {
                                ringtonePreference.setTitle("(R) " + getString(R.string.profile_preferences_soundNotificationSIM2));
                                ringtonePreference.setDialogTitle("(R) " + getString(R.string.profile_preferences_soundNotificationSIM2));
                            } else {
                                ringtonePreference.setTitle("(S)(R) " + getString(R.string.profile_preferences_soundNotificationSIM2));
                                ringtonePreference.setDialogTitle("(S)(R) " + getString(R.string.profile_preferences_soundNotificationSIM2));
                            }
                            String value = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2, "");
                            setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2, value);

                            ringtonePreference.setEnabled((preferenceAllowedSIM2.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                                    ((preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                                     (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                                     (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                                     (preferenceAllowedSIM2.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS)));
                            disableDependedPref(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2);
                        }
                    } else {
                        preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1);
                        if (preference != null)
                            preference.setVisible(false);
                        preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1);
                        if (preference != null)
                            preference.setVisible(false);
                        preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2);
                        if (preference != null)
                            preference.setVisible(false);
                        preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2);
                        if (preference != null)
                            preference.setVisible(false);
                        preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1);
                        if (preference != null)
                            preference.setVisible(false);
                        preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1);
                        if (preference != null)
                            preference.setVisible(false);
                        preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2);
                        if (preference != null)
                            preference.setVisible(false);
                        preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2);
                        if (preference != null)
                            preference.setVisible(false);
                    }

                } else {

                    preference = findPreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2);
                    if (preference != null)
                        preference.setVisible(false);

                    preference = findPreference(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2);
                    if (preference != null)
                        preference.setVisible(false);

                    preference = findPreference(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS);
                    if (preference != null)
                        preference.setVisible(false);

                    preference = findPreference(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2);
                    if (preference != null)
                        preference.setVisible(false);

                    preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2);
                    if (preference != null)
                        preference.setVisible(false);
                    preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2);
                    if (preference != null)
                        preference.setVisible(false);

                }
            } else {

                preference = findPreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2);
                if (preference != null)
                    preference.setVisible(false);

                preference = findPreference(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2);
                if (preference != null)
                    preference.setVisible(false);

                preference = findPreference(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS);
                if (preference != null)
                    preference.setVisible(false);

                preference = findPreference(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2);
                if (preference != null)
                    preference.setVisible(false);

                preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2);
                if (preference != null)
                    preference.setVisible(false);
                preference = findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2);
                if (preference != null)
                    preference.setVisible(false);

            }
        }

        if (!(PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI)) {
            preference = findPreference(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS);
            if (preference != null) {
                preference.setVisible(false);
            }
        }
        else {
            PPListPreference listPreference = findPreference(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS);
            if (listPreference != null) {
                listPreference.setTitle("(S)(R) "+getString(R.string.profile_preferences_soundSameRingtoneForBothSIMCards));
                listPreference.setDialogTitle("(S)(R) "+getString(R.string.profile_preferences_soundSameRingtoneForBothSIMCards));
                String value = preferences.getString(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, "");
                setSummary(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, value);
            }
        }

        if (PPApplication.deviceIsXiaomi || PPApplication.romIsMIUI) {
            preference = findPreference(PREF_PROFILE_DEVICE_RUN_APPLICATION_MIUI_PERMISSIONS);
            if (preference != null) {
                preference.setOnPreferenceClickListener(preference118 -> {
                    /*
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setTitle(preference118.getTitle());
                    dialogBuilder.setMessage(R.string.profile_preferences_deviceRunApplicationsShortcutsForMIU_dialod_message);
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(R.string.miui_permissions_alert_dialog_show, (dialog, which) -> {
                        boolean ok = false;
                        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                        intent.setClassName("com.miui.securitycenter",
                                "com.miui.permcenter.permissions.PermissionsEditorActivity");
                        intent.putExtra("extra_pkgname", PPApplication.PACKAGE_NAME);
                        if (GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext())) {
                            try {
                                startActivity(intent);
                                ok = true;
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                        if (!ok) {
                            AlertDialog.Builder dialogBuilder2 = new AlertDialog.Builder(getActivity());
                            dialogBuilder2.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder2.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder2.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog2 = dialogBuilder2.create();

//                            dialog2.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });

                            if (!getActivity().isFinishing())
                                dialog2.show();
                        }
                    });
                    dialogBuilder.setNegativeButton(android.R.string.cancel, null);
                    AlertDialog dialog = dialogBuilder.create();

//                        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                            @Override
//                            public void onShow(DialogInterface dialog) {
//                                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                if (positive != null) positive.setAllCaps(false);
//                                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                if (negative != null) negative.setAllCaps(false);
//                            }
//                        });
                    */

                    PPAlertDialog dialog = new PPAlertDialog(
                            preference118.getTitle(),
                            getString(R.string.profile_preferences_deviceRunApplicationsShortcutsForMIU_dialod_message),
                            getString(R.string.miui_permissions_alert_dialog_show),
                            getString(android.R.string.cancel),
                            null, null,
                            (dialog1, which) -> {
                                boolean ok = false;
                                Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                intent.setClassName("com.miui.securitycenter",
                                        "com.miui.permcenter.permissions.PermissionsEditorActivity");
                                intent.putExtra("extra_pkgname", PPApplication.PACKAGE_NAME);
                                if (GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext())) {
                                    try {
                                        startActivity(intent);
                                        ok = true;
                                    } catch (Exception e) {
                                        PPApplication.recordException(e);
                                    }
                                }
                                if (!ok) {
                                    PPAlertDialog dialog2 = new PPAlertDialog(
                                            preference118.getTitle(),
                                            getString(R.string.setting_screen_not_found_alert),
                                            getString(android.R.string.ok),
                                            null,
                                            null, null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            true, true,
                                            false, false,
                                            true,
                                            getActivity()
                                    );

                                    if (!getActivity().isFinishing())
                                        dialog2.show();
                                }
                            },
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            false,
                            getActivity()
                    );

                    if ((getActivity() != null) && (!getActivity().isFinishing()))
                        dialog.show();
                    return false;
                });
            }
        }
        else {
            preference = findPreference("prf_pref_deviceRunApplicationMIUIPermissions");
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("prf_pref_othersCategory");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }

        if (!PPApplication.deviceIsPixel) {
            preference = findPreference("prf_pref_volumeSoundModeVibrationInfo");
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("prf_pref_soundProfileCategory");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }

        /*
        infoDialogPreference = prefMng.findPreference("prf_pref_deviceVPNInfo");
        if (infoDialogPreference != null) {
            String url1 = "https://openvpn.net/vpn-server-resources/faq-regarding-openvpn-connect-android/#how-do-i-use-tasker-with-openvpn-connect-for-android";
            String url2 = "https://github.com/schwabe/ics-openvpn#controlling-from-external-apps";

            String infoText =
                    getString(R.string.profile_preferences_deviceVPNInfo_infoText)+"<br><br>"+
                            "<a href=" + url1 + ">OpenVPN Connect &#8658;</a>"+"<br><br>"+
                            "<a href=" + url2 + ">OpenVPN for Android &#8658;</a>";

            infoDialogPreference.setInfoText(infoText);
            infoDialogPreference.setIsHtml(true);
        }
        */

        preference = findPreference("prf_pref_deviceScreenTimeoutAndKeeepScreenOnInfo");
        if (preference != null) {
            String title = "\"" + getString(R.string.profile_preferences_deviceScreenTimeout) + "\" " +
                    getString(R.string.profile_preferences_deviceScreenTimeoutAndKeepScreenOn_title) +
                    " \"" + getString(R.string.profile_preferences_deviceScreenOnPermanent) + "\"";
            preference.setTitle(title);
            String summary = getString(R.string.profile_preferences_deviceScreenOnPermanent) + ": ";
            if (ApplicationPreferences.keepScreenOnPermanent)
                summary = summary + getString(R.string.profile_preferences_deviceScreenTimeoutAndKeepScreenOnInfo_summary_0_On);
            else
                summary = summary + getString(R.string.profile_preferences_deviceScreenTimeoutAndKeepScreenOnInfo_summary_0_Off);
            summary = summary + "\n\n";
            summary = summary + "\"" + getString(R.string.profile_preferences_deviceScreenTimeout) + "\" " +
                    getString(R.string.profile_preferences_deviceScreenTimeoutAndKeepScreenOnInfo_summary_1) +
                    " \"" + getString(R.string.profile_preferences_deviceScreenTimeoutAndKeepScreenOnInfo_summary_0_Off) + "\" " +
                    getString(R.string.profile_preferences_deviceScreenTimeoutAndKeepScreenOnInfo_summary_2) +
                    " \"" + getString(R.string.profile_preferences_deviceScreenOnPermanent) + "\". " +
                    getString(R.string.profile_preferences_deviceScreenTimeoutAndKeepScreenOnInfo_summary_3) +
                    " \"" + getString(R.string.profile_preferences_deviceScreenOnPermanent) + "\"=" +
                    "\"" + getString(R.string.array_pref_hardwareModeArray_off) + "\".";
            preference.setSummary(summary);
        }

        if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
            preference = prefMng.findPreference(PREF_PROFILE_DEVICE_BRIGHTNESS_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference116 -> {
                    Intent intent = new Intent(context, PhoneProfilesPrefsActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO, "specialProfileParametersCategoryRoot");
                    //intent.putExtra(PhoneProfilesPrefsActivity.EXTRA_SCROLL_TO_TYPE, "screen");
                    //noinspection deprecation
                    startActivityForResult(intent, RESULT_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON_SETTINGS);
                    return false;
                });
            }
        }
        else {
            preference = findPreference(PREF_PROFILE_DEVICE_BRIGHTNESS_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("prf_pref_screenCategory");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }

        Preference assistantPreference = prefMng.findPreference(PREF_PROFILE_DEVICE_AIRPLANE_MODE_ASSISTANT_SETTINGS);
        if (assistantPreference != null) {
            //assistantPreference.setWidgetLayoutResource(R.layout.start_activity_preference);
            assistantPreference.setOnPreferenceClickListener(preference13 -> {
                configureAssistant();
                return false;
            });
        }

        infoDialogPreference = prefMng.findPreference(PREF_PROFILE_AIRPLANE_MODE_RADIOS_INFO);
        if (infoDialogPreference != null) {

            String url;
            if (DebugVersion.enabled)
                url = PPApplication.HELP_AIRPLANE_MODE_RADIOS_CONFIG_DEVEL;
            else
                url = PPApplication.HELP_AIRPLANE_MODE_RADIOS_CONFIG;

            String infoText =
                    "<b>"+getString(R.string.profile_preferences_deviceAirplaneModeRadios_info1) + "</b><br><br>" +
                            getString(R.string.profile_preferences_deviceAirplaneModeRadios_info2) + " " +
                            getString(R.string.profile_preferences_deviceAirplaneModeRadios_info3) + ":<br>" +
                            "<a href=" + url + ">" + url+ " &#8658;</a><br><br>";

            String configuredRadios = Settings.Global.getString(context.getContentResolver(), "airplane_mode_radios");

            infoText = infoText + getString(R.string.profile_preferences_deviceAirplaneModeRadios_info4) + " " + configuredRadios;

            infoDialogPreference.setInfoText(infoText);
            infoDialogPreference.setIsHtml(true);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (!nestedFragment) {
            if (getActivity() == null)
                return;

            //final Context context = getActivity().getBaseContext();

            disableDependedPref(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            disableDependedPref(Profile.PREF_PROFILE_LOCK_DEVICE);
            setRedTextToPreferences();
//            PPApplication.logE("[PPP_NOTIFICATION] ProfilesPrefsFragment.onResume", "call of updateGUI");
            PPApplication.updateGUI(true, false, getActivity());
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);

            /*
            SharedPreferences.Editor editor = profilesPreferences.edit();
            updateSharedPreferences(editor, preferences);
            editor.apply();
            */

        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(Profile.PREF_PROFILE_NAME)) {
            String value = sharedPreferences.getString(key, "");
            if (getActivity() != null) {
                // must be used handler for rewrite toolbar title/subtitle
                final String _value = value;
                Handler handler = new Handler(getActivity().getMainLooper());
                handler.postDelayed(() -> {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=ProfilesPrefsFragment.onSharedPreferenceChanged");
                    if (getActivity() == null)
                        return;

                    Toolbar toolbar = getActivity().findViewById(R.id.activity_preferences_toolbar);
                    toolbar.setSubtitle(getString(R.string.profile_string_0) + ": " + _value);
                }, 200);
            }
        }

        if (getActivity() == null)
            return;

        String value;

        if (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR) ||
                key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
                key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE) ||
                key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON) ||
                key.endsWith(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND)) {
            boolean bValue = sharedPreferences.getBoolean(key, false);
            value = Boolean.toString(bValue);
        }
        else
        if (key.equals(Profile.PREF_PROFILE_END_OF_ACTIVATION_TIME)) {
            value = String.valueOf(sharedPreferences.getInt(key, 0));
        }
        else {
            if (prefMng.findPreference(key) != null)
                value = sharedPreferences.getString(key, "");
            else
                value = "";
        }
        setSummary(key, value);

        // disable depended preferences
        disableDependedPref(key, value);

        setRedTextToPreferences();

        ProfilesPrefsActivity activity = (ProfilesPrefsActivity)getActivity();
        if (activity != null) {
            activity.showSaveMenu = true;
            activity.invalidateOptionsMenu();
        }

    }

    void doOnActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_PROFILE)) {
            setRedTextToPreferences();
        }
        /*if (requestCode == Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_GRANT_ROOT) {
            PPApplication.isRootGranted();
            setRedTextToPreferences();
        }*/

        if ((requestCode == WallpaperViewPreference.RESULT_LOAD_IMAGE) && (resultCode == Activity.RESULT_OK) && (data != null))
        {
            //Uri selectedImage = data.getData();
            String  d = data.getDataString();
            if (d != null) {
                Uri selectedImage = Uri.parse(d);
                /*//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        ContentResolver resolver = getActivity().getContentResolver();
                        resolver.takePersistableUriPermission(selectedImage, takeFlags);
                    } catch (Exception e) {
                        Log.e("ProfilesPrefsFragment.doOnActivityResult", Log.getStackTraceString(e));
                    }
                //}*/
                WallpaperViewPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
                if (preference != null)
                    preference.setImageIdentifier(selectedImage.toString());
                /*
                if (ProfilesPrefsFragment.changedWallpaperViewPreference != null) {
                    // set image identifier for get bitmap path
                    ProfilesPrefsFragment.changedWallpaperViewPreference.setImageIdentifier(selectedImage.toString());
                    ProfilesPrefsFragment.changedWallpaperViewPreference = null;
                }
                */
            }
        }
        if ((requestCode == WallpaperFolderPreference.RESULT_GET_FOLDER) && (resultCode == Activity.RESULT_OK) && (data != null))
        {
            //Uri selectedImage = data.getData();
            String  d = data.getDataString();
            if (d != null) {
                Uri selectedFolder = Uri.parse(d);
                WallpaperFolderPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOLDER);
                if (preference != null)
                    preference.setWallpaperFolder(selectedFolder.toString());
            }
        }

        if ((requestCode == ProfileIconPreference.RESULT_LOAD_IMAGE) && (resultCode == Activity.RESULT_OK) && (data != null))
        {
            //Uri selectedImage = data.getData();
            String  d = data.getDataString();
            if (d != null) {
                Uri selectedImage = Uri.parse(d);
                /*//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        ContentResolver resolver = getActivity().getContentResolver();
                        resolver.takePersistableUriPermission(selectedImage, takeFlags);
                    } catch (Exception e) {
                        Log.e("ProfilesPrefsFragment.doOnActivityResult", Log.getStackTraceString(e));
                    }
                //}*/

                int height = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
                int width = GlobalGUIRoutines.dpToPx(GlobalGUIRoutines.ICON_SIZE_DP);
                if (BitmapManipulator.checkBitmapSize(selectedImage.toString(), width, height, getContext())) {
                    ProfileIconPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_ICON);
                    if (preference != null) {
                        preference.setImageIdentifierAndType(selectedImage.toString(), false);
                        preference.setValue(true);
                        preference.dismissDialog();
                    }
                    /*if (ProfilesPrefsFragment.changedProfileIconPreference != null) {
                        // set image identifier ant type for get bitmap path
                        ProfilesPrefsFragment.changedProfileIconPreference.dismissDialog();
                        ProfilesPrefsFragment.changedProfileIconPreference.setImageIdentifierAndType(selectedImage.toString(), false, true);
                        ProfilesPrefsFragment.changedProfileIconPreference = null;
                    }*/
                }
                else {
                    if (getActivity() != null) {
                        String text = getString(R.string.profileicon_pref_dialog_custom_icon_image_too_large);
                        text = text + " " + (width * BitmapManipulator.ICON_BITMAP_SIZE_MULTIPLIER);
                        text = text + "x" + (height * BitmapManipulator.ICON_BITMAP_SIZE_MULTIPLIER);
                        PPApplication.showToast(getActivity().getApplicationContext(), text, Toast.LENGTH_LONG);
                    }
                }
            }
        }
        if (requestCode == RESULT_NOTIFICATION_ACCESS_SETTINGS) {
            //final boolean canEnableZenMode =
            //        (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
            //                (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
            //        );

            final String sZenModeType = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, "");
            setSummary(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, sZenModeType);
        }
        if ((requestCode == RunApplicationsDialogPreference.RESULT_APPLICATIONS_EDITOR) && (resultCode == Activity.RESULT_OK) && (data != null))
        {
            RunApplicationsDialogPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            if (preference != null) {
                preference.updateShortcut(
                        data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT),
                        data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),
                        /*(Bitmap)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON),*/
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_POSITION, -1),
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, 0));
            }
            /*
            if (ProfilesPrefsFragment.applicationsDialogPreference != null) {
                ProfilesPrefsFragment.applicationsDialogPreference.updateShortcut(
                        (Intent)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT),
                        data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_POSITION, -1),
                        data.getIntExtra(LaunchShortcutActivity.EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, 0));

                ProfilesPrefsFragment.applicationsDialogPreference = null;
            }*/
        }
        if (requestCode == RunApplicationEditorDialog.RESULT_INTENT_EDITOR) {
            if (resultCode == Activity.RESULT_OK) {
                RunApplicationsDialogPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
                if ((preference != null) && (data != null)) {
                    preference.updateIntent(data.getParcelableExtra(RunApplicationEditorDialog.EXTRA_PP_INTENT),
                            data.getParcelableExtra(RunApplicationEditorDialog.EXTRA_APPLICATION),
                            data.getIntExtra(RunApplicationEditorIntentActivity.EXTRA_DIALOG_PREFERENCE_START_APPLICATION_DELAY, 0));
                }
            }
        }
        if (requestCode == RESULT_UNLINK_VOLUMES_APP_PREFERENCES) {
            setSummary(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);
            disableDependedPref(Profile.PREF_PROFILE_VOLUME_RINGTONE);
            disableDependedPref(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
        }
        if (requestCode == RESULT_ACCESSIBILITY_SETTINGS) {
            setSummary(PREF_FORCE_STOP_APPLICATIONS_ACCESSIBILITY_SETTINGS);
            setSummary(PREF_LOCK_DEVICE_ACCESSIBILITY_SETTINGS);
            disableDependedPref(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            disableDependedPref(Profile.PREF_PROFILE_LOCK_DEVICE);
            // show save menu
            ProfilesPrefsActivity activity = (ProfilesPrefsActivity)getActivity();
            if (activity != null) {
                activity.showSaveMenu = true;
                activity.invalidateOptionsMenu();
            }
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_IMAGE_WALLPAPER)) {
            WallpaperViewPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
            if (preference != null)
                preference.startGallery(); // image file
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_WALLPAPER_FOLDER)) {
            WallpaperFolderPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOLDER);
            if (preference != null)
                preference.startGallery(); // folder of images
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CUSTOM_PROFILE_ICON)) {
            ProfileIconPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_ICON);
            if (preference != null)
                preference.startGallery();
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_BRIGHTNESS_DIALOG)) {
            BrightnessDialogPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS);
            if (preference != null)
                preference.enableViews();
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_RINGTONE_PREFERENCE)) {
            RingtonePreference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE);
            if (preference != null)
                preference.refreshListView();
            preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
            if (preference != null)
                preference.refreshListView();
            preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_ALARM);
            if (preference != null)
                preference.refreshListView();
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_CONNECT_TO_SSID_DIALOG)) {
            ConnectToSSIDDialogPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID);
            if (preference != null)
                preference.refreshListView();
        }
        if (requestCode == RESULT_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON_SETTINGS) {
            setSummary(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS);
        }
        if (requestCode == RESULT_ASSISTANT_SETTINGS) {
            //disableDependedPref(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
            setSummary(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
            setSummary(PREF_PROFILE_DEVICE_AIRPLANE_MODE_ASSISTANT_SETTINGS);
            // show save menu
            ProfilesPrefsActivity activity = (ProfilesPrefsActivity)getActivity();
            if (activity != null) {
                activity.showSaveMenu = true;
                activity.invalidateOptionsMenu();
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //outState.putBoolean("nestedFragment", nestedFragment);
    }

    private void initPreferenceFragment(/*Bundle savedInstanceState*/) {
        prefMng = getPreferenceManager();

        preferences = prefMng.getSharedPreferences();

        /*

        if (savedInstanceState == null) {
            if (getContext() != null) {
                profilesPreferences = getContext().getSharedPreferences(PREFS_NAME_ACTIVITY, Activity.MODE_PRIVATE);

                SharedPreferences.Editor editor = preferences.edit();
                updateSharedPreferences(editor, profilesPreferences);
                editor.apply();
            }
        }
        */

        if (preferences != null)
            preferences.registerOnSharedPreferenceChangeListener(this);
    }

    /*
    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
    }
    */

    private boolean notGrantedG1Permission;
    private boolean notRootedOrGrantetRoot;
    private boolean notInstalledPPPPS;

    private String getCategoryTitleWhenPreferenceChanged(String key, int preferenceTitleId,
                                                         Context context) {
        //Preference preference = prefMng.findPreference(key);
        String title = "";
        PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
        boolean _notGrantedG1Permission =
                (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION);
        boolean _notRootedOrGrantedRoot =
                (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                        ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                         (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED));
        boolean _notDefaultAssistant =
                (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SET_AS_ASSISTANT);
        boolean _notInstalledPPPS =
                (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED) &&
                        (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS);
        if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                _notGrantedG1Permission ||
                _notRootedOrGrantedRoot ||
                _notDefaultAssistant ||
                _notInstalledPPPS) {
            switch (key) {
                case Profile.PREF_PROFILE_ASK_FOR_DURATION:
                case Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE:
                case Profile.PREF_PROFILE_VOLUME_MUTE_SOUND:
                //case Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR:
                    /*boolean defaultValue =
                            getResources().getBoolean(
                                    GlobalGUIRoutines.getResourceId(key, "bool", context));*/

                    boolean hasVibrator = true;
                    if (key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE)) {
                        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        hasVibrator = (vibrator != null) && vibrator.hasVibrator();
                    }

                    //noinspection ConstantConditions
                    boolean defaultValue = Profile.defaultValuesBoolean.get(key);
                    if (hasVibrator && preferences.getBoolean(key, defaultValue) != defaultValue) {
                        title = getString(preferenceTitleId);
                        notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                        notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                        notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                    }
                    break;
                case Profile.PREF_PROFILE_END_OF_ACTIVATION_TIME:
                    title = context.getString(R.string.profile_preferences_exactTime);
                    break;
                case Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE:
                    String defaultValueS = Profile.defaultValuesString.get(key);
                    if (!preferences.getString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, defaultValueS).equals(defaultValueS)) {
                        title = getString(preferenceTitleId);
                        notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                        notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                        notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                    }
                    break;
                default:
                    /*String defaultValue =
                            getResources().getString(
                                    GlobalGUIRoutines.getResourceId(key, "string", context));*/
                    defaultValueS = Profile.defaultValuesString.get(key);
                    String value = preferences.getString(key, defaultValueS);
                    if (value != null) {
                        switch (key) {
                            case Profile.PREF_PROFILE_VOLUME_RINGTONE:
                            case Profile.PREF_PROFILE_VOLUME_NOTIFICATION:
                            case Profile.PREF_PROFILE_VOLUME_MEDIA:
                            case Profile.PREF_PROFILE_VOLUME_ALARM:
                            case Profile.PREF_PROFILE_VOLUME_SYSTEM:
                            case Profile.PREF_PROFILE_VOLUME_VOICE:
                            case Profile.PREF_PROFILE_VOLUME_DTMF:
                            case Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY:
                            case Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO:
                                if (VolumeDialogPreference.changeEnabled(value)) {
                                    title = getString(preferenceTitleId);
                                    notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                                    notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                                    notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                                }
                                break;
                            case Profile.PREF_PROFILE_DEVICE_BRIGHTNESS:
                                if (BrightnessDialogPreference.changeEnabled(value)) {
                                    title = getString(preferenceTitleId);
                                    notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                                    notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                                    notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                                }
                                break;
                            case Profile.PREF_PROFILE_VOLUME_ZEN_MODE:
                                title = getString(preferenceTitleId);
                                notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                                notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                                notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                                break;
                            case Profile.PREF_PROFILE_GENERATE_NOTIFICATION:
                                if (GenerateNotificationDialogPreference.changeEnabled(value)) {
                                    title = getString(preferenceTitleId);
                                    notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                                    notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                                    notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                                }
                                break;
                            case Profile.PREF_PROFILE_DEVICE_VPN:
                                if (VPNDialogPreference.changeEnabled(value)) {
                                    title = getString(preferenceTitleId);
                                    notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                                    notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                                    notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                                }
                                break;
                            case Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING:
                            case Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS:
                            case Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION:
                                if (VibrationIntensityPreference.changeEnabled(value)) {
                                    //title = getString(preferenceTitleId);
                                    notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                                    notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                                    notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                                    if (!((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                                            PPApplication.deviceIsOnePlus)) {
                                        if (key.equals(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING))
                                            title = "(S)(R) " + getString(R.string.profile_preferences_vibrationIntensityRinging);
                                        else if (key.equals(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS))
                                            title = "(S)(R) " + getString(R.string.profile_preferences_vibrationIntensityNotificatiions);
                                        else/* if (key.equals(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION))*/
                                            title = "(S)(R) " + getString(R.string.profile_preferences_vibrationIntensityTouchInteraction);
                                    } else
                                        title = getString(preferenceTitleId);
                                }
                                break;
                            default:
                                if (!value.equals(defaultValueS)) {
                                    if (key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING) &&
                                            (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI))
                                        title = "(S)(R) " + getString(R.string.profile_preferences_vibrateWhenRinging);
                                    else if (key.equals(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS))
                                        title = "(S)(R) " + getString(R.string.profile_preferences_vibrateNotifications);
                                    else if (key.equals(Profile.PREF_PROFILE_DURATION))
                                        title = context.getString(R.string.profile_preferences_duration);
                                    else
                                        title = getString(preferenceTitleId);
                                    notGrantedG1Permission = notGrantedG1Permission || _notGrantedG1Permission;
                                    notRootedOrGrantetRoot = notRootedOrGrantetRoot || _notRootedOrGrantedRoot;
                                    notInstalledPPPPS = notInstalledPPPPS || _notInstalledPPPS;
                                }
                                break;
                        }
                    }
                    break;
            }
        }
        return title;
    }

    private static class CattegorySummaryData {
        String summary;
        boolean permissionGranted;
        boolean forceSet = false;
        boolean bold = false;
        boolean accessibilityEnabled = true;
        boolean defaultAssistantSet = true;
    }

    private boolean setCategorySummaryActivationDuration(Context context,
                                                         Preference preferenceScreen,
                                                         CattegorySummaryData cattegorySummaryData) {
        String title;
        String askForDurationTitle = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_ASK_FOR_DURATION, R.string.profile_preferences_askForDuration, context);
        if (askForDurationTitle.isEmpty()) {
            String value = preferences.getString(Profile.PREF_PROFILE_END_OF_ACTIVATION_TYPE, Profile.defaultValuesString.get(Profile.PREF_PROFILE_END_OF_ACTIVATION_TYPE));
            if (value.equals("0")) {
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION, R.string.profile_preferences_duration, context);
                String afterDurationDoTitle = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_AFTER_DURATION_DO, R.string.profile_preferences_afterDurationDo, context);
                if (!title.isEmpty()) {
                    cattegorySummaryData.bold = true;
                    value = preferences.getString(Profile.PREF_PROFILE_DURATION, Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION));
                    if (value != null) {
                        value = StringFormatUtils.getDurationString(Integer.parseInt(value));
                        cattegorySummaryData.summary = cattegorySummaryData.summary + title +
                                ": <b>" +
                                ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_activationDurationCategoryRoot", context)
                                + "</b> • ";

                        String afterDurationDoValue = preferences.getString(Profile.PREF_PROFILE_AFTER_DURATION_DO,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_AFTER_DURATION_DO));
                        value = StringFormatUtils.getListPreferenceString(afterDurationDoValue,
                                R.array.afterProfileDurationDoValues, R.array.afterProfileDurationDoArray, context);
                        cattegorySummaryData.summary = cattegorySummaryData.summary + afterDurationDoTitle + ": <b>" +
                                ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_activationDurationCategoryRoot", context)
                                + "</b>";

                        if ((afterDurationDoValue != null) && afterDurationDoValue.equals(String.valueOf(Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE))) {
                            DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
                            long profileId = Long.parseLong(preferences.getString(Profile.PREF_PROFILE_AFTER_DURATION_PROFILE, String.valueOf(Profile.PROFILE_NO_ACTIVATE)));
                            Profile profile = dataWrapper.getProfileById(profileId, false, false, false);
                            if (profile != null)
                                value = profile._name;
                            else {
                                if (profileId == Profile.PROFILE_NO_ACTIVATE)
                                    value = context.getString(R.string.profile_preference_profile_end_no_activate);
                            }
                            String _title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_AFTER_DURATION_PROFILE, R.string.profile_preferences_afterDurationProfile, context);
                            cattegorySummaryData.summary = cattegorySummaryData.summary + " • " + _title + ": <b>" +
                                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_activationDurationCategoryRoot", context)
                                    + "</b>";
                        }
                    } else
                        cattegorySummaryData.summary = cattegorySummaryData.summary + afterDurationDoTitle;
                }
            } else {
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_END_OF_ACTIVATION_TIME, R.string.profile_preferences_exactTime, context);
                String afterDurationDoTitle = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_AFTER_DURATION_DO, R.string.profile_preferences_afterExactTimeDo, context);
                if (!title.isEmpty()) {
                    cattegorySummaryData.bold = true;
                    //noinspection ConstantConditions
                    int iValue = preferences.getInt(Profile.PREF_PROFILE_END_OF_ACTIVATION_TIME, Integer.parseInt(Profile.defaultValuesString.get(Profile.PREF_PROFILE_END_OF_ACTIVATION_TIME)));
                    value = String.valueOf(iValue);
                    //if (value != null) {
                    value = StringFormatUtils.getTimeString(Integer.parseInt(value));
                    cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                            ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_activationDurationCategoryRoot", context)
                            + "</b> • ";

                    String afterDurationDoValue = preferences.getString(Profile.PREF_PROFILE_AFTER_DURATION_DO,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_AFTER_DURATION_DO));
                    value = StringFormatUtils.getListPreferenceString(afterDurationDoValue,
                            R.array.afterProfileDurationDoValues, R.array.afterProfileDurationDoArray, context);
                    cattegorySummaryData.summary = cattegorySummaryData.summary + afterDurationDoTitle + ": <b>" +
                            ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_activationDurationCategoryRoot", context)
                            + "</b>";

                    if ((afterDurationDoValue != null) && afterDurationDoValue.equals(String.valueOf(Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE))) {
                        DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false, DataWrapper.IT_FOR_EDITOR, 0, 0f);
                        long profileId = Long.parseLong(preferences.getString(Profile.PREF_PROFILE_AFTER_DURATION_PROFILE, String.valueOf(Profile.PROFILE_NO_ACTIVATE)));
                        Profile profile = dataWrapper.getProfileById(profileId, false, false, false);
                        if (profile != null)
                            value = profile._name;
                        else {
                            if (profileId == Profile.PROFILE_NO_ACTIVATE)
                                value = context.getString(R.string.profile_preference_profile_end_no_activate);
                        }
                        String _title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_AFTER_DURATION_PROFILE, R.string.profile_preferences_afterDurationProfile, context);
                        cattegorySummaryData.summary = cattegorySummaryData.summary + " • " + _title + ": <b>" +
                                ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_activationDurationCategoryRoot", context)
                                + "</b>";
                    }
                    //} else
                    //    cattegorySummaryData.summary = cattegorySummaryData.summary + afterDurationDoTitle;
                }
            }
        }
        else {
            cattegorySummaryData.bold = true;
            askForDurationTitle = "[M] " + askForDurationTitle;
            cattegorySummaryData.summary = cattegorySummaryData.summary + askForDurationTitle + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(getString(R.string.profile_preferences_enabled), prefMng, "prf_pref_activationDurationCategoryRoot", context)
                    + "</b>";
        }
        if (cattegorySummaryData.bold) {
            // any of duration preferences are set
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND, R.string.profile_preferences_durationNotificationSound, context);
            if (!title.isEmpty()) {
                if (!cattegorySummaryData.summary.isEmpty())
                    cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";
                cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                        ProfileStatic.getColorForChangedPreferenceValue("<ringtone_name>", prefMng, "prf_pref_activationDurationCategoryRoot", context)
                        + "</b>";
            }

            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if ((vibrator != null) && vibrator.hasVibrator()) {
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE, R.string.profile_preferences_durationNotificationVibrate, context);
                if (!title.isEmpty()) {
                    if (!cattegorySummaryData.summary.isEmpty())
                        cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";
                    cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                            ProfileStatic.getColorForChangedPreferenceValue(getString(R.string.profile_preferences_enabled), prefMng, "prf_pref_activationDurationCategoryRoot", context)
                            + "</b>";
                }
            }

            setRingtonePreferenceSummary(cattegorySummaryData.summary,
                    preferences.getString(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND)),
                    preferenceScreen, context);
            GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, cattegorySummaryData.bold, false, false, false);
            return true;
        }
        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummarySoundProfile(Context context,
                                                   CattegorySummaryData cattegorySummaryData) {
        String ringerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE,
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_RINGER_MODE));
        String zenMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE,
                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_ZEN_MODE));
        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, R.string.profile_preferences_volumeSoundMode, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;

            String value = StringFormatUtils.getListPreferenceString(ringerMode,
                    R.array.soundModeValues, R.array.soundModeArray, context);

            if (ringerMode != null) {
                boolean zenModeOffValue = ringerMode.equals("1") || ringerMode.equals("2") || ringerMode.equals("3");
                    /*if (Build.VERSION.SDK_INT < 23) {
                        if (zenModeOffValue)
                            value = value + " (" + getString(R.string.array_pref_soundModeArray_ZenModeL_Off) + ")";
                        else if (ringerMode.equals("4"))
                            value = value + " (" + getString(R.string.array_pref_soundModeArray_ZenModeL_On) + ")";
                    } else*/ {
                    if (zenModeOffValue)
                        value = value + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_Off) + ")";
                    else if (ringerMode.equals("4")) {
                        if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                                (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI))
                            value = value + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_Off) + ")";
                        else
                            value = value + " (" + getString(R.string.array_pref_soundModeArray_ZenModeM_On) + ")";
                    }
                }
            }

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_soundProfileCategoryRoot", context)
                    + "</b>";
        }
        if (cattegorySummaryData.bold) {
            int titleRes;
            //if (Build.VERSION.SDK_INT >= 23)
            titleRes = R.string.profile_preferences_volumeZenModeM;
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, titleRes, context);
            if (!title.isEmpty()) {
                final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context.getApplicationContext());
                if ((ringerMode != null) && (ringerMode.equals("5")) && canEnableZenMode) {
                    if (!cattegorySummaryData.summary.isEmpty())
                        cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                    String value = StringFormatUtils.getZenModePreferenceString(zenMode, context);

                    cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                            ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_soundProfileCategoryRoot", context)
                            + "</b>";
                }
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, R.string.profile_preferences_vibrateWhenRinging, context);
            if (!title.isEmpty()) {
                if (ringerMode != null) {
                    if (ringerMode.equals("1") || ringerMode.equals("4")) {
                        if (!cattegorySummaryData.summary.isEmpty())
                            cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                        String value = StringFormatUtils.getListPreferenceString(
                                preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING,
                                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING)),
                                R.array.vibrateWhenRingingValues, R.array.vibrateWhenRingingArray, context);

                        cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_soundProfileCategoryRoot", context)
                                + "</b>";
                    } else
                        //noinspection DuplicateExpressions
                        if ((ringerMode.equals("5")) && (zenMode != null) && (zenMode.equals("1") || zenMode.equals("2"))) {
                            if (!cattegorySummaryData.summary.isEmpty())
                                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                            String value = StringFormatUtils.getListPreferenceString(
                                    preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING,
                                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING)),
                                    R.array.vibrateWhenRingingValues, R.array.vibrateWhenRingingArray, context);

                            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_soundProfileCategoryRoot", context)
                                    + "</b>";
                        }
                }
            }
            if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, R.string.profile_preferences_vibrateNotifications, context);
                if (!title.isEmpty()) {
                    if (ringerMode != null) {
                        if (ringerMode.equals("1") || ringerMode.equals("4")) {
                            if (!cattegorySummaryData.summary.isEmpty())
                                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                            String value = StringFormatUtils.getListPreferenceString(
                                    preferences.getString(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS,
                                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS)),
                                    R.array.vibrateNotificationsValues, R.array.vibrateNotificationsArray, context);

                            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_soundProfileCategoryRoot", context)
                                    + "</b>";
                        } else
                            //noinspection DuplicateExpressions
                            if ((ringerMode.equals("5")) && (zenMode != null) && (zenMode.equals("1") || zenMode.equals("2"))) {
                                if (!cattegorySummaryData.summary.isEmpty())
                                    cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                                String value = StringFormatUtils.getListPreferenceString(
                                        preferences.getString(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS,
                                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS)),
                                        R.array.vibrateNotificationsValues, R.array.vibrateNotificationsArray, context);

                                cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                        ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_soundProfileCategoryRoot", context)
                                        + "</b>";
                            }
                    }
                }
            }
        }

        Profile profile = new Profile();
        profile._vibrateWhenRinging = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, "0"));
        if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {
            profile._vibrateNotifications = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, "0"));
        }
        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileVibrateWhenRinging(context, profile, permissions);
        //if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {
        //    Permissions.checkProfileVibrateNotifications(context, profile, permissions);
        //}
        cattegorySummaryData.permissionGranted = permissions.size() == 0;

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryVolume(Context context,
                                             CattegorySummaryData cattegorySummaryData) {

        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND, R.string.profile_preferences_volumeMuteSound, context);
        boolean isMuteEnabled = false;
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            isMuteEnabled = true;
            cattegorySummaryData.summary = cattegorySummaryData.summary + title;
        }
        if (!isMuteEnabled) {
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_RINGTONE, R.string.profile_preferences_volumeRingtone, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                //if (!summary.isEmpty()) summary = summary + " • ";

                if (audioManager != null) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_RINGTONE));

                    value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

                    cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                            ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_volumeCategoryRoot", context)
                            + "</b>";
                } else
                    cattegorySummaryData.summary = cattegorySummaryData.summary + title;
            }
            String ringtoneValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE, "");
            if ((!ActivateProfileHelper.getMergedRingNotificationVolumes() || ApplicationPreferences.applicationUnlinkRingerNotificationVolumes) &&
                    getEnableVolumeNotificationByRingtone(ringtoneValue)) {
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, R.string.profile_preferences_volumeNotification, context);
                if (!title.isEmpty()) {
                    cattegorySummaryData.bold = true;
                    if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                    if (audioManager != null) {
                        String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_NOTIFICATION,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_NOTIFICATION));

                        value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);

                        cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_volumeCategoryRoot", context)
                                + "</b>";
                    } else
                        cattegorySummaryData.summary = cattegorySummaryData.summary + title;
                }
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_MEDIA, R.string.profile_preferences_volumeMedia, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                if (audioManager != null) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_MEDIA,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_MEDIA));

                    value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                    cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                            ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_volumeCategoryRoot", context)
                            + "</b>";
                } else
                    cattegorySummaryData.summary = cattegorySummaryData.summary + title;
            }
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_ALARM, R.string.profile_preferences_volumeAlarm, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary +" • ";

            if (audioManager != null) {
                String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_ALARM,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_ALARM));

                value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

                cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                        ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_volumeCategoryRoot", context)
                        + "</b>";
            }
            else
                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
        }
        if (!isMuteEnabled) {
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_SYSTEM, R.string.profile_preferences_volumeSystem, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                if (audioManager != null) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_SYSTEM,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_SYSTEM));

                    value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);

                    cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                            ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_volumeCategoryRoot", context)
                            + "</b>";
                } else
                    cattegorySummaryData.summary = cattegorySummaryData.summary + title;
            }
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_VOICE, R.string.profile_preferences_volumeVoiceCall, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary +" • ";

            if (audioManager != null) {
                String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_VOICE,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_VOICE));

                value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);

                cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                        ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_volumeCategoryRoot", context)
                        + "</b>";
            }
            else
                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
        }
        if (!isMuteEnabled) {
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_DTMF, R.string.profile_preferences_volumeDTMF, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                if (audioManager != null) {
                    String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_DTMF,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_DTMF));

                    value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF);

                    cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                            ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_volumeCategoryRoot", context)
                            + "</b>";
                } else
                    cattegorySummaryData.summary = cattegorySummaryData.summary + title;
            }
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY, R.string.profile_preferences_volumeAccessibility, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary +" • ";

            if ((Build.VERSION.SDK_INT >= 26) && (audioManager != null)) {
                String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY));

                value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(AudioManager.STREAM_ACCESSIBILITY);

                cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                        ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_volumeCategoryRoot", context)
                        + "</b>";
            }
            else
                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO, R.string.profile_preferences_volumeBluetoothSCO, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary +" • ";

            if (audioManager != null) {
                String value = preferences.getString(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO));

                value = ProfileStatic.getVolumeValue(value) + "/" + audioManager.getStreamMaxVolume(ActivateProfileHelper.STREAM_BLUETOOTH_SCO);

                cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                        ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_volumeCategoryRoot", context)
                        + "</b>";
            }
            else
                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, R.string.profile_preferences_volumeSpeakerPhone, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE)),
                    R.array.volumeSpeakerPhoneValues, R.array.volumeSpeakerPhoneArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_volumeCategoryRoot", context)
                    + "</b>";
        }

        Profile profile = new Profile();
        profile._volumeSpeakerPhone = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, "0"));
        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.size() == 0;

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummarySounds(Context context,
                                             Preference preferenceScreen,
                                             CattegorySummaryData cattegorySummaryData,
                                             int phoneCount) {
        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, R.string.profile_preferences_soundRingtoneChange, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue("<ringtone_name>", prefMng, "prf_pref_soundsCategoryRoot", context)
                    + "</b>";
        }
        //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_RINGTONE);
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, R.string.profile_preferences_soundNotificationChange, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";
            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue("<notification_name>", prefMng, "prf_pref_soundsCategoryRoot", context)
                    + "</b>";
        }
        //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, R.string.profile_preferences_soundAlarmChange, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";
            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue("<alarm_name>", prefMng, "prf_pref_soundsCategoryRoot", context)
                    + "</b>";
        }
        //_bold = _bold || isBold(Profile.PREF_PROFILE_SOUND_ALARM);

        //_permissionGranted = true;

        boolean isDualSIM = (phoneCount > 1);

        if (isDualSIM &&
                ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                        (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                        (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI))) {
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, R.string.profile_preferences_soundRingtoneChangeSIM1, context);
            if (!title.isEmpty()) {
                if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";
                cattegorySummaryData.bold = true;
                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, R.string.profile_preferences_soundRingtoneChangeSIM2, context);
            if (!title.isEmpty()) {
                if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";
                cattegorySummaryData.bold = true;
                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
            }

            if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) {
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, R.string.profile_preferences_soundSameRingtoneForBothSIMCards, context);
                if (!title.isEmpty()) {
                    if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";
                    cattegorySummaryData.bold = true;
                    cattegorySummaryData.summary = cattegorySummaryData.summary + title;
                }
            }

            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, R.string.profile_preferences_soundNotificationChangeSIM1, context);
            if (!title.isEmpty()) {
                if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";
                cattegorySummaryData.bold = true;
                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, R.string.profile_preferences_soundNotificationChangeSIM2, context);
            if (!title.isEmpty()) {
                if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";
                cattegorySummaryData.bold = true;
                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
            }

            Profile profile = new Profile();
            profile._soundRingtoneChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, "0"));
            profile._soundRingtoneChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, "0"));
            profile._soundNotificationChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, "0"));
            profile._soundNotificationChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, "0"));
            ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
            Permissions.checkProfileRingtones(context, profile, permissions);
            cattegorySummaryData.permissionGranted = permissions.size() == 0;

            if (cattegorySummaryData.bold) {
                //noinspection ConstantConditions
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, cattegorySummaryData.bold, false, false,
                        (!cattegorySummaryData.permissionGranted) ||
                        notGrantedG1Permission ||
                        notRootedOrGrantetRoot ||
                        notInstalledPPPPS);
            }
        }

        Profile profile = new Profile();
        profile._soundRingtoneChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, "0"));
        profile._soundNotificationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0"));
        profile._soundAlarmChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, "0"));
        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileRingtones(context, profile, permissions);
        cattegorySummaryData.permissionGranted = cattegorySummaryData.permissionGranted && (permissions.size() == 0);

        if (cattegorySummaryData.bold) {
            setProfileSoundsPreferenceSummary(cattegorySummaryData.summary,
                    preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE)),
                    preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION)),
                    preferences.getString(Profile.PREF_PROFILE_SOUND_ALARM,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_ALARM)),
                    preferenceScreen, context);

            GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, cattegorySummaryData.bold, false, false, !cattegorySummaryData.permissionGranted);
        }

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryTouchEffects(Context context,
                                                   CattegorySummaryData cattegorySummaryData) {
        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_ON_TOUCH, R.string.profile_preferences_soundOnTouch, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_SOUND_ON_TOUCH,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_ON_TOUCH)),
                    R.array.soundOnTouchValues, R.array.soundOnTouchArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_touchEffectsCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, R.string.profile_preferences_vibrationOnTouch, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH)),
                    R.array.vibrationOnTouchValues, R.array.vibrationOnTouchArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_touchEffectsCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, R.string.profile_preferences_dtmfToneWhenDialing, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING)),
                    R.array.dtmfToneWhenDialingValues, R.array.dtmfToneWhenDialingArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_touchEffectsCategoryRoot", context)
                    + "</b>";
        }

        Profile profile = new Profile();
        profile._soundOnTouch = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_ON_TOUCH, "0"));
        profile._vibrationOnTouch = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, "0"));
        profile._dtmfToneWhenDialing = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, "0"));
        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileSoundOnTouch(context, profile, permissions);
        Permissions.checkProfileVibrationOnTouch(context, profile, permissions);
        Permissions.checkProfileDtmfToneWhenDialing(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.size() == 0;

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryVibrationIntensity(Context context,
                                                         CattegorySummaryData cattegorySummaryData) {
        PreferenceAllowed _preferenceAllowed = new PreferenceAllowed();
        PreferenceAllowed.isProfileCategoryAllowed_PREF_PROFILE_VIBRATION_INTENSITY(_preferenceAllowed, context);
        if (_preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
            String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING, R.string.profile_preferences_vibrationIntensityRinging, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (!cattegorySummaryData.summary.isEmpty())
                    cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING));

                value = ProfileStatic.getVolumeValue(value) + "/" + VibrationIntensityPreference.getMaxValue("RINGING");

                cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                        ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_vibrationIntensityCategoryRoot", context)
                        + "</b>";
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS, R.string.profile_preferences_vibrationIntensityNotificatiions, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (!cattegorySummaryData.summary.isEmpty())
                    cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS));

                value = ProfileStatic.getVolumeValue(value) + "/" + VibrationIntensityPreference.getMaxValue("NOTIFICATIONS");

                cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                        ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_vibrationIntensityCategoryRoot", context)
                        + "</b>";
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, R.string.profile_preferences_vibrationIntensityTouchInteraction, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (!cattegorySummaryData.summary.isEmpty())
                    cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                String value = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION,
                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION));

                value = ProfileStatic.getVolumeValue(value) + "/" + VibrationIntensityPreference.getMaxValue("TOUCHINTERACTION");

                cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                        ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_vibrationIntensityCategoryRoot", context)
                        + "</b>";
            }

            /*
            Profile profile = new Profile();
            profile._vibrationIntensityRinging = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING, "-1|1");
            profile._vibrationIntensityNotifications = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS, "-1|1");
            profile._vibrationIntensityTouchInteraction = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, "-1|1");
            ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
            Permissions.checkProfileVibrationIntensityForSamsung(context, profile, permissions);
            cattegorySummaryData.permissionGranted = permissions.size() == 0;
            */

        } else {
            // remove "Vibration intensity", because is not allowed for non-Samsung, non-OnePLus devices and API < 33
            Preference preference = prefMng.findPreference("prf_pref_vibrationIntensityCategoryRoot");
            if (preference != null) {
                //preference.setVisible(false);
                preference.setEnabled(false);
            }
            cattegorySummaryData.summary = _preferenceAllowed.getNotAllowedPreferenceReasonString(context);
            cattegorySummaryData.bold = false;
            cattegorySummaryData.forceSet = true;
        }

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryRadios(Context context,
                                             CattegorySummaryData cattegorySummaryData,
                                             TelephonyManager telephonyManager, int phoneCount) {
        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, R.string.profile_preferences_deviceAirplaneMode, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE)),
                    R.array.airplaneModeValues, R.array.airplaneModeArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                    + "</b>";

            Profile profile = new Profile();
            profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, "0"));
            ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
            Permissions.checkProfileMicrophone(context, profile, permissions);
            cattegorySummaryData.permissionGranted = permissions.size() == 0;

            if ((Build.VERSION.SDK_INT >= 26) && (profile._deviceAirplaneMode >= 4)) {
                // change only when default assistant is false, becuse may be checked also for another
                // profile parameters
                boolean defaultAssistantSet = ActivateProfileHelper.isPPPSetAsDefaultAssistant(context);
                if (!defaultAssistantSet)
                    cattegorySummaryData.defaultAssistantSet = false;
            }
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AUTOSYNC, R.string.profile_preferences_deviceAutosync, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_AUTOSYNC,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_AUTOSYNC)),
                    R.array.hardwareModeValues, R.array.hardwareModeArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, R.string.profile_preferences_deviceMobileData_21, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA)),
                    R.array.hardwareModeValues, R.array.hardwareModeArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, R.string.profile_preferences_deviceMobileDataPrefs, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS)),
                    R.array.mobileDataPrefsValues, R.array.mobileDataPrefsArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                    + "</b>";
        }

        //_permissionGranted = true;
        boolean isDualSIM = (phoneCount > 1);

        if (isDualSIM) {
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, R.string.profile_preferences_deviceOnOff_SIM1, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, R.string.profile_preferences_deviceOnOff_SIM2, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
            }

            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, R.string.profile_preferences_deviceDefaultSIM, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
            }

            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, R.string.profile_preferences_deviceMobileData_21_SIM1, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, R.string.profile_preferences_deviceMobileData_21_SIM2, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
            }

            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, R.string.profile_preferences_deviceNetworkTypeSIM1, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
            }
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, R.string.profile_preferences_deviceNetworkTypeSIM2, context);
            //PPApplication.logE("[DUAL_SIM] ProfilesPrefsFragment.setCategorySummary", "PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2 - notGrantedG1Permission="+notGrantedG1Permission);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
            }

            Profile profile = new Profile();
            profile._deviceDefaultSIMCards = preferences.getString(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, "0|0|0");
            profile._deviceMobileDataSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, "0"));
            profile._deviceMobileDataSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, "0"));
            profile._deviceNetworkTypeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, "0"));
            profile._deviceNetworkTypeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, "0"));
            profile._deviceOnOffSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, "0"));
            profile._deviceOnOffSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, "0"));
            ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
            Permissions.checkProfileRadioPreferences(context, profile, permissions);
            //Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
            cattegorySummaryData.permissionGranted = permissions.size() == 0;

        }

        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI, R.string.profile_preferences_deviceWiFi, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WIFI)),
                    R.array.wifiModeValues, R.array.wifiModeArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, R.string.profile_preferences_deviceConnectToSSID, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary +" • ";

            String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID));
            if (value != null) {
                if (value.equals(Profile.CONNECTTOSSID_JUSTANY))
                    value = getString(R.string.connect_to_ssid_pref_dlg_summary_text_just_any);

                cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                        ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                        + "</b>";
            }
            else
                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
        }
        //if (Build.VERSION.SDK_INT < 30) {
            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI_AP, R.string.profile_preferences_deviceWiFiAP, context);
            if (!title.isEmpty()) {
                cattegorySummaryData.bold = true;
                if (!cattegorySummaryData.summary.isEmpty())
                    cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                String value = StringFormatUtils.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WIFI_AP)),
                        R.array.wifiAPValues, R.array.wifiAPArray, context);

                cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                        ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                        + "</b>";
            }
        //}
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS, R.string.profile_preferences_deviceWiFiAPPrefs, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS)),
                    R.array.wiFiAPPrefsValues, R.array.wiFiAPPrefsArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, R.string.profile_preferences_deviceBluetooth, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_BLUETOOTH,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_BLUETOOTH)),
                    R.array.hardwareModeValues, R.array.hardwareModeArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE, R.string.profile_preferences_deviceLocationMode, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE)),
                    R.array.locationModeValues, R.array.locationModeArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_GPS, R.string.profile_preferences_deviceGPS, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_GPS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_GPS)),
                    R.array.hardwareModeValues, R.array.hardwareModeArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, R.string.profile_preferences_deviceLocationServicePrefs, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS)),
                    R.array.locationServicePrefsValues, R.array.locationServicePrefsArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NFC, R.string.profile_preferences_deviceNFC, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_NFC,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NFC)),
                    R.array.hardwareModeValues, R.array.hardwareModeArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, R.string.profile_preferences_deviceNetworkType, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary +" • ";

            //final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int phoneType = TelephonyManager.PHONE_TYPE_GSM;
            if (telephonyManager != null)
                phoneType = telephonyManager.getPhoneType();

            int arrayValues = 0;
            int arrayStrings = 0;
            if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                arrayStrings = R.array.networkTypeGSMArray;
                arrayValues = R.array.networkTypeGSMValues;
            }

            if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                arrayStrings = R.array.networkTypeCDMAArray;
                arrayValues = R.array.networkTypeCDMAValues;
            }

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)),
                    arrayValues, arrayStrings, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, R.string.profile_preferences_deviceNetworkTypePrefs, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS)),
                    R.array.networkTypePrefsValues, R.array.networkTypePrefsArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS, R.string.profile_preferences_deviceVPNSettingsPrefs, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS)),
                    R.array.vpnSettingsPrefsValues, R.array.vpnSettingsPrefsArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_VPN, R.string.profile_preferences_deviceVPN, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary +" • ";

            String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_VPN,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_VPN));

            int vpnApplication;

            String[] splits = value.split("\\|");
            try {
                vpnApplication = Integer.parseInt(splits[0]);
            } catch (Exception e) {
                //Log.e("VolumeDialogPreference.getValueVDP", Log.getStackTraceString(e));
                PPApplication.recordException(e);
                vpnApplication = 0;
            }

            String[] entries = getResources().getStringArray(R.array.vpnApplicationArray);
            String[] entryValues = getResources().getStringArray(R.array.vpnApplicationValues);

            int applicaitonIdx = 0;
            for (String entryValue : entryValues) {
                if (entryValue.equals(String.valueOf(vpnApplication))) {
                    break;
                }
                ++applicaitonIdx;
            }
            value = entries[applicaitonIdx];

            boolean enableVPN = true;
            try {
                enableVPN = Integer.parseInt(splits[1]) == 0;
            } catch (Exception e) {
                //Log.e("VolumeDialogPreference.getValueVDP", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
            if (enableVPN)
                value = value + "; " + getString(R.string.vpn_profile_pref_dlg_enable_vpn);
            else
                value = value + "; " + getString(R.string.vpn_profile_pref_dlg_disable_vpn);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_radiosCategoryRoot", context)
                    + "</b>";
        }

        Profile profile = new Profile();
        profile._deviceWiFiAP = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP, "0"));
        profile._deviceBluetooth = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, "0"));
        profile._deviceMobileData = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, "0"));
        profile._deviceNetworkType = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, "0"));
        profile._deviceConnectToSSID = preferences.getString(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, Profile.CONNECTTOSSID_JUSTANY);
        profile._deviceNetworkTypePrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, "0"));
        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileRadioPreferences(context, profile, permissions);
        //Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
        cattegorySummaryData.permissionGranted = cattegorySummaryData.permissionGranted && (permissions.size() == 0);

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryScreen(Context context,
                                             CattegorySummaryData cattegorySummaryData) {
        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, R.string.profile_preferences_deviceScreenTimeout, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT)),
                    R.array.screenTimeoutValues, R.array.screenTimeoutArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_screenCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, R.string.profile_preferences_deviceBrightness, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary +" • ";

            String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS));
            boolean automatic = ProfileStatic.getDeviceBrightnessAutomatic(value);
            boolean changeLevel = ProfileStatic.getDeviceBrightnessChangeLevel(value);
            int iValue = ProfileStatic.getDeviceBrightnessValue(value);

            //boolean adaptiveAllowed =
            //        (ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_DEVICE_ADAPTIVE_BRIGHTNESS, null, preferences, true, context).allowed
            //                == PreferenceAllowed.PREFERENCE_ALLOWED);

            String summaryString;
            if (automatic)
            {
                //if (android.os.Build.VERSION.SDK_INT >= 21) // for Android 5.0: adaptive brightness
                summaryString = context.getString(R.string.preference_profile_adaptiveBrightness);
                //else
                //    summaryString = _context.getString(R.string.preference_profile_autoBrightness);
            } else
                summaryString = context.getString(R.string.preference_profile_manual_brightness);

            if (changeLevel /*&& (adaptiveAllowed || !automatic)*/) {
                String _value = iValue + "/100";
                summaryString = summaryString + "; " + _value;
            }

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(summaryString, prefMng, "prf_pref_screenCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, R.string.profile_preferences_deviceAutoRotation,context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_AUTOROTATE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_AUTOROTATE)),
                    R.array.displayRotationValues, R.array.displayRotationArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_screenCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT, R.string.profile_preferences_deviceScreenOnPermanent, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT)),
                    R.array.screenOnPermanentValues, R.array.screenOnPermanentArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_screenCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_KEYGUARD, R.string.profile_preferences_deviceKeyguard, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_KEYGUARD,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_KEYGUARD)),
                    R.array.keyguardValues, R.array.keyguardArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_screenCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, R.string.profile_preferences_deviceWallpaperChange, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String wallpaperChangeValue = preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE));

            String sValue = StringFormatUtils.getListPreferenceString(wallpaperChangeValue,
                    R.array.changeWallpaperValues, R.array.changeWallpaperArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(sValue, prefMng, "prf_pref_screenCategoryRoot", context)
                    + "</b>";

            if (wallpaperChangeValue.equals("1") ||
                    wallpaperChangeValue.equals("3")) {
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                String value = StringFormatUtils.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR)),
                        R.array.wallpaperForValues, R.array.wallpaperForArray, context);

                cattegorySummaryData.summary = cattegorySummaryData.summary +
                        context.getString(R.string.profile_preferences_deviceWallpaperFor)
                        + ": <b>" +
                        ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_screenCategoryRoot", context)
                        + "</b>";
            }
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_LOCK_DEVICE, R.string.profile_preferences_lockDevice, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_LOCK_DEVICE)),
                    R.array.lockDeviceValues, R.array.lockDeviceArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_screenCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS, R.string.profile_preferences_headsUpNotifications, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS)),
                    R.array.headsUpNotificationsValues, R.array.headsUpNotificationsArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_screenCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY, R.string.profile_preferences_alwaysOnDisplay, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY)),
                    R.array.alwaysOnDisplayValues, R.array.alwaysOnDisplayArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_screenCategoryRoot", context)
                    + "</b>";
        }

        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SCREEN_DARK_MODE, R.string.profile_preferences_screenDarkMode, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_SCREEN_DARK_MODE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SCREEN_DARK_MODE)),
                    R.array.screenDarkModeValues, R.array.screenDarkModeArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_screenCategoryRoot", context)
                    + "</b>";
        }

        Profile profile = new Profile();
        profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, "0"));
        profile._screenOnPermanent = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT, "0"));
        profile._deviceBrightness = preferences.getString(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, "");
        profile._deviceAutoRotate = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, "0"));
        profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, "0"));
        profile._alwaysOnDisplay = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY, "0"));
        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileScreenTimeout(context, profile, permissions);
        Permissions.checkProfileScreenOnPermanent(context, profile, permissions);
        Permissions.checkProfileScreenBrightness(context, profile, permissions);
        Permissions.checkProfileAutoRotation(context, profile, permissions);
        Permissions.checkProfileImageWallpaper(context, profile, permissions);
        Permissions.checkProfileWallpaperFolder(context, profile, permissions);
        Permissions.checkProfileAlwaysOnDisplay(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.size() == 0;

        profile._lockDevice = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE, "0"));
        cattegorySummaryData.accessibilityEnabled = profile.isAccessibilityServiceEnabled(context, false) == 1;

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryLedAccessories(Context context,
                                                     CattegorySummaryData cattegorySummaryData) {
        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_NOTIFICATION_LED, R.string.profile_preferences_notificationLed, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            //if (!summary.isEmpty()) summary = summary +" • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_NOTIFICATION_LED,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_NOTIFICATION_LED)),
                    R.array.notificationLedValues, R.array.notificationLedArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_ledAccessoriesCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_CAMERA_FLASH, R.string.profile_preferences_cameraFlash, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_CAMERA_FLASH,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_CAMERA_FLASH)),
                    R.array.cameraFlashValues, R.array.cameraFlashArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_ledAccessoriesCategoryRoot", context)
                    + "</b>";
        }

        Profile profile = new Profile();
        profile._notificationLed = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_NOTIFICATION_LED, "0"));
        profile._cameraFlash = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_CAMERA_FLASH, "0"));
        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileNotificationLed(context, profile, permissions);
        Permissions.checkProfileCameraFlash(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.size() == 0;

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryOthers(Context context,
                                             CattegorySummaryData cattegorySummaryData) {
        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, R.string.profile_preferences_devicePowerSaveMode, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE)),
                    R.array.hardwareModeValues, R.array.hardwareModeArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_othersCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE, R.string.profile_preferences_deviceRunApplicationsShortcutsChange, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary +" • ";

            String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME));
            if ((value != null) &&
                    (!value.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME)))) {
                String[] splits = value.split("\\|");

                cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                        ProfileStatic.getColorForChangedPreferenceValue(context.getString(R.string.applications_multiselect_summary_text_selected) + " " + splits.length, prefMng, "prf_pref_othersCategoryRoot", context)
                        + "</b>";
            }
            else
                cattegorySummaryData.summary = cattegorySummaryData.summary + title;
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS, R.string.profile_preferences_deviceCloseAllApplications, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS)),
                    R.array.closeAllApplicationsValues, R.array.closeAllApplicationsArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_othersCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, R.string.profile_preferences_deviceForceStopApplicationsChange, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;

            if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary +" • ";

            String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME));
            if ((value != null) &&
                    (!value.equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME)))) {
                String[] splits = value.split("\\|");

                cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                        ProfileStatic.getColorForChangedPreferenceValue(context.getString(R.string.applications_multiselect_summary_text_selected) + " " + splits.length, prefMng, "prf_pref_othersCategoryRoot", context)
                        + "</b>";
            }
            else
                cattegorySummaryData.summary = cattegorySummaryData.summary + title;

            Profile profile = new Profile();
            profile._deviceForceStopApplicationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, "0"));
            cattegorySummaryData.accessibilityEnabled = profile.isAccessibilityServiceEnabled(context, false) == 1;
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_GENERATE_NOTIFICATION, R.string.profile_preferences_generateNotification, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary +" • ";

            String value = preferences.getString(Profile.PREF_PROFILE_GENERATE_NOTIFICATION,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_GENERATE_NOTIFICATION));

            //boolean generate = Profile.getGenerateNotificationChange(value);
            int iconType = ProfileStatic.getGenerateNotificationIconType(value);
            String notificationTitle = ProfileStatic.getGenerateNotificationTitle(value);
            String notificationBody = ProfileStatic.getGenerateNotificationBody(value);

            String summaryString = "";

            if (iconType == 0)
                summaryString = summaryString + getString(R.string.preference_profile_generate_notification_information_icon) + "; ";
            else if (iconType == 1)
                summaryString = summaryString + getString(R.string.preference_profile_generate_notification_exclamation_icon) + "; ";
            else
                summaryString = summaryString + getString(R.string.preference_profile_generate_notification_profile_icon) + "; ";

            if (notificationBody.isEmpty())
                summaryString = summaryString + notificationTitle;
            else
                summaryString = summaryString + notificationTitle + ", ...";

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(summaryString, prefMng, "prf_pref_othersCategoryRoot", context)
                    + "</b>";
        }

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryForceStopApplications(Context context,
                                             CattegorySummaryData cattegorySummaryData) {
        //String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, R.string.profile_preferences_deviceForceStopApplicationsChange, false, context);
        String title = context.getString(R.string.profile_preferences_deviceForceStopApplicationsChange);
        int index = 0;
        String defaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
        String sValue = preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, defaultValue);
        String[] entryValues = getResources().getStringArray(R.array.forceStopApplicationValues);
        for (String v : entryValues) {
            if (v.equals(sValue))
                break;
            index++;
        }
        String[] entries = getResources().getStringArray(R.array.forceStopApplicationArray);
        cattegorySummaryData.summary = title + ": " +
                ((index >= 0) ? "<b>" +
                        ProfileStatic.getColorForChangedPreferenceValue(entries[index], prefMng, "prf_pref_forceStopApplicationsCategoryRoot", context)
                        + "</b>" : null);

        boolean ok = true;
        int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
        if (extenderVersion == 0) {
            cattegorySummaryData.summary = getString(R.string.profile_preferences_device_not_allowed) +
                    ": " + getString(R.string.preference_not_allowed_reason_not_extender_installed);
            ok = false;
        } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST) {
            cattegorySummaryData.summary = getString(R.string.profile_preferences_device_not_allowed) +
                    ": " + getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
            ok = false;
        }
        else
        if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, false, true
                /*, "ProfilesPrefsFragment.setCategorySummaryForceStopApplications"*/)) {
            cattegorySummaryData.summary = getString(R.string.profile_preferences_device_not_allowed)+
                    ": "+ getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
            ok = false;
        }
        else
        if (PPApplication.accessibilityServiceForPPPExtenderConnected == 0) {
            cattegorySummaryData.summary = getString(R.string.profile_preferences_device_not_allowed) +
                    ": " + getString(R.string.preference_not_allowed_reason_state_of_accessibility_setting_for_extender_is_determined);
            ok = false;
        }

        if (ok) {
            if ((sValue != null) && sValue.equals("1")) {
                title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, R.string.profile_preferences_deviceForceStopApplicationsPackageName, context);
                defaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
                sValue = preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME, defaultValue);
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • " + title + ": <b>" +
                        ProfileStatic.getColorForChangedPreferenceValue(
                                ApplicationsMultiSelectDialogPreference.getSummaryForPreferenceCategory(sValue, "accessibility_2.0", context, false),
                                prefMng, "prf_pref_forceStopApplicationsCategoryRoot", context)
                        + "</b>";
            }
        }

        cattegorySummaryData.bold = (index > 0);
        cattegorySummaryData.forceSet = true;

        Profile profile = new Profile();
        profile._deviceForceStopApplicationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, "0"));
        cattegorySummaryData.accessibilityEnabled = profile.isAccessibilityServiceEnabled(context, false) == 1;

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryLockDevice(Context context,
                                                 CattegorySummaryData cattegorySummaryData) {
        int index = 0;
        String sValue;

        String defaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_LOCK_DEVICE);
        sValue = preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE, defaultValue);
        String[] entryValues = getResources().getStringArray(R.array.lockDeviceValues);
        for (String v : entryValues) {
            if (v.equals(sValue))
                break;
            index++;
        }
        String[] entries = getResources().getStringArray(R.array.lockDeviceArray);
        cattegorySummaryData.summary = (index >= 0) ? "<b>" +
                ProfileStatic.getColorForChangedPreferenceValue(entries[index], prefMng, "prf_pref_lockDeviceCategoryRoot", context)
                + "</b>" : null;

        if ((sValue != null) && sValue.equals("3")) {
            int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
            if (extenderVersion == 0) {
                //ok = false;
                cattegorySummaryData.summary = /*cattegorySummaryData.summary +*/
                        getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_not_extender_installed);
            } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST) {
                //ok = false;
                cattegorySummaryData.summary = /*cattegorySummaryData.summary +*/
                        getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
            } else if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, false, true
                    /*, "ProfilesPrefsFragment.setCategorySummaryLockDevice"*/)) {
                //ok = false;
                cattegorySummaryData.summary = /*cattegorySummaryData.summary +*/
                        getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
            } else if (PPApplication.accessibilityServiceForPPPExtenderConnected == 0) {
                //ok = false;
                cattegorySummaryData.summary = /*cattegorySummaryData.summary +*/
                        getString(R.string.profile_preferences_device_not_allowed) +
                        ": " + getString(R.string.preference_not_allowed_reason_state_of_accessibility_setting_for_extender_is_determined);
            }
        }

        cattegorySummaryData.bold = (index > 0);
        cattegorySummaryData.forceSet = true;

        Profile profile = new Profile();
        profile._lockDevice = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE, "0"));
        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileLockDevice(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.size() == 0;

        cattegorySummaryData.accessibilityEnabled = profile.isAccessibilityServiceEnabled(context, false) == 1;

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryApplication(Context context,
                                                  CattegorySummaryData cattegorySummaryData) {
        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING, R.string.profile_preferences_applicationEnableWifiScanning, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING)),
                    R.array.applicationDisableScanningValues, R.array.applicationDisableScanningArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_applicationCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING, R.string.profile_preferences_applicationEnableBluetoothScanning, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING)),
                    R.array.applicationDisableScanningValues, R.array.applicationDisableScanningArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_applicationCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING, R.string.profile_preferences_applicationEnableLocationScanning,context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING)),
                    R.array.applicationDisableScanningValues, R.array.applicationDisableScanningArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_applicationCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING, R.string.profile_preferences_applicationEnableMobileCellScanning, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING)),
                    R.array.applicationDisableScanningValues, R.array.applicationDisableScanningArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_applicationCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING, R.string.profile_preferences_applicationEnableOrientationScanning, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING)),
                    R.array.applicationDisableScanningValues, R.array.applicationDisableScanningArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_applicationCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_NOTIFICATION_SCANNING, R.string.profile_preferences_applicationEnableNotificationScanning, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_NOTIFICATION_SCANNING,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_NOTIFICATION_SCANNING)),
                    R.array.applicationDisableScanningValues, R.array.applicationDisableScanningArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_applicationCategoryRoot", context)
                    + "</b>";
        }
        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_PERIODIC_SCANNING, R.string.profile_preferences_applicationEnablePeriodicScanning, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_PERIODIC_SCANNING,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_PERIODIC_SCANNING)),
                    R.array.applicationDisableScanningValues, R.array.applicationDisableScanningArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_applicationCategoryRoot", context)
                    + "</b>";
        }

        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN, R.string.profile_preferences_applicationEnableGlobalEventsRun, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String value = StringFormatUtils.getListPreferenceString(
                    preferences.getString(Profile.PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN,
                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN)),
                    R.array.applicationDisableGlobalEventsRunValues, R.array.applicationDisableGlobalEventsRunArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_applicationCategoryRoot", context)
                    + "</b>";
        }

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryRadiosDualSIMSupport(Context context,
                                             Preference preferenceScreen,
                                             CattegorySummaryData cattegorySummaryData,
                                             TelephonyManager telephonyManager, int phoneCount) {
        boolean isDualSIM = true;
        if (telephonyManager != null) {
            if (phoneCount < 2) {
                preferenceScreen.setVisible(false);
                isDualSIM = false;
            }
            if (isDualSIM) {
                String title;
                if (Build.VERSION.SDK_INT >= 26) {
                    title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, R.string.profile_preferences_deviceOnOff_SIM1, context);
                    if (!title.isEmpty()) {
                        cattegorySummaryData.bold = true;
                        //if (!summary.isEmpty()) summary = summary + " • ";

                        String value = StringFormatUtils.getListPreferenceString(
                                preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1,
                                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1)),
                                R.array.onOffSIMValues, R.array.onOffSIMArray, context);

                        cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_deviceRadiosDualSIMSupportCategoryRoot", context)
                                + "</b>";
                    }
                    title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, R.string.profile_preferences_deviceOnOff_SIM2, context);
                    if (!title.isEmpty()) {
                        cattegorySummaryData.bold = true;
                        if (!cattegorySummaryData.summary.isEmpty())
                            cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                        String value = StringFormatUtils.getListPreferenceString(
                                preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2,
                                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2)),
                                R.array.onOffSIMValues, R.array.onOffSIMArray, context);

                        cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_deviceRadiosDualSIMSupportCategoryRoot", context)
                                + "</b>";
                    }

                    title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, R.string.profile_preferences_deviceDefaultSIM, context);
                    if (!title.isEmpty()) {
                        cattegorySummaryData.bold = true;
                        if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                        String value = preferences.getString(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS));

                        String[] splits = value.split("\\|");
                        String voiceStr = "";
                        try {
                            String[] arrayStrings = context.getResources().getStringArray(R.array.defaultSIMVoiceArray);
                            int index = Integer.parseInt(splits[0]);
                            voiceStr = arrayStrings[index];
                        } catch (Exception ignored) {
                        }
                        String smsStr = "";
                        try {
                            String[] arrayStrings = context.getResources().getStringArray(R.array.defaultSIMSMSArray);
                            int index = Integer.parseInt(splits[1]);
                            smsStr = arrayStrings[index];
                        } catch (Exception ignored) {
                        }
                        String dataStr = "";
                        try {
                            String[] arrayStrings = context.getResources().getStringArray(R.array.defaultSIMDataArray);
                            int index = Integer.parseInt(splits[2]);
                            dataStr = arrayStrings[index];
                        } catch (Exception ignored) {
                        }

                        cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                ProfileStatic.getColorForChangedPreferenceValue(
                                        voiceStr + "; " + smsStr + "; " + dataStr,
                                        prefMng, "prf_pref_deviceRadiosDualSIMSupportCategoryRoot", context)
                                + "</b>";
                    }

                    title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, R.string.profile_preferences_deviceMobileData_21_SIM1, context);
                    if (!title.isEmpty()) {
                        cattegorySummaryData.bold = true;
                        if (!cattegorySummaryData.summary.isEmpty())
                            cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                        String value = StringFormatUtils.getListPreferenceString(
                                preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1,
                                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1)),
                                R.array.hardwareModeValues, R.array.hardwareModeArray, context);

                        cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_deviceRadiosDualSIMSupportCategoryRoot", context)
                                + "</b>";
                    }
                    title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, R.string.profile_preferences_deviceMobileData_21_SIM2, context);
                    if (!title.isEmpty()) {
                        cattegorySummaryData.bold = true;
                        if (!cattegorySummaryData.summary.isEmpty())
                            cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                        String value = StringFormatUtils.getListPreferenceString(
                                preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2,
                                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2)),
                                R.array.hardwareModeValues, R.array.hardwareModeArray, context);

                        cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_deviceRadiosDualSIMSupportCategoryRoot", context)
                                + "</b>";
                    }

                    title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, R.string.profile_preferences_deviceNetworkTypeSIM1, context);
                    //PPApplication.logE("[DUAL_SIM] ProfilesPrefsFragment.setCategorySummary", "PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1 - notGrantedG1Permission="+notGrantedG1Permission);
                    if (!title.isEmpty()) {
                        cattegorySummaryData.bold = true;
                        if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                        int phoneType;// = TelephonyManager.PHONE_TYPE_GSM;
                        phoneType = telephonyManager.getPhoneType();

                        int arrayValues = 0;
                        int arrayStrings = 0;
                        if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                            arrayStrings = R.array.networkTypeGSMArray;
                            arrayValues = R.array.networkTypeGSMValues;
                        }

                        if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                            arrayStrings = R.array.networkTypeCDMAArray;
                            arrayValues = R.array.networkTypeCDMAValues;
                        }

                        String value = StringFormatUtils.getListPreferenceString(
                                preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1,
                                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1)),
                                arrayValues, arrayStrings, context);

                        cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_deviceRadiosDualSIMSupportCategoryRoot", context)
                                + "</b>";
                    }
                    title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, R.string.profile_preferences_deviceNetworkTypeSIM2, context);
                    //PPApplication.logE("[DUAL_SIM] ProfilesPrefsFragment.setCategorySummary", "PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2 - notGrantedG1Permission="+notGrantedG1Permission);
                    if (!title.isEmpty()) {
                        cattegorySummaryData.bold = true;
                        if (!cattegorySummaryData.summary.isEmpty()) cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                        int phoneType; // = TelephonyManager.PHONE_TYPE_GSM;
                        phoneType = telephonyManager.getPhoneType();

                        int arrayValues = 0;
                        int arrayStrings = 0;
                        if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                            arrayStrings = R.array.networkTypeGSMArray;
                            arrayValues = R.array.networkTypeGSMValues;
                        }

                        if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                            arrayStrings = R.array.networkTypeCDMAArray;
                            arrayValues = R.array.networkTypeCDMAValues;
                        }

                        String value = StringFormatUtils.getListPreferenceString(
                                preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2,
                                        Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2)),
                                arrayValues, arrayStrings, context);

                        cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_deviceRadiosDualSIMSupportCategoryRoot", context)
                                + "</b>";
                    }

                    Profile profile = new Profile();
                    profile._deviceDefaultSIMCards = preferences.getString(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, "0|0|0");
                    profile._deviceMobileDataSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, "0"));
                    profile._deviceMobileDataSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, "0"));
                    profile._deviceNetworkTypeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, "0"));
                    profile._deviceNetworkTypeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, "0"));
                    profile._deviceOnOffSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, "0"));
                    profile._deviceOnOffSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, "0"));
                    ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                    Permissions.checkProfileRadioPreferences(context, profile, permissions);
                    //Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
                    cattegorySummaryData.permissionGranted = permissions.size() == 0;

                }
            }
        }
        else
            preferenceScreen.setVisible(false);

        return false;
    }

    private boolean setCategorySummarySoundsDualSIMSupport(Context context,
                                                           Preference preferenceScreen,
                                                           CattegorySummaryData cattegorySummaryData,
                                                           TelephonyManager telephonyManager, int phoneCount) {
        if ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI)) {
            boolean isDualSIM = true;
            if (telephonyManager != null) {
                if (phoneCount < 2) {
                    preferenceScreen.setVisible(false);
                    isDualSIM = false;
                }
                if (isDualSIM) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, R.string.profile_preferences_soundRingtoneChangeSIM1, context);
                        if (!title.isEmpty()) {
                            cattegorySummaryData.bold = true;
                            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                    ProfileStatic.getColorForChangedPreferenceValue("<ringtone_name_sim1>", prefMng, "prf_pref_soundsDualSIMSupportCategoryRoot", context)
                                    + "</b>";
                        }
                        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, R.string.profile_preferences_soundRingtoneChangeSIM2, context);
                        if (!title.isEmpty()) {
                            if (!cattegorySummaryData.summary.isEmpty())
                                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";
                            cattegorySummaryData.bold = true;
                            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b><" +
                                    ProfileStatic.getColorForChangedPreferenceValue("ringtone_name_sim2>", prefMng, "prf_pref_soundsDualSIMSupportCategoryRoot", context)
                                    + "</b>";
                        }
                        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, R.string.profile_preferences_soundNotificationChangeSIM1, context);
                        if (!title.isEmpty()) {
                            if (!cattegorySummaryData.summary.isEmpty())
                                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";
                            cattegorySummaryData.bold = true;
                            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                    ProfileStatic.getColorForChangedPreferenceValue("<notification_name_sim1>", prefMng, "prf_pref_soundsDualSIMSupportCategoryRoot", context)
                                    + "</b>";
                        }
                        title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, R.string.profile_preferences_soundNotificationChangeSIM2, context);
                        if (!title.isEmpty()) {
                            if (!cattegorySummaryData.summary.isEmpty())
                                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";
                            cattegorySummaryData.bold = true;
                            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                    ProfileStatic.getColorForChangedPreferenceValue("<notification_name_sim2>", prefMng, "prf_pref_soundsDualSIMSupportCategoryRoot", context)
                                    + "</b>";
                        }

                        if (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI) {
                            title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS, R.string.profile_preferences_soundSameRingtoneForBothSIMCards, context);
                            if (!title.isEmpty()) {
                                if (!cattegorySummaryData.summary.isEmpty())
                                    cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";
                                cattegorySummaryData.bold = true;

                                String value = StringFormatUtils.getListPreferenceString(
                                        preferences.getString(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS,
                                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS)),
                                        R.array.soundSameRingtoneForBothSIMCardsValues, R.array.soundSameRingtoneForBothSIMCardsArray, context);

                                cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                                        ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_soundsDualSIMSupportCategoryRoot", context)
                                        + "</b>";
                            }
                        }

                        if (cattegorySummaryData.bold) {
                            setProfileSoundsDualSIMPreferenceSummary(cattegorySummaryData.summary,
                                    preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1,
                                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1)),
                                    preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2,
                                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2)),
                                    preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1,
                                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1)),
                                    preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2,
                                            Profile.defaultValuesString.get(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2)),
                                    preferenceScreen, context);

                            Profile profile = new Profile();
                            profile._soundRingtoneChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, "0"));
                            profile._soundRingtoneChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, "0"));
                            profile._soundNotificationChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, "0"));
                            profile._soundNotificationChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, "0"));
                            ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                            Permissions.checkProfileRingtones(context, profile, permissions);
                            cattegorySummaryData.permissionGranted = permissions.size() == 0;

                            GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, cattegorySummaryData.bold, false, false,
                                    (!cattegorySummaryData.permissionGranted) ||
                                    notGrantedG1Permission ||
                                    notRootedOrGrantetRoot ||
                                    notInstalledPPPPS);
                            return true;
                        }

                        Profile profile = new Profile();
                        profile._soundRingtoneChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, "0"));
                        profile._soundRingtoneChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, "0"));
                        profile._soundNotificationChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, "0"));
                        profile._soundNotificationChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, "0"));
                        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                        Permissions.checkProfileRingtones(context, profile, permissions);
                        cattegorySummaryData.permissionGranted = permissions.size() == 0;

                        GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, cattegorySummaryData.bold, false, false,
                                (!cattegorySummaryData.permissionGranted) ||
                                        notGrantedG1Permission ||
                                        notRootedOrGrantetRoot ||
                                        notInstalledPPPPS);
                    }
                }
            } else
                preferenceScreen.setVisible(false);
        } else
            preferenceScreen.setVisible(false);

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private boolean setCategorySummaryDeviceWallpaper(Context context,
                                                      CattegorySummaryData cattegorySummaryData) {

        String title = getCategoryTitleWhenPreferenceChanged(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, R.string.profile_preferences_deviceWallpaperChange, context);
        if (!title.isEmpty()) {
            cattegorySummaryData.bold = true;
            if (!cattegorySummaryData.summary.isEmpty())
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

            String wallpaperChangeValue = preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE,
                    Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE));

            String sValue = StringFormatUtils.getListPreferenceString(wallpaperChangeValue,
                    R.array.changeWallpaperValues, R.array.changeWallpaperArray, context);

            cattegorySummaryData.summary = cattegorySummaryData.summary + title + ": <b>" +
                    ProfileStatic.getColorForChangedPreferenceValue(sValue, prefMng, "prf_pref_deviceWallpaperCategoryRoot", context)
                    + "</b>";

            if (wallpaperChangeValue.equals("1") ||
                    wallpaperChangeValue.equals("3")) {
                cattegorySummaryData.summary = cattegorySummaryData.summary + " • ";

                String value = StringFormatUtils.getListPreferenceString(
                        preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR,
                                Profile.defaultValuesString.get(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR)),
                        R.array.wallpaperForValues, R.array.wallpaperForArray, context);

                cattegorySummaryData.summary = cattegorySummaryData.summary +
                        context.getString(R.string.profile_preferences_deviceWallpaperFor)
                        + ": <b>" +
                        ProfileStatic.getColorForChangedPreferenceValue(value, prefMng, "prf_pref_deviceWallpaperCategoryRoot", context)
                        + "</b>";
            }
        }

        Profile profile = new Profile();
        profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, "0"));
        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
        Permissions.checkProfileImageWallpaper(context, profile, permissions);
        Permissions.checkProfileWallpaperFolder(context, profile, permissions);
        cattegorySummaryData.permissionGranted = permissions.size() == 0;

        cattegorySummaryData.forceSet = true;

        return false;
    }

    private void setCategorySummary(String key, Context context) {
        Preference preferenceScreen = prefMng.findPreference(key);
        if (preferenceScreen == null)
            return;

        //SharedPreferences preferences = prefMng.getSharedPreferences();

        CattegorySummaryData cattegorySummaryData = new CattegorySummaryData();
        cattegorySummaryData.summary = "";
        cattegorySummaryData.permissionGranted = true;
        cattegorySummaryData.forceSet = false;
        cattegorySummaryData.bold = false;
        cattegorySummaryData.accessibilityEnabled = true;
        cattegorySummaryData.defaultAssistantSet = true;

        int phoneCount = 1;
        TelephonyManager telephonyManager = null;
        if (Build.VERSION.SDK_INT >= 26) {
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                phoneCount = telephonyManager.getPhoneCount();
            }
        }

        notGrantedG1Permission = false;
        notRootedOrGrantetRoot = false;
        notInstalledPPPPS = false;

        if (key.equals("prf_pref_activationDurationCategoryRoot")) {
            if (setCategorySummaryActivationDuration(context,
                    preferenceScreen, cattegorySummaryData))
                return;
        }

        if (key.equals("prf_pref_soundProfileCategoryRoot")) {
            if (setCategorySummarySoundProfile(context, cattegorySummaryData))
                return;
        }

        if (key.equals("prf_pref_volumeCategoryRoot")) {
            if (setCategorySummaryVolume(context, cattegorySummaryData))
                return;
        }

        if (key.equals("prf_pref_soundsCategoryRoot")) {
            if (setCategorySummarySounds(context,
                    preferenceScreen, cattegorySummaryData, phoneCount))
                return;
        }

        if (key.equals("prf_pref_touchEffectsCategoryRoot")) {
            if (setCategorySummaryTouchEffects(context, cattegorySummaryData))
                return;
        }

        if (key.equals("prf_pref_vibrationIntensityCategoryRoot")) {
            if (setCategorySummaryVibrationIntensity(context, cattegorySummaryData))
                return;
        }

        if (key.equals("prf_pref_radiosCategoryRoot")) {
            if (setCategorySummaryRadios(context, cattegorySummaryData,
                    telephonyManager, phoneCount))
                return;
        }

        if (key.equals("prf_pref_screenCategoryRoot")) {
            if (setCategorySummaryScreen(context, cattegorySummaryData))
                return;
        }

        if (key.equals("prf_pref_ledAccessoriesCategoryRoot")) {
            if (setCategorySummaryLedAccessories(context, cattegorySummaryData))
                return;
        }

        if (key.equals("prf_pref_othersCategoryRoot")) {
            if (setCategorySummaryOthers(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_FORCE_STOP_APPLICATIONS_CATEGORY)) {
            if (setCategorySummaryForceStopApplications(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_LOCK_DEVICE_CATEGORY)) {
            if (setCategorySummaryLockDevice(context, cattegorySummaryData))
                return;
        }

        if (key.equals("prf_pref_applicationCategoryRoot")) {
            if (setCategorySummaryApplication(context, cattegorySummaryData))
                return;
        }

        if (key.equals(PREF_PROFILE_DEVICE_RADIOS_DUAL_SIM_SUPPORT_CATEGORY_ROOT)) {
            if (setCategorySummaryRadiosDualSIMSupport(context, preferenceScreen,
                    cattegorySummaryData, telephonyManager, phoneCount))
                return;
        }

        if (key.equals(PREF_PROFILE_SOUNDS_DUAL_SIM_SUPPORT_CATEGORY_ROOT)) {
            if (setCategorySummarySoundsDualSIMSupport(context, preferenceScreen,
                    cattegorySummaryData, telephonyManager, phoneCount))
                return;
        }

        if (key.equals(PREF_DEVICE_WALLPAPER_CATEGORY)) {
            if (setCategorySummaryDeviceWallpaper(context, cattegorySummaryData))
                return;
        }

        GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceScreen, true, cattegorySummaryData.bold, false, false,
                (!cattegorySummaryData.permissionGranted) ||
                (!cattegorySummaryData.accessibilityEnabled) ||
                (!cattegorySummaryData.defaultAssistantSet) ||
                notGrantedG1Permission ||
                notRootedOrGrantetRoot ||
                notInstalledPPPPS);
        if (cattegorySummaryData.bold || cattegorySummaryData.forceSet)
            preferenceScreen.setSummary(StringFormatUtils.fromHtml(cattegorySummaryData.summary, false, false, false, 0, 0, true));
        else
            preferenceScreen.setSummary("");
    }

    private void setSummaryForNotificationVolume0(/*Context context*/) {
        Preference preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
        if (preference != null) {
            String notificationToneChange = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0");
            String notificationTone = preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION, "");
            //String uriId = TonesHandler.getPhoneProfilesSilentUri(context, RingtoneManager.TYPE_NOTIFICATION);
            /*if (notificationToneChange.equals("1") && notificationTone.equals(uriId))
                preference.setSummary(R.string.profile_preferences_volumeNotificationVolume0_summaryPhoneProfilesSilentConfigured);
            else*/
            if (notificationToneChange.equals("1") && (notificationTone.isEmpty() ||
                                    notificationTone.equals(TonesHandler.NOTIFICATION_TONE_URI_NONE)))
                preference.setSummary(R.string.profile_preferences_volumeNotificationVolume0_summaryNoneConfigured);
            else
                preference.setSummary(R.string.profile_preferences_volumeNotificationVolume0_summaryConfigureForVolume0);
        }
    }

    private void setSummary(String key, Object value)
    {
        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();

        int phoneCount = 1;
        if (Build.VERSION.SDK_INT >= 26) {
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                phoneCount = telephonyManager.getPhoneCount();
            }
        }

        if (key.equals(Profile.PREF_PROFILE_NAME))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                preference.setSummary(value.toString());
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !value.toString().isEmpty(), false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_ICON))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                //preference.setSummary(value.toString());
                boolean valueChanged = !value.toString().equals(Profile.defaultValuesString.get(Profile.PREF_PROFILE_ICON));

                Profile profile = new Profile();
                profile._icon = value.toString();
                ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                Permissions.checkProfileCustomProfileIcon(context, profile, false, permissions);
                boolean permissionGranted = permissions.size() == 0;

                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, valueChanged, false, false, !permissionGranted);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR)) {
            String sValue = value.toString();
            SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                GlobalGUIRoutines.setPreferenceTitleStyleX(checkBoxPreference, true, show, false, false, false);
            }
        }

        boolean alsoSetZenMode = false;
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE))
        {
            String sValue = value.toString();
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                if (sValue.equals("5")) {
                    // do not disturb
                    value = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, "");
                    alsoSetZenMode = true;
                    //setSummary(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, zenModeValue);
                } else {
                    PPListPreference zenModePreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
                    if (zenModePreference != null) {
                        zenModePreference.setEnabled(false);
                        Preference zenModePreferenceInfo = prefMng.findPreference("prf_pref_volumeZenModeInfo");
                        if (zenModePreferenceInfo != null) {
                            zenModePreferenceInfo.setEnabled(zenModePreference.isEnabled());
                        }
                    }
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE) || alsoSetZenMode)
        {
            //if (android.os.Build.VERSION.SDK_INT >= 21)
            //{
                /*final boolean canEnableZenMode =
                        (PPNotificationListenerService.isNotificationListenerServiceEnabled(context.getApplicationContext()) ||
                         (PPApplication.isRooted(false) && PPApplication.settingsBinaryExists())
                        );*/
            final boolean canEnableZenMode = ActivateProfileHelper.canChangeZenMode(context);

            if (!canEnableZenMode)
            {
                PPListPreference listPreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
                if (listPreference != null) {
                    listPreference.setEnabled(false);
                    listPreference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                            ": "+getString(R.string.preference_not_allowed_reason_not_configured_in_system_settings));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, false, false, false);

                    Preference zenModePreferenceInfo = prefMng.findPreference("prf_pref_volumeZenModeInfo");
                    if (zenModePreferenceInfo != null) {
                        zenModePreferenceInfo.setEnabled(listPreference.isEnabled());
                    }
                }
            }
            else
            {
                String sValue = value.toString();
                PPListPreference listPreference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
                if (listPreference != null) {
                    int iValue = Integer.parseInt(sValue);
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    if ((iValue != Profile.NO_CHANGE_VALUE) /*&& (iValue != Profile.SHARED_PROFILE_VALUE)*/) {
                        if (!((iValue == 6) /*&& (android.os.Build.VERSION.SDK_INT < 23)*/)) {
                            String[] summaryArray = getResources().getStringArray(R.array.zenModeSummaryArray);
                            summary = summary + " - " + summaryArray[iValue - 1];
                        }
                    }
                    listPreference.setSummary(summary);

                    final String sRingerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "");
                    int iRingerMode;
                    if (sRingerMode.isEmpty())
                        iRingerMode = 0;
                    else
                        iRingerMode = Integer.parseInt(sRingerMode);

                    if (iRingerMode == 5) {
                        GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, true, false, false, false);
                    }
                    listPreference.setEnabled(iRingerMode == 5);

                    Preference zenModePreferenceInfo = prefMng.findPreference("prf_pref_volumeZenModeInfo");
                    if (zenModePreferenceInfo != null) {
                        zenModePreferenceInfo.setEnabled(listPreference.isEnabled());
                    }
                }

                /*Preference notificationAccessPreference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS);
                if (notificationAccessPreference != null) {
                    PreferenceScreen preferenceCategory = findPreference("prf_pref_soundProfileCategory");
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(notificationAccessPreference);
                }*/
            }
            //}
        }

        if (key.equals(Profile.PREF_PROFILE_DURATION))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                String defaultValue = Profile.defaultValuesString.get(key);
                //preference.setSummary(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, (!sValue.equals(defaultValue)), false, false, false);
                preference = prefMng.findPreference(Profile.PREF_PROFILE_AFTER_DURATION_DO);
                if (preference != null) {
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, (!sValue.equals(defaultValue)), false, false, false);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_AFTER_DURATION_DO))
        {
            String sValue = value.toString();
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
                String durationDefaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION);
                String durationValue = preferences.getString(Profile.PREF_PROFILE_DURATION, durationDefaultValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true,
                        (durationValue != null) && (!durationValue.equals(durationDefaultValue)), false,
                        false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION))
        {
            String sValue = value.toString();
            SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                GlobalGUIRoutines.setPreferenceTitleStyleX(checkBoxPreference, true, show, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND))
        {
            String sValue = value.toString();
            RingtonePreference ringtonePreference = prefMng.findPreference(key);
            if (ringtonePreference != null) {
                boolean show = !sValue.isEmpty();
                GlobalGUIRoutines.setPreferenceTitleStyleX(ringtonePreference, true, show, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE))
        {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if ((vibrator != null) && vibrator.hasVibrator()) {
                String sValue = value.toString();
                SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
                if (checkBoxPreference != null) {
                    checkBoxPreference.setVisible(true);
                    boolean show = sValue.equals("true");
                    GlobalGUIRoutines.setPreferenceTitleStyleX(checkBoxPreference, true, show, false, false, false);
                }
            }
            else {
                SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
                if (checkBoxPreference != null)
                    checkBoxPreference.setVisible(false);
            }
        }

        if (key.equals(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String summary;
                boolean bold = false;
                if (ApplicationPreferences.applicationUnlinkRingerNotificationVolumes) {
                    summary = getString(R.string.profile_preferences_applicationUnlinkRingerNotificationVolumes_enabled);
                    bold = true;
                }
                else
                    summary = getString(R.string.profile_preferences_applicationUnlinkRingerNotificationVolumes_disabled);

                summary = summary + "\n" + getString(R.string.phone_profiles_pref_applicationForceSetMergeRingNotificationVolumes) + ": ";
                int forceMergeValue = ApplicationPreferences.applicationForceSetMergeRingNotificationVolumes;
                String[] valuesArray = getResources().getStringArray(R.array.forceSetMergeRingNotificationVolumesValues);
                String[] labelsArray = getResources().getStringArray(R.array.forceSetMergeRingNotificationVolumesArray);
                int index = 0;
                for (String _value : valuesArray) {
                    if (_value.equals(String.valueOf(forceMergeValue))) {
                        summary = summary + labelsArray[index];
                        break;
                    }
                    ++index;
                }

                if (!ApplicationPreferences.prefMergedRingNotificationVolumes)
                    // detection of volumes merge = volumes are not merged
                    summary = summary + "\n\n" + getString(R.string.profile_preferences_applicationUnlinkRingerNotificationVolumes_not_merged);
                else
                    // detection of volumes merge = volumes are merged
                    summary = summary + "\n\n" + getString(R.string.profile_preferences_applicationUnlinkRingerNotificationVolumes_merged);

                preference.setSummary(summary);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, false, false, false);
            }
        }

        setSummaryTones(key, value, context, phoneCount);

        setSummaryRadios(key, value, context, phoneCount);

        if (key.equals(Profile.PREF_PROFILE_DEVICE_KEYGUARD))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        listPreference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, errorColor, false, false, errorColor);
                }
                else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT))
        {
            String sValue = value.toString();
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);

                Profile profile = new Profile();
                ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                profile._deviceScreenTimeout = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT, "0"));
                Permissions.checkProfileScreenTimeout(context, profile, permissions);
                boolean _permissionGranted = permissions.size() == 0;

                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_AUTOROTATE))
        {
            String sValue = value.toString();
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);

                Profile profile = new Profile();
                ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                profile._deviceAutoRotate = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AUTOROTATE, "0"));
                Permissions.checkProfileAutoRotation(context, profile, permissions);
                boolean _permissionGranted = permissions.size() == 0;

                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE) ||
                key.equals(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
                key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING) ||
                key.equals(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR) ||
                key.equals(Profile.PREF_PROFILE_LOCK_DEVICE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ON_TOUCH) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS))
        {
            PreferenceAllowed preferenceAllowed;
            if (key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING)) {
                preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
            }
            else
            if (key.equals(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS)) {
                if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {
                    preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                }
                else {
                    preferenceAllowed = new PreferenceAllowed();
                    preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_NOT_ALLOWED;
                }
            }
            else {
                preferenceAllowed = new PreferenceAllowed();
                preferenceAllowed.allowed = PreferenceAllowed.PREFERENCE_ALLOWED;
            }
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
            {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        preference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                }
            }
            else {
                String sValue = value.toString();
                PPListPreference preference = prefMng.findPreference(key);
                if (preference !=  null) {
                    int index = preference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? preference.getEntries()[index] : null;
                    preference.setSummary(summary);

                    boolean _permissionGranted = true;

                    if (key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE) ||
                            key.equals(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE) ||
                            key.equals(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH) ||
                            key.equals(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING) ||
                            key.equals(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS) ||
                            key.equals(Profile.PREF_PROFILE_LOCK_DEVICE) ||
                            key.equals(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING) ||
                            key.equals(Profile.PREF_PROFILE_SOUND_ON_TOUCH)) {
                        Profile profile = new Profile();
                        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                        profile._deviceWallpaperChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE, "0"));
                        profile._volumeSpeakerPhone = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE, "0"));
                        profile._vibrationOnTouch = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH, "0"));
                        profile._vibrateWhenRinging = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, "0"));
                        if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {
                            profile._vibrateNotifications = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, "0"));
                        }
                        profile._lockDevice = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE, "0"));
                        profile._dtmfToneWhenDialing = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING, "0"));
                        profile._soundOnTouch = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_ON_TOUCH, "0"));
                        Permissions.checkProfileImageWallpaper(context, profile, permissions);
                        Permissions.checkProfileWallpaperFolder(context, profile, permissions);
                        Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
                        Permissions.checkProfileVibrationOnTouch(context, profile, permissions);
                        Permissions.checkProfileVibrateWhenRinging(context, profile, permissions);
                        //if (Build.VERSION.SDK_INT >= 28)
                        //    Permissions.checkProfileVibrateNotifications(context, profile, permissions);
                        Permissions.checkProfileLockDevice(context, profile, permissions);
                        Permissions.checkProfileDtmfToneWhenDialing(context, profile, permissions);
                        Permissions.checkProfileSoundOnTouch(context, profile, permissions);
                        _permissionGranted = permissions.size() == 0;
                    }

                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, index > 0, false, false, !_permissionGranted);
                }
            }
        }

        if (key.equals(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING) ||
            key.equals(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS) ||
            key.equals(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION))
        {
            PreferenceAllowed _preferenceAllowed = new PreferenceAllowed();
            PreferenceAllowed.isProfileCategoryAllowed_PREF_PROFILE_VIBRATION_INTENSITY(_preferenceAllowed, context);
            if (_preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                String sValue = value.toString();
                PreferenceAllowed preferenceAllowed =
                        ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
                {
                    Preference preference = prefMng.findPreference(key);
                    if (preference != null) {
                        boolean errorColor = false;
                        if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                            preference.setEnabled(false);
                        else
                            errorColor = ProfileStatic.getVibrationIntensityChange(sValue);
                        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                            preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                    ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                        GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                    }
                }
                /*else {
                    VibrationIntensityPreference preference = prefMng.findPreference(key);
                    if (preference !=  null) {
                        boolean change = VibrationIntensityPreference.changeEnabled(sValue);

                        boolean _permissionGranted;

                        Profile profile = new Profile();
                        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                        profile._vibrationIntensityRinging = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING, "-1|1");
                        profile._vibrationIntensityNotifications = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS, "-1|1");
                        profile._vibrationIntensityTouchInteraction = preferences.getString(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION, "-1|1");
                        //Permissions.checkProfileVibrationIntensityForSamsung(context, profile, permissions);
                        _permissionGranted = permissions.size() == 0;

                        GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, change, false, false, !_permissionGranted);
                    }
                }*/
            } else {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + _preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                }
            }
        }

        if (key.equals(Profile.PREF_PROFILE_NOTIFICATION_LED))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        listPreference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, errorColor, false, false, errorColor);
                } else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);

                    Profile profile = new Profile();
                    ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                    profile._notificationLed = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_NOTIFICATION_LED, "0"));
                    Permissions.checkProfileNotificationLed(context, profile, permissions);
                    boolean _permissionGranted = permissions.size() == 0;

                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS) ||
                key.equals(Profile.PREF_PROFILE_SCREEN_DARK_MODE) ||
                key.equals(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY) ||
                key.equals(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        listPreference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, errorColor, false, false, errorColor);
                } else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);

                    boolean _permissionGranted = true;

                    if (key.equals(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT) ||
                            key.equals(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY)) {
                        Profile profile = new Profile();
                        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                        profile._screenOnPermanent = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT, "0"));
                        profile._alwaysOnDisplay = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY, "0"));
                        Permissions.checkProfileScreenOnPermanent(context, profile, permissions);
                        Permissions.checkProfileAlwaysOnDisplay(context, profile, permissions);
                        _permissionGranted = permissions.size() == 0;
                    }

                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON) && (Build.VERSION.SDK_INT < 26))
        {
            String sValue = value.toString();
            SwitchPreferenceCompat checkBoxPreference = prefMng.findPreference(key);
            if (checkBoxPreference != null) {
                boolean show = sValue.equals("true");
                GlobalGUIRoutines.setPreferenceTitleStyleX(checkBoxPreference, true, show, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_MEDIA) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ALARM) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_SYSTEM) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_VOICE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_DTMF) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = VolumeDialogPreference.changeEnabled(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, change, false, false, false);
            }
        }
        if (key.equals(PREF_VOLUME_NOTIFICATION_VOLUME0)) {
            setSummaryForNotificationVolume0(/*context*/);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = BrightnessDialogPreference.changeEnabled(sValue);

                Profile profile = new Profile();
                ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                profile._deviceBrightness = preferences.getString(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS, "");
                Permissions.checkProfileScreenBrightness(context, profile, permissions);
                boolean _permissionGranted = permissions.size() == 0;

                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, change, false, false, !_permissionGranted);

                if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
                    preference = prefMng.findPreference(PREF_PROFILE_DEVICE_BRIGHTNESS_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON);
                    if (preference != null) {
                        boolean forceSetBrightnessAtScreenOn = ApplicationPreferences.applicationForceSetBrightnessAtScreenOn;
                        String summary = context.getString(R.string.profile_preferences_forceSetBrightnessAtScreenOn_summary);
                        if (forceSetBrightnessAtScreenOn)
                            summary = context.getString(R.string.profile_preferences_enabled) + "\n\n" + summary;
                        else {
                            summary = context.getString(R.string.profile_preferences_disabled) + "\n\n" + summary;
                        }
                        preference.setSummary(summary);
                        GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, forceSetBrightnessAtScreenOn, false, false, false);
                    }
                }

            }
        }
        if (key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_NOTIFICATION_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_PERIODIC_SCANNING) ||
                key.equals(Profile.PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN))
        {
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        listPreference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        listPreference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, errorColor, false, false, errorColor);
                }
                else {
                    String sValue = value.toString();
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                }
            }
        }

        if (key.equals(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0) {
                    String summary = getString(R.string.profile_preferences_PPPExtender_not_installed_summary) +
                            "\n\n" + getString(R.string.profile_preferences_deviceForceStopApplications_PPPExtender_install_summary);
                    preference.setSummary(summary);
                }
                else {
                    String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(context);
                    String summary =  getString(R.string.profile_preferences_PPPExtender_installed_summary) +
                            " " + extenderVersionName + " (" + extenderVersion + ")\n\n";
                    if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)
                        summary = summary + getString(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                    else
                        summary = summary + getString(R.string.event_preferences_applications_PPPExtender_upgrade_summary);
                    preference.setSummary(summary);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE)) {
            int index;
            String sValue;

            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                boolean ok = true;
                CharSequence changeSummary = "";
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0) {
                    ok = false;
                    changeSummary = getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + getString(R.string.preference_not_allowed_reason_not_extender_installed);
                }
                else
                if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST) {
                    ok = false;
                    changeSummary = getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                }
                else
                if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, false, true
                        /*, "ProfilesPrefsFragment.setSummary (PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE)"*/)) {
                    ok = false;
                    changeSummary = getString(R.string.profile_preferences_device_not_allowed)+
                            ": "+getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                }
                else
                if (PPApplication.accessibilityServiceForPPPExtenderConnected == 0) {
                    ok = false;
                    changeSummary = getString(R.string.profile_preferences_device_not_allowed) +
                            ": " + getString(R.string.preference_not_allowed_reason_state_of_accessibility_setting_for_extender_is_determined);
                }

                if (!ok) {
                    listPreference.setSummary(changeSummary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, false, false, false, false);
                }
                else {
                    sValue = listPreference.getValue();
                    index = listPreference.findIndexOfValue(sValue);
                    changeSummary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(changeSummary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                }
            }
        }

        if (key.equals(PREF_LOCK_DEVICE_INSTALL_EXTENDER)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                if (extenderVersion == 0) {
                    String summary = getString(R.string.profile_preferences_PPPExtender_not_installed_summary) +
                            "\n\n" + getString(R.string.profile_preferences_lockDevice_PPPExtender_install_summary);
                    preference.setSummary(summary);
                }
                else {
                    String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(context);
                    String summary =  getString(R.string.profile_preferences_PPPExtender_installed_summary) +
                            " " + extenderVersionName + " (" + extenderVersion + ")\n\n";
                    if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST)
                        summary = summary + getString(R.string.event_preferences_applications_PPPExtender_new_version_summary);
                    else
                        summary = summary + getString(R.string.event_preferences_applications_PPPExtender_upgrade_summary);
                    preference.setSummary(summary);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
            int index;
            String sValue;

            PPListPreference listPreference =
                    prefMng.findPreference(Profile.PREF_PROFILE_LOCK_DEVICE);
            if (listPreference != null) {
                sValue = listPreference.getValue();
                //boolean ok = true;
                CharSequence changeSummary;// = "";

                index = listPreference.findIndexOfValue(sValue);
                changeSummary = (index >= 0) ? listPreference.getEntries()[index] : null;

                if (sValue.equals("3")) {
                    int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                    if (extenderVersion == 0) {
                        //ok = false;
                        changeSummary = changeSummary + "\n\n" +
                                getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_not_extender_installed);
                    } else if (extenderVersion < PPApplication.VERSION_CODE_EXTENDER_LATEST) {
                        //ok = false;
                        changeSummary = changeSummary + "\n\n" +
                                getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                    } else if (!PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, false, true
                            /*, "ProfilesPrefsFragment.setSummary (PREF_PROFILE_LOCK_DEVICE)"*/)) {
                        //ok = false;
                        changeSummary = changeSummary + "\n\n" +
                                getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_not_enabled_accessibility_settings_for_extender);
                    } else if (PPApplication.accessibilityServiceForPPPExtenderConnected == 0) {
                        //ok = false;
                        changeSummary = changeSummary + getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + getString(R.string.preference_not_allowed_reason_state_of_accessibility_setting_for_extender_is_determined);
                    }
                }

                listPreference.setSummary(changeSummary);

                Profile profile = new Profile();
                ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                profile._lockDevice = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE, "0"));
                Permissions.checkProfileLockDevice(context, profile, permissions);
                boolean _permissionGranted = permissions.size() == 0;

                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted);
            }
        }

        if (key.equals(PREF_LOCK_DEVICE_ACCESSIBILITY_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                Profile profile = new Profile();
                profile._lockDevice = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_LOCK_DEVICE, "0"));

                if (profile._lockDevice == 3) {
                    int _isAccessibilityEnabled = profile.isAccessibilityServiceEnabled(context, false);
                    boolean _accessibilityEnabled = _isAccessibilityEnabled == 1;

                    String summary;
                    if (_accessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1))
                        summary = getString(R.string.accessibility_service_enabled);
                    else {
                        if (_isAccessibilityEnabled == -1) {
                            summary = getString(R.string.accessibility_service_not_used);
                            summary = summary + "\n\n" + getString(R.string.preference_not_used_extender_reason) + " " +
                                    getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                        } else {
                            summary = getString(R.string.accessibility_service_disabled);
                            summary = summary + "\n\n" + getString(R.string.profile_preferences_lockDevice_AccessibilitySettingsForExtender_summary);
                        }
                    }
                    preference.setSummary(summary);
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, true, !_accessibilityEnabled);
                } else {
                    preference.setSummary(R.string.accessibility_service_not_used);
                }
            }
        }
        if (key.equals(PREF_FORCE_STOP_APPLICATIONS_ACCESSIBILITY_SETTINGS)) {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                Profile profile = new Profile();
                profile._deviceForceStopApplicationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE, "0"));

                int _isAccessibilityEnabled = profile.isAccessibilityServiceEnabled(context, false);
                boolean _accessibilityEnabled = _isAccessibilityEnabled == 1;

                String summary;
                if (_accessibilityEnabled && (PPApplication.accessibilityServiceForPPPExtenderConnected == 1))
                    summary = getString(R.string.accessibility_service_enabled);
                else {
                    if (_isAccessibilityEnabled == -1) {
                        summary = getString(R.string.accessibility_service_not_used);
                        summary = summary + "\n\n" + getString(R.string.preference_not_used_extender_reason) + " " +
                                getString(R.string.preference_not_allowed_reason_extender_not_upgraded);
                    } else {
                        summary = getString(R.string.accessibility_service_disabled);
                        summary = summary + "\n\n" + getString(R.string.profile_preferences_deviceForceStopApplications_AccessibilitySettingsForExtender_summary);
                    }
                }
                preference.setSummary(summary);

                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, false, false, true, !_accessibilityEnabled);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_GENERATE_NOTIFICATION))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = GenerateNotificationDialogPreference.changeEnabled(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, change, false, false, false);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_CAMERA_FLASH))
        {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
            {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        preference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                }
            } else {
                String sValue = value.toString();
                PPListPreference listPreference = prefMng.findPreference(key);
                if (listPreference != null) {
                    int index = listPreference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                    listPreference.setSummary(summary);

                    Profile profile = new Profile();
                    ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                    profile._cameraFlash = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_CAMERA_FLASH, "0"));
                    Permissions.checkProfileCameraFlash(context, profile, permissions);
                    boolean _permissionGranted = permissions.size() == 0;

                    GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_END_OF_ACTIVATION_TYPE))
        {
            String sValue = value.toString();
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);
//                String durationDefaultValue = Profile.defaultValuesString.get(Profile.PREF_PROFILE_DURATION);
//                String durationValue = preferences.getString(Profile.PREF_PROFILE_DURATION, durationDefaultValue);
//                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true,
//                        (durationValue != null) && (!durationValue.equals(durationDefaultValue)),
//                        false, false, false);
            }

            listPreference = prefMng.findPreference(Profile.PREF_PROFILE_AFTER_DURATION_DO);
            if (listPreference != null) {
                sValue = value.toString();
                if (sValue.equals("1")) {
                    listPreference.setTitle(R.string.profile_preferences_afterExactTimeDo);
                    listPreference.setDialogTitle(R.string.profile_preferences_afterExactTimeDo);
                }
                else {
                    listPreference.setTitle(R.string.profile_preferences_afterDurationDo);
                    listPreference.setDialogTitle(R.string.profile_preferences_afterDurationDo);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_VPN))
        {
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                String sValue = value.toString();
                boolean change = VPNDialogPreference.changeEnabled(sValue);
                GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, change, false, false, false);
            }
        }

    }

    private void setSummaryTones(String key, Object value, Context context, int phoneCount) {
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE))
        {
            String sValue = value.toString();
            PPListPreference listPreference = prefMng.findPreference(key);
            if (listPreference != null) {
                int index = listPreference.findIndexOfValue(sValue);
                CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                listPreference.setSummary(summary);

                Profile profile = new Profile();
                ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                profile._soundRingtoneChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE, "0"));
                profile._soundNotificationChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE, "0"));
                profile._soundAlarmChange = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE, "0"));
                Permissions.checkProfileRingtones(context, profile, permissions);
                boolean _permissionGranted = permissions.size() == 0;

                GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted);
            }
            setSummaryForNotificationVolume0(/*context*/);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE) ||
                key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_SOUND_ALARM))
        {
            setSummaryForNotificationVolume0(/*context*/);
        }

        if ((Build.VERSION.SDK_INT >= 26) &&
                ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                        (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                        (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI))) {

            if (phoneCount > 1) {

                if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1) ||
                        key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2)) {
                    PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                    if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            boolean errorColor = false;
                            if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                preference.setEnabled(false);
                            else
                                errorColor = !value.toString().equals("0");
                            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);

                            if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1)) {
                                preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1);
                                if (preference != null) {
                                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                        preference.setEnabled(false);
                                    else
                                        errorColor = !value.toString().equals("0");
                                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                                }
                            }
                            if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2)) {
                                preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2);
                                if (preference != null) {
                                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                        preference.setEnabled(false);
                                    else
                                        errorColor = !value.toString().equals("0");
                                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                                }
                            }
                        }
                    } else {
                        String sValue = value.toString();
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            int index = listPreference.findIndexOfValue(sValue);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);

                            boolean _permissionGranted;
                            Profile profile = new Profile();
                            ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                            profile._soundRingtoneChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1, "0"));
                            profile._soundRingtoneChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2, "0"));
                            Permissions.checkProfileRingtones(context, profile, permissions);
                            //Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
                            _permissionGranted = permissions.size() == 0;

                            GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted);
                        }
                    }
                }

                if (key.equals(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS)) {
                    PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                    if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            boolean errorColor = false;
                            if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                preference.setEnabled(false);
                            else
                                errorColor = !value.toString().equals("0");
                            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                        }
                    } else {
                        String sValue = value.toString();
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            int index = listPreference.findIndexOfValue(sValue);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);

                            GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, false);
                        }
                    }
                }

                if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1) ||
                        key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2)) {
                    PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                    if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            boolean errorColor = false;
                            if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                preference.setEnabled(false);
                            else
                                errorColor = !value.toString().equals("0");
                            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);

                            if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1)) {
                                preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1);
                                if (preference != null) {
                                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                        preference.setEnabled(false);
                                    else
                                        errorColor = !value.toString().equals("0");
                                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                                }
                            }
                            if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2)) {
                                preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2);
                                if (preference != null) {
                                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                        preference.setEnabled(false);
                                    else
                                        errorColor = !value.toString().equals("0");
                                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                                }
                            }

                        }
                    } else {
                        String sValue = value.toString();
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            int index = listPreference.findIndexOfValue(sValue);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);

                            boolean _permissionGranted;
                            Profile profile = new Profile();
                            ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                            profile._soundNotificationChangeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1, "0"));
                            profile._soundNotificationChangeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2, "0"));
                            Permissions.checkProfileRingtones(context, profile, permissions);
                            _permissionGranted = permissions.size() == 0;

                            GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted);
                        }
                    }
                }
            }
        }

    }

    private void setSummaryRadios(String key, Object value, Context context, int phoneCount)
    {
        if (key.equals(Profile.PREF_PROFILE_DEVICE_AUTOSYNC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_BLUETOOTH) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_GPS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NFC) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS) ||
                key.equals(Profile.PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS))
        {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
            {
                Preference preference = prefMng.findPreference(key);
                if (preference != null) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        preference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed)+
                                ": "+ preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                }
            }
            else
            {
                String sValue = value.toString();
                PPListPreference preference = prefMng.findPreference(key);
                if (preference !=  null) {
                    int index = preference.findIndexOfValue(sValue);
                    CharSequence summary = (index >= 0) ? preference.getEntries()[index] : null;
                    preference.setEnabled(true);
                    preference.setSummary(summary);

                    boolean _permissionGranted = true;
                    if (key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP) ||
                            key.equals(Profile.PREF_PROFILE_DEVICE_BLUETOOTH) ||
                            key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA) ||
                            key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                        Profile profile = new Profile();
                        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                        profile._deviceWiFiAP = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_WIFI_AP, "0"));
                        profile._deviceBluetooth = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_BLUETOOTH, "0"));
                        profile._deviceMobileData = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, "0"));
                        profile._deviceNetworkType = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE, "0"));
                        profile._deviceMobileData = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA, "0"));
                        profile._deviceNetworkTypePrefs = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS, "0"));
                        Permissions.checkProfileRadioPreferences(context, profile, permissions);
                        //Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
                        _permissionGranted = permissions.size() == 0;
                    }

                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, index > 0, false, false, !_permissionGranted);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE)) {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean errorColor; // = false;
                    /*if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SET_AS_ASSISTANT))
                        preference.setEnabled(false);
                    else*/
                        errorColor = !value.toString().equals("0");
                    //if (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_SET_AS_ASSISTANT) {
                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    //}
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                } else {
                    preference.setEnabled(true);

                    String sValue = value.toString();
                    PPListPreference listPreference = prefMng.findPreference(key);
                    if (listPreference != null) {
                        int index = listPreference.findIndexOfValue(sValue);
                        CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                        listPreference.setSummary(summary);

                        Profile profile = new Profile();
                        ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                        profile._deviceAirplaneMode = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE, "0"));
                        Permissions.checkProfileMicrophone(context, profile, permissions);
                        boolean _permissionGranted = permissions.size() == 0;

                        GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted);
                    }
                }
            }
        }
        if (key.equals(PREF_PROFILE_DEVICE_AIRPLANE_MODE_ASSISTANT_SETTINGS)) {
            String summary = getString(R.string.profile_preferences_deviceAirplaneMode_assistantSettings_summary);
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                if (ActivateProfileHelper.isPPPSetAsDefaultAssistant(context)) {
                    summary = getString(R.string.profile_preferences_deviceAirplaneMode_assistantSettings_summary_ststus_1) +
                            "\n\n" + summary;
                }
                else {
                    summary = getString(R.string.profile_preferences_deviceAirplaneMode_assistantSettings_summary_ststus_0) +
                            "\n\n" + summary;
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID)) {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                        (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        preference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                } else {
                    preference.setEnabled(true);

                    String sValue = value.toString();
                    boolean bold = !sValue.equals(Profile.CONNECTTOSSID_JUSTANY);

                    Profile profile = new Profile();
                    ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                    profile._deviceConnectToSSID = preferences.getString(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, Profile.CONNECTTOSSID_JUSTANY);
                    Permissions.checkProfileRadioPreferences(context, profile, permissions);
                    boolean _permissionGranted = permissions.size() == 0;

                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, false, false, !_permissionGranted);
                }
            }
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_VPN)) {
            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
            Preference preference = prefMng.findPreference(key);
            if (preference != null) {
                if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean errorColor = false;
                    if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                            (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                            (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                            (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                        preference.setEnabled(false);
                    else
                        errorColor = !value.toString().equals("0");
                    if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                        preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                } else {
                    preference.setEnabled(true);

                    String sValue = value.toString();
                    boolean bold = !sValue.startsWith("0");

                    Profile profile = new Profile();
                    ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                    profile._deviceVPN = preferences.getString(Profile.PREF_PROFILE_DEVICE_VPN, "0|0|||0");
                    Permissions.checkProfileWireGuard(context, profile, permissions);
                    boolean _permissionGranted = permissions.size() == 0;

                    GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, bold, false, false, !_permissionGranted);
                }
            }
        }
        if (Build.VERSION.SDK_INT >= 26) {

            if (phoneCount > 1) {

                if (key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1) ||
                        key.equals(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2)) {
                    PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                    if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            boolean errorColor = false;
                            if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                preference.setEnabled(false);
                            else
                                errorColor = !value.toString().equals("0");
                            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                        }
                    } else {
                        String sValue = value.toString();
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            listPreference.setEnabled(true);

                            int index = listPreference.findIndexOfValue(sValue);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);

                            boolean _permissionGranted;
                            Profile profile = new Profile();
                            ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                            profile._deviceMobileDataSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1, "0"));
                            profile._deviceMobileDataSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2, "0"));
                            Permissions.checkProfileRadioPreferences(context, profile, permissions);
                            //Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
                            _permissionGranted = permissions.size() == 0;

                            GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted);
                        }
                    }
                }
                if (key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1) ||
                        key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2)) {
                    PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                    if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            boolean errorColor = false;
                            if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                preference.setEnabled(false);
                            else
                                errorColor = !value.toString().equals("0");
                            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                        }
                    } else {
                        String sValue = value.toString();
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            listPreference.setEnabled(true);

                            int index = listPreference.findIndexOfValue(sValue);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);

                            boolean _permissionGranted;
                            Profile profile = new Profile();
                            ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                            profile._deviceNetworkTypeSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1, "0"));
                            profile._deviceNetworkTypeSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2, "0"));
                            Permissions.checkProfileRadioPreferences(context, profile, permissions);
                            _permissionGranted = permissions.size() == 0;

                            GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted);
                        }
                    }
                }
                if (key.equals(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS)) {
                    PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                    if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            boolean errorColor = false;
                            if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                preference.setEnabled(false);
                            else
                                errorColor = !value.toString().equals("0|0|0");
                            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                        }
                    } else {
                        String sValue = value.toString();
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            preference.setEnabled(true);

                            //int index = listPreference.findIndexOfValue(sValue);
                            //CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            //listPreference.setSummary(summary);

                            boolean _permissionGranted;
                            Profile profile = new Profile();
                            ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                            profile._deviceDefaultSIMCards = preferences.getString(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS, "0|0|0");
                            Permissions.checkProfileRadioPreferences(context, profile, permissions);
                            _permissionGranted = permissions.size() == 0;

                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, !sValue.equals("0|0|0"), false, false, !_permissionGranted);
                        }
                    }
                }
                if (key.equals(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1) ||
                        key.equals(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2)) {
                    PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                    if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                        Preference preference = prefMng.findPreference(key);
                        if (preference != null) {
                            boolean errorColor = false;
                            if ((preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) &&
                                (preferenceAllowed.notAllowedReason != PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))
                                preference.setEnabled(false);
                            else
                                errorColor = !value.toString().equals("0");
                            if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_NOT_ALLOWED)
                                preference.setSummary(getString(R.string.profile_preferences_device_not_allowed) +
                                        ": " + preferenceAllowed.getNotAllowedPreferenceReasonString(context));
                            GlobalGUIRoutines.setPreferenceTitleStyleX(preference, true, errorColor, false, false, errorColor);
                        }
                    } else {
                        String sValue = value.toString();
                        PPListPreference listPreference = prefMng.findPreference(key);
                        if (listPreference != null) {
                            listPreference.setEnabled(true);

                            int index = listPreference.findIndexOfValue(sValue);
                            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
                            listPreference.setSummary(summary);

                            boolean _permissionGranted;
                            Profile profile = new Profile();
                            ArrayList<Permissions.PermissionType> permissions = new ArrayList<>();
                            profile._deviceOnOffSIM1 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1, "0"));
                            profile._deviceOnOffSIM2 = Integer.parseInt(preferences.getString(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2, "0"));
                            Permissions.checkProfileRadioPreferences(context, profile, permissions);
                            //Permissions.checkProfileLinkUnkinkAndSpeakerPhone(context, profile, permissions);
                            _permissionGranted = permissions.size() == 0;

                            GlobalGUIRoutines.setPreferenceTitleStyleX(listPreference, true, index > 0, false, false, !_permissionGranted);
                        }
                    }
                }

            }
        }
    }

    private void setSummary(String key) {
        String value;
        if (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR) ||
            key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
            key.equals(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE) ||
            key.equals(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND)) {
            boolean b = preferences.getBoolean(key, false);
            value = Boolean.toString(b);
        }
        else
            value = preferences.getString(key, "");
        setSummary(key, value);
    }

    private void updateAllSummary() {
        if (getActivity() == null)
            return;

        //if (startupSource != PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE)
        //{
        setSummary(Profile.PREF_PROFILE_NAME);
        setSummary(Profile.PREF_PROFILE_ICON);
        setSummary(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR);
        setSummary(Profile.PREF_PROFILE_DURATION);
        setSummary(Profile.PREF_PROFILE_AFTER_DURATION_DO);
        setSummary(Profile.PREF_PROFILE_ASK_FOR_DURATION);
        setSummary(Profile.PREF_PROFILE_AFTER_DURATION_PROFILE);
        setSummary(Profile.PREF_PROFILE_DURATION_NOTIFICATION_SOUND);
        setSummary(Profile.PREF_PROFILE_DURATION_NOTIFICATION_VIBRATE);
        setSummary(Profile.PREF_PROFILE_HIDE_STATUS_BAR_ICON);
        //}
        setSummary(Profile.PREF_PROFILE_VOLUME_RINGER_MODE);
        setSummary(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
        setSummary(Profile.PREF_PROFILE_VOLUME_UNLINK_VOLUMES_APP_SETTINGS);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
        setSummary(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE);
        setSummary(Profile.PREF_PROFILE_SOUND_ALARM);
        setSummary(Profile.PREF_PROFILE_DEVICE_AIRPLANE_MODE);
        setSummary(PREF_PROFILE_DEVICE_AIRPLANE_MODE_ASSISTANT_SETTINGS);
        setSummary(Profile.PREF_PROFILE_DEVICE_WIFI);
        setSummary(Profile.PREF_PROFILE_DEVICE_BLUETOOTH);
        setSummary(Profile.PREF_PROFILE_DEVICE_SCREEN_TIMEOUT);
        setSummary(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA);
        setSummary(Profile.PREF_PROFILE_DEVICE_LOCATION_MODE);
        setSummary(Profile.PREF_PROFILE_DEVICE_GPS);
        setSummary(Profile.PREF_PROFILE_DEVICE_AUTOSYNC);
        setSummary(Profile.PREF_PROFILE_DEVICE_AUTOROTATE);
        setSummary(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
        setSummary(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS);
        setSummary(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
        setSummary(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
        setSummary(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
        setSummary(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER);
        setSummary(Profile.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS);
        setSummary(Profile.PREF_PROFILE_VOLUME_SPEAKER_PHONE);
        setSummary(Profile.PREF_PROFILE_DEVICE_NFC);
        setSummary(Profile.PREF_PROFILE_DEVICE_KEYGUARD);
        setSummary(Profile.PREF_PROFILE_VOLUME_RINGTONE);
        setSummary(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
        setSummary(Profile.PREF_PROFILE_VOLUME_MEDIA);
        setSummary(Profile.PREF_PROFILE_VOLUME_ALARM);
        setSummary(Profile.PREF_PROFILE_VOLUME_SYSTEM);
        setSummary(Profile.PREF_PROFILE_VOLUME_VOICE);
        setSummary(Profile.PREF_PROFILE_DEVICE_BRIGHTNESS);
        setSummary(Profile.PREF_PROFILE_VIBRATION_ON_TOUCH);
        setSummary(Profile.PREF_PROFILE_DEVICE_WIFI_AP);
        setSummary(Profile.PREF_PROFILE_DEVICE_POWER_SAVE_MODE);
        setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE);
        setSummary(Profile.PREF_PROFILE_NOTIFICATION_LED);
        setSummary(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
        setSummary(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS);
        setSummary(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
        setSummary(Profile.PREF_PROFILE_LOCK_DEVICE);
        setSummary(Profile.PREF_PROFILE_DEVICE_CONNECT_TO_SSID);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_WIFI_SCANNING);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_BLUETOOTH_SCANNING);
        setSummary(Profile.PREF_PROFILE_DEVICE_WIFI_AP_PREFS);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_LOCATION_SCANNING);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_MOBILE_CELL_SCANNING);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_ORIENTATION_SCANNING);
        setSummary(Profile.PREF_PROFILE_HEADS_UP_NOTIFICATIONS);
        setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_PREFS);
        setSummary(Profile.PREF_PROFILE_DEVICE_CLOSE_ALL_APPLICATIONS);
        setSummary(Profile.PREF_PROFILE_SCREEN_DARK_MODE);
        setSummary(Profile.PREF_PROFILE_DTMF_TONE_WHEN_DIALING);
        setSummary(Profile.PREF_PROFILE_SOUND_ON_TOUCH);
        setSummary(PREF_LOCK_DEVICE_INSTALL_EXTENDER);
        setSummary(Profile.PREF_PROFILE_VOLUME_DTMF);
        setSummary(Profile.PREF_PROFILE_VOLUME_ACCESSIBILITY);
        setSummary(Profile.PREF_PROFILE_VOLUME_BLUETOOTH_SCO);
        setSummary(Profile.PREF_PROFILE_ALWAYS_ON_DISPLAY);
        setSummary(Profile.PREF_PROFILE_SCREEN_ON_PERMANENT);
        setSummary(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_NOTIFICATION_SCANNING);
        setSummary(PREF_LOCK_DEVICE_ACCESSIBILITY_SETTINGS);
        setSummary(PREF_FORCE_STOP_APPLICATIONS_ACCESSIBILITY_SETTINGS);
        setSummary(Profile.PREF_PROFILE_GENERATE_NOTIFICATION);
        setSummary(Profile.PREF_PROFILE_CAMERA_FLASH);
        setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1);
        setSummary(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2);
        setSummary(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM1);
        setSummary(Profile.PREF_PROFILE_DEVICE_MOBILE_DATA_SIM2);
        setSummary(Profile.PREF_PROFILE_DEVICE_DEFAULT_SIM_CARDS);
        setSummary(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM1);
        setSummary(Profile.PREF_PROFILE_DEVICE_ONOFF_SIM2);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2);
        setSummary(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2);
        setSummary(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2);
        setSummary(Profile.PREF_PROFILE_SOUND_SAME_RINGTONE_FOR_BOTH_SIM_CARDS);
        setSummary(Profile.PREF_PROFILE_DEVICE_LIVE_WALLPAPER);
        setSummary(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOLDER);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_GLOBAL_EVENTS_RUN);
        setSummary(Profile.PREF_PROFILE_DEVICE_VPN_SETTINGS_PREFS);
        setSummary(Profile.PREF_PROFILE_END_OF_ACTIVATION_TYPE);
        setSummary(Profile.PREF_PROFILE_APPLICATION_DISABLE_PERIODIC_SCANNING);
        setSummary(Profile.PREF_PROFILE_DEVICE_VPN);
        setSummary(Profile.PREF_PROFILE_VIBRATION_INTENSITY_RINGING);
        setSummary(Profile.PREF_PROFILE_VIBRATION_INTENSITY_NOTIFICATIONS);
        setSummary(Profile.PREF_PROFILE_VIBRATION_INTENSITY_TOUCH_INTERACTION);

        // disable depended preferences
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_RINGTONE);
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
        disableDependedPref(Profile.PREF_PROFILE_DEVICE_WIFI_AP);
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_RINGER_MODE);
        disableDependedPref(Profile.PREF_PROFILE_VOLUME_ZEN_MODE);
        disableDependedPref(Profile.PREF_PROFILE_AFTER_DURATION_DO);
        disableDependedPref(Profile.PREF_PROFILE_ASK_FOR_DURATION);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2);
        disableDependedPref(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2);
    }

    private boolean getEnableVolumeNotificationByRingtone(String ringtoneValue) {
        boolean enabled = ProfileStatic.getVolumeChange(ringtoneValue);
        if (enabled) {
            int volume = ProfileStatic.getVolumeValue(ringtoneValue);
            return volume > 0;
        }
        else
            return true;
    }

    private boolean getEnableVolumeNotificationVolume0(boolean notificationEnabled, String notificationValue/*, Context context*/) {
        return  notificationEnabled && ActivateProfileHelper.getMergedRingNotificationVolumes() &&
                ApplicationPreferences.applicationUnlinkRingerNotificationVolumes &&
                ProfileStatic.getVolumeChange(notificationValue) && (ProfileStatic.getVolumeValue(notificationValue) == 0);
    }

    private void disableDependedPref(String key, Object value)
    {
        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();

        String sValue = value.toString();

        final String ON = "1";

        boolean enabledMuteSound = preferences.getBoolean(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND, false);
        if (key.equals(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_NOTIFICATION) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_RINGTONE)) {
            if (!enabledMuteSound) {

                String ringtoneValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGTONE, "");
                boolean enabled = (!ActivateProfileHelper.getMergedRingNotificationVolumes() || ApplicationPreferences.applicationUnlinkRingerNotificationVolumes) &&
                        getEnableVolumeNotificationByRingtone(ringtoneValue);
                Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
                if (preference != null)
                    preference.setEnabled(enabled);

                String notificationValue = preferences.getString(Profile.PREF_PROFILE_VOLUME_NOTIFICATION, "");
                enabled = getEnableVolumeNotificationVolume0(enabled, notificationValue);
                preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
                if (preference != null)
                    preference.setEnabled(enabled);

                String ringerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "0");
                enabled = (ProfileStatic.getVolumeChange(ringtoneValue) ||
                        ProfileStatic.getVolumeChange(notificationValue)) &&
                        ringerMode.equals("0");
                preference = prefMng.findPreference("prf_pref_volumeSoundMode_info");
                if (preference != null)
                    preference.setEnabled(enabled);
            }
            else {
                Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_RINGTONE);
                if (preference != null)
                    preference.setEnabled(false);
                preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_NOTIFICATION);
                if (preference != null)
                    preference.setEnabled(false);
            }
        }
        Preference _preference;
        if (enabledMuteSound) {
            _preference = prefMng.findPreference(PREF_VOLUME_NOTIFICATION_VOLUME0);
            if (_preference != null)
                _preference.setEnabled(false);
            _preference = prefMng.findPreference("prf_pref_volumeSoundMode_info");
            if (_preference != null)
                _preference.setEnabled(false);
        }
        _preference = prefMng.findPreference("prf_pref_volumeRingtone0Info");
        if (_preference != null)
            _preference.setEnabled(!enabledMuteSound);
        _preference = prefMng.findPreference("prf_pref_volumeIgnoreSoundModeInfo2");
        if (_preference != null)
            _preference.setEnabled(!enabledMuteSound);
        _preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_SYSTEM);
        if (_preference != null)
            _preference.setEnabled(!enabledMuteSound);
        _preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_MEDIA);
        if (_preference != null)
            _preference.setEnabled(!enabledMuteSound);
        _preference = prefMng.findPreference(Profile.PREF_PROFILE_VOLUME_DTMF);
        if (_preference != null)
            _preference.setEnabled(!enabledMuteSound);

        if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE))
        {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE))
        {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_SOUND_ALARM_CHANGE))
        {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_ALARM);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_WALLPAPER_CHANGE))
        {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER);
            if (preference != null)
                preference.setEnabled(enabled && (sValue.equals("1") || sValue.equals("4")));
            preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOR);
            if (preference != null)
                preference.setEnabled(enabled && (sValue.equals("1") || sValue.equals("3")));
            preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_LIVE_WALLPAPER);
            if (preference != null)
                preference.setEnabled(enabled && sValue.equals("2"));
            preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WALLPAPER_FOLDER);
            if (preference != null)
                preference.setEnabled(enabled && sValue.equals("3"));
        }
        if (key.equals(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_CHANGE))
        {
            boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_RUN_APPLICATION_PACKAGE_NAME);
            if (preference != null)
                preference.setEnabled(enabled);
            preference = prefMng.findPreference(PREF_PROFILE_DEVICE_RUN_APPLICATION_MIUI_PERMISSIONS);
            if (preference != null)
                preference.setEnabled(enabled);
        }
        //if (Build.VERSION.SDK_INT < 30) {
            if (key.equals(Profile.PREF_PROFILE_DEVICE_WIFI_AP)) {
                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(key, null, preferences, true, context);
                if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    boolean enabled = !sValue.equals(ON);
                    PPListPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_WIFI);
                    if (preference != null) {
                        if (!enabled)
                            preference.setValue(Profile.NO_CHANGE_VALUE_STR);
                        preference.setEnabled(enabled);
                    }
                }
            }
        //}
        if (key.equals(Profile.PREF_PROFILE_VOLUME_RINGER_MODE) ||
                key.equals(Profile.PREF_PROFILE_VOLUME_ZEN_MODE)) {
            String ringerMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_RINGER_MODE, "0");
            String zenMode = preferences.getString(Profile.PREF_PROFILE_VOLUME_ZEN_MODE, "0");
            boolean enabled = false;

            PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING, null, preferences, true, context);
            if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                    ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                    (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                    (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED) ||
                    (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))) {
                if (ringerMode.equals("1") || ringerMode.equals("4"))
                    enabled = true;
                if (ringerMode.equals("5")) {
                    if (zenMode.equals("1") || zenMode.equals("2"))
                        enabled = true;
                }
            }
            PPListPreference preference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_WHEN_RINGING);
            if (preference != null) {
                if (!enabled)
                    preference.setValue(Profile.NO_CHANGE_VALUE_STR);
                preference.setEnabled(enabled);
            }

            if ((Build.VERSION.SDK_INT >= 28) && (Build.VERSION.SDK_INT < 33)) {
                enabled = false;
                preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS, null, preferences, true, context);
                if ((preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) ||
                        ((preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) ||
                         (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOTED) ||
                         (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED)||
                         (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_INSTALLED_PPPPS))) {
                    if (ringerMode.equals("1") || ringerMode.equals("4"))
                        enabled = true;
                    if (ringerMode.equals("5")) {
                        if (zenMode.equals("1") || zenMode.equals("2"))
                            enabled = true;
                    }
                }
                preference = prefMng.findPreference(Profile.PREF_PROFILE_VIBRATE_NOTIFICATIONS);
                if (preference != null) {
                    if (!enabled)
                        preference.setValue(Profile.NO_CHANGE_VALUE_STR);
                    preference.setEnabled(enabled);
                }
            }
        }

        if (key.equals(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE)) {
            setSummary(PREF_FORCE_STOP_APPLICATIONS_INSTALL_EXTENDER);
            boolean enabled;
            enabled = PPPExtenderBroadcastReceiver.isEnabled(context/*, PPApplication.VERSION_CODE_EXTENDER_7_0*/, true, false
                    /*, "ProfilesPrefsFragment.disableDependedPref (Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE)"*/);
            //enabled = PPPExtenderBroadcastReceiver.isAccessibilityServiceEnabled(context, true);

            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            if (preference != null) {
                preference.setEnabled(enabled);
                setSummary(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_CHANGE);
            }
            ApplicationsMultiSelectDialogPreference appPreference =
                    prefMng.findPreference(Profile.PREF_PROFILE_DEVICE_FORCE_STOP_APPLICATION_PACKAGE_NAME);
            if (appPreference != null) {
                appPreference.setEnabled(enabled && (!(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR))));
                appPreference.setSummaryAMSDP();
            }
        }

        if (key.equals(Profile.PREF_PROFILE_LOCK_DEVICE)) {
            setSummary(PREF_LOCK_DEVICE_INSTALL_EXTENDER);
            setSummary(PREF_LOCK_DEVICE_ACCESSIBILITY_SETTINGS);
            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_LOCK_DEVICE);
            if (preference != null) {
                setSummary(Profile.PREF_PROFILE_LOCK_DEVICE);
            }
        }

        if (key.equals(Profile.PREF_PROFILE_DURATION) ||
            key.equals(Profile.PREF_PROFILE_AFTER_DURATION_DO) ||
            key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
            key.equals(Profile.PREF_PROFILE_END_OF_ACTIVATION_TYPE)) {

            String endOfActivationType = preferences.getString(Profile.PREF_PROFILE_END_OF_ACTIVATION_TYPE, "0");
            Preference durationPreference = prefMng.findPreference(Profile.PREF_PROFILE_DURATION);
            Preference endOfActivationTimePreference = prefMng.findPreference(Profile.PREF_PROFILE_END_OF_ACTIVATION_TIME);
            if (durationPreference != null)
                durationPreference.setEnabled(endOfActivationType.equals("0"));
            if (endOfActivationTimePreference != null)
                endOfActivationTimePreference.setEnabled(endOfActivationType.equals("1"));

            String duration = preferences.getString(Profile.PREF_PROFILE_DURATION, "0");
            boolean askForDuration = preferences.getBoolean(Profile.PREF_PROFILE_ASK_FOR_DURATION, false);

            boolean enable;
            if (endOfActivationType.equals("0"))
                enable = (!askForDuration) && (!duration.equals("0"));
            else
                enable = true;

            Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_AFTER_DURATION_DO);
            if (preference != null)
                preference.setEnabled(enable);

            preference = prefMng.findPreference(Profile.PREF_PROFILE_AFTER_DURATION_PROFILE);
            if (preference != null) {
                String afterDurationDo = preferences.getString(Profile.PREF_PROFILE_AFTER_DURATION_DO, "0");
                int afterDurationDoValue = Integer.parseInt(afterDurationDo);
                preference.setEnabled(enable &&
                        ((afterDurationDoValue ==  Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE) ||
                         (afterDurationDoValue ==  Profile.AFTER_DURATION_DO_SPECIFIC_PROFILE_THEN_RESTART_EVENTS)));
            }
        }

        if ((Build.VERSION.SDK_INT >= 26) &&
                ((PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) ||
                        (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) ||
                        (PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI))) {
            if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM1)) {
                boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
                Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM1);
                if (preference != null)
                    preference.setEnabled(enabled);
            }
            if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM1)) {
                boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
                Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM1);
                if (preference != null)
                    preference.setEnabled(enabled);
            }
            if (key.equals(Profile.PREF_PROFILE_SOUND_RINGTONE_CHANGE_SIM2)) {
                boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
                Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_RINGTONE_SIM2);
                if (preference != null)
                    preference.setEnabled(enabled);
            }
            if (key.equals(Profile.PREF_PROFILE_SOUND_NOTIFICATION_CHANGE_SIM2)) {
                boolean enabled = !(/*sValue.equals(Profile.SHARED_PROFILE_VALUE_STR) ||*/ sValue.equals(Profile.NO_CHANGE_VALUE_STR));
                Preference preference = prefMng.findPreference(Profile.PREF_PROFILE_SOUND_NOTIFICATION_SIM2);
                if (preference != null)
                    preference.setEnabled(enabled);
            }
        }
        /*if (Build.VERSION.SDK_INT >= 26) {
            if (key.equals(PREF_PROFILE_DEVICE_AIRPLANE_MODE_ASSISTANT_SETTINGS)) {
                // RECORD_AUDIO must be granted for set PPP as default assistant
                // must be enabled when PPP is defult assistant
                // because must be possible remove it
                boolean enabled = ActivateProfileHelper.isPPPSetAsDefaultAssistant(context) ||
                                    Permissions.checkMicrophone(context);
                Preference preference = prefMng.findPreference(PREF_PROFILE_DEVICE_AIRPLANE_MODE_ASSISTANT_SETTINGS);
                if (preference != null)
                    preference.setEnabled(enabled);
            }
        }*/
    }

    private void disableDependedPref(String key) {
        String value;
        if (key.equals(Profile.PREF_PROFILE_SHOW_IN_ACTIVATOR) ||
            key.equals(Profile.PREF_PROFILE_ASK_FOR_DURATION) ||
            key.equals(Profile.PREF_PROFILE_VOLUME_MUTE_SOUND)) {
            boolean b = preferences.getBoolean(key, false);
            value = Boolean.toString(b);
        }
        else
            value = preferences.getString(key, "");
        disableDependedPref(key, value);
    }

    static boolean isRedTextNotificationRequired(Profile profile, boolean againCheckAccessibilityInDelay, Context context) {
        boolean grantedAllPermissions = Permissions.checkProfilePermissions(context, profile).size() == 0;
        /*if (Build.VERSION.SDK_INT >= 29) {
            if (!Settings.canDrawOverlays(context))
                grantedAllPermissions = false;
        }*/
        // test only root or G1 parameters, because key is not set but profile is
        PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed("-", profile, null, true, context);
        boolean grantedRoot = true;
        //if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
        if (preferenceAllowed.notAllowedRoot) {
            if (!ApplicationPreferences.applicationNeverAskForGrantRoot)
            //if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) && (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED))
                grantedRoot = false;
        }
        //preferenceAllowed = Profile.isProfilePreferenceAllowed("-", profile, null, false, true, true, context);
        boolean grantedG1Permission = true;
        if (preferenceAllowed.notAllowedG1) {
            //if (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION)
                grantedG1Permission = false;
        }

        boolean installedPPPPS = true;
        if (preferenceAllowed.notAllowedPPPPS) {
            installedPPPPS = false;
        }

        boolean enabledNotificationAccess = /*(profile._volumeRingerMode == 0) ||*/ ActivateProfileHelper.canChangeZenMode(context);

        boolean accessibilityNotRequired = true;
        if ((profile != null) && ((profile._lockDevice == 3) || (profile._deviceForceStopApplicationChange != 0)))
            accessibilityNotRequired = false;
        boolean accessibilityEnabled = accessibilityNotRequired || (profile.isAccessibilityServiceEnabled(context.getApplicationContext(), againCheckAccessibilityInDelay) == 1);

        boolean defaultAssistantNotRequired = true;
        if ((profile != null) && (profile._deviceAirplaneMode >= 4))
            defaultAssistantNotRequired = false;
        boolean defaultAssistantEnabled = defaultAssistantNotRequired || (ActivateProfileHelper.isPPPSetAsDefaultAssistant(context.getApplicationContext()));

        if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED)
            return (!grantedAllPermissions) ||
                    (!enabledNotificationAccess) ||
                    (!accessibilityEnabled) ||
                    (!defaultAssistantEnabled)  ||
                    (!installedPPPPS);
        else
            return (!grantedAllPermissions) ||
                    (!grantedRoot) ||
                    (!grantedG1Permission) ||
                    (!enabledNotificationAccess) ||
                    (!accessibilityEnabled) ||
                    (!defaultAssistantEnabled)  ||
                    (!installedPPPPS);
    }

    void setRedTextToPreferences() {
        if (nestedFragment)
            return;

        if (getActivity() == null)
            return;

        final ProfilesPrefsActivity activity = (ProfilesPrefsActivity)getActivity();

        Context context = activity.getApplicationContext();

        String rootScreen = "rootScreen";

        boolean hidePreferences = false;
        long profile_id = activity.profile_id;
        if (profile_id != 0) {
            int order = 1;

            int newProfileMode = activity.newProfileMode;
            int predefinedProfileIndex = activity.predefinedProfileIndex;

            final Profile profile = ((ProfilesPrefsActivity) getActivity())
                    .getProfileFromPreferences(profile_id, newProfileMode, predefinedProfileIndex);
            if (profile != null) {

                // test only root or G1 parameters, because key is not set but profile is
                PreferenceAllowed preferenceAllowed = ProfileStatic.isProfilePreferenceAllowed("-", profile, null, true, context);

                // not enabled G1 preferences
/*                Preference preference = prefMng.findPreference(PRF_GRANT_G1_PREFERENCES);
                if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                    preference = prefMng.findPreference(PRF_GRANT_ROOT);
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                }
                else {*/
                Preference preference = prefMng.findPreference(PRF_GRANT_G1_PREFERENCES);
                if (!preferenceAllowed.notAllowedG1) {
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                } else {
                    //if (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_GRANTED_G1_PERMISSION) {
                    if (preference == null) {
                        PreferenceScreen preferenceCategory = findPreference(rootScreen);
                        if (preferenceCategory != null) {
                            preference = new Preference(context);
                            preference.setKey(PRF_GRANT_G1_PREFERENCES);
                            preference.setIconSpaceReserved(false);
                            preference.setWidgetLayoutResource(R.layout.preference_widget_preference_with_subpreferences);
                            preference.setLayoutResource(R.layout.mp_preference_material_widget);
                            preference.setOrder(-100);
                            preferenceCategory.addPreference(preference);
                        }
                    }
                    if (preference != null) {
                        String _title = order + ". " + getString(R.string.preferences_grantG1Preferences_title);
                        ++order;
                        Spannable title = new SpannableString(_title);
                        title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                        preference.setTitle(title);
                        Spannable summary = new SpannableString(getString(R.string.preferences_grantG1Preferences_summary));
                        summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                        preference.setSummary(summary);

                        final ProfilesPrefsFragment fragment = this;
                        preference.setOnPreferenceClickListener(preference12 -> {
                            Permissions.grantG1Permission(fragment, activity);
                            return false;
                        });
                    }
                    //}
                }

                //preferenceAllowed = Profile.isProfilePreferenceAllowed("-", profile, null, true, false, true, context);
                // not enabled grant root
                //if (preferenceAllowed.allowed == PreferenceAllowed.PREFERENCE_ALLOWED) {
                if (!preferenceAllowed.notAllowedRoot) {
                    preference = prefMng.findPreference(PRF_GRANT_ROOT);
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                } else {
                    //if ((!ApplicationPreferences.applicationNeverAskForGrantRoot) && (preferenceAllowed.notAllowedReason == PreferenceAllowed.PREFERENCE_NOT_ALLOWED_NOT_ROOT_GRANTED)) {
                    if (!ApplicationPreferences.applicationNeverAskForGrantRoot) {
                        preference = prefMng.findPreference(PRF_GRANT_ROOT);
                        if (preference == null) {
                            PreferenceScreen preferenceCategory = findPreference(rootScreen);
                            if (preferenceCategory != null) {
                                preference = new Preference(context);
                                preference.setKey(PRF_GRANT_ROOT);
                                preference.setIconSpaceReserved(false);
                                preference.setWidgetLayoutResource(R.layout.preference_widget_preference_with_subpreferences);
                                preference.setLayoutResource(R.layout.mp_preference_material_widget);
                                preference.setOrder(-100);
                                preferenceCategory.addPreference(preference);
                            }
                        }
                        if (preference != null) {
                            String _title = order + ". " + getString(R.string.preferences_grantRoot_title);
                            ++order;
                            Spannable title = new SpannableString(_title);
                            title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                            preference.setTitle(title);
                            Spannable summary = new SpannableString(getString(R.string.preferences_grantRoot_summary));
                            summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                            preference.setSummary(summary);

                            final ProfilesPrefsFragment fragment = this;
                            preference.setOnPreferenceClickListener(preference13 -> {
                                Permissions.grantRootX(fragment, activity);
                                return false;
                            });
                        }
                    }
                }
                //}

                // not some permissions
                if (Permissions.checkProfilePermissions(context, profile).size() == 0) {
                    preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = prefMng.findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                } else {
                    preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
                    if (preference == null) {
                        PreferenceScreen preferenceCategory = findPreference(rootScreen);
                        if (preferenceCategory != null) {
                            preference = new Preference(context);
                            preference.setKey(PRF_GRANT_PERMISSIONS);
                            preference.setIconSpaceReserved(false);
                            if (profile._id > 0)
                                preference.setWidgetLayoutResource(R.layout.preference_widget_preference_with_subpreferences);
                            else
                                preference.setWidgetLayoutResource(R.layout.preference_widget_exclamation_preference);
                            preference.setLayoutResource(R.layout.mp_preference_material_widget);
                            preference.setOrder(-100);
                            preferenceCategory.addPreference(preference);
                        }
                    }
                    if (preference != null) {
                        String _title = order + ". " + getString(R.string.preferences_grantPermissions_title);
                        ++order;
                        Spannable title = new SpannableString(_title);
                        title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                        preference.setTitle(title);
                        Spannable summary = new SpannableString(getString(R.string.preferences_grantPermissions_summary));
                        summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                        preference.setSummary(summary);

                        if (profile._id > 0) {
                            preference.setOnPreferenceClickListener(preference1 -> {
                                //Profile mappedProfile = Profile.getMappedProfile(profile, appContext);
                                Permissions.grantProfilePermissions(activity, profile/*, false, false,*/
                                        /*true, false, 0,*/ /*PPApplication.STARTUP_SOURCE_EDITOR, false, false, true*/);
                                return false;
                            });
                        }
                    }
                }

                // not enabled notification access
                if (/*(profile._volumeRingerMode == 0) ||*/ ActivateProfileHelper.canChangeZenMode(context)) {
                    preference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS_ENABLED);
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                } else {
                    preference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS_ENABLED);
                    if (preference == null) {
                        PreferenceScreen preferenceCategory = findPreference(rootScreen);
                        if (preferenceCategory != null) {
                            preference = new Preference(context);
                            preference.setKey(PREF_NOTIFICATION_ACCESS_ENABLED);
                            preference.setIconSpaceReserved(false);
                            preference.setWidgetLayoutResource(R.layout.preference_widget_preference_with_subpreferences);
                            preference.setLayoutResource(R.layout.mp_preference_material_widget);
                            preference.setOrder(-100);
                            preferenceCategory.addPreference(preference);
                        }
                    }
                    if (preference != null) {
                        String _title = order + ". ";
                        String _summary;
                        //boolean a60 = /*(android.os.Build.VERSION.SDK_INT == 23) &&*/ Build.VERSION.RELEASE.equals("6.0");
                        //final boolean showDoNotDisturbPermission =
                        /*(android.os.Build.VERSION.SDK_INT >= 23) &&*/ /*(!a60) &&*/
                        //                GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, getActivity().getApplicationContext());
                        //if (showDoNotDisturbPermission) {
                        _title = _title + getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions);
                        _summary = getString(R.string.profile_preferences_red_volumeNotificationsAccessSettings_summary_2);
                        //} else {
                        //    _title = _title + getString(R.string.profile_preferences_volumeNotificationsAccessSettings_title);
                        //    _summary = getString(R.string.profile_preferences_red_volumeNotificationsAccessSettings_summary_notification_access);
                        //}
                        ++order;
                        Spannable title = new SpannableString(_title);
                        title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                        preference.setTitle(title);
                        Spannable summary = new SpannableString(_summary);
                        summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                        preference.setSummary(summary);

                        preference.setOnPreferenceClickListener(preference14 -> {
                            enableNotificationAccess(true/*showDoNotDisturbPermission*/);
                            return false;
                        });
                    }
                }

                // not enabled accessibility service
                int accessibilityEnabled = profile.isAccessibilityServiceEnabled(context.getApplicationContext(), false);
                /*if (accessibilityEnabled == 1) {
                    int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(context);
                    if (extenderVersion != 0) {
                        // PPPE is installed
                        if (PPApplication.accessibilityServiceForPPPExtenderConnected == 2)
                            // Extender is not connected
                            accessibilityEnabled = 0;
                    }
                }*/
                preference = prefMng.findPreference(PRF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
                if (accessibilityEnabled == 1) {
                    if (preference != null) {
                        PreferenceScreen preferenceCategory = findPreference(rootScreen);
                        if (preferenceCategory != null)
                            preferenceCategory.removePreference(preference);
                    }
                } else {
                    if (preference == null) {
                        PreferenceScreen preferenceCategory = findPreference(rootScreen);
                        if (preferenceCategory != null) {
                            preference = new Preference(context);
                            preference.setKey(PRF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
                            preference.setIconSpaceReserved(false);
                            preference.setWidgetLayoutResource(R.layout.preference_widget_preference_with_subpreferences);
                            preference.setLayoutResource(R.layout.mp_preference_material_widget);
                            preference.setOrder(-97);
                            preferenceCategory.addPreference(preference);
                        }
                    }
                    if (preference != null) {
                        int stringRes = R.string.preferences_not_enabled_accessibility_service_title;
                        if (accessibilityEnabled == -2)
                            stringRes = R.string.preferences_not_installed_PPPExtender_title;
                        else if (accessibilityEnabled == -1)
                            stringRes = R.string.preferences_old_version_PPPExtender_title;
                        String _title = order + ". " + getString(stringRes);
                        ++order;
                        Spannable title = new SpannableString(_title);
                        title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                        preference.setTitle(title);
                        if ((accessibilityEnabled == -1) || (accessibilityEnabled == -2)) {
                            _title = getString(R.string.event_preferences_red_install_PPPExtender);
                            Spannable summary = new SpannableString(_title);
                            summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                            preference.setSummary(summary);

                            preference.setOnPreferenceClickListener(preference15 -> {
                                installExtender();
                                return false;
                            });
                        } else {
                            _title = getString(R.string.event_preferences_red_enable_PPPExtender);
                            Spannable summary = new SpannableString(_title);
                            summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                            preference.setSummary(summary);

                            preference.setOnPreferenceClickListener(preference16 -> {
                                enableExtender();
                                return false;
                            });
                        }
                    }
                }

                // not installed PPPPs
                if (preferenceAllowed.notAllowedPPPPS) {
                    boolean installedPPPPS = ActivateProfileHelper.isPPPPutSSettingsInstalled(context);
                    preference = prefMng.findPreference(PRF_NOT_INSTALLED_PPPPS);
                    if (installedPPPPS) {
                        if (preference != null) {
                            PreferenceScreen preferenceCategory = findPreference(rootScreen);
                            if (preferenceCategory != null)
                                preferenceCategory.removePreference(preference);
                        }
                    } else {
                        if (preference == null) {
                            PreferenceScreen preferenceCategory = findPreference(rootScreen);
                            if (preferenceCategory != null) {
                                preference = new Preference(context);
                                preference.setKey(PRF_NOT_INSTALLED_PPPPS);
                                preference.setIconSpaceReserved(false);
                                preference.setWidgetLayoutResource(R.layout.preference_widget_preference_with_subpreferences);
                                preference.setLayoutResource(R.layout.mp_preference_material_widget);
                                preference.setOrder(-97);
                                preferenceCategory.addPreference(preference);
                            }
                        }
                        if (preference != null) {
                            int stringRes = R.string.preferences_not_installed_PPPPutSettings_title;
                            String _title = order + ". " + getString(stringRes);
                            ++order;
                            Spannable title = new SpannableString(_title);
                            title.setSpan(new ForegroundColorSpan(Color.RED), 0, title.length(), 0);
                            preference.setTitle(title);
                            _title = getString(R.string.event_preferences_red_install_PPPExtender);
                            Spannable summary = new SpannableString(_title);
                            summary.setSpan(new ForegroundColorSpan(Color.RED), 0, summary.length(), 0);
                            preference.setSummary(summary);

                            preference.setOnPreferenceClickListener(preference15 -> {
                                installPPPPutSettings();
                                return false;
                            });
                        }
                    }
                }
            }
            else
                hidePreferences = true;
        }
        else
            hidePreferences = true;

        if (hidePreferences) {
            Preference preference = prefMng.findPreference(PRF_GRANT_PERMISSIONS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(rootScreen);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PRF_GRANT_G1_PREFERENCES);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(rootScreen);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PRF_GRANT_ROOT);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(rootScreen);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PREF_NOTIFICATION_ACCESS_ENABLED);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(rootScreen);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = prefMng.findPreference(PRF_NOT_ENABLED_ACCESSIBILITY_SERVICE);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference(rootScreen);
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }

    }

    private void enableNotificationAccess(
            @SuppressWarnings("SameParameterValue") boolean showDoNotDisturbPermission) {
        boolean ok = false;
        if (showDoNotDisturbPermission) {
            // Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS exists
            try {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                //noinspection deprecation
                startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                ok = true;
            } catch (Exception e) {
                if (getActivity() != null) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            getString(R.string.phone_profiles_pref_notificationSystemSettings),
                            getString(R.string.setting_screen_not_found_alert),
                            getString(android.R.string.ok),
                            null,
                            null, null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            true, true,
                            false, false,
                            true,
                            getActivity()
                    );

                    if (!getActivity().isFinishing())
                        dialog.show();
                }
            }
        }
        /*else
        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS, getActivity())) {
            try {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivityForResult(intent, RESULT_NOTIFICATION_ACCESS_SETTINGS);
                ok = true;
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }*/
        if (!ok) {
            if (getActivity() != null) {
                PPAlertDialog dialog = new PPAlertDialog(
                        getString(R.string.phone_profiles_pref_notificationSystemSettings),
                        getString(R.string.setting_screen_not_found_alert),
                        getString(android.R.string.ok),
                        null,
                        null, null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true, true,
                        false, false,
                        true,
                        getActivity()
                );

                if (!getActivity().isFinishing())
                    dialog.show();
            }
        }
    }

    private void installExtenderFromGitHub() {
        if (getActivity() == null) {
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.install_extender_dialog_title);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_install_ppp_pppe_from_github, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.install_ppp_pppe_from_github_dialog_info_text);

        String dialogText = "";

        int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(getActivity().getApplicationContext());
        if (extenderVersion != 0) {
            String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(getActivity().getApplicationContext());
            dialogText = dialogText + getString(R.string.install_extender_installed_version) + " " + extenderVersionName + " (" + extenderVersion + ")\n";
        }
        dialogText = dialogText + getString(R.string.install_extender_required_version) +
                " " + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")\n\n";
        dialogText = dialogText + getString(R.string.install_extender_text1) + " \"" + getString(R.string.alert_button_install) + "\".\n\n";
        dialogText = dialogText + getString(R.string.install_extender_text2) + "\n\n";
        dialogText = dialogText + getString(R.string.install_extender_text3);

        text.setText(dialogText);

        text = layout.findViewById(R.id.install_ppp_pppe_from_github_dialog_github_releases);
        CharSequence str1 = getString(R.string.install_extender_github_releases);
        CharSequence str2 = str1 + " " + PPApplication.GITHUB_PPPE_RELEASES_URL + " \u21D2";
        Spannable sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = PPApplication.GITHUB_PPPE_RELEASES_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    if (getActivity() != null)
                        getActivity().startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
            //String url = PPApplication.GITHUB_PPPE_DOWNLOAD_URL_1 + PPApplication.VERSION_NAME_EXTENDER_LATEST + PPApplication.GITHUB_PPPE_DOWNLOAD_URL_2;
            String url = PPApplication.GITHUB_PPPE_DOWNLOAD_URL;

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = dialogBuilder.create();

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if ((getActivity() != null) && (!getActivity().isFinishing()))
            dialog.show();
    }

    private void installExtender() {
        if (getActivity() == null) {
            return;
        }

        if (PPApplication.deviceIsSamsung && PPApplication.romIsGalaxy) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder.setTitle(R.string.install_extender_dialog_title);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_install_pppe_from_store, null);
            dialogBuilder.setView(layout);

            TextView text = layout.findViewById(R.id.install_pppe_from_store_dialog_info_text);

            String dialogText = "";

            int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(getActivity().getApplicationContext());
            if (extenderVersion != 0) {
                String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(getActivity().getApplicationContext());
                dialogText = dialogText + getString(R.string.install_extender_installed_version) + " " + extenderVersionName + " (" + extenderVersion + ")\n";
            }
            dialogText = dialogText + getString(R.string.install_extender_required_version) +
                    " " + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")\n\n";
            dialogText = dialogText + getString(R.string.install_extender_text1) + " \"" + getString(R.string.alert_button_install) + "\".";

            text.setText(dialogText);

            dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("samsungapps://ProductDetail/sk.henrichg.phoneprofilesplusextender"));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            Button button = layout.findViewById(R.id.install_pppe_from_store_dialog_installFromGitHub);

            final AlertDialog dialog = dialogBuilder.create();

            button.setText(getActivity().getString(R.string.alert_button_install_extender_from_github));
            button.setOnClickListener(v -> {
                dialog.cancel();
                installExtenderFromGitHub();
            });

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

            if ((getActivity() != null) && (!getActivity().isFinishing()))
                dialog.show();
        }
/*        else if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder.setTitle(R.string.install_extender_dialog_title);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_install_pppe_from_store, null);
            dialogBuilder.setView(layout);

            TextView text = layout.findViewById(R.id.install_pppe_from_store_dialog_info_text);

            String dialogText = "";

            int extenderVersion = PPPExtenderBroadcastReceiver.isExtenderInstalled(getActivity().getApplicationContext());
            if (extenderVersion != 0) {
                String extenderVersionName = PPPExtenderBroadcastReceiver.getExtenderVersionName(getActivity().getApplicationContext());
                dialogText = dialogText + getString(R.string.install_extender_installed_version) + " " + extenderVersionName + " (" + extenderVersion + ")\n";
            }
            dialogText = dialogText + getString(R.string.install_extender_required_version) +
                    " " + PPApplication.VERSION_NAME_EXTENDER_LATEST + " (" + PPApplication.VERSION_CODE_EXTENDER_LATEST + ")\n\n";
            dialogText = dialogText + getString(R.string.install_extender_text1) + " \"" + getString(R.string.alert_button_install) + "\".\n\n";

            text.setText(dialogText);

            dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("appmarket://details?id=sk.henrichg.phoneprofilesplusextender"));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            });
            dialogBuilder.setNegativeButton(android.R.string.cancel, null);

            Button button = layout.findViewById(R.id.install_pppe_from_store_dialog_installFromGitHub);

            final AlertDialog dialog = dialogBuilder.create();

            //button.setText(getActivity().getString(R.string.alert_button_install_extender_from_github));
            button.setOnClickListener(v -> {
                dialog.cancel();
                installExtenderFromGitHub();
            });

//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

            if ((getActivity() != null) && (!getActivity().isFinishing()))
                dialog.show();
        }*/
        else
            installExtenderFromGitHub();
    }

    private void enableExtender() {
        if (getActivity() == null)
            return;

        boolean ok = false;
        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, getActivity())) {
            try {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                //noinspection deprecation
                startActivityForResult(intent, RESULT_ACCESSIBILITY_SETTINGS);
                ok = true;
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
        if (!ok) {
            if (getActivity() != null) {
                PPAlertDialog dialog = new PPAlertDialog(
                        getString(R.string.event_preferences_applications_AccessibilitySettings_title),
                        getString(R.string.setting_screen_not_found_alert),
                        getString(android.R.string.ok),
                        null,
                        null, null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true, true,
                        false, false,
                        true,
                        getActivity()
                );

                if (!getActivity().isFinishing())
                    dialog.show();
            }
        }
    }

    private void configureAssistant() {
        if (getActivity() == null)
            return;

        boolean ok = false;
        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_ACCESSIBILITY_SETTINGS, getActivity())) {
            try {
                //activity.startActivity(new Intent("android.settings.VOICE_INPUT_SETTINGS"));

                Intent intent = new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS);
                //noinspection deprecation
                startActivityForResult(intent, RESULT_ASSISTANT_SETTINGS);
                ok = true;
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        }
        if (!ok) {
            if (getActivity() != null) {
                PPAlertDialog dialog = new PPAlertDialog(
                        getString(R.string.profile_preferences_deviceAirplaneMode_assistantSettings),
                        getString(R.string.setting_screen_not_found_alert),
                        getString(android.R.string.ok),
                        null,
                        null, null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        true, true,
                        false, false,
                        true,
                        getActivity()
                );

                if (!getActivity().isFinishing())
                    dialog.show();
            }
        }
    }

    private void installPPPPutSettings() {
        if (getActivity() == null) {
            return;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.install_pppps_dialog_title);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_install_pppps, null);
        dialogBuilder.setView(layout);

        TextView text = layout.findViewById(R.id.install_pppps_from_github_dialog_info_text);

        String dialogText = "";

        dialogText = dialogText + getString(R.string.install_pppps_text1) + " \"" + getString(R.string.alert_button_install) + "\"\n";
        dialogText = dialogText + getString(R.string.install_pppps_text2) + "\n";
        dialogText = dialogText + getString(R.string.install_pppps_text3) + "\n\n";
        dialogText = dialogText + getString(R.string.install_pppps_text4);
        text.setText(dialogText);

        dialogBuilder.setPositiveButton(R.string.alert_button_install, (dialog, which) -> {
            String url = PPApplication.GITHUB_PPPPS_DOWNLOAD_URL;

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            try {
                startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);
        dialogBuilder.setCancelable(false);
        /*dialogBuilder.setOnCancelListener(dialog -> {
            if (finishActivity)
                activity.finish();
        });*/

        final AlertDialog dialog = dialogBuilder.create();

        text = layout.findViewById(R.id.install_pppps_from_github_dialog_github_releases);
        CharSequence str1 = getString(R.string.install_extender_github_releases);
        CharSequence str2 = str1 + " " + PPApplication.GITHUB_PPPPS_RELEASES_URL + " \u21D2";
        Spannable sbt = new SpannableString(str2);
        sbt.setSpan(new StyleSpan(android.graphics.Typeface.NORMAL), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = PPApplication.GITHUB_PPPPS_RELEASES_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    dialog.cancel();
                    //if (activity != null)
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplication.recordException(e);
                }
            }
        };
        sbt.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        text.setText(sbt);
        text.setMovementMethod(LinkMovementMethod.getInstance());


//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);
//            }
//        });

        if ((getActivity() != null) && (!getActivity().isFinishing()))
            dialog.show();

    }

    private void fillDeviceNetworkTypePreference(String key, Context context) {
        PPListPreference networkTypePreference = prefMng.findPreference(key);
        if (networkTypePreference != null) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (!key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE)) {
                int subscriptionId = -1;

                SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                //SubscriptionManager.from(context);
                if (mSubscriptionManager != null) {
                    List<SubscriptionInfo> subscriptionList = null;
                    try {
                        if (Permissions.hasPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                            // Loop through the subscription list i.e. SIM list.
                            subscriptionList = mSubscriptionManager.getActiveSubscriptionInfoList();
                        }
                    } catch (SecurityException e) {
                        PPApplication.recordException(e);
                    }
                    if (subscriptionList != null) {
                        for (int i = 0; i < subscriptionList.size();/*mSubscriptionManager.getActiveSubscriptionInfoCountMax();*/ i++) {
                            // Get the active subscription ID for a given SIM card.
                            SubscriptionInfo subscriptionInfo = subscriptionList.get(i);
                            if (subscriptionInfo != null) {
                                int slotIndex = subscriptionInfo.getSimSlotIndex();
                                if ((slotIndex == 0) && key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM1))
                                    subscriptionId = subscriptionInfo.getSubscriptionId();
                                if ((slotIndex == 1) && key.equals(Profile.PREF_PROFILE_DEVICE_NETWORK_TYPE_SIM2))
                                    subscriptionId = subscriptionInfo.getSubscriptionId();
                            }
                        }
                    }
                }

                if (subscriptionId != -1) {
                    telephonyManager = telephonyManager.createForSubscriptionId(subscriptionId);
                }
            }

            int phoneType = TelephonyManager.PHONE_TYPE_GSM;
            if (telephonyManager != null)
                phoneType = telephonyManager.getPhoneType();

            if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
                    /*if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeGSMDPArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeGSMDPValues));
                    } else {*/

                // https://github.com/aosp-mirror/platform_frameworks_base/blob/master/telephony/java/com/android/internal/telephony/RILConstants.java
                networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeGSMArray));
                networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeGSMValues));

                //}
                String value = preferences.getString(key, "");
                networkTypePreference.setValue(value);
                setSummary(key, value);
            }

            if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                    /*if (startupSource == PPApplication.PREFERENCES_STARTUP_SOURCE_SHARED_PROFILE) {
                        networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeCDMADPArray));
                        networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeCDMADPValues));
                    } else {*/

                // https://github.com/aosp-mirror/platform_frameworks_base/blob/master/telephony/java/com/android/internal/telephony/RILConstants.java
                networkTypePreference.setEntries(context.getResources().getStringArray(R.array.networkTypeCDMAArray));
                networkTypePreference.setEntryValues(context.getResources().getStringArray(R.array.networkTypeCDMAValues));

                //}
                String value = preferences.getString(key, "");
                networkTypePreference.setValue(value);
                setSummary(key, value);
            }
        }
    }

    private void setRingtonePreferenceSummary(final String initSummary, final String ringtoneUri,
                                             final androidx.preference.Preference preference, final Context context) {
        SetRingtonePreferenceSummaryAsyncTask asyncTask =
                new SetRingtonePreferenceSummaryAsyncTask(initSummary, ringtoneUri, preference, context);
        asyncTask.execute();
    }

    private static class SetRingtonePreferenceSummaryAsyncTask extends AsyncTask<Void, Integer, Void> {
        private String ringtoneName;

        final String initSummary;
        final String ringtoneUri;
        private final WeakReference<Preference> preferenceWeakRef;
        private final WeakReference<Context> contextWeakReference;

        public SetRingtonePreferenceSummaryAsyncTask(final String initSummary, final String ringtoneUri,
                                                     final androidx.preference.Preference preference, final Context context) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.contextWeakReference = new WeakReference<>(context);
            this.initSummary = initSummary;
            this.ringtoneUri = ringtoneUri;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Context context = contextWeakReference.get();
            androidx.preference.Preference preference = preferenceWeakRef.get();
            if ((context != null) && (preference != null)) {
                if ((ringtoneUri == null) || ringtoneUri.isEmpty())
                    ringtoneName = context.getString(R.string.ringtone_preference_none);
                else {
                    Uri uri = Uri.parse(ringtoneUri);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        ringtoneName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        ringtoneName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Context context = contextWeakReference.get();
            androidx.preference.Preference preference = preferenceWeakRef.get();
            if ((context != null) && (preference != null)) {
                String summary = TextUtils.replace(initSummary, new String[]{"<ringtone_name>"}, new String[]{ringtoneName}).toString();
                preference.setSummary(StringFormatUtils.fromHtml(summary, false, false, false, 0, 0, true));
            }
        }

    }

    private void setProfileSoundsPreferenceSummary(final String initSummary,
                                                  final String ringtoneUri, final String notificationUri, final String alarmUri,
                                                  final androidx.preference.Preference preference, final Context context) {
        SetProfileSoundsPreferenceSummaryAsyncTask asyncTask =
                new SetProfileSoundsPreferenceSummaryAsyncTask(initSummary,
                        ringtoneUri, notificationUri, alarmUri,
                        preference, context);
        asyncTask.execute();
    }

    private static class SetProfileSoundsPreferenceSummaryAsyncTask extends AsyncTask<Void, Integer, Void> {
        private String ringtoneName;
        private String notificationName;
        private String alarmName;

        final String initSummary;
        final String ringtoneUri;
        final String notificationUri;
        final String alarmUri;
        private final WeakReference<androidx.preference.Preference> preferenceWeakRef;
        private final WeakReference<Context> contextWeakReference;

        public SetProfileSoundsPreferenceSummaryAsyncTask(final String initSummary,
                                                          final String ringtoneUri, final String notificationUri, final String alarmUri,
                                                          final androidx.preference.Preference preference, final Context context) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.contextWeakReference = new WeakReference<>(context);
            this.initSummary = initSummary;
            this.ringtoneUri = ringtoneUri;
            this.notificationUri = notificationUri;
            this.alarmUri = alarmUri;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Context context = contextWeakReference.get();
            androidx.preference.Preference preference = preferenceWeakRef.get();
            if ((context != null) && (preference != null)) {
                if ((ringtoneUri == null) || ringtoneUri.isEmpty())
                    ringtoneName = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = ringtoneUri.split("\\|");
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        ringtoneName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        ringtoneName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                if ((notificationUri == null) || notificationUri.isEmpty())
                    notificationName = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = notificationUri.split("\\|");
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        notificationName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        notificationName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                if ((alarmUri == null) || alarmUri.isEmpty())
                    alarmName = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = alarmUri.split("\\|");
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        alarmName = ringtone.getTitle(context);
                    } catch (Exception e) {
                        alarmName = context.getString(R.string.ringtone_preference_not_set);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Context context = contextWeakReference.get();
            androidx.preference.Preference preference = preferenceWeakRef.get();
            if ((context != null) && (preference != null)) {
                String summary = TextUtils.replace(initSummary,
                        new String[]{"<ringtone_name>", "<notification_name>", "<alarm_name>"},
                        new String[]{ringtoneName, notificationName, alarmName}).toString();
                preference.setSummary(StringFormatUtils.fromHtml(summary, false, false, false, 0, 0, true));
            }
        }

    }

    private void setProfileSoundsDualSIMPreferenceSummary(final String initSummary,
                                                         final String ringtoneSIM1Uri, final String ringtoneSIM2Uri,
                                                         final String notificationSIM1Uri, final String notificationSIM2Uri,
                                                         final androidx.preference.Preference preference, final Context context) {
        SetProfileSoundsDualSIMPreferenceSummaryAsyncTask asyncTask =
                new SetProfileSoundsDualSIMPreferenceSummaryAsyncTask(initSummary,
                        ringtoneSIM1Uri, ringtoneSIM2Uri, notificationSIM1Uri, notificationSIM2Uri,
                        preference, context);
        asyncTask.execute();
    }

    private static class SetProfileSoundsDualSIMPreferenceSummaryAsyncTask extends AsyncTask<Void, Integer, Void> {

        private String ringtoneNameSIM1;
        private String ringtoneNameSIM2;
        private String notificationNameSIM1;
        private String notificationNameSIM2;

        final String initSummary;
        final String ringtoneSIM1Uri;
        final String ringtoneSIM2Uri;
        final String notificationSIM1Uri;
        final String notificationSIM2Uri;
        private final WeakReference<androidx.preference.Preference> preferenceWeakRef;
        private final WeakReference<Context> contextWeakReference;

        public SetProfileSoundsDualSIMPreferenceSummaryAsyncTask(final String initSummary,
                                                                 final String ringtoneSIM1Uri, final String ringtoneSIM2Uri,
                                                                 final String notificationSIM1Uri, final String notificationSIM2Uri,
                                                                 final androidx.preference.Preference preference, final Context context) {
            this.preferenceWeakRef = new WeakReference<>(preference);
            this.contextWeakReference = new WeakReference<>(context);
            this.initSummary = initSummary;
            this.ringtoneSIM1Uri = ringtoneSIM1Uri;
            this.ringtoneSIM2Uri = ringtoneSIM2Uri;
            this.notificationSIM1Uri = notificationSIM1Uri;
            this.notificationSIM2Uri = notificationSIM2Uri;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Context context = contextWeakReference.get();
            androidx.preference.Preference preference = preferenceWeakRef.get();
            if ((context != null) && (preference != null)) {
                if ((ringtoneSIM1Uri == null) || ringtoneSIM1Uri.isEmpty())
                    ringtoneNameSIM1 = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = ringtoneSIM1Uri.split("\\|");
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        ringtoneNameSIM1 = ringtone.getTitle(context);
                    } catch (Exception e) {
                        ringtoneNameSIM1 = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                if ((ringtoneSIM2Uri == null) || ringtoneSIM2Uri.isEmpty())
                    ringtoneNameSIM2 = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = ringtoneSIM2Uri.split("\\|");
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        ringtoneNameSIM2 = ringtone.getTitle(context);
                    } catch (Exception e) {
                        ringtoneNameSIM2 = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                if ((notificationSIM1Uri == null) || notificationSIM1Uri.isEmpty())
                    notificationNameSIM1 = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = notificationSIM1Uri.split("\\|");
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        notificationNameSIM1 = ringtone.getTitle(context);
                    } catch (Exception e) {
                        notificationNameSIM1 = context.getString(R.string.ringtone_preference_not_set);
                    }
                }

                if ((notificationSIM2Uri == null) || notificationSIM2Uri.isEmpty())
                    notificationNameSIM2 = context.getString(R.string.ringtone_preference_none);
                else {
                    String[] splits = notificationSIM2Uri.split("\\|");
                    Uri uri = Uri.parse(splits[0]);
                    Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
                    try {
                        notificationNameSIM2 = ringtone.getTitle(context);
                    } catch (Exception e) {
                        notificationNameSIM2 = context.getString(R.string.ringtone_preference_not_set);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Context context = contextWeakReference.get();
            androidx.preference.Preference preference = preferenceWeakRef.get();
            if ((context != null) && (preference != null)) {
                String summary = TextUtils.replace(initSummary,
                        new String[]{"<ringtone_name_sim1>", "<ringtone_name_sim2>", "<notification_name_sim1>", "<notification_name_sim2>"},
                        new String[]{ringtoneNameSIM1, ringtoneNameSIM2, notificationNameSIM1, notificationNameSIM2}).toString();
                preference.setSummary(StringFormatUtils.fromHtml(summary, false, false, false, 0, 0, true));
            }
        }

    }

}
