package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocaleChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] LocaleChangedReceiver.onReceive", "xxx");

        if ((intent != null) && (intent.getAction() != null) && intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {

            //final Context appContext = context.getApplicationContext();

            if (PPApplication.getApplicationStarted(false, false)) {

                PPApplication.collator = GlobalUtils.getCollator();
                //if (ApplicationPreferences.applicationLanguage(appContext).equals("system")) {
                //PPApplication.showProfileNotification(/*true*/);
                //if (PhoneProfilesService.getInstance() != null)
//                PPApplication.logE("[PPP_NOTIFICATION] LocaleChangedReceiver.onReceive", "call of showProfileNotification");
                PhoneProfilesNotification.showProfileNotification(context.getApplicationContext(),
                            false, true, false);
                //}

/*
                PPApplication.startHandlerThreadBroadcast();
                final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=LocaleChangedReceiver.onReceive");

                        PPApplication.collator = PPApplication.getCollator();
                        //if (ApplicationPreferences.applicationLanguage(appContext).equals("system")) {
                            //PPApplication.showProfileNotification();
                        if (PhoneProfilesService.getInstance() != null)
                            PhoneProfilesService.getInstance().showProfileNotification(false);
                        //}

                    }
                });
*/
            }
        }
    }

}
