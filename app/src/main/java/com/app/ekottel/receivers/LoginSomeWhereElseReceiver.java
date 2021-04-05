package com.app.ekottel.receivers;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.app.ekottel.utils.GlobalVariables.LOG;

public class LoginSomeWhereElseReceiver extends BroadcastReceiver {

    private static final String TAG = "LoginSomeReceiver";
    Dialog mAutoLogoutDialog = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        LOG.info("Received LoginSomeWhereElseReceiver");

        Intent registrationIntent = new Intent();
        registrationIntent.setAction("com.app.ekottel.autoLogout");
        context.sendBroadcast(registrationIntent);

    }

}
