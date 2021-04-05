package com.app.ekottel.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import com.app.ekottel.utils.ChatConstants;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.Utils;

/**
 * This activity is used to receive update call.
 *
 * @author Ramesh U
 * @version 2017
 */
public class CallReceiver extends BroadcastReceiver {

    private String TAG1;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            LOG.info("CallReceiver","CallReceiver called");
            if (GlobalVariables.incallcount > 0) {
                Utils.startCall(context, intent, true);
            } else {
                Utils.startCall(context, intent, false);
            }
            Intent intentForChat = new Intent(ChatConstants.STOP_MEDIA_PLAYER);
            context.sendBroadcast(intentForChat);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}