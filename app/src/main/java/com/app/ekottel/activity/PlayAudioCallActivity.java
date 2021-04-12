package com.app.ekottel.activity;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import androidx.multidex.MultiDex;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.foregroundservices.ForeGroundServiceApis;
import com.app.ekottel.fragment.ContactsFragment;
import com.app.ekottel.utils.ChatConstants;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.ManageAudioFocus;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.dao.CSLocation;
import com.ca.wrapper.CSCall;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

//import com.ca.uiutils.UIActions;

public class PlayAudioCallActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mProximity;
    boolean shownotifiction = true;
    //NotificationManager notificationManager;app
    //private int notificationID;
    androidx.appcompat.app.AlertDialog.Builder successfullyLogin;
    AlertDialog dismisssuccessfullyLogin;
    int audiomodechangecount = 0;
    CSClient CSClientObj = new CSClient();
    long callDate = 0;
    final Handler h = new Handler();
    final Handler h1 = new Handler();
    private MediaPlayer mConnectingMediaPlayer;
    Runnable RunnableObj;
    MediaPlayer mp;
    String mycallid = "";
    boolean isdtmfenabled = true;
    String displayname = "";
    String destinationnumbettocall = "";
    private long mLastClickTime = 0;
    TextView reconnecting;
    TextView timer;
    TextView mTvEndAudioCallButton;
    int delay = 1000;
    boolean isaudioenabled = true;
    boolean isspeakerenabled = false;
    boolean mIsHoldEnabled = false;
    CircleImageView avatharButton;
    RelativeLayout blankscreen;
    LinearLayout mainscreen;
    CSCall CSCallsObj = new CSCall();
    private static Context context;
    ImageView muteButton;
    ImageView speakerButton;
    ImageView contactsButton;
    ImageView messageButton;
    ImageView dtmfButton;
    ImageView holdButton;
    TextView calledNumber;
    TextView contactName;
    TextView mTvCallAvailCreditText;
    TextView tvAppCountry;
    ImageView ivAppCountry;
    PreferenceProvider mPreferenceProvider;
    private AudioManager mAudioManager;
    private String TAG = "PlayAudioCallActivity";
    private ServicePhoneStateReceiver mPhoneConnectivityReceiver;
    private TelephonyManager mTelephonyManager;
    private boolean isMediaDisconnected = false;
    private boolean isNeteorkErrorReceived = false;
    private boolean isGSMCallCame = false;
    private boolean isRemoteMediaConnected = false;
    ManageAudioFocus manageAudioFocus = new ManageAudioFocus();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_call);
        try {
            LOG.info("onCreateCalled", "audio onCreateCalled");
            context = this;
            //notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            ChatConstants.IS_AUDIO_CALL_RUNNING = true;
            GlobalVariables.incallcount = GlobalVariables.incallcount + 1;
            GlobalVariables.answeredcallcount = GlobalVariables.answeredcallcount + 1;
            // Wake up the screen
            try {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } catch (Exception e) {
                e.printStackTrace();
            }
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            IntentFilter filter3 = new IntentFilter(CSEvents.CSCALL_NOANSWER);
            IntentFilter filter4 = new IntentFilter(CSEvents.CSCALL_CALLANSWERED);
            IntentFilter filter5 = new IntentFilter(CSEvents.CSCALL_NOMEDIA);
            IntentFilter filter6 = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter7 = new IntentFilter(CSEvents.CSCALL_RINGING);
            IntentFilter filter8 = new IntentFilter(CSEvents.CSCALL_MEDIACONNECTED);
            IntentFilter filter9 = new IntentFilter(CSEvents.CSCALL_MEDIADISCONNECTED);
            IntentFilter filter10 = new IntentFilter("TerminateForSecondCall");
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter3);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter4);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter5);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter6);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter7);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter8);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter9);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter10);
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            shownotifiction = true;
            callDate = CSClientObj.getTime();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            successfullyLogin = new androidx.appcompat.app.AlertDialog.Builder(PlayAudioCallActivity.this);
            mPreferenceProvider = new PreferenceProvider(getApplicationContext());
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            Utils.setAudioFocus(mAudioManager, mPreferenceProvider, true);


            dtmfButton = findViewById(R.id.tv_audio_call_keypad);
            muteButton =  findViewById(R.id.tv_audio_call_mute);
            speakerButton =  findViewById(R.id.tv_audio_call_speaker);
            contactsButton =  findViewById(R.id.tv_audio_call_contacts);
            messageButton = findViewById(R.id.tv_audio_call_message);
            holdButton = findViewById(R.id.tv_audio_call_hold);

            reconnecting = (TextView) findViewById(R.id.tv_audio_call_duration);

            contactName = (TextView) findViewById(R.id.tv_audio_call_caller_name);
            calledNumber = (TextView) findViewById(R.id.tv_audio_call_caller_number);
            timer = (TextView) findViewById(R.id.tv_audio_call_timer);
            mTvEndAudioCallButton = (TextView) findViewById(R.id.tv_audio_call_end_button);

            blankscreen = (RelativeLayout) findViewById(R.id.rl_audio_call_blankscreen);
            mainscreen = (LinearLayout) findViewById(R.id.ll_audio_call_mainscreen);

            avatharButton = (CircleImageView) findViewById(R.id.profile_image);
            reconnecting.setVisibility(View.GONE);

            mTvCallAvailCreditText = (TextView) findViewById(R.id.tv_audio_call_avail_credit_text);

            ivAppCountry = (ImageView) findViewById(R.id.iv_app_country_flag);
            tvAppCountry = (TextView) findViewById(R.id.tv_app_country_flag);
            tvAppCountry.setSelected(true);


            mPreferenceProvider.setPrefboolean("CallRunning", true);
            mPreferenceProvider.setPrefboolean(getString(R.string.call_logs_incall_message), true);
            mPreferenceProvider.setPrefboolean(getString(R.string.play_video_call_pref_already_login), true);

            mPreferenceProvider.setPrefboolean(getString(R.string.call_logs_pref_make_call), true);

            Typeface typeface = Utils.getTypeface(getApplicationContext());
            mTvEndAudioCallButton.setTypeface(typeface);

            GlobalVariables.INCALL=true;

            muteButton.setEnabled(false);
            messageButton.setEnabled(false);
            dtmfButton.setEnabled(false);
            contactsButton.setEnabled(false);
            holdButton.setEnabled(false);
            new CSCall().setPreferredAudioCodec(CSConstants.PreferredAudioCodec.opus);
            try {
                // phone must begin with '+'
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                String number;
                if (GlobalVariables.destinationnumbettocall != null && !GlobalVariables.destinationnumbettocall.startsWith("+")) {
                    number = "+" + GlobalVariables.destinationnumbettocall;
                } else {
                    number = GlobalVariables.destinationnumbettocall;
                }
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse(number, "");
                int countryCode = numberProto.getCountryCode();


                ivAppCountry.setVisibility(View.VISIBLE);


                String countrycode = String.valueOf(countryCode);

                countrycode = countrycode.replaceAll("[^0-9]", "");


                int resorceID = getResources().getIdentifier(getString(R.string.dialpad_tringy_message) + countrycode, getString(R.string.dialpad_drawable_message), getPackageName());

                ivAppCountry.setImageResource(resorceID);

                getcountryname(GlobalVariables.destinationnumbettocall);
            } catch (Exception e) {
                e.printStackTrace();
                ivAppCountry.setVisibility(View.GONE);

            }

            // Native GSM call state Receiver
            mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            // Telephony
            if (mPhoneConnectivityReceiver == null) {
                mPhoneConnectivityReceiver = new ServicePhoneStateReceiver();
                mTelephonyManager.listen(mPhoneConnectivityReceiver,
                        PhoneStateListener.LISTEN_CALL_STATE);
            }


            try {

                RunnableObj = new Runnable() {
                    int i = 0;

                    public void run() {
                        h.postDelayed(this, delay);
                        //LOG.info("printing at 1 sec");
                        PlayAudioCallActivity.this.runOnUiThread(new Runnable() {
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

                                timer.setText(minutes + ":" + seconds);
                            }
                        });
                    }
                };

                    IntentFilter filter1 = new IntentFilter(CSEvents.CSCALL_CALLENDED);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);

                    if (getIntent().getBooleanExtra("isinitiatior", false)) {

                    destinationnumbettocall = getIntent().getStringExtra("dstnumber");
                        mycallid = CSCallsObj.startVoiceCall(destinationnumbettocall,CSConstants.CALLRECORD.DONTRECORD,CallLocationActivity.callcontext,new CSLocation(CallLocationActivity.final_lat,CallLocationActivity.final_lng,CallLocationActivity.final_address));
                   // mycallid = CSCallsObj.startVoiceCall(destinationnumbettocall, CSConstants.CALLRECORD.DONTRECORD);
                    manageAudioFocus.requestAudioFocus(PlayAudioCallActivity.this, mycallid, destinationnumbettocall,false);


                } else {
                    destinationnumbettocall = getIntent().getStringExtra("srcnumber");
                    h.postDelayed(RunnableObj, delay);
                    mycallid = getIntent().getStringExtra("callid");

                    //String sdp = getIntent().getStringExtra("sdp");
                        CSCallsObj.answerVoiceCall(destinationnumbettocall, getIntent().getStringExtra("callid"),CallLocationActivity.callcontext,new CSLocation(CallLocationActivity.final_lat,CallLocationActivity.final_lng,CallLocationActivity.final_address));
                  //  CSCallsObj.answerVoiceCall(destinationnumbettocall, getIntent().getStringExtra("callid"));
                    //CSCallsObj.answerVoiceCall(destinationnumbettocall, getIntent().getStringExtra("callid"), CSConstants.CALLRECORD.DONTRECORD);
                }
                GlobalVariables.lastcallid = mycallid;
                mPreferenceProvider.setPrefString(PreferenceProvider.IN_CALL_CALLER_ID, mycallid);
                mPreferenceProvider.setPrefString(PreferenceProvider.RUNNING_CALL_TYPE, "Audio");
                mPreferenceProvider.setPrefString(PreferenceProvider.IN_CALL_NUMBER, destinationnumbettocall);
                mPreferenceProvider.setPrefboolean(PreferenceProvider.IS_CALL_RUNNING, true);

                //notificationID = Utils.NotifyCallInBackGound(getString(R.string.play_audio_in_audio_call), destinationnumbettocall, "", "AUDIO", 1, getApplicationContext());
                new ForeGroundServiceApis().startCallService(getApplicationContext(),destinationnumbettocall,getString(R.string.play_audio_in_audio_call),"AUDIO");
                getProfilePicture();

                Cursor ccfr = CSDataProvider.getContactCursorByNumber(destinationnumbettocall);
                if (ccfr.getCount() > 0) {
                    ccfr.moveToNext();
                    displayname = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
                }
                ccfr.close();
                if (!displayname.isEmpty() && displayname != null) {
                    contactName.setText(displayname);
                    calledNumber.setVisibility(View.VISIBLE);
                    calledNumber.setText(destinationnumbettocall);
                } else {
                    contactName.setText(destinationnumbettocall);
                    calledNumber.setVisibility(View.GONE);
                }


            } catch (Exception ex) {
                ex.printStackTrace();
            }


            mTvEndAudioCallButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        shownotifiction = false;
                        stopConnectingTone();
                        stopringbacktone();

                        CSCallsObj.endVoiceCall(destinationnumbettocall, mycallid, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
                        endTheCall();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            contactsButton.setOnClickListener(new View.OnClickListener() {
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

            messageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();
                        ChatConstants.IS_CHAT_SCREEN_ACTIVE = false;
                        Intent intent = new Intent(PlayAudioCallActivity.this, AppContactsActivity.class);
                        startActivity(intent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            dtmfButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {

                        if (isdtmfenabled) {
                            isdtmfenabled = false;
                            //mDTMFButtonTv.setBackgroundResource(R.drawable.call_bg_selector_prpl));
                            dtmfButton.setSelected(true);
                            showdialpad();
                        } else {
                            isdtmfenabled = true;
                            //mDTMFButtonTv.setBackgroundColor(Color.parseColor("#00000000"));
                            dtmfButton.setSelected(false);
                            dismisssuccessfullyLogin.cancel();

                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            successfullyLogin.setOnCancelListener(new DialogInterface.OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    isdtmfenabled = true;
                    dtmfButton.setSelected(false);
                    dismisssuccessfullyLogin.cancel();
                }
            });
            muteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {

                        if (!mycallid.equals("")) {
                            if (isaudioenabled) {
                                CSCallsObj.muteAudio(mycallid, true);
                                isaudioenabled = false;
                                muteButton.setSelected(true);
                            } else {
                                CSCallsObj.muteAudio(mycallid, false);
                                isaudioenabled = true;
                                muteButton.setSelected(false);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            holdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {

                        if (!mycallid.equals("")) {
                            CSCall csCall = new CSCall();
                            if (mIsHoldEnabled) {
                                csCall.holdAVCall(destinationnumbettocall, mycallid, false);
                                mIsHoldEnabled = false;
                                holdButton.setSelected(false);
                            } else {
                                csCall.holdAVCall(destinationnumbettocall, mycallid, true);
                                mIsHoldEnabled = true;
                                holdButton.setSelected(true);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            });


            speakerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (!mycallid.equals("")) {
                            if (isspeakerenabled) {
                                isspeakerenabled = false;
                                CSCallsObj.enableSpeaker(mycallid, false);
                                speakerButton.setSelected(false);
                            } else {
                                isspeakerenabled = true;
                                CSCallsObj.enableSpeaker(mycallid, true);
                                speakerButton.setSelected(true);
                            }
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    public boolean showdialpad() {
        try {


            runOnUiThread(new Runnable() {

                @Override
                public void run() {


                    LayoutInflater inflater = PlayAudioCallActivity.this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dtmf_dialog, null);
                    successfullyLogin.setView(dialogView);
                    TextView zero = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_0);
                    TextView one = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_1);
                    TextView two = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_2);
                    TextView three = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_3);
                    TextView four = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_4);
                    TextView five = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_5);
                    TextView six = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_6);
                    TextView seven = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_7);
                    TextView eight = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_8);
                    TextView nine = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_9);

                    TextView plus = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_plus);
                    TextView hash = (TextView) dialogView.findViewById(R.id.imageButton_dialpad_hash);


                    final EditText edittext = (EditText) dialogView.findViewById(R.id.editText1);
                    ImageView backarrow = (ImageView) dialogView.findViewById(R.id.imageView5);

                    Button endcall = (Button) dialogView.findViewById(R.id.dtmf_end_call_button);
                    Button hidekeyboard = (Button) dialogView.findViewById(R.id.dtmf_hide_keypad_button);

                    edittext.setFocusable(false);
                    edittext.setClickable(true);

                    endcall.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dismisssuccessfullyLogin.cancel();
                            mTvEndAudioCallButton.performClick();
                        }
                    });

                    hidekeyboard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dtmfButton.setSelected(false);
                            if (dismisssuccessfullyLogin != null) {
                                dismisssuccessfullyLogin.cancel();
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
                    backarrow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                String str = edittext.getText().toString();
                                str = str.substring(0, (str.length() - 1));
                                edittext.setText(str);
                            } catch (Exception ex) {
                            }
                        }
                    });
                    zero.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "0";
                            edittext.setText(str);
                        }
                    });

                    one.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "1";
                            edittext.setText(str);
                        }
                    });
                    two.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "2";
                            edittext.setText(str);
                        }
                    });
                    three.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "3";
                            edittext.setText(str);
                        }
                    });
                    four.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "4";
                            edittext.setText(str);
                        }
                    });
                    five.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "5";
                            edittext.setText(str);
                        }
                    });
                    six.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "6";
                            edittext.setText(str);
                        }
                    });
                    seven.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "7";
                            edittext.setText(str);
                        }
                    });
                    eight.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "8";
                            edittext.setText(str);
                        }
                    });
                    nine.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "9";
                            edittext.setText(str);
                        }
                    });
                    plus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "*";
                            edittext.setText(str);
                        }
                    });
                    hash.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String str = edittext.getText().toString();
                            str = str + "#";
                            edittext.setText(str);
                        }
                    });
                    dismisssuccessfullyLogin = successfullyLogin.show();

                }
            });
            return true;
        } catch (Exception ex) {
            return false;
        }

    }

    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                LOG.info("onreceiver intent actiuon " + intent.getAction().toString());

                if (mycallid.equals(intent.getStringExtra("callid"))) {
                    if (intent.getAction().equals(CSEvents.CSCALL_CALLENDED)) {
                        shownotifiction = false;
                        if (intent.getStringExtra("endReason").toString().equals(CSConstants.UserBusy)) {
                            showStreamStoppedAlert("callEnded! UserBusy!");
                        } else {
                            showStreamStoppedAlert("callEnded");
                        }

                    }
                   /* else if (intent.getAction().equals(CSEvents.CSCALL_MEDIADISCONNECTED)) {
                        shownotifiction = false;
                        timer.setText("Calling..");
                    }*/
                    else if (intent.getAction().equals(CSEvents.CSCALL_NOANSWER)) {
                        shownotifiction = false;
                        showStreamStoppedAlert("NoAnswer");
                        timer.setText("NoAnswer");
                    } else if (intent.getAction().equals(CSEvents.CSCALL_CALLANSWERED)) {
                        stopConnectingTone();
                        stopringbacktone();
                        h.postDelayed(RunnableObj, delay);
                    } else if (intent.getAction().equals(CSEvents.CSCALL_NOMEDIA)) {
                        shownotifiction = false;
                        showStreamStoppedAlert("NoMedia! CallEnded!");
                    } else if (intent.getAction().equals(CSEvents.CSCALL_RINGING)) {
                        boolean iscallwaiting = intent.getBooleanExtra("iscallwaiting", false);
                        if (iscallwaiting) {
                            timer.setText("User busy");
                        } else {
                            timer.setText("Ringing");
                        }

                        stopConnectingTone();
                        playringbacktone(iscallwaiting);

                    } else if (intent.getAction().equals(CSEvents.CSCALL_MEDIACONNECTED)) {
                        muteButton.setEnabled(true);
                        messageButton.setEnabled(true);
                        dtmfButton.setEnabled(true);
                        contactsButton.setEnabled(true);
                        holdButton.setEnabled(true);
                        isRemoteMediaConnected = true;
                        if (isMediaDisconnected) {
                            h.postDelayed(RunnableObj, delay);
                        }
                        reconnecting.setVisibility(View.GONE);
                        stopConnectingTone();
                        stopringbacktone();
                    } else if (intent.getAction().equals(CSEvents.CSCALL_MEDIADISCONNECTED)) {
                        if (isNeteorkErrorReceived) {
                            isMediaDisconnected = true;
                            isNeteorkErrorReceived = false;
                            reconnecting.setVisibility(View.VISIBLE);
                            h.removeCallbacks(RunnableObj);
                        }
                    } else if (intent.getAction().equals("TerminateForSecondCall")) {
                        mTvEndAudioCallButton.performClick();
                    }

                }
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    isNeteorkErrorReceived = true;
                    stopConnectingTone();
                    stopringbacktone();

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    public void onResume() {
        super.onResume();

        try {
            shownotifiction = true;

            mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            GlobalVariables.INCALL=false;
            //notificationManager.cancel(notificationID);
            new ForeGroundServiceApis().stopCallService(getApplicationContext());
           LOG.info("iscallplayaudio-->");
            mPreferenceProvider.setPrefboolean(getString(R.string.call_logs_incall_message), false);
            mPreferenceProvider.setPrefboolean(getString(R.string.play_video_call_pref_already_login), false);
            mPreferenceProvider.setPrefboolean("CallRunning", false);
            mPreferenceProvider.setPrefboolean("CallScreenStart", true);
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            ContactsFragment.isContactClick = false;

            boolean isBackgroundClear = mPreferenceProvider.getPrefBoolean("clearBackground");
            /*if (!isBackgroundClear) {
                try {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Utils.NotifyAppInBackground(getString(R.string.notification_title), HomeScreenActivity.status, "", "", 0, getApplicationContext());
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }*/
            if (mPhoneConnectivityReceiver != null) {
                mTelephonyManager.listen(mPhoneConnectivityReceiver,
                        PhoneStateListener.LISTEN_NONE);
                mPhoneConnectivityReceiver = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            Utils.setAudioFocus(mAudioManager, mPreferenceProvider, false);

            stopConnectingTone();
            stopringbacktone();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ChatConstants.IS_AUDIO_CALL_RUNNING = false;
    }


    @Override
    public void onBackPressed() {
        try {

            showstreamstopalert();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] < mProximity.getMaximumRange()) {
                //near
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                blankscreen.setVisibility(View.VISIBLE);
                mainscreen.setVisibility(View.GONE);

            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                blankscreen.setVisibility(View.GONE);
                mainscreen.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public boolean showStreamStoppedAlert(final String message) {
        try {

            stopConnectingTone();
            stopringbacktone();

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

    public boolean showstreamstopalert() {
        try {
            if (!isFinishing()) {
                android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(PlayAudioCallActivity.this);
                successfullyLogin.setTitle("Confirmation");
                successfullyLogin.setCancelable(false);
                successfullyLogin.setMessage("End Call?");
                successfullyLogin.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                shownotifiction = false;
                                stopConnectingTone();
                                stopringbacktone();
                                CSCallsObj.endVoiceCall(destinationnumbettocall, mycallid, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
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


    public void endTheCall() {
        try {
            GlobalVariables.INCALL=false;
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            h.removeCallbacks(RunnableObj);
            //notificationManager.cancel(notificationID);
            new ForeGroundServiceApis().stopCallService(getApplicationContext());

            stopConnectingTone();
            stopringbacktone();
            GlobalVariables.incallcount = GlobalVariables.incallcount - 1;
            GlobalVariables.answeredcallcount = GlobalVariables.answeredcallcount - 1;
            if (GlobalVariables.answeredcallcount < 0) {
                GlobalVariables.answeredcallcount = 0;
            }
            if (GlobalVariables.incallcount < 0) {
                GlobalVariables.incallcount = 0;
            }
            manageAudioFocus.abandonAudioFocus();
            finish();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void stopConnectingTone() {
        try {
            if (mConnectingMediaPlayer != null) {
                mConnectingMediaPlayer.stop();
                mConnectingMediaPlayer.reset();
                mConnectingMediaPlayer.release();
                mConnectingMediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playConnectingTone() {
        try {
            mConnectingMediaPlayer = MediaPlayer.create(this, R.raw.call_connecting);
            mConnectingMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    mConnectingMediaPlayer.start();
                }
            });
            mConnectingMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playringbacktone(boolean iscallwaiting) {

        if (getIntent().getBooleanExtra("isinitiatior", false)) {
            try {
                if (mp != null) {
                    if (mp.isPlaying())
                        mp.stop();
                    mp.reset();
                    mp.release();
                    mp = null;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                int resID = R.raw.standard_6_ringback;
                if (iscallwaiting) {
                    resID = R.raw.busy_remote_end;
                } else {
                    resID = R.raw.standard_6_ringback;
                }
                mp = MediaPlayer.create(PlayAudioCallActivity.this, resID);
                if (mp != null) {
                    mp.setLooping(true);
                    mp.start();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public void stopringbacktone() {
        try {
            if (mp != null) {
                mp.stop();
                mp.reset();
                mp.release();
                mp = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This is used for retrieve country name
     *
     * @param code_num this is used for get name of country
     */
    public void getcountryname(String code_num) {

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
                tvAppCountry.setText(countryName);


            } else {
                tvAppCountry.setText("");
            }
        }


    }

    /**
     * This is used for retrieve country code
     *
     * @param code This is used for get country code
     * @return which returns country code
     */
    private String getTwoCharCountryCode(String code) {
        if (!code.contains("+")) {
            code = "+" + code;
        }


        Log.i(TAG, "country: " + code);
        for (Map.Entry<String, String> item : ZoneSelectActivity.country2Phone.entrySet()) {
            if (item.getValue().equalsIgnoreCase(code)) {
                return item.getKey();
            }

        }
        return "";
    }

    /****  native GSM Call handling    **/

    private class ServicePhoneStateReceiver extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            LOG.info("onCallStateChanged called" + state);
            if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                isGSMCallCame = true;
                if (isRemoteMediaConnected) {
                    CSCall csCall = new CSCall();
                    csCall.holdAVCall(destinationnumbettocall, mycallid, true);
                    mIsHoldEnabled = true;
                    holdButton.setSelected(true);
                } else {
                    mTvEndAudioCallButton.performClick();
                }
                super.onCallStateChanged(state, incomingNumber);
            } else {
                if (isGSMCallCame) {
                    isGSMCallCame = false;
                    CSCall csCall = new CSCall();
                    csCall.holdAVCall(destinationnumbettocall, mycallid, false);
                    mIsHoldEnabled = false;
                    holdButton.setSelected(false);
                }

            }
        }
    }

    private void getProfilePicture() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, destinationnumbettocall);
                    if (cur.getCount() > 0) {
                        cur.moveToNext();
                        String picid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
                        Bitmap mybitmap = CSDataProvider.getImageBitmap(picid);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mybitmap != null) {
                                    avatharButton.setImageBitmap(mybitmap);
                                } else {
                                    Bitmap contactbitmap = Utils.getContactImage(getApplicationContext(),
                                            destinationnumbettocall);
                                    if (contactbitmap != null) {
                                        avatharButton.setImageBitmap(contactbitmap);
                                    } else {
                                        avatharButton.setImageResource(R.mipmap.ic_contact_profile_avatar);
                                    }
                                }
                            }
                        });

                    } else {
                        Bitmap contactbitmap = Utils.getContactImage(getApplicationContext(),
                                destinationnumbettocall);
                        if (contactbitmap != null) {
                            avatharButton.setImageBitmap(contactbitmap);
                        } else {
                            avatharButton.setImageResource(R.mipmap.ic_contact_avatar);
                        }
                    }
                    cur.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            }
        }).start();
    }
}
