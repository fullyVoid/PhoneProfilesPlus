package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class RingerModeChangeReceiver extends BroadcastReceiver {

    public static volatile boolean internalChange = false;
    public static volatile boolean notUnlinkVolumes = false;

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] RingerModeChangeReceiver.onReceive", "xxx");

        if (!internalChange) {
            synchronized (PPApplication.notUnlinkVolumesMutex) {
                notUnlinkVolumes = true;
            }
            final AudioManager audioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            setRingerMode(context.getApplicationContext(), audioManager/*, "PringerModeChangeReceiver.onReceive"*/);
        }

        //setAlarmForDisableInternalChange(context);
    }

    private static int getRingerMode(Context context, AudioManager audioManager) {
        int ringerMode = audioManager.getRingerMode();
        // convert to profile ringerMode
        int pRingerMode = 0;
        int systemZenMode = ActivateProfileHelper.getSystemZenMode(context/*, -1*/);
        if (systemZenMode == ActivateProfileHelper.ZENMODE_ALL) {
            switch (ringerMode) {
                case AudioManager.RINGER_MODE_NORMAL:
                    //if (ActivateProfileHelper.vibrationIsOn(/*context, */audioManager, false))
                    //    pRingerMode = Profile.RINGERMODE_RING_AND_VIBRATE;
                    //else
                        pRingerMode = Profile.RINGERMODE_RING;
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    pRingerMode = Profile.RINGERMODE_VIBRATE;
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    pRingerMode = Profile.RINGERMODE_SILENT;
                    break;
            }
        }
        else
            pRingerMode = Profile.RINGERMODE_ZENMODE;

        return pRingerMode;
    }

    static void setRingerMode(Context context, AudioManager audioManager/*, String from*/) {
        int pRingerMode = getRingerMode(context, audioManager);
        if (pRingerMode != 0) {
            int systemZenMode = ActivateProfileHelper.getSystemZenMode(context);
            ActivateProfileHelper.saveZenMode(context, systemZenMode);
            if (systemZenMode == ActivateProfileHelper.ZENMODE_ALL)
                ActivateProfileHelper.saveRingerMode(context, pRingerMode);
            else
                ActivateProfileHelper.saveRingerMode(context, Profile.RINGERMODE_ZENMODE);
        }
    }

}
