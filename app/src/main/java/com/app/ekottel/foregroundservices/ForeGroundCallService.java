package com.app.ekottel.foregroundservices;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.app.ekottel.utils.NotificationMethodHelper;

import static com.app.ekottel.utils.GlobalVariables.LOG;

public class ForeGroundCallService extends Service {

    private final IBinder mBinder = new ForeGroundCallServiceBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            String callType=intent.getStringExtra("callType");

            Notification notification=NotificationMethodHelper.NotifyCallInBackGound(title, description, callType, getApplicationContext());

            startForeground(NotificationMethodHelper.callNotificationID, notification);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LOG.info("ForeGroundCallService","ForeGroundCallService onDestroy called");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ForeGroundCallServiceBinder extends Binder {
        ForeGroundCallService getService() {
            return ForeGroundCallService.this;
        }
    }

}