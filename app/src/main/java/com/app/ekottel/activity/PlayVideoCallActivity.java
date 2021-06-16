package com.app.ekottel.activity;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;

import androidx.multidex.MultiDex;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.ca.views.CSPercentFrameLayout;
import com.ca.views.CSSurfaceViewRenderer;
import com.ca.wrapper.CSCall;
import com.ca.wrapper.CSDataProvider;

public class PlayVideoCallActivity extends Activity {
    private String destinationNumberToCall = "";
    private MediaPlayer mConnectingMediaPlayer;
    private Runnable ringBackRunnableObj;
    private final Handler h = new Handler();
    private MediaPlayer mMediaPlayer;
    int delay = 1000;
    boolean showNotification = true;
    private CSSurfaceViewRenderer mLocalCaptureView;
    private CSSurfaceViewRenderer mRemoteRenderView;

    boolean isAudioEnabled = true;
    final Handler handler = new Handler();
    Runnable RunnableObj;
    ImageView togglecamera;
    ImageButton sharestream;
    ImageButton closestream;
    String mycallid = "";
    ImageView muteButton;
    private ImageView mVideoMessagetv;
    ImageView chatButton;

    //int notificationid = 0;
    String displayname = "";

    TextView reconnecting;
    TextView callednumber;
    TextView calledname;
    TextView timer;
    CSPercentFrameLayout localRenderLayout;
    CSPercentFrameLayout remoteRenderLayout;
    CSCall CSCallsObj = new CSCall();

    PreferenceProvider mPreferenceProvider;

    ImageView mRlVideoToggle, rl_video_disable;

    boolean isVideoToggle = true;
    TextView mTvCallEnd;
    //private boolean isMediaDisconnected = false;
    private boolean isRemoteVideoConnected = false;
    private boolean isToggleVideoEnable = false;
    private AudioManager mAudioManager;
    private boolean isVideoEnagled = true;
    //private NotificationManager notificationManager;
    private String TAG = "PlayVideoCallActivity";
    private ServicePhoneStateReceiver mPhoneConnectivityReceiver;
    private TelephonyManager mTelephonyManager;
    private HeadSetIntentReceiver mHeadsetReceiver;
    private boolean isNetworkErrorReceived = false;
    private boolean isGSMCallRunning = false;
    private ManageAudioFocus manageAudioFocus = new ManageAudioFocus();
    private CallBackReceiver MainActivityReceiverObj = new CallBackReceiver();
    private LinearLayout mOptionsLayout;
    private RelativeLayout mParentLayout;
    private Handler mViewOptionsHandler = new Handler();
    private int mTimeInvervel = 5000;
    private Runnable viewOptionsRunnable = new Runnable() {
        @Override
        public void run() {
            mOptionsLayout.setVisibility(View.GONE);
            mTvCallEnd.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        try {
            //notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            GlobalVariables.incallcount = GlobalVariables.incallcount + 1;
            GlobalVariables.answeredcallcount = GlobalVariables.answeredcallcount + 1;

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            showNotification = true;
            reconnecting = (TextView) findViewById(R.id.tv_video_counter);
            callednumber = (TextView) findViewById(R.id.tv_video_number);
            calledname = (TextView) findViewById(R.id.tv_video_name);
            timer = (TextView) findViewById(R.id.tv_video_timer);
            mVideoMessagetv = findViewById(R.id.tv_video_message);
            mVideoMessagetv.setEnabled(false);
            mTvCallEnd = (TextView) findViewById(R.id.tv_video_callbutton);
            mRlVideoToggle = findViewById(R.id.rl_video_toggle);
            rl_video_disable =  findViewById(R.id.rl_video_disable);
            mParentLayout = findViewById(R.id.video_call_parent_layout);
            mOptionsLayout = findViewById(R.id.ll_info);
            mLocalCaptureView = (CSSurfaceViewRenderer) findViewById(R.id.local_video_view);
            mRemoteRenderView = (CSSurfaceViewRenderer) findViewById(R.id.remote_video_view);
            localRenderLayout = (CSPercentFrameLayout) findViewById(R.id.local_video_layout);
            remoteRenderLayout = (CSPercentFrameLayout) findViewById(R.id.remote_video_layout);
            muteButton = findViewById(R.id.tv_video_mute);
            chatButton =  findViewById(R.id.chatButton);
            // sep_hr = findViewById(R.id.sep_hr);
            //commentboxlayout = (LinearLayout) findViewById(R.id.commentboxlayout);
            Typeface typeface = Utils.getTypeface(getApplicationContext());
            togglecamera =  findViewById(R.id.imageButton1);
            sharestream = (ImageButton) findViewById(R.id.shareButton);
            reconnecting.setSelected(true);
            calledname.setSelected(true);
            callednumber.setSelected(true);
            timer.setSelected(true);

            closestream = (ImageButton) findViewById(R.id.closeButton);
            //send  = (ImageView) findViewById(R.id.send);
            //viewercount= (TextView) findViewById(R.id.viewercount);

            //comments = (ListView) findViewById(R.id.comments);
            //comments.setAdapter(ChannelCommentsAdapterObj);
            //comments.setSelection(ChannelCommentsAdapterObj.getCount()-1);
            sharestream.setVisibility(View.INVISIBLE);
            chatButton.setVisibility(View.INVISIBLE);
            Typeface text_font = Utils.getTypeface(getApplicationContext());
            mTvCallEnd.setTypeface(text_font);

            // commentboxlayout.setVisibility(View.INVISIBLE);
            reconnecting.setVisibility(View.GONE);

            mPreferenceProvider = new PreferenceProvider(getApplicationContext());
            mPreferenceProvider.setPrefboolean(getString(R.string.call_logs_incall_message), true);
            boolean isInCall = mPreferenceProvider.getPrefBoolean(getApplicationContext().getString(R.string.call_logs_incall_message));
            LOG.info("isInCall--"+isInCall);
            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            mHeadsetReceiver = new HeadSetIntentReceiver();
            registerReceiver(mHeadsetReceiver, filter);
            mPreferenceProvider.setPrefboolean("CallRunning", true);
            mPreferenceProvider.setPrefboolean(getString(R.string.play_video_call_pref_already_login), true);
            mPreferenceProvider.setPrefboolean(getString(R.string.call_logs_pref_make_call), true);
            GlobalVariables.INCALL=true;
            // Native GSM call state Receiver
            mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            // Telephony
            if (mPhoneConnectivityReceiver == null) {
                mPhoneConnectivityReceiver = new ServicePhoneStateReceiver();
                mTelephonyManager.listen(mPhoneConnectivityReceiver,
                        PhoneStateListener.LISTEN_CALL_STATE);
            }

            RunnableObj = new Runnable() {
                int i = 0;

                public void run() {
                    h.postDelayed(this, delay);
                    //LOG.info("printing at 1 sec");
                    PlayVideoCallActivity.this.runOnUiThread(new Runnable() {
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

            try {

                IntentFilter filter1 = new IntentFilter(CSEvents.CSCALL_CALLENDED);
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);

                if (getIntent().getBooleanExtra("isinitiatior", false)) {
                    playConnectingTone();
                    mViewOptionsHandler.postDelayed(viewOptionsRunnable, mTimeInvervel);
                    destinationNumberToCall = getIntent().getStringExtra("dstnumber");
                    mycallid = CSCallsObj.startVideoCall(destinationNumberToCall, mLocalCaptureView,
                            mRemoteRenderView,CSConstants.CALLRECORD.DONTRECORD,
                            CallLocationActivity.callcontext,new CSLocation(CallLocationActivity.final_lat,
                                    CallLocationActivity.final_lng,CallLocationActivity.final_address));
                   // mycallid = CSCallsObj.startVideoCall(destinationNumberToCall, mLocalCaptureView, mRemoteRenderView, CSConstants.CALLRECORD.DONTRECORD);
                    //mycallid = CSCallsObj.startVideoCall(destinationNumberToCall, mLocalCaptureView, mRemoteRenderView,localRenderLayout,remoteRenderLayout, CSConstants.CALLRECORD.DONTRECORD);
                    manageAudioFocus.requestAudioFocus(PlayVideoCallActivity.this, mycallid, destinationNumberToCall,false);
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    mLocalCaptureView.setLayoutParams(layoutParams);
                } else {
                    mViewOptionsHandler.postDelayed(viewOptionsRunnable, mTimeInvervel);
                    destinationNumberToCall = getIntent().getStringExtra("srcnumber");
                    h.postDelayed(RunnableObj, delay);
                    mycallid = getIntent().getStringExtra("callid");
                    CSCallsObj.answerVideoCall(destinationNumberToCall, getIntent().getStringExtra("callid"),
                            mLocalCaptureView, mRemoteRenderView,CallLocationActivity.callcontext,new
                                    CSLocation(CallLocationActivity.final_lat,CallLocationActivity.final_lng,
                                    CallLocationActivity.final_address));
                    //String sdp = getIntent().getStringExtra("sdp");
                    //CSCallsObj.answerVideoCall(destinationNumberToCall, getIntent().getStringExtra("callid"), mLocalCaptureView, mRemoteRenderView);
                    //CSCallsObj.answerVideoCall(destinationNumberToCall, getIntent().getStringExtra("callid"), mLocalCaptureView, mRemoteRenderView,localRenderLayout,remoteRenderLayout, CSConstants.CALLRECORD.DONTRECORD);

                }

                GlobalVariables.lastcallid = mycallid;
                mPreferenceProvider.setPrefString(PreferenceProvider.IN_CALL_CALLER_ID, mycallid);
                mPreferenceProvider.setPrefString(PreferenceProvider.RUNNING_CALL_TYPE, "Video");
                mPreferenceProvider.setPrefString(PreferenceProvider.IN_CALL_NUMBER, destinationNumberToCall);
                mPreferenceProvider.setPrefboolean(PreferenceProvider.IS_CALL_RUNNING, true);
                //GlobalVariables.lastdestnationnumber = destinationNumberToCall;
                new CSCall().setPreferredAudioCodec(CSConstants.PreferredAudioCodec.opus);
                // displayname = destinationNumberToCall;
                Cursor ccfr = CSDataProvider.getContactCursorByNumber(destinationNumberToCall);
                if (ccfr.getCount() > 0) {
                    ccfr.moveToNext();
                    displayname = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
                }
                ccfr.close();
                if (displayname != null && !displayname.isEmpty()) {
                    callednumber.setVisibility(View.VISIBLE);
                    calledname.setText(displayname);
                    callednumber.setText(destinationNumberToCall);
                } else {
                    callednumber.setVisibility(View.GONE);
                    calledname.setText(destinationNumberToCall);
                }
                //notificationid = Utils.NotifyCallInBackGound(getString(R.string.play_video_in_video_call), destinationNumberToCall, "", "VIDEO", 2, getApplicationContext());
                new ForeGroundServiceApis().startCallService(getApplicationContext(), destinationNumberToCall, getString(R.string.play_video_in_video_call), "VIDEO");


            } catch (Exception ex) {
                ex.printStackTrace();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }


        mParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOptionsLayout.getVisibility() == View.GONE) {
                    mOptionsLayout.setVisibility(View.VISIBLE);
                    mTvCallEnd.setVisibility(View.VISIBLE);
                    mViewOptionsHandler.postDelayed(viewOptionsRunnable, mTimeInvervel);
                } else {
                    mOptionsLayout.setVisibility(View.GONE);
                    mTvCallEnd.setVisibility(View.GONE);
                    mViewOptionsHandler.removeCallbacks(viewOptionsRunnable);
                }

            }
        });

        rl_video_disable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mycallid.equals("")) {
                    if (isVideoEnagled) {
                        CSCallsObj.muteVideo(mycallid, true);
                        isVideoEnagled = false;
                        //mMuteButtonTv.setSelected(true);
                        rl_video_disable.setSelected(true);
                        //mMuteButtonTv.setBackgroundColor(Color.parseColor("#1A000000"));
                        //CSCallsObj.enableOrDisableSpeakerInVideoCall(false);
                    } else {
                        CSCallsObj.muteVideo(mycallid, false);
                        isVideoEnagled = true;
                        rl_video_disable.setSelected(false);
                        // mMuteButtonTv.setSelected(false);
                        //mMuteButtonTv.setBackgroundColor(Color.parseColor("#4E3164"));
                        //CSCallsObj.enableOrDisableSpeakerInVideoCall(true);
                    }
                }
            }
        });
        mVideoMessagetv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ChatConstants.IS_CHAT_SCREEN_ACTIVE = false;
                    Intent intent = new Intent(PlayVideoCallActivity.this, AppContactsActivity.class);
                    intent.putExtra("Incall","Incall");
                    startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        mRlVideoToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (Utils.isFrontCameraAvailable()) {
                        if (isVideoToggle) {
                            mRlVideoToggle.setSelected(true);
                            isVideoToggle = false;
                        } else {
                            mRlVideoToggle.setSelected(false);
                            isVideoToggle = true;
                        }
                        if (!mycallid.equals("")) {
                            CSCallsObj.toggleCamera(mycallid);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.Front_Camera_Not_Available), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!mycallid.equals("")) {
                        if (isAudioEnabled) {
                            CSCallsObj.muteAudio(mycallid, true);
                            isAudioEnabled = false;
                            //mMuteButtonTv.setSelected(true);
                            muteButton.setSelected(true);
                            //mMuteButtonTv.setBackgroundColor(Color.parseColor("#1A000000"));
                            //CSCallsObj.enableOrDisableSpeakerInVideoCall(false);
                        } else {
                            CSCallsObj.muteAudio(mycallid, false);
                            isAudioEnabled = true;
                            muteButton.setSelected(false);
                            // mMuteButtonTv.setSelected(false);
                            //mMuteButtonTv.setBackgroundColor(Color.parseColor("#4E3164"));
                            //CSCallsObj.enableOrDisableSpeakerInVideoCall(true);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        mTvCallEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showNotification = false;
                //onBackPressed();
                try {
                    CSCallsObj.endVideoCall(destinationNumberToCall, mycallid, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
                    endTheCall();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


    }


    public class CallBackReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                LOG.info("receiver intent-->"+intent.getAction());

                if (mycallid.equals(intent.getStringExtra("callid"))) {
                    if (intent.getAction().equals(CSEvents.CSCALL_CALLENDED)) {
                        showNotification = false;
                        if (intent.getStringExtra("endReason").equals(CSConstants.UserBusy)) {
                            showStreamStoppedAlert("callEnded! UserBusy!");
                            timer.setText("callEnded! UserBusy!");
                        } else {
                            showStreamStoppedAlert("callEnded");
                            timer.setText("callEnded");
                        }

                    } else if (intent.getAction().equals(CSEvents.CSCALL_NOANSWER)) {
                        showNotification = false;
                        showStreamStoppedAlert("NoAnswer");
                        timer.setText("NoAnswer");
                    } else if (intent.getAction().equals(CSEvents.CSCALL_CALLANSWERED)) {
                        stopringbacktone();
                        stopConnectingTone();
                        h.postDelayed(RunnableObj, delay);
                    } else if (intent.getAction().equals(CSEvents.CSCALL_NOMEDIA)) {
                        showNotification = false;
                        showStreamStoppedAlert("NoMedia! CallEnded!");
                        timer.setText("NoMedia! CallEnded!");
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
                        isRemoteVideoConnected = true;
                        mVideoMessagetv.setEnabled(true);
                        stopringbacktone();
                        stopConnectingTone();

                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) getResources().getDimension(R.dimen._120dp), (int) getResources().getDimension(R.dimen._120dp));
                        params.gravity = Gravity.TOP | Gravity.LEFT;
                        params.setMargins((int) getResources().getDimension(R.dimen._20dp), (int) getResources().getDimension(R.dimen._30dp), 0, 0);
                        mLocalCaptureView.setLayoutParams(params);

                        reconnecting.setVisibility(View.GONE);
                        /* after network connect show timer adding by deepika */
                        ////h.postDelayed(RunnableObj, delay);

                        if (!isToggleVideoEnable) {
                            isToggleVideoEnable = true;

                            mLocalCaptureView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    CSCallsObj.toggleVideocallViews(mycallid);
                                }
                            });
                        }

                    } else if (intent.getAction().equals(CSEvents.CSCALL_MEDIADISCONNECTED)) {
                        if (isNetworkErrorReceived) {
                          //  h.removeCallbacks(RunnableObj);
                            isNetworkErrorReceived = false;
                            //isMediaDisconnected = true;
                            reconnecting.setVisibility(View.VISIBLE);
                        }
                    } else if (intent.getAction().equals("TerminateForSecondCall")) {
                        closestream.performClick();
                    }

                }

                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    isNetworkErrorReceived = true;
                    stopringbacktone();
                    stopConnectingTone();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            showNotification = true;
            if (isRemoteVideoConnected) {
              //  rl_video_disable.performClick();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onPause() {

        if (rl_video_disable != null) {
          //  rl_video_disable.performClick();
        }
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        try {
            showstreamstopalert();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public boolean showStreamStoppedAlert(final String message) {
        try {
            try {
                stopringbacktone();
                stopConnectingTone();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            endTheCall();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
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
                android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(PlayVideoCallActivity.this);
                successfullyLogin.setTitle("Confirmation");
                successfullyLogin.setCancelable(false);
                successfullyLogin.setMessage("End Call?");
                successfullyLogin.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                try {
                                    showNotification = false;
                                    stopringbacktone();
                                    stopConnectingTone();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                CSCallsObj.endVideoCall(destinationNumberToCall, mycallid, CSConstants.E_UserTerminated, CSConstants.UserTerminated);
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

            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            h.removeCallbacks(RunnableObj);
            GlobalVariables.INCALL=false;
            stopConnectingTone();
            stopringbacktone();
            //notificationManager.cancel(notificationid);
            new ForeGroundServiceApis().stopCallService(getApplicationContext());

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

        }
    }

    public void playringbacktone(boolean iscallwaiting) {
        if (getIntent().getBooleanExtra("isinitiatior", false)) {
            try {
                if (mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying())
                        mMediaPlayer.stop();
                    mMediaPlayer.reset();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            } catch (Exception ex) {
            }


            ringBackRunnableObj = new Runnable() {
                public void run() {
                    //handler.postDelayed(this, 2000);
                    int resID = R.raw.standard_6_ringback;
                    if (iscallwaiting) {
                        resID = R.raw.busy_remote_end;
                    } else {
                        resID = R.raw.standard_6_ringback;
                    }

                    mMediaPlayer = MediaPlayer.create(PlayVideoCallActivity.this, resID);
                    if (mMediaPlayer != null) {
                        mMediaPlayer.setLooping(true);
                        mMediaPlayer.start();
                    }

                }
            };
            handler.postDelayed(ringBackRunnableObj, 2000);

        }
    }

    public void stopringbacktone() {
        //if(getIntent().getBooleanExtra("isinitiatior", false)) {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            handler.removeCallbacks(ringBackRunnableObj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //}
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            Log.e("onDestroy","onDestroy-->");
            GlobalVariables.INCALL=false;
            //notificationManager.cancel(notificationid);
            new ForeGroundServiceApis().stopCallService(getApplicationContext());
            LOG.info("iscallplayvideo-->");
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            mPreferenceProvider.setPrefboolean(getString(R.string.play_video_call_pref_already_login), false);
            mPreferenceProvider.setPrefboolean(getString(R.string.call_logs_incall_message), false);
            mPreferenceProvider.setPrefboolean("CallRunning", false);
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
            if (mHeadsetReceiver != null) {
                unregisterReceiver(mHeadsetReceiver);
            }

            AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_NORMAL);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /****  native GSM Call handling    **/

    private class ServicePhoneStateReceiver extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            LOG.info("onCallStateChanged called" + state);
            if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                isGSMCallRunning = true;
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setSpeakerphoneOn(false);
                if (isRemoteVideoConnected) {
                    CSCall csCall = new CSCall();
                    csCall.holdAVCall(destinationNumberToCall, mycallid, true);
                } else {
                    mTvCallEnd.performClick();
                }
            } else {
                if (isGSMCallRunning) {
                    isGSMCallRunning = false;
                    CSCall csCall = new CSCall();
                    AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    audioManager.setSpeakerphoneOn(true);
                    csCall.holdAVCall(destinationNumberToCall, mycallid, false);
                }
            }
            super.onCallStateChanged(state, incomingNumber);
        }

    }

    /**
     * This Class is used to handle the speaker when headSet is plugged or unplugged case
     */
    private class HeadSetIntentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        // LOG.debug("Headset is unplugged");
                        LOG.info("Headset is unplugged");
                        CSCallsObj.enableSpeaker(mycallid, true);
                        break;
                    case 1:
                        LOG.info("Headset is plugged");
                        CSCallsObj.enableSpeaker(mycallid, false);

                        break;
                    default:
                        LOG.info("I have no idea what the headset state is");
                }
            }
        }
    }

}
