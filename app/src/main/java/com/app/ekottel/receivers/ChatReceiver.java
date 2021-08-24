package com.app.ekottel.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Looper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import com.app.ekottel.R;
import com.app.ekottel.utils.MainActivity;
import com.app.ekottel.utils.NotificationMethodHelper;
import com.app.ekottel.utils.PreferenceProvider;
import com.ca.Utils.CSDbFields;
import com.ca.wrapper.CSDataProvider;

/**
 * This activity is used to receive update chat.
 *
 * @author Ramesh U
 * @version 2017
 */
public class ChatReceiver extends BroadcastReceiver {

    private String TAG1="ChatReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {
                    MainActivity.context = context.getApplicationContext();
                    LocalBroadcastManager.getInstance(MainActivity.context).sendBroadcast(intent);

                    LOG.info(TAG1,"Chat Receieved in CSChatReceiver");
                    //if(!CSDataProvider.getUINotificationsMuteStatus()) {

                    String chatid = intent.getStringExtra("chatid");
                    Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_ID, chatid);
                    String grpmessagesender = "";
                    String lastmessage = "";
                    String name = "";
                    int chattype = 0;
                    int issender = 0;
                    if (cur.getCount() > 0) {
                        cur.moveToNext();
                        lastmessage = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        chattype = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MSG_TYPE));
                        name = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_NAME));
                        issender = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_IS_SENDER));
                        grpmessagesender = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_GROUP_MESSAGE_SENDER));
                        LOG.info(TAG1,"grpmessagesender:" + grpmessagesender);
                    }
                    cur.close();

                    if (lastmessage.startsWith("I have transferred")) {
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(context.getString(R.string.balance_transfered_successful)));

                    }
                    if (issender == 0) {

                        int isgroupmessage = intent.getIntExtra("isgroupmessage", 0);
                        String destinationname = intent.getStringExtra("destinationname");

                        String finalmessage = "";


                        if (chattype == 0) {
                            finalmessage = lastmessage;
                        } else if (chattype == 1) {
                            finalmessage = lastmessage;
                        } else if (chattype == 2) {
                            finalmessage = lastmessage;
                        } else if (chattype == 3) {
                            finalmessage = "Location Received";
                        } else if (chattype == 4) {
                            finalmessage = "Image Received";
                        } else if (chattype == 5) {
                            finalmessage = "Video Received";
                        } else if (chattype == 6) {
                            finalmessage = "Contact Received";
                        } else if (chattype == 7) {
                            finalmessage = "Document Received";
                        } else if (chattype == 8) {
                            finalmessage = "Audio Received";
                        }

                        String destination = "";
                        if (isgroupmessage == 0) {
                            destination = intent.getStringExtra("destinationnumber");
                            if (destinationname.equals("")) {
                                destinationname = destination;
                            }

                        } else {
                            destination = intent.getStringExtra("groupid");

                            if (destinationname.equals("")) {
                                Cursor cor = CSDataProvider.getGroupsCursorByFilter(CSDbFields.KEY_GROUP_ID, destination);
                                //LOG.info(TAG1,"cor count:"+cor.getCount());
                                if (cor.getCount() > 0) {
                                    cor.moveToNext();
                                    destinationname = cor.getString(cor.getColumnIndexOrThrow(CSDbFields.KEY_GROUP_NAME));

                                }
                                cor.close();
                            }


                            Cursor cur1 = CSDataProvider.getContactCursorByNumber(grpmessagesender);
                            if (cur1.getCount() > 0) {
                                cur1.moveToNext();
                                grpmessagesender = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
                            }
                            cur1.close();

                            finalmessage = grpmessagesender + ":" + finalmessage;


                        }
                        PreferenceProvider pf = new PreferenceProvider(context);
                        pf.setPrefboolean("chatUpdated", true);
                        String activeDestination = pf.getPrefString("activeDestination");
                        LOG.info(TAG1, "Chat Receieved active destination" + activeDestination);
                        if (!destination.equals(activeDestination)) {
                          //  NotificationMethodHelper.NotifyUserChat(context, destinationname, finalmessage, destination, isgroupmessage, name);
                        }



                    }
                    //}
                } catch (Exception ex) {
                   ex.printStackTrace();
                }
            }
        }).start();
    }

    private int getCount() {
        Cursor cursor = CSDataProvider.getChatCursorGroupedByRemoteId();
        int badgeCount = 0;
        if (cursor.moveToNext()) {
            do {
                String destNumber = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_LOGINID));
                int isGroupMessage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_IS_GROUP_MESSAGE));
                Cursor ccr = null;
                if (isGroupMessage == 0) {
                    ccr = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(destNumber);
                } else {
                    ccr = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(destNumber);
                }
                if (ccr.getCount() > 0) {
                    badgeCount++;
                }
                ccr.close();
            } while (cursor.moveToNext());
        }
        return badgeCount;
    }

}