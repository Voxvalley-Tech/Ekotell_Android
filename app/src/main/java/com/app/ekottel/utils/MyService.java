package com.app.ekottel.utils;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import com.ca.Utils.CSConstants;
import com.ca.wrapper.CSCall;

/**
 * This activity is used to background running task.
 *
 * @author Ramesh U
 * @version 2017
 */
public class MyService extends Service {
    @Override
    public void onTaskRemoved(Intent rootIntent) {

        try {
            NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.cancelAll();

            PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
            pf.setPrefboolean("clearBackground", true);
            pf.setPrefboolean("CallRunning", false);
            LOG.info("MyService", "Incall Status " + pf.getPrefBoolean(PreferenceProvider.IS_CALL_RUNNING));
            if (pf.getPrefBoolean(PreferenceProvider.IS_CALL_RUNNING)) {
                CSCall csCall = new CSCall();
                pf.setPrefboolean(PreferenceProvider.IS_CALL_RUNNING, false);
                String numberToEnd = pf.getPrefString(PreferenceProvider.IN_CALL_NUMBER);
                String callerID = pf.getPrefString(PreferenceProvider.IN_CALL_CALLER_ID);
                String callType = pf.getPrefString(PreferenceProvider.RUNNING_CALL_TYPE);
                if (callType.equals("PSTN")) {
                    csCall.endPstnCall(numberToEnd, callerID);
                } else if (callType.equals("Audio")) {
                    csCall.endVideoCall(numberToEnd, callerID, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
                } else if (callType.equals("Video")) {
                    csCall.endVideoCall(numberToEnd, callerID, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
