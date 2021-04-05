package com.app.ekottel.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.ekottel.R;
import com.app.ekottel.adapter.ChatAdvancedAdapter;
import com.app.ekottel.utils.CallMethodHelper;
import com.app.ekottel.utils.ChatConstants;
import com.app.ekottel.utils.ChatMethodHelper;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.PermissionUtils;
import com.app.ekottel.utils.PreferenceProvider;

import com.app.ekottel.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.Utils.CSExplicitEvents;
import com.ca.dao.CSChatContact;
import com.ca.dao.CSChatLocation;
import com.ca.wrapper.CSChat;
import com.ca.wrapper.CSDataProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.app.ekottel.utils.ChatConstants.INTENT_CHAT_CONTACT_NUMBER;


public class ChatAdvancedActivity extends AppCompatActivity {
    private static final Logger LOG = LoggerFactory.getLogger("connectSdk");
    public static boolean isChatScreenActive;
    ProgressDialog mdialog1;
    RecyclerView mRecyclerview;
    String TAG = ChatAdvancedActivity.class.getSimpleName();
    private PreferenceProvider mPrefereceProvider;
    ChatAdvancedAdapter chatAdvancedAdapter;
    boolean chatfiledelete = false;
    private boolean isCameFromVideoCall = false;


    static String destination;
    boolean isGroupChat = false;

    private static Toolbar mToolbar;
    private TextView toolbarTitleTextView, toolbarSubTitleTextView;
    private EditText mChatMessageEditText, edittextsearchchatview;
    private RelativeLayout mAudioCallLayout, mVideoCallLayout;
    //private RelativeLayout mAttachmentsOverlayLayout;


    private static RelativeLayout chatoptionsview, searchchatview;
    private LinearLayout toolbar_title_layout_view, chatoptionsbacklayoutview;
    private static RelativeLayout chat_message_layoutview;
    private TextView mRecordTimerTv, mRecordHelpTextTv;
    private static TextView chat_selected_countview;
    private ImageView userImageView, searchupview, searchdownview;
    LinearLayout backview;
    private static ImageView chatdeleteview, chatcopyview, chatforwardview, backviewsearch;
    BroadcastReceiver hideTheTextViewReciever;
    private RelativeLayout rl_file_share, rl_image_share, rl_voice_transfer, rl_more_transfer;

    Runnable RunnableObj;
    final Handler h = new Handler();
    private boolean isRecordButtonLongPressed = false;
    int delay = 1000;
    private int startTime = 0;
    private String AudioSavePathInDevice = "";
    private MediaRecorder mAudioRecorder;
    public static Context mContext;

    private static final int IMAGE_CAPTURE_REQUEST_CODE = 1;
    private static final int VIDEO_CAPTURE_REQUEST_CODE = 3;
    private static final int GALLERY_REQUEST_CODE = 2;
    private static final int DOCUMENTS_REQUEST_CODE = 4;
    private static final int CONTACT_REQUEST_CODE = 5;
    private static final int AUDIO_REQUEST_CODE = 6;
    private static final int LOCATION_REQUEST_CODE = 7;
    private static final int FORWARD_MESSAGE_INTNET = 8;

    static ArrayList<Integer> selectedpositions = new ArrayList<Integer>();


    boolean onpaused = false;

    int chatsearchrefcnt = 0;
    String chatsearchrefstring = "";
    boolean chatsearchrefmode = false;

    CSChat CSChatObj = new CSChat();
    private long mLastClickTime;

    private int unReadCount = -1;
    public static int readCount;
    private String mFriendNumber;
    private String mFriendName;
    private TextView mReturnToVideoCallTv;
    private TextView mReturnToVideoCallTimer;
    private Runnable mTimeRunnable;
    public Handler mTimerHandler = new Handler();
    private long mTimerDelay = 1000;
    private boolean alertDialog;
    private TextView tv_file_transfer, tv_image_transfer, tv_voice_transfer, tv_more_transfer;
    private TextView mSendButton;
    private PopupWindow popupwindow_obj;
    private String mImagePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //LOG.info("TEST DELAY 3:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));
        setContentView(R.layout.advanced_activity_chat);
        mToolbar = (Toolbar) findViewById(R.id.chat_tool_bar);
        this.setSupportActionBar(mToolbar);
        mToolbar.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        mContext = this.getApplicationContext();

        Typeface typeface = Utils.getTypeface(getApplicationContext());


        //LOG.info("TEST DELAY 3 3:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));
        Log.e("onNewIntent chat", "");
        mRecyclerview = (RecyclerView) findViewById(R.id.conversation_recycler_view);
        mPrefereceProvider = new PreferenceProvider(getApplicationContext());


        tv_file_transfer = (TextView) findViewById(R.id.tv_file_transfer);
        tv_image_transfer = (TextView) findViewById(R.id.tv_image_transfer);
        tv_voice_transfer = (TextView) findViewById(R.id.tv_voice_transfer);
        tv_more_transfer = (TextView) findViewById(R.id.tv_more_transfer);

        rl_file_share = (RelativeLayout) findViewById(R.id.rl_file_share);
        rl_image_share = (RelativeLayout) findViewById(R.id.rl_image_share);
        rl_voice_transfer = (RelativeLayout) findViewById(R.id.rl_voice_share);
        rl_more_transfer = (RelativeLayout) findViewById(R.id.more_share_layout);
        mSendButton = (TextView) findViewById(R.id.send);

        tv_file_transfer.setTypeface(typeface);
        tv_image_transfer.setTypeface(typeface);
        tv_voice_transfer.setTypeface(typeface);
        tv_more_transfer.setTypeface(typeface);
        mSendButton.setTypeface(typeface);


        mChatMessageEditText = findViewById(R.id.chat_edittext);


        //mRecyclerView = findViewById(R.id.conversation_recycler_view);
        mRecordTimerTv = findViewById(R.id.recording_timer_tv);
        mRecordHelpTextTv = findViewById(R.id.record_help_text_tv);
        mVideoCallLayout = findViewById(R.id.video_call_return_layout);


        toolbarTitleTextView = findViewById(R.id.toolbar_title);
        toolbarSubTitleTextView = findViewById(R.id.toolbar_sub_title);
        userImageView = findViewById(R.id.user_image_view);
        backview = findViewById(R.id.toolbar_back_layout);
        toolbar_title_layout_view = findViewById(R.id.toolbar_title_layout);

        mToolbar = findViewById(R.id.chat_tool_bar);
        searchchatview = findViewById(R.id.lyt_search_box);
        backviewsearch = findViewById(R.id.back_arrow_view_search);
        searchupview = findViewById(R.id.search_up);
        searchdownview = findViewById(R.id.search_down);
        edittextsearchchatview = findViewById(R.id.edittextsearchchat);
        chat_message_layoutview = findViewById(R.id.conversation_layout);

        chatdeleteview = findViewById(R.id.chatdelete);
        chatcopyview = findViewById(R.id.chatcopy);
        chatforwardview = findViewById(R.id.chatforward);
        chat_selected_countview = findViewById(R.id.chat_selected_count);
        chatoptionsbacklayoutview = findViewById(R.id.chatoptionsbacklayout);
        chatoptionsview = findViewById(R.id.chatoptions_layout);
        mReturnToVideoCallTv = findViewById(R.id.video_call_return_tv);
        mReturnToVideoCallTimer = findViewById(R.id.video_call_timer);

        Intent i = getIntent();
        mFriendNumber = i.getStringExtra(INTENT_CHAT_CONTACT_NUMBER);
        mFriendName = i.getStringExtra(Constants.INTENT_CHAT_CONTACT_NAME);

        if (mFriendName == null) {
            Cursor contactcursor = CSDataProvider.getContactCursorByNumber(mFriendNumber);

            if (contactcursor.getCount() > 0) {
                contactcursor.moveToNext();
                mFriendName = contactcursor.getString(contactcursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
            }

            contactcursor.close();
        }
        isCameFromVideoCall = i.getBooleanExtra(Constants.IS_VIDEO_CALL_RUNNING, false);

        mSendButton.setText(getResources().getString(R.string.contact_invite));
        tv_image_transfer.setText(getResources().getString(R.string.chat_image_transfer));
        tv_file_transfer.setText(getResources().getString(R.string.chat_file_transfer));
        tv_voice_transfer.setText(getResources().getString(R.string.chat_voice_transfer));
        tv_more_transfer.setText(getResources().getString(R.string.icon_more));

        //registering the broadcast reciever
        chatEventReceiverObj = new ChatEventReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(CSEvents.CSCLIENT_NETWORKERROR);
        filter.addAction(CSEvents.CSCHAT_CHATUPDATED);
        filter.addAction(CSExplicitEvents.CSChatReceiver);
        filter.addAction(CSEvents.CSCHAT_GETPRESENCE_RESPONSE);
        filter.addAction(CSEvents.CSCHAT_UPLOADFILEDONE);
        filter.addAction(CSEvents.CSCHAT_UPLOADFILEFAILED);
        filter.addAction(CSEvents.CSCHAT_DOWNLOADFILEDONE);
        filter.addAction(CSEvents.CSCHAT_DOWNLOADFILEFAILED);
        filter.addAction(CSEvents.CSCLIENT_LOGIN_RESPONSE);
        filter.addAction(CSEvents.CSCHAT_DOWNLOADPROGRESS);
        filter.addAction(CSEvents.CSCHAT_UPLOADPROGRESS);
        filter.addAction(CSEvents.CSCHAT_ISTYPING);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(chatEventReceiverObj, filter);

        //LOG.info("TEST DELAY 4:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));

        ChatMethodHelper.setFileTrasferPathsHelper(this);
        //LOG.info("TEST DELAY 5:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));

        //setSupportActionBar(toolbar);
        selectedpositions.clear();
        //LOG.info("TEST DELAY 6:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));

        try {
            if (getIntent() != null)
                destination = getIntent().getStringExtra(INTENT_CHAT_CONTACT_NUMBER);
            isGroupChat = getIntent().getBooleanExtra("IS_GROUP", false);
        } catch (Exception e) {

        }
        setTitleName();
        //this method asks the user for sho notifications on lock screen permision in miui devices only
        ChatMethodHelper.showLockScreenNotificationsMIUI(this);


//        if (!destination.contains("@"))
//            destination = ChatMethodHelper.appendDomainToUsername(ChatAdvancedActivity.this, destination);

        //this vursor gives all the unread messages count
        Cursor ccr = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(destination);
        unReadCount = ccr.getCount();
        //this cursor gives all the messages count including all unread messages also
        Cursor allMessagesCursor = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination);

        mToolbar.getOverflowIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        if (allMessagesCursor.getCount() > 0 && unReadCount > 0) {
            readCount = allMessagesCursor.getCount() - unReadCount;
        } else {
            readCount = -1;
        }
        //LOG.info("TEST DELAY 7:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        //mRecyclerview.addItemDecoration(new DividerItemDecoration(ChatAdvancedActivity.this, RecyclerView.VERTICAL));
        //mRecyclerview.addItemDecoration(new MyDividerItemDecoration(ChatAdvancedActivity.this, DividerItemDecoration.VERTICAL, 36));

        DividerItemDecoration divider = new DividerItemDecoration(ChatAdvancedActivity.this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getBaseContext(), R.drawable.custom_divider));
        mRecyclerview.addItemDecoration(divider);
        chatAdvancedAdapter = new ChatAdvancedAdapter(ChatAdvancedActivity.this, allMessagesCursor, destination);
        mRecyclerview.setNestedScrollingEnabled(false);
        mRecyclerview.setLayoutManager(mLayoutManager);
        mRecyclerview.setItemAnimator(new DefaultItemAnimator());
        mRecyclerview.setAdapter(chatAdvancedAdapter);
        notifyDataSetChanged(true, false, "", true);


        loadProfilePicture();
        //LOG.info("TEST DELAY 10:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));

        //init data to the recyclerview
        initAdapterData();


        //LOG.info("TEST DELAY 11:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));
        hideTheTextViewReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mVideoCallLayout.setVisibility(View.GONE);
                mTimerHandler.removeCallbacks(mTimeRunnable);
                isCameFromVideoCall = false;
                invalidateOptionsMenu();
                unregisterReceiver(hideTheTextViewReciever);
            }
        };


        mTimeRunnable = new Runnable() {

            int i = Constants.videoCallTimeValue;

            public void run() {
                mTimerHandler.postDelayed(this, mTimerDelay);
                //Log.i(TAG,"printing at 1 sec");
                ChatAdvancedActivity.this.runOnUiThread(new Runnable() {
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

                        mReturnToVideoCallTimer.setText(minutes + ":" + seconds);
                    }
                });
            }
        };

        if (isCameFromVideoCall) {
            mReturnToVideoCallTv.setVisibility(View.VISIBLE);
            mVideoCallLayout.setVisibility(View.VISIBLE);
            registerReceiver(hideTheTextViewReciever, new IntentFilter(Constants.CLOSE_RETURN_TO_VIDEO_CALL_TEXT));

//            NotificationHelper notificationHelper = NotificationHelper.getInstance(getApplicationContext());
//
//            NotificationMethodHelper.cancelNotificationList(this);
//            if (notificationHelper != null) {
//                notificationHelper.cancelVideoCalls();
//            }
            mTimerHandler.postDelayed(mTimeRunnable, mTimerDelay);
        }

        backview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        mVideoCallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTimerHandler.removeCallbacks(mTimeRunnable);
                finish();
            }
        });
        //LOG.info("TEST DELAY 12:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));

        backviewsearch.setOnClickListener((v) -> {
            searchchatview.setVisibility(View.GONE);
            mToolbar.setVisibility(View.VISIBLE);
            chat_message_layoutview.setVisibility(View.VISIBLE);

            hideKeyboard();
            notifyDataSetChanged(true, false, "", true);
            chatsearchrefmode = false;
            chatsearchrefstring = "";
            chatsearchrefcnt = 0;

            List<String> numbers = new ArrayList<String>();
            numbers.add(destination);
            // LOG.info("getPresenceReq:" + destination);
            CSChatObj.getPresence(numbers);
            edittextsearchchatview.setText("");
        });
        //LOG.info("TEST DELAY 13:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));

        searchupview.setOnClickListener(v -> {
            handleSearch(1);
        });
        searchdownview.setOnClickListener(v -> {
            handleSearch(2);
        });
        //LOG.info("TEST DELAY 14:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));

        edittextsearchchatview.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                ChatAdvancedActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });
        //LOG.info("TEST DELAY 15:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));
        edittextsearchchatview.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Log.i(TAG, "search from keyboard");
                    chatsearchrefstring = edittextsearchchatview.getText().toString();
                    chatsearchrefmode = true;
                    chatsearchrefcnt = 0;
                    notifyDataSetChanged(false, true, edittextsearchchatview.getText().toString(), true);
                    handleSearch(1);
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });
        edittextsearchchatview.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence searchstring, int start, int before, int count) {
                Log.i(TAG, "searchstring:" + searchstring);
                if (searchstring != null && !searchstring.toString().equals("")) {
                    //notifyDataSetChangedForSearch(searchstring.toString());
                    chatsearchrefstring = searchstring.toString();
                    chatsearchrefmode = true;
                    chatsearchrefcnt = 0;
                    notifyDataSetChanged(false, true, searchstring.toString(), true);
                    //handleSearch(1);
                } else {
                    chatsearchrefmode = false;
                    chatsearchrefstring = "";
                    chatsearchrefcnt = 0;
                    notifyDataSetChanged(false, false, "", true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        mSendButton.setOnClickListener(v -> {
            String chatMessage = mChatMessageEditText.getText().toString().trim();
            if (!chatMessage.equals("")) {
                CSChatObj.sendMessage(destination, chatMessage, isGroupChat);
                updtateReadCount();
                mChatMessageEditText.setText("");
                notifyDataSetChanged(true, false, "", true);
            }
        });

        rl_file_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!PermissionUtils.checkReadExternalStoragePermission(ChatAdvancedActivity.this)) {
                    ActivityCompat.requestPermissions(ChatAdvancedActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                    return;
                }
                try {
                    Intent intent = new Intent(ChatAdvancedActivity.this, DocumentsActivity.class);
                    startActivityForResult(intent, DOCUMENTS_REQUEST_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        rl_more_transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LOG.info("onClick: more clicked");
                popupwindow_obj = dialog_Select(ChatAdvancedActivity.this, mRecyclerview);
                popupwindow_obj.showAsDropDown(v);
            }
        });

        rl_image_share.setOnClickListener(v -> {
            showGalleryOptions();
        });


        //LOG.info("TEST DELAY 18:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));


        RunnableObj = new Runnable() {
            public void run() {
                h.postDelayed(this, delay);
                //Log.i(TAG,"printing at 1 sec");
                ChatAdvancedActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        String minutes = "00";
                        String seconds = "00";
                        int x = startTime++;
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
                        mRecordTimerTv.setText(minutes + ":" + seconds);
                    }
                });
            }
        };
        //LOG.info("TEST DELAY 21:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));

        rl_voice_transfer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (isRecordButtonLongPressed) {
                        try {
                            h.removeCallbacks(RunnableObj);
                            if (mAudioRecorder != null)
                                mAudioRecorder.stop();
                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(AudioSavePathInDevice);
                            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            mmr.release();
                            Log.i(TAG, "File size " + duration);
                            if (Integer.parseInt(duration) > 1000) {
                                if (new File(AudioSavePathInDevice).exists()) {
                                    showConfirmationDialog();
                                } else {
                                    Toast.makeText(ChatAdvancedActivity.this, getString(R.string.media_not_available), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                File file = new File(AudioSavePathInDevice);
                                file.delete();
                            }


                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                        mChatMessageEditText.setVisibility(View.VISIBLE);
                        mRecordHelpTextTv.setVisibility(View.GONE);
                        mRecordTimerTv.setVisibility(View.GONE);
                        isRecordButtonLongPressed = false;
                    }
                }
                return false;
            }
        });


        rl_voice_transfer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                isRecordButtonLongPressed = true;
                getTheFileNameWithPath();
                MediaRecorderReady();
                try {
                    mChatMessageEditText.setVisibility(View.GONE);
                    mRecordHelpTextTv.setVisibility(View.VISIBLE);
                    mRecordTimerTv.setVisibility(View.VISIBLE);
                    mRecordTimerTv.setText("00" + ":" + "00");
                    startTime = 0;
                    h.postDelayed(RunnableObj, 0);

                    mAudioRecorder.prepare();
                    mAudioRecorder.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(ChatAdvancedActivity.this, getString(R.string.chat_screen_audio_start_message),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        rl_voice_transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), getString(R.string.record_help_text), Toast.LENGTH_SHORT).show();
            }
        });
        //LOG.info("TEST DELAY 22:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));

        mChatMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (mChatMessageEditText.getText().toString().length() == 1 || mChatMessageEditText.getText().toString().length() % 3 == 1) {
                    Log.i(TAG, "Sending typing request");
                    CSChatObj.sendIsTyping(destination, false, true);
                }

                if (s.length() > 0) {
                    mSendButton.setTextColor(ContextCompat.getColor(mContext, R.color.theme_color));
                } else {
                    mSendButton.setTextColor(ContextCompat.getColor(mContext, R.color.chat_send_button_normal));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        chatdeleteview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAlert();
            }

        });


        chatcopyview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    String final_mesage = "";
                    Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination);
                    for (int cursorposition : selectedpositions) {
                        cur.moveToPosition(cursorposition);
                        int chattype = cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
                        if (chattype == CSConstants.E_TEXTPLAIN || chattype == CSConstants.E_CONTACT || chattype == CSConstants.E_LOCATION) {
                            final_mesage = final_mesage + cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE)) + "\n";
                        }
                    }
                    final_mesage = final_mesage.trim();
                    cur.close();


                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", final_mesage);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(ChatAdvancedActivity.this, "Copied to clipboard", Toast.LENGTH_LONG).show();
                    chatoptionsbacklayoutview.performClick();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        });

        chatforwardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactIntent = new Intent(ChatAdvancedActivity.this, ContactShareActivity.class);
                startActivityForResult(contactIntent, FORWARD_MESSAGE_INTNET);

            }
        });
        //LOG.info("TEST DELAY 23:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));

        chatoptionsbacklayoutview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatoptionsview.setVisibility(View.GONE);
                mToolbar.setVisibility(View.VISIBLE);
                chat_message_layoutview.setVisibility(View.VISIBLE);
                selectedpositions.clear();
                chatAdvancedAdapter.clearselecteditems();
                notifyDataSetChanged(false, false, "", true);
            }

        });
//        toolbar_title_layout_view.setOnClickListener(view -> {
//            Intent intent = new Intent(mContext, NewContactDetailsActivity.class);
//            intent.putExtra(Constants.INTENT_CONTACT_DETAILS_NAME, mFriendName);
//            intent.putExtra(Constants.INTENT_CONTACT_DETAILS_NUMBER, ChatMethodHelper.removeDomainNameFromUserName(getApplicationContext(), mFriendNumber));
//            startActivity(intent);
//        });

        //sending the intent Data as message if exists
        sendIntentData();
        //LOG.info("TEST DELAY 23 23:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));

        try {
            int notificationid = Integer.parseInt(mFriendNumber.substring((mFriendNumber.length() / 2), mFriendNumber.length()).replace("+", ""));

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(notificationid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * this method is used to send the messages files(photos,files) shared from the other apps or in app
     */
    private void sendIntentData() {

        if (mPrefereceProvider.getPrefBoolean(PreferenceProvider.IS_FILE_SHARE_AVAILABLE)) {
            mPrefereceProvider.setPrefboolean(PreferenceProvider.IS_FILE_SHARE_AVAILABLE, false);

            boolean isFileAvailable = getIntent().getBooleanExtra("isShareFileAvailable", false);
            if (isFileAvailable) {
                String receivedFileType = getIntent().getStringExtra("receivedFileType");
                String receivedFilePath = getIntent().getStringExtra("receivedFilePath");
                if (receivedFileType.equals("text")) {
                    CSChatObj.sendMessage(mFriendNumber, receivedFilePath, false);

                } else if (receivedFileType.equals("audio")) {
                    String audioPath = ChatMethodHelper.getPath(getApplicationContext(),
                            Uri.parse(receivedFilePath));
                    CSChatObj.sendAudio(mFriendNumber, audioPath, false);

//                    new SendFile(Uri.parse(receivedFilePath), "audio").execute();

                } else if (receivedFileType.equals("image")) {
                    String filepath = ChatMethodHelper.getRealPathFromURI(getApplicationContext(), Uri.parse(receivedFilePath));
                    // LOG.info("filepath from share:" + filepath);
                    if (filepath.equals("")) {
                        Log.i(TAG, "Showing toast:" + filepath);
                        Toast.makeText(getApplicationContext(), "No Video Selected", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.i(TAG, "not showing toast:" + filepath);
                        CSChatObj.sendPhoto(mFriendNumber, filepath, false);

                    }
                } else if (receivedFileType.equals("video")) {
                    String filepath = ChatMethodHelper.getRealPathFromURI(getApplicationContext(), Uri.parse(receivedFilePath));

                    if (filepath.equals("")) {
                        Log.i(TAG, "Showing toast:" + filepath);
                        Toast.makeText(getApplicationContext(), "No Video Selected", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.i(TAG, "not showing toast:" + filepath);
                        CSChatObj.sendVideo(mFriendNumber, filepath, false);

                    }
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        getMenuInflater().inflate(R.menu.chatmenu, menu);
        if (isCameFromVideoCall) {
            MenuItem audioCallItem = menu.findItem(R.id.audiocall);
            MenuItem videMenuItem = menu.findItem(R.id.videocall);
            audioCallItem.setVisible(false);
            videMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.audiocall:
                String numberToPass = destination;

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return true;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if (GlobalVariables.INCALL) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.call_running_error_message), Toast.LENGTH_LONG).show();
                } else {
                    CallMethodHelper.processAudioCall(ChatAdvancedActivity.this, mFriendNumber, "PSTN");
                }
                return true;
            case R.id.videocall:
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return true;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (GlobalVariables.INCALL) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.call_running_error_message), Toast.LENGTH_LONG).show();

                } else {
                    CallMethodHelper.placeVideoCall(ChatAdvancedActivity.this, mFriendNumber);
                }

                return true;
            case R.id.menu_search:

                searchchatview.setVisibility(View.VISIBLE);
                searchchatview.requestFocus();


                mToolbar.setVisibility(View.GONE);
                chat_message_layoutview.setVisibility(View.GONE);

                if (chatoptionsview.getVisibility() == View.VISIBLE) {
                    chatoptionsbacklayoutview.performClick();
                }
                return true;
            case R.id.menu_chat_clear:
                showClearChatOptions();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public class ChatEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                //LOG.info("Yes Something receieved in RecentReceiver:" + intent.getAction().toString());
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    //  LOG.info("NetworkError receieved");
                    //to do toolbarSubTitleTextView.setText("");

                } else if (intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        List<String> numbers = new ArrayList<String>();
                        numbers.add(destination);
                        // LOG.info("getPresenceReq:" + destination);
                        CSChatObj.getPresence(numbers);
                    }
                } else if (intent.getAction().equals(CSEvents.CSCHAT_CHATUPDATED)) {
                    if (destination.equals(intent.getStringExtra("destinationnumber"))) {
                        notifyDataSetChanged(true, false, "", true);
                    }
                } else if (intent.getAction().equals(CSEvents.CSCHAT_ISTYPING)) {
                    // LOG.info("compare isTypingReq:" + destination);
                    //LOG.info("compare destinationnumber:" + intent.getStringExtra("destinationnumber"));
                    boolean istyping = intent.getBooleanExtra("istyping", false);

                    if (destination.equals(intent.getStringExtra("destinationnumber")) && istyping && intent.getStringExtra("destinationgroupid").equals("")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //LOG.info("runOnUiThread:");
                                final String subtitle = "online";
                                toolbarSubTitleTextView.setText("typing..");
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // LOG.info("runOnUiThread1:");

                                        toolbarSubTitleTextView.setText(subtitle);
                                    }
                                }, 1000);

                            }
                        });

                    }
                } else if (intent.getAction().equals(CSExplicitEvents.CSChatReceiver)) {
                    if (destination.equals(intent.getStringExtra("destinationnumber"))) {
                        //  LOG.info("Chat Receieved in ChatActivity");
                        notifyDataSetChanged(false, false, "", true);
                    }
                } else if (intent.getAction().equals(CSEvents.CSCHAT_GETPRESENCE_RESPONSE)) {
                    try {

                        if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {

                            if (destination.equals(intent.getStringExtra("presencenumber"))) {
                                String presencestatus = intent.getStringExtra("presencestatus");
                                long lastseentime = intent.getLongExtra("lastseentime", 0);
                                if (presencestatus.equals("ONLINE")) {
                                    toolbarSubTitleTextView.setText("online");
                                } else {
                                    if (DateUtils.isToday(lastseentime)) {
                                        toolbarSubTitleTextView.setText("active today at " + new SimpleDateFormat("hh:mm a").format(lastseentime));
                                        applyAnimationToSubTitle();
                                    } else if (ChatMethodHelper.isYesterday(lastseentime)) {
                                        toolbarSubTitleTextView.setText("active yesterday at " + new SimpleDateFormat("hh:mm a").format(lastseentime));
                                        applyAnimationToSubTitle();
                                    } else {
                                        toolbarSubTitleTextView.setText("active " + new SimpleDateFormat("dd-MM-yyyy hh:mm a").format(lastseentime));
                                        applyAnimationToSubTitle();
                                    }
                                }
                            }
                        }

                    } catch (Exception ex) {
                    }
                } else if (intent.getAction().equals(CSEvents.CSCHAT_UPLOADFILEDONE)) {
                    //notifyDataSetChanged(true,false,"");
                } else if (intent.getAction().equals(CSEvents.CSCHAT_UPLOADFILEFAILED)) {
                    chatAdvancedAdapter.uploadfailedchatids.add(intent.getStringExtra("chatid"));

                    notifyDataSetChanged(false, false, "", false);
                    /*
                    int position = getpositionfromchatid(intent.getStringExtra("chatid"));
                    if(position>=0) {
                        chatAdvancedAdapter.notifyItemChanged(position);
                    }*/

                    if (chatAdvancedAdapter.uploadfailedchatids.contains(intent.getStringExtra("chatid"))) {
                        chatAdvancedAdapter.uploadfailedchatids.remove(intent.getStringExtra("chatid"));
                    }
                    //notifyDataSetChanged(true,false,"");
                } else if (intent.getAction().equals(CSEvents.CSCHAT_UPLOADPROGRESS)) {
                    int percentage = intent.getIntExtra("transferpercentage", 0);

//                    LOG.info("uploadprogress percentage:" + percentage);
//                    LOG.info("chatid from activity upload:" + intent.getStringExtra("chatid"));

                    notifyDataSetChanged(false, false, "", false);
                    /*
                    int position = getpositionfromchatid(intent.getStringExtra("chatid"));
                    if(position>=0) {
                        chatAdvancedAdapter.notifyItemChanged(position);
                    }
                    */
                    //to do updateView(intent.getStringExtra("chatid"),CSEvents.CSCHAT_UPLOADPROGRESS,percentage);
                } else if (intent.getAction().equals(CSEvents.CSCHAT_DOWNLOADPROGRESS)) {
                    int percentage = intent.getIntExtra("transferpercentage", 0);
//                    LOG.info("Download percentage:" + percentage);
//                    LOG.info("chatid from activity download:" + intent.getStringExtra("chatid"));

                    //to do updateView(intent.getStringExtra("chatid"),CSEvents.CSCHAT_DOWNLOADPROGRESS,percentage);
                    notifyDataSetChanged(false, false, "", false);
                    /*
                    int position = getpositionfromchatid(intent.getStringExtra("chatid"));
                    if(position>=0) {
                        chatAdvancedAdapter.notifyItemChanged(position);
                    }
                    */

                } else if (intent.getAction().equals(CSEvents.CSCHAT_DOWNLOADFILEDONE)) {
                    String chatId = intent.getStringExtra("chatid");
                    Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_ID, chatId);
                    String filePath = "";
                    if (cur.getCount() > 0) {
                        cur.moveToNext();
                        filePath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));

                    }
                    toScannMediaFile(filePath);
                    //to do
                     /*int position = mChatList.getFirstVisiblePosition();
                    chatAdapter.changeCursor(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination));
                    chatAdapter.notifyDataSetChanged();
                    mChatList.setSelection(position);

*/
                    //updateView(intent.getStringExtra("chatid"),"downloadfiledone",0);
                } else if (intent.getAction().equals(CSEvents.CSCHAT_DOWNLOADFILEFAILED)) {
                    //to do
                    /* chatAdapter.changeCursor(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination));
                    chatAdapter.notifyDataSetChanged();
                    */

//					updateView(intent.getStringExtra("chatid"),"downloadfilefailed",0);


                    if (chatAdvancedAdapter.filedownloadinitiatedchatids.contains(intent.getStringExtra("chatid"))) {
                        chatAdvancedAdapter.filedownloadinitiatedchatids.remove(intent.getStringExtra("chatid"));
                    }
                    notifyDataSetChanged(false, false, "", false);
                    /*
                    int position = getpositionfromchatid(intent.getStringExtra("chatid"));
                    if(position>=0) {
                        chatAdvancedAdapter.notifyItemChanged(position);
                    }
                    */

                }
            } catch (Exception ex) {
            }
        }
    }


    ChatEventReceiver chatEventReceiverObj = new ChatEventReceiver();


    @Override
    public void onResume() {
        super.onResume();
        isChatScreenActive = true;
        initAdapterData();
        Constants.IS_IMPLICIT_ACTIVITY_OPEN = false;
    }


    public void initAdapterData() {
        try {
            // LOG.info("onNewIntent onResume manage chat");
            //LOG.info("TEST DELAY 24:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));

            onpaused = false;

            Cursor ccr = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(destination);
            int focus = ccr.getCount();
            //LOG.info("Read req test: unread count:" + focus);
            while (ccr.moveToNext()) {
                if (ccr.getInt(ccr.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_RECEIVED || ccr.getInt(ccr.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED_ACK || ccr.getInt(ccr.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED) {
                    LOG.info("Read req test: sending read request from app");
                    CSChatObj.sendReadReceipt(ccr.getString(ccr.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID)));
                }
            }
            ccr.close();
            LOG.info("Focus count:" + focus);

            // notifyDataSetChanged(true, false, "", true);

            try {
                h.removeCallbacks(RunnableObj);
                if (mAudioRecorder != null) {
                    mAudioRecorder.stop();
                    mChatMessageEditText.setVisibility(View.VISIBLE);
                    mRecordHelpTextTv.setVisibility(View.GONE);
                    mRecordTimerTv.setVisibility(View.GONE);
                    isRecordButtonLongPressed = false;
                }
            } catch (Exception e) {

                e.printStackTrace();
            }

            ChatConstants.CHAT_ACTIVITY_DESTINATION_NUMBER = destination;

            if (CSDataProvider.getLoginstatus()) {
                List<String> numbers = new ArrayList<String>();
                numbers.add(destination);
                LOG.info("getPresenceReq:" + destination);
                CSChatObj.getPresence(numbers);
            }


            //LOG.info("TEST DELAY 25:"+new SimpleDateFormat("hh:mm:ss.SSS a").format(new Date().getTime()));


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        if (chatoptionsview.getVisibility() == View.GONE && searchchatview.getVisibility() == View.GONE) {
            super.onBackPressed();
        } else {
            if (chatoptionsview.getVisibility() == View.VISIBLE) {
                chatoptionsbacklayoutview.performClick();
                return;
            }
            if (searchchatview.getVisibility() == View.VISIBLE) {
                backviewsearch.performClick();
                return;
            }
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        LOG.info("onNewIntent is called manage chat");
        try {
            //setIntent(intent);

            finish();
            startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(chatEventReceiverObj);
    }

    @Override
    public void onPause() {
        super.onPause();

        LOG.info("onNewIntent onPause manage chat");
        isChatScreenActive = false;
        onpaused = true;
        ChatConstants.CHAT_ACTIVITY_DESTINATION_NUMBER = "";

        try {
            h.removeCallbacks(RunnableObj);
            if (mAudioRecorder != null) {
                mAudioRecorder.stop();
                mChatMessageEditText.setVisibility(View.VISIBLE);
                mRecordHelpTextTv.setVisibility(View.GONE);
                mRecordTimerTv.setVisibility(View.GONE);
                isRecordButtonLongPressed = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (chatAdvancedAdapter != null) {
            chatAdvancedAdapter.releaseMediaPlayer();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        Log.i(TAG, "onActivityResult, RequestCode: " + requestCode);
        switch (requestCode) {

            case IMAGE_CAPTURE_REQUEST_CODE:
                if (mImagePath == null || mImagePath.equals("")) {
                    Toast.makeText(ChatAdvancedActivity.this, "No Image Seected", Toast.LENGTH_SHORT).show();
                } else {
                    LOG.info("File path:" + mImagePath);
                    LOG.info("orifinal File length:" + new File(mImagePath).length());
                    new SendFile(Uri.fromFile(new File(mImagePath)), "cameraimage").execute();

                }
                break;

            case GALLERY_REQUEST_CODE:
                if (data != null) {
                    boolean isImageFile = data.getBooleanExtra("isImageData", false);
                    if (isImageFile) {
                        ArrayList<String> returnedData = data.getStringArrayListExtra("selectedList");
                        for (int i = 0; i < returnedData.size(); i++) {
                            new SendFile(Uri.fromFile(new File(returnedData.get(i))), "gallaryimage").execute();
                        }
                    } else {
                        String filePath = data.getStringExtra("selectedList");
                        new SendFile(Uri.fromFile(new File(filePath)), "video").execute();
                    }
                }

                break;
            case VIDEO_CAPTURE_REQUEST_CODE:

                if (mImagePath != null) {
                    new SendFile(Uri.fromFile(new File(mImagePath)), "video").execute();
                }

                break;
            case DOCUMENTS_REQUEST_CODE:

                if (data != null) {
                    Log.i(TAG, "Sending document, FilePath: " + data.getData());
                    try {
                        //CSChatObj.sendDocument(destination, data.getData(), false);
                        String docfilePath = data.getStringExtra("DocumentsURL");
                        Uri uri = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", new File(docfilePath));
                        } else {
                            uri = Uri.fromFile(new File(docfilePath));
                        }
                        new SendFile(uri, "doc").execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ChatAdvancedActivity.this, getResources().getString(R.string.wrong_document), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChatAdvancedActivity.this, getResources().getString(R.string.wrong_document), Toast.LENGTH_SHORT).show();
                }

                break;

            case LOCATION_REQUEST_CODE:

                if (data != null) {
                    CSChatObj.sendLocation(destination, new CSChatLocation(data.getDoubleExtra(ChatConstants.INTENT_LOCATION_LATITUDE, 0.0), data.getDoubleExtra(ChatConstants.INTENT_LOCATION_LONGITUDE, 0.0), data.getStringExtra(ChatConstants.INTENT_LOCATION_ADDRESS)), false);
                    updtateReadCount();
                }

                break;

            case AUDIO_REQUEST_CODE:

                if (data != null && data.getData() != null) {
                    Uri selectedAudio = data.getData();
                    Log.i(TAG, "Selected Audio URI: " + selectedAudio);

                    //String audioPath = ChatMethodHelper.getPath(getApplicationContext(), selectedAudio);
                    //CSChatObj.sendAudio(destination, audioPath, false);
                    new SendFile(selectedAudio, "audio").execute();
                }

                break;
            case FORWARD_MESSAGE_INTNET:
                showProgressBar1();

                try {
                    if (data != null && resultCode == Activity.RESULT_OK) {
                        LOG.info("Got to be forwarded data");
                        List<String> numbers = data.getStringArrayListExtra("contactNumbers");
                        List<String> names = data.getStringArrayListExtra("contactNames");

                        Toast.makeText(ChatAdvancedActivity.this, "Forwarding..", Toast.LENGTH_SHORT).show();

                        Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination);
                        LOG.info("Got to be forwarded data selectedpositions.size():" + selectedpositions.size());

                        for (int cursorposition : selectedpositions) {
                            boolean showforward = true;
                            cur.moveToPosition(cursorposition);
                            int chattype = cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
                            String chatid = cur.getString(cur.getColumnIndex(CSDbFields.KEY_CHAT_ID));
                            String message = cur.getString(cur.getColumnIndex(CSDbFields.KEY_CHAT_MESSAGE));
                            String uploadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                            String downloadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                            int issender = cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_IS_SENDER));
                            int ismultidevice = cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE));
                            int downloadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                            //int uploadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));

                            Log.e(TAG, "ChatId :  " + chatid + "  message +++" + message);

                            if (chattype == CSConstants.E_TEXTPLAIN || chattype == CSConstants.E_CONTACT || chattype == CSConstants.E_LOCATION) {
                            } else {
                                if (issender == 1) {
                                    if (ismultidevice == 0) {
                                        if (!new File(uploadfilepath).exists()) {
                                            showforward = false;
                                        }
                                    } else {
                                        if (!new File(downloadfilepath).exists() || downloadpercentage != 100) {
                                            showforward = false;
                                        }
                                    }
                                } else {
                                    if (!new File(downloadfilepath).exists() || downloadpercentage != 100) {
                                        showforward = false;
                                    }
                                }
                            }

                            if (showforward) {
                                for (String number : numbers) {

                                    boolean isgroupmessage = false;

                                    LOG.info("Forwarding to:" + number);
                                    LOG.info("Forwarding to isgroupmessage:" + isgroupmessage);

                                    switch (chattype) {
                                        case CSConstants.E_TEXTPLAIN:
                                            CSChatObj.sendMessage(number, message, isgroupmessage);
                                            break;
                                        case CSConstants.E_TEXTHTML:
                                            CSChatObj.sendMessage(number, message, isgroupmessage);
                                            break;
                                        case CSConstants.E_LOCATION:
                                            CSChatLocation cschatlocation = CSChatObj.getLocationFromChatID(chatid);
                                            CSChatObj.sendLocation(number, cschatlocation, isgroupmessage);
                                            break;
                                        case CSConstants.E_IMAGE:
                                            if (issender == 1) {
                                                if (ismultidevice == 0) {
                                                    CSChatObj.sendPhoto(number, uploadfilepath, isgroupmessage);
                                                } else {
                                                    CSChatObj.sendPhoto(number, downloadfilepath, isgroupmessage);
                                                }
                                            } else {
                                                CSChatObj.sendPhoto(number, downloadfilepath, isgroupmessage);
                                            }
                                            break;
                                        case CSConstants.E_VIDEO:
                                            LOG.info("uploadfilepath:" + uploadfilepath);

                                            if (issender == 1) {
                                                if (ismultidevice == 0) {
                                                    CSChatObj.sendVideo(number, uploadfilepath, isgroupmessage);
                                                } else {
                                                    CSChatObj.sendVideo(number, downloadfilepath, isgroupmessage);
                                                }
                                            } else {
                                                CSChatObj.sendVideo(number, downloadfilepath, isgroupmessage);
                                            }
                                            break;
                                        case CSConstants.E_CONTACT:
                                            Log.e(TAG, "ChatId in contact:  " + chatid);
                                            CSChatContact cschatContact = CSChatObj.getContactFromChatID(chatid);
                                            Log.e(TAG, "cotact numbers list :  " + cschatContact.getNumbers().size());
                                            CSChatObj.sendContact(number, cschatContact, isgroupmessage);
                                            break;
                                        case CSConstants.E_DOCUMENT:
                                            if (issender == 1) {
                                                if (ismultidevice == 0) {
                                                    CSChatObj.sendDocument(number, uploadfilepath, isgroupmessage);
                                                } else {
                                                    CSChatObj.sendDocument(number, downloadfilepath, isgroupmessage);
                                                }
                                            } else {
                                                CSChatObj.sendDocument(number, downloadfilepath, isgroupmessage);
                                            }
                                            break;
                                        case CSConstants.E_AUDIO:
                                            if (issender == 1) {
                                                if (ismultidevice == 0) {
                                                    CSChatObj.sendAudio(number, uploadfilepath, isgroupmessage);
                                                } else {
                                                    CSChatObj.sendAudio(number, downloadfilepath, isgroupmessage);
                                                }
                                            } else {
                                                CSChatObj.sendAudio(number, downloadfilepath, isgroupmessage);
                                            }
                                            break;
                                    }
                                }

                            }

                        }
                        cur.close();
                        if (numbers.size() == 1) {
                            boolean isgroupmessage = false;
                            String number = numbers.get(0);
                            String name = names.get(0);

                            if (isgroupmessage) {
//                                Intent intent = new Intent(ChatAdvancedActivity.this, ChatActivityGroup.class);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                intent.putExtra("Sender", number);
//                                intent.putExtra("IS_GROUP", true);
//                                intent.putExtra("grpname", utils.getGroupname(number));
//                                MainActivity.context.startActivity(intent);
                            } else {
                                Intent intent = new Intent(getApplicationContext(), ChatAdvancedActivity.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(INTENT_CHAT_CONTACT_NUMBER, number);
                                intent.putExtra(Constants.INTENT_CHAT_CONTACT_NAME, name);
                                intent.putExtra(Constants.IS_VIDEO_CALL_RUNNING, false);
//                                intent.putExtra("Sender", number);
                                intent.putExtra("IS_GROUP", false);
                                getApplicationContext().startActivity(intent);
                            }
                            finish();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


                cancelProgressBar();

                chatoptionsbacklayoutview.performClick();
                break;

            case CONTACT_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Uri contactData = data.getData();
                    Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
                    if (cursor.moveToFirst()) {
                        String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        List<String> numbersList = new ArrayList<>();
                        List<String> labelsList = new ArrayList<>();

                        String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (hasPhone.equalsIgnoreCase("1"))
                            hasPhone = "true";
                        else
                            hasPhone = "false";

                        if (Boolean.parseBoolean(hasPhone)) {
                            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                            while (phones.moveToNext()) {
                                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                                String label = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
                                numbersList.add(phoneNumber);
//                                labelsList.add(label);
                            }
                            phones.close();
                        }
                        Log.i(TAG, "Sending Contact, Name: " + contactName + " ,numbers: " + numbersList + " ,Labels: " + labelsList);

                        CSChatContact contactObject = new CSChatContact(contactName, numbersList, labelsList);
                        boolean result = CSChatObj.sendContact(destination, contactObject, false);
                        updtateReadCount();

                    }
                }
                break;

        }
    }


    private boolean showGalleryOptions() {
        try {
            final Dialog dialog = new Dialog(ChatAdvancedActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.chat_popup);
            TextView tv_video = (TextView) dialog.findViewById(R.id.tv_profile_video);
            TextView tv_camera = (TextView) dialog.findViewById(R.id.tv_profile_camera);
            TextView tv_gallery = (TextView) dialog.findViewById(R.id.tv_profile_gallery);
            Typeface text_medium = Utils.getTypefaceMedium(getApplicationContext());
            tv_camera.setTypeface(text_medium);
            tv_gallery.setTypeface(text_medium);
            tv_video.setTypeface(text_medium);
            tv_video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    boolean callRunning = mPrefereceProvider.getPrefBoolean("CallRunning");
                    if (callRunning) {
                        Toast.makeText(ChatAdvancedActivity.this, getResources().getString(R.string.video_record_error_during_call), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }

                    if (PermissionUtils.checkCameraPermission(ChatAdvancedActivity.this) && PermissionUtils.checkReadExternalStoragePermission(ChatAdvancedActivity.this)) {
                        String directoryPath = Environment.getExternalStorageDirectory() + Constants.TRINGY_DIRECTORY;
                        String fileName = "VIDEO_"
                                + new SimpleDateFormat(Utils.getTimeFormatForChatScreen(getApplicationContext())).format(new Date())
                                + ".mp4";
                        File ImagePath = checkFileExistence(directoryPath, fileName);

                        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
                        //takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", ImagePath));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", ImagePath));
                        } else {
                            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(ImagePath));
                        }
                        startActivityForResult(takeVideoIntent, VIDEO_CAPTURE_REQUEST_CODE);
                        mImagePath = ImagePath.getAbsolutePath();
                        dialog.dismiss();
                    } else {
                        ActivityCompat.requestPermissions(ChatAdvancedActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                    }
                }
            });
            tv_camera.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    boolean callRunning = mPrefereceProvider.getPrefBoolean("CallRunning");
                    if (callRunning) {
                        Toast.makeText(ChatAdvancedActivity.this, getResources().getString(R.string.video_record_error_during_call), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }

                    if (PermissionUtils.checkCameraPermission(ChatAdvancedActivity.this) && PermissionUtils.checkReadExternalStoragePermission(ChatAdvancedActivity.this)) {
                        String directoryPath = Environment.getExternalStorageDirectory() + Constants.TRINGY_DIRECTORY;
                        String fileName = "IMG_"
                                + new SimpleDateFormat(Utils.getTimeFormatForChatScreen(getApplicationContext())).format(new Date())
                                + ".jpg";
                        File ImagePath = checkFileExistence(directoryPath, fileName);
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        //intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", ImagePath));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", ImagePath));
                        } else {
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(ImagePath));
                        }
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        startActivityForResult(intent, IMAGE_CAPTURE_REQUEST_CODE);
                        mImagePath = ImagePath.getAbsolutePath();
                        dialog.dismiss();
                    } else {
                        ActivityCompat.requestPermissions(ChatAdvancedActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                    }
                }
            });

            tv_gallery.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!PermissionUtils.checkReadExternalStoragePermission(ChatAdvancedActivity.this)) {
                        ActivityCompat.requestPermissions(ChatAdvancedActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                        return;
                    }
                    Intent i = new Intent(ChatAdvancedActivity.this, ShowImagesActivity.class);
                    //  i.setType("image/* video/*");
                    startActivityForResult(i, GALLERY_REQUEST_CODE);

                    dialog.dismiss();
                }
            });

            dialog.show();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void cancelProgressBar() {
        try {
            Log.i(TAG, "cancelProgressBar");
            //if(!chatsearchrefstring.equals("hi he")) {
            if (mdialog1 != null) {
                mdialog1.dismiss();
            }
            //}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void showProgressBar1() {
        try {
            if (mdialog1 != null && mdialog1.isShowing()) {
                return;
            }
            Log.i(TAG, "showProgressBar1");
            mdialog1 = new ProgressDialog(ChatAdvancedActivity.this);
            mdialog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //mdialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            //mdialog1.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            mdialog1.setMessage("Loading media...");
            mdialog1.setCancelable(false);
            //mdialog1.getWindow().setDimAmount(0.0f);
            mdialog1.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void cancelProgressBar1() {
        try {
            Log.i(TAG, "cancelProgressBar1");
            //if(!chatsearchrefstring.equals("hi he")) {
            if (mdialog1 != null) {
                mdialog1.dismiss();
            }
            //}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean showactivatelocationalert(String message) {
        try {
            AlertDialog.Builder successfullyLogin = new AlertDialog.Builder(ChatAdvancedActivity.this);
            successfullyLogin.setMessage(message);
            successfullyLogin.setPositiveButton("Settings",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    });
            successfullyLogin.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {

                        }
                    });

            successfullyLogin.show();

            return true;
        } catch (Exception ex) {
            return false;
        }
    }


    private void notifyDataSetChanged(boolean focustounreadorbottom, boolean insearchmode, String searchstring, boolean isfocusneeded) {
        try {
            Log.i(TAG, "chatAdvancedAdapter.getItemCount():" + chatAdvancedAdapter.getItemCount());
            chatAdvancedAdapter.swapCursorAndNotifyDataSetChanged(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination), insearchmode, searchstring);


            if (isfocusneeded) {
                Cursor unreadCursor = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(destination);
                int focus = unreadCursor.getCount();
                if (focustounreadorbottom) {
                    mRecyclerview.getLayoutManager().scrollToPosition(chatAdvancedAdapter.getItemCount() - (1 + focus));
                } else {
                    mRecyclerview.getLayoutManager().scrollToPosition(chatAdvancedAdapter.getItemCount() - (1));
                }
                unreadCursor.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

/*
    private void notifyDataSetChangedForSearch(String searchstring) {
        try {

            chatAdvancedAdapter.swapCursorAndNotifyDataSetChanged(CSDataProvider.getSearchInSingleChatCursor(destination, searchstring),true,searchstring);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
*/


    public int checkOrientation(String imagePath) {
        int orientation = 0;
        try {

            ExifInterface exif = new ExifInterface(imagePath);

            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = 270;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = 180;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = 90;

                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                    orientation = 0;

                    break;
                default:
                    LOG.info("orientation is invalid");
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return orientation;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        //matrix.preRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    /**
     * Handle media recorder
     */
    public void MediaRecorderReady() {
        mAudioRecorder = new MediaRecorder();
        mAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mAudioRecorder.setOutputFile(AudioSavePathInDevice);
    }

    private void getTheFileNameWithPath() {
        try {
            File wallpaperDirectory = new File(ChatConstants.CHAT_AUDIO_DIRECTORY_SENT);
            if (!wallpaperDirectory.exists()) {
                if (wallpaperDirectory.mkdirs()) {
                    Log.d(TAG, "Successfully created the parent dir:" + wallpaperDirectory.getName());
                } else {
                    Log.d(TAG, "Failed to create the parent dir:" + wallpaperDirectory.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        AudioSavePathInDevice = ChatConstants.CHAT_AUDIO_DIRECTORY_SENT + "/" + "Rec_" + new SimpleDateFormat("dd-MM-yyyy hh mm ss a").format(new Date().getTime()) + ".mp3";

    }

    /**
     * This method opens the dialog to get confirmation from user to share the recorded file
     */
    private void showConfirmationDialog() {
        try {
            try {
                h.removeCallbacks(RunnableObj);
                if (mAudioRecorder != null)
                    mAudioRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            AlertDialog.Builder dialog = new AlertDialog.Builder(ChatAdvancedActivity.this);
            dialog.setTitle("Confirmation");
            dialog.setMessage("Are you sure want to send this audio?");
            dialog.setPositiveButton(("Send"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    CSChatObj.sendAudio(destination, AudioSavePathInDevice, false);
                    updtateReadCount();
                }
            });

            dialog.setNegativeButton(("Cancel"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        File file = new File(AudioSavePathInDevice);
                        file.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTitleName() {
        try {
            if (mFriendName == null || mFriendName.equals("")) {
                toolbarTitleTextView.setText(destination);
            } else {
                toolbarTitleTextView.setText(mFriendName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * this  method loads the prifile pic using glide into UserImageView
     */
    private void loadProfilePicture() {
        try {

            String picid = "";
            Cursor cur1 = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, destination);
            if (cur1.getCount() > 0) {
                cur1.moveToNext();
                picid = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
            }
            cur1.close();
            Log.i(TAG, "picid:" + picid);
            String filepath = CSDataProvider.getImageThumbnailFilePath(picid);
            // String filepath1 = CSDataProvider.getImageFilePath(picid);
//            Log.i(TAG, "filepath:" + filepath);
//            Log.i(TAG, "filepath:" + filepath1);

            if (filepath != null && new File(filepath).exists()) {
                Glide.with(ChatAdvancedActivity.this)
                        .load(Uri.fromFile(new File(filepath)))
                        .apply(new RequestOptions().error(R.mipmap.ic_contact_avatar))
                        .apply(RequestOptions.circleCropTransform())
                        .apply(new RequestOptions().signature(new ObjectKey(String.valueOf(new File(filepath).length() + new File(filepath).lastModified()))))
                        .into(userImageView);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void applyAnimationToSubTitle() {
        try {


            toolbarSubTitleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            toolbarSubTitleTextView.setMarqueeRepeatLimit(1);
            toolbarSubTitleTextView.setSelected(true);


            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!onpaused) { //change later
                            toolbarSubTitleTextView.setText(toolbarSubTitleTextView.getText().toString().replace("active ", ""));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }, 2950);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void hideKeyboard() {
        try {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    private void handleSearch(int direction) {


        showProgressBar1();
        if (mdialog1 != null) {
            Log.i(TAG, "mdialog:" + mdialog1.isShowing());
        } else {
            Log.i(TAG, "mdialog null:");
        }
        try {
            Thread.sleep(500);//remove this later
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                try {


                    if (chatsearchrefmode && chatsearchrefstring != null && !chatsearchrefstring.equals("")) {
                        //showProgressBar();
                        if (direction == 1) {
                            chatsearchrefcnt++;
                        } else {
                            chatsearchrefcnt--;
                        }

                        Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination);

                        int i = 0;
                        int temp_chatsearchrefcnt = 0;
                        boolean searchstringfound = false;
                        int totalcount = cur.getCount();

/*
                Log.i(TAG,"i:"+i);
                Log.i(TAG,"temp_chatsearchrefcnt:"+temp_chatsearchrefcnt);
                Log.i(TAG,"searchstringfound:"+searchstringfound);
                Log.i(TAG,"chatsearchrefcnt:"+chatsearchrefcnt);
*/
                        //cur.moveToLast();
                        for (int k = (totalcount - 1); k >= 0; k--) {
                            cur.moveToPosition(k);
                            //while (cur.moveToPrevious()) {
                            String message = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                            String filename = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                            if (!chatsearchrefstring.equals("") && (message.toLowerCase().contains(chatsearchrefstring.toLowerCase()) || filename.toLowerCase().contains(chatsearchrefstring.toLowerCase()))) {
                                temp_chatsearchrefcnt++;
                            }
                            i++;

                            if (temp_chatsearchrefcnt == chatsearchrefcnt) {
                                searchstringfound = true;
                        /*Log.i(TAG,"matching:"+chatsearchrefstring);
                        Log.i(TAG,"matching message:"+message);
                        Log.i(TAG,"matching filename:"+filename);*/
                                break;
                            }
                        }
                        cur.close();

                        if (searchstringfound) {

                            final int final_i = i;
                            ChatAdvancedActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    mRecyclerview.getLayoutManager().scrollToPosition(totalcount - final_i);
                                }
                            });

                        } else {

                            if (direction == 1) {
                                chatsearchrefcnt--;
                            } else {
                                chatsearchrefcnt++;
                            }
                            ChatAdvancedActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Log.i(TAG, "search String not found");
                                    Toast.makeText(ChatAdvancedActivity.this, "Not found", Toast.LENGTH_SHORT).show();
                                }
                            });


                        }


                    }
                    cancelProgressBar();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    cancelProgressBar();
                }
            }
        }).start();
    }


    public static void handlelongclick(ArrayList<Integer> selpositions) {
        try {
            selectedpositions = new ArrayList<>(selpositions);
            if (selectedpositions.size() > 0) {
                chatoptionsview.setVisibility(View.VISIBLE);

                mToolbar.setVisibility(View.GONE);

                if (searchchatview.getVisibility() == View.VISIBLE) {
                    backviewsearch.performClick();
                }


                chat_selected_countview.setText(String.valueOf(selectedpositions.size()));

                boolean showdelete = true; //dont assign true any where
                boolean showcopy = true;
                boolean showforward = true;

                Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination);

                for (int cursorposition : selpositions) {
                    cur.moveToPosition(cursorposition);
                    int chattype = cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
                    //String chatid = cur.getString(cur.getColumnIndex(CSDbFields.KEY_CHAT_ID));
                    String uploadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                    String downloadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                    int issender = cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_IS_SENDER));
                    int ismultidevice = cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE));
                    int downloadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                    int uploadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));

                    if (chattype == CSConstants.E_TEXTPLAIN || chattype == CSConstants.E_CONTACT || chattype == CSConstants.E_LOCATION) {

                        //showdelete //always true for this types but don't assign here
                        //showcopy //always true for this types but don't assign here
                        //showforward //always true for this types but don't assign here

                    } else {
                        showcopy = false; //always false for this types but don't assign here

                        if (issender == 1) {

                            if (ismultidevice == 0) {
                                if (!new File(uploadfilepath).exists()) {
                                    showforward = false;
                                }
                            } else {
                                if (!new File(downloadfilepath).exists() || downloadpercentage != 100) {
                                    showforward = false;
                                }
                            }

                        } else {
                            if (!new File(downloadfilepath).exists() || downloadpercentage != 100) {
                                showforward = false;
                            }
                        }
                    }


                }

                cur.close();

                if (showdelete) {
                    chatdeleteview.setVisibility(View.VISIBLE);
                } else {
                    chatdeleteview.setVisibility(View.GONE);
                }
                if (showcopy) {
                    chatcopyview.setVisibility(View.VISIBLE);
                } else {
                    chatcopyview.setVisibility(View.GONE);
                }

                if (showforward) {
                    chatforwardview.setVisibility(View.VISIBLE);
                } else {
                    chatforwardview.setVisibility(View.GONE);
                }

            } else {
                chatoptionsview.setVisibility(View.GONE);

                mToolbar.setVisibility(View.VISIBLE);
                chat_message_layoutview.setVisibility(View.VISIBLE);
                if (searchchatview.getVisibility() == View.VISIBLE) {
                    backviewsearch.performClick();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * this method shows delete all messages allert
     *
     * @return
     */
    public boolean showDeleteAlert() {
        try {
            //android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(ChatAdvancedActivity.this);

            chatfiledelete = false;
            boolean containsfiles = false;


            final Dialog successfullyLogin = new Dialog(ChatAdvancedActivity.this);

            successfullyLogin.setCancelable(true);
            String deletetitle = "Delete " + selectedpositions.size() + " message/s ?";
            successfullyLogin.setTitle(deletetitle);
            successfullyLogin.setContentView(R.layout.deletechatconfirmation);

            TextView title = (TextView) successfullyLogin.findViewById(R.id.title);
            CheckBox checkbox = (CheckBox) successfullyLogin.findViewById(R.id.checkbox);
            TextView cancel = (TextView) successfullyLogin.findViewById(R.id.cancel);
            TextView delete = (TextView) successfullyLogin.findViewById(R.id.delete);
            title.setText(deletetitle);


            Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination);

            for (int cursorposition : selectedpositions) {
                cur.moveToPosition(cursorposition);
                int chattype = cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
                //String chatid = cur.getString(cur.getColumnIndex(CSDbFields.KEY_CHAT_ID));
                String uploadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                String downloadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                int issender = cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_IS_SENDER));
                int ismultidevice = cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE));
                int downloadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                int uploadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));

                if (chattype == CSConstants.E_TEXTPLAIN || chattype == CSConstants.E_CONTACT) {

                } else {

                    if (issender == 1) {

                        if (ismultidevice == 0) {
                            containsfiles = false;
                        } else {
                            if (new File(downloadfilepath).exists() && downloadpercentage == 100) {
                                containsfiles = true;
                            }
                        }

                    } else {
                        if (new File(downloadfilepath).exists() && downloadpercentage == 100) {
                            containsfiles = true;
                        }
                    }
                }


            }

            cur.close();


            if (containsfiles) {
                checkbox.setVisibility(View.VISIBLE);
            } else {
                checkbox.setVisibility(View.GONE);
            }

            checkbox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        if (checkbox.isChecked()) {
                            chatfiledelete = true;
                        } else {
                            chatfiledelete = false;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unReadCount = -1;
                    chatAdvancedAdapter.releaseMediaPlayer();

                    Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination);

                    for (int cursorposition : selectedpositions) {
                        cur.moveToPosition(cursorposition);
                        int chattype = cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
                        String chatid = cur.getString(cur.getColumnIndex(CSDbFields.KEY_CHAT_ID));
                        String uploadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                        String downloadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int issender = cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_IS_SENDER));
                        int ismultidevice = cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE));
                        int downloadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        int uploadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));

                        CSChatObj.deleteChatMessagebyfilter(CSDbFields.KEY_CHAT_ID, chatid);

                        if (chatfiledelete) {
                            if (chattype == CSConstants.E_TEXTPLAIN || chattype == CSConstants.E_CONTACT) {

                            } else {

                                if (issender == 1) {

                                    if (ismultidevice == 0) {

                                    } else {
                                        if (new File(downloadfilepath).exists() && downloadpercentage == 100) {
                                            //containsfiles = true;
                                            new File(downloadfilepath).delete();
                                        }
                                    }

                                } else {
                                    if (new File(downloadfilepath).exists() && downloadpercentage == 100) {
                                        //containsfiles = true;
                                        new File(downloadfilepath).delete();
                                    }
                                }
                            }
                        }


                    }

                    cur.close();
                    successfullyLogin.dismiss();

                    chatoptionsbacklayoutview.performClick();

                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    successfullyLogin.dismiss();
                }
            });

        /*
            successfullyLogin.setPositiveButton("Delete",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {



                        }
                    });

            successfullyLogin.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {

                        }
                    });
            */

            successfullyLogin.show();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    /*use //doesnt work like this. use swapCursorAndNotifyDataSetChanged
    public static int getpositionfromchatid(String chatid) {
        try {
            int position = -1;
            Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination);
            if(cur.getCount()>0) {
                for(int i = 0;i<cur.getCount();i++) {
                    cur.moveToNext();
                    String cursorchatid = cur.getString(cur.getColumnIndex(CSDbFields.KEY_CHAT_ID));
if(cursorchatid.equals(chatid)) {
    position = i;
    break;
}
                }
            }
            cur.close();
            return position;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }

    }
*/


    public boolean showClearChatOptions() {
        try {

            AlertDialog.Builder successfullyLogin = new AlertDialog.Builder(ChatAdvancedActivity.this);
            String name = "";
            Cursor ccfr = CSDataProvider.getContactCursorByNumber(destination);
            //LOG.info("ccfr.getCount():"+ccfr.getCount());
            if (ccfr.getCount() > 0) {
                ccfr.moveToNext();
                name = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
            }
            ccfr.close();
            if (name.equals("")) {
                successfullyLogin.setMessage("Clear all Chat with " + destination + "?");
            } else {
                successfullyLogin.setMessage("Clear all Chat with " + name + "?");
            }


            successfullyLogin.setCancelable(true);


            successfullyLogin.setPositiveButton("Delete",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {

                            boolean status = CSChatObj.deleteChatMessagebyfilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination);
                            if (status) {
                                Intent filter = new Intent(Constants.ACTION_CLEAR_ALL_CHAT);
                                sendBroadcast(filter);
                                notifyDataSetChanged(false, false, "", true);
                            } else {
                                Toast.makeText(ChatAdvancedActivity.this, "Unable to delete,try again", Toast.LENGTH_LONG).show();
                            }
                            //todo update batch icon count in main activity
                            // MainActivity.updateChatBadgeCount();

                        }
                    });

            successfullyLogin.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {


                        }
                    });

            //AlertDialog ad =
            successfullyLogin.show();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }


    public String getfilepathAfterCopy(Uri fileUri, String targetpath) {

        String attachmentpath = "";
        try {
            String extension = "";
            String filename = "";


            try {
                Cursor cursor = getApplicationContext().getContentResolver().query(fileUri, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }


            if (filename.equals("")) {
                try {
                    int i = fileUri.toString().lastIndexOf('/');
                    if (i > 0) {
                        filename = fileUri.toString().substring(i);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


            if (filename.equals("")) {
                try {

                    ContentResolver cR = getApplicationContext().getContentResolver();
                    if (fileUri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                        //If scheme is a content
                        final MimeTypeMap mime = MimeTypeMap.getSingleton();
                        extension = mime.getExtensionFromMimeType(getApplicationContext().getContentResolver().getType(fileUri));
                    } else {
                        //If scheme is a File
                        //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
                        //extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(imageUri.getPath())).toString());
                        extension = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());

                    }
                    LOG.info("extension from mime:" + extension);

                    if (extension == null) {
                        int i = fileUri.toString().lastIndexOf('.');
                        if (i > 0) {
                            extension = fileUri.toString().substring(i + 1);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (extension == null) {
                    extension = "";
                }


                if (filename.equals("")) {
                    filename = "Doc_" + String.valueOf(new Date().getTime()) + "." + extension;
                }
            }


            createDirIfNotExists(targetpath);


            InputStream input = getApplicationContext().getContentResolver().openInputStream(fileUri);
            try {
                if (new File(targetpath, filename).exists()) {
                    LOG.info("File name to copy:" + filename);
                    try {
                        String[] filenames = filename.split("\\.");
                        String t_name = filenames[0];
                        String t_extention = filenames[1];
                        filename = t_name + "_" + String.valueOf(new Date().getTime()) + "." + t_extention;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        filename = filename + "_" + String.valueOf(new Date().getTime());

                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            File file = new File(targetpath, filename);
            Log.i(TAG, "File exists: " + file.exists() + " , File Path: " + file.getAbsolutePath());
            try {

                OutputStream output = new FileOutputStream(file);
                try {
                    try {
                        byte[] buffer = new byte[4 * 1024]; // or other buffer size
                        int read;

                        if (input != null) {
                            while ((read = input.read(buffer)) != -1) {
                                output.write(buffer, 0, read);
                            }
                        }
                        output.flush();
                    } finally {
                        output.close();
                        attachmentpath = file.getAbsolutePath();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return attachmentpath;
    }


    public static boolean createDirIfNotExists(String path) {
        try {

            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {


                Boolean isSDPresent = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
                //GlobalVariables.appname = IAmLiveCore.getApplicationName();

                if (isSDPresent) {
                    //LOG.info("yes SD-card is present:"+Environment.getExternalStorageDirectory().getAbsolutePath());

//                    File file = new File(Environment.getExternalStorageDirectory() + File.separator + path);
                    File file = new File(path);
                    if (!file.exists()) {
                        if (!file.mkdirs()) {
                            //LOG.info("Problem creating a folder");
                            return false;
                        }
                    }
                    return true;

                } else {

                    //LOG.info("NO SD-card is present\n");
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;

    }

    class SendFile extends AsyncTask<String, Integer, String> {
        String TAG = getClass().getSimpleName();


        private Uri fileUri;
        private String filetype = "doc";


        public SendFile(Uri uri, String type) {
            fileUri = uri;
            filetype = type;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG + " PreExceute", "On pre Exceute......");
            showProgressBar1();
        }

        protected String doInBackground(String... params) {
            Log.d(TAG + " DoINBackGround", "On doInBackground...");

            String targetpath = ChatConstants.docsdirectorysent;
            if (filetype.equals("audio")) {
                targetpath = ChatConstants.audiodirectorysent;
            } else if (filetype.equals("video")) {
                targetpath = ChatConstants.videodirectorysent;
            } else if (filetype.equals("gallaryimage")) {
                targetpath = ChatConstants.imagedirectorysent;
                // return ChatMethodHelper.getImagePath(ChatAdvancedActivity.this,fileUri);
            } else if (filetype.equals("cameraimage")) {
                targetpath = ChatConstants.imagedirectorysent;

            } else {
                targetpath = ChatConstants.docsdirectorysent;
            }
            createDirIfNotExists(targetpath);
            //  return getfilepathAfterCopy(fileUri, targetpath);
            String result = getfilepathAfterCopy(fileUri, targetpath);

            cancelProgressBar1();

            if (result != null && !result.equals("")) {
                if (filetype.equals("audio")) {
                    CSChatObj.sendAudio(destination, result, false);
                } else if (filetype.equals("video")) {
                    CSChatObj.sendVideo(destination, result, false);
                } else if (filetype.equals("gallaryimage")) {
                    CSChatObj.sendPhoto(destination, result, false);
                } else if (filetype.equals("cameraimage")) {
                    CSChatObj.sendPhoto(destination, result, false);
                    File cemaraFile = new File(mImagePath);
                    if (cemaraFile.exists()) {
                        cemaraFile.delete();
                    }
                } else {
                    CSChatObj.sendDocument(destination, result, false);
                }

            }
            return result;
        }

        /*
        protected void onProgressUpdate(Integer...a){
            super.onProgressUpdate(a);
            Log.d(TAG + " onProgressUpdate", "You are in progress update ... " + a[0]);
        }
        */

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG + " onPostExecute", "" + result);

//        if (result != null && !result.equals("")) {
//            if (filetype.equals("audio")) {
//                CSChatObj.sendAudio(destination, result, false);
//            } else if (filetype.equals("video")) {
//                CSChatObj.sendVideo(destination, result, false);
//            } else if (filetype.equals("gallaryimage")) {
//                CSChatObj.sendPhoto(destination, result, false);
//            } else if (filetype.equals("cameraimage")) {
//                CSChatObj.sendPhoto(destination, result, false);
//                if (imageFile.exists()) {
//                    imageFile.delete();
//                }
//            } else {
//                CSChatObj.sendDocument(destination, result, false);
//            }
//        }
            updtateReadCount();
            notifyDataSetChanged(true, false, "", true);
        }

    }


    /**
     * this method updates the phone gallery ,it scans the given file
     *
     * @param file
     */
    private void toScannMediaFile(String file) {

        // refressing data in Gallery after download...
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//               contentUri = FileProvider.getUriForFile(this,getPackageName() + ".provider",new File(file));
//            }else {
//                contentUri = Uri.fromFile(new File(file)); //out is your file you saved/deleted/moved/copied
//            }
            contentUri = Uri.fromFile(new File(file));
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
        } else {
            sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://"
                            + Environment.getExternalStorageDirectory())));
        }
    }

    private void updtateReadCount() {
        if (unReadCount != 0) {
            Cursor unreadCursor = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(destination);
            unReadCount = unreadCursor.getCount();

            Cursor allMessagesCursor = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination);
            readCount = allMessagesCursor.getCount() - unReadCount;
        }
    }

    private PopupWindow dialog_Select(final Context context, RecyclerView lin) {

        final PopupWindow dialog_Select = new PopupWindow(context);

        View v = View.inflate(context, R.layout.contacts_popup, null);


        dialog_Select.setContentView(v);
        dialog_Select.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        dialog_Select.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        dialog_Select.setFocusable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog_Select.setElevation(10f);
        }
        dialog_Select.setBackgroundDrawable(new BitmapDrawable(
                context.getApplicationContext().getResources(),
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)));

        // Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
        int OFFSET_X = 0;
        int OFFSET_Y = 0;

        LinearLayout ll_share = v.findViewById(R.id.ll_drop_box);

        LinearLayout ll_contact_share = v.findViewById(R.id.ll_contact_details);
        LinearLayout ll_location_share = v.findViewById(R.id.ll_contact_invite);
        TextView tv_location_share = v.findViewById(R.id.tv_contact_invite);
        TextView tv_contact_share = v.findViewById(R.id.tv_contact_details);
        tv_contact_share.setText(getString(R.string.chat_screen_share_contact_message));
        tv_location_share.setText(getString(R.string.chat_screen_share_location_message));

        Typeface text_light = Utils.getTypefaceLight(context);

        tv_location_share.setTypeface(text_light);
        tv_contact_share.setTypeface(text_light);

//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ll_share.getLayoutParams();
//        params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen._100dp));
//        ll_share.setLayoutParams(params);


        TextView tv_details = (TextView) v.findViewById(R.id.tv_contacts_details);
        TextView tv_invite = (TextView) v.findViewById(R.id.tv_contacts_invite);

        Typeface typeface = Utils.getTypeface(context);
        tv_details.setTypeface(typeface);
        tv_invite.setTypeface(typeface);

        tv_details.setText(context.getResources().getString(R.string.chat_contact));
        tv_invite.setText(context.getResources().getString(R.string.chat_location));

        ll_contact_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog_Select.dismiss();
                if (!PermissionUtils.checkReadContactsPermission(ChatAdvancedActivity.this)) {
                    ActivityCompat.requestPermissions(ChatAdvancedActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 101);
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, CONTACT_REQUEST_CODE);
            }
        });

        ll_location_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog_Select.dismiss();
                if (PermissionUtils.checkLocationPermission(ChatAdvancedActivity.this)) {
                    Intent i_map = new Intent(context, ChatMapActivity.class);
                    startActivityForResult(i_map, LOCATION_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(ChatAdvancedActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                }


            }
        });


        Rect location = locateView(lin);

        dialog_Select.showAtLocation(v, Gravity.TOP | Gravity.CENTER, 0, location.bottom);

        // Getting a reference to Close button, and close the popup when clicked.

        return dialog_Select;

    }

    /**
     * This method is used for display popup proper
     *
     * @param v this is used for locate view where you want
     * @return returns rect object
     */
    public static Rect locateView(View v) {
        int[] loc_int = new int[2];
        if (v == null) return null;
        try {
            v.getLocationOnScreen(loc_int);
        } catch (Exception npe) {
            //Happens when the view doesn't exist on screen anymore.
            npe.printStackTrace();
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }

    public static File checkFileExistence(String path, String fileName) {
        if (!new File(path).exists()) {
            new File(path).mkdirs();
        }
        File file = new File(path, fileName);
        return file;
    }


}
