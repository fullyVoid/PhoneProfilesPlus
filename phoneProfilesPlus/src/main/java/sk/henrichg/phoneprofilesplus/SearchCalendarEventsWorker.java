package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SearchCalendarEventsWorker extends Worker {

    private final Context context;

    static final String WORK_TAG  = "SearchCalendarEventsJob";
    static final String WORK_TAG_SHORT  = "SearchCalendarEventsJobShort";

    public SearchCalendarEventsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
//            long start = System.currentTimeMillis();
//            PPApplication.logE("[IN_WORKER]  SearchCalendarEventsWorker.doWork", "--------------- START");

            if (!PPApplication.getApplicationStarted(true, true)) {
                // application is not started
                return Result.success();
            }

            if (Event.getGlobalEventsRunning()) {
                // start events handler
//                PPApplication.logE("[EVENTS_HANDLER_CALL] SearchCalendarEventsWorker.doWork", "sensorType=SENSOR_TYPE_SEARCH_CALENDAR_EVENTS");
                EventsHandler eventsHandler = new EventsHandler(context);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_SEARCH_CALENDAR_EVENTS);
            }

//            PPApplication.logE("[EXECUTOR_CALL]  ***** SearchCalendarEventsWorker.doWork", "schedule - SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG");
            //final Context appContext = context;
            //final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
            Runnable runnable = () -> {
//                long start1 = System.currentTimeMillis();
//                PPApplication.logE("[IN_EXECUTOR]  ***** SearchCalendarEventsWorker.doWork", "--------------- START - SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG");
                SearchCalendarEventsWorker.scheduleWork(false);
//                long finish = System.currentTimeMillis();
//                long timeElapsed = finish - start1;
//                PPApplication.logE("[IN_EXECUTOR]  ***** SearchCalendarEventsWorker.doWork", "--------------- END - SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG - timeElapsed="+timeElapsed);
                //worker.shutdown();
            };
            PPApplication.createDelayedEventsHandlerExecutor();
            PPApplication.delayedEventsHandlerExecutor.schedule(runnable, 5, TimeUnit.SECONDS);
            /*
            //scheduleWork(false);
            OneTimeWorkRequest worker =
                    new OneTimeWorkRequest.Builder(MainWorker.class)
                            .addTag(MainWorker.SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG)
                            .setInitialDelay(5000, TimeUnit.MILLISECONDS)
                            .build();
            try {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

//                            //if (PPApplication.logEnabled()) {
//                            ListenableFuture<List<WorkInfo>> statuses;
//                            statuses = workManager.getWorkInfosForUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG);
//                            try {
//                                List<WorkInfo> workInfoList = statuses.get();
//                            } catch (Exception ignored) {
//                            }
//                            //}

//                    PPApplication.logE("[WORKER_CALL] SearchCalendarEventsWorker.doWork", "xxx");
                    workManager.enqueueUniqueWork(MainWorker.SCHEDULE_LONG_INTERVAL_SEARCH_CALENDAR_WORK_TAG, ExistingWorkPolicy.REPLACE, worker);
                }
            } catch (Exception e) {
                PPApplication.recordException(e);
            }
            */
            /*
            PPApplication.startHandlerThreadPPScanners();
            final Handler handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scheduleWork(false);
                }
            }, 1500);
            */

//            long finish = System.currentTimeMillis();
//            long timeElapsed = finish - start;
//            PPApplication.logE("[IN_WORKER]  SearchCalendarEventsWorker.doWork", "--------------- END - timeElapsed="+timeElapsed);
            return Result.success();
        } catch (Exception e) {
            //Log.e("SearchCalendarEventsWorker.doWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            /*Handler _handler = new Handler(getApplicationContext().getMainLooper());
            Runnable r = new Runnable() {
                public void run() {
                    android.os.Process.killProcess(PPApplication.pid);
                }
            };
            _handler.postDelayed(r, 1000);*/
            return Result.failure();
        }
    }

    public void onStopped () {
//        PPApplication.logE("[IN_LISTENER] SearchCalendarEventsWorker.onStopped", "xxx");
    }

    private static void _scheduleWork(final boolean shortInterval) {
        try {
            if (PPApplication.getApplicationStarted(true, true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

                    if (!shortInterval) {
                        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SearchCalendarEventsWorker.class)
                                .setInitialDelay(24, TimeUnit.HOURS)
                                .addTag(SearchCalendarEventsWorker.WORK_TAG)
                                .build();

//                        //if (PPApplication.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(SearchCalendarEventsWorker.WORK_TAG);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                        } catch (Exception ignored) {
//                        }
//                        //}

//                        PPApplication.logE("[WORKER_CALL] SearchCalendarEventsWorker._scheduleWork", "(1)");
                        workManager.enqueueUniqueWork(SearchCalendarEventsWorker.WORK_TAG, ExistingWorkPolicy.REPLACE/*KEEP*/, workRequest);
                    } else {
                        //waitForFinish();
                        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SearchCalendarEventsWorker.class)
                                .addTag(SearchCalendarEventsWorker.WORK_TAG_SHORT)
                                .build();

//                        //if (PPApplication.logEnabled()) {
//                        ListenableFuture<List<WorkInfo>> statuses;
//                        statuses = workManager.getWorkInfosForUniqueWork(SearchCalendarEventsWorker.WORK_TAG_SHORT);
//                        try {
//                            List<WorkInfo> workInfoList = statuses.get();
//                        } catch (Exception ignored) {
//                        }
//                        //}

//                        PPApplication.logE("[WORKER_CALL] SearchCalendarEventsWorker._scheduleWork", "(2)");
                        workManager.enqueueUniqueWork(SearchCalendarEventsWorker.WORK_TAG_SHORT, ExistingWorkPolicy.REPLACE/*KEEP*/, workRequest);
                    }

                }
            }
        } catch (Exception e) {
            //Log.e("SearchCalendarEventsWorker._scheduleWork", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    // shortInterval = true is called only from PPService.scheduleSearchCalendarEventsWorker and TimeVjangedReceiver.doWork
    static void scheduleWork(final boolean shortInterval) {
        if (shortInterval) {
            _cancelWork(false);
            //PPApplication.sleep(5000);
            _scheduleWork(true);

            /*PPApplication.startHandlerThreadPPScanners();
            final Handler __handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            __handler.post(() -> {
//                PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThreadPPScanners", "START run - from=SearchCalendarEventsWorker.scheduleWork" + " shortInterval=true");
                _cancelWork();
                PPApplication.sleep(5000);
                _scheduleWork(true);
            });*/
        }
        else
            _scheduleWork(false);
    }

    private static void _cancelWork(final boolean useHandler) {
        if (isWorkScheduled(false) || isWorkScheduled(true)) {
            try {
                waitForFinish(false);
                waitForFinish(true);

                if (useHandler) {
                    PPApplication.cancelWork(WORK_TAG, false);
                    PPApplication.cancelWork(WORK_TAG_SHORT, false);
                } else {
                    PPApplication._cancelWork(WORK_TAG, false);
                    PPApplication._cancelWork(WORK_TAG_SHORT, false);
                }

            } catch (Exception e) {
                //Log.e("SearchCalendarEventsWorker._cancelWork", Log.getStackTraceString(e));
                PPApplication.recordException(e);
            }
        }
    }

    private static void waitForFinish(boolean shortWork) {
        if (!isWorkRunning(shortWork)) {
            return;
        }

        try {
            if (PPApplication.getApplicationStarted(true, true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {

                    long start = SystemClock.uptimeMillis();
                    do {

                        ListenableFuture<List<WorkInfo>> statuses;
                        if (shortWork)
                            statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG_SHORT);
                        else
                            statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                        boolean allFinished = true;
                        //noinspection TryWithIdenticalCatches
                        try {
                            List<WorkInfo> workInfoList = statuses.get();
                            for (WorkInfo workInfo : workInfoList) {
                                WorkInfo.State state = workInfo.getState();
                                if (state == WorkInfo.State.RUNNING) {
                                    allFinished = false;
                                    break;
                                }
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (allFinished) {
                            break;
                        }

                        GlobalUtils.sleep(200);
                    } while (SystemClock.uptimeMillis() - start < 10 * 1000);

                }
            }
        } catch (Exception e) {
            //Log.e("SearchCalendarEventsWorker.waitForFinish", Log.getStackTraceString(e));
            PPApplication.recordException(e);
        }
    }

    static void cancelWork(final boolean useHandler) {
        _cancelWork(useHandler);

        /*if (useHandler) {
            PPApplication.startHandlerThreadPPScanners();
            final Handler __handler = new Handler(PPApplication.handlerThreadPPScanners.getLooper());
            __handler.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThreadPPScanners", "START run - from=SearchCalendarEventsWorker.cancelWork");
                _cancelWork();
            });
        }
        else {
            _cancelWork();
        }*/
    }

    private static boolean isWorkRunning(boolean shortWork) {
        try {
            if (PPApplication.getApplicationStarted(true, true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    ListenableFuture<List<WorkInfo>> statuses;
                    if (shortWork)
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG_SHORT);
                    else
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                    //noinspection TryWithIdenticalCatches
                    try {
                        List<WorkInfo> workInfoList = statuses.get();
                        boolean running = false;
                        for (WorkInfo workInfo : workInfoList) {
                            WorkInfo.State state = workInfo.getState();
                            running = state == WorkInfo.State.RUNNING;
                            break;
                        }
                        return running;
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        return false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                else
                    return false;
            }
            else
                return false;
        } catch (Exception e) {
            //Log.e("SearchCalendarEventsWorker.isWorkRunning", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            return false;
        }
    }

    static boolean isWorkScheduled(boolean shortWork) {
        try {
            if (PPApplication.getApplicationStarted(true, true)) {
                WorkManager workManager = PPApplication.getWorkManagerInstance();
                if (workManager != null) {
                    ListenableFuture<List<WorkInfo>> statuses;
                    if (shortWork)
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG_SHORT);
                    else
                        statuses = workManager.getWorkInfosForUniqueWork(WORK_TAG);
                    //noinspection TryWithIdenticalCatches
                    try {
                        List<WorkInfo> workInfoList = statuses.get();
                        boolean running = false;
                        for (WorkInfo workInfo : workInfoList) {
                            WorkInfo.State state = workInfo.getState();
                            running = (state == WorkInfo.State.RUNNING) || (state == WorkInfo.State.ENQUEUED);
                            break;
                        }
                        return running;
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        return false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                else
                    return false;
            }
            else
                return false;
        } catch (Exception e) {
            //Log.e("SearchCalendarEventsWorker.isWorkScheduled", Log.getStackTraceString(e));
            PPApplication.recordException(e);
            return false;
        }
    }

}
