package com.app.ekottel.utils;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.app.ekottel.foregroundservices.ForeGroundChatService;
import com.app.ekottel.foregroundservices.ForeGroundServiceApis;
import com.app.ekottel.model.ChatFtNotifiationModel;
import com.app.ekottel.receivers.RingtonePlayingService;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.Utils.CSExplicitEvents;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;

import java.util.ArrayList;


public class App extends com.ca.app.App {
    boolean ischateventregistrationdone = false;
    NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            LOG.info("App", "jibi APP onCreate");
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            registerChatEvents();

            PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
            boolean dontshowagain = pf.getPrefBoolean("smoothupgradefornotifications");
            if (!dontshowagain) {
                PreferenceProvider pff = new PreferenceProvider(getApplicationContext());
                pff.setPrefboolean("smoothupgradefornotifications", true);
                CSDataProvider.updateChatMessagebyFilter(CSDbFields.KEY_CHAT_FILEAUTOSENDORRECV, 0);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public void registerChatEvents() {
        try {

            if (!ischateventregistrationdone) {
                ischateventregistrationdone = true;
                LOG.info("App", "Registering registerChatEvents");

                IntentFilter filter = new IntentFilter();
                filter.addAction(CSEvents.CSCLIENT_NETWORKERROR);
                filter.addAction(CSExplicitEvents.CSChatReceiver);
                filter.addAction(CSEvents.CSCHAT_UPLOADFILEDONE);
                filter.addAction(CSEvents.CSCHAT_UPLOADFILEFAILED);
                filter.addAction(CSEvents.CSCHAT_DOWNLOADFILEDONE);
                filter.addAction(CSEvents.CSCHAT_DOWNLOADFILEFAILED);
                filter.addAction(CSEvents.CSCHAT_DOWNLOADPROGRESS);
                filter.addAction(CSEvents.CSCHAT_UPLOADPROGRESS);
                filter.addAction(CSEvents.CSCHAT_FT_TX_STATE_CHANGED);
                filter.addAction(CSEvents.CSCHAT_CHATDELETED);
                filter.addAction(CSEvents.CSCALL_CALLENDED);

                //ChatEventReceiver chatEventReceiverObj = new ChatEventReceiver();
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new ChatEventReceiver(), filter);
            } else {
                LOG.info("App", "RegisterChatEvents already registered");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public ChatFtNotifiationModel getNotificationData() {
        ChatFtNotifiationModel chatFtNotifiationModel = new ChatFtNotifiationModel();
        ArrayList<String> messageList = new ArrayList<>();
        try {

            Cursor cur = CSDataProvider.getChatCursorForPendingUPnDownloads();
            LOG.info("App", "upanddownloading pending files in chat count from app :" + cur.getCount());
            if (cur.getCount() > 0) {
                cur.moveToNext();
                String title = Constants.APP_NAME;
                String message = constructMessage(cur);

                chatFtNotifiationModel.setIsempty(false);
                chatFtNotifiationModel.setTitle(title);

                chatFtNotifiationModel.setDescription(message);

                messageList.add(message);
                while (cur.moveToNext()) {
                    message = constructMessage(cur);
                    messageList.add(message);
                    chatFtNotifiationModel.setDescription(message);
                }
                chatFtNotifiationModel.setMessageList(messageList);


            } else {
                chatFtNotifiationModel.setIsempty(true);
            }
            cur.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return chatFtNotifiationModel;
    }


    public class ChatEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                LOG.info("App", "APP class Yes Something receieved in ChatEventReceiver:" + intent.getAction().toString());

                if(intent.getAction().equals(CSEvents.CSCALL_CALLENDED)){
                    NotificationMethodHelper.removeCallNotification(context);
                    Intent ringtoneIntent = new Intent(context, RingtonePlayingService.class);
                    context.stopService(ringtoneIntent);

                }

                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR) || intent.getAction().equals(CSEvents.CSCHAT_UPLOADFILEFAILED) || intent.getAction().equals(CSEvents.CSCHAT_DOWNLOADFILEFAILED)) {
                    LOG.info("App", "NetworkError receieved");
                    new ForeGroundServiceApis().stopChatService(getApplicationContext());

                } else {
                    if (intent.getAction().equals(CSExplicitEvents.CSChatReceiver)) {
                        int chattype = intent.getIntExtra("chattype", 0);
                        if (chattype != 4 && chattype != 5 && chattype != 7 && chattype != 8) {
                            return;
                        }
                    }

                    ChatFtNotifiationModel chatFtNotifiationModel = getNotificationData();
                    if (chatFtNotifiationModel.getisEmpty()) {
                        LOG.info("App", "stopChatService");
                        new ForeGroundServiceApis().stopChatService(getApplicationContext());
                    } else {

                        if (CSClient.getLoginstatus()) {

                            if (intent.getAction().equals(CSEvents.CSCHAT_DOWNLOADPROGRESS)) {
                                String chatid = intent.getStringExtra("chatid");
                                // Update below code
                                /*if (!ChatAdvancedAdapter.filedownloadinitiatedchatids.contains(chatid)) {
                                    ChatAdvancedAdapter.filedownloadinitiatedchatids.add(chatid);
                                }*/
                            }


                            if (isMyServiceRunning(ForeGroundChatService.class)) {
                                if (!intent.getAction().equals(CSEvents.CSCHAT_UPLOADPROGRESS) && !intent.getAction().equals(CSEvents.CSCHAT_DOWNLOADPROGRESS)) {
                                    //update notification
                                    LOG.info("App", "update notification");
                                    Notification noti = new ForeGroundChatService().buildNotification(getApplicationContext(), chatFtNotifiationModel.getTitle(), chatFtNotifiationModel.getDescription(), chatFtNotifiationModel.getMessageList());
                                    if (noti != null && mNotificationManager != null) {
                                        mNotificationManager.notify(101, noti);
                                    }
                                }
                            } else {
                                LOG.info("App", "startchatservice");
                                new ForeGroundServiceApis().startChatService(getApplicationContext(), chatFtNotifiationModel.getTitle(), chatFtNotifiationModel.getDescription(), chatFtNotifiationModel.getMessageList());
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        try {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public String constructMessage(Cursor cur) {
        try {
            String message = "Sending video to name"; //"Downloading video from name"
            String direction = "";
            String msgtype = "";
            String fromto = "";

            String destinationname = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_NAME));
            int issender = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_IS_SENDER));
            int chattype = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MSG_TYPE));
            int ismultidevicemessage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE));

            if (issender == 0 || ismultidevicemessage == 1) {
                direction = "Downloading ";
                fromto = "from ";
            } else {
                direction = "Sending ";
                fromto = "to ";
            }

            if (chattype == 4) {
                msgtype = "Image ";
            } else if (chattype == 5) {
                msgtype = "Video ";
            } else if (chattype == 7) {
                msgtype = "Document ";
            } else if (chattype == 8) {
                msgtype = "Audio ";
            }

            message = direction + msgtype + fromto + destinationname;

            return message;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }
}