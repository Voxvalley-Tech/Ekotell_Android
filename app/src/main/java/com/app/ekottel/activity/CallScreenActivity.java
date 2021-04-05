package com.app.ekottel.activity;


import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.receivers.RingtonePlayingService;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.ManageAudioFocus;
import com.app.ekottel.utils.NotificationMethodHelper;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.wrapper.CSCall;
import com.ca.wrapper.CSDataProvider;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.app.ekottel.utils.GlobalVariables.LOG;

/**
 * This activity is used to display incoming call related data.
 *
 * @author Ramesh U
 * @version 2017
 */
public class CallScreenActivity extends AppCompatActivity {
    private final String TAG = "CallScreenActivity";
    public static boolean isClosedIncomingCall = false;
    private AppCompatImageView mTvCallButton, mTvEndButton;
    private TextView mTvCallType;
    private TextView mTvCallNumber, mTvCallInfoType;
    private Typeface mTypeFaceRegular;
    private Typeface mTypeFaceLight;
    private CircleImageView mIvCallerImage;
    private PreferenceProvider mPreferenceProvider;
    private int delay1 = 1000;
    private Runnable runnableObj2;
    private final Handler h2 = new Handler();
    private CSCall CSCallsObj = new CSCall();
    private boolean secondcall = false;
    private Ringtone ringtone;
    private long remainingTimeOffet = 35;
    private String sDstMobNu = "";
    private String mCallId = "0";
    private int callActive = 0;
    private String sdp = "";
    private String callType;
    private String srcNumber = "";
    private long callstarttime = new Date().getTime();
    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();
    ManageAudioFocus manageAudioFocus = new ManageAudioFocus();
    String displayname = "";
    private int notificationid = 0;
    private boolean mIsRingingCallBackSent = false;
    TextView countryNameTv;
    ImageView countryFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //   This condition will avoid opening this activity again from overview(multiple apps) button
        if (isClosedIncomingCall) {
            finish();
        }
        setContentView(R.layout.callscreen);
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);


            callType = getString(R.string.call_screen_call_type_audio);


            mTvCallType = findViewById(R.id.tv_call_type);
            mTvCallNumber = findViewById(R.id.tv_call_number);
            mTvCallInfoType = findViewById(R.id.tv_call_info_type);

            mTvCallButton = findViewById(R.id.incoming_callbutton);
            mTvEndButton = findViewById(R.id.incoming_endbutton);
            mIvCallerImage = findViewById(R.id.iv_caller_image);
            countryNameTv = findViewById(R.id.countryNameTv);
            countryFlag = findViewById(R.id.countryFlag);

            mPreferenceProvider = new PreferenceProvider(getApplicationContext());
            mPreferenceProvider.setPrefboolean(getString(R.string.play_video_call_pref_ringing_key), true);
            mPreferenceProvider.setPrefboolean(getString(R.string.call_logs_incall_message), true);
            mPreferenceProvider.setPrefboolean("isIncomingCall", true);

            Typeface typeface = Utils.getTypeface(getApplicationContext());


            mTypeFaceRegular = Utils.getTypefaceRegular(getApplicationContext());
            mTypeFaceLight = Utils.getTypefaceLight(getApplicationContext());
            mTvCallType.setTypeface(mTypeFaceLight);
            mTvCallNumber.setTypeface(mTypeFaceRegular);

            sDstMobNu = getIntent().getStringExtra(getString(R.string.call_screen_dstnum));
            mCallId = getIntent().getStringExtra(getString(R.string.call_screen_callid));
            callActive = getIntent().getIntExtra(getString(R.string.call_screen_call_active), 0);
            sdp = getIntent().getStringExtra(getString(R.string.call_screen_sdp));
            callType = getIntent().getStringExtra(getString(R.string.call_screen_call_type));
            srcNumber = getIntent().getStringExtra(getString(R.string.call_screen_src_number));
            callstarttime = getIntent().getLongExtra(getString(R.string.call_screen_call_start_time), new Date().getTime());
            Log.e("callType","callTypess---->"+callType);
            if (callType.equals(getString(R.string.call_screen_call_type_video))) {
                mTvCallInfoType.setText(getString(R.string.call_screen_incoming_video_call_message));
                if (CSDataProvider.getLoginstatus()) {
                    mIsRingingCallBackSent = true;
                    CSCallsObj.sendVideoCallRinging(srcNumber, mCallId, secondcall);
                }
            } else if (callType.equals("pstn")) {
                mTvCallInfoType.setText(getString(R.string.call_screen_incoming_pstn_call_message));
                if (CSDataProvider.getLoginstatus()) {
                    mIsRingingCallBackSent = true;
                }
            } else {
                mTvCallInfoType.setText(getString(R.string.call_screen_incoming_call_message));
                if (CSDataProvider.getLoginstatus()) {
                    mIsRingingCallBackSent = true;
                    CSCallsObj.sendVoiceCallRinging(srcNumber, mCallId, secondcall);
                }
            }

            long presenttime = new Date().getTime();

            LOG.info("Present Time present" + presenttime);
            LOG.info("Present Time callstarttime" + callstarttime);

            remainingTimeOffet = 60 - ((presenttime - callstarttime) / 1000);
            if (remainingTimeOffet < 0) {
                remainingTimeOffet = 0;
            }

            LOG.info("remainingTimeOffet:" + remainingTimeOffet);
            LOG.info("sDstMobNu:" + sDstMobNu);
            LOG.info("mCallId:" + mCallId);
            LOG.info("callActive:" + callActive);
            LOG.info("sdp:" + sdp);
            LOG.info("callType:" + callType);
            LOG.info("srcNumber:" + srcNumber);
            String nativecontactid = "";
            displayname = "UnKnown";
            String displayName = srcNumber;
            Cursor ccfr = CSDataProvider.getContactCursorByNumber(srcNumber);
            if (ccfr.getCount() > 0) {

                ccfr.moveToNext();
                displayname = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
                nativecontactid = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));


            }
            if (displayName != null && !displayName.equalsIgnoreCase(srcNumber)) {
                mTvCallNumber.setVisibility(View.VISIBLE);
                mTvCallType.setText(displayName);
                mTvCallNumber.setText(srcNumber);
            } else {
                mTvCallType.setText(displayName);
                mTvCallNumber.setVisibility(View.GONE);
                try {
                    mTvCallType.setTextColor(getResources().getColor(R.color.white));
                    mTvCallType.setTextSize(22);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ccfr.close();
            String notificationName = displayname;
            if (displayname.equals("UnKnown")) {
                notificationName = srcNumber;
            }
            if (callType.equals("video")) {
                mTvCallInfoType.setText(getString(R.string.call_screen_incoming_video_call_message));
                notificationid = NotificationMethodHelper.notifyIncomigCall(getApplicationContext(), notificationName, "Incoming video call", secondcall);
                manageAudioFocus.requestAudioFocus(CallScreenActivity.this, mCallId, srcNumber, false);
            } else if (callType.equals("pstn")) {
                notificationid = NotificationMethodHelper.notifyIncomigCall(getApplicationContext(), notificationName, "Incoming pstn call", secondcall);
            } else {
                mTvCallInfoType.setText(getString(R.string.call_screen_incoming_call_message));
                notificationid = NotificationMethodHelper.notifyIncomigCall(getApplicationContext(), notificationName, "Incoming audio call", secondcall);
                manageAudioFocus.requestAudioFocus(CallScreenActivity.this, mCallId, srcNumber, false);
            }
            registerReceiver(NotificationReceiver, new IntentFilter("NotificationHandleReceiver"));
            manageAudioFocus.requestAudioFocus(CallScreenActivity.this, mCallId, srcNumber, false);

         /*   if (callType.equals("video")) {
                CSCallsObj.sendVideoCallRinging(srcNumber, mCallId, secondcall);
            } else if (callType.equals("audio")) {
                CSCallsObj.sendVoiceCallRinging(srcNumber, mCallId, secondcall);
            }*/

            Intent i = new Intent(this, RingtonePlayingService.class);
            stopService(i);

            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
            ringtone.play();

            new Thread(new Runnable() {
                public void run() {
                    try {
                        Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, srcNumber);
                        if (cur.getCount() > 0) {
                            cur.moveToNext();
                            String picid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
                            Bitmap mybitmap = CSDataProvider.getImageBitmap(picid);
                            if (mybitmap != null) {
                                mIvCallerImage.setImageBitmap(mybitmap);
                            } else {
                                new ImageDownloaderTask(mIvCallerImage).execute(picid);
                            }
                        }
                        cur.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();

            getcountryname(srcNumber);
            if (displayname != null && !displayname.equalsIgnoreCase(srcNumber)) {
                mTvCallNumber.setVisibility(View.VISIBLE);
                mTvCallType.setText(displayname);
                mTvCallNumber.setText(srcNumber);
            } else {
                mTvCallType.setText(displayname);
                mTvCallNumber.setVisibility(View.GONE);
                try {
                    mTvCallType.setTextColor(getResources().getColor(R.color.white));
                    mTvCallType.setTextSize(22);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            runnableObj2 = new Runnable() {
                int i = 0;

                public void run() {
                    h2.postDelayed(this, delay1);
                    CallScreenActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            i++;
                            if (i >= 30) {

                                h2.removeCallbacks(runnableObj2);
                                GlobalVariables.incallcount = GlobalVariables.incallcount - 1;
                                if (GlobalVariables.incallcount < 0) {
                                    GlobalVariables.incallcount = 0;
                                }

                               /* if (callType.equals("video")) {
                                    Utils.NotifyAppInMissedCall(getString(R.string.missed_video_call_message), mTvCallType.getText().toString().trim(), "", "", 0, getApplicationContext());
                                } else {
                                    Utils.NotifyAppInMissedCall(getString(R.string.missed_audio_call_message), mTvCallType.getText().toString().trim(), "", "", 0, getApplicationContext());
                                }*/
                                manageAudioFocus.abandonAudioFocus();
                                finish();
                            }
                        }
                    });
                }
            };
            h2.postDelayed(runnableObj2, delay1);

            try {
                MainActivityReceiverObj = new MainActivityReceiver();
                IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
                IntentFilter filter1 = new IntentFilter(CSEvents.CSCALL_CALLENDED);
                IntentFilter filter2 = new IntentFilter(CSEvents.CSCALL_CALLTERMINATED);
                IntentFilter filter3 = new IntentFilter(CSEvents.CSCLIENT_LOGIN_RESPONSE);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter2);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter3);


                // Screen ON and OFF receiver register
                IntentFilter screenStateFilter = new IntentFilter();
                screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
                screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
                registerReceiver(mScreenStateReceiver, screenStateFilter);

            } catch (Exception e) {
                e.printStackTrace();
            }

            mTvCallButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (Utils.getNetwork(getApplicationContext())) {
                            h2.removeCallbacks(runnableObj2);

                            if (GlobalVariables.answeredcallcount > 0) {
                                //for (String activecallid:GlobalVariables.activecallids) {
                                Intent intentt = new Intent("TerminateForSecondCall");
                                intentt.putExtra("callid", GlobalVariables.lastcallid);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentt);

                            } else {
                                answerCall();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.network_unavail_message), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            mTvEndButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (Utils.getNetwork(getApplicationContext())) {

                            h2.removeCallbacks(runnableObj2);

                            endCall(srcNumber, mCallId);

                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.network_unavail_message), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });


        } catch (
                Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (ringtone != null) {
                    ringtone.stop();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (ringtone != null) {
                    ringtone.stop();
                }

                return true;

            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * This BroadCast Receiver is used for knowing the screen lock and unlock events
     */
    BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                LOG.info("Screen went OFF");
                if (ringtone != null) {
                    ringtone.stop();
                }
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                LOG.info("Screen went ON");
            }
        }
    };

    public void answerCall() {
        try {
            Intent intent1;
            if (callType.equals("video")) {
                intent1 = new Intent(CallScreenActivity.this, PlayVideoCallActivity.class);
            } else if (callType.equals("pstn")) {
                intent1 = new Intent(CallScreenActivity.this, PSTNCallActivity.class);
            } else {
                intent1 = new Intent(CallScreenActivity.this, PlayAudioCallActivity.class);
            }
            intent1.putExtra("isinitiatior", false);
            intent1.putExtra("sDstMobNu", sDstMobNu);
            intent1.putExtra("callactive", callActive);
            //intent.putExtra("sdp", sdp);
            intent1.putExtra("callType", callType);
            intent1.putExtra("srcnumber", srcNumber);

            intent1.putExtra("callid", mCallId);
            startActivity(intent1);
            GlobalVariables.incallcount = GlobalVariables.incallcount - 1;
            if (GlobalVariables.incallcount < 0) {
                GlobalVariables.incallcount = 0;
            }
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationid);
            manageAudioFocus.abandonAudioFocus();
            finish();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void endCall(String srcnumber, String callid) {
        try {

            if (callType.equals("video")) {
                CSCallsObj.endVideoCall(srcnumber, callid, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
            } else if (callType.equals("pstn")) {
                CSCallsObj.endPstnCall(srcnumber, callid);
            } else {
                CSCallsObj.endVoiceCall(srcnumber, callid, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
            }


            GlobalVariables.incallcount = GlobalVariables.incallcount - 1;
            if (GlobalVariables.incallcount < 0) {
                GlobalVariables.incallcount = 0;
            }
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationid);

            manageAudioFocus.abandonAudioFocus();
            finish();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This is used to get image and update UI
     */
    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap photo;
            try {
                photo = loadContactPhoto(Long.parseLong(params[0]));
                if (photo == null) {
                    photo = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_contact_avatar);
                }

            } catch (Exception e) {
                photo = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_contact_avatar);
                e.printStackTrace();
            }
            return photo;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }
    }


    /**
     * Method which is load image bitmap
     *
     * @param id this is a id to provide and get bitmap image
     * @return which returns bitmap image
     */
    public Bitmap loadContactPhoto(long id) {
        try {
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
            InputStream stream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), uri, true);
            return BitmapFactory.decodeStream(stream);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }


    }


    @Override
    public void onBackPressed() {

    }


    public void getcountryname(String code_num) {
        try {
            if (code_num != null) {
                code_num = code_num.replaceAll("[^0-9]", "");
                if (code_num != null && code_num.length() > 3) {
                    code_num = code_num.substring(0, 3);
                }

                Locale obj = new Locale("", getTwoCharCountryCode(code_num));
                String countryName = obj.getDisplayCountry();
                if (code_num != null && countryName.equals("") && code_num.length() > 2) {
                    obj = new Locale("", getTwoCharCountryCode(code_num.substring(0, 2)));
                    countryName = obj.getDisplayCountry();
                }
                if (code_num != null && countryName.equals("") && code_num.length() > 1) {
                    obj = new Locale("", getTwoCharCountryCode(code_num.substring(0, 1)));
                    countryName = obj.getDisplayCountry();
                }
                if (!countryName.equals("")) {
                    countryNameTv.setText(countryName);


                } else {
                    countryNameTv.setText("");
                }
            }

            // phone must begin with '+'
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(srcNumber, "");
            int countryCode = numberProto.getCountryCode();


            countryFlag.setVisibility(View.VISIBLE);


            String countrycode = String.valueOf(countryCode);

            countrycode = countrycode.replaceAll("[^0-9]", "");


            int resorceID = getResources().getIdentifier(getString(R.string.dialpad_tringy_message) + countrycode, getString(R.string.dialpad_drawable_message), getPackageName());

            countryFlag.setImageResource(resorceID);
        } catch (Exception e) {
            e.printStackTrace();
            countryFlag.setVisibility(View.GONE);
        }

    }

    private String getTwoCharCountryCode(String code) {
        for (Map.Entry<String, String> item : ZoneSelectActivity.country2Phone.entrySet()) {
            if (item.getValue().equalsIgnoreCase(code)) {
                return item.getKey();
            }


        }
        return "";
    }


    public void updateUI(String str) {

        try {

            if (str.equals(getString(R.string.network_message))) {
                LOG.info("NetworkError receieved");
                h2.removeCallbacks(runnableObj2);
                isClosedIncomingCall = true;
                manageAudioFocus.abandonAudioFocus();
                finish();
            } else if (str.equals(getString(R.string.direct_audio_video_call_end_resuccess_message))) {

                h2.removeCallbacks(runnableObj2);
                isClosedIncomingCall = true;
                manageAudioFocus.abandonAudioFocus();
                finish();
            } else if (str.equals(getString(R.string.direct_audio_video_call_end_resuccess_failure_message))) {
                Toast.makeText(this, getString(R.string.call_screen_call_end_message), Toast.LENGTH_SHORT).show();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


/**
 * This class is handle call back
 */

    /**
     * This class is handle call back
     */

    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                LOG.info("CallScreenActivity events " + intent.getAction().toString());
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    updateUI(CSEvents.CSCLIENT_NETWORKERROR);
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS) && !mIsRingingCallBackSent) {

                        if (callType.equals(getString(R.string.call_screen_call_type_video))) {
                            mIsRingingCallBackSent = true;
                            CSCallsObj.sendVideoCallRinging(srcNumber, mCallId, secondcall);
                        } else {
                            mIsRingingCallBackSent = true;
                            CSCallsObj.sendVoiceCallRinging(srcNumber, mCallId, secondcall);
                        }

                    }

                } else if (intent.getAction().equals(CSEvents.CSCALL_CALLTERMINATED)) {
                    if (GlobalVariables.lastcallid.equals(intent.getStringExtra("callid"))) {
                        answerCall();
                    }


                } else if (intent.getAction().equals(CSEvents.CSCALL_CALLENDED)) {

                    if (mCallId.equals(intent.getStringExtra("callid"))) {
                        h2.removeCallbacks(runnableObj2);
                        GlobalVariables.incallcount = GlobalVariables.incallcount - 1;
                        if (GlobalVariables.incallcount < 0) {
                            GlobalVariables.incallcount = 0;
                        }
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(notificationid);
                        manageAudioFocus.abandonAudioFocus();
                        finish();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (ringtone != null) {
                ringtone.stop();
            }

            if (mScreenStateReceiver != null) {
                unregisterReceiver(mScreenStateReceiver);
            }
            LOG.info("iscallclassscreen->");
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            mPreferenceProvider.setPrefboolean(getString(R.string.call_logs_incall_message), false);
            mPreferenceProvider.setPrefboolean(getString(R.string.play_video_call_pref_ringing_key), false);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationid);
            unregisterReceiver(NotificationReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    BroadcastReceiver NotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LOG.info("CallScreenActivity", "NotificationReceiver called");
            String action = intent.getAction();
            if (action.equals("NotificationHandleReceiver")) {
                String actionToperFrom = intent.getStringExtra("operationToPerform");
                LOG.info("CallScreenActivity", "operationToPerform " + actionToperFrom);
                if (actionToperFrom.equals("EndCall")) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(notificationid);
                    mTvEndButton.performClick();
                } else if (actionToperFrom.equals("AnswerCall")) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(notificationid);
                    mTvCallButton.performClick();
                }
            }
        }
    };
}
