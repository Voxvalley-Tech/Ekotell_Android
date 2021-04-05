package com.app.ekottel.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.app.ekottel.activity.PSTNCallActivity;
import com.app.ekottel.activity.PlayAudioCallActivity;
import com.app.ekottel.activity.PlayVideoCallActivity;
import com.app.ekottel.utils.App;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.NotificationMethodHelper;
import com.ca.Utils.CSConstants;
import com.ca.wrapper.CSCall;

import static com.app.ekottel.utils.GlobalVariables.LOG;

public class InComingCallHandlingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

    String action = intent.getAction();
    Intent stopIntent = new Intent(context, RingtonePlayingService.class);
        context.stopService(stopIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && App.getActivityStackCount() == 0) {

        String secondcall = intent.getStringExtra("secondcall");
        Boolean isinitiatior = intent.getBooleanExtra("isinitiatior", false);
        String sDstMobNu = intent.getStringExtra("sDstMobNu");
        String callactive = intent.getStringExtra("callactive");
        String callType = intent.getStringExtra("callType");
        String srcnumber = intent.getStringExtra("srcnumber");
        String callid = intent.getStringExtra("callid");
        String callstarttime = intent.getStringExtra("callstarttime");
        String name = intent.getStringExtra("name");


        Log.i("call Type Data", "call Type Data" + secondcall + "----" + isinitiatior + "--" + "--" + sDstMobNu + "--" + callactive + "---" + callType + "--" + srcnumber + "--" + callid + "--" + callstarttime);
        Log.i("IncomingNotification", "getting action" + action);

        if (action.equals("EndCall")) {
            Intent endIntent = new Intent("NotificationHandleReceiver");
            endIntent.putExtra("operationToPerform",action);
            context.sendBroadcast(endIntent);

            endCall(srcnumber, callid, callType,name, context);
            //context.startActivity(new Intent(context, CallScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else if (action.equals("AnswerCall")) {
            Intent intent1 = new Intent(context.getApplicationContext(), PlayAudioCallActivity.class);
            if (callType.equals("video")) {
                intent1 = new Intent(context.getApplicationContext(), PlayVideoCallActivity.class);
            } else if (callType.equals("audio")) {
                intent1 = new Intent(context.getApplicationContext(), PlayAudioCallActivity.class);
            } else if (callType.equals("pstn")) {
                intent1 = new Intent(context.getApplicationContext(), PSTNCallActivity.class);
            }
            // Intent intent1 = new Intent(context.getApplicationContext(), CallScreenActivity.class);

            if (App.getActivityStackCount() > 0) {
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent1.putExtra("secondcall", secondcall);
            intent1.putExtra("isinitiatior", false);
            intent1.putExtra("sDstMobNu", sDstMobNu);
            intent1.putExtra("callactive", callactive);
            intent1.putExtra("callType", callType);
            intent1.putExtra("srcnumber", srcnumber);
            intent1.putExtra("callid", callid);
            intent1.putExtra("callstarttime", callstarttime);
            context.startActivity(intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }


    } else {
        if (action.equals("EndCall")) {
            Intent endIntent = new Intent("NotificationHandleReceiver");
            endIntent.putExtra("operationToPerform", action);
            context.sendBroadcast(endIntent);
        } else if (action.equals("AnswerCall")) {
            Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(closeIntent);
            Intent endIntent = new Intent("NotificationHandleReceiver");
            endIntent.putExtra("operationToPerform", action);
            context.sendBroadcast(endIntent);

        }

/*
            String secondcall = intent.getStringExtra("secondcall");
            Boolean isinitiatior = intent.getBooleanExtra("isinitiatior", false);
            String sDstMobNu = intent.getStringExtra("sDstMobNu");
            String callactive = intent.getStringExtra("callactive");
            String callType = intent.getStringExtra("callType");
            String srcnumber = intent.getStringExtra("srcnumber");
            String callid = intent.getStringExtra("callid");
            String callstarttime = intent.getStringExtra("callstarttime");


            Log.i("call Type Data", "call Type Data" + secondcall + "----" + isinitiatior + "--" + "--" + sDstMobNu + "--" + callactive + "---" + callType + "--" + srcnumber + "--" + callid + "--" + callstarttime);
            Log.i("IncomingNotification", "getting action" + action);

            if (action.equals("EndCall")) {
            */
/*Intent endIntent = new Intent("NotificationHandleReceiver");
            endIntent.putExtra("operationToPerform",action);
            context.sendBroadcast(endIntent);*//*


                endCall(srcnumber, callid, callType, context);
                //context.startActivity(new Intent(context, CallScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } else if (action.equals("AnswerCall")) {
                Intent intent1 = new Intent(context.getApplicationContext(), PlayAudioCallActivity.class);
                if (callType.equals("video")) {
                    intent1 = new Intent(context.getApplicationContext(), PlayVideoCallActivity.class);
                } else if (callType.equals("audio")) {
                    intent1 = new Intent(context.getApplicationContext(), PlayAudioCallActivity.class);
                } else if (callType.equals("pstn")) {
                    intent1 = new Intent(context.getApplicationContext(), PSTNCallActivity.class);
                }
                // Intent intent1 = new Intent(context.getApplicationContext(), CallScreenActivity.class);

                if (App.getActivityStackCount() > 0) {
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                } else {
                    intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                }


                intent1.putExtra("secondcall", secondcall);
                intent1.putExtra("isinitiatior", false);
                intent1.putExtra("sDstMobNu", sDstMobNu);
                intent1.putExtra("callactive", callactive);
                intent1.putExtra("callType", callType);
                intent1.putExtra("srcnumber", srcnumber);
                intent1.putExtra("callid", callid);
                intent1.putExtra("callstarttime", callstarttime);
                context.startActivity(intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

           */
/* Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(closeIntent);
            Intent endIntent = new Intent("NotificationHandleReceiver");
            endIntent.putExtra("operationToPerform",action);
            context.sendBroadcast(endIntent);*//*

            }
*/
    }
}
    public void endCall (String srcnumber, String callid, String callType, String name,Context context){

        String calldirection= "";
        LOG.info("Utils", "run: Start call retunring " + calldirection);
        CSCall CSCallsObj = new CSCall();
        if (callType.equals("video")) {
            //LOG.info("Test here call id:"+callid);
            calldirection = "MISSED VIDEO CALL";
            CSCallsObj.endVoiceCall(srcnumber, callid, CSConstants.E_UserBusy, CSConstants.UserBusy);
            //CSPaymentsObj.directAudioVideoCallEndReq(srcnumber,2, callid,CSClientObj.getTime());
        } else {
            calldirection = "MISSED AUDIO CALL";
            CSCallsObj.endVoiceCall(srcnumber, callid, CSConstants.E_UserBusy, CSConstants.UserBusy);
            //CSPaymentsObj.directAudioVideoCallEndReq(srcnumber, 1, callid,CSClientObj.getTime());
        }
        try {

            if (callType.equals("video")) {
//                Utils.NotifyAppInMissedCall(context.getString(R.string.missed_video_call_message), srcnumber.trim(), "", "", 0, context);
                NotificationMethodHelper.NotifyAppInMissedCall(context, srcnumber.trim(), name,callid, CSConstants.MISSED_VIDEO_CALL, "", 0);

                CSCallsObj.endVideoCall(srcnumber, callid, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
            } else if (callType.equals("audio")) {
//                Utils.NotifyAppInMissedCall(context.getString(R.string.missed_audio_call_message), srcnumber, "", "", 0, context);
                NotificationMethodHelper.NotifyAppInMissedCall(context, srcnumber.trim(), name,callid, CSConstants.MISSED_AUDIO_CALL, "", 0);

                CSCallsObj.endVoiceCall(srcnumber, callid, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
            } else if (callType.equals("pstn")) {
//                Utils.NotifyAppInMissedCall(context.getString(R.string.missed_audio_call_message), srcnumber, "", "", 0, context);
                NotificationMethodHelper.NotifyAppInMissedCall(context, srcnumber.trim(), name,callid, CSConstants.MISSED_AUDIO_CALL, "", 0);
                CSCallsObj.endPstnCall(srcnumber, callid);
            }


            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (GlobalVariables.notification_id_list != null && GlobalVariables.notification_id_list.size() > 0) {
                for (int i = 0; i < GlobalVariables.notification_id_list.size(); i++) {
                    if (notificationManager != null)
                        notificationManager.cancel(GlobalVariables.notification_id_list.get(i));
                }
            }
            notificationManager.cancel(1001);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
