package com.app.ekottel.foregroundservices;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import static com.app.ekottel.utils.GlobalVariables.LOG;

import com.app.ekottel.R;
import com.app.ekottel.activity.EmptyActivity;
import com.ca.Utils.CSDbFields;
import com.ca.wrapper.CSDataProvider;

import java.util.ArrayList;


public class ForeGroundChatService extends Service {
    public static final String CHANNEL_ID = "Tringy_Channel";
    CharSequence name = "Ekottel";
    String description = "";

    int notificationid = 101;

    private final IBinder mBinder = new ForeGroundChatServiceBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

            createNotificationChannel(getApplicationContext());

            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            ArrayList<String> messageList = intent.getStringArrayListExtra("messageList");

            Notification noti = buildNotification(getApplicationContext(), title, description, messageList);
            startForeground(notificationid, noti);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LOG.info("ForeGroundChatService", "ForeGroundChatService onDestroy called");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ForeGroundChatServiceBinder extends Binder {
        ForeGroundChatService getService() {
            return ForeGroundChatService.this;
        }
    }

    ForeGroundChatService chatService = null;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            chatService = new ForeGroundChatServiceBinder().getService();
            // now you have the instance of service.

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            chatService = null;
        }
    };

    private void createNotificationChannel(Context context) {

        try {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public Notification buildNotification(Context context, String title, String description, ArrayList<String> messageList) {
        try {
            LOG.info("ForeGroundChatService", "build chat Notification");
            LOG.info("ForeGroundChatService", "build chat Notification title:" + title);
            LOG.info("ForeGroundChatService", "build chat Notification description:" + description);
            LOG.info("ForeGroundChatService", "build chat Notification messageList:" + messageList.size());
            createNotificationChannel(context);

            Cursor cur = CSDataProvider.getContactCursorByNumber(title);
            if (cur.getCount() > 0) {
                cur.moveToNext();
                title = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
            }
            cur.close();

            String[] notificationMessages = messageList.toArray(new String[messageList.size()]);

            Intent emptyActivityIntent = new Intent(context, EmptyActivity.class);
            emptyActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            emptyActivityIntent.putExtra("from", "chatnotification");
            emptyActivityIntent.setAction(Long.toString(System.currentTimeMillis()));

            PendingIntent pIntent = PendingIntent.getActivity(context, 0, emptyActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder noti = new NotificationCompat.Builder(context, CHANNEL_ID)
                    //Notification noti = new Notification.Builder(context)
                    .setContentTitle(title)
                    .setContentText(description)
                    //.setSound(uri)
                    //.setOnlyAlertOnce(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pIntent)
                    .setAutoCancel(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                noti.setSmallIcon(R.drawable.ic_notification_transaperent);
                noti.setColor(context.getResources().getColor(R.color.theme_color));
            } else {
                noti.setSmallIcon(R.mipmap.ic_launcher);
            }

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            for (int i = notificationMessages.length - 1; i >= 0; i--) {
                inboxStyle.addLine(notificationMessages[i]);
            }
            noti.setStyle(inboxStyle);
            noti.setNumber(notificationMessages.length);
            return noti.build();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}