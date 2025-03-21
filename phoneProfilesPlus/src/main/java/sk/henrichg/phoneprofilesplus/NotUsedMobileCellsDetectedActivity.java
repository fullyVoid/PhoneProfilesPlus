package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class NotUsedMobileCellsDetectedActivity extends AppCompatActivity {

    AlertDialog mDialog;
    private TextView cellIdTextView;
    private TextView lastConnectTimeTextView;
    TextView cellNameTextView;
    private ListView lastRunningEventsListView;
    private MobileCellNamesDialog mMobileCellNamesDialog;

    private int mobileCellId = Integer.MAX_VALUE;
    private long lastConnectedTime = 0;
    //private String lastRunningEvents = "";
    private String lastPausedEvents = "";

    private final List<Event> eventList = new ArrayList<>();

    static final String EXTRA_MOBILE_CELL_ID = "mobile_cell_id";
    static final String EXTRA_MOBILE_LAST_CONNECTED_TIME = "last_connected_time";
    //static final String EXTRA_MOBILE_LAST_RUNNING_EVENTS = "last_running_events";
    static final String EXTRA_MOBILE_LAST_PAUSED_EVENTS = "last_paused_events";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

//        PPApplication.logE("[BACKGROUND_ACTIVITY] NotUsedMobileCellsDetectedActivity.onCreate", "xxx");

        Intent intent = getIntent();
        if (intent != null) {
            mobileCellId = intent.getIntExtra(EXTRA_MOBILE_CELL_ID, 0);
            lastConnectedTime = intent.getLongExtra(EXTRA_MOBILE_LAST_CONNECTED_TIME, 0);
            //lastRunningEvents = intent.getStringExtra(EXTRA_MOBILE_LAST_RUNNING_EVENTS);
            lastPausedEvents = intent.getStringExtra(EXTRA_MOBILE_LAST_PAUSED_EVENTS);
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

        GlobalGUIRoutines.lockScreenOrientation(this, true);

        // set theme and language for dialog alert ;-)
        GlobalGUIRoutines.setTheme(this, true, false/*, false*/, false, false, false, false);
        //GlobalGUIRoutines.setLanguage(this);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.not_used_mobile_cells_detected_title);
        dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            // 1. test existence of mobile cell id in table, may be deleted
            // 2. test existence of event in table, may be deleted

            final int _mobileCellId = mobileCellId;
            final long _lastConnectedTime = lastConnectedTime;
            //final String _lastRunningEvents = lastRunningEvents;
            //final String _lastPausedEvents = lastPausedEvents;
            final String _cellName = cellNameTextView.getText().toString();

            final Context appContext = getApplicationContext();
            //PPApplication.startHandlerThread(/*"NotUsedMobileCellsDetectedActivity.onClick"*/);
            //final Handler __handler = new Handler(PPApplication.handlerThread.getLooper());
            //__handler.post(new PPApplication.PPHandlerThreadRunnable(getApplicationContext()) {
            //__handler.post(() -> {
            Runnable runnable = () -> {
                //                        PPApplication.logE("[IN_EXECUTOR] PPApplication.startHandlerThread", "START run - from=NotUsedMobileCellsDetectedActivity.onStart (1)");

                //Context appContext= appContextWeakRef.get();

                //if (appContext != null) {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":NotUsedMobileCellsDetectedActivity_onStart_1");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    if (!_cellName.isEmpty()) {

                        DatabaseHandler db = DatabaseHandler.getInstance(appContext);

                        // add or rename cell with _cellName
                        List<MobileCellsData> localCellsList = new ArrayList<>();
                        localCellsList.add(new MobileCellsData(_mobileCellId, _cellName,
                                true, false, _lastConnectedTime/*, _lastRunningEvents, _lastPausedEvents, false*/));
                        db.saveMobileCellsList(localCellsList, true, true);

                        // used are lastRunningEvents and lastPausedEvents
                        // for update configured mobile cellls in events

                        /*
                        // add cell to running events
                        String[] eventIds = _lastRunningEvents.split("\\|");
                        for (String eventId : eventIds) {
                            if (!eventId.isEmpty()) {
                                long _eventId = Long.parseLong(eventId);
                                String currentCells = db.getEventMobileCellsCells(_eventId);
                                if (!currentCells.isEmpty()) {
                                    String newCells = MobileCellsScanner.addCellId(currentCells, _mobileCellId);
                                    db.updateMobileCellsCells(_eventId, newCells);

                                    // broadcast for event preferences
                                    Intent intent = new Intent(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_NEW_CELL);
                                    intent.putExtra(PPApplication.EXTRA_EVENT_ID, eventId);
                                    intent.putExtra(MobileCellsRegistrationService.EXTRA_NEW_CELL_VALUE, _mobileCellId);
                                    intent.setPackage(PPApplication.PACKAGE_NAME);
                                    appContext.sendBroadcast(intent);

                                    //Intent refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver");
                                    //refreshIntent.putExtra(PPApplication.EXTRA_EVENT_ID, eventId);
                                    //LocalBroadcastManager.getInstance(appContext).sendBroadcast(refreshIntent);
                                }
                            }
                        }
                        */
                        // add cell to paused events
                        for (Event event : eventList) {
                            Event eventFromDB = db.getEvent(event._id);
                            if ((eventFromDB != null) &&
                                    (eventFromDB.getStatus() != Event.ESTATUS_STOP) &&
                                    (eventFromDB._eventPreferencesMobileCells != null) &&
                                    eventFromDB._eventPreferencesMobileCells._enabled) {
                                //if (db.eventExists(event)) {
                                if (event.getStatus() == 1) {
                                    // event is checked in listView
                                    String currentCells = db.getEventMobileCellsCells(event._id);
                                    if (!currentCells.isEmpty()) {
                                        String newCells = MobileCellsScanner.addCellId(currentCells, _mobileCellId);
                                        db.updateMobileCellsCells(event._id, newCells);

                                        // broadcast for event preferences
                                        Intent intent = new Intent(MobileCellsRegistrationService.ACTION_MOBILE_CELLS_REGISTRATION_NEW_CELL);
                                        intent.putExtra(PPApplication.EXTRA_EVENT_ID, event._id);
                                        intent.putExtra(MobileCellsRegistrationService.EXTRA_NEW_CELL_VALUE, _mobileCellId);
                                        intent.setPackage(PPApplication.PACKAGE_NAME);
                                        appContext.sendBroadcast(intent);

                                        //Intent refreshIntent = new Intent(PPApplication.PACKAGE_NAME + ".RefreshActivitiesBroadcastReceiver");
                                        //refreshIntent.putExtra(PPApplication.EXTRA_EVENT_ID, eventId);
                                        //LocalBroadcastManager.getInstance(appContext).sendBroadcast(refreshIntent);
                                    }
                                }
                            }
                        }

                        if ((PhoneProfilesService.getInstance() != null) && PPApplication.mobileCellsScanner != null) {
                            PPApplication.mobileCellsScanner.handleEvents(appContext);
                        }
                        // must be higher then delay in handleEvents
                        //                        PPApplication.logE("[PPP_NOTIFICATION] NotUsedMobileCellsDetectedActivity.onStart", "call of updateGUI");
                        PPApplication.updateGUI(false, true, appContext);
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
            PPApplication.createBasicExecutorPool();
            PPApplication.basicExecutorPool.submit(runnable);

            finish();
        });
        dialogBuilder.setNegativeButton(android.R.string.cancel, null);

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_not_used_mobile_cells_detected, null);
        dialogBuilder.setView(layout);

        mDialog = dialogBuilder.create();

        mDialog.setOnShowListener(dialog -> {
//                Button positive = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
//                if (positive != null) positive.setAllCaps(false);
//                Button negative = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
//                if (negative != null) negative.setAllCaps(false);

            new ShowActivityAsyncTask(this).execute();
        });

        cellIdTextView = layout.findViewById(R.id.not_used_mobile_cells_dlg_cell_id);
        lastConnectTimeTextView = layout.findViewById(R.id.not_used_mobile_cells_dlg_connection_time);
        cellNameTextView = layout.findViewById(R.id.not_used_mobile_cells_dlg_cells_name);
        lastRunningEventsListView = layout.findViewById(R.id.not_used_mobile_cells_dlg_last_running_events_listview);

        mMobileCellNamesDialog = new MobileCellNamesDialog(this, null, false);
        cellNameTextView.setOnClickListener(view -> {
            if (!isFinishing())
                mMobileCellNamesDialog.show();
        }
        );

        cellNameTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                String cellName = cellNameTextView.getText().toString();

                boolean anyChecked = false;
                for (Event event : eventList) {
                    if (event.getStatus() == 1) {
                        anyChecked = true;
                        break;
                    }
                }
                mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!cellName.isEmpty() && anyChecked);
            }
        });


        if (!isFinishing())
            mDialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        GlobalGUIRoutines.unlockScreenOrientation(this);
    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /*
    private void doShow() {
        new ShowActivityAsyncTask(this).execute();
    }
    */

    private static class ShowActivityAsyncTask extends AsyncTask<Void, Integer, Void> {

        DatabaseHandler db;
        List<MobileCellsData> _cellsList = null;
        String cellName;
        final List<Event> _eventList = new ArrayList<>();

        private final WeakReference<NotUsedMobileCellsDetectedActivity> activityWeakReference;

        public ShowActivityAsyncTask(NotUsedMobileCellsDetectedActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            NotUsedMobileCellsDetectedActivity activity = activityWeakReference.get();
            if (activity != null) {
                db = DatabaseHandler.getInstance(activity.getApplicationContext());
                _cellsList = new ArrayList<>();
                cellName = "";
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            NotUsedMobileCellsDetectedActivity activity = activityWeakReference.get();
            if (activity != null) {
                db.addMobileCellsToList(_cellsList, activity.mobileCellId);
                if (!_cellsList.isEmpty())
                    cellName = _cellsList.get(0).name;

                //eventList.clear();

                /*
                String[] eventIds = activity.lastRunningEvents.split("\\|");
                for (String eventId : eventIds) {
                    if (!eventId.isEmpty()) {
                        Event event = db.getEvent(Long.parseLong(eventId));
                        if (event != null) {
                            event.setStatus(1); // use status of event for checkbox status
                            _eventList.add(event);
                        }
                    }
                }
                */

                String[] eventIds = activity.lastPausedEvents.split("\\|");
                for (String eventId : eventIds) {
                    if (!eventId.isEmpty()) {
                        Event event = db.getEvent(Long.parseLong(eventId));
                        if (event != null) {
                            event.setStatus(1); // use status of event for checkbox status
                            _eventList.add(event);
                        }
                    }
                }
            }

            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            NotUsedMobileCellsDetectedActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.cellIdTextView.setText(activity.getString(R.string.not_used_mobile_cells_detected_cell_id) + " " + activity.mobileCellId);
                activity.lastConnectTimeTextView.setText(activity.getString(R.string.not_used_mobile_cells_detected_connection_time) + " " +
                        StringFormatUtils.timeDateStringFromTimestamp(activity, activity.lastConnectedTime));
                if (!cellName.isEmpty())
                    activity.cellNameTextView.setText(cellName);

                activity.eventList.clear();
                activity.eventList.addAll(_eventList);

                NotUsedMobileCellsDetectedAdapter notUsedMobileCellsDetectedAdapter =
                        new NotUsedMobileCellsDetectedAdapter(activity, activity.eventList);
                activity.lastRunningEventsListView.setAdapter(notUsedMobileCellsDetectedAdapter);

                boolean anyChecked = false;
                for (Event event : activity.eventList) {
                    if (event.getStatus() == 1) {
                        anyChecked = true;
                        break;
                    }
                }
                // cellName must be set to enable positive button
                activity.mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!cellName.isEmpty() && anyChecked);
            }
        }

    }

}
