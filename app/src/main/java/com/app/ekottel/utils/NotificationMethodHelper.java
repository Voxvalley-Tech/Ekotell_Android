package com.app.ekottel.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import static com.app.ekottel.utils.GlobalVariables.LOG;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.app.ekottel.R;
import com.app.ekottel.activity.ChatAdvancedActivity;
import com.app.ekottel.activity.EmptyActivity;
import com.app.ekottel.activity.PSTNCallActivity;
import com.app.ekottel.activity.PlayAudioCallActivity;
import com.app.ekottel.activity.PlayVideoCallActivity;
import com.app.ekottel.activity.ShowCallLogHistory;
import com.app.ekottel.activity.SplashScreenActivity;
import com.app.ekottel.activity.ViewMyPromotionsActivity;
import com.app.ekottel.receivers.InComingCallHandlingReceiver;
import com.app.ekottel.receivers.MissedCallNotificationReceiver;
import com.ca.Utils.CSConstants;
import com.ca.app.App;

import java.util.Random;

public class NotificationMethodHelper {

    private static String TAG = "NotificationMethodHelper";
    private static String CHANNEL_ID = "Tringy_Channel";
    public static int callNotificationID = 1001;
    private static NotificationManager notificationManager;
    private static String CHANNEL_ID_PROMOTIONS = "minidialer_channel_promotions";
    private static final int PROMOTION_NOTIFICATION_ID = 777;

    /**
     * This is used for display notification when app is in background using call
     *
     * @param userdisplaystring display message
     * @param channelname       display name
     * @param channelrole       empty
     * @param context           current object
     * @return notification value
     */
    public static Notification NotifyCallInBackGound(String userdisplaystring, String channelname, String channelrole, Context context) {
        Random rand = new Random();
        //callNotificationID = rand.nextInt(1000000);
        Notification notification = null;


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        Intent intent = null;
        if (channelrole.equalsIgnoreCase("AUDIO")) {
            intent = new Intent(context, PlayAudioCallActivity.class);
        } else if (channelrole.equalsIgnoreCase("VIDEO")) {
            intent = new Intent(context, PlayVideoCallActivity.class);
        } else if (channelrole.equalsIgnoreCase("PSTN")) {
            intent = new Intent(context, PSTNCallActivity.class);
        } else {
            intent = new Intent(context, EmptyActivity.class);
        }

        intent.setAction(Long.toString(System.currentTimeMillis()));
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(context)
                    .setContentTitle(userdisplaystring)
                    .setContentText(channelname)
                    .setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_notification_transaperent)
                    .setColor(context.getResources().getColor(R.color.colorPrimaryDark))
                    .setChannelId(CHANNEL_ID)
                    .setContentIntent(pIntent)
                    .build();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification = new Notification.Builder(context)
                    .setContentTitle(userdisplaystring)
                    .setContentText(channelname)
                    .setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_notification_transaperent)
                    .setColor(context.getResources().getColor(R.color.colorPrimaryDark))
                    .setContentIntent(pIntent).build();
        } else {

            notification = new Notification.Builder(context)
                    .setContentTitle(userdisplaystring)
                    .setContentText(channelname)
                    .setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pIntent).build();
            // hide the notification after its selected
        }
        notification.flags |= Notification.FLAG_NO_CLEAR;

        notificationManager.notify(callNotificationID, notification);


        return notification;
    }
    public static void showPromotionalNotification(Context context, String title, String description) {

        try {
            if (notificationManager == null) {
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            Log.i("title", "title===" + title+"---"+description);
            Log.i("notificationManager", "notificationManager=" + notificationManager.toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = context.getString(R.string.app_name);// The user-visible name of the channel.
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID_PROMOTIONS, name, importance);
                mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(mChannel);
            }
            PreferenceProvider pf = new PreferenceProvider(context);
            /*boolean isHomePage = false;
            if (pf != null) {
                isHomePage = pf.getBooleanValue("RegisterHome");
            }
            Intent intent = null;
            if (isHomePage) {
                intent = new Intent(context, NotificationsActivity.class);
            } else {
                intent = new Intent(context, SplashActivity.class);
            }*/
            Intent intent = null;
            //if 0 need to launch splash other noti

            Log.i("activityStackcount", "" + com.ca.app.App.getActivityStackCount());
            if (App.getActivityStackCount() > 0) {
                intent = new Intent(context, ViewMyPromotionsActivity.class);
            } else {
                intent = new Intent(context, SplashScreenActivity.class);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("IsPromotionalNotification", "promotions");
            intent.setAction(Long.toString(System.currentTimeMillis()));
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification notification = null;
            // Notification not working in API level 27 so if API level more than 26 this will work
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notification = new Notification.Builder(context)
                        .setContentTitle(title)
                        .setContentText(description)
                        .setSmallIcon(R.drawable.ic_notification_transaperent)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                        .setChannelId(CHANNEL_ID_PROMOTIONS)
                        .setContentIntent(pIntent)
                        .build();

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notification = new Notification.Builder(context)
                        .setContentTitle(title)
                        .setContentText(description)
                        .setSmallIcon(R.drawable.ic_notification_transaperent)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                        .setContentIntent(pIntent).build();
            } else {
                notification = new Notification.Builder(context)
                        .setContentTitle(title)
                        .setContentText(description)
                        .setSound(uri)
                        .setSmallIcon(R.drawable.ic_notification_transaperent)
                        .setContentIntent(pIntent).build();
            }

            // hide the notification after its selected
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(PROMOTION_NOTIFICATION_ID, notification);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // return PROMOTION_NOTIFICATION_ID;
    }
    /**
     * This is used for remove call notification
     *
     * @param context current object
     */

    public static void removeCallNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(callNotificationID);
    }

    /**
     * This is used for display promotional notifications
     *
     * @param context           current object
     * @param userdisplaystring message
     * @param channelname       display name
     * @param channelid         message id
     * @param reqcode           empty
     * @return which returns value for notification
     */
    public static int NotifyUserPromotional(Context context, String userdisplaystring, String channelname, String channelid, int reqcode) {
        Random rand = new Random();
        int notificationid = rand.nextInt(1000001);
        try {
            LOG.info("I am in NotifyUserPromotional" + channelid);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = context.getString(R.string.app_name);// The user-visible name of the channel.
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                notificationManager.createNotificationChannel(mChannel);
            }
            PreferenceProvider pf = new PreferenceProvider(context);
            boolean isHomePage = false;
            if (pf != null) {
                isHomePage = pf.getPrefBoolean("RegisterHome");
            }
            Intent intent = null;
            if (isHomePage) {
                intent = new Intent(context, ViewMyPromotionsActivity.class);
            } else {
                intent = new Intent(context, SplashScreenActivity.class);
            }
            GlobalVariables.notification_id_list.add(notificationid);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("promotionalmessageid", channelid);
            intent.putExtra("PromotionalNotification", "PromotionalNotification");
            intent.setAction(Long.toString(System.currentTimeMillis()));
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification notification = null;
            // Notification not working in API level 27 so if API level more than 26 this will work
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notification = new Notification.Builder(context)
                        .setContentTitle(userdisplaystring)
                        .setContentText(channelname)
                        .setSmallIcon(R.drawable.ic_notification_transaperent)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                        .setChannelId(CHANNEL_ID)
                        .setContentIntent(pIntent)
                        .build();

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notification = new Notification.Builder(context)
                        .setContentTitle(userdisplaystring)
                        .setContentText(channelname)
                        .setSmallIcon(R.drawable.ic_notification_transaperent)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                        .setContentIntent(pIntent).build();
            } else {
                notification = new Notification.Builder(context)
                        .setContentTitle(userdisplaystring)
                        .setContentText(channelname)
                        .setSound(uri)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentIntent(pIntent).build();
            }

            // hide the notification after its selected
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(notificationid, notification);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return notificationid;
    }

    public static void cancelNotificationList(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (GlobalVariables.notification_id_list != null && GlobalVariables.notification_id_list.size() > 0) {
            for (int i = 0; i < GlobalVariables.notification_id_list.size(); i++) {
                if (notificationManager != null)
                    notificationManager.cancel(GlobalVariables.notification_id_list.get(i));
            }
        }

    }

    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "Default Channel";
            CharSequence name = context.getString(R.string.app_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(mChannel);
        }
    }

    public static int notifyIncomigCall(Context context, String conatctName, String callType, boolean secondcall) {
        int notificationId = 5;
        createNotificationChannel(context);
        Intent intent = new Intent(context, EmptyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Long.toString(System.currentTimeMillis()));


        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentAction = new Intent(context, InComingCallHandlingReceiver.class);
        intentAction.setAction("EndCall");
        PendingIntent endCallIntent = PendingIntent.getBroadcast(context, 1, intentAction, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent intentAction1 = new Intent(context, InComingCallHandlingReceiver.class);
        intentAction1.setAction("AnswerCall");
        PendingIntent answerCallIntent = PendingIntent.getBroadcast(context, 1, intentAction1, PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder noti = new NotificationCompat.Builder(context, "Default")
                .setContentTitle(conatctName)
                .setContentText(callType)
                .addAction(0, "Decline", endCallIntent)
                .addAction(0, "Answer", answerCallIntent)
                .setOngoing(true)
                .setContentIntent(pIntent)
                .setAutoCancel(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            noti.setSmallIcon(R.drawable.ic_notification_transaperent);
            noti.setColor(context.getResources().getColor(R.color.colorPrimaryDark));
        } else {
            noti.setSmallIcon(R.drawable.ic_launcher);
        }
        if (secondcall) {
            NotificationCompat.InboxStyle inboxStyle =
                    new NotificationCompat.InboxStyle();
            inboxStyle.addLine("On Clicking Answer any existing ");
            inboxStyle.addLine("call/s will be disconnected.");
            noti.setStyle(inboxStyle);
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, noti.build());
        return notificationId;
    }

    public void getCallSensitiveNotofication(String srcNumber, String callid, String calltype, Intent intent, Context context) {
        Log.e("getVideoCall", "getVideoCallSensitiveNotofication-->" + context);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence channelName = context.getString(R.string.app_name);// The user-visible name of the channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);

        }


        String title = "";
        if (calltype.equals("video")) {
            title = context.getString(R.string.missed_video_call_message);

        } else if (calltype.equals("audio")) {
            title = context.getString(R.string.missed_audio_call_message);

        } else {
            title = context.getString(R.string.missed_audio_call_message);

        }


        Intent intent1 = new Intent(context, EmptyActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.setAction(Long.toString(System.currentTimeMillis()));

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);



        PendingIntent endCallIntent = PendingIntent.getBroadcast(context, 1,  getActionIntent(context,"EndCall",intent), PendingIntent.FLAG_CANCEL_CURRENT);

        PendingIntent answerCallIntent = PendingIntent.getBroadcast(context, 1,  getActionIntent(context,"AnswerCall",intent), PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setChannelId(CHANNEL_ID)
                        .setContentTitle(srcNumber)
                        .setContentText(channelName)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(0)
                        .setAutoCancel(false)
                        .addAction(0, "Decline", endCallIntent)
                        .addAction(0, "Answer", answerCallIntent)//getPendingIntent(Constants.ACCEPT_CALL,callId,callType,number)) //getPendingIntent(Constants.ACCEPT_CALL,callId,callType))
                        // .setDeleteIntent(getPendingIntentForVideoCall(Constants.CANCEL_NOTIFICATION, model))
                        .setFullScreenIntent(fullScreenPendingIntent, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setSmallIcon(R.drawable.ic_notification_transaperent);
            notificationBuilder.setColor(context.getResources().getColor(R.color.colorPrimaryDark));
            notificationBuilder.setChannelId(CHANNEL_ID);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.drawable.ic_notification_transaperent);
            notificationBuilder.setColor(context.getResources().getColor(R.color.colorPrimaryDark));
        } else {
            notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
        }
        Notification incomingCallNotification = notificationBuilder.build();
        notificationManager.notify(callNotificationID, incomingCallNotification);
    }



    private static Intent getActionIntent(Context context,String action,Intent intent){
        Intent intentAction = new Intent(context, InComingCallHandlingReceiver.class);
        intentAction.setAction(action);
        Log.e("intent","intent-->"+intent.getStringExtra("secondcall"));
        intentAction.putExtra("secondcall",intent.getStringExtra("secondcall"));
        intentAction.putExtra("isinitiatior",intent.getBooleanExtra("srcnumber",false));
        intentAction.putExtra("sDstMobNu",intent.getStringExtra("sDstMobNu"));
        intentAction.putExtra("callactive",intent.getStringExtra("callactive"));
        intentAction.putExtra("callType",intent.getStringExtra("callType"));
        intentAction.putExtra("srcnumber",intent.getStringExtra("srcnumber"));
        intentAction.putExtra("callid",intent.getStringExtra("callid"));
        intentAction.putExtra("callstarttime",intent.getStringExtra("callstarttime"));
        intentAction.putExtra("name",intent.getStringExtra("name"));

        return intentAction;
    }
    public static int NotifyAppInMissedCall(Context context, String number, String name, String callid, String calldirection, String calltime, int reqcode) {
        Random rand = new Random();
        PreferenceProvider preferenceProvider = new PreferenceProvider(context);

        int notificationid = 0;
        try {
            {
                notificationid = preferenceProvider.getPrefInt(number + "Missed");
                if (notificationid == 0) {
                    notificationid = rand.nextInt(1000001);
                    preferenceProvider.setPrefint(number + "Missed", notificationid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //  LOG.info("notifyCallMissed: " + isNotificationVisible(context, notificationid));
        try {
            createNotificationChannel(context);
            LOG.info("I am in notifyCallMissed" + name + " direction " + calldirection);

            Intent intent = new Intent(context, MissedCallNotificationReceiver.class);
            intent.putExtra("number", number);
            intent.putExtra("name", name);
            intent.putExtra("id", callid);
            intent.putExtra("direction", calldirection);
            Intent intent1 = new Intent(context, ShowCallLogHistory.class);
            intent1.putExtra("number", number);
            intent1.putExtra("name", name);
            intent1.putExtra("id", callid);
            intent1.putExtra("direction", calldirection);
            LOG.info("I am in userdisplaystring name" + name);
            String userdisplaystring = "";
            if (name.equals("")) {
                LOG.info("yes name is null" + number);
                userdisplaystring = number;
            } else {
                userdisplaystring = name;
            }
            LOG.info("I am in userdisplaystring" + userdisplaystring);

            String mycalldirection = "";
            if (calldirection.equals(CSConstants.MISSED_VIDEO_CALL)) {
                mycalldirection = "missed video call";
            } else if (calldirection.equals(CSConstants.MISSED_PSTN_CALL)) {
                mycalldirection = "missed pstn call";
            } else {
                mycalldirection = "missed audio call";
            }
            String description = mycalldirection;

            String notificationMessage = "";
            {
                notificationMessage = preferenceProvider.getPrefString(number + "MissedData");
                if (notificationMessage.equals("")) {
                    notificationMessage = mycalldirection;
                } else {
                    notificationMessage = notificationMessage + "|" + mycalldirection;
                }
                preferenceProvider.setPrefString(number + "MissedData", notificationMessage);
            }
            String[] notificationMessages = notificationMessage.split("\\|");


            PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pIntent1 = PendingIntent.getActivity(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            //Notification noti = new Notification.Builder(context)
            NotificationCompat.Builder noti = new NotificationCompat.Builder(context, "Default")

                    .setContentTitle(userdisplaystring)
                    .setContentText(description)
                    .setSound(uri)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pIntent1)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setDeleteIntent(pIntent);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // hide the notification after its selected
            //noti.flags |= Notification.FLAG_AUTO_CANCEL;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                noti.setSmallIcon(R.drawable.ic_notification_transaperent);
                noti.setColor(context.getResources().getColor(R.color.colorPrimaryDark));
                noti.setChannelId(CHANNEL_ID);

            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                noti.setSmallIcon(R.drawable.ic_notification_transaperent);
                noti.setColor(context.getResources().getColor(R.color.colorPrimaryDark));
            } else {
                noti.setSmallIcon(R.drawable.ic_launcher);
            }

            NotificationCompat.InboxStyle inboxStyle =
                    new NotificationCompat.InboxStyle();
            for (int i = notificationMessages.length - 1; i >= 0; i--) {
                inboxStyle.addLine(notificationMessages[i]);
            }
            noti.setStyle(inboxStyle);
            noti.setNumber(notificationMessages.length);

            noti.setStyle(inboxStyle);

            notificationManager.notify(notificationid, noti.build());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return notificationid;
    }

    public static int NotifyUserChat(Context context, String userdisplaystring, String channelname, String channelid, int isgroupmessage, String name) {
        LOG.info("util", "notifyUserChat: " + channelid + " " + isgroupmessage + " " + name);
        Random rand = new Random();
        PreferenceProvider preferenceProvider = new PreferenceProvider(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name1 = context.getString(R.string.app_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name1, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        int notificationid = 0;
        try {
            if (isgroupmessage == 0) {
                notificationid = Integer.parseInt(channelid.substring((channelid.length() / 2), channelid.length()).replace("+", ""));

            } else {
                notificationid = preferenceProvider.getPrefInt(name);
                if (notificationid == 0) {
                    notificationid = rand.nextInt(1000001);
                    preferenceProvider.setPrefint(name, notificationid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {

            String notificationMessage = "";
            if (isgroupmessage == 0) {
                notificationMessage = preferenceProvider.getPrefString(channelid + "ChatMessage");
                if (notificationMessage.equals("")) {
                    notificationMessage = channelname;
                } else {
                    notificationMessage = notificationMessage + "|" + channelname;
                }
                preferenceProvider.setPrefString(channelid + "ChatMessage", notificationMessage);
            } else {
                notificationMessage = preferenceProvider.getPrefString(channelid + "ChatMessage");
                if (notificationMessage.equals("")) {
                    notificationMessage = channelname;
                } else {
                    notificationMessage = notificationMessage + "|" + channelname;
                }
                preferenceProvider.setPrefString(channelid + "ChatMessage", notificationMessage);
            }
            String[] notificationMessages = notificationMessage.split("\\|");

            LOG.info("I am in NotifyChat" + channelid);
            boolean isHomePage = false;
            if (preferenceProvider != null) {
                isHomePage = preferenceProvider.getPrefBoolean("RegisterHome");
            }
            Intent intent;
            if (isHomePage) {
                intent = new Intent(context, ChatAdvancedActivity.class);
            } else {
                intent = new Intent(context, SplashScreenActivity.class);
            }

            intent.putExtra("Notification", "Notification");
            intent.putExtra("Sender", channelid);
            intent.putExtra("IS_GROUP", false);

            intent.putExtra(Constants.INTENT_CHAT_CONTACT_NUMBER, channelid);
            intent.putExtra(Constants.INTENT_CHAT_CONTACT_NAME, name);
            intent.putExtra(Constants.IS_VIDEO_CALL_RUNNING, false);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Long.toString(System.currentTimeMillis()));


            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder noti = new NotificationCompat.Builder(context, "Default")
                    //Notification noti = new Notification.Builder(context)
                    .setContentTitle(userdisplaystring)
                    .setContentText(channelname)
                    .setSound(uri)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(pIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                noti.setSmallIcon(R.drawable.ic_notification_transaperent);
                noti.setColor(context.getResources().getColor(R.color.colorPrimaryDark));
                noti.setChannelId(CHANNEL_ID);
                noti.setContentText(channelname);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                noti.setSmallIcon(R.drawable.ic_notification_transaperent);
                noti.setColor(context.getResources().getColor(R.color.colorPrimaryDark));
                //noti.setPriority(Notification.PRIORITY_MAX);

            } else {
                noti.setSmallIcon(R.drawable.ic_launcher);
            }

            //  NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.InboxStyle inboxStyle =
                    new NotificationCompat.InboxStyle();
            for (int i = notificationMessages.length - 1; i >= 0; i--) {
                inboxStyle.addLine(notificationMessages[i]);
            }
            noti.setStyle(inboxStyle);
            noti.setNumber(notificationMessages.length);
            // hide the notification after its selected
            //noti.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(notificationid, noti.build());

        } catch (
                Exception ex) {
            ex.printStackTrace();
        }
        return notificationid;
    }


}
