package com.app.ekottel.receivers;


import static com.app.ekottel.utils.GlobalVariables.LOG;
import static com.app.ekottel.utils.NotificationMethodHelper.showPromotionalNotification;

import android.text.TextUtils;
import android.util.Log;

import com.app.ekottel.utils.Utils;
import com.ca.custom.CSCustomApis;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FireBaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        try {
            LOG.debug("onMessageReceived: called and remoteMessage: "+remoteMessage);
            if (CSDataProvider.getSignUpstatus()) {
                CSClient csClient = new CSClient();
                csClient.processPushMessage(getApplicationContext(), remoteMessage);

                Log.i("data", "getNotification==" + remoteMessage.getData());
                String title = remoteMessage.getData().get("title");
                //String title = remoteMessage.getNotification().getTitle();
                Log.i(TAG, "title: " + title);
                String message = remoteMessage.getData().get("body");
                // String body = remoteMessage.getNotification().getBody();
                Log.i(TAG, "message: " + message);
                //  Log.i(TAG,"message: "+body);
                Log.i(TAG, "getNotification==" + remoteMessage.getData());
                Log.i(TAG, "onMessageReceived: " + message);
                Log.i(TAG, "onMessageReceived:Remote " + remoteMessage);
                Log.i(TAG, "onMessageReceived:Remote " + remoteMessage);
                //this is for topic base data insert through sdk
                if (!TextUtils.isEmpty(message)) {
                    CSCustomApis cs = new CSCustomApis();
                    cs.insertpromotionalnotification("Ekottel", "ekottelpromotionsandroid", title, message, "1");
                    showPromotionalNotification(getApplicationContext(), title, message);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewToken(String newToken) {

        CSClient csClient = new CSClient();
        csClient.processInstanceID(getApplicationContext(), newToken);
        Utils.subscribeToPushTopic();
        super.onNewToken(newToken);
    }
}


