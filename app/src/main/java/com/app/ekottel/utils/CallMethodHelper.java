package com.app.ekottel.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.ActivityCompat;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import android.util.Log;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.activity.HomeScreenActivity;
import com.app.ekottel.activity.PlayAudioCallActivity;
import com.app.ekottel.activity.PSTNCallActivity;
import com.app.ekottel.activity.PlayVideoCallActivity;
import com.app.ekottel.fragment.CallLogsFragment;

/**
 * This calls will handle the making of calls from any screens based on call types
 */
public class CallMethodHelper {

    private static String TAG = "CallMethodHelper";

    /**
     * This Method will process outgoing video call
     *
     * @param context
     * @param numberToDial
     */
    public static void placeVideoCall(Context context, String numberToDial) {
        try {
            LOG.info("number to call " + numberToDial);
            if (!Utils.getNetwork(context)) {
                Toast.makeText(context, context.getString(R.string.network_unavail_message), Toast.LENGTH_LONG).show();
                return;
            }
            if (!PermissionUtils.checkCameraPermission((Activity) context)) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, 101);
                return;
            }
            if (!Utils.getNetworkType(context)) {
                Toast.makeText(context, context.getString(R.string.network_type_message), Toast.LENGTH_LONG).show();
                return;
            }

            if (!HomeScreenActivity.status.equalsIgnoreCase(context.getString(R.string.registered))) {
                Toast.makeText(context, context.getString(R.string.registered_message), Toast.LENGTH_LONG).show();
                return;
            }

            PreferenceProvider pf = new PreferenceProvider(context);

            boolean isInCall = pf.getPrefBoolean(context.getString(R.string.call_logs_incall_message));
            Log.e("isInCall","isInCall--->"+isInCall);
            if (isInCall) {
                Toast.makeText(context, context.getString(R.string.call_running_error_message), Toast.LENGTH_LONG).show();
                return;
            }

            if (new PreferenceProvider(context).getPrefBoolean(context.getString(R.string.call_logs_pref_gsm_key))) {

                Toast.makeText(context,
                        context.getString(R.string.gsm_message),
                        Toast.LENGTH_SHORT).show();

                return;
            }


            if (!numberToDial.equals("") && !numberToDial.equals(GlobalVariables.phoneNumber)) {

                boolean callRunning = pf.getPrefBoolean("CallRunning");
                if (!callRunning) {
                    if (CallLogsFragment.callLogDetailsDialog != null) {
                        CallLogsFragment.callLogDetailsDialog.dismiss();
                    }
                    pf.setPrefboolean("CallRunning", true);
                                /*if (!GlobalVariables.ispeerconnectionclosed) {
                                    GlobalVariables.peerConnectionClient.close();
                                    GlobalVariables.ispeerconnectionclosed = true;
                                }*/
                    GlobalVariables.ispeerconnectionclosed = false;

                    Intent intent = new Intent(context, PlayVideoCallActivity.class);
                    intent.putExtra(context.getString(R.string.call_logs_intent_dntnum_key), numberToDial);
                    intent.putExtra(context.getString(R.string.call_logs_intent_stream_subscr_key), "");
                    intent.putExtra(context.getString(R.string.call_logs_intent_initiator_key), true);

                    context.startActivity(intent);
                }else{
                    Toast.makeText(context, context.getString(R.string.call_running_error_message), Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(context, context.getString(R.string.call_logs_invalid_number), Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void processAudioCall(Context context, String numberToDial, String mydirection) {
        try {
            LOG.info("number to call " + numberToDial);
            if (new PreferenceProvider(context).getPrefBoolean(context.getString(R.string.call_logs_pref_gsm_key))) {
                Toast.makeText(context,
                        context.getString(R.string.gsm_message),
                        Toast.LENGTH_SHORT).show();

                return;
            }
          numberToDial=numberToDial.replace(" ","");
            PreferenceProvider pf = new PreferenceProvider(context);
            if (!PermissionUtils.checkRecordAudioPermission((Activity) context)) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.RECORD_AUDIO}, 101);
                return;
            }
            pf.setPrefboolean(context.getString(R.string.call_logs_pref_make_call), true);

            // Commented below line of code to fix this issue TRIIN-1010
            //if ((isApp != null && isApp.equalsIgnoreCase("1")) && (mydirection != null && mydirection.toUpperCase().contains("AUDIO"))) {
            if (mydirection != null && mydirection.toUpperCase().contains("AUDIO")) {
                if (!Utils.getNetwork(context)) {
                    if (context != null) {
                        Toast.makeText(context, context.getString(R.string.network_unavail_message), Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                if (!HomeScreenActivity.status.equalsIgnoreCase(context.getString(R.string.registered))) {
                    if (context != null) {
                        Toast.makeText(context, context.getString(R.string.registered_message), Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                PSTNCallActivity.isCallLive = true;
                GlobalVariables.destinationnumbettocall = numberToDial;
                pf = new PreferenceProvider(context);

                boolean isInCall = pf.getPrefBoolean(context.getString(R.string.call_logs_incall_message));
                Log.e("isInCall","isInCallAudioCall--->"+isInCall);
                if (isInCall) {
                    Toast.makeText(context, context.getString(R.string.call_running_error_message), Toast.LENGTH_LONG).show();
                    return;
                }

                boolean callRunning = pf.getPrefBoolean("CallRunning");
                if (!callRunning) {
                    if (!numberToDial.equals("") && !numberToDial.equals(GlobalVariables.phoneNumber)) {
                        LOG.info("CallScreen launched");
                        pf.setPrefboolean("CallRunning", true);
                        if (CallLogsFragment.callLogDetailsDialog != null) {
                            CallLogsFragment.callLogDetailsDialog.dismiss();
                        }
                        GlobalVariables.destinationnumbettocall = numberToDial;
                        GlobalVariables.ispeerconnectionclosed = false;
                        Intent intent = new Intent(context, PlayAudioCallActivity.class);
                        intent.putExtra(context.getString(R.string.call_logs_intent_dntnum_key), numberToDial);
                        intent.putExtra(context.getString(R.string.call_logs_intent_stream_subscr_key), "");
                        intent.putExtra(context.getString(R.string.call_logs_intent_initiator_key), true);
                        context.startActivity(intent);

                    } else {
                        pf.setPrefboolean("CallRunning", false);
                        Toast.makeText(context, context.getString(R.string.call_logs_invalid_number), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(context, context.getString(R.string.call_running_error_message), Toast.LENGTH_LONG).show();
                }

            } else {
                if (!Utils.getNetwork(context)) {
                    Toast.makeText(context, context.getString(R.string.network_unavail_message), Toast.LENGTH_LONG).show();
                    return;
                }

                if (!HomeScreenActivity.status.equalsIgnoreCase(context.getString(R.string.registered))) {
                    Toast.makeText(context, context.getString(R.string.registered_message), Toast.LENGTH_LONG).show();
                    return;
                }

                GlobalVariables.destinationnumbettocall = numberToDial;
                String phoneNumber;
                phoneNumber = numberToDial.replaceAll("[^0-9]", "");
                String number = phoneNumber;
                number = number.replace(" ", "").replace("\u00A0", "").replace("+", "").replace("#",
                        "").replace("$", "").replace("#", "").replace("*", "").replace("(", "").replace(")", "").replace("-", "").replace("/", "");
                int len = number.length();

                if (len == 0) {
                    Toast.makeText(context, "Please Enter Valid Number", Toast.LENGTH_SHORT).show();
                    return;
                }

                LOG.info("Call Number Recents" + phoneNumber);
                GlobalVariables.ispeerconnectionclosed = false;


                boolean callRunning = pf.getPrefBoolean("CallRunning");
                if (!callRunning) {
                    pf.setPrefboolean("CallRunning", true);
                    Intent intent = new Intent(context, PSTNCallActivity.class);
                    intent.putExtra(context.getString(R.string.call_logs_intent_dntnum_key), phoneNumber);
                    intent.putExtra(context.getString(R.string.call_logs_intent_stream_subscr_key), "");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(context.getString(R.string.call_logs_intent_initiator_key), true);

                    context.startActivity(intent);
                }else{
                    Toast.makeText(context, context.getString(R.string.call_running_error_message), Toast.LENGTH_LONG).show();
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
