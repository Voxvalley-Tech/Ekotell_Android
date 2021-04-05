package com.app.ekottel.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import com.app.ekottel.utils.PreferenceProvider;

public class MissedCallNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PreferenceProvider preferenceProvider = new PreferenceProvider(context);
        LOG.info("Misses", "onReceive: missedCallReceievr called");
        String managecontactnumber = intent.getStringExtra("number");
        String managedirection = intent.getStringExtra("direction");
        String name = intent.getStringExtra("name");
        String id = intent.getStringExtra("id");
        preferenceProvider.setPrefString(managecontactnumber + "MissedData", "");
        LOG.info("Misses", "onReceive: " + managecontactnumber + managedirection + name + id);
        LOG.info("Misses", "onReceive: "+intent.getAction());
    }
}
