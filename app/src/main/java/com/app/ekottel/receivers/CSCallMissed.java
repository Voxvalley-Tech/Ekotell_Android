package com.app.ekottel.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.text.format.DateUtils;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import com.app.ekottel.utils.MainActivity;
import com.app.ekottel.utils.NotificationMethodHelper;
import com.app.ekottel.utils.Utils;
import com.ca.wrapper.CSClient;


import java.text.SimpleDateFormat;
import java.util.Date;


public class CSCallMissed extends BroadcastReceiver {
    CSClient CSClientObj = new CSClient();
    private String TAG = "CSCallMissed";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    MainActivity.context = context.getApplicationContext();
                    LOG.info("CSCallMissed Received:" + intent.getStringExtra("name"));
                    long time = intent.getLongExtra("time", new Date().getTime());
                    String calltime = "";
                    if (DateUtils.isToday(time)) {
                        calltime = "Today " + new SimpleDateFormat("hh:mm a").format(time);
                    } else if (Utils.isYesterday(time)) {
                        calltime = "Yesterday " + new SimpleDateFormat("hh:mm a").format(time);
                    } else {
                        calltime = new SimpleDateFormat("dd-MM-yyyy hh:mm a").format(time);
                    }
                    String calldirection = intent.getStringExtra("direction");

                    LOG.info("CSCallMissed Received name before broadcast:" + intent.getStringExtra("name"));
                    NotificationMethodHelper.NotifyAppInMissedCall(context, intent.getStringExtra("number"), intent.getStringExtra("name"), intent.getStringExtra("callid"), calldirection, calltime, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

}