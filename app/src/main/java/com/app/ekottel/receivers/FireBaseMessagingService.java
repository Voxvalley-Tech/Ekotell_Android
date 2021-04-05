package com.app.ekottel.receivers;


import static com.app.ekottel.utils.GlobalVariables.LOG;

import com.ca.wrapper.CSClient;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FireBaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        try {
            LOG.debug("onMessageReceived: called and remoteMessage: "+remoteMessage);

            CSClient csClient = new CSClient();
            csClient.processPushMessage(getApplicationContext(), remoteMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNewToken(String newToken) {

        CSClient csClient = new CSClient();
        csClient.processInstanceID(getApplicationContext(),newToken);

        super.onNewToken(newToken);
    }
}


