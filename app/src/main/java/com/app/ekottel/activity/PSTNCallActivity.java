package com.app.ekottel.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;

import androidx.multidex.MultiDex;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;

import static com.app.ekottel.utils.GlobalVariables.LOG;

import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.foregroundservices.ForeGroundServiceApis;
import com.app.ekottel.fragment.ContactsFragment;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.DialingFeedback;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.HTTPUtils;
import com.app.ekottel.utils.ManageAudioFocus;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.dao.CSLocation;
import com.ca.wrapper.CSCall;
import com.ca.wrapper.CSDataProvider;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.json.JSONArray;
import org.json.JSONObject;
import org.webrtc.EglBase;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

/**
 * This activity is used to handle after accepting app to pstn call information.
 *
 * @author Ramesh U
 * @version 2017
 */
public class PSTNCallActivity extends Activity implements SensorEventListener {

    private static final String TAG = "PlaySipCallActivity";
    //private NotificationManager notificationManager;
    public static boolean isCallLive = false;
    private final Handler mTimerHandler = new Handler();
    public static String myCallId = null;
    private EglBase rootEglBase;
    private boolean isHoldEnabled = false;
    private boolean isAudioEnabled = false;
    private boolean isSpeakerEnabled = false;
    ImageView mEndCallButtonTv;
    private TextView mContactNumberTv, mContactNameTv, mTimerTv, mTvCallAvailCreditText, mTvCallAvailCredit, mCallRatesTv, mPSTNCountryTv;
    private ImageView mMuteButtonTv, mContactsButtonTv, mChatButtonTv, mSpeakerButtonTv, mDTMFButtonTv, mHoldButtonTv;
    private int mTimerDelay = 1000;
    private ImageView mContactImage, mPSTNCountryImg, mCallingGifImg;
    private PreferenceProvider mPreferenceProvider;
    private String username = "";
    private RelativeLayout mBlankScreenLayout;
    private LinearLayout mMainScreenLayout;
    private String TAG1 = "PSTNCallActivity";
    private DialingFeedback mDialingFeedback;
    private String contactName = null;
    private Runnable mTimerRunnable;
    private String mDestinationNumberToCall = "";
    private CSCall CSCallsObj = new CSCall();
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private static final int SENSOR_SENSITIVITY = 4;
    private boolean isMediaDisconnected = false;
    private TextView mTvDuration;
    private ServicePhoneStateReceiver mPhoneConnectivityReceiver;
    private TelephonyManager mTelephonyManager;
    private boolean isGSMCallCame = false;
    private long mLastClickTime = 0;
    private boolean isNetworkErrorReceived = false;
    boolean shownotifiction = true;
    ManageAudioFocus manageAudioFocus = new ManageAudioFocus();
    //private int notificationID;
    private boolean isMediaConnected = false;
    private String callEndReason = "";
    TextView reconnecting;
    private boolean isConnectingTone = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstcall_incallscreen);
        LOG.info(TAG1, "onCreateCalled");
        try {
            isCallLive = true;

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            //notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            GlobalVariables.isgolive_live = false;
            mContactNumberTv = findViewById(R.id.caller_number);
            mContactNameTv = findViewById(R.id.caller_name);
            mContactNameTv.setSelected(true);
            mContactNumberTv.setSelected(true);
            mTimerTv = findViewById(R.id.tv_play_sip_timer);
            mEndCallButtonTv = findViewById(R.id.callbutton);
            mMuteButtonTv = findViewById(R.id.mute);
            mCallRatesTv = findViewById(R.id.tv_call_rates);
            mCallRatesTv.setSelected(true);
            mBlankScreenLayout = findViewById(R.id.blankscreen);
            mMainScreenLayout = findViewById(R.id.ll_mainscreen);
            mTvDuration = findViewById(R.id.duration);
            mBlankScreenLayout.setVisibility(View.GONE);
            //  mMainScreenLayout.setVisibility(View.VISIBLE);
            reconnecting = (TextView) findViewById(R.id.tv_audio_call_duration);
            username = CSDataProvider.getLoginID();
            username = username.replace("+", "");

            mSpeakerButtonTv = findViewById(R.id.speaker);
            mDTMFButtonTv = findViewById(R.id.keypad);
            mHoldButtonTv = findViewById(R.id.hold);
            mContactsButtonTv = findViewById(R.id.contacts);
            mChatButtonTv = findViewById(R.id.message);
            mTvCallAvailCredit = findViewById(R.id.tv_call_avail_credit);
            mTvCallAvailCredit.setSelected(true);
            mTvCallAvailCreditText = findViewById(R.id.tv_call_avail_credit_text);

            mPSTNCountryImg = findViewById(R.id.iv_pstn_country_flag);
            mPSTNCountryTv = findViewById(R.id.tv_pstn_country_flag);
            mPSTNCountryTv.setSelected(true);
            mCallingGifImg = findViewById(R.id.iv_gif);
            CSCall csCall = new CSCall();
            csCall.setIceTransportsType(CSConstants.IceTransportsType.ALL);
            // As discussed with Mr.Srnivas updted codec to opus
            mPreferenceProvider = new PreferenceProvider(getApplicationContext());
            mPreferenceProvider.setPrefboolean("CallRunning", true);
            mPreferenceProvider.setPrefboolean(getString(R.string.call_logs_incall_message), true);
            mPreferenceProvider.setPrefboolean(getString(R.string.call_logs_pref_make_call), true);
            mPreferenceProvider.setPrefboolean(PreferenceProvider.IS_RETURNING_FROM_CALL, true);
            mPreferenceProvider.setPrefboolean(getString(R.string.play_video_call_pref_already_login), true);
            GlobalVariables.INCALL = true;
            String availBal = mPreferenceProvider.getPrefString(getString(R.string.bal_trans_pref_avail_message));

            Intent intent = getIntent();
            contactName = intent.getStringExtra("ContactName");

            if (availBal.isEmpty()) {
                mTvCallAvailCredit.setText(Html.fromHtml("<font color=\"#ffffff\">" + getString(R.string.play_sip_call_avail_credit_message) + "</font>" + "$0"));
            } else {
                mTvCallAvailCredit.setText("$" + availBal);
            }


            Typeface typeface = Utils.getTypeface(getApplicationContext());

            mContactImage = (ImageView) findViewById(R.id.profile_image);

            mMuteButtonTv.setEnabled(false);
            mDTMFButtonTv.setEnabled(false);
            mHoldButtonTv.setEnabled(false);

            // Native GSM call state Receiver
            mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            // Telephony
            if (mPhoneConnectivityReceiver == null) {
                mPhoneConnectivityReceiver = new ServicePhoneStateReceiver();
                mTelephonyManager.listen(mPhoneConnectivityReceiver,
                        PhoneStateListener.LISTEN_CALL_STATE);
            }
            mPreferenceProvider.setPrefString(PreferenceProvider.LAST_DIAL_NUMBER, GlobalVariables.destinationnumbettocall);
            new CSCall().setPreferredPtimeForG729("80");
            new CSCall().setPreferredAudioCodec(CSConstants.PreferredAudioCodec.G729);
            try {
                // phone must begin with '+'
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                String number = "";
                String desinationNumber;
                if (getIntent().getBooleanExtra("isinitiatior", false))
                    desinationNumber = getIntent().getStringExtra("dstnumber");
                else {
                    desinationNumber = getIntent().getStringExtra("srcnumber");
                    GlobalVariables.destinationnumbettocall = desinationNumber;
                }
                if (desinationNumber != null && !desinationNumber.startsWith("+")) {
                    number = "+" + desinationNumber;
                } else {
                    number = desinationNumber;
                }
              /*  if (desinationNumber != null && !desinationNumber.startsWith("+")) {
                    number = "+" + getIntent().getStringExtra("dstnumber");
                } else {
                    number = getIntent().getStringExtra("dstnumber");
                }*/
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse(number, "");
                int countryCode = numberProto.getCountryCode();


                LOG.info(TAG1, "Calling Screen Code" + countryCode);
                mPSTNCountryImg.setVisibility(View.VISIBLE);


                String countrycode = String.valueOf(countryCode);
                LOG.info(TAG1, "Country Code before" + countrycode);

                countrycode = countrycode.replaceAll("[^0-9]", "");

                LOG.info(TAG1, "Country Code after" + countrycode);

                int resorceID = getResources().getIdentifier(getString(R.string.dialpad_tringy_message) + countrycode, getString(R.string.dialpad_drawable_message), getPackageName());

                mPSTNCountryImg.setImageResource(resorceID);

                if (getIntent().getBooleanExtra("isinitiatior", false))
                    getCountryName(getIntent().getStringExtra("dstnumber"));
                else {
                    getCountryName(getIntent().getStringExtra("srcnumber"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                mPSTNCountryImg.setVisibility(View.GONE);

            }
            if (Build.MODEL.equals("GT-I9060I")) {
                updateDimensions();
            }
            String contactName = "";
            String contactNumber = "";
            if (GlobalVariables.destinationnumbettocall != null && !GlobalVariables.destinationnumbettocall.startsWith("+")) {
                contactNumber = "+" + GlobalVariables.destinationnumbettocall;
            } else {
                contactNumber = GlobalVariables.destinationnumbettocall;
            }
            Cursor connamecr = CSDataProvider.getContactCursorByNumber(contactNumber);
            LOG.info(TAG1, "Caller Name Start");
            if (connamecr.getCount() > 0) {
                LOG.info(TAG1, "Caller Name Inside");
                connamecr.moveToNext();
                contactName = connamecr.getString(connamecr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
            }
            connamecr.close();

            LOG.info(TAG1, "Caller Name" + contactName);
            if (contactName.equals("")) {
                mContactNumberTv.setText(GlobalVariables.destinationnumbettocall);
            }
            if (!contactName.equals("")) {

                String str = GlobalVariables.destinationnumbettocall;

                str = str.replaceAll("[^0-9]", "").trim();

                if (!contactName.equalsIgnoreCase(str)) {
                    mContactNameTv.setVisibility(View.VISIBLE);

                    if (this.contactName != null && !this.contactName.isEmpty()) {
                        mContactNameTv.setText(this.contactName);
                    } else {
                        mContactNameTv.setText(contactName);
                    }

                    mContactNumberTv.setText(GlobalVariables.destinationnumbettocall);
                } else {
                    mContactNameTv.setVisibility(View.GONE);
                    mContactNumberTv.setText(GlobalVariables.destinationnumbettocall);
                }

            } else if (this.contactName != null && !this.contactName.isEmpty()) {
                mContactNameTv.setVisibility(View.VISIBLE);
                mContactNameTv.setText(this.contactName);
            }


            String value = GlobalVariables.destinationnumbettocall;
            String username1 = CSDataProvider.getLoginID();
            String password1 = CSDataProvider.getPassword();

            String pwd = username1 + password1;

            LOG.debug(TAG1, "Password" + pwd);
            String actualPassword = "";
            try {
                actualPassword = Utils.generateSHA256(pwd);
                LOG.debug(TAG1, "Password after SHA" + actualPassword);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String url = Constants.CALL_RATES_URL + value + "?username=%2B" + username + "&password=" + actualPassword;
            new APITask(url, getString(R.string.dialpad_call_rates_api_message)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            new Thread(new Runnable() {
                public void run() {
                    try {

                        Bitmap bitmap = getIMAGEPhoto(GlobalVariables.destinationnumbettocall);

                        if (bitmap != null) {
                            mContactImage.setImageBitmap(Utils.getRoundedCornerBitmap(bitmap, 600));
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }


                }
            }).start();


            rootEglBase = EglBase.create();


            mTimerRunnable = new Runnable() {
                int i = 0;

                public void run() {
                    mTimerHandler.postDelayed(this, mTimerDelay);
                    //LOG.info("printing at 1 sec");
                    PSTNCallActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            String minutes = "00";
                            String seconds = "00";
                            int x = i++;
                            int mins = (x) / 60;
                            int sec = (x) % 60;
                            if (mins < 10) {
                                minutes = "0" + String.valueOf(mins);
                            } else {
                                minutes = String.valueOf(mins);
                            }
                            if (sec < 10) {
                                seconds = "0" + String.valueOf(sec);
                            } else {
                                seconds = String.valueOf(sec);
                            }

                            mTimerTv.setText("In Call" + " ( " + minutes + ":" + seconds + " ) ");
                        }
                    });
                }
            };


            IntentFilter filter1 = new IntentFilter(CSEvents.CSCALL_CALLENDED);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);

            if (getIntent().getBooleanExtra("isinitiatior", false)) {
                mDestinationNumberToCall = getIntent().getStringExtra("dstnumber");
                //destinationNumberToCall = destinationNumberToCall.replace("+","");
                myCallId = CSCallsObj.startPstnCall(mDestinationNumberToCall,
                        GlobalVariables.smsdidnumber, CallLocationActivity.callcontext,
                        new CSLocation(CallLocationActivity.final_lat, CallLocationActivity.final_lng, CallLocationActivity.final_address));
                //myCallId = CSCallsObj.startPstnCall(mDestinationNumberToCall, CSConstants.CALLRECORD.DONTRECORD);
                manageAudioFocus.requestAudioFocus(PSTNCallActivity.this, myCallId, mDestinationNumberToCall, false);
            }/* else {
                mDestinationNumberToCall = getIntent().getStringExtra("srcnumber");
                //destinationNumberToCall = destinationNumberToCall.replace("+","");
                myCallId = getIntent().getStringExtra("callid");
                myCallId = CSCallsObj.startPstnCall(mDestinationNumberToCall,GlobalVariables.smsdidnumber,CallLocationActivity.callcontext,new CSLocation(CallLocationActivity.final_lat, CallLocationActivity.final_lng,CallLocationActivity.final_address));
                CSCallsObj.answerPstnCall(mDestinationNumberToCall, getIntent().getStringExtra("callid"));
            }*/


            GlobalVariables.lastcallid = myCallId;
            mPreferenceProvider.setPrefString(PreferenceProvider.IN_CALL_CALLER_ID, myCallId);
            mPreferenceProvider.setPrefString(PreferenceProvider.RUNNING_CALL_TYPE, "PSTN");
            mPreferenceProvider.setPrefString(PreferenceProvider.IN_CALL_NUMBER, mDestinationNumberToCall);
            mPreferenceProvider.setPrefboolean(PreferenceProvider.IS_CALL_RUNNING, true);
            //notificationID = Utils.NotifyCallInBackGound(getString(R.string.play_audio_in_audio_call), mDestinationNumberToCall, "", "PSTN", 0, getApplicationContext());
            new ForeGroundServiceApis().startCallService(getApplicationContext(), mDestinationNumberToCall, getString(R.string.play_audio_in_audio_call), "PSTN");

            mEndCallButtonTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        shownotifiction = false;
                        CSCallsObj.endPstnCall(mDestinationNumberToCall, myCallId);
                        endTheCall();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            mChatButtonTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    Intent intent = new Intent(getApplicationContext(), ChatAdvancedActivity.class);
                    intent.putExtra(Constants.INTENT_CHAT_CONTACT_NUMBER, GlobalVariables.destinationnumbettocall);
                    intent.putExtra(Constants.INTENT_CHAT_CONTACT_NAME, PSTNCallActivity.this.contactName);
                    intent.putExtra(Constants.IS_VIDEO_CALL_RUNNING, false);
                    getApplicationContext().startActivity(intent);

                }
            });

            mContactsButtonTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    Intent intent = new Intent(getApplicationContext(), ContactsFragmentActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);

                }
            });

            mDTMFButtonTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        mDTMFButtonTv.setSelected(true);
                        showdialpad();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });


            mHoldButtonTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        LOG.info("onClick: Hold " + isHoldEnabled);
                        if (isHoldEnabled) {
                            CSCallsObj.holdPstnCall(myCallId, false);
                            isHoldEnabled = false;
                            mHoldButtonTv.setSelected(false);
                        } else {
                            CSCallsObj.holdPstnCall(myCallId, true);
                            isHoldEnabled = true;
                            mHoldButtonTv.setSelected(true);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            mSpeakerButtonTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        LOG.info("onClick: isSpeakerEnabled " + isSpeakerEnabled);

                        if (isSpeakerEnabled) {
                            isSpeakerEnabled = false;
                            CSCallsObj.enableSpeaker(myCallId, false);
                            mSpeakerButtonTv.setSelected(false);

                        } else {
                            isSpeakerEnabled = true;
                            CSCallsObj.enableSpeaker(myCallId, true);
                            mSpeakerButtonTv.setSelected(true);
                        }


                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            mMuteButtonTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        /*   if (getIntent().getBooleanExtra(getString(R.string.call_logs_intent_initiator_key), false)) {*/

                        if (isAudioEnabled) {
                            Log.i("isAudioEnabled", "iffff--->");
                            CSCallsObj.muteAudio(myCallId, false);
                            mMuteButtonTv.setSelected(false);
                            isAudioEnabled = false;
                        } else {
                            Log.i("isAudioEnabled", "elseee--->");
                            CSCallsObj.muteAudio(myCallId, true);
                            isAudioEnabled = true;
                            mMuteButtonTv.setSelected(true);
                        }


                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });


        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    private void updateDimensions() {
        try {
            ViewGroup.LayoutParams params = mContactImage.getLayoutParams();
            params.height = (int) getResources().getDimension(R.dimen._80dp);
            params.width = (int) getResources().getDimension(R.dimen._90dp);
            mContactImage.setLayoutParams(params);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will finish the activity to end the running call
     */
    public void endTheCall() {
        try {

            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            mTimerHandler.removeCallbacks(mTimerRunnable);

            new ForeGroundServiceApis().stopCallService(getApplicationContext());
            GlobalVariables.INCALL = false;
            GlobalVariables.incallcount = GlobalVariables.incallcount - 1;
            GlobalVariables.answeredcallcount = GlobalVariables.answeredcallcount - 1;
            if (GlobalVariables.answeredcallcount < 0) {
                GlobalVariables.answeredcallcount = 0;
            }
            if (GlobalVariables.incallcount < 0) {
                GlobalVariables.incallcount = 0;
            }
            manageAudioFocus.abandonAudioFocus();

            // Updating subscribed packages to reduce the minutes
            mPreferenceProvider.setPrefboolean(getString(R.string.subscribe_package_buy_package_pref_my_packages), true);
            Intent registrationIntent = new Intent("com.app.ekottel.RegistrationStatus");
            sendBroadcast(registrationIntent);

            finish();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This AsyncTask will updated the call rates of ongoing call
     */
    class APITask extends AsyncTask<Void, Void, Void> {
        private String url;
        String api;
        String response = "";

        APITask(String url, String api) {
            this.url = url;
            this.api = api;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            response = HTTPUtils.getResponseFromURLGET(url);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            processResponse(api, response);
        }
    }

    private void processResponse(String api, String returndata) {

        if (api.equals(getString(R.string.dialpad_call_rates_api_message))) {
            try {


                //LOG.info(TAG1, "returndata:" + returndata);
                JSONObject jsonObj = new JSONObject(returndata);
                String message = "";
                JSONArray array = jsonObj.getJSONObject(getString(R.string.dialpad_response_message_key)).getJSONArray(getString(R.string.dialpad_response_message_value));
                LOG.info(TAG1, "array length:" + array.length());
                for (int i = 0; i < array.length(); i++) {
                    message = array.getJSONObject(i).getString(getString(R.string.dialpad_response_cost_key));
                }

                if (message.equals("")) {

                    mCallRatesTv.setVisibility(View.INVISIBLE);
                } else {
                    mCallRatesTv.setVisibility(View.VISIBLE);
                    mCallRatesTv.setText("$" + message);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This Method will show the DTMF dialog
     *
     * @return
     */
    public boolean showdialpad() {
        try {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {


                    final Dialog dialogView = new Dialog(PSTNCallActivity.this);
                    dialogView.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {

                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                mDTMFButtonTv.setSelected(false);
                                dialogView.dismiss();
                            }

                            return true;
                        }
                    });
                    dialogView.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialogView.setCancelable(true);
                    dialogView.setContentView(R.layout.dtmf_dialog);
                    dialogView.getWindow().setBackgroundDrawable(
                            new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    Window window = dialogView.getWindow();
                    WindowManager.LayoutParams wlp = window.getAttributes();
                    wlp.gravity = Gravity.BOTTOM;
                    wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
                    wlp.windowAnimations = R.style.DialogAnimation;
                    window.setAttributes(wlp);
                    dialogView.getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    TextView zero = dialogView.findViewById(R.id.imageButton_dialpad_0);
                    TextView one = dialogView.findViewById(R.id.imageButton_dialpad_1);
                    TextView two = dialogView.findViewById(R.id.imageButton_dialpad_2);
                    TextView three = dialogView.findViewById(R.id.imageButton_dialpad_3);
                    TextView four = dialogView.findViewById(R.id.imageButton_dialpad_4);
                    TextView five = dialogView.findViewById(R.id.imageButton_dialpad_5);
                    TextView six = dialogView.findViewById(R.id.imageButton_dialpad_6);
                    TextView seven = dialogView.findViewById(R.id.imageButton_dialpad_7);
                    TextView eight = dialogView.findViewById(R.id.imageButton_dialpad_8);
                    TextView nine = dialogView.findViewById(R.id.imageButton_dialpad_9);

                    TextView plus = dialogView.findViewById(R.id.imageButton_dialpad_plus);
                    TextView hash = dialogView.findViewById(R.id.imageButton_dialpad_hash);


                    final EditText edittext = dialogView.findViewById(R.id.editText1);
                    ImageView backarrow = dialogView.findViewById(R.id.imageView5);

                    Button endcall = dialogView.findViewById(R.id.dtmf_end_call_button);
                    Button hidekeyboard = dialogView.findViewById(R.id.dtmf_hide_keypad_button);

                    edittext.setFocusable(false);
                    edittext.setClickable(true);
                    dialogView.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            mDTMFButtonTv.setSelected(false);
                        }
                    });
                    endcall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (dialogView != null) {
                                dialogView.dismiss();
                                mEndCallButtonTv.performClick();
                            }
                        }
                    });

                    hidekeyboard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (dialogView != null) {
                                dialogView.dismiss();
                                mDTMFButtonTv.setSelected(false);
                            }
                        }
                    });

                    backarrow.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            try {
                                edittext.setText("");

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            return true;
                        }
                    });

                    edittext
                            .setOnTouchListener(new View.OnTouchListener() {

                                @Override
                                public boolean onTouch(View v, MotionEvent event) {

                                    v.onTouchEvent(event);
                                    InputMethodManager imm = (InputMethodManager) v
                                            .getContext().getSystemService(
                                                    Context.INPUT_METHOD_SERVICE);
                                    if (imm != null) {
                                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                                    }
                                    return true;
                                }
                            });
                    backarrow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                String str = edittext.getText().toString();
                                str = str.substring(0, (str.length() - 1));
                                edittext.setText(str);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                        }
                    });
                    zero.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "0";
                            edittext.setText(str);
                            edittext.setSelection(str.length());
                            mDialingFeedback.giveFeedback(ToneGenerator.TONE_DTMF_0);
                            CSCallsObj.sendDTMF(myCallId,"0",500,100);
                        }
                    });

                    one.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "1";
                            edittext.setText(str);
                            edittext.setSelection(str.length());
                            mDialingFeedback.giveFeedback(ToneGenerator.TONE_DTMF_1);
                            CSCallsObj.sendDTMF(myCallId,"1",500,100);

                        }
                    });
                    two.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "2";
                            edittext.setText(str);
                            edittext.setSelection(str.length());
                            mDialingFeedback.giveFeedback(ToneGenerator.TONE_DTMF_2);
                            CSCallsObj.sendDTMF(myCallId,"2",500,100);

                        }
                    });
                    three.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "3";
                            edittext.setText(str);
                            edittext.setSelection(str.length());
                            mDialingFeedback.giveFeedback(ToneGenerator.TONE_DTMF_3);
                            CSCallsObj.sendDTMF(myCallId,"3",500,100);

                        }
                    });
                    four.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "4";
                            edittext.setText(str);
                            edittext.setSelection(str.length());
                            mDialingFeedback.giveFeedback(ToneGenerator.TONE_DTMF_4);
                            CSCallsObj.sendDTMF(myCallId,"4",500,100);

                        }
                    });
                    five.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "5";
                            edittext.setText(str);
                            edittext.setSelection(str.length());
                            mDialingFeedback.giveFeedback(ToneGenerator.TONE_DTMF_5);
                            CSCallsObj.sendDTMF(myCallId,"5",500,100);

                        }
                    });
                    six.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "6";
                            edittext.setText(str);
                            edittext.setSelection(str.length());
                            mDialingFeedback.giveFeedback(ToneGenerator.TONE_DTMF_6);
                            CSCallsObj.sendDTMF(myCallId,"6",500,100);

                        }
                    });
                    seven.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "7";
                            edittext.setText(str);
                            edittext.setSelection(str.length());
                            mDialingFeedback.giveFeedback(ToneGenerator.TONE_DTMF_7);
                            CSCallsObj.sendDTMF(myCallId,"7",500,100);

                        }
                    });
                    eight.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "8";
                            edittext.setText(str);
                            edittext.setSelection(str.length());
                            mDialingFeedback.giveFeedback(ToneGenerator.TONE_DTMF_8);
                            CSCallsObj.sendDTMF(myCallId,"8",500,100);

                        }
                    });
                    nine.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "9";
                            edittext.setText(str);
                            edittext.setSelection(str.length());
                            mDialingFeedback.giveFeedback(ToneGenerator.TONE_DTMF_9);
                            CSCallsObj.sendDTMF(myCallId,"9",500,100);

                        }
                    });
                    plus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "*";
                            edittext.setText(str);
                            edittext.setSelection(str.length());
                            mDialingFeedback.giveFeedback(ToneGenerator.TONE_DTMF_B);
                            CSCallsObj.sendDTMF(myCallId,"*",500,100);

                        }
                    });
                    hash.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "#";
                            edittext.setText(str);
                            edittext.setSelection(str.length());
                            mDialingFeedback.giveFeedback(ToneGenerator.TONE_DTMF_A);
                            CSCallsObj.sendDTMF(myCallId,"#",500,100);

                        }
                    });


                    edittext.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            LOG.info(TAG1, "Yes on touch up:" + edittext.getText().toString());
                            try {
                                edittext.setSelection(edittext.getText().length());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {


                        }
                    });


                    if (dialogView != null)
                        dialogView.show();

                }
            });
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }


    /**
     * This method is used for get bitmap image
     *
     * @param phoneNumber get image
     * @return which returns bitmap
     */
    public Bitmap getIMAGEPhoto(String phoneNumber) {
        Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Uri photoUri = null;
        ContentResolver cr = PSTNCallActivity.this.getContentResolver();

        Cursor contact = null;
        try {
            contact = cr.query(phoneUri,
                    new String[]{ContactsContract.Contacts._ID}, null, null,
                    null);
            if (contact != null)
                if (contact.moveToFirst()) {
                    long userId = contact.getLong(contact
                            .getColumnIndex(ContactsContract.Contacts._ID));
                    photoUri = ContentUris.withAppendedId(
                            ContactsContract.Contacts.CONTENT_URI, userId);


                } else {

                    return null;
                }
            if (photoUri != null) {
                InputStream input = ContactsContract.Contacts
                        .openContactPhotoInputStream(cr, photoUri, true);
                if (input != null) {
                    return BitmapFactory.decodeStream(input);
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (contact != null)
                contact.close();
        }

        return null;
    }

    /**
     * This method is used for get country name
     *
     * @param code_num this is used for get country name
     */
    public void getCountryName(String code_num) {
        LOG.info("getCountryName: " + code_num);
        code_num = code_num.replaceAll("[^0-9]", "");
        if (code_num != null && code_num.length() > 3) {
            code_num = code_num.substring(0, 3);
        }

        Locale obj = new Locale("", getTwoCharCountryCode(code_num));
        String countryName = obj.getDisplayCountry();
        if (countryName.equals("") && code_num.length() > 2) {
            obj = new Locale("", getTwoCharCountryCode(code_num.substring(0, 2)));
            countryName = obj.getDisplayCountry();
        }
        if (countryName.equals("") && code_num.length() > 1) {
            obj = new Locale("", getTwoCharCountryCode(code_num.substring(0, 1)));
            countryName = obj.getDisplayCountry();
        }
        if (!countryName.equals("")) {
            mPSTNCountryTv.setText(countryName);

        } else {
            mPSTNCountryTv.setText("");
        }


    }


    /**
     * This method is used for get country code
     *
     * @param code this is used for get country code
     * @return this is used for two char country code
     */
    private String getTwoCharCountryCode(String code) {
        if (!code.contains("+")) {
            code = "+" + code;
        }
        for (Map.Entry<String, String> item : ZoneSelectActivity.country2Phone.entrySet()) {
            if (item.getValue().equalsIgnoreCase(code)) {
                return item.getKey();
            }


        }
        return "";
    } public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
               // stopringbacktone();
                LOG.info("PSTN Call back action " + intent.getAction().toString());
                if (myCallId.equals(intent.getStringExtra("callid"))) {
                    //stopringbacktone();
                    if (intent.getAction().equals(CSEvents.CSCALL_CALLENDED)) {
                        LOG.info("onReceive: Callend reason " + intent.getStringExtra("endReason").toString());
                        callEndReason = intent.getStringExtra("endReason");
                        reconnecting.setVisibility(View.GONE);
                        if (callEndReason.equalsIgnoreCase("No Credit")) {
                            mPreferenceProvider.setPrefboolean(PreferenceProvider.NEED_TO_SHOW_CALL_END_REASON, true);
                        }
                        if (intent.getStringExtra("endReason").toString().equals(CSConstants.UserBusy)) {
                            showStreamStoppedAlert("CallEnded! UserBusy!");
                            mTimerTv.setText("CallEnded! UserBusy!");
                        } else {
                            showStreamStoppedAlert("CallEnded");
                            mTimerTv.setText("CallEnded");
                        }
                        // Toast.makeText(getApplicationContext(), callEndReason, Toast.LENGTH_SHORT).show();
                        //showAlertToUser(callEndReason, PSTNCallActivity.this);
                    } else if (intent.getAction().equals(CSEvents.CSCALL_NOANSWER)) {
                        String noAnswerString = "";

                        if (!isConnectingTone) {
                            noAnswerString = "unreachable";
                            mTimerTv.setText("unreachable");
                        } else {
                            noAnswerString = "NoAnswer";
                            mTimerTv.setText("NoAnswer");
                        }
                        reconnecting.setVisibility(View.GONE);
                        showStreamStoppedAlert("NoAnswer");
                        // Toast.makeText(context, "NoAnswer", Toast.LENGTH_SHORT).show();
                      //  showAlertToUser(noAnswerString, PSTNCallActivity.this);

                    } else if (intent.getAction().equals(CSEvents.CSCALL_SESSION_IN_PROGRESS)) {
                        //183 for session in progress
                        isConnectingTone = true;
                        mTimerTv.setText("Ringing");
                        reconnecting.setVisibility(View.GONE);


                    } else if (intent.getAction().equals(CSEvents.CSCALL_CALLANSWERED)) {
                        PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
                        pf.setPrefboolean("inCallSuccess", true);
                        reconnecting.setVisibility(View.GONE);
                        LOG.info(TAG1, "Call Answer Success");
                        mCallingGifImg.setVisibility(View.GONE);
                        mTvDuration.setVisibility(View.VISIBLE);
                        mTvDuration.setText("In Call");
                        //  mTimerTv.setText("");
                        mMuteButtonTv.setEnabled(true);
                        mDTMFButtonTv.setEnabled(true);
                        mHoldButtonTv.setEnabled(true);
                        mContactsButtonTv.setEnabled(true);
                        LOG.info(TAG1, "Call Answer Success after");
                        mTimerTv.setText("Connecting..");
                        mTimerHandler.postDelayed(mTimerRunnable, mTimerDelay);
                        //stopringbacktone();
                    } else if (intent.getAction().equals(CSEvents.CSCALL_NOMEDIA)) {
                        showStreamStoppedAlert("NoMedia! CallEnded!");
                        mTimerTv.setText("NoMedia! CallEnded!");
                        reconnecting.setVisibility(View.GONE);
                    } else if (intent.getAction().equals(CSEvents.CSCALL_RINGING)) {
                        boolean iscallwaiting = intent.getBooleanExtra("iscallwaiting", false);
                        isConnectingTone = true;
                        reconnecting.setVisibility(View.GONE);
                        if (iscallwaiting) {
                            mTimerTv.setText("User busy");
                        } else {
                            mTimerTv.setText("Ringing");
                        }
                        //playringbacktone(false);
                      //  playringbacktone(iscallwaiting);
                        //stopConnectingTone();
                    } else if (intent.getAction().equals(CSEvents.CSCALL_MEDIACONNECTED)) {
                        mTvDuration.setVisibility(View.VISIBLE);
                        reconnecting.setVisibility(View.GONE);
                        if (!isMediaConnected && !getIntent().getBooleanExtra("isinitiatior", false)) {
                            isMediaConnected = true;
                            mTimerHandler.postDelayed(mTimerRunnable, mTimerDelay);
                        }
                        // stopringbacktone();
                        // stopConnectingTone();
                        mMuteButtonTv.setEnabled(true);
                        mDTMFButtonTv.setEnabled(true);
                        mHoldButtonTv.setEnabled(true);
                        if (isMediaDisconnected) {
                            isMediaDisconnected = false;
                            mTimerHandler.postDelayed(mTimerRunnable, mTimerDelay);
                            mTvDuration.setText("In Call");
                        }
                    } else if (intent.getAction().equals(CSEvents.CSCALL_MEDIADISCONNECTED)) {
                        if (isNetworkErrorReceived) {
                            mTvDuration.setVisibility(View.VISIBLE);
                            reconnecting.setVisibility(View.GONE);
                            mTimerHandler.removeCallbacks(mTimerRunnable);
                            isNetworkErrorReceived = false;
                            isMediaDisconnected = true;
                        }
                    } else if (intent.getAction().equals(CSEvents.CSCALL_HOLD_UNHOLD_RESPONSE)) {

                        if (myCallId.equals(intent.getStringExtra("callid"))) {
                            boolean ishold_sdk = intent.getBooleanExtra("hold", false);

                            if (!ishold_sdk) {


                            }
                        }
                    }


                }
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    isNetworkErrorReceived = true;
                    reconnecting.setVisibility(View.VISIBLE);
                    // Toast.makeText(getApplicationContext(), "NetWork Error", Toast.LENGTH_SHORT).show();
                    //  showAlertToUser("NetWork Error ", PSTNCallActivity.this);

                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


/*
    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                LOG.info("PSTN Call back action " + intent.getAction().toString());
                if (myCallId.equals(intent.getStringExtra("callid"))) {

                    if (intent.getAction().equals(CSEvents.CSCALL_CALLENDED)) {
                        reconnecting.setVisibility(View.GONE);
                        LOG.info("onReceive: Callend reason " + intent.getStringExtra("endReason").toString());
                        callEndReason = intent.getStringExtra("endReason");

                        if (callEndReason.equalsIgnoreCase("No Credit")) {
                            mPreferenceProvider.setPrefboolean(PreferenceProvider.NEED_TO_SHOW_CALL_END_REASON, true);
                        }
                        if (intent.getStringExtra("endReason").toString().equals(CSConstants.UserBusy)) {
                            showStreamStoppedAlert("callEnded! UserBusy!");
                            mTimerTv.setText("callEnded! UserBusy!");
                        } else {
                            showStreamStoppedAlert("callEnded");
                            mTimerTv.setText("callEnded");
                        }

                    } else if (intent.getAction().equals(CSEvents.CSCALL_NOANSWER)) {
                        reconnecting.setVisibility(View.GONE);
                        showStreamStoppedAlert("NoAnswer");
                        mTimerTv.setText("NoAnswer");
                    } else if (intent.getAction().equals(CSEvents.CSCALL_CALLANSWERED)) {
                        PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
                        pf.setPrefboolean("inCallSuccess", true);
                        LOG.info(TAG1, "Call Answer Success");
                        mCallingGifImg.setVisibility(View.GONE);
                        reconnecting.setVisibility(View.GONE);
                        mTvDuration.setVisibility(View.VISIBLE);
                        mTvDuration.setText("In Call");
                        mMuteButtonTv.setEnabled(true);
                        mDTMFButtonTv.setEnabled(true);
                        mHoldButtonTv.setEnabled(true);
                        LOG.info(TAG1, "Call Answer Success after");
                        mTimerHandler.postDelayed(mTimerRunnable, mTimerDelay);
                    } else if (intent.getAction().equals(CSEvents.CSCALL_NOMEDIA)) {
                        reconnecting.setVisibility(View.GONE);
                        showStreamStoppedAlert("NoMedia! CallEnded!");
                        mTimerTv.setText("NoMedia! CallEnded!");
                    } else if (intent.getAction().equals(CSEvents.CSCALL_RINGING)) {
                        // isConnectingTone = true;
                        reconnecting.setVisibility(View.GONE);
                        mTimerTv.setText("Ringing");
                    } else if (intent.getAction().equals(CSEvents.CSCALL_SESSION_IN_PROGRESS)) {
                        mTimerTv.setText("In progress");

                        reconnecting.setVisibility(View.GONE);
                    } else if (intent.getAction().equals(CSEvents.CSCALL_MEDIACONNECTED)) {
                        mTvDuration.setVisibility(View.VISIBLE);
                        reconnecting.setVisibility(View.GONE);
                        mTvDuration.setVisibility(View.VISIBLE);
                        if (!getIntent().getBooleanExtra("isinitiatior", false)) {
                            mTimerHandler.postDelayed(mTimerRunnable, mTimerDelay);
                        }
                        // stopringbacktone();
                        // stopConnectingTone();
                        mMuteButtonTv.setEnabled(true);
                        mDTMFButtonTv.setEnabled(true);
                        mHoldButtonTv.setEnabled(true);
                        mHoldButtonTv.setEnabled(true);
                        if (isMediaDisconnected) {
                            isMediaDisconnected = false;
                            mTimerHandler.postDelayed(mTimerRunnable, mTimerDelay);
                            mTvDuration.setText("In Call");
                        }
                    } else if (intent.getAction().equals(CSEvents.CSCALL_MEDIADISCONNECTED)) {
                        if (isNetworkErrorReceived) {
                            mTvDuration.setVisibility(View.VISIBLE);
                            reconnecting.setVisibility(View.VISIBLE);
                            mTimerHandler.removeCallbacks(mTimerRunnable);
                            isNetworkErrorReceived = false;
                            isMediaDisconnected = true;
                            mTvDuration.setText("Reconnecting..");
                        }
                    } else if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                        isNetworkErrorReceived = true;
                        if (isNetworkErrorReceived) {
                            mTvDuration.setVisibility(View.VISIBLE);
                            mTimerHandler.removeCallbacks(mTimerRunnable);
                            isNetworkErrorReceived = false;
                            isMediaDisconnected = true;
                            mTvDuration.setText("Reconnecting..");
                        }
                    }
                    if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                        isNetworkErrorReceived = true;
                        reconnecting.setVisibility(View.VISIBLE);
                        // Toast.makeText(getApplicationContext(), "NetWork Error", Toast.LENGTH_SHORT).show();
                        //  showAlertToUser("NetWork Error ", PSTNCallActivity.this);

                    }
                }
                } catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }
*/


        MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

        @Override
        public void onResume() {
            super.onResume();

            try {
                shownotifiction = true;
                mDialingFeedback = new DialingFeedback(PSTNCallActivity.this, false);
                mDialingFeedback.resume();
                IntentFilter filter3 = new IntentFilter(CSEvents.CSCALL_NOANSWER);
                IntentFilter filter4 = new IntentFilter(CSEvents.CSCALL_CALLANSWERED);
                IntentFilter filter5 = new IntentFilter(CSEvents.CSCALL_NOMEDIA);
                IntentFilter filter6 = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
                IntentFilter filter7 = new IntentFilter(CSEvents.CSCALL_RINGING);
                IntentFilter filter8 = new IntentFilter(CSEvents.CSCALL_MEDIACONNECTED);
                IntentFilter filter9 = new IntentFilter(CSEvents.CSCALL_MEDIADISCONNECTED);
                IntentFilter filter10 = new IntentFilter("TerminateForSecondCall");
                IntentFilter filter11 = new IntentFilter(CSEvents.CSCALL_SEND_DTMF_TONE_RESPONSE);
                IntentFilter filter12 = new IntentFilter(CSEvents.CSCALL_HOLD_UNHOLD_RESPONSE);
                IntentFilter filter13 = new IntentFilter(CSEvents.CSCALL_SESSION_IN_PROGRESS);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter3);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter4);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter5);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter6);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter7);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter8);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter9);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter10);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter11);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter12);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter13);
                mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);


            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        @Override
        public void onPause() {
            super.onPause();
            mSensorManager.unregisterListener(this);
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            try {
                LOG.info("onDestroy: ondestory called");
                GlobalVariables.INCALL = false;
                mPreferenceProvider.setPrefboolean(getString(R.string.call_logs_incall_message), false);
                mPreferenceProvider.setPrefboolean(getString(R.string.play_video_call_pref_already_login), false);
                mPreferenceProvider.setPrefboolean("CallRunning", false);
                mPreferenceProvider.setPrefboolean("CallScreenStart", true);
                ContactsFragment.isContactClick = false;

                //notificationManager.cancel(notificationID);
                new ForeGroundServiceApis().stopCallService(getApplicationContext());

            /*try {
                runOnUiThread(new Runnable() {
                    public void run() {
                        notificationManager.cancelAll();
                        Utils.NotifyAppInBackground(getString(R.string.notification_title), HomeScreenActivity.status, "", "", 0, getApplicationContext());

                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }*/
                if (mPhoneConnectivityReceiver != null) {
                    mTelephonyManager.listen(mPhoneConnectivityReceiver,
                            PhoneStateListener.LISTEN_NONE);
                    mPhoneConnectivityReceiver = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }


        @Override
        public void onBackPressed() {
            //super.onBackPressed();
            try {
                showStreamStopAlert();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ;
            //return;
        }


        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {

                if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                    LOG.info("onSensorChanged: proximity ON");
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    params.screenBrightness = 0;
                    getWindow().setAttributes(params);
                    mBlankScreenLayout.setVisibility(View.VISIBLE);
                    mMainScreenLayout.setVisibility(View.GONE);

                } else {
                    LOG.info("onSensorChanged: proximity OFF");
                    WindowManager.LayoutParams params = getWindow().getAttributes();
                    params.screenBrightness = -1;
                    getWindow().setAttributes(params);
                    mBlankScreenLayout.setVisibility(View.GONE);
                    mMainScreenLayout.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        /**
         * This method will close the running call
         *
         * @param message
         * @return
         */
        public boolean showStreamStoppedAlert(final String message) {
            try {


                endTheCall();

                return true;
            } catch (Exception ex) {
                return false;
            }

        }

        @Override
        protected void attachBaseContext(Context base) {
            super.attachBaseContext(base);
            MultiDex.install(this);
        }

        /**
         * This method will show the popup dialog if user pressed back button while call running state
         *
         * @return
         */
        public boolean showStreamStopAlert() {
            try {
                if (!isFinishing()) {
                    android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(PSTNCallActivity.this);
                    successfullyLogin.setTitle("Confirmation");
                    successfullyLogin.setCancelable(false);
                    successfullyLogin.setMessage("End Call?");
                    successfullyLogin.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                    CSCallsObj.endPstnCall(mDestinationNumberToCall, myCallId);
                                    endTheCall();
                                }
                            });

                    successfullyLogin.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                }
                            });
                    if (!isFinishing()) {
                        successfullyLogin.show();
                    }
                }
                return true;
            } catch (Exception ex) {
                return false;
            }

        }


        /****  native GSM Call handling    **/

        private class ServicePhoneStateReceiver extends PhoneStateListener {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                LOG.info("onCallStateChanged called" + state);
                if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    isGSMCallCame = true;
                    isHoldEnabled = false;
                    mHoldButtonTv.setSelected(true);
                    mMuteButtonTv.setSelected(true);
                    CSCallsObj.holdPstnCall(myCallId, true);
                    //}
                } else {
                    if (isGSMCallCame) {
                        isGSMCallCame = false;
                        isHoldEnabled = true;
                        mHoldButtonTv.setSelected(false);
                        mMuteButtonTv.setSelected(false);
                        CSCallsObj.holdPstnCall(myCallId, false);
                    }
                }
                super.onCallStateChanged(state, incomingNumber);
            }

        }
    }

