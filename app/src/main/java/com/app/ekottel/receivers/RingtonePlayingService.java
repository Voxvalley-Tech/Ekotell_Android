package com.app.ekottel.receivers;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

public class RingtonePlayingService extends Service
{
    private Ringtone ringtone;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //activating alarm sound
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(getBaseContext(), notification);
        //playing sound alarm
        ringtone.play();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if(ringtone != null)
           ringtone.stop();
    }
}