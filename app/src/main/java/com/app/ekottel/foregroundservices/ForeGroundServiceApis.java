package com.app.ekottel.foregroundservices;

import android.content.Context;
import android.content.Intent;

import com.app.ekottel.utils.NotificationMethodHelper;

import java.util.ArrayList;

import static com.app.ekottel.utils.GlobalVariables.LOG;

public class ForeGroundServiceApis {

    public void startCallService(Context context, String title, String description, String callType) {
        try {
            LOG.info("ForeGroundServiceApis", "startedForegroundCall Service");
            Intent i = new Intent(context, ForeGroundCallService.class);
            i.putExtra("title", title);
            i.putExtra("description", description);
            i.putExtra("callType", callType);
            context.startService(i);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void stopCallService(Context context) {
        try {
            Intent intent = new Intent(context, ForeGroundCallService.class);
            context.stopService(intent);

            NotificationMethodHelper.removeCallNotification(context);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void startChatService(Context context, String title, String description, ArrayList<String> messageList) {
        try {
            Intent i = new Intent(context, ForeGroundChatService.class);
            i.putExtra("title", title);
            i.putExtra("description", description);
            i.putStringArrayListExtra("messageList", messageList);
            context.startService(i);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void stopChatService(Context context) {
        try {
            Intent intent = new Intent(context, ForeGroundChatService.class);
            context.stopService(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}


