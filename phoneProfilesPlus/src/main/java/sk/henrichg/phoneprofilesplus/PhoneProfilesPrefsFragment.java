package sk.henrichg.phoneprofilesplus;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.PeriodicWorkRequest;

import java.util.concurrent.TimeUnit;

class PhoneProfilesPrefsFragment extends PreferenceFragmentCompat
                        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private PreferenceManager prefMng;
    private SharedPreferences preferences;
    private SharedPreferences applicationPreferences;

    //boolean scrollToSet = false;
    private boolean nestedFragment = false;

    private static final String PREF_APPLICATION_PERMISSIONS = "permissionsApplicationPermissions";
    private static final int RESULT_APPLICATION_PERMISSIONS = 1990;
    private static final String PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS = "permissionsWriteSystemSettingsPermissions";
    private static final int RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS = 1991;
    //private static final String PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = "permissionsAccessNotificationPolicyPermissions";
    //private static final int RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS = 1997;
    private static final String PREF_DRAW_OVERLAYS_PERMISSIONS = "permissionsDrawOverlaysPermissions";
    private static final int RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS = 1998;
    static final String PREF_GRANT_ROOT_PERMISSION = "permissionsGrantRootPermission";
    private static final String PREF_GRANT_G1_PERMISSION = "permissionsGrantG1Permission";

    private static final String PREF_WIFI_LOCATION_SYSTEM_SETTINGS = "applicationEventWiFiLocationSystemSettings";
    private static final String PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS = "applicationEventBluetoothLocationSystemSettings";
    private static final String PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS = "applicationEventMobileCellsLocationSystemSettings";
    private static final int RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS = 1992;
    //static final String PREF_POWER_SAVE_MODE_INTERNAL = "applicationPowerSaveModeInternal";
    private static final String PREF_LOCATION_SYSTEM_SETTINGS = "applicationEventLocationSystemSettings";
    private static final int RESULT_LOCATION_SYSTEM_SETTINGS = 1994;
    private static final String PREF_LOCATION_EDITOR = "applicationEventLocationsEditor";
    private static final String PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS = "applicationBatteryOptimization";
    //private static final String PREF_APPLICATION_LANGUAGE_24 = "applicationLanguage24";
    //static final int RESULT_LOCALE_SETTINGS = 1996;
    private static final String PREF_AUTOSTART_MANAGER = "applicationAutoStartManager";
    private static final String PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS = "applicationEventWiFiKeepOnSystemSettings";
    private static final int RESULT_WIFI_KEEP_ON_SETTINGS = 1999;
    private static final String PREF_ACTIVATED_PROFILE_NOTIFICATION_SYSTEM_SETTINGS = "notificationSystemSettingsActivatedProfile";
    private static final String PREF_ALL_NOTIFICATIONS_SYSTEM_SETTINGS = "notificationSystemSettingsAll";
    //private static final String PREF_APPLICATION_POWER_MANAGER = "applicationPowerManager";
    //private static final String PREF_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_SYSTEM_SETTINGS = "applicationEventMobileCellNotUsedCellsDetectionNotificationSystemSettings";
    private static final String PREF_SYSTEM_POWER_SAVE_MODE_SETTINGS = "applicationSystemPowerSaveMode";
    private static final String PREF_LOCATION_POWER_SAVE_MODE_SETTINGS = "applicationLocationPowerSaveMode";
    private static final String PREF_WIFI_POWER_SAVE_MODE_SETTINGS = "applicationWifiPowerSaveMode";
    private static final String PREF_BLUETOOTH_POWER_SAVE_MODE_SETTINGS = "applicationBluetoothPowerSaveMode";
    private static final String PREF_MOBILE_CELL_POWER_SAVE_MODE_SETTINGS = "applicationMobileCellPowerSaveMode";
    private static final String PREF_ORIENTATION_POWER_SAVE_MODE_SETTINGS = "applicationOrientationPowerSaveMode";
    private static final String PREF_PERIODIC_SCANNING_POWER_SAVE_MODE_SETTINGS = "applicationPeriodicScanningPowerSaveMode";
    private static final String PREF_NOTIFICATION_POWER_SAVE_MODE_SETTINGS = "applicationNotificationPowerSaveMode";
    private static final int RESULT_POWER_SAVE_MODE_SETTINGS = 1993;
    private static final String PREF_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS = "applicationEventNotificationNotificationsAccessSettings";
    private static final int RESULT_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS = 1994;

    //static final String PREF_POWER_SAVE_MODE_INTERNAL = "applicationPowerSaveModeInternal";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // is required for to not call onCreate and onDestroy on orientation change
        //noinspection deprecation
        setRetainInstance(true);

        nestedFragment = !(this instanceof PhoneProfilesPrefsActivity.PhoneProfilesPrefsRoot);

        initPreferenceFragment(/*savedInstanceState*/);
        //prefMng = getPreferenceManager();
        //preferences = prefMng.getSharedPreferences();

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
        if (preference instanceof ProfilePreference)
        {
            ((ProfilePreference)preference).fragment = new ProfilePreferenceFragment();
            dialogFragment = ((ProfilePreference)preference).fragment;
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
        if (preference instanceof LocationGeofencePreference)
        {
            ((LocationGeofencePreference)preference).fragment = new LocationGeofencePreferenceFragment();
            dialogFragment = ((LocationGeofencePreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof BetterNumberPickerPreference)
        {
            ((BetterNumberPickerPreference)preference).fragment = new BetterNumberPickerPreferenceFragment();
            dialogFragment = ((BetterNumberPickerPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof ColorChooserPreference)
        {
            ((ColorChooserPreference)preference).fragment = new ColorChooserPreferenceFragment();
            dialogFragment = ((ColorChooserPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof CustomColorDialogPreference)
        {
            ((CustomColorDialogPreference)preference).fragment = new CustomColorDialogPreferenceFragment();
            dialogFragment = ((CustomColorDialogPreference)preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof TimeDialogPreference) {
            ((TimeDialogPreference) preference).fragment = new TimeDialogPreferenceFragment();
            dialogFragment = ((TimeDialogPreference) preference).fragment;
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            dialogFragment.setArguments(bundle);
        }
        else
        if (preference instanceof OpaquenessLightingPreference) {
            ((OpaquenessLightingPreference) preference).fragment = new OpaquenessLightingPreferenceFragment();
            dialogFragment = ((OpaquenessLightingPreference) preference).fragment;
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
        if (preference instanceof RestartEventsIconColorChooserPreference)
        {
            ((RestartEventsIconColorChooserPreference)preference).fragment = new RestartEventsIconColorChooserPreferenceFragment();
            dialogFragment = ((RestartEventsIconColorChooserPreference)preference).fragment;
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
                dialogFragment.show(fragmentManager, PPApplication.PACKAGE_NAME + ".PhoneProfilesPrefsActivity.DIALOG");
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

        if (!((PhoneProfilesPrefsActivity)getActivity()).activityStarted)
            return;

        final PhoneProfilesPrefsFragment fragment = this;

        // must be used handler for rewrite toolbar title/subtitle
        Handler handler = new Handler(getActivity().getMainLooper());
        handler.postDelayed(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=PhoneProfilesPrefsFragment.onActivityCreated");
            if (getActivity() == null)
                return;

            Toolbar toolbar = getActivity().findViewById(R.id.activity_preferences_toolbar);
            if (nestedFragment) {
                toolbar.setTitle(fragment.getPreferenceScreen().getTitle());
                toolbar.setSubtitle(getString(R.string.title_activity_phone_profiles_preferences));
            }
            else {
                toolbar.setTitle(getString(R.string.title_activity_phone_profiles_preferences));
                toolbar.setSubtitle(null);
            }

        }, 200);

        setDivider(null); // this remove dividers for categories

        /*
        prefMng = getPreferenceManager();
        prefMng.setSharedPreferencesName(PPApplication.APPLICATION_PREFS_NAME);
        prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);
        */
        if (!nestedFragment) {
            Preference preferenceCategoryScreen;
            preferenceCategoryScreen = findPreference("applicationInterfaceCategoryRoot");
            if (preferenceCategoryScreen != null) {
                setCategorySummary(preferenceCategoryScreen);
            }
            preferenceCategoryScreen = findPreference("categoryApplicationStartRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);

            preferenceCategoryScreen = findPreference("categorySystemRoot");
            if (preferenceCategoryScreen != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceCategoryScreen, true, false, true, false, false);
                setCategorySummary(preferenceCategoryScreen);
            }
            preferenceCategoryScreen = findPreference("categoryPermissionsRoot");
            if (preferenceCategoryScreen != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceCategoryScreen, true, false, true, false, false);
                setCategorySummary(preferenceCategoryScreen);
            }
            preferenceCategoryScreen = findPreference("profileActivationCategoryRoot");
            if (preferenceCategoryScreen != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceCategoryScreen, true, false, true, false, false);
                setCategorySummary(preferenceCategoryScreen);
            }
            preferenceCategoryScreen = findPreference("eventRunCategoryRoot");
            if (preferenceCategoryScreen != null) {
                GlobalGUIRoutines.setPreferenceTitleStyleX(preferenceCategoryScreen, true, false, true, false, false);
                setCategorySummary(preferenceCategoryScreen);
            }
            preferenceCategoryScreen = findPreference("categoryNotificationsRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference("specialProfileParametersCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference("periodicScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference("locationScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference("wifiScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference("bluetoothScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference("mobileCellsScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference("orientationScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference("notificationScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference("categoryActivatorRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference("categoryEditorRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference("categoryWidgetListRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference("categoryWidgetOneRowRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference("categoryWidgetIconRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            preferenceCategoryScreen = findPreference("categoryShortcutRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
            if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
                preferenceCategoryScreen = findPreference("categorySamsungEdgePanelRoot");
                if (preferenceCategoryScreen != null)
                    setCategorySummary(preferenceCategoryScreen);
            }
        }

        Preference preference;/* = findPreference("applicationUnlinkRingerNotificationVolumesInfo");
        if (preference != null) {
            preference.setShouldDisableView(false);
            preference.setEnabled(false);
        }*/

        //if (!ActivateProfileHelper.getMergedRingNotificationVolumes(getActivity().getApplicationContext())) {
        if (!ApplicationPreferences.prefMergedRingNotificationVolumes) {
            // detection of volumes merge = volumes are not merged
            preference = findPreference("applicationUnlinkRingerNotificationVolumesInfo");
            if (preference != null) {
                //preference.setEnabled(false);
                preference.setTitle(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumesUnlinked_summary);
                //systemCategory.removePreference(preference);
            }
        }
        else {
            preference = findPreference("applicationUnlinkRingerNotificationVolumesInfo");
            if (preference != null) {
                //preference.setEnabled(true);
                preference.setTitle(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumes_summary);
            }
            /*Preference preference = findPreference(ApplicationPreferences.PREF_APPLICATION_RINGER_NOTIFICATION_VOLUMES_UNLINKED_INFO);
            if (preference != null)
                systemCategory.removePreference(preference);*/
        }

        doOnActivityCreatedBatterySaver(PREF_PERIODIC_SCANNING_POWER_SAVE_MODE_SETTINGS);
        doOnActivityCreatedBatterySaver(PREF_SYSTEM_POWER_SAVE_MODE_SETTINGS);
        doOnActivityCreatedBatterySaver(PREF_LOCATION_POWER_SAVE_MODE_SETTINGS);
        doOnActivityCreatedBatterySaver(PREF_WIFI_POWER_SAVE_MODE_SETTINGS);
        doOnActivityCreatedBatterySaver(PREF_BLUETOOTH_POWER_SAVE_MODE_SETTINGS);
        doOnActivityCreatedBatterySaver(PREF_MOBILE_CELL_POWER_SAVE_MODE_SETTINGS);
        doOnActivityCreatedBatterySaver(PREF_ORIENTATION_POWER_SAVE_MODE_SETTINGS);
        doOnActivityCreatedBatterySaver(PREF_NOTIFICATION_POWER_SAVE_MODE_SETTINGS);

        //if (Build.VERSION.SDK_INT >= 23) {
            preference = findPreference(PREF_APPLICATION_PERMISSIONS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference1 -> {
                    //Permissions.saveAllPermissions(getActivity().getApplicationContext(), false);
                    boolean ok = false;
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:"+PPApplication.PACKAGE_NAME));
                    if (GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext())) {
                        try {
                            //noinspection deprecation
                            startActivityForResult(intent, RESULT_APPLICATION_PERMISSIONS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok){
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference1.getTitle(),
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
                    return false;
                });
            }
            preference = findPreference(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                /*if (PPApplication.romIsMIUI) {
                    preference.setSummary(R.string.phone_profiles_pref_writeSystemSettingPermissions_summary_miui);
                }*/
                preference.setOnPreferenceClickListener(preference12 -> {
                    boolean ok = false;
                    //if (!PPApplication.romIsMIUI) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_MANAGE_WRITE_SETTINGS, getActivity().getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intent.setData(Uri.parse("package:" + PPApplication.PACKAGE_NAME));
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            //noinspection deprecation
                            startActivityForResult(intent, RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference12.getTitle(),
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
                    /*}
                    else {
                        try {
                            // MIUI 8
                            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                            localIntent.putExtra("extra_pkgname", getActivity().PPApplication.PACKAGE_NAME);
                            startActivityForResult(localIntent, RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                        } catch (Exception e) {
                            try {
                                // MIUI 5/6/7
                                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                localIntent.putExtra("extra_pkgname", getActivity().PPApplication.PACKAGE_NAME);
                                startActivityForResult(localIntent, RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                            } catch (Exception e1) {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = dialogBuilder.create();
//                                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                        @Override
//                                        public void onShow(DialogInterface dialog) {
//                                            Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                            if (positive != null) positive.setAllCaps(false);
//                                            Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                            if (negative != null) negative.setAllCaps(false);
//                                        }
//                                    });
                                dialog.show();
                            }
                        }
                    }*/
                    return false;
                });
            }
            /*preference = findPreference(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
            if (preference != null) {
                boolean a60 = (android.os.Build.VERSION.SDK_INT == 23) && Build.VERSION.RELEASE.equals("6.0");
                if ((!a60) &&
                        GlobalGUIRoutines.activityActionExists(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, getActivity().getApplicationContext())) {
                    //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivityForResult(intent, RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
                            return false;
                        }
                    });
                } else {
                    PreferenceScreen preferenceCategory = findPreference("categoryPermissions");
                    if (preferenceCategory != null)
                        preferenceCategory.removePreference(preference);
                }
            }*/
            preference = findPreference(PREF_DRAW_OVERLAYS_PERMISSIONS);
            if (preference != null) {
                //if (android.os.Build.VERSION.SDK_INT >= 25) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                    /*if (PPApplication.romIsMIUI) {
                        preference.setTitle(R.string.phone_profiles_pref_drawOverlaysPermissions_miui);
                        preference.setSummary(R.string.phone_profiles_pref_drawOverlaysPermissions_summary_miui);
                    }*/
                preference.setOnPreferenceClickListener(preference13 -> {
                    boolean ok = false;
                    //if (!PPApplication.romIsMIUI) {
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, getActivity().getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            intent.setData(Uri.parse("package:"+PPApplication.PACKAGE_NAME));
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            //noinspection deprecation
                            startActivityForResult(intent, RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference13.getTitle(),
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
                        /*}
                        else {
                            try {
                                // MIUI 8
                                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                                localIntent.putExtra("extra_pkgname", getActivity().PPApplication.PACKAGE_NAME);
                                startActivityForResult(localIntent, RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS);
                            } catch (Exception e) {
                                try {
                                    // MIUI 5/6/7
                                    Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                    localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                    localIntent.putExtra("extra_pkgname", getActivity().PPApplication.PACKAGE_NAME);
                                    startActivityForResult(localIntent, RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS);
                                } catch (Exception e1) {
                                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                    dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog = dialogBuilder.create();
//                                        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                            @Override
//                                            public void onShow(DialogInterface dialog) {
//                                                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                                if (positive != null) positive.setAllCaps(false);
//                                                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                                if (negative != null) negative.setAllCaps(false);
//                                            }
//                                        });
                                    dialog.show();
                                }
                            }
                        }*/
                    return false;
                });
            }

            //int locationMode = Settings.Secure.getInt(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);

            /*
            if (WifiScanWorker.wifi == null)
                WifiScanWorker.wifi = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            boolean isScanAlwaysAvailable = WifiScanWorker.wifi.isScanAlwaysAvailable();

            if ((locationMode == Settings.Secure.LOCATION_MODE_OFF) || (!isScanAlwaysAvailable)) {*/
            preference = findPreference(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference14 -> {
                    boolean ok = false;
                    //Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getActivity().getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            //noinspection deprecation
                            startActivityForResult(intent, RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference14.getTitle(),
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
                    return false;
                });
            }

            preference = findPreference(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference15 -> {
                    boolean ok = false;
                    //Intent intent = new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_WIFI_SETTINGS, getActivity().getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            //noinspection deprecation
                            startActivityForResult(intent, RESULT_WIFI_KEEP_ON_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference15.getTitle(),
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
                    return false;
                });
            }

            //if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {
            preference = findPreference(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference16 -> {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getActivity().getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            //noinspection deprecation
                            startActivityForResult(intent, RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference16.getTitle(),
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
                    return false;
                });
            }

            //if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {
            preference = findPreference(PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference17 -> {
                    boolean ok = false;
                    if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getActivity().getApplicationContext())) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            //intent.addCategory(Intent.CATEGORY_DEFAULT);
                            //noinspection deprecation
                            startActivityForResult(intent, RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference17.getTitle(),
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
                    return false;
                });
            }

            preference = findPreference(PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS);
            if (preference != null) {
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference18 -> {
//                    PowerManager pm = (PowerManager) getActivity().getApplicationContext().getSystemService(Context.POWER_SERVICE);
//                    String packageName = PPApplication.PACKAGE_NAME;
//                    if (pm.isIgnoringBatteryOptimizations(packageName) //||
//                        //(!GlobalGUIRoutines.activityActionExists(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, getActivity().getApplicationContext()))
//                    ) {
                        boolean ok = false;
                        if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, getActivity().getApplicationContext())) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                                startActivity(intent);
                                ok = true;
                            } catch (Exception e) {
                                PPApplication.recordException(e);
                            }
                        }
                        if (!ok) {
                            PPAlertDialog dialog = new PPAlertDialog(
                                    preference18.getTitle(),
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
//                    } else {
//                        DO NOT USE IT, CHANGE IS NOT DISPLAYED IN SYSTEM SETTINGS
//                        boolean ok = false;
//                        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//                        intent.setData(Uri.parse("package:" + packageName));
//                        if (GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext())) {
//                            try {
//                                startActivity(intent);
//                                ok = true;
//                            } catch (Exception ignored) {
//                            }
//                        } else {
//                            if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, getActivity().getApplicationContext())) {
//                                try {
//                                    intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//                                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
//                                    startActivity(intent);
//                                    ok = true;
//                                } catch (Exception e) {
//                                    PPApplication.recordException(e);
//                                }
//                            }
//                        }
//                        if (!ok) {
//                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
//                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
//                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
//                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
//                            AlertDialog dialog = dialogBuilder.create();
////                                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
////                                    @Override
////                                    public void onShow(DialogInterface dialog) {
////                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
////                                        if (positive != null) positive.setAllCaps(false);
////                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
////                                        if (negative != null) negative.setAllCaps(false);
////                                    }
////                                });
//                            if (!getActivity().isFinishing())
//                                dialog.show();
//                        }
//                    }
                    return false;
                });
            }

        /*}
        else {
            // hide user permissions
            PreferenceScreen preferenceCategory = findPreference("categoryPermissions");
            if (preferenceCategory != null) {
                preference = findPreference(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                if (preference != null)
                    preferenceCategory.removePreference(preference);
                //preference = findPreference(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
                //if (preference != null)
                //    preferenceCategory.removePreference(preference);
                preference = findPreference(PREF_DRAW_OVERLAYS_PERMISSIONS);
                if (preference != null)
                    preferenceCategory.removePreference(preference);
                preference = findPreference(PREF_APPLICATION_PERMISSIONS);
                if (preference != null)
                    preferenceCategory.removePreference(preference);
            }

            if (!PPApplication.isRooted(true)) {
                preference = findPreference(PREF_GRANT_ROOT_PERMISSION);
                if (preference != null)
                    preference.setEnabled(false);
            }

//            if (PPApplication.isRooted(true)) {
//                PreferenceScreen preferenceCategory = findPreference("categoryPermissions");
//                if (preferenceCategory != null) {
//                    preference = findPreference(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
//                    if (preference != null)
//                        preferenceCategory.removePreference(preference);
//                    //preference = findPreference(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
//                    //if (preference != null)
//                    //    preferenceCategory.removePreference(preference);
//                    preference = findPreference(PREF_DRAW_OVERLAYS_PERMISSIONS);
//                    if (preference != null)
//                        preferenceCategory.removePreference(preference);
//                    preference = findPreference(PREF_APPLICATION_PERMISSIONS);
//                    if (preference != null)
//                        preferenceCategory.removePreference(preference);
//                }
//            }
//            else {
//                PreferenceScreen preferenceScreen = findPreference("rootScreen");
//                Preference preferenceCategory = findPreference("categoryPermissionsRoot");
//                if ((preferenceScreen != null) && (preferenceCategory != null))
//                        preferenceScreen.removePreference(preferenceCategory);
//            }

            preferenceCategory = findPreference("wifiScanningCategory");
            preference = findPreference(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
            if ((preferenceCategory != null) && (preference != null))
                preferenceCategory.removePreference(preference);

            preferenceCategory = findPreference("bluetoothScanningCategory");
            preference = findPreference(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
            if ((preferenceCategory != null) && (preference != null))
                preferenceCategory.removePreference(preference);

            PreferenceCategory preferenceCategory2 = findPreference("applicationPowerParametersCategory");
            preference = findPreference(PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS);
            if ((preferenceCategory2 != null) && (preference != null))
                preferenceCategory2.removePreference(preference);
        }*/

        // force check root
        boolean rooted;
        synchronized (PPApplication.rootMutex) {
            PPApplication.rootMutex.rootChecked = false;
            rooted = RootUtils._isRooted();
        }
        if (!rooted) {
            preference = findPreference(PREF_GRANT_ROOT_PERMISSION);
            if (preference != null)
                preference.setEnabled(false);
        }
        if (rooted) {
            preference = findPreference(PREF_GRANT_ROOT_PERMISSION);
            if (preference != null) {
                preference.setOnPreferenceClickListener(preference19 -> {
                    Permissions.grantRootX(null, getActivity());
                    setSummary(PREF_GRANT_ROOT_PERMISSION);
                    return false;
                });
            }
        }
        preference = findPreference(PREF_GRANT_G1_PERMISSION);
        if (preference != null) {
            preference.setOnPreferenceClickListener(preference110 -> {
                Permissions.grantG1Permission(null, getActivity());
                return false;
            });
        }

        if (!BluetoothScanner.bluetoothLESupported(/*getActivity().getApplicationContext()*/)) {
            PreferenceScreen preferenceCategory = findPreference("bluetoothScanningCategory");
            preference = findPreference(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION);
            if ((preferenceCategory != null) && (preference != null))
                preferenceCategory.removePreference(preference);
        }
        preference = findPreference(PREF_LOCATION_SYSTEM_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference111 -> {
                boolean ok = false;
                if (GlobalGUIRoutines.activityActionExists(Settings.ACTION_LOCATION_SOURCE_SETTINGS, getActivity().getApplicationContext())) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        //intent.addCategory(Intent.CATEGORY_DEFAULT);
                        //noinspection deprecation
                        startActivityForResult(intent, RESULT_LOCATION_SYSTEM_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
                if (!ok) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            preference111.getTitle(),
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
                return false;
            });
        }
        /*
        if (android.os.Build.VERSION.SDK_INT < 21) {
            PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("categoryNotificationsStatusBar");
            preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN);
            if (preference != null)
                preferenceCategory.removePreference(preference);
        }
        */
        if ((PPApplication.sLook == null) || (!PPApplication.sLookCocktailPanelEnabled)) {
            PreferenceScreen preferenceScreen = findPreference("rootScreen");
            Preference preferenceCategory = findPreference("categorySamsungEdgePanelRoot");
            if ((preferenceScreen != null) && (preferenceCategory != null))
                preferenceScreen.removePreference(preferenceCategory);
        }

        preference = findPreference(PREF_AUTOSTART_MANAGER);
        if (preference != null) {
            final AutoStartPermissionHelper autoStartPermissionHelper = AutoStartPermissionHelper.getInstance();
            if (autoStartPermissionHelper.isAutoStartPermissionAvailable(getActivity().getApplicationContext())) {
                preference.setOnPreferenceClickListener(preference119 -> {
                    boolean success;
                    try {
                        success = autoStartPermissionHelper.getAutoStartPermission(getActivity());
                    }catch (Exception e) {
                        Log.e("****** PhoneProfilesPrefsFragment.onActivityCreated", Log.getStackTraceString(e));
                        success = false;
                    }
                    if (!success) {
                        /*
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                        if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI)
                            dialogBuilder.setMessage(R.string.phone_profiles_pref_systemAutoStartManager_settingScreenNotFound_huawei_alert);
                        else
                            dialogBuilder.setMessage(R.string.phone_profiles_pref_systemAutoStartManager_settingScreenNotFound_alert);
                        //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                        dialogBuilder.setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = dialogBuilder.create();
//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });
                        */

                        CharSequence message;
                        if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI)
                            message = getString(R.string.phone_profiles_pref_systemAutoStartManager_settingScreenNotFound_huawei_alert);
                        else
                            message = getString(R.string.phone_profiles_pref_systemAutoStartManager_settingScreenNotFound_alert);

                        PPAlertDialog dialog = new PPAlertDialog(
                                getString(R.string.phone_profiles_pref_systemAutoStartManager),
                                message,
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
                                false,
                                getActivity()
                        );

                        if (!getActivity().isFinishing())
                            dialog.show();
                    }
                    return false;
                });
            } else {
                /*PreferenceScreen preferenceScreen = findPreference("categorySystem");
                if (preferenceScreen != null) {
                    PreferenceCategory preferenceCategory = findPreference("applicationAutostartCategory");
                    if (preferenceCategory != null)
                        preferenceScreen.removePreference(preferenceCategory);
                }*/
                PreferenceScreen preferenceScreen = findPreference("categoryApplicationStart");
                if (preferenceScreen != null) {
                    preference = findPreference(PREF_AUTOSTART_MANAGER);
                    if (preference != null)
                        preferenceScreen.removePreference(preference);
                }
            }
        }

        long workMinInterval = TimeUnit.MILLISECONDS.toMinutes(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS);
        String summary = getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary1) + " " +
                workMinInterval + " " +
                getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary2);
        preference = findPreference("applicationEventPeriodicScanningScanIntervalInfo");
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = findPreference("applicationEventLocationUpdateIntervalInfo");
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = findPreference("applicationEventWifiScanIntervalInfo");
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = findPreference("applicationEventBluetoothScanIntervalInfo");
        if (preference != null) {
            preference.setSummary(summary);
        }
        preference = findPreference("applicationEventOrientationScanIntervalInfo");
        if (preference != null) {
            summary = getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary1) + " 10 " +
                    getString(R.string.phone_profiles_pref_applicationEventScanIntervalInfo_summary3);
            preference.setSummary(summary);
        }
        if (Build.VERSION.SDK_INT >= 27) {
            preference = findPreference(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("wifiScanningCategory");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }
        if (Build.VERSION.SDK_INT >= 26) {
            preference = findPreference(PREF_ACTIVATED_PROFILE_NOTIFICATION_SYSTEM_SETTINGS);
            if (preference != null) {
                preference.setSummary(getString(R.string.phone_profiles_pref_notificationSystemSettings_summary) +
                        " " + getString(R.string.notification_channel_activated_profile));
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(preference112 -> {
                    boolean ok = false;
//                    PPApplication.logE("[PPP_NOTIFICATION] PhoneProfilesPrefsFragment.onActivityCreated - activated porofile notification preference", "call of createProfileNotificationChannel()");
                    PPApplication.createProfileNotificationChannel(getActivity().getApplicationContext());
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, PPApplication.PROFILE_NOTIFICATION_CHANNEL);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, PPApplication.PACKAGE_NAME);
                    if (GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext())) {
                        try {
                            startActivity(intent);
                            ok = true;
                        } catch (Exception e) {
                            PPApplication.recordException(e);
                        }
                    }
                    if (!ok) {
                        PPAlertDialog dialog = new PPAlertDialog(
                                preference112.getTitle(),
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
                    return false;
                });
            }
        }

        preference = findPreference(PREF_ALL_NOTIFICATIONS_SYSTEM_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference113 -> {
                boolean ok = false;
//                PPApplication.logE("[PPP_NOTIFICATION] PhoneProfilesPrefsFragment.onActivityCreated - all notifications preference", "call of createProfileNotificationChannel()");
                PPApplication.createProfileNotificationChannel(getActivity().getApplicationContext());

                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT > 26) {
                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, PPApplication.PACKAGE_NAME);
                } else if (Build.VERSION.SDK_INT == 26) {
                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra("android.provider.extra.APP_PACKAGE", PPApplication.PACKAGE_NAME);
                } else {
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra("app_package", PPApplication.PACKAGE_NAME);
                    intent.putExtra("app_uid", getActivity().getApplicationInfo().uid);
                }

                if (GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext())) {
                    try {
                        startActivity(intent);
                        ok = true;
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
                if (!ok) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            preference113.getTitle(),
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
                return false;
            });
        }

        preference = findPreference(PREF_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference114 -> {
                boolean ok = false;
                String activity;
                activity = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
                if (GlobalGUIRoutines.activityActionExists(activity, getActivity().getApplicationContext())) {
                    try {
                        Intent intent = new Intent(activity);
                        //noinspection deprecation
                        startActivityForResult(intent, RESULT_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS);
                        ok = true;
                    } catch (Exception e) {
                        PPApplication.recordException(e);
                    }
                }
                if (!ok) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            preference114.getTitle(),
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
                return false;
            });
        }

        //preference = findPreference(PREF_APPLICATION_POWER_MANAGER);
        //if (preference != null) {
            /*boolean intentFound = false;
            KillerManager.init(getActivity());
            DeviceBase device = KillerManager.getDevice();
            if (device != null) {
                Intent intent = device.getActionPowerSaving(getActivity());
                if (intent != null && ActionsUtils.isIntentAvailable(getActivity(), intent))
                    intentFound = true;
                //if (intent != null && GlobalGUIRoutines.activityIntentExists(intent, getActivity()))
                //    intentFound = true;
            }
            /*
            if (KillerManager.isActionAvailable(getActivity(), KillerManager.Actions.ACTION_POWERSAVING)) {
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            KillerManager.doActionPowerSaving(getActivity());
                        }catch (Exception e) {
                            if (getActivity() != null) {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                                dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dialogBuilder.setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = dialogBuilder.create();
//                                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                    @Override
//                                    public void onShow(DialogInterface dialog) {
//                                        Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                        if (positive != null) positive.setAllCaps(false);
//                                        Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                        if (negative != null) negative.setAllCaps(false);
//                                    }
//                                });
                                if (!getActivity().isFinishing())
                                    dialog.show();
                            }
                        }
                        return false;
                    }
                });
            } else {
                PreferenceCategory preferenceCategory = findPreference("applicationPowerParametersCategory");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            */
        //}
//        if (Build.VERSION.SDK_INT < 24) {
//            PreferenceCategory preferenceCategory = findPreference("categoryNotificationsStatusBar");
//            preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE);
//            if ((preferenceCategory != null) && (preference != null))
//                preferenceCategory.removePreference(preference);
//            preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION);
//            if ((preferenceCategory != null) && (preference != null))
//                preferenceCategory.removePreference(preference);
//            preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT);
//            if ((preferenceCategory != null) && (preference != null))
//                preferenceCategory.removePreference(preference);
//            preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON);
//            if ((preferenceCategory != null) && (preference != null))
//                preferenceCategory.removePreference(preference);
//        }
        preference = findPreference("applicationUnlinkRingerNotificationVolumesImportantInfo");
        if (preference != null) {
            preference.setOnPreferenceClickListener(preference115 -> {
                Intent intentLaunch = new Intent(getActivity(), ImportantInfoActivityForceScroll.class);
                intentLaunch.putExtra(ImportantInfoActivity.EXTRA_SHOW_QUICK_GUIDE, false);
                intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SHOW_FRAGMENT, 1);
                intentLaunch.putExtra(ImportantInfoActivityForceScroll.EXTRA_SCROLL_TO, R.id.activity_info_notification_how_does_volume_separation_work_title);
                startActivity(intentLaunch);
                return false;
            });
        }

        preference = findPreference("applicationDoNotKillMyApp");
        if (preference != null) {
            preference.setSummary(getString(R.string.phone_profiles_pref_applicationDoNotKillMyApp_summary1) + " " +
                    getString(R.string.phone_profiles_pref_applicationDoNotKillMyApp_webSiteName) + " " +
                    getString(R.string.phone_profiles_pref_applicationDoNotKillMyApp_summary2));
            preference.setOnPreferenceClickListener(preference116 -> {
                PPApplication.showDoNotKillMyAppDialog(getActivity());
                return false;
            });
        }
/*
        if (Build.VERSION.SDK_INT >= 26) {
            preference = findPreference(PREF_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_SYSTEM_SETTINGS);
            if (preference != null) {
                preference.setSummary(getString(R.string.phone_profiles_pref_notificationSystemSettings_summary) +
                        " " + getString(R.string.notification_channel_not_used_mobile_cell));
                //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @TargetApi(Build.VERSION_CODES.O)
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        boolean ok = false;
                        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_CHANNEL_ID, PPApplication.NOT_USED_MOBILE_CELL_NOTIFICATION_CHANNEL);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getActivity().PPApplication.PACKAGE_NAME);
                        if (GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext())) {
                            try {
                                startActivity(intent);
                                ok = true;
                            } catch (Exception ignored) {}
                        }
                        if (!ok) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                            dialogBuilder.setMessage(R.string.setting_screen_not_found_alert);
                            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                            dialogBuilder.setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = dialogBuilder.create();
//                            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                                @Override
//                                public void onShow(DialogInterface dialog) {
//                                    Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                                    if (positive != null) positive.setAllCaps(false);
//                                    Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                                    if (negative != null) negative.setAllCaps(false);
//                                }
//                            });
                            if (!getActivity().isFinishing())
                                dialog.show();
                        }
                        return false;
                    }
                });
            }
        }
*/
        preference = findPreference("applicationWidgetLauncher");
        if (preference != null) {
            preference.setTitle(preference.getTitle() + " (" + getString(R.string.widget_label_icon) +
                                    ", " + getString(R.string.widget_label_one_row) + ")");
        }

        /*if (Build.VERSION.SDK_INT < 29) {
            ListPreference listPreference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR);
            if (listPreference != null) {
                CharSequence[] entries = listPreference.getEntries();
                entries[3] = getString(R.string.phone_profiles_pref_notificationBackgroundColor_nightMode) + " (" +
                        getString(R.string.phone_profiles_pref_notificationBackgroundColor_dark) + ")";
                entries[4] = getString(R.string.phone_profiles_pref_notificationBackgroundColor_nightMode) + " (" +
                        getString(R.string.phone_profiles_pref_notificationBackgroundColor_black) + ")";
                listPreference.setEntries(entries);
                setSummary(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR);
            }
        }*/

        if (PPApplication.deviceIsOppo || PPApplication.deviceIsRealme) {
            preference = findPreference("applicationColorOsWifiBluetoothDialogsInfo");
            if (preference != null) {
                preference.setOnPreferenceClickListener(preference117 -> {
                    /*
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setTitle(preference117.getTitle());
                    dialogBuilder.setMessage(R.string.phone_profiles_pref_applicationColorOsWifiBluetoothDialogsInfo_message_fix);
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(android.R.string.ok, null);
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
                            preference117.getTitle(),
                            getString(R.string.phone_profiles_pref_applicationColorOsWifiBluetoothDialogsInfo_message_fix),
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
                            false,
                            getActivity()
                    );

                    if (!getActivity().isFinishing())
                        dialog.show();
                    return false;
                });
            }
        }
        else {
            preference = findPreference("applicationColorOsWifiBluetoothDialogsInfo");
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("categoryPermissions");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }
        if (PPApplication.deviceIsXiaomi || PPApplication.romIsMIUI) {
            preference = findPreference("applicationMIUIWifiBluetoothDialogsInfo");
            if (preference != null) {
                preference.setOnPreferenceClickListener(preference118 -> {
                    /*
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setTitle(preference118.getTitle());
                    dialogBuilder.setMessage(R.string.phone_profiles_pref_applicationMIUIWifiBluetoothDialogsInfo_message);
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
                            getString(R.string.phone_profiles_pref_applicationMIUIWifiBluetoothDialogsInfo_message),
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
            preference = findPreference("applicationMIUIWifiBluetoothDialogsInfo");
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("categoryPermissions");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }

        if (!(PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI)) {
            preference = findPreference("applicationWidgetIconNotWorkingMIUIInfo");
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("categoryWidgetIcon");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = findPreference("applicationWidgetOneRowNotWorkingMIUIInfo");
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("categoryWidgetOneRow");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = findPreference("applicationWidgetListNotWorkingMIUIInfo");
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("categoryWidgetList");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
            preference = findPreference("notificationAppInstedProfileIconInStatusBarInfo");
            if (preference != null) {
                PreferenceCategory preferenceCategory = findPreference("notificationStatusBarCategory");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }

        if ((Build.VERSION.SDK_INT >= 28) &&
                (!(PPApplication.deviceIsXiaomi && PPApplication.romIsMIUI))) {
            preference = findPreference("applicationWifiControlInfo");
            if (preference != null) {
                preference.setOnPreferenceClickListener(preference118 -> {
                    /*
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setTitle(preference118.getTitle());
                    dialogBuilder.setMessage(R.string.phone_profiles_pref_applicationWifiControlInfo_message);
                    //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    dialogBuilder.setPositiveButton(R.string.phone_profiles_pref_applicationWifiControlInfo_showButton, (dialog, which) -> {
                        boolean ok = false;
                        final Intent intent = new Intent(Settings.ACTION_SETTINGS);
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
                            getString(R.string.phone_profiles_pref_applicationWifiControlInfo_message),
                            getString(R.string.phone_profiles_pref_applicationWifiControlInfo_showButton),
                            getString(android.R.string.cancel),
                            null, null,
                            (dialog1, which) -> {
                                boolean ok = false;
                                final Intent intent = new Intent(Settings.ACTION_SETTINGS);
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
            preference = findPreference("applicationWifiControlInfo");
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("categoryPermissions");
                if (preferenceCategory != null)
                    preferenceCategory.removePreference(preference);
            }
        }

        /////////////////

        if (Build.VERSION.SDK_INT >= 31) {
            if (PPApplication.isPixelLauncherDefault(getActivity()) ||
                    PPApplication.isOneUILauncherDefault(getActivity())) {
                //preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS);
                //if (preference != null)
                //    preference.setVisible(false);
                preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS);
                if (preference != null)
                    preference.setVisible(false);

                //preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS);
                //if (preference != null)
                //    preference.setVisible(false);
                preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_RADIUS);
                if (preference != null)
                    preference.setVisible(false);

                //preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS);
                //if (preference != null)
                //    preference.setVisible(false);
                preference = findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS);
                if (preference != null)
                    preference.setVisible(false);
            }
            if (PPApplication.deviceIsPixel) {
                PPListPreference listPreference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_STYLE);
                if (listPreference != null) {
                    String value = listPreference.getValue();
                    if (value.equals("0"))
                        value = "1";
                    listPreference.setEntries(R.array.notificationIconStylePixelA12Array);
                    listPreference.setEntryValues(R.array.notificationIconStylePixelA12Values);
                    listPreference.setValue(value);
                }
            }
        }

        if (!(PPApplication.deviceIsHuawei && PPApplication.romIsEMUI)) {
            preference = findPreference(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_BRIGHTNESS_AT_SCREEN_ON);
            if (preference != null) {
                PreferenceScreen preferenceCategory = findPreference("specialProfileParametersCategory");
                if (preferenceCategory != null) {
                    preferenceCategory.removePreference(preference);
                    if (preferenceCategory.getPreferenceCount() == 0) {
                        if (getActivity() != null) {
                            preference = new Preference(getActivity().getApplicationContext());
                            preference.setKey("specialProfileParameters_noParameters");
                            preference.setIconSpaceReserved(false);
                            preference.setLayoutResource(R.layout.mp_preference_material_widget);
                            preference.setOrder(-100);
                            preference.setTitle(R.string.phone_profiles_pref_applicationSpecialPreferencesNotAny);
                            preference.setEnabled(false);
                            preferenceCategory.addPreference(preference);
                        }
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= 29) {
            InfoDialogPreference infoDialogPreference = prefMng.findPreference("applicationEventWifiScanThrottlingInfo");
            if (infoDialogPreference != null) {

                String url;
                if (DebugVersion.enabled)
                    url = PPApplication.HELP_WIFI_SCAN_THROTTLING_DEVEL;
                else
                    url = PPApplication.HELP_WIFI_SCAN_THROTTLING;

                String infoText =
                        "<b>"+getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info1) + "</b><br>" +
                        getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info2) + "<br>" +
                        getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info3) + "<br><br>" +
                        "<b>"+getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info4) + "</b><br><br>" +
                        "<b>"+getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info5) + "</b><br>" +
                        getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info6) + "<br><br>" +
                        getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info7) + "<br><br>" +
                        getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info8) + " " +
                        getString(R.string.phone_profiles_pref_applicationEventWifiScanThrottling_info9) + ":<br>" +
                        "<a href=" + url + ">" + url+ " &#8658;</a>";

                infoDialogPreference.setInfoText(infoText);
                infoDialogPreference.setIsHtml(true);
            }
        }
    }

    private void doOnActivityCreatedBatterySaver(String key) {
        Preference preference = findPreference(key);
        if (preference != null) {
            //preference.setWidgetLayoutResource(R.layout.start_activity_preference);
            preference.setOnPreferenceClickListener(preference1 -> {
                boolean activityExists;
                Intent intent;
                /*if (Build.VERSION.SDK_INT == 21) {
                    intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$BatterySaverSettingsActivity"));
                    activityExists = GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext());
                } else*/ {
                    activityExists = (getActivity() != null) && GlobalGUIRoutines.activityActionExists(Settings.ACTION_BATTERY_SAVER_SETTINGS, getActivity().getApplicationContext());
                    intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                }
                if (activityExists) {
                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    try {
                        //noinspection deprecation
                        startActivityForResult(intent, RESULT_POWER_SAVE_MODE_SETTINGS);
                    } catch (Exception e) {
                        //if (Build.VERSION.SDK_INT > 21) {
                            intent = new Intent();
                            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$BatterySaverSettingsActivity"));
                            activityExists = GlobalGUIRoutines.activityIntentExists(intent, getActivity().getApplicationContext());
                            if (activityExists) {
                                try {
                                    //noinspection deprecation
                                    startActivityForResult(intent, RESULT_POWER_SAVE_MODE_SETTINGS);
                                } catch (Exception ee) {
                                    PPApplication.recordException(ee);
                                }
                            }
                        //}
                    }
                }
                if (!activityExists) {
                    PPAlertDialog dialog = new PPAlertDialog(
                            preference1.getTitle(),
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
                return false;
            });
        }
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();

        try {
            preferences.unregisterOnSharedPreferenceChangeListener(this);

            //SharedPreferences.Editor editor = applicationPreferences.edit();
            //updateSharedPreferences(editor, preferences);
            //editor.apply();
        } catch (Exception e) {
            PPApplication.recordException(e);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setSummary(key);
        try {
            SharedPreferences.Editor editor = applicationPreferences.edit();
            updateSharedPreferences(editor, preferences);
            editor.apply();
        } catch (Exception e) {
            PPApplication.recordException(e);
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_THEME)) {
            if (getActivity() != null)
                GlobalGUIRoutines.switchNightMode(getActivity().getApplicationContext(), false);
        }
        /*if (key.equals(ApplicationPreferences.PREF_APPLICATION_LANGUAGE)) {
            if (getActivity() != null) {
                //PhoneProfilesPrefsActivity activity = (PhoneProfilesPrefsActivity)getActivity();
                GlobalGUIRoutines.setLanguage(getActivity());
                GlobalGUIRoutines.reloadActivity(getActivity(), true);
                //activity.setResult(Activity.RESULT_OK);
                //activity.finish();
            }
        }*/

        /*
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING)) {
            SharedPreferences.Editor editor = applicationPreferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_DISABLED_SCANNING_BY_PROFILE, false);
            editor.apply();
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING)) {
            SharedPreferences.Editor editor = applicationPreferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_DISABLED_SCANNING_BY_PROFILE, false);
            editor.apply();
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING)) {
            SharedPreferences.Editor editor = applicationPreferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_DISABLED_SCANNING_BY_PROFILE, false);
            editor.apply();
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING)) {
            SharedPreferences.Editor editor = applicationPreferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_DISABLED_SCANNING_BY_PROFILE, false);
            editor.apply();
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING)) {
            SharedPreferences.Editor editor = applicationPreferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_DISABLED_SCANNING_BY_PROFILE, false);
            editor.apply();
        }
        */
    }

    void doOnActivityResult(int requestCode, int resultCode/*, Intent data*/)
    {
        if ((requestCode == RESULT_APPLICATION_PERMISSIONS) ||
                (requestCode == RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS) ||
                //(requestCode == RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS) ||
                (requestCode == RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS)) {

                Activity activity = getActivity();
                if (activity != null) {
                    Context context = activity.getApplicationContext();

                    boolean finishActivity = false;
                    boolean permissionsChanged = Permissions.getPermissionsChanged(context);

                    if (requestCode == RESULT_WRITE_SYSTEM_SETTINGS_PERMISSIONS) {
                        boolean canWrite = Settings.System.canWrite(context);
                        permissionsChanged = Permissions.getWriteSystemSettingsPermission(context) != canWrite;
                        if (canWrite)
                            Permissions.setShowRequestWriteSettingsPermission(context, true);
                    }
                    /*if (requestCode == RESULT_ACCESS_NOTIFICATION_POLICY_PERMISSIONS) {
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        boolean notificationPolicyGranted = (mNotificationManager != null) && (mNotificationManager.isNotificationPolicyAccessGranted());
                        permissionsChanged = Permissions.getNotificationPolicyPermission(context) != notificationPolicyGranted;
                        if (notificationPolicyGranted)
                            Permissions.setShowRequestAccessNotificationPolicyPermission(context, true);
                    }*/
                    if (requestCode == RESULT_DRAW_OVERLAYS_POLICY_PERMISSIONS) {
                        boolean canDrawOverlays = Settings.canDrawOverlays(context);
                        permissionsChanged = Permissions.getDrawOverlayPermission(context) != canDrawOverlays;
                        if (canDrawOverlays)
                            Permissions.setShowRequestDrawOverlaysPermission(context, true);
                    }
                    if (requestCode == RESULT_APPLICATION_PERMISSIONS) {
                        boolean calendarPermission = Permissions.checkCalendar(context);
                        permissionsChanged = Permissions.getCalendarPermission(context) != calendarPermission;
                        // finish Editor when permission is disabled
                        finishActivity = permissionsChanged && (!calendarPermission);
                        if (!permissionsChanged) {
                            boolean contactsPermission = Permissions.checkContacts(context);
                            permissionsChanged = Permissions.getContactsPermission(context) != contactsPermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!contactsPermission);
                        }
                        if (!permissionsChanged) {
                            boolean locationPermission = Permissions.checkLocation(context);
                            permissionsChanged = Permissions.getLocationPermission(context) != locationPermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!locationPermission);
                        }
                        if (!permissionsChanged) {
                            boolean smsPermission = Permissions.checkSMS(context);
                            permissionsChanged = Permissions.getSMSPermission(context) != smsPermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!smsPermission);
                        }
                        if (!permissionsChanged) {
                            boolean phonePermission = Permissions.checkPhone(context);
                            permissionsChanged = Permissions.getPhonePermission(context) != phonePermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!phonePermission);
                        }
                        if (!permissionsChanged) {
                            boolean storagePermission = Permissions.checkReadStorage(context);
                            permissionsChanged = Permissions.getReadStoragePermission(context) != storagePermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!storagePermission);
                        }
                        if (!permissionsChanged) {
                            boolean storagePermission = Permissions.checkWriteStorage(context);
                            permissionsChanged = Permissions.getWriteStoragePermission(context) != storagePermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!storagePermission);
                        }
                        if (!permissionsChanged) {
                            boolean cameraPermission = Permissions.checkCamera(context);
                            permissionsChanged = Permissions.getCameraPermission(context) != cameraPermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!cameraPermission);
                        }
                        if (!permissionsChanged) {
                            boolean microphonePermission = Permissions.checkMicrophone(context);
                            permissionsChanged = Permissions.getMicrophonePermission(context) != microphonePermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!microphonePermission);
                        }
                        if (!permissionsChanged) {
                            boolean sensorsPermission = Permissions.checkSensors(context);
                            permissionsChanged = Permissions.getSensorsPermission(context) != sensorsPermission;
                            // finish Editor when permission is disabled
                            finishActivity = permissionsChanged && (!sensorsPermission);
                        }
                    }

                    Permissions.saveAllPermissions(context, permissionsChanged);

                    if (permissionsChanged) {
                        //DataWrapper dataWrapper = new DataWrapper(context, false, 0);

//                        PPApplication.logE("[PPP_NOTIFICATION] PhoneProfilesPrefsFragment.doOnActivityResult", "call of updateGUI");
                        PPApplication.updateGUI(true, false, context);

                        if (finishActivity) {
                            activity.setResult(Activity.RESULT_CANCELED);
                            activity.finishAffinity();
                        } else {
                            setSummary(PREF_APPLICATION_PERMISSIONS);
                            setSummary(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
                            //setSummary(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
                            setSummary(PREF_DRAW_OVERLAYS_PERMISSIONS);

                            activity.setResult(Activity.RESULT_OK);
                        }
                    } else
                        activity.setResult(Activity.RESULT_CANCELED);
                }
        }

        if (requestCode == RESULT_WIFI_BLUETOOTH_MOBILE_CELLS_LOCATION_SETTINGS) {
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING);
            setSummary(PREF_LOCATION_SYSTEM_SETTINGS);
            setSummary(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
            setSummary(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
            setSummary(PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
        }
        if (requestCode == RESULT_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS) {
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING);
            setSummary(PREF_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS);
        }
        if (requestCode == RESULT_LOCATION_SYSTEM_SETTINGS) {
            final boolean enabled = GlobalUtils.isLocationEnabled(getContext());
            Preference preference = prefMng.findPreference(PREF_LOCATION_EDITOR);
            if (preference != null)
                preference.setEnabled(enabled);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING);
            setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING);
            setSummary(PREF_LOCATION_SYSTEM_SETTINGS);
            setSummary(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
            setSummary(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
            setSummary(PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
        }

        if (resultCode == RESULT_WIFI_KEEP_ON_SETTINGS) {
            setSummary(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS);
        }

        if (requestCode == LocationGeofencePreference.RESULT_GEOFENCE_EDITOR) {
            if (resultCode == Activity.RESULT_OK) {
                LocationGeofencePreference preference = prefMng.findPreference(PREF_LOCATION_EDITOR);
                if (preference != null) {
                    preference.setGeofenceFromEditor(/*geofenceId*/);
                }
            }
            /*if (PhoneProfilesPrefsFragment.changedLocationGeofencePreference != null) {
                if(resultCode == Activity.RESULT_OK){
                    //long geofenceId = data.getLongExtra(LocationGeofencePreference.EXTRA_GEOFENCE_ID, 0);
                    // this persistGeofence, for multiselect this mus only refresh listView in preference
                    PhoneProfilesPrefsFragment.changedLocationGeofencePreference.setGeofenceFromEditor();
                    PhoneProfilesPrefsFragment.changedLocationGeofencePreference = null;
                }
            }*/
        }
        if (requestCode == (Permissions.REQUEST_CODE + Permissions.GRANT_TYPE_RINGTONE_PREFERENCE)) {
            RingtonePreference preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND);
            if (preference != null)
                preference.refreshListView();
            preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_SOUND);
            if (preference != null)
                preference.refreshListView();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        doOnActivityResult(requestCode, resultCode);
    }


    private void initPreferenceFragment(/*Bundle savedInstanceState*/) {
        prefMng = getPreferenceManager();

        //prefMng.setSharedPreferencesName(PPApplication.APPLICATION_PREFS_NAME);
        //prefMng.setSharedPreferencesMode(Activity.MODE_PRIVATE);

        preferences = prefMng.getSharedPreferences();
        if (getContext() != null) {
            applicationPreferences = getContext().getApplicationContext().getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Activity.MODE_PRIVATE);
        }

        preferences.registerOnSharedPreferenceChangeListener(this);

        /*if (savedInstanceState == null) {
            SharedPreferences.Editor editor = preferences.edit();
            updateSharedPreferences(editor, applicationPreferences);
            editor.apply();
        }*/
    }

    void updateSharedPreferences(SharedPreferences.Editor editor, SharedPreferences fromPreference) {
    }

    void updateSharedPreferences() {
        SharedPreferences.Editor editor = applicationPreferences.edit();
        updateSharedPreferences(editor, preferences);
        editor.apply();
    }

    void loadSharedPreferences(SharedPreferences preferences, SharedPreferences applicationPreferences) {
        SharedPreferences.Editor editor = preferences.edit();
        updateSharedPreferences(editor, applicationPreferences);
        editor.apply();
    }

    private void updateAllSummary()
    {
        if (getActivity() == null)
            return;

        setSummary(ApplicationPreferences.PREF_APPLICATION_START_ON_BOOT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ALERT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_CLOSE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_LONG_PRESS_ACTIVATION);
        setSummary(ApplicationPreferences.PREF_APPLICATION_HOME_LAUNCHER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_NOTIFICATION_LAUNCHER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LAUNCHER);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_LANGUAGE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_THEME);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_NIGHT_MODE_OFF_THEME);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EDITOR_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EDITOR_HIDE_HEADER_OR_BOTTOM_BAR);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_HEADER);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_EDITOR_HEADER);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_TOAST);
        //if (Build.VERSION.SDK_INT < 26)
        //    setSummary(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT);
        //setSummary(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE);

        if (Build.VERSION.SDK_INT < 26) {
            setSummary(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
            //if (android.os.Build.VERSION.SDK_INT >= 21) {
            Preference preference = prefMng.findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR);
            if (preference != null) {
                preference.setTitle(R.string.phone_profiles_pref_notificationShowInStatusBarAndLockscreen);
            }
            //}
        }

        //if (Build.VERSION.SDK_INT < 26)
        //    setSummary(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_PERMANENT);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_STATUS_BAR_STYLE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_HEADER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_GRID_LAYOUT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_GRID_LAYOUT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_COMPACT_GRID);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_POWER_SAVE_MODE_INTERNAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_IN_POWER_SAVE_MODE);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_POWER_SAVE_MODE);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_RESCAN);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELLS_SCAN_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T);
        setSummary(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES);
        if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
            //setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_PREF_INDICATOR);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_HEADER);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS);
            //setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_GRID_LAYOUT);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS);
            setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_VERTICAL_POSITION);
            if (Build.VERSION.SDK_INT >= 30)
                setSummary(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CHANGE_COLOR_BY_NIGHT_MODE);
        }
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER);
        setSummary(ApplicationPreferences.PREF_APPLICATION_START_EVENTS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
        setSummary(PREF_BATTERY_OPTIMIZATION_SYSTEM_SETTINGS);
        //setSummary(PREF_APPLICATION_POWER_MANAGER);
        setSummary(PREF_SYSTEM_POWER_SAVE_MODE_SETTINGS);
        setSummary(PREF_LOCATION_POWER_SAVE_MODE_SETTINGS);
        setSummary(PREF_WIFI_POWER_SAVE_MODE_SETTINGS);
        setSummary(PREF_BLUETOOTH_POWER_SAVE_MODE_SETTINGS);
        setSummary(PREF_MOBILE_CELL_POWER_SAVE_MODE_SETTINGS);
        setSummary(PREF_ORIENTATION_POWER_SAVE_MODE_SETTINGS);
        setSummary(PREF_GRANT_ROOT_PERMISSION);
        setSummary(PREF_GRANT_G1_PERMISSION);
        setSummary(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS);
        //setSummary(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS);
        setSummary(PREF_DRAW_OVERLAYS_PERMISSIONS);
        setSummary(PREF_APPLICATION_PERMISSIONS);
        setSummary(PREF_AUTOSTART_MANAGER);
        setSummary(PREF_ACTIVATED_PROFILE_NOTIFICATION_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_USAGE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_USE_PRIORITY);
        setSummary(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ALERT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING);
        setSummary(PREF_LOCATION_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(PREF_LOCATION_EDITOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_USE_GPS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING);
        setSummary(PREF_WIFI_LOCATION_SYSTEM_SETTINGS);
        setSummary(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING);
        setSummary(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IF_BLUETOOTH_OFF);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING);
        setSummary(PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_EDITOR_SAVE_EDITOR_STATE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS);
        setSummary(PREF_ACTIVATED_PROFILE_NOTIFICATION_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_NOT_USED_CELLS_DETECTION_NOTIFICATION_ENABLED);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_NOTIFICATION_STYLE);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(PREF_PERIODIC_SCANNING_POWER_SAVE_MODE_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_POWER_SAVE_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_ONLY_WHEN_SCREEN_IS_ON);
        setSummary(PREF_NOTIFICATION_POWER_SAVE_MODE_SETTINGS);
        setSummary(PREF_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_RADIUS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_NUM_COLUMNS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_SOUND);
        setSummary(ApplicationPreferences.PREF_APPLICATION_APPLICATION_PROFILE_ACTIVATION_NOTIFICATION_VIBRATE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_ADD_RESTART_EVENTS_INTO_PROFILE_LIST);
        setSummary(ApplicationPreferences.PREF_APPLICATION_ACTIVATOR_INCREASE_BRIGHTNESS);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LAYOUT_HEIGHT);
        //setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_HIGHER_LAYOUT);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_USE_DYNAMIC_COLORS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS);
        setSummary(ApplicationPreferences.PREF_APPLICATION_RESTART_EVENTS_ICON_COLOR);
        setSummary(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LAYOUT_HEIGHT);

        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_FROM);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY_TO);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_IN_TIME_MULTIPLY);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_FROM);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY_TO);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_TIME_MULTIPLY);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_FROM);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY_TO);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_SCAN_IN_TIME_MULTIPLY);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_FROM);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY_TO);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_SCAN_IN_TIME_MULTIPLY);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_FROM);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY_TO);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_SCAN_IN_TIME_MULTIPLY);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_FROM);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY_TO);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_IN_TIME_MULTIPLY);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_FROM);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY_TO);
        setSummary(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_TIME_MULTIPLY);

        PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, getActivity().getApplicationContext());
        if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            /*prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_SCAN_IN_POWER_SAVE_MODE).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_WIFI_RESCAN).setEnabled(false);*/
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_WIFI, false);
            editor.apply();
            Preference preference = prefMng.findPreference("wifiScanningCategory");
            if (preference != null)
                preference.setEnabled(false);
        }

        preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, getActivity().getApplicationContext());
        if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            /*prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_IN_POWER_SAVE_MODE).setEnabled(false);
            prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_BLUETOOTH_RESCAN).setEnabled(false);
            if (WifiBluetoothScanner.bluetoothLESupported(preferencesActivity.getApplicationContext()))
                prefMng.findPreference(PPApplication.PREF_APPLICATION_EVENT_BLUETOOTH_LE_SCAN_DURATION).setEnabled(false);*/
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_BLUETOOTH, false);
            editor.apply();
            Preference preference = prefMng.findPreference("bluetoothScanningCategory");
            if (preference != null)
                preference.setEnabled(false);
        }

        preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, getActivity().getApplicationContext());
        if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
        {
            Preference preference = prefMng.findPreference("orientationScanningCategory");
            if (preference != null)
                preference.setEnabled(false);
        }

        preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, getActivity().getApplicationContext());
        if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED)
        {

            Preference preference = prefMng.findPreference("mobileCellsScanningCategory");
            if (preference != null)
                preference.setEnabled(false);
        }

        if (!GlobalUtils.isLocationEnabled(getActivity().getApplicationContext())) {
            Preference preference = prefMng.findPreference(PREF_LOCATION_EDITOR);
            if (preference != null)
                preference.setEnabled(false);
        }

    }

    private void setEnabledWidgets(String key) {
        boolean keyIsWidgetIconChangeColorByNightMode = false;
        boolean keyIsWidgetOneRowChangeColorByNightMode = false;
        boolean keyIsWidgetListChangeColorByNightMode = false;
        boolean keyIsSamsungEdgeChangeColorByNightMode = false;
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE)) {
            keyIsWidgetIconChangeColorByNightMode = true;
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE)) {
            keyIsWidgetOneRowChangeColorByNightMode = true;
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE)) {
            keyIsWidgetListChangeColorByNightMode = true;
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CHANGE_COLOR_BY_NIGHT_MODE)) {
            keyIsSamsungEdgeChangeColorByNightMode = true;
        }

        boolean changeWidgetIconColorsByNightMode = false;
        boolean changeWidgetOneRowColorsByNightMode = false;
        boolean changeWidgetListColorsByNightMode = false;
        boolean changeSamsungEdgeColorsByNightMode = false;
        if (Build.VERSION.SDK_INT >= 30) {
            changeWidgetIconColorsByNightMode = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CHANGE_COLOR_BY_NIGHT_MODE, false);
            changeWidgetOneRowColorsByNightMode = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CHANGE_COLOR_BY_NIGHT_MODE, false);
            changeWidgetListColorsByNightMode = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CHANGE_COLOR_BY_NIGHT_MODE, false);
            changeSamsungEdgeColorsByNightMode = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CHANGE_COLOR_BY_NIGHT_MODE, false);
        }

        boolean useDynamicColorsWidgetIcon = false;
        boolean useDynamicColorsWidgetOneRow = false;
        boolean useDynamicColorsWidgetList = false;
        if (Build.VERSION.SDK_INT >= 31) {
            useDynamicColorsWidgetIcon = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS, false);
            useDynamicColorsWidgetOneRow = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_USE_DYNAMIC_COLORS, false);
            useDynamicColorsWidgetList = preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS, false);
        }

        //boolean roundedCornersListEnabled =
        //        preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS, true);
        boolean preferenceIndicatorsListEnabled =
                preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR, true);
        boolean monochromeIconList =
                preferences.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR, GlobalGUIRoutines.OPAQUENESS_LIGHTNESS_0).equals("1");
        //boolean roundedCornersOneRowEnabled =
        //        preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS, true);
        boolean preferenceIndicatorsOneRowEnabled =
                preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR, true);
        boolean monochromeIconOneRow =
                preferences.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR, "0").equals("1");
        //boolean roundedCornersIconEnabled =
        //        preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS, true);
        boolean monochromeIconIcon =
                preferences.getString(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR, "0").equals("1");
        boolean hideProfileNameIcon =
                preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME, false);
        boolean monochromeIconSamsungEdge =
                preferences.getString(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR, "0").equals("1");
        boolean monochromeIconShortcut =
                preferences.getString(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR, "0").equals("1");

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T) || keyIsWidgetListChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_T);
            if (_preference != null)
                _preference.setEnabled(!changeWidgetListColorsByNightMode);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T) || keyIsWidgetOneRowChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_T);
            if (_preference != null)
                _preference.setEnabled(!changeWidgetOneRowColorsByNightMode);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T) || keyIsWidgetIconChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T);
            if (_preference != null)
                _preference.setEnabled(!changeWidgetIconColorsByNightMode);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE) || keyIsWidgetListChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE);
            if (_preference != null)
                _preference.setEnabled(!changeWidgetListColorsByNightMode);
            if (changeWidgetListColorsByNightMode) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_TYPE, false)) {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(true);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(false);
                } else {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(false);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(true);
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE) || keyIsWidgetOneRowChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE);
            if (_preference != null)
                _preference.setEnabled(!changeWidgetOneRowColorsByNightMode);
            if (changeWidgetOneRowColorsByNightMode) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_TYPE, false)) {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(true);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(false);
                } else {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(false);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(true);
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE) || keyIsWidgetIconChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE);
            if (_preference != null)
                _preference.setEnabled(!changeWidgetIconColorsByNightMode);
            if (changeWidgetIconColorsByNightMode) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_TYPE, false)) {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(true);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(false);
                } else {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(false);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(true);
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER) || keyIsWidgetIconChangeColorByNightMode) {
            Preference _preference;// = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER);
            //if (_preference != null)
            //    _preference.setEnabled(!changeWidgetColorsByNightMode);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_BORDER);
            if (changeWidgetIconColorsByNightMode) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_BORDER, false));
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER) || keyIsWidgetOneRowChangeColorByNightMode) {
            Preference _preference;// = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER);
            //if (_preference != null)
            //    _preference.setEnabled(!changeWidgetColorsByNightMode);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_LIGHTNESS_BORDER);
            if (changeWidgetOneRowColorsByNightMode) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_SHOW_BORDER, false));
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER) || keyIsWidgetListChangeColorByNightMode) {
            Preference _preference;// = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER);
            //if (_preference != null)
            //    _preference.setEnabled(!changeWidgetColorsByNightMode);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_LIGHTNESS_BORDER);
            if (changeWidgetListColorsByNightMode) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }

            } else {
                if (_preference != null) {
                    _preference.setEnabled(
                            preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_SHOW_BORDER, false));
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR) || keyIsWidgetIconChangeColorByNightMode) {
            Preference _preference;// = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_COLOR);
            //if (_preference != null)
            //    _preference.setEnabled(!changeWidgetIconColorsByNightMode);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS);
            if (changeWidgetIconColorsByNightMode) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconIcon);
                }
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_CUSTOM_ICON_LIGHTNESS);
            if (_preference != null) {
                _preference.setEnabled(monochromeIconIcon);
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR) || keyIsWidgetOneRowChangeColorByNightMode) {
            Preference _preference;// = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_COLOR);
            //if (_preference != null)
            //    _preference.setEnabled(!changeWidgetOneRowColorsByNightMode);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ICON_LIGHTNESS);
            if (changeWidgetOneRowColorsByNightMode) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS);
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconOneRow);
                }
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS);
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconOneRow);
                }
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_CUSTOM_ICON_LIGHTNESS);
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconOneRow);
                }
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS);
                if (_preference != null) {
                    _preference.setEnabled(preferenceIndicatorsOneRowEnabled);
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR) || keyIsWidgetIconChangeColorByNightMode) {
            Preference _preference;// = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_COLOR);
            //if (_preference != null)
            //    _preference.setEnabled(!changeWidgetListColorsByNightMode);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ICON_LIGHTNESS);
            if (changeWidgetListColorsByNightMode) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS);
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconList);
                }
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS);
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconList);
                }
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_CUSTOM_ICON_LIGHTNESS);
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconList);
                }
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS);
                if (_preference != null) {
                    _preference.setEnabled(preferenceIndicatorsListEnabled);
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR)) {
            Preference _preference;// = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_COLOR);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_ICON_LIGHTNESS);
            if (_preference != null) {
                _preference.setEnabled(monochromeIconShortcut);
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SHORTCUT_CUSTOM_ICON_LIGHTNESS);
            if (_preference != null) {
                _preference.setEnabled(monochromeIconShortcut);
            }
        }
        //if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS)) {
        //    Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_ROUNDED_CORNERS_RADIUS);
        //    if (_preference != null)
        //        _preference.setEnabled(roundedCornersListEnabled);
        //}
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR) || keyIsWidgetListChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_PREF_INDICATOR_LIGHTNESS);
            if (_preference != null) {
                if (changeWidgetListColorsByNightMode) {
                    _preference.setEnabled(false);
                } else {
                    _preference.setEnabled(preferenceIndicatorsListEnabled);
                }
            }
        }
        //if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS)) {
        //    Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_ROUNDED_CORNERS_RADIUS);
        //    if (_preference != null)
        //        _preference.setEnabled(roundedCornersOneRowEnabled);
        //}
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR) || keyIsWidgetOneRowChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_PREF_INDICATOR_LIGHTNESS);
            if (_preference != null) {
                if (changeWidgetOneRowColorsByNightMode) {
                    _preference.setEnabled(false);
                } else {
                    _preference.setEnabled(preferenceIndicatorsOneRowEnabled);
                }
            }
        }
        //if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS)) {
        //    Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_ROUNDED_CORNERS_RADIUS);
        //    if (_preference != null)
        //        _preference.setEnabled(roundedCornersIconEnabled);
        //}
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_HIDE_PROFILE_NAME) || keyIsWidgetIconChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_LIGHTNESS_T);
            if (_preference != null) {
                if (changeWidgetIconColorsByNightMode) {
                    _preference.setEnabled(false);
                } else {
                    _preference.setEnabled(!hideProfileNameIcon);
                }
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_SHOW_PROFILE_DURATION);
            if (_preference != null)
                _preference.setEnabled(!hideProfileNameIcon);
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T) || keyIsSamsungEdgeChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_T);
            if (_preference != null)
                _preference.setEnabled(!changeSamsungEdgeColorsByNightMode);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE) || keyIsSamsungEdgeChangeColorByNightMode) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE);
            if (_preference != null)
                _preference.setEnabled(!changeSamsungEdgeColorsByNightMode);
            if (changeSamsungEdgeColorsByNightMode) {
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                if (_preference != null)
                    _preference.setEnabled(false);
            } else {
                if (preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_TYPE, false)) {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(true);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(false);
                } else {
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_BACKGROUND_COLOR);
                    if (_preference != null)
                        _preference.setEnabled(false);
                    _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_LIGHTNESS_B);
                    if (_preference != null)
                        _preference.setEnabled(true);
                }
            }
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR) || keyIsSamsungEdgeChangeColorByNightMode) {
            Preference _preference;// = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_COLOR);
            //if (_preference != null)
            //    _preference.setEnabled(!changeSamsungEdgeColorsByNightMode);
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_ICON_LIGHTNESS);
            if (changeSamsungEdgeColorsByNightMode) {
                if (_preference != null) {
                    _preference.setEnabled(false);
                }
            } else {
                if (_preference != null) {
                    _preference.setEnabled(monochromeIconSamsungEdge);
                }
            }
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_SAMSUNG_EDGE_CUSTOM_ICON_LIGHTNESS);
            if (_preference != null) {
                //_preference.setEnabled(false);
                _preference.setEnabled(monochromeIconSamsungEdge);
            }
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_OFF) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_USE_DYNAMIC_COLORS) ||
                keyIsWidgetIconChangeColorByNightMode) {
            Preference _preference;
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_OFF);
            if (_preference != null)
                _preference.setEnabled(changeWidgetIconColorsByNightMode && (!useDynamicColorsWidgetIcon));
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ICON_BACKGROUND_COLOR_NIGHT_MODE_ON);
            if (_preference != null)
                _preference.setEnabled(changeWidgetIconColorsByNightMode && (!useDynamicColorsWidgetIcon));
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_OFF) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_USE_DYNAMIC_COLORS) ||
                keyIsWidgetOneRowChangeColorByNightMode) {
            Preference _preference;
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_OFF);
            if (_preference != null)
                _preference.setEnabled(changeWidgetOneRowColorsByNightMode && (!useDynamicColorsWidgetOneRow));
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_ONE_ROW_BACKGROUND_COLOR_NIGHT_MODE_ON);
            if (_preference != null)
                _preference.setEnabled(changeWidgetOneRowColorsByNightMode && (!useDynamicColorsWidgetOneRow));
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_OFF) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_USE_DYNAMIC_COLORS) ||
                keyIsWidgetListChangeColorByNightMode) {
            Preference _preference;
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_OFF);
            if (_preference != null)
                _preference.setEnabled(changeWidgetListColorsByNightMode && (!useDynamicColorsWidgetList));
            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_WIDGET_LIST_BACKGROUND_COLOR_NIGHT_MODE_ON);
            if (_preference != null)
                _preference.setEnabled(changeWidgetListColorsByNightMode && (!useDynamicColorsWidgetList));
        }
    }

    private void setEnabled(String key) {

        Preference preference = prefMng.findPreference(key);

        if (preference == null)
            return;

        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();

        if (/*(android.os.Build.VERSION.SDK_INT >= 21) &&*/ (android.os.Build.VERSION.SDK_INT < 26)) {
            if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_SHOW_IN_STATUS_BAR)) {
                boolean show = preferences.getBoolean(key, true);
                Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_NOTIFICATION_HIDE_IN_LOCKSCREEN);
                if (_preference != null)
                    _preference.setEnabled(show);
            }
        }

        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT) ||
                //key.equals(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_NOTIFICATION_STYLE) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS) ||
                key.equals(ApplicationPreferences.PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS)) {
            String notificationStyle = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_NOTIFICATION_STYLE, "0");
            if (notificationStyle.equals("0")) {
                String backgroundColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
                //boolean nightMode = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE, false);
                boolean useDecoration = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, true);

                Preference _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR);
                if (_preference != null)
                    _preference.setEnabled(backgroundColor.equals("0") || backgroundColor.equals("5"));
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION);
                if (_preference != null) {
                    //if (Build.VERSION.SDK_INT < 29)
                    //    _preference.setEnabled(backgroundColor.equals("0") && (!nightMode));
                    //else
                    _preference.setEnabled(backgroundColor.equals("0"));
                }
                _preference = findPreference("notificationUseDecoratorInfo");
                if (_preference != null) {
                    //if (Build.VERSION.SDK_INT < 29)
                    //    _preference.setEnabled(backgroundColor.equals("0") && (!nightMode));
                    //else
                    _preference.setEnabled(backgroundColor.equals("0"));
                }

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT);
                if (_preference != null) {
                    //if (Build.VERSION.SDK_INT < 29)
                    //    _preference.setEnabled(useDecoration && backgroundColor.equals("0") && (!nightMode));
                    //else
                    _preference.setEnabled(useDecoration && backgroundColor.equals("0"));
                }
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON);
                if (_preference != null) {
                    //if (Build.VERSION.SDK_INT < 29)
                    //    _preference.setEnabled(useDecoration && backgroundColor.equals("0") && (!nightMode));
                    //else
                    _preference.setEnabled(useDecoration && backgroundColor.equals("0"));
                }

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR);
                if (_preference != null)
                    _preference.setEnabled(backgroundColor.equals("5"));

                _preference = findPreference("notificationBackgroundColorInfo");
                if (_preference != null)
                    _preference.setEnabled(true);
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);
                //_preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE);
                //if (_preference != null)
                //    _preference.setEnabled(true);
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE);
                if (_preference != null)
                    _preference.setEnabled(true);

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON);
                if (_preference != null)
                    _preference.setEnabled(useDecoration);
                // show profile icon for Android 12+ is better false
                boolean showProfileIcon = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON,
                        Build.VERSION.SDK_INT < 31);
                String profileIconColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR, "0");
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS);
                if (_preference != null)
                    _preference.setEnabled(showProfileIcon && (profileIconColor.equals("1")));
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS);
                if (_preference != null)
                    _preference.setEnabled(showProfileIcon && (profileIconColor.equals("1")));

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS);
                SwitchPreferenceCompat __preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR);
                if ((_preference != null) && (__preference != null)) {
                    _preference.setEnabled(__preference.isChecked());
                }
            } else {
                Preference _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT);
                if (_preference != null)
                    _preference.setEnabled(true);
                // dislabe, restart events action button is forced for native style
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_RESTART_EVENTS_AS_BUTTON);
                if (_preference != null)
                    _preference.setEnabled(false);

                _preference = findPreference("notificationBackgroundColorInfo");
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                //_preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_NIGHT_MODE);
                //if (_preference != null)
                //    _preference.setEnabled(false);
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_TEXT_COLOR);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION);
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = findPreference("notificationUseDecoratorInfo");
                if (_preference != null)
                    _preference.setEnabled(false);
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_LAYOUT_TYPE);
                if (_preference != null)
                    _preference.setEnabled(false);

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR);
                if (_preference != null)
                    _preference.setEnabled(true);

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON);
                if (_preference != null)
                    _preference.setEnabled(true);
                // show profile icon for Android 12+ is better false
                boolean showProfileIcon = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_SHOW_PROFILE_ICON,
                        Build.VERSION.SDK_INT < 31);
                String profileIconColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_COLOR, "0");
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PROFILE_ICON_LIGHTNESS);
                if (_preference != null)
                    _preference.setEnabled(showProfileIcon && (profileIconColor.equals("1")));
                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_CUSTOM_PROFILE_ICON_LIGHTNESS);
                if (_preference != null)
                    _preference.setEnabled(showProfileIcon && (profileIconColor.equals("1")));

                _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_PREF_INDICATOR_LIGHTNESS);
                if (_preference != null)
                    _preference.setEnabled(false);
            }
        }
        /*if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION)) {
            boolean useDecoration = preferences.getBoolean(ApplicationPreferences.PREF_NOTIFICATION_USE_DECORATION, true);
            String backgroundColor;// = "0";
            //if (Build.VERSION.SDK_INT < 29)
                backgroundColor = preferences.getString(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_COLOR, "0");
            Preference _preference = findPreference(ApplicationPreferences.PREF_NOTIFICATION_SHOW_BUTTON_EXIT);
            if (_preference != null)
                _preference.setEnabled(useDecoration && backgroundColor.equals("0"));
        }*/

        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR)) {
            return;
        }

        /*
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCANNING_IGNORE_HOTSPOT) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF)) {
            preference = findPreference(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCANNING_IGNORE_HOTSPOT);
            if (preference != null) {
                SwitchPreferenceCompat scanningEnabledPreference = findPreference(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING);
                SwitchPreferenceCompat scanIfWifiOffPreference = findPreference(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_IF_WIFI_OFF);
                if ((scanningEnabledPreference != null) && (scanIfWifiOffPreference != null))
                    preference.setEnabled(scanningEnabledPreference.isChecked() && scanIfWifiOffPreference.isChecked());
            }
        }
        */

        setEnabledWidgets(key);

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_FORCE_SET_MERGE_RINGER_NOTIFICATION_VOLUMES)) {
            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_UNLINK_RINGER_NOTIFICATION_VOLUMES);
            if (_preference != null) {
                boolean enabled;
                String value = preferences.getString(key, "0");
                if (!value.equals("0"))
                    enabled = value.equals("1");
                else
                    enabled = ApplicationPreferences.prefMergedRingNotificationVolumes;
                //Log.d("PhoneProfilesPrefsFragment.setSummary","enabled="+enabled);
                _preference.setEnabled(enabled);
            }
        }

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE)) {
            String stringValue = preferences.getString(key, "");
            long lProfileId;
            try {
                lProfileId = Long.parseLong(stringValue);
            } catch (Exception e) {
                lProfileId = 0;
            }

            Preference _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_SOUND);
            if (_preference != null)
                _preference.setEnabled(lProfileId != Profile.PROFILE_NO_ACTIVATE);

            _preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_NOTIFICATION_VIBRATE);
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            boolean hasVibrator = (vibrator != null) && vibrator.hasVibrator();
            if (hasVibrator) {
                if (_preference != null) {
                    _preference.setVisible(true);
                    _preference.setEnabled(lProfileId != Profile.PROFILE_NO_ACTIVATE);
                }
            } else {
                if (_preference != null)
                    _preference.setVisible(false);
            }

            //_preference = prefMng.findPreference(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE_USAGE);
            //if (_preference != null)
            //    _preference.setEnabled(lProfileId != Profile.PROFILE_NO_ACTIVATE);
        }
    }

    private void setSummary(String key) {

        setEnabled(key);

        Preference preference = prefMng.findPreference(key);

        if (preference == null)
            return;

        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_SCAN_INTERVAL)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_PERIODIC_SCANNING_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile)
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("periodicScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_UPDATE_INTERVAL)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_LOCATION_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventLocationDisabledScannigByProfile)
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("locationScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_SCAN_INTERVAL)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_WIFI_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventWifiDisabledScannigByProfile)
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("wifiScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_SCAN_INTERVAL)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_BLUETOOTH_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile)
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("bluetoothScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_MOBILE_CELL_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile)
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("mobileCellsScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING) ||
                key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_SCAN_INTERVAL)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_ORIENTATION_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile)
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("orientationScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
        }
        if (key.equals(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING)) {
            if (!preferences.getBoolean(ApplicationPreferences.PREF_APPLICATION_EVENT_NOTIFICATION_ENABLE_SCANNING, false)) {
                if (ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile)
                    preference.setSummary(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile);
                else
                    preference.setSummary(R.string.empty_string);
            }
            else
                preference.setSummary(R.string.empty_string);
            PreferenceScreen preferenceCategoryScreen = prefMng.findPreference("notififcationScanningCategoryRoot");
            if (preferenceCategoryScreen != null) setCategorySummary(preferenceCategoryScreen);
        }

        if (key.equals(ApplicationPreferences.PREF_NOTIFICATION_BACKGROUND_CUSTOM_COLOR)) {
            return;
        }

        ////////////////////

        /*if (key.equals(PREF_GRANT_ROOT_PERMISSION)) {
            if (PPApplication.isRooted()) {
                String summary;
                if (PPApplication.isRootGranted(true))
                    summary = getString(R.string.permission_granted);
                else
                    summary = getString(R.string.permission_not_granted);
                preference.setSummary(summary);
            }
        }*/
        //if (Build.VERSION.SDK_INT >= 23) {
            /*if (key.equals(PREF_APPLICATION_PERMISSIONS)) {
                // not possible to get granted runtime permission groups :-(
            }*/
        if (key.equals(PREF_WRITE_SYSTEM_SETTINGS_PERMISSIONS)) {
            String summary;
            if (Settings.System.canWrite(context))
                summary = getString(R.string.permission_granted);
            else {
                summary = getString(R.string.permission_not_granted);
                //summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_writeSystemSettingPermissions_summary);
            }
            preference.setSummary(summary);
        }
            /*if (key.equals(PREF_ACCESS_NOTIFICATION_POLICY_PERMISSIONS)) {
                String summary;
                if (Permissions.checkAccessNotificationPolicy(context))
                    summary = getString(R.string.permission_granted);
                else {
                    summary = getString(R.string.permission_not_granted);
                    summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions_summary);
                }
                preference.setSummary(summary);
            }*/
        if (key.equals(PREF_DRAW_OVERLAYS_PERMISSIONS)) {
            String summary;
            if (Settings.canDrawOverlays(context))
                summary = getString(R.string.permission_granted);
            else {
                summary = getString(R.string.permission_not_granted);
                //summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_drawOverlaysPermissions_summary);
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_GRANT_G1_PERMISSION)) {
            String summary;
            if (Permissions.hasPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS))
                summary = getString(R.string.permission_granted);
            else
                summary = getString(R.string.permission_not_granted);
            summary = summary + "\n\n" + getString(R.string.important_info_profile_grant) + " " +
                    getString(R.string.profile_preferences_types_G1_show_info);
            preference.setSummary(summary);
        }
            /*if (key.equals(PREF_APPLICATION_PERMISSIONS)) {
                String summary = getString(R.string.permission_granted);


                summary = summary + "\n\n" + getString(R.string.phone_profiles_pref_applicationPermissions_summary);
                preference.setSummary(summary);
            }*/
        //}
        if (key.equals(PREF_LOCATION_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventLocationSystemSettings_summary);
            if (!GlobalUtils.isLocationEnabled(context)) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + ".\n\n" +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n" +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_WIFI_LOCATION_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventWiFiLocationSystemSettings_summary);
            if (!GlobalUtils.isLocationEnabled(context)) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + ".\n\n" +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n" +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_BLUETOOTH_LOCATION_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventBluetoothLocationSystemSettings_summary);
            if (!GlobalUtils.isLocationEnabled(context)) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + ".\n\n" +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n" +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_MOBILE_CELLS_LOCATION_SYSTEM_SETTINGS)) {
            String summary;
            if (Build.VERSION.SDK_INT < 28)
                summary = getString(R.string.phone_profiles_pref_eventMobileCellsLocationSystemSettingsNotA9_summary);
            else
                summary = getString(R.string.phone_profiles_pref_eventMobileCellsLocationSystemSettings_summary);
            if (!GlobalUtils.isLocationEnabled(context)) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary) + ".\n\n" +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary) + ".\n\n" +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_WIFI_KEEP_ON_SYSTEM_SETTINGS)) {
            if (Build.VERSION.SDK_INT < 27) {
                String summary = getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_summary);
                if (GlobalUtils.isWifiSleepPolicySetToNever(context)) {
                    summary = getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_setToAlways_summary) + ".\n\n" +
                            summary;
                } else {
                    summary = getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_notSetToAlways_summary) + ".\n\n" +
                            summary;
                }
                preference.setSummary(summary);
            }
        }
        if (key.equals(PREF_NOTIFICATION_NOTIFICATION_ACCESS_SYSTEM_SETTINGS)) {
            String summary = getString(R.string.phone_profiles_pref_eventNotificationNotificationAccessSystemSettings_summary);
            if (!PPNotificationListenerService.isNotificationListenerServiceEnabled(context, true)) {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningNotificationAccessSettingsDisabled_summary) + ".\n\n" +
                        summary;
            }
            else {
                summary = getString(R.string.phone_profiles_pref_applicationEventScanningNotificationAccessSettingsEnabled_summary) + ".\n\n" +
                        summary;
            }
            preference.setSummary(summary);
        }
        if (key.equals(PREF_GRANT_ROOT_PERMISSION)) {
            String summary;
            boolean rooted = RootUtils.isRooted(true);
            if (rooted) {
                summary = getString(R.string.phone_profiles_pref_grantRootPermission_summary);
                if (ApplicationPreferences.applicationNeverAskForGrantRoot)
                    summary = getString(R.string.phone_profiles_pref_grantRootPermission_neverAsk_set_summary) + "\n\n" +
                            summary;
                else
                    summary = getString(R.string.phone_profiles_pref_grantRootPermission_neverAsk_notSet_summary_2) + "\n\n" +
                            summary;
                summary = getString(R.string.phone_profiles_pref_device_is_rooted) + "\n\n" + summary;
            }
            else
                summary = getString(R.string.phone_profiles_pref_device_is_not_rooted);
            preference.setSummary(summary);
        }



        // Do not bind toggles.
        if (preference instanceof CheckBoxPreference || preference instanceof SwitchPreferenceCompat) {
            return;
        }

        // Do not bind toggles.
        if (preference instanceof TimeDialogPreference) {
            return;
        }

        String stringValue = preferences.getString(key, "");

        if (key.equals(ApplicationPreferences.PREF_APPLICATION_DEFAULT_PROFILE)) {
            long lProfileId;
            try {
                lProfileId = Long.parseLong(stringValue);
            } catch (Exception e) {
                lProfileId = 0;
            }
            ProfilePreference profilePreference = (ProfilePreference) preference;
            profilePreference.setSummary(lProfileId);

        } else if (preference instanceof PPListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            PPListPreference listPreference = (PPListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            // added support for "%" in list items
            CharSequence summary = (index >= 0) ? listPreference.getEntries()[index] : null;
            if (summary != null) {
                String sSummary = summary.toString();
                sSummary = sSummary.replace("%", "%%");
                preference.setSummary(sSummary);
            } else
                preference.setSummary(null);

            //if (key.equals(PPApplication.PREF_APPLICATION_LANGUAGE))
            //    setTitleStyle(preference, true, false);
        } else
        //noinspection StatementWithEmptyBody
        if (preference instanceof RingtonePreference) {
            // keep summary from preference
        }
        else
        //noinspection StatementWithEmptyBody
        if (preference instanceof ColorChooserPreference) {
            // keep summary from preference
        }
        else
        //noinspection StatementWithEmptyBody
        if (preference instanceof RestartEventsIconColorChooserPreference) {
            // keep summary from preference
        }
        else {
            if (!stringValue.isEmpty()) {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                //preference.setSummary(preference.toString());
                preference.setSummary(stringValue);
            }
        }
    }

    private void setCategorySummary(Preference preferenceCategory) {
        if (getActivity() == null)
            return;

        Context context = getActivity().getApplicationContext();


        String key = preferenceCategory.getKey();

        //boolean addEnd = true;

        String summary = "";
        if (key.equals("applicationInterfaceCategoryRoot")) {
            /*if (DebugVersion.enabled) {
                // force set it to english for debug mode
                preferenceCategory.setTitle("Application interface (not translated in DEBUG)");
            }*/
            /*summary = summary + getString(R.string.phone_profiles_pref_applicationLanguage);
            if (!summary.isEmpty()) summary = summary +" • ";*/
            summary = summary + getString(R.string.phone_profiles_pref_applicationTheme);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHomeLauncher);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLauncher);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationLauncher);
        }
        if (key.equals("categoryApplicationStartRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationStartOnBoot);
            /*if (KillerManager.isActionAvailable(context, KillerManager.Actions.ACTION_AUTOSTART)) {
                if (!summary.isEmpty()) summary = summary +" • ";
                summary = summary + getString(R.string.phone_profiles_pref_systemAutoStartManager);
            }*/
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationActivate);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationStartEvents);
        }
        if (key.equals("categorySystemRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationUnlinkRingerNotificationVolumes);
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationForceSetMergeRingNotificationVolumes);
            //if (Build.VERSION.SDK_INT >= 23) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationBatteryOptimization);
            //}
            /*if (KillerManager.isActionAvailable(context, KillerManager.Actions.ACTION_POWERSAVING)) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationPowerManager);
            }*/
            if (!summary.isEmpty()) summary = summary +" • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationPowerSaveMode);
            //if (Build.VERSION.SDK_INT >= 21) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationBatterySaver);
            //}
        }
        if (key.equals("categoryPermissionsRoot")) {
            if (RootUtils.isRooted(true)) {
                summary = summary + getString(R.string.phone_profiles_pref_grantRootPermission);
            }
            //if (Build.VERSION.SDK_INT >= 23) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_writeSystemSettingPermissions);
                //if (!summary.isEmpty()) summary = summary + " • ";
                //summary = summary + getString(R.string.phone_profiles_pref_accessNotificationPolicyPermissions);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_drawOverlaysPermissions);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationPermissions);
            //}
        }
        if (key.equals("categoryNotificationsRoot")) {
            //summary = summary + getString(R.string.phone_profiles_pref_notificationsToast);
            if (Build.VERSION.SDK_INT >= 26) {
                //if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_notificationSystemSettings);
            }
            /*else {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_notificationStatusBar);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_notificationStatusBarPermanent);
            }*/
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationStatusBarStyle);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationNotificationStyle);
            //if (Build.VERSION.SDK_INT >= 24) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_notificationShowProfileIcon);
            //}
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationLayoutType);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationBackgroundColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationTextColor);
            //if (Build.VERSION.SDK_INT >= 24) {
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_notificationUseDecoration);
            //}
        }
        if (key.equals("profileActivationCategoryRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventBackgroundProfile);
            //if (!summary.isEmpty()) summary = summary + " • ";
            //summary = summary + getString(R.string.phone_profiles_pref_backgroundProfileUsage);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationAlert);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_notificationsToast);
        }
        if (key.equals("specialProfileParametersCategoryRoot")) {
            if (PPApplication.deviceIsHuawei && PPApplication.romIsEMUI) {
                summary = summary + getString(R.string.phone_profiles_pref_applicationForceSetBrightnessAtScreenOn);
                //if (!summary.isEmpty()) summary = summary + " • ";
            }
        }
        if (key.equals("eventRunCategoryRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_eventRunUsePriority);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationRestartEventsAlert);
        }
        if (key.equals("periodicScanningCategoryRoot")) {
            ApplicationPreferences.applicationEventPeriodicScanningEnableScanning(context);
            ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventPeriodicScanningScanInterval(context);
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventBackgroundScanningEnableScanning) + ": ";
            if (ApplicationPreferences.applicationEventPeriodicScanningEnableScanning) {
                summary = summary + "<b>" +
                        getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_enabled), prefMng, key, context)
                        + "</b>";

                summary = summary + "<br><br>";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventBackgroundScanningScanInterval) + ": " +
                        "<b>" +
                        getColorForChangedPreferenceValue(String.valueOf(ApplicationPreferences.applicationEventPeriodicScanningScanInterval), prefMng, key, context)
                        + "</b>";
                summary = summary + "<br><br>";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
            } else {
                if (!ApplicationPreferences.applicationEventPeriodicScanningDisabledScannigByProfile)
                    summary = summary + "<b>" +
                            getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_disabled), prefMng, key, context)
                            + "</b>";
                else
                    summary = summary + "<b>" +
                            getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile), prefMng, key, context)
                            + "</b>";
            }
        }
        if (key.equals("locationScanningCategoryRoot")) {
            ApplicationPreferences.applicationEventLocationEnableScanning(context);
            ApplicationPreferences.applicationEventLocationDisabledScannigByProfile(context);
            ApplicationPreferences.applicationEventLocationUpdateInterval(context);
            summary = summary + getString(R.string.phone_profiles_pref_applicationEventLocationEnableScanning) + ": ";
            if (ApplicationPreferences.applicationEventLocationEnableScanning) {
                summary = summary + "<b>" +
                        getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_enabled), prefMng, key, context)
                        + "</b>";
                if (!GlobalUtils.isLocationEnabled(context)) {
                    summary = summary + "<br>";
                    summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                            "<b>" +
                            getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary), prefMng, key, context)
                            + "</b>";
                } else {
                    summary = summary + "<br>";
                    summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                            "<b>" +
                            getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary), prefMng, key, context)
                            + "</b>";
                }

                summary = summary + "<br><br>";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventLocationScanInterval) + ": " +
                        "<b>" +
                        getColorForChangedPreferenceValue(String.valueOf(ApplicationPreferences.applicationEventLocationUpdateInterval), prefMng, key, context)
                        + "</b>";
                summary = summary + "<br><br>";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventLocationsEditor);
                summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
                summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventLocationsUseGPS);
            }
            else {
                if (!ApplicationPreferences.applicationEventLocationDisabledScannigByProfile)
                    summary = summary + "<b>" +
                            getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_disabled), prefMng, key, context)
                            + "</b>";
                else
                    summary = summary + "<b>" +
                            getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile), prefMng, key, context)
                            + "</b>";
            }
        }
        if (key.equals("wifiScanningCategoryRoot")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context);
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getString(R.string.profile_preferences_device_not_allowed) +
                        ": <b>" +
                        getColorForChangedPreferenceValue(preferenceAllowed.getNotAllowedPreferenceReasonString(context), prefMng, key, context)
                        + "</b>";
            }
            else {
                ApplicationPreferences.applicationEventWifiEnableScanning(context);
                ApplicationPreferences.applicationEventWifiDisabledScannigByProfile(context);
                ApplicationPreferences.applicationEventWifiScanInterval(context);
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventWifiEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventWifiEnableScanning) {
                    summary = summary + "<b>" +
                            getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_enabled), prefMng, key, context)
                            + "</b>";
                    if (!GlobalUtils.isLocationEnabled(context)) {
                        summary = summary + "<br>";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary), prefMng, key, context)
                                + "</b>";
                    } else {
                        summary = summary + "<br>";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary), prefMng, key, context)
                                + "</b>";
                    }
                    if (Build.VERSION.SDK_INT < 27) {
                        if (GlobalUtils.isWifiSleepPolicySetToNever(context)) {
                            summary = summary + "<br>";
                            summary = summary + getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings) + ": " +
                                    "<b>" +
                                    getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_setToAlways_summary), prefMng, key, context)
                                    + "</b>";
                        } else {
                            summary = summary + "<br>";
                            summary = summary + getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings) + ": " +
                                    "<b>" +
                                    getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_eventWiFiKeepOnSystemSettings_notSetToAlways_summary), prefMng, key, context)
                                    + "</b>";
                        }
                    }

                    summary = summary + "<br><br>";
                    summary = summary + getString(R.string.phone_profiles_pref_applicationEventWifiScanInterval) + ": " +
                            "<b>" +
                            getColorForChangedPreferenceValue(String.valueOf(ApplicationPreferences.applicationEventWifiScanInterval), prefMng, key, context)

                            + "</b>";
                    summary = summary + "<br><br>";
                    summary = summary + getString(R.string.phone_profiles_pref_applicationEventWifiScanIfWifiOff);
                    summary = summary + " • ";
                    summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
                } else {
                    if (!ApplicationPreferences.applicationEventWifiDisabledScannigByProfile)
                        summary = summary + "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_disabled), prefMng, key, context)
                                + "</b>";
                    else
                        summary = summary + "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile), prefMng, key, context)
                                + "</b>";
                }
            }
        }
        if (key.equals("bluetoothScanningCategoryRoot")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context);
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getString(R.string.profile_preferences_device_not_allowed) +
                        ": <b>" +
                        getColorForChangedPreferenceValue(preferenceAllowed.getNotAllowedPreferenceReasonString(context), prefMng, key, context)
                        + "</b>";
            }
            else {
                ApplicationPreferences.applicationEventBluetoothEnableScanning(context);
                ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile(context);
                ApplicationPreferences.applicationEventBluetoothScanInterval(context);
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventBluetoothEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventBluetoothEnableScanning) {
                    summary = summary + "<b>" +
                            getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_enabled), prefMng, key, context)
                            + "</b>";
                    if (!GlobalUtils.isLocationEnabled(context)) {
                        summary = summary + "<br>";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary), prefMng, key, context)
                                + "</b<";
                    } else {
                        summary = summary + "<br>";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary), prefMng, key, context)
                                + "</b>";
                    }

                    summary = summary + "<br><br>";
                    summary = summary + getString(R.string.phone_profiles_pref_applicationEventBluetoothScanInterval) + ": " +
                            "<b>" +
                            getColorForChangedPreferenceValue(String.valueOf(ApplicationPreferences.applicationEventBluetoothScanInterval), prefMng, key, context)
                            + "</b>";
                    summary = summary + "<br><br>";
                    summary = summary + getString(R.string.phone_profiles_pref_applicationEventBluetoothScanIfBluetoothOff);
                    summary = summary + " • ";
                    summary = summary + getString(R.string.phone_profiles_pref_applicationEventBluetoothLEScanDuration);
                    summary = summary + " • ";
                    summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
                } else {
                    if (!ApplicationPreferences.applicationEventBluetoothDisabledScannigByProfile)
                        summary = summary + "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_disabled), prefMng, key, context)
                                + "</b>";
                    else
                        summary = summary + "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile), prefMng, key, context)
                                + "</b>";
                }
            }
        }
        if (key.equals("mobileCellsScanningCategoryRoot")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, context);
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getString(R.string.profile_preferences_device_not_allowed) +
                        ": <b>" +
                        getColorForChangedPreferenceValue(preferenceAllowed.getNotAllowedPreferenceReasonString(context), prefMng, key, context)
                        + "</b>";
                //addEnd = false;
            }
            else {
                ApplicationPreferences.applicationEventMobileCellEnableScanning(context);
                ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile(context);
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventMobileCellEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventMobileCellEnableScanning) {
//                    PPApplication.logE("[TEST BATTERY] PhoneProfilesPrefsFragment.setCategorySummary", "******** ### *******");
                    summary = summary + "<b>" +
                            getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_enabled), prefMng, key, context)
                            + "</b>";
                    if (!GlobalUtils.isLocationEnabled(context)) {
                        summary = summary + "<br>";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsDisabled_summary), prefMng, key, context)
                                + "</b>";
                    } else {
                        summary = summary + "<br>";
                        summary = summary + getString(R.string.phone_profiles_pref_eventLocationSystemSettings) + ": " +
                                "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningLocationSettingsEnabled_summary), prefMng, key, context)
                                + "</b>";
                    }
                    summary = summary + "<br><br>";
                    summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
                } else {
                    if (!ApplicationPreferences.applicationEventMobileCellDisabledScannigByProfile)
                        summary = summary + "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_disabled), prefMng, key, context)
                                + "</b>";
                    else
                        summary = summary + "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile), prefMng, key, context)
                                + "</b>";
                }
            }
        }
        if (key.equals("orientationScanningCategoryRoot")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, context);
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getString(R.string.profile_preferences_device_not_allowed) +
                        ": <b>" +
                        getColorForChangedPreferenceValue(preferenceAllowed.getNotAllowedPreferenceReasonString(context), prefMng, key, context)
                        + "</b>";
            }
            else {
                ApplicationPreferences.applicationEventOrientationEnableScanning(context);
                ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile(context);
                ApplicationPreferences.applicationEventOrientationScanInterval(context);
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventOrientationEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventOrientationEnableScanning) {
//                    PPApplication.logE("[TEST BATTERY] PhoneProfilesPrefsFragment.setCategorySummary", "******** ### *******");
                    summary = summary + "<b>" +
                            getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_enabled), prefMng, key, context)
                            + "</b>";
                    summary = summary + "<br><br>";
                    summary = summary + getString(R.string.phone_profiles_pref_applicationEventOrientationScanInterval) + ": " +
                            "<b>" +
                            getColorForChangedPreferenceValue(String.valueOf(ApplicationPreferences.applicationEventOrientationScanInterval), prefMng, key, context)
                            + "</b>";
                    summary = summary + "<br><br>";
                    summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
                }
                else {
                    if (!ApplicationPreferences.applicationEventOrientationDisabledScannigByProfile)
                        summary = summary + "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_disabled), prefMng, key, context)
                                + "</b>";
                    else
                        summary = summary + "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile), prefMng, key, context)
                                + "</b>";
                }
            }
        }
        if (key.equals("notificationScanningCategoryRoot")) {
            PreferenceAllowed preferenceAllowed = Event.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, context);
            if (preferenceAllowed.allowed != PreferenceAllowed.PREFERENCE_ALLOWED) {
                summary = summary + getString(R.string.profile_preferences_device_not_allowed) +
                        ": <b>" +
                        getColorForChangedPreferenceValue(preferenceAllowed.getNotAllowedPreferenceReasonString(context), prefMng, key, context)
                        + "</b>";
            }
            else {
                ApplicationPreferences.applicationEventNotificationEnableScanning(context);
                ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile(context);
                summary = summary + getString(R.string.phone_profiles_pref_applicationEventNotificationEnableScanning) + ": ";
                if (ApplicationPreferences.applicationEventNotificationEnableScanning) {
                    summary = summary + "<b>" +
                            getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_enabled), prefMng, key, context)
                            + "</b>";
                    if (!PPNotificationListenerService.isNotificationListenerServiceEnabled(context, true)) {
                        summary = summary + "<br>";
                        summary = summary + getString(R.string.phone_profiles_pref_eventNotificationAccessSystemSettings) + ": " +
                                "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningNotificationAccessSettingsDisabled_summary), prefMng, key, context)
                                + "</b>";
                    } else {
                        summary = summary + "<br>";
                        summary = summary + getString(R.string.phone_profiles_pref_eventNotificationAccessSystemSettings) + ": " +
                                "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningNotificationAccessSettingsEnabled_summary), prefMng, key, context)
                                + "</b>";
                    }
                    summary = summary + "<br><br>";
                    summary = summary + getString(R.string.phone_profiles_pref_applicationEventScanOnlyWhenScreenIsOn);
                } else {
                    if (!ApplicationPreferences.applicationEventNotificationDisabledScannigByProfile)
                        summary = summary + "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.array_pref_applicationDisableScanning_disabled), prefMng, key, context)
                                + "</b>";
                    else
                        summary = summary + "<b>" +
                                getColorForChangedPreferenceValue(getString(R.string.phone_profiles_pref_applicationEventScanningDisabledByProfile), prefMng, key, context)
                                + "</b>";
                }
            }
        }
        if (key.equals("categoryActivatorRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            //if (!summary.isEmpty()) summary = summary + " • ";
            //summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationClose);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationGridLayout);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationActivatorNumColumns);
        }
        if (key.equals("categoryEditorRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            //if (!summary.isEmpty()) summary = summary + " • ";
            //summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHideHeaderOrBottomBar);
            //if (!summary.isEmpty()) summary = summary + " • ";
            //summary = summary + getString(R.string.phone_profiles_pref_applicationEditorSaveEditorState);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_deleteOldActivityLogs);
        }
        if (key.equals("categoryWidgetListRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationGridLayout);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackground);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetColorB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetShowBorder);
            //if (!summary.isEmpty()) summary = summary + " • ";
            //summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetRoundedCorners);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetCornerRadius);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessT);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
        }
        if (key.equals("categoryWidgetOneRowRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationPrefIndicator);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackground);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetColorB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetShowBorder);
            //if (!summary.isEmpty()) summary = summary + " • ";
            //summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetRoundedCorners);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetCornerRadius);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessT);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
        }
        if (key.equals("categoryWidgetIconRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconBackgroundType);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconBackground);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconLightnessB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColorB);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconShowBorder);
            //if (!summary.isEmpty()) summary = summary + " • ";
            //summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconRoundedCorners);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconCornerRadius);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconHideProfileName);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconLightnessT);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconShowProfileEndOfActivation);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
        }
        if (key.equals("categoryShortcutRoot")) {
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
        }
        if ((PPApplication.sLook != null) && PPApplication.sLookCocktailPanelEnabled) {
            if (key.equals("categorySamsungEdgePanelRoot")) {
                summary = summary + getString(R.string.phone_profiles_pref_applicationSamsungEdgeVerticalPosition);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationHeader);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackgroundType);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetBackground);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessB);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetColorB);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessT);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetIconColor);
                if (!summary.isEmpty()) summary = summary + " • ";
                summary = summary + getString(R.string.phone_profiles_pref_applicationWidgetLightnessI);
            }
        }

        /*if (addEnd) {
            if (!summary.isEmpty()) summary = summary + " • ";
            summary = summary + "…";
        }*/

        preferenceCategory.setSummary(StringFormatUtils.fromHtml(summary, false, false, false, 0, 0, true));
        //preferenceCategory.setSummary(summary);
    }

    static String getColorForChangedPreferenceValue(String preferenceValue,
                                                    PreferenceManager prefMng,
                                                    String preferenceKey,
                                                    Context context) {
        Preference preference = prefMng.findPreference(preferenceKey);
        if ((preference != null) && preference.isEnabled()) {
            int labelColor = ContextCompat.getColor(context, R.color.activityNormalTextColor);
            String colorString = String.format("%X", labelColor).substring(2); // !!strip alpha value!!
            return String.format("<font color=\"#%s\">%s</font>"/*+":"*/, colorString, preferenceValue);
        } else
            return preferenceValue;
    }

}
