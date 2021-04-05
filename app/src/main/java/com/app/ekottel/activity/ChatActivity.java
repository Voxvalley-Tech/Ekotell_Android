package com.app.ekottel.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.interfaces.ChatInterface;
import com.app.ekottel.interfaces.NetworkChangeCallback;
import com.app.ekottel.model.ChatData;
import com.app.ekottel.receivers.NetworkChangeReceiver;
import com.app.ekottel.utils.CallMethodHelper;
import com.app.ekottel.utils.ChatConstants;
import com.app.ekottel.utils.ChatMethodHelper;
import com.app.ekottel.utils.CircleProgressBar;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.PermissionUtils;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.Utils.CSExplicitEvents;
import com.ca.dao.CSChatContact;
import com.ca.dao.CSChatLocation;
import com.ca.wrapper.CSChat;
import com.ca.wrapper.CSDataProvider;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements ChatInterface {
    private static final int IMAGE_CAPTURE_REQUEST_CODE = 1;
    private static final int IMAGE_GALLERY_REQUEST_CODE = 2;
    private static final int VIDEO_GALLERY_REQUEST_CODE = 3;
    private static final int DOCUMENTS_REQUEST_CODE = 4;
    private static final int CONTACT_REQUEST_CODE = 5;
    private static final int AUDIO_REQUEST_CODE = 6;
    private static final int LOCATION_REQUEST_CODE = 7;
    private static final int FORWARD_MESSAGE_INTNET = 8;
    private Toolbar mToolbar;
    private TextView mToolbarTitleTextView, mToolbarSubTitleTextView;
    private CircleImageView mUserImageView;
    private EditText mChatMessageEditText;

    private LinearLayout mChatOptionsLayout;
    private RelativeLayout mChatOptionsBackLayout, mChatOptionsDeleteLayout, mChatOptionsCopyLayout, mChatOptionsForward;
    private TextView tv_call, tv_video_call, tv_add_contact, tv_back, tv_sender_name, tv_last_seen, tv_file_transfer, tv_image_transfer, tv_voice_transfer, tv_more_transfer;
    private RelativeLayout rl_file_share, rl_image_share, rl_voice_transfer, rl_more_transfer;
    private TextView mSendButton;
    private TextView mSelectedMessagesCountTextView;
    private ChatCallBacksReceiver mChatCallBacks = new ChatCallBacksReceiver();
    private CSChat mCSChat;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mRecyclerViewLayoutManager;
    private Context mContext;
    private ArrayList<ChatData> mAllChatMessages = new ArrayList<>();
    private String mFriendNumber = "", mFriendName = "", mPresenceStatus = "";
    private long mLastClickTime = 0;
    private PreferenceProvider mPrefereceProvider;
    public static int readCount = -1;
    private String TAG = "ChatActivity";
    private ChatsAdapter mAdapter;
    private boolean isRecordButtonLongPressed = false;
    private String AudioSavePathInDevice = null;
    private TextView mRecordTimerTv, mRecordHelpTextTv;
    Runnable RunnableObj;
    final Handler h = new Handler();
    int delay = 1000;
    private int startTime = 0;
    private MediaRecorder mAudioRecorder;
    private String mImagePath;
    private ArrayList<ChatData> mChatForwardArrayList = new ArrayList<>();
    private boolean isUpdateChatSelectionCalled = false;
    PopupWindow popupwindow_obj;
    public static Dialog popup_close_dialog = null;
    Random random;
    String RandomAudioFileName;
    private BroadcastReceiver mNetworkReceiver;
    private NetworkChangeReceiver networkChangeReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat1);
        LOG.info("onCreate: timeStamp Prasad" + Utils.getTiemStamp(System.currentTimeMillis()));
        mToolbar = findViewById(R.id.chat_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        RandomAudioFileName = getString(R.string.chats_screen_audio_random_file_name);
        mToolbarTitleTextView = mToolbar.findViewById(R.id.toolbar_title);
        mToolbarSubTitleTextView = mToolbar.findViewById(R.id.toolbar_sub_title);
        mUserImageView = mToolbar.findViewById(R.id.user_image_view);
        mRecyclerView = findViewById(R.id.conversation_recycler_view);
        mPrefereceProvider = new PreferenceProvider(getApplicationContext());
        mChatMessageEditText = findViewById(R.id.chat_message);
        tv_file_transfer = (TextView) findViewById(R.id.tv_file_transfer);
        tv_image_transfer = (TextView) findViewById(R.id.tv_image_transfer);
        tv_voice_transfer = (TextView) findViewById(R.id.tv_voice_transfer);
        tv_more_transfer = (TextView) findViewById(R.id.tv_more_transfer);
        mSendButton = (TextView) findViewById(R.id.send);
        rl_file_share = (RelativeLayout) findViewById(R.id.rl_file_share);
        rl_image_share = (RelativeLayout) findViewById(R.id.rl_image_share);
        rl_voice_transfer = (RelativeLayout) findViewById(R.id.rl_voice_share);
        rl_more_transfer = (RelativeLayout) findViewById(R.id.more_share_layout);
        tv_call = (TextView) findViewById(R.id.iv_chat_conversation_call);
        tv_video_call = (TextView) findViewById(R.id.iv_chat_conversation_video_call);
        mChatOptionsLayout = findViewById(R.id.chat_message_options_layout);
        mChatOptionsBackLayout = findViewById(R.id.chat_message_options_back);
        mChatOptionsCopyLayout = findViewById(R.id.chat_messages_copy);
        mChatOptionsDeleteLayout = findViewById(R.id.chat_messages_delete);
        mChatOptionsForward = findViewById(R.id.chat_messages_forward);
        mSelectedMessagesCountTextView = findViewById(R.id.chat_messages_count);
        mRecordTimerTv = findViewById(R.id.recording_timer);
        mRecordHelpTextTv = findViewById(R.id.record_help_text_tv);
        mContext = ChatActivity.this;
        mRecyclerView.setHasFixedSize(true);
        mRecyclerViewLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerViewLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mRecyclerViewLayoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);
        Typeface typeface = Utils.getTypeface(getApplicationContext());
        random = new Random();
        tv_file_transfer.setTypeface(typeface);
        tv_image_transfer.setTypeface(typeface);
        tv_voice_transfer.setTypeface(typeface);
        tv_more_transfer.setTypeface(typeface);
        tv_call.setTypeface(typeface);
        tv_video_call.setTypeface(typeface);
        mSendButton.setTypeface(typeface);
        mSendButton.setText(getResources().getString(R.string.contact_invite));
        tv_image_transfer.setText(getResources().getString(R.string.chat_image_transfer));
        tv_file_transfer.setText(getResources().getString(R.string.chat_file_transfer));
        tv_voice_transfer.setText(getResources().getString(R.string.chat_voice_transfer));
        tv_more_transfer.setText(getResources().getString(R.string.icon_more));
        tv_call.setText(getResources().getString(R.string.dialpad_call));
        mCSChat = new CSChat();

        networkChangeReceiver = new NetworkChangeReceiver();
        PreferenceProvider pf = new PreferenceProvider(getApplicationContext());

        Intent intent = getIntent();
        mFriendNumber = intent.getStringExtra(getString(R.string.call_logs_intent_sender_key));
        //   mFriendName = intent.getStringExtra(ChatConstants.INTENT_CHAT_CONTACT_NAME);
        LOG.info("onCreate: " + mFriendNumber + " " + mFriendName);
        mPrefereceProvider.setPrefString(mFriendNumber, "");
        try {
            int notificationid = Integer.parseInt(mFriendNumber.substring((mFriendNumber.length() / 2), mFriendNumber.length()).replace("+", ""));

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(notificationid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        Cursor ccr = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(mFriendNumber);
        readCount = ccr.getCount();
        getProfilePicture();

        mToolbarSubTitleTextView.setSelected(true);
        if (CSDataProvider.getLoginstatus()) {
            List<String> numbers = new ArrayList<>();
            numbers.add(mFriendNumber);
            mCSChat.getPresence(numbers);
        }
        LOG.info("onCreate: laodChat messages before timeStamp Prasad" + Utils.getTiemStamp(System.currentTimeMillis()));
        loadChatMessages();
        LOG.info("onCreate: Load chat messahes afetr timeStamp Prasad" + Utils.getTiemStamp(System.currentTimeMillis()));
        try {
            mChatCallBacks = new ChatCallBacksReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter1 = new IntentFilter(CSEvents.CSCHAT_CHATUPDATED);
            IntentFilter filter2 = new IntentFilter(CSExplicitEvents.CSChatReceiver);
            IntentFilter filter3 = new IntentFilter(CSEvents.CSCHAT_GETPRESENCE_RESPONSE);
            IntentFilter filter4 = new IntentFilter(CSEvents.CSCHAT_UPLOADFILEDONE);
            IntentFilter filter5 = new IntentFilter(CSEvents.CSCHAT_UPLOADFILEFAILED);
            IntentFilter filter6 = new IntentFilter(CSEvents.CSCHAT_DOWNLOADFILEDONE);
            IntentFilter filter7 = new IntentFilter(CSEvents.CSCHAT_DOWNLOADFILEFAILED);
            IntentFilter filter8 = new IntentFilter(CSEvents.CSCLIENT_LOGIN_RESPONSE);
            IntentFilter filter9 = new IntentFilter(CSEvents.CSCHAT_DOWNLOADPROGRESS);
            IntentFilter filter10 = new IntentFilter(CSEvents.CSCHAT_UPLOADPROGRESS);
            IntentFilter filter11 = new IntentFilter(CSEvents.CSCHAT_ISTYPING);
            IntentFilter filter12 = new IntentFilter(CSEvents.CSCLIENT_IMAGESDBUPDATED);
            IntentFilter filter13 = new IntentFilter(CSEvents.CSCLIENT_USERPROFILECHANGED);

            LocalBroadcastManager.getInstance(mContext).registerReceiver(mChatCallBacks, filter);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mChatCallBacks, filter1);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mChatCallBacks, filter2);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mChatCallBacks, filter3);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mChatCallBacks, filter4);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mChatCallBacks, filter5);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mChatCallBacks, filter6);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mChatCallBacks, filter7);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mChatCallBacks, filter8);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mChatCallBacks, filter9);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mChatCallBacks, filter10);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mChatCallBacks, filter11);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mChatCallBacks, filter12);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mChatCallBacks, filter13);

            registerReceiver(mChatCallBacks, filter2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RunnableObj = new Runnable() {


            public void run() {
                h.postDelayed(this, delay);
                //LOG.info("printing at 1 sec");
                runOnUiThread(new Runnable() {
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

        // Toolbar Navigation Icon
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                if (GlobalVariables.INCALL) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.call_running_error_message), Toast.LENGTH_LONG).show();
                    return;
                }else{
                   CallMethodHelper.processAudioCall(ChatActivity.this, mFriendNumber, "PSTN");
                }
            }
        });

        tv_video_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if (GlobalVariables.INCALL) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.call_running_error_message), Toast.LENGTH_LONG).show();
                    return;
                }else{
                    CallMethodHelper.placeVideoCall(ChatActivity.this, mFriendNumber);
                }
            }
        });
        mChatMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mChatMessageEditText.getText().toString().length() == 1 || mChatMessageEditText.getText().toString().length() % 3 == 1) {
                    LOG.info("Sending typing request");
                    mCSChat.sendIsTyping(mFriendNumber, false, true);
                }
                String chatMessage = mChatMessageEditText.getText().toString().trim();
                if (chatMessage.length() > 0) {
                    mSendButton.setTextColor(ContextCompat.getColor(mContext, R.color.theme_color));
                } else {
                    mSendButton.setTextColor(ContextCompat.getColor(mContext, R.color.chat_send_button_normal));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        mSendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String chatMessage = mChatMessageEditText.getText().toString().trim();
                sendChatMessage(chatMessage);
            }
        });
        rl_file_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUpdateChatSelectionCalled) {
                    disableMessageSelection();
                }
                if (!PermissionUtils.checkReadExternalStoragePermission(ChatActivity.this)) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                    return;
                }
                try {
                    Intent intent = new Intent(ChatActivity.this, DocumentsActivity.class);
                    startActivityForResult(intent, DOCUMENTS_REQUEST_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        rl_more_transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUpdateChatSelectionCalled) {
                    disableMessageSelection();
                }
                LOG.info("onClick: more clicked");
                popupwindow_obj = dialog_Select(ChatActivity.this, mRecyclerView);
                popupwindow_obj.showAsDropDown(v);
            }
        });


        rl_image_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUpdateChatSelectionCalled) {
                    disableMessageSelection();
                }
                popupusertoselectimagesource();
            }
        });
        rl_voice_transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUpdateChatSelectionCalled) {
                    disableMessageSelection();
                }
                if (!Utils.getNetwork(ChatActivity.this)) {
                    Toast.makeText(ChatActivity.this, getString(R.string.network_unavail_message), Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(ChatActivity.this, getString(R.string.chat_screen_audio_release_send_message),
                        Toast.LENGTH_SHORT).show();


            }
        });
        rl_voice_transfer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (isUpdateChatSelectionCalled) {
                    disableMessageSelection();
                }

                if (!PermissionUtils.checkRecordAudioPermission(ChatActivity.this)) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 101);
                    return true;
                }
                if (!PermissionUtils.checkReadExternalStoragePermission(ChatActivity.this)) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                    return true;
                }

                boolean callRunning = mPrefereceProvider.getPrefBoolean("CallRunning");

                if (callRunning) {
                    Toast.makeText(ChatActivity.this, getResources().getString(R.string.audio_record_error_during_call), Toast.LENGTH_SHORT).show();
                    return true;
                }

                isRecordButtonLongPressed = true;

                AudioSavePathInDevice =
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                                CreateRandomAudioFileName(5) + getString(R.string.chat_screen_audio_message);

                MediaRecorderReady();

                try {
                    mChatMessageEditText.setVisibility(View.GONE);
                    mRecordHelpTextTv.setVisibility(View.VISIBLE);
                    mRecordTimerTv.setVisibility(View.VISIBLE);
                    mRecordTimerTv.setText("00" + ":" + "00");
                    startTime = 0;
                    h.postDelayed(RunnableObj, delay);

                    mAudioRecorder.prepare();
                    mAudioRecorder.start();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Toast.makeText(ChatActivity.this, getString(R.string.chat_screen_audio_start_message),
                        Toast.LENGTH_SHORT).show();

                return true;
            }
        });

        rl_voice_transfer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (isRecordButtonLongPressed) {
                        try {
                            if (mAudioRecorder != null)
                                mAudioRecorder.stop();

                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(AudioSavePathInDevice);
                            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            mmr.release();
                            LOG.info("File size " + duration);
                            if (Integer.parseInt(duration) > 1000) {
                                invokeAudioDialog();
                            } else {
                                File file = new File(AudioSavePathInDevice);
                                file.delete();
                                Toast.makeText(ChatActivity.this, getString(R.string.chat_screen_audio_release_send_message),
                                        Toast.LENGTH_SHORT).show();
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
        mChatOptionsBackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableMessageSelection();
            }
        });
        mChatOptionsDeleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(ChatActivity.this);
                alertDialogBuilder.setMessage(getResources().getString(R.string.want_to_delete));

                alertDialogBuilder.setPositiveButton(getResources().getString(R.string.delete_cap), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        //ArrayList<ChatData> deletedMessages = new ArrayList<ChatData>();
                        // Deletes teh selected chat messages from array list.
                        ArrayList<ChatData> selectedList = new ArrayList<>();
                        selectedList.addAll(mAllChatMessages);
                        for (int i = 0; i < selectedList.size(); i++) {
                            ChatData chatData = selectedList.get(i);
                            if (chatData.isChatSelected()) {
                                LOG.info("onClick: delete chat came");
                                mCSChat.deleteChatMessagebyfilter(CSDbFields.KEY_CHAT_ID, chatData.getChatId());
                                for (int j = 0; j < mAllChatMessages.size(); j++) {
                                    if (mAllChatMessages.get(j).getChatId().equals(chatData.getChatId())) {
                                        mAllChatMessages.remove(j);
                                    }
                                }
                            }
                        }

                        readCount = -1;
                        mAdapter.notifyDataSetChanged();
                        disableMessageSelection();
                        Intent intent = new Intent(CSEvents.CSCHAT_CHATDELETED);
                        mContext.sendBroadcast(intent);
                    }
                });

                alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel_cap), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }
        });

        mChatOptionsCopyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String allConcatenatedMessages = "";
                for (int i = 0; i < mAllChatMessages.size(); i++) {
                    if (mAllChatMessages.get(i).isChatSelected()) {
                        if (allConcatenatedMessages.length() > 0) {
                            allConcatenatedMessages = allConcatenatedMessages + "\n" + mAllChatMessages.get(i).getMessage();
                        } else {
                            allConcatenatedMessages = mAllChatMessages.get(i).getMessage();
                        }
                    }
                }

                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Chat Messages", allConcatenatedMessages);
                clipboard.setPrimaryClip(clip);

                disableMessageSelection();
            }
        });
        mChatOptionsForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for (int i = 0; i < mAllChatMessages.size(); i++) {
                    if (mAllChatMessages.get(i).isChatSelected()) {
                        mChatForwardArrayList.add(mAllChatMessages.get(i));
                    }
                }
                Intent contactIntent = new Intent(ChatActivity.this, ContactShareActivity.class);
                startActivityForResult(contactIntent, FORWARD_MESSAGE_INTNET);
                disableMessageSelection();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        mPrefereceProvider.setPrefString("activeDestination", mFriendNumber);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableMessageSelection();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
        mPrefereceProvider.setPrefString("activeDestination", CSDataProvider.getLoginID());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mChatCallBacks);
        unregisterNetworkChanges();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If result is not OK closing method.
        readCount = -1;
        if (resultCode != RESULT_OK)
            return;

        LOG.info("onActivityResult, RequestCode: " + requestCode);
        switch (requestCode) {

            case IMAGE_CAPTURE_REQUEST_CODE:

                if (mImagePath != null) {
                    //if (data != null && data.getData() != null) {
                    mCSChat.sendPhoto(mFriendNumber, mImagePath, false);
                    //String picturePath = ChatMethodHelper.getImagePath(ChatActivity.this, data.getData());
                    //LOG.info("Captured image path: " + picturePath);
                    //mCSChat.sendPhoto(mFriendNumber, picturePath, false);
                }

                break;

            case IMAGE_GALLERY_REQUEST_CODE:

                if (data != null) {
                    boolean isImageFile = data.getBooleanExtra("isImageData", false);
                    if (isImageFile) {
                        ArrayList<String> returnedData = data.getStringArrayListExtra("selectedList");
                        if (Utils.getNetwork(getApplicationContext())) {
                            for (int i = 0; i < returnedData.size(); i++) {
                                mCSChat.sendPhoto(mFriendNumber, returnedData.get(i), false);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.network_unavail_message), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String filePath = data.getStringExtra("selectedList");
                        mCSChat.sendVideo(mFriendNumber, filePath, false);
                    }
                   /* Uri imageUri = data.getData();
                    LOG.info("onActivityResult:gaerry path " + imageUri);
                    String mimeType = getMimeType(imageUri);
                    if (mimeType.contains("image/")) {
                        String picturePath = ChatMethodHelper.getImagePath(ChatActivity1.this, data.getData());
                        mCSChat.sendPhoto(mFriendNumber, picturePath, false);
                    } else if (mimeType.contains("video/")) {
                        String videoPath = ChatMethodHelper.getImagePath(ChatActivity1.this, data.getData());
                        mCSChat.sendVideo(mFriendNumber, videoPath, false);
                    } else {
                        String picturePath = ChatMethodHelper.getImagePath(ChatActivity1.this, imageUri);
                        mCSChat.sendPhoto(mFriendNumber, picturePath, false);
                    }*/
                }
                break;
            case VIDEO_GALLERY_REQUEST_CODE:

                if (mImagePath != null) {
                    mCSChat.sendVideo(mFriendNumber, mImagePath, false);
                }


                break;
            case DOCUMENTS_REQUEST_CODE:

                if (data != null) {
                    LOG.info("Sending document, FilePath: " + data.getData());
                    try {

                        String docfilePath = data.getStringExtra("DocumentsURL");
                        //Uri uri = Uri.fromFile(new File(docfilePath));

                        mCSChat.sendDocument(mFriendNumber, docfilePath, false);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, getResources().getString(R.string.wrong_document), Toast.LENGTH_SHORT).show();
                    }
                }

                break;

            case LOCATION_REQUEST_CODE:

                if (data != null) {
                    double latitude = data.getDoubleExtra(ChatConstants.INTENT_LOCATION_LATITUDE, 0.0);
                    double longitude = data.getDoubleExtra(ChatConstants.INTENT_LOCATION_LONGITUDE, 0.0);
                    String address = data.getStringExtra(ChatConstants.INTENT_LOCATION_ADDRESS);
                    CSChatLocation CSChatLocationObj = new CSChatLocation(latitude, longitude, address);
                    mCSChat.sendLocation(mFriendNumber, CSChatLocationObj, false);
                }

                break;

            case AUDIO_REQUEST_CODE:

                if (data != null && data.getData() != null) {
                    Uri selectedAudio = data.getData();
                    LOG.info("Selected Audio URI: " + selectedAudio);

                    String audioPath = ChatMethodHelper.getPath(getApplicationContext(),
                            selectedAudio);
                    mCSChat.sendAudio(mFriendNumber, audioPath, false);
                }

                break;
            case FORWARD_MESSAGE_INTNET:
                if (data != null) {
                    ArrayList<String> numbersList = data.getStringArrayListExtra("contactNumbers");


                    for (String number : numbersList) {
                        LOG.info("selected messages size: " + mChatForwardArrayList.size());
                        for (int i = 0; i < mChatForwardArrayList.size(); i++) {
                            ChatData chatData = mChatForwardArrayList.get(i);
                            String message = chatData.getMessage();
                            boolean issender = chatData.isSender();
                            int ismultidevicechat = chatData.getIsMultiDeviceMessage();
                            int msgtype = chatData.getMessageType();
                            String uploadfilepath = chatData.getUploadFilePath();
                            String dowloadfilepath = chatData.getDownloadFilePath();
                            boolean isgroupmessage = false;

                            LOG.info("Forwarding to:" + number);
                            LOG.info("Forwarding to isgroupmessage:" + isgroupmessage);

                            switch (msgtype) {
                                case CSConstants.E_TEXTPLAIN:
                                    mCSChat.sendMessage(number, message, isgroupmessage);
                                    break;
                                case CSConstants.E_TEXTHTML:
                                    mCSChat.sendMessage(number, message, isgroupmessage);
                                    break;
                                case CSConstants.E_LOCATION:
                                    CSChatLocation cschatlocation = mCSChat.getLocationFromChatID(chatData.getChatId());
                                    mCSChat.sendLocation(number, cschatlocation, isgroupmessage);
                                    break;
                                case CSConstants.E_IMAGE:
                                    if (issender) {
                                        if (ismultidevicechat == 0) {
                                            mCSChat.sendPhoto(number, uploadfilepath, isgroupmessage);
                                        } else {
                                            mCSChat.sendPhoto(number, dowloadfilepath, isgroupmessage);
                                        }
                                    } else {
                                        //mCSChat.sendPhoto(number, ChatConstants.CHAT_IMAGES_DIRECTORY + "/" + filename, isgroupmessage);
                                        mCSChat.sendPhoto(number, dowloadfilepath, isgroupmessage);
                                    }
                                    break;
                                case CSConstants.E_VIDEO:
                                    LOG.info("uploadfilepath:" + uploadfilepath);

                                    if (issender) {
                                        if (ismultidevicechat == 0) {
                                            mCSChat.sendVideo(number, uploadfilepath, isgroupmessage);
                                        } else {
                                            mCSChat.sendVideo(number, dowloadfilepath, isgroupmessage);
                                        }
                                    } else {
                                        //mCSChat.sendVideo(number, ChatConstants.CHAT_VIDEOS_DIRECTORY + "/" + filename, isgroupmessage);
                                        mCSChat.sendVideo(number, dowloadfilepath, isgroupmessage);
                                    }
                                    break;
                                case CSConstants.E_CONTACT:
                                    CSChatContact cschatContact = mCSChat.getContactFromChatID(chatData.getChatId());
                                    mCSChat.sendContact(number, cschatContact, isgroupmessage);
                                    break;
                                case CSConstants.E_DOCUMENT:
                                    if (issender) {
                                        if (ismultidevicechat == 0) {
                                            //mCSChat.sendDocument(number, Uri.fromFile(new File(uploadfilepath)), isgroupmessage);
                                            mCSChat.sendDocument(number, uploadfilepath, isgroupmessage);
                                        } else {
                                            //mCSChat.sendDocument(number, Uri.fromFile(new File(dowloadfilepath)), isgroupmessage);
                                            mCSChat.sendDocument(number, dowloadfilepath, isgroupmessage);
                                        }
                                    } else {
                                        //mCSChat.sendDocument(number, Uri.fromFile(new File(ChatConstants.CHAT_DOCUMENTS_DIRECTORY + "/" + filename)), isgroupmessage);
                                        //mCSChat.sendDocument(number, ChatConstants.CHAT_DOCUMENTS_DIRECTORY + "/" + filename, isgroupmessage);
                                        mCSChat.sendDocument(number, dowloadfilepath, isgroupmessage);
                                    }
                                    break;
                                case CSConstants.E_AUDIO:
                                    if (issender) {
                                        if (ismultidevicechat == 0) {
                                            mCSChat.sendAudio(number, uploadfilepath, isgroupmessage);
                                        } else {
                                            mCSChat.sendAudio(number, dowloadfilepath, isgroupmessage);
                                        }
                                    } else {
                                        //mCSChat.sendAudio(number, ChatConstants.CHAT_AUDIO_DIRECTORY + "/" + filename, isgroupmessage);
                                        mCSChat.sendAudio(number, dowloadfilepath, isgroupmessage);
                                    }
                                    break;
                            }
                        }
                    }

                    mChatForwardArrayList.clear();

                    if (numbersList.size() == 1) {
                        String openChatNumber = numbersList.get(0);

                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(getString(R.string.call_logs_intent_sender_key), openChatNumber);
                        intent.putExtra(getString(R.string.call_logs_intent_is_group_key), false);
                        startActivity(intent);
                        finish();
                    }

                }
                break;

            case CONTACT_REQUEST_CODE:
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
                           try {
                               String label = "";

                               int labelType = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                               if (labelType == ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT) {
                                   label = "Assistant";
                               } else if (labelType == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM) {
                                   label = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
                               } else {
                                   CharSequence seq = ContactsContract.CommonDataKinds.Phone.getTypeLabel(getApplicationContext().getResources(), labelType, "mobile");
                                   label = seq.toString();
                               }
                               numbersList.add(phoneNumber);
                               labelsList.add(label.toLowerCase());
                           }catch (Exception e){
                           }
                        }
                        phones.close();
                    }
                    LOG.info("Sending Contact, Name: " + contactName + " ,numbers: " + numbersList + " ,Labels: " + labelsList);

                    CSChatContact contactObject = new CSChatContact(contactName, numbersList, labelsList);
                    boolean result = mCSChat.sendContact(mFriendNumber, contactObject, false);
                }


                break;
        }
    }


    private void loadChatMessages() {
        new Thread() {
            public void run() {
                Cursor cursor = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, mFriendNumber);

                mAllChatMessages.clear();
                if (cursor.moveToNext()) {
                    do {
                        ChatData chatData = new ChatData();

                        chatData.setChatId(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID)));
                        LOG.info("Chat ID: " + chatData.getChatId());
                        chatData.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE)));
                        chatData.setThumbnail(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY)));
                        chatData.setTimestamp("" + cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TIME)));
                        chatData.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME)));
                        LOG.info("run: filename " + chatData.getFileName());
                        chatData.setUploadFilePath(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH)));
                        chatData.setContentType(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_CONTENT_TYPE)));
                        chatData.setMessageStatus(cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)));
                        LOG.info("run: sending read receipt " + cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)));
                        chatData.setMessageType(cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MSG_TYPE)));
                        chatData.setSender(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_IS_SENDER)) == 1);
                        chatData.setIsMultiDeviceMessage(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE)));
                        chatData.setDownloadFilePath(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH)));
                        chatData.setMediaUploadingOrDownloadingPercentage(cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE)));

                        /*if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILEAUTOSENDORRECV)) == 0) {
                            chatData.setChancelClicked(true);
                        }*/
                        LOG.info("run: sivaprasad traafer ID" + cursor.getInt(cursor.getColumnIndexOrThrow("chatfiletransferid")));
                        LOG.info("run: sivaprasad percentage" + cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE)));
                        LOG.info("run: sivaprasad retry count" + cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_RETRY_COUNT)));
                        //LOG.info("autosend" + cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILEAUTOSENDORRECV)));
                        mAllChatMessages.add(chatData);
                    } while (cursor.moveToNext());
                }
                cursor.close();
                if (mAllChatMessages.size() > 0 && readCount > 0) {
                    readCount = mAllChatMessages.size() - readCount;
                } else {
                    readCount = -1;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter = new ChatsAdapter(ChatActivity.this, mAllChatMessages, mCSChat);
                        mRecyclerView.setAdapter(mAdapter);

                        mRecyclerView.scrollToPosition(mAllChatMessages.size() - 1);
                        LOG.info("run: send read before timeStamp Prasad" + Utils.getTiemStamp(System.currentTimeMillis()));
                        // This method reads all the messages
                        sendReadReceipts();
                        LOG.info("run: send read after timeStamp Prasad" + Utils.getTiemStamp(System.currentTimeMillis()));
                    }
                });
            }
        }.start();
    }

    /**
     * This method read the messages and send the receipts to server.
     */
    private void sendReadReceipts() {

        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < mAllChatMessages.size(); i++) {
                    ChatData chatData = mAllChatMessages.get(i);

                    if (chatData.getMessageStatus() == CSConstants.MESSAGE_RECEIVED || chatData.getMessageStatus() == CSConstants.MESSAGE_DELIVERED_ACK) {
                        LOG.info("run: sending read receipt");
                        mCSChat.sendReadReceipt(chatData.getChatId());
                    }
                }
            }
        }.start();

    }


    private void getProfilePicture() {
        String nativecontactid = "";
        Cursor cur = CSDataProvider.getContactCursorByNumber(mFriendNumber);

        if (cur.getCount() > 0) {
            cur.moveToNext();
            nativecontactid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
            mFriendName = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
        }
        cur.close();
        LOG.info("getProfilePicture: contactName " + mFriendName);
        String picid = "";
        Cursor cur1 = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, mFriendNumber);
        if (cur1.getCount() > 0) {
            cur1.moveToNext();
            picid = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));

        }
        cur1.close();
        if (mFriendName != null && mFriendName.length() > 0) {
            mToolbarTitleTextView.setText(mFriendName);
        } else {
            mToolbarTitleTextView.setText(mFriendNumber);
        }
        new ImageDownloaderTask(mUserImageView).execute("app", picid, nativecontactid);
    }

    /**
     * This method sends the chat message.
     *
     * @param chatMessage text message
     */
    private void sendChatMessage(String chatMessage) {
        if (Utils.getNetwork(getApplicationContext())) {
            if (chatMessage.length() > 0) {
                try {
                    mChatMessageEditText.setText("");
                    boolean isGroupMessage = false;
                    LOG.info("Destination number: " + mFriendNumber + " , Message: " + chatMessage);
                    mCSChat.sendMessage(mFriendNumber, chatMessage, isGroupMessage);
                    readCount = -1;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(ChatActivity.this, R.string.network_unavailable, Toast.LENGTH_SHORT);
        }
    }

    /**
     * This method hides the long press option on top of the chat screen.
     */
    void disableMessageSelection() {
        isUpdateChatSelectionCalled = false;
        mChatOptionsLayout.setVisibility(View.GONE);
        mToolbar.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mRecyclerView.getLayoutParams();
        params.addRule(RelativeLayout.BELOW, R.id.chat_tool_bar);

        mAdapter.mIsChatSelectionEnabled = false;
        for (int i = 0; i < mAllChatMessages.size(); i++) {
            ChatData chatData = mAllChatMessages.get(i);
            chatData.setChatSelected(false);
            mAllChatMessages.set(i, chatData);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateChatSelection(String chatID) {
        int totalSelectedChats = 0;
        boolean isMediaAvialble = false;
        isUpdateChatSelectionCalled = true;
        for (int i = 0; i < mAllChatMessages.size(); i++) {
            ChatData chatData = mAllChatMessages.get(i);
            if (chatData.isChatSelected()) {
                totalSelectedChats = totalSelectedChats + 1;
                if (chatData.getMessageType() != CSConstants.E_TEXTPLAIN) {
                    isMediaAvialble = true;
                }
            }
            LOG.info("Message Type " + chatData.getMessageType() + " contect type " + chatData.getContentType());
        }
        if (isMediaAvialble) {
            mChatOptionsCopyLayout.setVisibility(View.GONE);
        } else {
            mChatOptionsCopyLayout.setVisibility(View.VISIBLE);
        }
        /*if (totalSelectedChats > 1) {
            mChatOptionsForward.setVisibility(View.GONE);
        } else {
            mChatOptionsForward.setVisibility(View.VISIBLE);
        }*/
        if (totalSelectedChats == 0) {
            disableMessageSelection();
        } else {
            mSelectedMessagesCountTextView.setText("" + totalSelectedChats);

            mChatOptionsLayout.setVisibility(View.VISIBLE);
            mToolbar.setVisibility(View.GONE);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mRecyclerView.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.chat_message_options_layout);
        }
    }

    public class ChatCallBacksReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                LOG.info("SDK Callbacks, Action: " + intent.getAction());
                if (isUpdateChatSelectionCalled) {
                    disableMessageSelection();
                }
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    mToolbarSubTitleTextView.setVisibility(View.GONE);
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {

                    }
                } else if (intent.getAction().equals(CSExplicitEvents.CSChatReceiver)) {
                    if (mFriendNumber.equals(intent.getStringExtra(ChatConstants.INTENT_DESTINATION_NUMBER))) {
                        String chatID = intent.getStringExtra(ChatConstants.INTENT_CHAT_ID);
                        LOG.info("Chat ID: " + chatID);

                        for (int i = 0; i < mAllChatMessages.size(); i++) {
                            if (mAllChatMessages.get(i).getChatId().equalsIgnoreCase(chatID)) {
                                // If we got duplicate chat not adding into list.
                                return;
                            }
                        }

                        Cursor cursor = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_ID, chatID);
                        // mToolbarSubTitleTextView.setText("Online");
                        if (cursor.moveToNext()) {
                            ChatData chatData = new ChatData();
                            chatData.setChatId(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID)));
                            chatData.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE)));
                            chatData.setThumbnail(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY)));
                            chatData.setTimestamp("" + cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TIME)));
                            chatData.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME)));
                            chatData.setUploadFilePath(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH)));
                            chatData.setContentType(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_CONTENT_TYPE)));
                            chatData.setMessageStatus(cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)));
                            chatData.setMessageType(cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MSG_TYPE)));
                            chatData.setSender(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_IS_SENDER)) == 1);
                            chatData.setIsMultiDeviceMessage(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE)));
                            chatData.setDownloadFilePath(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH)));
                            chatData.setMediaUploadingOrDownloadingPercentage(cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE)));
                            LOG.info("chat message: " + chatData.getMessage());
                            mAllChatMessages.add(chatData);
                        }
                        cursor.close();
                        mAdapter.notifyDataSetChanged();
                        mRecyclerView.scrollToPosition(mAllChatMessages.size() - 1);

                    }
                } else if (intent.getAction().equals(CSEvents.CSCHAT_CHATUPDATED)) {
                    LOG.info("onReceive: mFriendNumber " + mFriendNumber + " desti " + intent.getStringExtra(ChatConstants.INTENT_DESTINATION_NUMBER));
                    if (mFriendNumber.equals(intent.getStringExtra(ChatConstants.INTENT_DESTINATION_NUMBER))) {
                        LOG.info("Chat ID: " + intent.getStringExtra(ChatConstants.INTENT_CHAT_ID));
                        Cursor cursor = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_ID, intent.getStringExtra(ChatConstants.INTENT_CHAT_ID));

                        if (cursor.moveToNext()) {
                            ChatData chatData = new ChatData();
                            chatData.setChatId(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID)));
                            chatData.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE)));
                            chatData.setThumbnail(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY)));
                            chatData.setTimestamp("" + cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TIME)));
                            chatData.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME)));
                            chatData.setUploadFilePath(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH)));
                            chatData.setContentType(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_CONTENT_TYPE)));
                            chatData.setMessageStatus(cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)));
                            chatData.setMessageType(cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MSG_TYPE)));
                            chatData.setSender(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_IS_SENDER)) == 1);
                            chatData.setIsMultiDeviceMessage(cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE)));
                            chatData.setDownloadFilePath(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH)));
                            chatData.setMediaUploadingOrDownloadingPercentage(cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE)));
                            LOG.info("chat message: " + chatData.getMessage());
                            for (int i = 0; i < mAllChatMessages.size(); i++) {
                                if (mAllChatMessages.get(i).getChatId().equals(chatData.getChatId())) {
                                    mAllChatMessages.set(i, chatData);
                                }
                            }
                        }
                        cursor.close();

                        mAdapter.notifyDataSetChanged();

                    }
                } else if (intent.getAction().equals(CSEvents.CSCHAT_ISTYPING)) {
                    LOG.info("compare isTypingReq:" + mFriendNumber);
                    LOG.info("compare destination number:" + intent.getStringExtra(ChatConstants.INTENT_DESTINATION_NUMBER));
                    boolean istyping = intent.getBooleanExtra("istyping", false);

                    if (mFriendNumber.equals(intent.getStringExtra(ChatConstants.INTENT_DESTINATION_NUMBER)) && istyping && intent.getStringExtra("destinationgroupid").equals("")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                mToolbarSubTitleTextView.setVisibility(View.VISIBLE);
                                mToolbarSubTitleTextView.setText("Typing..");

                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mPresenceStatus != null && mPresenceStatus.length() > 0) {
                                            mToolbarSubTitleTextView.setVisibility(View.VISIBLE);
                                            mToolbarSubTitleTextView.setText(mPresenceStatus);
                                        } else {
                                            mToolbarSubTitleTextView.setVisibility(View.VISIBLE);
                                            mToolbarSubTitleTextView.setText("Online");
                                        }

                                    }
                                }, 1000);

                            }
                        });

                    }
                } else if (intent.getAction().equals(CSEvents.CSCHAT_GETPRESENCE_RESPONSE)) {
                    try {
                        if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {

                            if (mFriendNumber.equals(intent.getStringExtra("presencenumber"))) {
                                String presencestatus = intent.getStringExtra("presencestatus");
                                long lastseentime = intent.getLongExtra("lastseentime", 0);
                                LOG.info("presence status: " + presencestatus);
                                LOG.info("last seen time: " + lastseentime);
                                if (presencestatus.equals("ONLINE")) {
                                    mPresenceStatus = "Online";

                                } else {
                                    if (DateUtils.isToday(lastseentime)) {
                                        mPresenceStatus = "last seen today at " + new SimpleDateFormat("hh:mm a").format(lastseentime);
                                    } else {
                                        mPresenceStatus = "last seen at " + new SimpleDateFormat("dd-MM-yyyy hh:mm a").format(lastseentime);
                                    }

                                }
                                mToolbarSubTitleTextView.setText(mPresenceStatus);
                                mToolbarSubTitleTextView.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (intent.getAction().equals(CSEvents.CSCHAT_UPLOADFILEDONE)) {
                    String chatID = intent.getStringExtra(ChatConstants.INTENT_CHAT_ID);

                    for (int i = 0; i < mAllChatMessages.size(); i++) {
                        ChatData chatData = mAllChatMessages.get(i);
                        if (chatData.getChatId().equals(chatID)) {
                            chatData.setMediaDownloadingOrUploading(false);
                            mAllChatMessages.set(i, chatData);
                            if (mAdapter != null)
                                mAdapter.notifyItemChanged(chatData.getAdapterPosition());
                        }
                    }
                } else if (intent.getAction().equals(CSEvents.CSCHAT_UPLOADFILEFAILED)) {
                    String chatID = intent.getStringExtra(ChatConstants.INTENT_CHAT_ID);
                    LOG.info("CSCHAT_UPLOADFILEFAILED--" + chatID);
                    LOG.info("chat count--" + mAllChatMessages.size());
                    for (int i = 0; i < mAllChatMessages.size(); i++) {
                        ChatData chatData = mAllChatMessages.get(i);
                        LOG.info("getChatId--" +chatData.getChatId());
                        if (chatData.getChatId().equals(chatID)) {

                            chatData.setMediaDownloadingOrUploading(false);
                            chatData.setChancelClicked(true);
                            mAllChatMessages.set(i, chatData);
                            LOG.info("mAdapter--" +mAdapter);
                            if (mAdapter != null) {
                                LOG.info("notifyItemChanged--" + chatData.getAdapterPosition());
                                mAdapter.notifyItemChanged(chatData.getAdapterPosition());
                            }
                        }
                    }
                } else if (intent.getAction().equals(CSEvents.CSCHAT_UPLOADPROGRESS)) {
                    String chatID = intent.getStringExtra(ChatConstants.INTENT_CHAT_ID);
                    int percentage = intent.getIntExtra("transferpercentage", 0);
                    LOG.info("upload progress percentage:" + percentage);

                    for (int i = 0; i < mAllChatMessages.size(); i++) {
                        ChatData chatData = mAllChatMessages.get(i);
                        if (chatData.getChatId().equals(chatID)) {
                            if (!chatData.isChancelClicked())
                                chatData.setMediaDownloadingOrUploading(true);
                            chatData.setMediaUploadingOrDownloadingPercentage(percentage);
                            mAllChatMessages.set(i, chatData);
                            if (mAdapter != null)
                                mAdapter.notifyItemChanged(chatData.getAdapterPosition());
                        }
                    }

                } else if (intent.getAction().equals(CSEvents.CSCHAT_DOWNLOADPROGRESS)) {
                    String chatID = intent.getStringExtra(ChatConstants.INTENT_CHAT_ID);
                    int percentage = intent.getIntExtra("transferpercentage", 0);
                    LOG.info("Download percentage:" + percentage);

                    for (int i = 0; i < mAllChatMessages.size(); i++) {
                        ChatData chatData = mAllChatMessages.get(i);
                        if (chatData.getChatId().equals(chatID)) {
                            chatData.setMediaDownloadingOrUploading(true);
                            chatData.setMediaUploadingOrDownloadingPercentage(percentage);

                            mAllChatMessages.set(i, chatData);
                            if (mAdapter != null)
                                mAdapter.notifyItemChanged(chatData.getAdapterPosition());
                        }
                    }
                } else if (intent.getAction().equals(CSEvents.CSCHAT_DOWNLOADFILEDONE)) {
                    String chatID = intent.getStringExtra(ChatConstants.INTENT_CHAT_ID);

                    for (int i = 0; i < mAllChatMessages.size(); i++) {
                        ChatData chatData = mAllChatMessages.get(i);
                        if (chatData.getChatId().equals(chatID)) {
                            chatData.setMediaDownloadingOrUploading(false);
                            chatData.setDownloadFilePath(intent.getStringExtra("filepath"));
                            LOG.info("onReceive: downlaod file path " + intent.getStringExtra("filepath"));
                            scannMediaFile(intent.getStringExtra("filepath"));
                            mAllChatMessages.set(i, chatData);
                            if (mAdapter != null)
                                mAdapter.notifyItemChanged(chatData.getAdapterPosition());
                        }
                    }

                } else if (intent.getAction().equals(CSEvents.CSCHAT_DOWNLOADFILEFAILED)) {
                    String chatID = intent.getStringExtra(ChatConstants.INTENT_CHAT_ID);
                    Log.e("chatID","chatID---->"+chatID);

                    for (int i = 0; i < mAllChatMessages.size(); i++) {
                        ChatData chatData = mAllChatMessages.get(i);
                        if (chatData.getChatId().equals(chatID)) {
                            chatData.setMediaDownloadingOrUploading(false);
                            mAllChatMessages.set(i, chatData);
                            if (mAdapter != null)
                                mAdapter.notifyItemChanged(chatData.getAdapterPosition());
                        }
                    }

                } else if (intent.getAction().equals(CSEvents.CSCLIENT_IMAGESDBUPDATED) || intent.getAction().equals(CSEvents.CSCLIENT_USERPROFILECHANGED)) {
                    getProfilePicture();
                }
                else if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    String chatID = intent.getStringExtra(ChatConstants.INTENT_CHAT_ID);
                    Log.e("chatID","chatID---->"+chatID);

                    for (int i = 0; i < mAllChatMessages.size(); i++) {
                        ChatData chatData = mAllChatMessages.get(i);
                        if (chatData.getChatId().equals(chatID)) {
                            chatData.setMediaDownloadingOrUploading(false);
                            mAllChatMessages.set(i, chatData);
                            if (mAdapter != null)
                                mAdapter.notifyItemChanged(chatData.getAdapterPosition());
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This class will get the profile image of contact and set to ImageView
     */
    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        boolean scaleit = false;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap photo = null;
            try {
                if (params[0].equals("app")) {
                    photo = CSDataProvider.getImageBitmap(params[1]);
                    if (photo == null) {
                        photo = Utils.loadContactPhoto(mContext, Long.parseLong(params[2]));
                    }
                } else if (params[0].equals("native")) {
                    photo = Utils.loadContactPhoto(mContext, Long.parseLong(params[1]));
                } else if (params[0].equals("group") && photo == null) {
                    photo = CSDataProvider.getImageBitmap(params[1]);
                }

                if (photo == null) {
                    photo = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_contact_avatar);
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    if (photo == null) {
                        photo = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_contact_avatar);

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
     * This is handle camera and gallery using popup
     *
     * @return which returns boolean  value
     */

    public boolean popupusertoselectimagesource() {
        try {
            final Dialog dialog = new Dialog(ChatActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.chat_popup);
            TextView tv_video = (TextView) dialog.findViewById(R.id.tv_profile_video);
            TextView tv_camera = (TextView) dialog.findViewById(R.id.tv_profile_camera);
            TextView tv_gallery = (TextView) dialog.findViewById(R.id.tv_profile_gallery);
            tv_video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    boolean callRunning = mPrefereceProvider.getPrefBoolean("CallRunning");
                    if (callRunning) {
                        Toast.makeText(ChatActivity.this, getResources().getString(R.string.video_record_error_during_call), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }

                    if (PermissionUtils.checkCameraPermission(ChatActivity.this) && PermissionUtils.checkReadExternalStoragePermission(ChatActivity.this)) {
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
                        startActivityForResult(takeVideoIntent, VIDEO_GALLERY_REQUEST_CODE);
                        mImagePath = ImagePath.getAbsolutePath();
                        dialog.dismiss();
                    } else {
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                    }
                }
            });
            tv_camera.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    boolean callRunning = mPrefereceProvider.getPrefBoolean("CallRunning");
                    if (callRunning) {
                        Toast.makeText(ChatActivity.this, getResources().getString(R.string.video_record_error_during_call), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }

                    if (PermissionUtils.checkCameraPermission(ChatActivity.this) && PermissionUtils.checkReadExternalStoragePermission(ChatActivity.this)) {
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
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                    }
                }
            });

            tv_gallery.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!PermissionUtils.checkReadExternalStoragePermission(ChatActivity.this)) {
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                        return;
                    }
                    Intent i = new Intent(ChatActivity.this, ShowImagesActivity.class);
                    //  i.setType("image/* video/*");
                    startActivityForResult(i, IMAGE_GALLERY_REQUEST_CODE);

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

    /**
     * Method is used to check file exists or not
     *
     * @param path     this is used for file path exist or not
     * @param fileName this is used for given file name exists or not
     * @return which returns file
     */
    public static File checkFileExistence(String path, String fileName) {
        if (!new File(path).exists()) {
            new File(path).mkdirs();
        }
        File file = new File(path, fileName);
        return file;
    }

    /**
     * This method is to handle location and contact sharing
     *
     * @param context refers current object
     * @param lin     listview
     * @return which returns popup window
     */
    private PopupWindow dialog_Select(final Context context, RecyclerView lin) {

        final PopupWindow dialog_Select = new PopupWindow(context);

        View v = View.inflate(context, R.layout.contacts_popup, null);


        dialog_Select.setContentView(v);
        dialog_Select.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        dialog_Select.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        dialog_Select.setFocusable(true);
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


        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ll_share.getLayoutParams();
        params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen._40dp));
        ll_share.setLayoutParams(params);


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
                if (!PermissionUtils.checkReadContactsPermission(ChatActivity.this)) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 101);
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
                if (PermissionUtils.checkLocationPermission(ChatActivity.this)) {
                    Intent i_map = new Intent(context, ChatMapActivity.class);
                    startActivityForResult(i_map, LOCATION_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
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

    /**
     * Method is generate random file name
     *
     * @param string this is used for range of file
     * @return which returns random file name
     */
    public String CreateRandomAudioFileName(int string) {
        StringBuilder stringBuilder = new StringBuilder(string);
        int i = 0;
        while (i < string) {
            stringBuilder.append(RandomAudioFileName.
                    charAt(random.nextInt(RandomAudioFileName.length())));

            i++;
        }
        return stringBuilder.toString();
    }

    /**
     * This is handle to display popup when user share audio
     */
    private void invokeAudioDialog() {

        if (popup_close_dialog == null) {

            try {

                popup_close_dialog = new Dialog(ChatActivity.this);

                popup_close_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                popup_close_dialog.setContentView(R.layout.alertdialog_close);
                popup_close_dialog.setCancelable(false);
                popup_close_dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));


                Button yes = popup_close_dialog
                        .findViewById(R.id.btn_alert_ok);
                Button no = popup_close_dialog
                        .findViewById(R.id.btn_alert_cancel);

                yes.setText(getString(R.string.chat_screen_alert_send));
                no.setText(getString(R.string.chat_screen_alert_cancel));


                TextView tv_title = popup_close_dialog
                        .findViewById(R.id.tv_alert_title);
                TextView tv_message = popup_close_dialog
                        .findViewById(R.id.tv_alert_message);

                tv_message.setText(getString(R.string.chat_screen_alert_message));


                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popup_close_dialog != null)
                            popup_close_dialog.dismiss();
                        popup_close_dialog = null;

                        try {

                            if (AudioSavePathInDevice != null) {
                                mCSChat.sendAudio(mFriendNumber, AudioSavePathInDevice, false);
                            }

                        } catch (Exception e) {

                            e.printStackTrace();
                        }

                        isRecordButtonLongPressed = false;

                    }
                });
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popup_close_dialog != null)
                            popup_close_dialog.dismiss();
                        popup_close_dialog = null;

                        try {
                            File file = new File(AudioSavePathInDevice);
                            file.delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                });

                if (popup_close_dialog != null)
                    popup_close_dialog.show();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private void scannMediaFile(String file) {

        // refreshing data in Gallery after download...
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(new File(file)); //out is your file you saved/deleted/moved/copied
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
        } else {
            sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://"
                            + Environment.getExternalStorageDirectory())));
        }
    }

    public class ChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements NetworkChangeCallback {

        private static final String TAG = "ChatAdapter";
        private Context mContext;
        private ArrayList<ChatData> allMessages;
        private CSChat mCSChat;
        private int mDeviceWidth = 0;
        private Handler mHandler = new Handler();
        private ChatInterface mChatInterface;
        public boolean mIsChatSelectionEnabled = false;
        // Audio player variables
        private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
        public  int PLAYING_PROGRESS = 0;
        private MediaPlayer mMediaPlayer;
        private SeekBar mSeekBar;
        private TextView mAudioCountTextView;
        private ImageView mPlayPauseImageView;
        private double mTimeElapsed = 0, mFinalTime = 0, mTimeProgressUpdate;
        private Handler mDurationHandler = new Handler();
        private String mAudioPlayingMessageID = "", mCurrentAudioMessageId = "";
        private boolean mIsAudioPaused = false;

        public ChatsAdapter(Context context, ArrayList<ChatData> allMessages, CSChat CSChat) {
            mContext = context;
            this.allMessages = allMessages;
            mCSChat = CSChat;

            mChatInterface = (ChatInterface) mContext;
            // Getting device width
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            mDeviceWidth = size.x;
            //int height = size.y;
            mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            LOG.info("AUDIOFOCUS_GAIN");
                            if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                                if (mIsAudioPaused) {
                                    mIsAudioPaused = false;
                                    mMediaPlayer.start();
                                    if (mPlayPauseImageView != null)
                                        mPlayPauseImageView.setBackgroundResource(R.drawable.ic_video_pause);
                                }
                            }
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                            LOG.info("AUDIOFOCUS_GAIN_TRANSIENT");
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                            LOG.info("AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                            LOG.info("AUDIOFOCUS_LOSS");
                            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                                mMediaPlayer.pause();
                                mIsAudioPaused = true;

                                if (mPlayPauseImageView != null)
                                    mPlayPauseImageView.setBackgroundResource(R.drawable.ic_video_play);
                            }
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            LOG.info("AUDIOFOCUS_LOSS_TRANSIENT");
                            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                                mMediaPlayer.pause();
                                mIsAudioPaused = true;

                                if (mPlayPauseImageView != null)
                                    mPlayPauseImageView.setBackgroundResource(R.drawable.ic_video_play);
                            }
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            LOG.info("AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                            break;
                        case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                            LOG.info("AUDIOFOCUS_REQUEST_FAILED");
                            break;
                        default:
                            //
                    }
                }
            };


        }

        @Override
        public void onNetworkChanged(boolean status) {
            Log.e("ChatActivity","Status: " + status);
        }

        public class ChatViewHolder extends RecyclerView.ViewHolder {
            int position;
            LinearLayout parentLayout;
            // sender views
            TextView senderMessageTextView, senderTimeStampTextView, senderLocationAddressTextView, senderDocTypeTv, unreadMessagesTv, dateTv, senderDocumentNameTv, senderContactNameTv;
            LinearLayout senderLayout, unreadMessageLayout, senderConatctLayout;
            RelativeLayout senderLocationLayout, senderImageLayout, senderDocumentLayout;
            ImageView senderLocationImageView, senderMediaImageView, senderVideoPlayIcon, senderImageDownloadImg, senderDocDownloadImg, senderAudioDownloadImg, senderCancelImgUpload, senderUploadImg, senderDocUploadImg, senderDocCancelDonloadImg;
            CircleProgressBar senderImageProgressbarLayout;
            CircleProgressBar senderDocumentProgress;
            ImageView senderStatusImageView;
            // sender audio layouts
            LinearLayout senderAudioLayout;
            ImageView senderAudioPlayPauseImageView, senderAudioUploadImg, senderAudioCancelDownloadImg;
            SeekBar senderAudioSeekBar;
            TextView senderTimerTextView;
            CircleProgressBar senderAudioProgressBar;
            // receiver view
            TextView receiverMessageTextView, receiverTimestampTextView, receiverLocationAddressTextView, receiverDocTypeTv, receiverDocumentNameTv, receiverContactNameTv;
            LinearLayout receiverLayout, receiverContactLayout;
            RelativeLayout receiverLocationLayout, receiverImageLayout, receiverDocumentLayout;
            ImageView receiverLocationImageView, receiverMediaImageView, receiverVideoPlayIcon, receiverImageDownloadImg, receiverDocDownloadImg, receiverAudioDownloadImg, receiverCancelImgDownLoad, receiverCancelAudioDownload, receiverCancelDocDownload;
            CircleProgressBar receiverImageProgressbarLayout;
            CircleProgressBar receiverDocumentProgress;

            // Receiver audio layouts
            LinearLayout receiverAudioLayout;
            ImageView receiverAudioPlayPauseImageView;
            SeekBar receiverAudioSeekBar;
            TextView receiverTimerTextView;
            CircleProgressBar receiverAudioProgressBar;

            public ChatViewHolder(View itemView) {
                super(itemView);
                // sender views
                parentLayout = itemView.findViewById(R.id.chat_adapter_parent_layout);

                senderLayout = itemView.findViewById(R.id.sender_layout);
                senderMessageTextView = itemView.findViewById(R.id.sender_message);
                senderLocationLayout = itemView.findViewById(R.id.sender_location_layout);
                senderLocationImageView = itemView.findViewById(R.id.sender_location_image_view);
                senderLocationAddressTextView = itemView.findViewById(R.id.sender_location_address);
                senderTimeStampTextView = itemView.findViewById(R.id.sender_timestamp);
                senderImageLayout = itemView.findViewById(R.id.sender_image_layout);
                senderMediaImageView = itemView.findViewById(R.id.sender_media_image_view);
                senderVideoPlayIcon = itemView.findViewById(R.id.sender_video_play_icon);
                senderImageProgressbarLayout = itemView.findViewById(R.id.sender_image_progressbar_layout);
                senderDocumentLayout = itemView.findViewById(R.id.sender_documents_layout);
                senderDocTypeTv = itemView.findViewById(R.id.sender_document_file_type);
                senderDocumentProgress = itemView.findViewById(R.id.sender_document_progress);
                senderStatusImageView = itemView.findViewById(R.id.sender_message_status);
                unreadMessageLayout = itemView.findViewById(R.id.unread_messages_layout);
                unreadMessagesTv = itemView.findViewById(R.id.unread_messages_tv);
                dateTv = itemView.findViewById(R.id.chat_date_tv);
                senderDocumentNameTv = itemView.findViewById(R.id.sender_document_tv);
                senderConatctLayout = itemView.findViewById(R.id.sender_contact_layout);
                senderContactNameTv = itemView.findViewById(R.id.sender_contact_name_tv);
                senderAudioDownloadImg = itemView.findViewById(R.id.sender_audio_download_img);
                senderDocDownloadImg = itemView.findViewById(R.id.sender_document_download_img);
                senderImageDownloadImg = itemView.findViewById(R.id.sender_download_img);
                senderCancelImgUpload = itemView.findViewById(R.id.sender_cancel_img);
                senderUploadImg = itemView.findViewById(R.id.sender_upload_img);
                senderDocCancelDonloadImg = itemView.findViewById(R.id.sender_document_cancel_img);
                senderDocUploadImg = itemView.findViewById(R.id.sender_document_upload_img);
                // sender audio layouts
                senderAudioLayout = itemView.findViewById(R.id.sender_audio_layout);
                senderAudioPlayPauseImageView = itemView.findViewById(R.id.sender_audio_play_pause);
                senderAudioSeekBar = itemView.findViewById(R.id.sender_audio_seekBar);
                senderTimerTextView = itemView.findViewById(R.id.sender_audio_timer);
                senderAudioProgressBar = itemView.findViewById(R.id.sender_audio_progress_bar);
                senderAudioUploadImg = itemView.findViewById(R.id.sender_audio_upload_img);
                senderAudioCancelDownloadImg = itemView.findViewById(R.id.sender_audio_cancel_img);


                // receiver views
                receiverMessageTextView = itemView.findViewById(R.id.receiver_message);
                receiverLayout = itemView.findViewById(R.id.receiver_layout);
                receiverLocationLayout = itemView.findViewById(R.id.receiver_location_layout);
                receiverLocationImageView = itemView.findViewById(R.id.receiver_location_image_view);
                receiverLocationAddressTextView = itemView.findViewById(R.id.receiver_location_address);
                receiverTimestampTextView = itemView.findViewById(R.id.receiver_timestamp);
                receiverImageLayout = itemView.findViewById(R.id.receiver_image_layout);
                receiverVideoPlayIcon = itemView.findViewById(R.id.receiver_video_play_icon);
                receiverMediaImageView = itemView.findViewById(R.id.receiver_media_image_view);
                receiverImageProgressbarLayout = itemView.findViewById(R.id.receiver_image_progressbar_layout);
                receiverDocTypeTv = itemView.findViewById(R.id.receiver_document_file_type);
                receiverDocumentLayout = itemView.findViewById(R.id.documents_layout);
                receiverDocumentProgress = itemView.findViewById(R.id.receiver_document_progress);
                receiverDocumentNameTv = itemView.findViewById(R.id.receiver_document_tv);
                receiverContactLayout = itemView.findViewById(R.id.receiver_contact_layout);
                receiverContactNameTv = itemView.findViewById(R.id.receiver_contact_name_tv);
                receiverImageDownloadImg = itemView.findViewById(R.id.receive_download_img);
                receiverAudioDownloadImg = itemView.findViewById(R.id.receiver_audio_download_img);
                receiverDocDownloadImg = itemView.findViewById(R.id.receiver_document_download_img);
                receiverCancelImgDownLoad = itemView.findViewById(R.id.receive_cancel_img);
                receiverCancelAudioDownload = itemView.findViewById(R.id.receiver_audio_cancel_img);
                receiverCancelDocDownload = itemView.findViewById(R.id.receiver_document_cancel_img);
                // receiver audio layouts
                receiverAudioLayout = itemView.findViewById(R.id.receiver_audio_layout);
                receiverAudioPlayPauseImageView = itemView.findViewById(R.id.receiver_audio_play_pause);
                receiverAudioSeekBar = itemView.findViewById(R.id.receiver_audio_seekBar);
                receiverTimerTextView = itemView.findViewById(R.id.receiver_audio_timer);
                receiverAudioProgressBar = itemView.findViewById(R.id.receiver_audio_progress_bar);
            }
        }

        @Override
        public int getItemCount() {
            return allMessages.size();
        }

        /**
         * This method update the contacts data according user search filed.
         *
         * @param filteredArrayList
         */
        public void filterList(ArrayList<ChatData> filteredArrayList) {
            allMessages = filteredArrayList;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.adapter_chats, parent, false);

            return new ChatViewHolder(itemView);

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof  ChatViewHolder) {

                final ChatViewHolder chatViewHolder = (ChatViewHolder) holder;
                final ChatData chatData = allMessages.get(position);
                chatViewHolder.senderLayout.setVisibility(View.GONE);
                chatViewHolder.senderMessageTextView.setVisibility(View.GONE);
                chatViewHolder.senderImageLayout.setVisibility(View.GONE);
                chatViewHolder.senderVideoPlayIcon.setVisibility(View.GONE);
                chatViewHolder.senderLocationLayout.setVisibility(View.GONE);
                chatViewHolder.senderDocumentLayout.setVisibility(View.GONE);
                chatViewHolder.receiverDocumentLayout.setVisibility(View.GONE);
                chatViewHolder.receiverDocumentProgress.setVisibility(View.GONE);
                chatViewHolder.receiverLayout.setVisibility(View.GONE);
                chatViewHolder.receiverMessageTextView.setVisibility(View.GONE);
                chatViewHolder.receiverImageLayout.setVisibility(View.GONE);
                chatViewHolder.receiverVideoPlayIcon.setVisibility(View.GONE);
                chatViewHolder.receiverLocationLayout.setVisibility(View.GONE);
                chatViewHolder.receiverContactLayout.setVisibility(View.GONE);
                chatViewHolder.receiverImageDownloadImg.setVisibility(View.GONE);
                chatViewHolder.senderImageDownloadImg.setVisibility(View.GONE);
                chatViewHolder.senderConatctLayout.setVisibility(View.GONE);

                Long dateStr = Long.parseLong(chatData.getTimestamp());
                String prevDate = null;
                String curDate = getFormattedDate(dateStr);
                // String shortTimeStr = getFormattedTime(dateStr);
                //final String filePath = file_Path;
                chatData.setAdapterPosition(position);
                if (position > 0) {
                    prevDate = getFormattedDate(Long.parseLong(allMessages.get(position - 1).getTimestamp()));
                }

                // different from the previous one
                if (prevDate == null || !prevDate.equals(curDate)) {
                    chatViewHolder.dateTv.setVisibility(View.VISIBLE);

                    if (DateUtils.isToday(dateStr)) {
                        chatViewHolder.dateTv.setText(mContext.getString(R.string.chat_screen_today_message));
                    } else if (isYesterday(dateStr)) {
                        chatViewHolder.dateTv.setText(mContext.getString(R.string.chat_screen_yesterday_message));
                    } else {
                        chatViewHolder.dateTv.setText(curDate);
                    }
                } else {
                    chatViewHolder.dateTv.setVisibility(View.GONE);
                }





                // Audio
                chatViewHolder.senderAudioLayout.setVisibility(View.GONE);
                chatViewHolder.receiverAudioLayout.setVisibility(View.GONE);

                String timeStamp = "";
                if (chatData.getTimestamp() != null && chatData.getTimestamp().length() > 0) {
                    long milliSeconds = Long.parseLong(chatData.getTimestamp());
                    DateFormat df;
                    boolean is24fromat = android.text.format.DateFormat.is24HourFormat(mContext);
                    if (is24fromat) {
                        df = new SimpleDateFormat(ChatConstants.TIME_24_FORMAT);
                    } else {
                        df = new SimpleDateFormat(ChatConstants.TIME_FORMAT);
                    }

                    new SimpleDateFormat(ChatConstants.TIME_FORMAT);
                    Date currentDate = new Date(milliSeconds);
                    timeStamp = df.format(currentDate);
                }

                if ((allMessages.size() - ChatActivity.readCount) == 1) {
                    chatViewHolder.unreadMessagesTv.setText(allMessages.size() - ChatActivity.readCount + " Unread message");
                } else {
                    chatViewHolder.unreadMessagesTv.setText(allMessages.size() - ChatActivity.readCount + " Unread messages");
                }
                if (ChatActivity.readCount >= 0 && position == ChatActivity.readCount) {
                    chatViewHolder.unreadMessageLayout.setVisibility(View.VISIBLE);
                } else {
                    chatViewHolder.unreadMessageLayout.setVisibility(View.GONE);
                }

                if (chatData.isChatSelected()) {
                    chatViewHolder.parentLayout.setBackgroundColor(Color.parseColor("#4dF4AD1B"));
                } else {
                    // sets transparent color
                    chatViewHolder.parentLayout.setBackgroundColor(Color.parseColor("#00000000"));
                }
               /* if(networkChangeReceiver.isNetworkAvailable(getApplicationContext())){
                    // chatViewHolder.receiverImageDownloadImg.setVisibility(View.GONE);
                }else {


                    chatViewHolder.senderLayout.setVisibility(View.VISIBLE);
                    chatViewHolder.senderImageLayout.setVisibility(View.VISIBLE);
                    chatViewHolder.senderCancelImgUpload.setVisibility(View.GONE);
                    chatViewHolder.senderUploadImg.setVisibility(View.VISIBLE);
                    // chatViewHolder.senderMediaImageView.setBackground(getResources().getDrawable(R.drawable.download_background));
                    chatViewHolder.senderUploadImg.setImageDrawable(getResources().getDrawable(R.drawable.upload));
                    Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.signup_no_internet_avail),
                            Toast.LENGTH_SHORT).show();
                }*/
                if (chatData.isSender()) {
                    chatViewHolder.senderLayout.setVisibility(View.VISIBLE);
                    LOG.info("onBindViewHolder: " + chatViewHolder);
                    chatViewHolder.senderTimeStampTextView.setText("" + timeStamp);

                    chatViewHolder.senderStatusImageView.setBackgroundResource(R.drawable.message);
                    chatViewHolder.senderDocumentProgress.setVisibility(View.GONE);
                    LOG.info("onBindViewHolder: message status " + chatData.getMessageStatus());
                    switch (chatData.getMessageStatus()) {
                        case CSConstants.MESSAGE_SENT:
                            chatViewHolder.senderStatusImageView.setBackgroundResource(R.drawable.mytick);
                            break;
                        case CSConstants.MESSAGE_DELIVERED:
                            chatViewHolder.senderStatusImageView.setBackgroundResource(R.drawable.tick1);
                            break;
                        case CSConstants.MESSAGE_READ:
                            chatViewHolder.senderStatusImageView.setBackgroundResource(R.drawable.tick);
                            break;
                        case CSConstants.MESSAGE_SENT_FAILED:
                            chatViewHolder.senderDocumentProgress.setVisibility(View.GONE);
                            break;
                        default:
                            chatViewHolder.senderDocumentProgress.setVisibility(View.VISIBLE);
                            break;

                    }
                    LOG.info("File Name " + chatData.getFileName());
                    LOG.info("Message Type in Sender: " + chatData.getMessageType());
                    LOG.info("FilePatth " + chatData.getUploadFilePath());

                    LOG.info("chat id--"+chatData.getChatId());
                    Log.e("isMedia","isMediaDownloadingOrUploading---"+chatData.isMediaDownloadingOrUploading());
                    switch (chatData.getMessageType()) {
                        case CSConstants.E_IMAGE:

                            chatViewHolder.senderImageLayout.setVisibility(View.VISIBLE);
                            chatViewHolder.senderMediaImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            chatViewHolder.senderCancelImgUpload.setVisibility(View.GONE);
                            chatViewHolder.senderUploadImg.setVisibility(View.GONE);

                            if (chatData.isMediaDownloadingOrUploading()) {
                                chatViewHolder.senderCancelImgUpload.setVisibility(View.VISIBLE);
                                chatViewHolder.senderVideoPlayIcon.setVisibility(View.GONE);
                                chatViewHolder.senderImageProgressbarLayout.setVisibility(View.VISIBLE);
                                chatViewHolder.senderImageProgressbarLayout.setProgress(chatData.getMediaUploadingOrDownloadingPercentage());
                            } else {

                                chatViewHolder.senderImageProgressbarLayout.setVisibility(View.GONE);
                                chatViewHolder.senderCancelImgUpload.setVisibility(View.GONE);
                                if (chatData.getIsMultiDeviceMessage() == 0) {
                                    if (chatData.getMediaUploadingOrDownloadingPercentage() < 100 && chatData.isChancelClicked()) {
                                        chatViewHolder.senderUploadImg.setVisibility(View.VISIBLE);
                                    } else if (chatData.getMediaUploadingOrDownloadingPercentage() < 100) {
                                        chatViewHolder.senderImageProgressbarLayout.setVisibility(View.VISIBLE);
                                        chatViewHolder.senderCancelImgUpload.setVisibility(View.VISIBLE);
                                        chatViewHolder.senderImageProgressbarLayout.setProgress(chatData.getMediaUploadingOrDownloadingPercentage());
                                    }
                                }
                            }
                            if (!chatData.isOriginalImageLoaded() && chatData.getThumbnail() != null && chatData.getThumbnail().length() > 0) {
                                new  ImageDownloaderTask(chatViewHolder.position, chatViewHolder, chatViewHolder.senderMediaImageView, chatData.getThumbnail(), false).execute();
                            }
                            //}
                            if (chatData.getIsMultiDeviceMessage() == 0) {
                                if (chatData.getUploadFilePath().length() > 0) {
                                    LOG.info("File Path: " + chatData.getUploadFilePath());
                                    Glide.with(mContext)
                                            .load(new File(chatData.getUploadFilePath()))
                                            .listener(new RequestListener<Drawable>() {
                                                @Override
                                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                    // log exception
                                                    LOG.error("Error loading image", e);
                                                    return false; // important to return false so the error placeholder can be placed
                                                }

                                                @Override
                                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                                    if (!chatData.isOriginalImageLoaded()) {
                                                        mHandler.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                notifyDataSetChanged();
                                                            }
                                                        });
                                                        chatData.setOriginalImageLoaded(true);
                                                    }
                                                    return false;
                                                }
                                            })
                                            .into(chatViewHolder.senderMediaImageView);
                                }

                            } else {
                                String filePath = chatData.getDownloadFilePath();
                            /*if (chatData.getFileName().equalsIgnoreCase("")) {
                                filePath = ChatConstants.CHAT_IMAGES_DIRECTORY + "/" + chatData.getDownloadFilePath();
                            } else {
                                filePath = ChatConstants.CHAT_IMAGES_DIRECTORY + "/" + chatData.getFileName();
                            }*/
                                LOG.info("onBindViewHolder: uplaod and download filepaths " + filePath + " " + chatData.getDownloadFilePath());
                                if (new File(filePath).exists() && chatData.getMediaUploadingOrDownloadingPercentage() == 100) {

                                    if (filePath.length() > 0) {
                                        LOG.info("File Path: " + filePath);

                                        Glide.with(mContext)
                                                .load(new File(filePath))
                                                .listener(new RequestListener<Drawable>() {
                                                    @Override
                                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                        // log exception
                                                        LOG.error("Error loading image", e);
                                                        return false; // important to return false so the error placeholder can be placed
                                                    }

                                                    @Override
                                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                                        if (!chatData.isOriginalImageLoaded()) {
                                                            mHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    notifyDataSetChanged();
                                                                }
                                                            });
                                                            chatData.setOriginalImageLoaded(true);
                                                        }
                                                        return false;
                                                    }
                                                })
                                                .into(chatViewHolder.senderMediaImageView);
                                    }
                                } else {

                                    LOG.info("File not exists downloading file: " + filePath);
                                    //  mCSChat.downloadFile(chatData.getChatId(), filePath);
                                    if (chatData.isMediaDownloadingOrUploading()) {
                                        chatViewHolder.senderImageProgressbarLayout
                                                .setVisibility(View.VISIBLE);
                                        chatViewHolder.senderImageDownloadImg.setVisibility(View.GONE);
                                    } else {
                                        chatViewHolder.senderImageProgressbarLayout
                                                .setVisibility(View.GONE);
                                        chatViewHolder.senderImageDownloadImg.setVisibility(View.VISIBLE);
                                    }
                                }

                            }


                            break;
                        case CSConstants.E_VIDEO:

                            chatViewHolder.senderImageLayout.setVisibility(View.VISIBLE);
                            chatViewHolder.senderMediaImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            chatViewHolder.senderCancelImgUpload.setVisibility(View.GONE);
                            chatViewHolder.senderUploadImg.setVisibility(View.GONE);

                            if (chatData.isMediaDownloadingOrUploading()) {
                                chatViewHolder.senderCancelImgUpload.setVisibility(View.VISIBLE);
                                chatViewHolder.senderVideoPlayIcon.setVisibility(View.GONE);
                                chatViewHolder.senderImageProgressbarLayout.setVisibility(View.VISIBLE);
                                chatViewHolder.senderImageProgressbarLayout.setProgress(chatData.getMediaUploadingOrDownloadingPercentage());
                            } else {
                                chatViewHolder.senderImageProgressbarLayout.setVisibility(View.GONE);
                                chatViewHolder.senderCancelImgUpload.setVisibility(View.GONE);
                                if (chatData.getIsMultiDeviceMessage() == 0) {
                                    if (chatData.getMediaUploadingOrDownloadingPercentage() < 100 && chatData.isChancelClicked()) {
                                        chatViewHolder.senderUploadImg.setVisibility(View.VISIBLE);
                                    } else if (chatData.getMediaUploadingOrDownloadingPercentage() < 100) {
                                        chatViewHolder.senderImageProgressbarLayout.setVisibility(View.VISIBLE);
                                        chatViewHolder.senderCancelImgUpload.setVisibility(View.VISIBLE);
                                        chatViewHolder.senderImageProgressbarLayout.setProgress(chatData.getMediaUploadingOrDownloadingPercentage());
                                    }
                                }
                            }

                            if (chatData.getIsMultiDeviceMessage() == 0) {
                                if (chatData.getUploadFilePath().length() > 0) {
                                    if (chatData.isMediaDownloadingOrUploading()) {
                                        chatViewHolder.senderVideoPlayIcon.setVisibility(View.GONE);
                                    } else {
                                        chatViewHolder.senderVideoPlayIcon.setVisibility(View.VISIBLE);
                                    }

                                    if (new File(chatData.getUploadFilePath()).exists()) {
                                        Glide.with(mContext)
                                                .asBitmap()
                                                .load(chatData.getUploadFilePath()) // or URI/path
                                                .into(chatViewHolder.senderMediaImageView);
                                    }
                                }
                            } else {
                                //String videoFilePath = ChatConstants.CHAT_VIDEOS_DIRECTORY + "/" + chatData.getFileName();
                                String videoFilePath = chatData.getDownloadFilePath();
                                if (videoFilePath.length() > 0) {
                                    if (new File(videoFilePath).exists() && chatData.getMediaUploadingOrDownloadingPercentage() == 100) {
                                        chatViewHolder.senderVideoPlayIcon.setVisibility(View.VISIBLE);
                                        Glide.with(mContext)
                                                .asBitmap()
                                                .load(videoFilePath) // or URI/path
                                                .into(chatViewHolder.senderMediaImageView);
                                    } else {
                                        LOG.info("File not exists downloading file: " + videoFilePath);
                                        if (chatData.isMediaDownloadingOrUploading()) {
                                            chatViewHolder.senderImageProgressbarLayout
                                                    .setVisibility(View.VISIBLE);
                                            chatViewHolder.senderImageDownloadImg.setVisibility(View.GONE);
                                        } else {
                                            chatViewHolder.senderImageProgressbarLayout
                                                    .setVisibility(View.GONE);
                                            chatViewHolder.senderImageDownloadImg.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            }

                            break;
                        case CSConstants.E_DOCUMENT:
                            try {
                                chatViewHolder.senderDocumentProgress.setVisibility(View.GONE);
                                chatViewHolder.senderDocDownloadImg.setVisibility(View.GONE);
                                chatViewHolder.senderDocUploadImg.setVisibility(View.GONE);
                                chatViewHolder.senderDocCancelDonloadImg.setVisibility(View.GONE);
                                if (chatData.isMediaDownloadingOrUploading()) {
                                    chatViewHolder.senderDocCancelDonloadImg.setVisibility(View.VISIBLE);
                                    chatViewHolder.senderDocumentProgress.setVisibility(View.VISIBLE);
                                    chatViewHolder.senderDocumentProgress.setProgress(chatData.getMediaUploadingOrDownloadingPercentage());
                                    chatViewHolder.senderDocDownloadImg.setVisibility(View.GONE);
                                    chatViewHolder.senderDocUploadImg.setVisibility(View.GONE);
                                } else {
                                    if ((chatData.getMediaUploadingOrDownloadingPercentage() < 100)) {
                                        if (chatData.getIsMultiDeviceMessage() == 0) {
                                            if (chatData.isChancelClicked()) {
                                                chatViewHolder.senderDocumentProgress.setVisibility(View.GONE);
                                                chatViewHolder.senderDocCancelDonloadImg.setVisibility(View.GONE);
                                                chatViewHolder.senderDocUploadImg.setVisibility(View.VISIBLE);
                                            } else {
                                                chatViewHolder.senderDocumentProgress.setVisibility(View.VISIBLE);
                                                chatViewHolder.senderDocCancelDonloadImg.setVisibility(View.VISIBLE);
                                                chatViewHolder.senderDocUploadImg.setVisibility(View.GONE);
                                            }

                                        } else {
                                            chatViewHolder.senderDocumentProgress.setVisibility(View.GONE);
                                            chatViewHolder.senderDocCancelDonloadImg.setVisibility(View.GONE);
                                            chatViewHolder.senderDocDownloadImg.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }

                                if (chatData.getIsMultiDeviceMessage() != 0) {
                                    String documentFilePath = chatData.getDownloadFilePath();
                                    //String documentFilePath = ChatConstants.CHAT_DOCUMENTS_DIRECTORY + "/" + chatData.getFileName();
                                    if (!(new File(documentFilePath).exists()) || chatData.getMediaUploadingOrDownloadingPercentage() < 100) {
                                        if (chatData.isMediaDownloadingOrUploading()) {
                                            chatViewHolder.senderDocCancelDonloadImg.setVisibility(View.VISIBLE);
                                            chatViewHolder.senderDocumentProgress.setVisibility(View.VISIBLE);
                                            chatViewHolder.senderDocumentProgress.setProgress(chatData.getMediaUploadingOrDownloadingPercentage());
                                            chatViewHolder.senderDocumentProgress.setText(String.valueOf(chatData.getMediaUploadingOrDownloadingPercentage()));
                                            chatViewHolder.senderDocumentProgress.setSuffix("%");
                                            chatViewHolder.senderDocDownloadImg.setVisibility(View.GONE);
                                        } else {
                                            chatViewHolder.senderDocumentProgress.setVisibility(View.GONE);
                                            chatViewHolder.senderDocDownloadImg.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                                chatViewHolder.senderDocumentNameTv.setText(getFileNameWithoutExtension(chatData.getFileName()));
                                chatViewHolder.senderDocTypeTv.setText(chatData.getFileName().substring(chatData.getFileName().lastIndexOf(".")).toUpperCase().replace(".", ""));
                                chatViewHolder.senderDocumentLayout.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;

                        case CSConstants.E_LOCATION:
                            chatViewHolder.senderLocationLayout.setVisibility(View.VISIBLE);
                            CSChatLocation location = mCSChat.getLocationFromChatID(chatData.getChatId());

                            String address = location.getAddress();
                            chatViewHolder.senderLocationAddressTextView.setText(address);
                            String filePath = "https://maps.googleapis.com/maps/api/staticmap?center=" + location.getLat() + "," + location.getLng() + "&zoom=16&size=200x200&sensor=false&markers=color:blue%" + location.getLat() + "," + location.getLng() + "&key=AIzaSyBfBYH6e-sguUxKWKl3dKxfoS3rOG6ku1A";
                            LOG.info("onBindViewHolder: location URL " + filePath);
                            Glide.with(mContext)
                                    .load(filePath)
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            // log exception
                                            LOG.error("Error loading image", e);
                                            return false; // important to return false so the error placeholder can be placed
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                            if (!chatData.isOriginalImageLoaded()) {
                                                mHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        notifyDataSetChanged();
                                                    }
                                                });

                                                chatData.setOriginalImageLoaded(true);
                                            }
                                            return false;
                                        }
                                    })
                                    .into(chatViewHolder.senderLocationImageView);
                            break;

                        case CSConstants.E_CONTACT:

                            try {
                                LOG.info("Contact Data: " + chatData.getMessage());
                                JSONObject contactJSONObject = new JSONObject(chatData.getMessage());
                                String contactName = contactJSONObject.getString("name");
                                chatViewHolder.senderConatctLayout.setVisibility(View.VISIBLE);
                                chatViewHolder.senderContactNameTv.setText(contactName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            break;
                        case CSConstants.E_AUDIO:
                            try {

                                chatViewHolder.senderAudioLayout.setVisibility(View.VISIBLE);
                                chatViewHolder.senderAudioPlayPauseImageView.setVisibility(View.GONE);
                                chatViewHolder.senderAudioProgressBar.setVisibility(View.GONE);
                                chatViewHolder.senderAudioDownloadImg.setVisibility(View.GONE);
                                chatViewHolder.senderAudioCancelDownloadImg.setVisibility(View.GONE);
                                chatViewHolder.senderAudioUploadImg.setVisibility(View.GONE);
                                if (chatData.isMediaDownloadingOrUploading()) {
                                    chatViewHolder.senderAudioProgressBar.setVisibility(View.VISIBLE);
                                    chatViewHolder.senderAudioProgressBar.setProgress(chatData.getMediaUploadingOrDownloadingPercentage());
                                    chatViewHolder.senderAudioCancelDownloadImg.setVisibility(View.VISIBLE);
                                    chatViewHolder.senderAudioUploadImg.setVisibility(View.GONE);
                                } else {
                                    if (chatData.getIsMultiDeviceMessage() == 0) {
                                        if (chatData.getMediaUploadingOrDownloadingPercentage() == 100) {
                                            if (chatData.getUploadFilePath().length() > 0) {
                                                chatViewHolder.senderAudioPlayPauseImageView.setVisibility(View.VISIBLE);
                                                if (!mAudioPlayingMessageID.equals(chatData.getChatId()))
                                                    chatViewHolder.senderTimerTextView.setText(getDurationOfAudio(chatData.getUploadFilePath()));
                                            } else {
                                                chatViewHolder.senderAudioLayout.setVisibility(View.GONE);
                                                chatViewHolder.senderMessageTextView.setVisibility(View.VISIBLE);
                                                chatViewHolder.senderMessageTextView.setText("Audio file not found");
                                            }
                                        } else {
                                            if (chatData.isChancelClicked()) {
                                                chatViewHolder.senderAudioUploadImg.setVisibility(View.VISIBLE);
                                                chatViewHolder.senderAudioCancelDownloadImg.setVisibility(View.GONE);
                                                chatViewHolder.senderAudioProgressBar.setVisibility(View.GONE);
                                            } else {
                                                chatViewHolder.senderAudioUploadImg.setVisibility(View.GONE);
                                                chatViewHolder.senderAudioCancelDownloadImg.setVisibility(View.VISIBLE);
                                                chatViewHolder.senderAudioProgressBar.setVisibility(View.VISIBLE);
                                            }

                                        }
                                    } else {
                                        String audioFilePath = chatData.getDownloadFilePath();
                                        //String audioFilePath = ChatConstants.CHAT_AUDIO_DIRECTORY + "/" + chatData.getFileName();
                                        if (((new File(audioFilePath).exists()) && chatData.getMediaUploadingOrDownloadingPercentage() == 100)) {
                                            chatViewHolder.senderAudioPlayPauseImageView.setVisibility(View.VISIBLE);
                                            if (!mAudioPlayingMessageID.equals(chatData.getChatId()))
                                                chatViewHolder.senderTimerTextView.setText(getDurationOfAudio(audioFilePath));
                                        } else {
                                            if (chatData.isMediaDownloadingOrUploading()) {
                                                chatViewHolder.senderAudioProgressBar.setVisibility(View.VISIBLE);
                                                chatViewHolder.senderAudioDownloadImg.setVisibility(View.GONE);
                                            } else {
                                                chatViewHolder.senderAudioProgressBar.setVisibility(View.GONE);
                                                chatViewHolder.senderAudioDownloadImg.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            chatViewHolder.senderMessageTextView.setVisibility(View.VISIBLE);
                            chatViewHolder.senderMessageTextView.setText(chatData.getMessage().trim());
                            break;
                    }

                } else {
                    chatViewHolder.receiverLayout.setVisibility(View.VISIBLE);
                    chatViewHolder.receiverTimestampTextView.setText("" + timeStamp);
                    if (chatData.getMessageStatus() == CSConstants.MESSAGE_RECEIVED || chatData.getMessageStatus() == CSConstants.MESSAGE_DELIVERED_ACK) {
                        LOG.info("onBindViewHolder: sending read receipt");
                        mCSChat.sendReadReceipt(chatData.getChatId());
                    }
                    switch (chatData.getMessageType()) {
                        case CSConstants.E_IMAGE:

                            chatViewHolder.receiverImageLayout.setVisibility(View.VISIBLE);
                            chatViewHolder.receiverMediaImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            chatViewHolder.receiverImageDownloadImg.setVisibility(View.GONE);
                            chatViewHolder.receiverImageProgressbarLayout.setVisibility(View.GONE);
                            chatViewHolder.receiverCancelImgDownLoad.setVisibility(View.GONE);
                            if (!chatData.isOriginalImageLoaded() && chatData.getThumbnail() != null && chatData.getThumbnail().length() > 0) {
                                new  ImageDownloaderTask(chatViewHolder.position, chatViewHolder, chatViewHolder.receiverMediaImageView, chatData.getThumbnail(), false).execute();
                            }
                            String filePath = chatData.getDownloadFilePath();
                        /*if (chatData.getFileName().equalsIgnoreCase("")) {
                            filePath = ChatConstants.CHAT_IMAGES_DIRECTORY + "/" + chatData.getDownloadFilePath();
                        } else {
                            filePath = ChatConstants.CHAT_IMAGES_DIRECTORY + "/" + chatData.getFileName();
                        }*/
                            LOG.info("onBindViewHolder: download percentage " + chatData.getMediaUploadingOrDownloadingPercentage());
                            LOG.info("isExists: "+new File(filePath).exists()+" , FilePath: "+filePath);
                            if (new File(filePath).exists() && chatData.getMediaUploadingOrDownloadingPercentage() == 100) {
                                chatViewHolder.receiverImageProgressbarLayout
                                        .setVisibility(View.GONE);
                                LOG.info("image path:" + filePath);
                                LOG.error("File path :  ", filePath);
                                Glide.with(mContext)
                                        .load(new File(filePath))
                                        .listener(new RequestListener<Drawable>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                // log exception
                                                LOG.error("Error loading image", e);
                                                return false; // important to return false so the error placeholder can be placed
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                                if (!chatData.isOriginalImageLoaded()) {
                                                    mHandler.post(new Runnable() {
                                                        @Override
                                                        public void run()
                                                        {

                                                            notifyDataSetChanged();

                                                        }
                                                    });
                                                    chatData.setOriginalImageLoaded(true);
                                                }
                                                return false;
                                            }
                                        })
                                        .into(chatViewHolder.receiverMediaImageView);
                            } else {
                                LOG.info("File not exists downloading file: " + filePath);
                                //  mCSChat.downloadFile(chatData.getChatId(), filePath);
                                if (chatData.isMediaDownloadingOrUploading()) {
                                    chatViewHolder.receiverCancelImgDownLoad.setVisibility(View.VISIBLE);
                                    chatViewHolder.receiverImageProgressbarLayout
                                            .setVisibility(View.VISIBLE);
                                    chatViewHolder.receiverImageProgressbarLayout.setProgress(chatData.getMediaUploadingOrDownloadingPercentage());
                                    chatViewHolder.receiverImageDownloadImg.setVisibility(View.GONE);
                                } else {
                                    chatViewHolder.receiverImageProgressbarLayout
                                            .setVisibility(View.GONE);
                                    chatViewHolder.receiverImageDownloadImg.setVisibility(View.VISIBLE);
                                }
                            }

                            break;
                        case CSConstants.E_VIDEO:
                            chatViewHolder.receiverImageLayout.setVisibility(View.VISIBLE);
                            chatViewHolder.receiverMediaImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            chatViewHolder.receiverImageProgressbarLayout
                                    .setVisibility(View.GONE);
                            chatViewHolder.receiverCancelImgDownLoad.setVisibility(View.GONE);
                            chatViewHolder.receiverImageDownloadImg.setVisibility(View.GONE);
                            chatViewHolder.receiverVideoPlayIcon.setVisibility(View.GONE);

                            String videoFilePath = chatData.getDownloadFilePath();
                            //String videoFilePath = ChatConstants.CHAT_VIDEOS_DIRECTORY + "/" + chatData.getFileName();
                            LOG.info("Receiver side Video Path: " + videoFilePath + " ,Video exists: " + new File(videoFilePath).exists());

                            if (new File(videoFilePath).exists() && chatData.getMediaUploadingOrDownloadingPercentage() == 100) {
                                chatViewHolder.receiverVideoPlayIcon.setVisibility(View.VISIBLE);
                                Glide.with(mContext)
                                        .asBitmap()
                                        .load(videoFilePath) // or URI/path
                                        .into(chatViewHolder.receiverMediaImageView);
                            } else {
                                LOG.info("File not exists downloading file: " + videoFilePath + " thumbnail path " + chatData.getThumbnail());
                                //   mCSChat.downloadFile(chatData.getChatId(), videoFilePath);
                                new  ImageDownloaderTask(chatViewHolder.position, chatViewHolder, chatViewHolder.receiverMediaImageView, chatData.getThumbnail(), false).execute();
                                if (chatData.isMediaDownloadingOrUploading()) {
                                    chatViewHolder.receiverCancelImgDownLoad.setVisibility(View.VISIBLE);
                                    chatViewHolder.receiverImageProgressbarLayout
                                            .setVisibility(View.VISIBLE);
                                    chatViewHolder.receiverImageProgressbarLayout.setProgress(chatData.getMediaUploadingOrDownloadingPercentage());
                                    chatViewHolder.receiverImageDownloadImg.setVisibility(View.GONE);
                                } else {
                                    chatViewHolder.receiverCancelImgDownLoad.setVisibility(View.GONE);
                                    chatViewHolder.receiverImageProgressbarLayout
                                            .setVisibility(View.GONE);
                                    chatViewHolder.receiverImageDownloadImg.setVisibility(View.VISIBLE);
                                }
                            }

                            break;

                        case CSConstants.E_DOCUMENT:
                            chatViewHolder.receiverDocumentProgress.setVisibility(View.GONE);
                            chatViewHolder.receiverDocDownloadImg.setVisibility(View.GONE);
                            chatViewHolder.receiverCancelDocDownload.setVisibility(View.GONE);
                            chatViewHolder.receiverDocumentNameTv.setSelected(true);
                            String documentFilePath = chatData.getDownloadFilePath();
                            //String documentFilePath = ChatConstants.CHAT_DOCUMENTS_DIRECTORY + "/" + chatData.getFileName();
                            if (!(new File(documentFilePath).exists()) || chatData.getMediaUploadingOrDownloadingPercentage() < 100) {

                                if (chatData.isMediaDownloadingOrUploading()) {
                                    chatViewHolder.receiverCancelDocDownload.setVisibility(View.VISIBLE);
                                    chatViewHolder.receiverDocumentProgress.setVisibility(View.VISIBLE);
                                    chatViewHolder.receiverDocumentProgress.setProgress(chatData.getMediaUploadingOrDownloadingPercentage());
                                    chatViewHolder.receiverDocDownloadImg.setVisibility(View.GONE);
                                } else {
                                    chatViewHolder.receiverCancelDocDownload.setVisibility(View.GONE);
                                    chatViewHolder.receiverDocumentProgress.setVisibility(View.GONE);
                                    chatViewHolder.receiverDocDownloadImg.setVisibility(View.VISIBLE);
                                }
                            }
                            try {
                                chatViewHolder.receiverDocumentNameTv.setText(getFileNameWithoutExtension(chatData.getFileName()));
                                chatViewHolder.receiverDocTypeTv.setText(chatData.getFileName().substring(chatData.getFileName().lastIndexOf(".")).toUpperCase().replace(".", ""));
                                chatViewHolder.receiverDocumentLayout.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case CSConstants.E_LOCATION:
                            chatViewHolder.receiverLocationLayout.setVisibility(View.VISIBLE);
                            CSChatLocation location = mCSChat.getLocationFromChatID(chatData.getChatId());

                            String address = location.getAddress();
                            chatViewHolder.receiverLocationAddressTextView.setText(address);

                            String filePath1 = "https://maps.googleapis.com/maps/api/staticmap?center=" + location.getLat() + "," + location.getLng() + "&zoom=16&size=200x200&sensor=false&markers=color:blue%" + location.getLat() + "," + location.getLng() + "&key=AIzaSyBfBYH6e-sguUxKWKl3dKxfoS3rOG6ku1A";
                            Glide.with(mContext)
                                    .load(filePath1)
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            // log exception
                                            LOG.error("Error loading image", e);
                                            return false; // important to return false so the error placeholder can be placed
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                            if (!chatData.isOriginalImageLoaded()) {
                                                mHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        notifyDataSetChanged();
                                                    }
                                                });

                                                chatData.setOriginalImageLoaded(true);
                                            }
                                            return false;
                                        }
                                    })
                                    .into(chatViewHolder.receiverLocationImageView);
                            break;
                        case CSConstants.E_CONTACT:
                            try {
                                LOG.info("Contact Data: " + chatData.getMessage());
                                JSONObject contactJSONObject = new JSONObject(chatData.getMessage());
                                String contactName = contactJSONObject.getString("name");
                                chatViewHolder.receiverContactLayout.setVisibility(View.VISIBLE);
                                chatViewHolder.receiverContactNameTv.setText(contactName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            break;
                        case CSConstants.E_AUDIO:
                            //String audioFilePath = ChatConstants.CHAT_AUDIO_DIRECTORY + "/" + chatData.getFileName();
                            String audioFilePath = chatData.getDownloadFilePath();
                            chatViewHolder.receiverAudioLayout.setVisibility(View.VISIBLE);
                            chatViewHolder.receiverAudioPlayPauseImageView.setVisibility(View.GONE);
                            chatViewHolder.receiverAudioProgressBar.setVisibility(View.GONE);
                            chatViewHolder.receiverAudioDownloadImg.setVisibility(View.GONE);
                            chatViewHolder.receiverCancelAudioDownload.setVisibility(View.GONE);
                            if (((new File(audioFilePath).exists()) && chatData.getMediaUploadingOrDownloadingPercentage() == 100)) {
                                chatViewHolder.receiverAudioPlayPauseImageView.setVisibility(View.VISIBLE);
                                if (!mAudioPlayingMessageID.equals(chatData.getChatId()))
                                    chatViewHolder.receiverTimerTextView.setText(getDurationOfAudio(audioFilePath));
                            } else {
                                if (chatData.isMediaDownloadingOrUploading()) {
                                    chatViewHolder.receiverCancelAudioDownload.setVisibility(View.VISIBLE);
                                    chatViewHolder.receiverAudioProgressBar.setVisibility(View.VISIBLE);
                                    chatViewHolder.receiverAudioProgressBar.setProgress(chatData.getMediaUploadingOrDownloadingPercentage());
                                    chatViewHolder.receiverAudioDownloadImg.setVisibility(View.GONE);
                                } else {
                                    chatViewHolder.receiverAudioProgressBar.setVisibility(View.GONE);
                                    chatViewHolder.receiverAudioDownloadImg.setVisibility(View.VISIBLE);
                                    chatViewHolder.receiverCancelAudioDownload.setVisibility(View.GONE);
                                }
                            }
                            break;
                        default:
                            chatViewHolder.receiverMessageTextView.setVisibility(View.VISIBLE);
                            chatViewHolder.receiverMessageTextView.setText(chatData.getMessage().trim());
                            break;
                    }
                }
                chatViewHolder.senderAudioUploadImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCSChat.resumeTransfer(chatData.getChatId());
                        chatData.setMediaDownloadingOrUploading(true);
                        chatData.setChancelClicked(false);
                    }
                });
                chatViewHolder.senderAudioCancelDownloadImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCSChat.pauseTransfer(chatData.getChatId());
                        chatViewHolder.senderAudioCancelDownloadImg.setVisibility(View.GONE);
                        chatViewHolder.senderAudioProgressBar.setVisibility(View.GONE);
                        chatViewHolder.senderAudioUploadImg.setVisibility(View.VISIBLE);
                        chatData.setMediaDownloadingOrUploading(false);
                        chatData.setChancelClicked(true);
                    }
                });
                chatViewHolder.senderDocUploadImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCSChat.resumeTransfer(chatData.getChatId());
                        chatData.setMediaDownloadingOrUploading(true);
                        chatData.setChancelClicked(false);
                    }
                });
                chatViewHolder.senderDocCancelDonloadImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCSChat.pauseTransfer(chatData.getChatId());
                        chatViewHolder.senderDocCancelDonloadImg.setVisibility(View.GONE);
                        chatViewHolder.senderDocumentProgress.setVisibility(View.GONE);
                        chatViewHolder.senderDocUploadImg.setVisibility(View.VISIBLE);
                        chatData.setMediaDownloadingOrUploading(false);
                        chatData.setChancelClicked(true);
                    }
                });
                chatViewHolder.senderCancelImgUpload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCSChat.pauseTransfer(chatData.getChatId());
                        chatViewHolder.senderCancelImgUpload.setVisibility(View.GONE);
                        chatViewHolder.senderImageProgressbarLayout.setVisibility(View.GONE);
                        chatViewHolder.senderUploadImg.setVisibility(View.VISIBLE);
                        chatData.setMediaDownloadingOrUploading(false);
                        chatData.setChancelClicked(true);
                    }
                });
                chatViewHolder.senderUploadImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCSChat.resumeTransfer(chatData.getChatId());
                        chatData.setMediaDownloadingOrUploading(true);
                        chatData.setChancelClicked(false);
                    }
                });
                chatViewHolder.receiverCancelDocDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCSChat.cancelTransfer(chatData.getChatId());
                        chatViewHolder.receiverCancelDocDownload.setVisibility(View.GONE);
                        chatViewHolder.receiverDocumentProgress.setVisibility(View.GONE);
                        chatViewHolder.receiverDocDownloadImg.setVisibility(View.VISIBLE);
                        chatData.setMediaDownloadingOrUploading(false);
                    }
                });
                chatViewHolder.receiverCancelAudioDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCSChat.cancelTransfer(chatData.getChatId());
                        chatViewHolder.receiverCancelAudioDownload.setVisibility(View.GONE);
                        chatViewHolder.receiverAudioProgressBar.setVisibility(View.GONE);
                        chatViewHolder.receiverAudioDownloadImg.setVisibility(View.VISIBLE);
                        chatData.setMediaDownloadingOrUploading(false);
                    }
                });
                chatViewHolder.receiverCancelImgDownLoad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCSChat.cancelTransfer(chatData.getChatId());
                        chatViewHolder.receiverCancelImgDownLoad.setVisibility(View.GONE);
                        chatViewHolder.receiverImageProgressbarLayout.setVisibility(View.GONE);
                        chatViewHolder.receiverImageDownloadImg.setVisibility(View.VISIBLE);
                        chatData.setMediaDownloadingOrUploading(false);
                    }
                });
                chatViewHolder.receiverImageDownloadImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (Utils.getNetwork(getApplicationContext())) {


                            chatViewHolder.receiverImageDownloadImg.setVisibility(View.GONE);


                            switch (chatData.getMessageType()) {
                                case CSConstants.E_IMAGE:

                                    String filePath = chatData.getDownloadFilePath();

                                    //mCSChat.downloadFile(chatData.getChatId(), filePath);
                                    mCSChat.downloadFile(chatData.getChatId());
                                    break;
                                case CSConstants.E_VIDEO:
                                    //String filePath1 = ChatConstants.CHAT_VIDEOS_DIRECTORY + "/" + chatData.getFileName();
                                    //mCSChat.downloadFile(chatData.getChatId(), filePath1);
                                    mCSChat.downloadFile(chatData.getChatId());
                                    break;
                            }
                        }else {
                            Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.signup_no_internet_avail),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                chatViewHolder.receiverAudioDownloadImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        chatViewHolder.receiverAudioDownloadImg.setVisibility(View.INVISIBLE);
                        //String filePath1 = ChatConstants.CHAT_AUDIO_DIRECTORY + "/" + chatData.getFileName();
                        //mCSChat.downloadFile(chatData.getChatId(), filePath1);
                        mCSChat.downloadFile(chatData.getChatId());
                    }
                });
                chatViewHolder.receiverDocDownloadImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        chatViewHolder.receiverDocDownloadImg.setVisibility(View.GONE);
                        //String filePath1 = ChatConstants.CHAT_DOCUMENTS_DIRECTORY + "/" + chatData.getFileName();
                        //mCSChat.downloadFile(chatData.getChatId(), filePath1);
                        mCSChat.downloadFile(chatData.getChatId());
                    }
                });

                chatViewHolder.senderImageDownloadImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        chatViewHolder.senderImageDownloadImg.setVisibility(View.GONE);
                        switch (chatData.getMessageType()) {
                            case CSConstants.E_IMAGE:
                            /*String filePath = "";
                            if (chatData.getFileName().equalsIgnoreCase("")) {
                                filePath = ChatConstants.CHAT_IMAGES_DIRECTORY + "/" + chatData.getDownloadFilePath();
                            } else {
                                filePath = ChatConstants.CHAT_IMAGES_DIRECTORY + "/" + chatData.getFileName();
                            }*/
                                //mCSChat.downloadFile(chatData.getChatId(), filePath);
                                mCSChat.downloadFile(chatData.getChatId());
                                break;
                            case CSConstants.E_VIDEO:
                                //String filePath1 = ChatConstants.CHAT_VIDEOS_DIRECTORY + "/" + chatData.getFileName();
                                //mCSChat.downloadFile(chatData.getChatId(), filePath1);
                                mCSChat.downloadFile(chatData.getChatId());
                                break;
                        }
                    }
                });
                chatViewHolder.senderAudioDownloadImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        chatViewHolder.senderAudioDownloadImg.setVisibility(View.INVISIBLE);
                        //String filePath1 = ChatConstants.CHAT_AUDIO_DIRECTORY + "/" + chatData.getFileName();
                        //mCSChat.downloadFile(chatData.getChatId(), filePath1);
                        mCSChat.downloadFile(chatData.getChatId());
                    }
                });
                chatViewHolder.senderDocDownloadImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        chatViewHolder.senderDocDownloadImg.setVisibility(View.GONE);
                        //String filePath1 = ChatConstants.CHAT_DOCUMENTS_DIRECTORY + "/" + chatData.getFileName();
                        //mCSChat.downloadFile(chatData.getChatId(), filePath1);
                        mCSChat.downloadFile(chatData.getChatId());
                    }
                });

                chatViewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chatMessageSelection(chatData);
                    }
                });

                chatViewHolder.parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        LOG.info("isChatSelectionEnabled: " + mIsChatSelectionEnabled);
                        if (!mIsChatSelectionEnabled) {
                            mIsChatSelectionEnabled = true;
                            chatData.setChatSelected(true);
                            mChatInterface.updateChatSelection(chatData.getChatId());
                            notifyDataSetChanged();
                        }
                        return false;
                    }
                });
                chatViewHolder.senderImageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (!mIsChatSelectionEnabled) {
                            mIsChatSelectionEnabled = true;
                            chatData.setChatSelected(true);
                            mChatInterface.updateChatSelection(chatData.getChatId());
                            notifyDataSetChanged();
                        } else {
                            chatMessageSelection(chatData);
                        }
                        return false;
                    }
                });
                chatViewHolder.senderImageLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(Utils.getNetwork(getApplicationContext())) {
                            if (mIsChatSelectionEnabled) {
                                chatMessageSelection(chatData);
                            } else {
                                getPathAndOpenPlayer(chatData);
                            }
                        }else {
                            Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.signup_no_internet_avail),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                chatViewHolder.receiverImageLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (!mIsChatSelectionEnabled) {
                            mIsChatSelectionEnabled = true;
                            chatData.setChatSelected(true);
                            mChatInterface.updateChatSelection(chatData.getChatId());
                            notifyDataSetChanged();
                        } else {
                            chatMessageSelection(chatData);
                        }
                        return false;
                    }
                });
                chatViewHolder.receiverImageLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mIsChatSelectionEnabled) {
                            chatMessageSelection(chatData);
                        } else {
                            getPathAndOpenPlayer(chatData);
                        }
                    }
                });
                chatViewHolder.senderMessageTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mIsChatSelectionEnabled) {
                            chatMessageSelection(chatData);
                        } else {
                            getPathAndOpenPlayer(chatData);
                        }
                    }
                });
                chatViewHolder.receiverContactLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mIsChatSelectionEnabled) {
                            chatMessageSelection(chatData);
                        } else {
                            Intent conatctViewIntent = new Intent(mContext, ContactViewActivity.class);
                            conatctViewIntent.putExtra("chatId", chatData.getChatId());
                            mContext.startActivity(conatctViewIntent);
                        }
                    }
                });
                chatViewHolder.senderConatctLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (!mIsChatSelectionEnabled) {
                            mIsChatSelectionEnabled = true;
                            chatData.setChatSelected(true);
                            mChatInterface.updateChatSelection(chatData.getChatId());
                            notifyDataSetChanged();
                        } else {
                            chatMessageSelection(chatData);
                        }
                        return false;
                    }
                });
                chatViewHolder.senderConatctLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mIsChatSelectionEnabled) {
                            chatMessageSelection(chatData);
                        } else {
                            Intent conatctViewIntent = new Intent(mContext, ContactViewActivity.class);
                            conatctViewIntent.putExtra("chatId", chatData.getChatId());
                            mContext.startActivity(conatctViewIntent);
                        }
                    }
                });
                chatViewHolder.receiverMessageTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mIsChatSelectionEnabled) {
                            chatMessageSelection(chatData);
                        } else {
                            getPathAndOpenPlayer(chatData);
                        }
                    }
                });
                chatViewHolder.senderLocationLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (!mIsChatSelectionEnabled) {
                            mIsChatSelectionEnabled = true;
                            chatData.setChatSelected(true);
                            mChatInterface.updateChatSelection(chatData.getChatId());
                            notifyDataSetChanged();
                        } else {
                            chatMessageSelection(chatData);
                        }
                        return false;
                    }
                });
                chatViewHolder.senderLocationImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mIsChatSelectionEnabled) {
                            chatMessageSelection(chatData);
                        } else {
                            CSChatLocation location = mCSChat.getLocationFromChatID(chatData.getChatId());
                            Double lat = location.getLat();
                            Double lng = location.getLng();
                            String urlAddress = "http://maps.google.com/maps?q=" + lat + "," + lng + "(" + location.getAddress() + ")&iwloc=A&hl=es";
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }
                    }
                });
                chatViewHolder.receiverLocationLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (!mIsChatSelectionEnabled) {
                            mIsChatSelectionEnabled = true;
                            chatData.setChatSelected(true);
                            mChatInterface.updateChatSelection(chatData.getChatId());
                            notifyDataSetChanged();
                        } else {
                            chatMessageSelection(chatData);
                        }
                        return false;
                    }
                });
                chatViewHolder.receiverLocationImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mIsChatSelectionEnabled) {
                            chatMessageSelection(chatData);
                        } else {
                            CSChatLocation location = mCSChat.getLocationFromChatID(chatData.getChatId());
                            Double lat = location.getLat();
                            Double lng = location.getLng();
                            String urlAddress = "http://maps.google.com/maps?q=" + lat + "," + lng + "(" + location.getAddress() + ")&iwloc=A&hl=es";
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        }
                    }
                });


                chatViewHolder.receiverDocumentLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mIsChatSelectionEnabled) {
                            chatMessageSelection(chatData);
                        } else {
                            getPathAndOpenPlayer(chatData);
                        }
                    }
                });
                chatViewHolder.senderDocumentLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mIsChatSelectionEnabled) {
                            chatMessageSelection(chatData);
                        } else {
                            getPathAndOpenPlayer(chatData);
                        }
                    }
                });
//=============================== Audio player related code ======================//
                chatViewHolder.receiverAudioPlayPauseImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!mIsChatSelectionEnabled) {
                            chatViewHolder.receiverAudioSeekBar.setClickable(true);
                            chatViewHolder.receiverAudioSeekBar.setFocusable(true);
                            chatViewHolder.receiverAudioSeekBar.setEnabled(true);

                            for (int i = 0; i < allMessages.size(); i++) {
                                allMessages.get(position).setAudioProgress(0);
                            }

                            allMessages.get(position).setAudioProgress(PLAYING_PROGRESS);

                            if (mAudioPlayingMessageID.equals(chatData.getChatId())) {
                                pausePlaying();
                            } else {
                                releaseMediaPlayer();

                                mSeekBar = chatViewHolder.receiverAudioSeekBar;
                                mAudioCountTextView = chatViewHolder.receiverTimerTextView;
                                mPlayPauseImageView = chatViewHolder.receiverAudioPlayPauseImageView;
                                mAudioPlayingMessageID = chatData.getChatId();
                                mCurrentAudioMessageId = chatData.getChatId();

                                //String filePath = ChatConstants.CHAT_AUDIO_DIRECTORY + "/" + chatData.getFileName();
                                String filePath = chatData.getDownloadFilePath();
                                initialisePlayer(filePath);
                            }
                            notifyDataSetChanged();
                        } else {
                            chatMessageSelection(chatData);
                        }
                    }
                });

                chatViewHolder.receiverAudioPlayPauseImageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        chatMessageSelection(chatData);
                        return false;
                    }
                });

                chatViewHolder.senderAudioPlayPauseImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!mIsChatSelectionEnabled) {
                            chatViewHolder.senderAudioSeekBar.setClickable(true);
                            chatViewHolder.senderAudioSeekBar.setFocusable(true);
                            chatViewHolder.senderAudioSeekBar.setEnabled(true);

                            for (int i = 0; i < allMessages.size(); i++) {
                                allMessages.get(position).setAudioProgress(0);
                            }

                            allMessages.get(position).setAudioProgress(PLAYING_PROGRESS);

                            if (mAudioPlayingMessageID.equals(chatData.getChatId())) {
                                pausePlaying();
                            } else {
                                releaseMediaPlayer();
                                mSeekBar = chatViewHolder.senderAudioSeekBar;
                                mAudioCountTextView = chatViewHolder.senderTimerTextView;
                                mPlayPauseImageView = chatViewHolder.senderAudioPlayPauseImageView;
                                mAudioPlayingMessageID = chatData.getChatId();
                                mCurrentAudioMessageId = chatData.getChatId();
                                if (chatData.getIsMultiDeviceMessage() == 0) {
                                    String filePath = chatData.getUploadFilePath();
                                    initialisePlayer(filePath);
                                } else {
                                    //String filePath = ChatConstants.CHAT_AUDIO_DIRECTORY + "/" + chatData.getFileName();
                                    String filePath = chatData.getUploadFilePath();
                                    initialisePlayer(filePath);
                                }
                            }
                            notifyDataSetChanged();
                        } else {
                            chatMessageSelection(chatData);
                        }
                    }
                });


                chatViewHolder.senderAudioPlayPauseImageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        chatMessageSelection(chatData);
                        return false;
                    }
                });

                chatViewHolder.senderAudioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        LOG.info("SeekBar changed progress: " + progress + " , position: " + position);

                        PLAYING_PROGRESS = progress;

                        if (mMediaPlayer != null && mMediaPlayer.isPlaying() && fromUser) {
                            mMediaPlayer.seekTo(progress);

                            mTimeProgressUpdate = (100 * mMediaPlayer.getCurrentPosition() / mMediaPlayer.getDuration());
                            allMessages.get(position).setAudioProgress(progress);
                        }
                    }
                });


                chatViewHolder.receiverAudioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        LOG.info("SeekBar changed progress: " + progress + " , position: " + position);

                        PLAYING_PROGRESS = progress;

                        if (mMediaPlayer != null && mMediaPlayer.isPlaying() && fromUser) {
                            mMediaPlayer.seekTo(progress);
                            mTimeProgressUpdate = (100 * mMediaPlayer.getCurrentPosition() / mMediaPlayer.getDuration());
                            allMessages.get(position).setAudioProgress(progress);

                        }
                    }
                });

                if (mCurrentAudioMessageId.equals(chatData.getChatId())) {

                    LOG.error("Progress A : ", chatData.getAudioProgress() + "");

                    chatViewHolder.receiverAudioSeekBar.setClickable(true);
                    chatViewHolder.receiverAudioSeekBar.setFocusable(true);
                    chatViewHolder.receiverAudioSeekBar.setEnabled(true);

                    chatViewHolder.senderAudioSeekBar.setClickable(true);
                    chatViewHolder.senderAudioSeekBar.setFocusable(true);
                    chatViewHolder.senderAudioSeekBar.setEnabled(true);

                    LOG.error("Time elapsed ", (int) mTimeElapsed + "");
                } else {
                    LOG.error("Progress P : ", chatData.getAudioProgress() + "");
                    chatViewHolder.receiverAudioSeekBar.setClickable(false);
                    chatViewHolder.receiverAudioSeekBar.setFocusable(false);
                    chatViewHolder.receiverAudioSeekBar.setEnabled(false);

                    chatViewHolder.senderAudioSeekBar.setClickable(false);
                    chatViewHolder.senderAudioSeekBar.setFocusable(false);
                    chatViewHolder.senderAudioSeekBar.setEnabled(false);
                }

                if (!mCurrentAudioMessageId.equals(chatData.getChatId())) {
                    chatViewHolder.senderAudioSeekBar.setProgress(0);
                    chatViewHolder.receiverAudioSeekBar.setProgress(0);
                }

                if (mSeekBar != null && mMediaPlayer != null) {
                    if (mCurrentAudioMessageId.equals(chatData.getChatId()) && chatData.isSender()) {

                        LOG.error("Audio pause ", "Pause Play " + mMediaPlayer.getCurrentPosition() + "");
                        mSeekBar.setMax(mMediaPlayer.getDuration());

                    } else if (mCurrentAudioMessageId.equals(chatData.getChatId()) && !chatData.isSender()) {
                        mSeekBar.setMax(mMediaPlayer.getDuration());
                    }
                }

                // Updating view when list view re using views.
                if (mAudioPlayingMessageID.equals(chatData.getChatId())) {
                    if (chatData.isSender()) {
                        mSeekBar = chatViewHolder.senderAudioSeekBar;

                        mSeekBar.setMax(mMediaPlayer.getDuration());
                        mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());

                        mAudioCountTextView = chatViewHolder.senderTimerTextView;
                        if (mMediaPlayer != null && mMediaPlayer.isPlaying())
                            chatViewHolder.senderAudioPlayPauseImageView.setBackgroundResource(R.drawable.ic_video_pause);
                        else
                            chatViewHolder.senderAudioPlayPauseImageView.setBackgroundResource(R.drawable.ic_video_play);
                    } else {
                        mSeekBar = chatViewHolder.receiverAudioSeekBar;
                        mAudioCountTextView = chatViewHolder.receiverTimerTextView;

                        mSeekBar.setMax(mMediaPlayer.getDuration());
                        mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());

                        if (mMediaPlayer != null && mMediaPlayer.isPlaying())
                            chatViewHolder.receiverAudioPlayPauseImageView.setBackgroundResource(R.drawable.ic_video_pause);
                        else
                            chatViewHolder.receiverAudioPlayPauseImageView.setBackgroundResource(R.drawable.ic_video_play);
                    }
                } else {
                    chatViewHolder.senderAudioPlayPauseImageView.setBackgroundResource(R.drawable.ic_video_play);
                    chatViewHolder.receiverAudioPlayPauseImageView.setBackgroundResource(R.drawable.ic_video_play);
                }
            }


        }

        class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
            private int mPosition;
            private  ChatViewHolder mHolder;
            private String mFilePath;
            private final WeakReference<ImageView> imageViewReference;
            boolean mIsVideo;

            public ImageDownloaderTask(int position,  ChatViewHolder holder, ImageView imageView, String filePath, boolean isVideo) {
                mPosition = position;
                mHolder = holder;
                mFilePath = filePath;
                imageViewReference = new WeakReference<>(imageView);
                mIsVideo = isVideo;
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                Bitmap photo = null;
                try {
                    LOG.info("ImageDownloaderTask, File path: " + mFilePath);
                    if (mIsVideo) {
                        photo = ThumbnailUtils.createVideoThumbnail(mFilePath, MediaStore.Images.Thumbnails.MICRO_KIND);
                    } else {
                        photo = CSDataProvider.getImageBitmap(mFilePath);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //photo = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.imageplaceholder);
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
                            if (mHolder.position == mPosition) {
                                imageView.setImageBitmap(bitmap);
                            }
                        }
                    }
                }
            }
        }

        /**
         * This method gets file path and redirects to appropriate player.
         *
         * @param chatData chatData
         */
        private void getPathAndOpenPlayer(ChatData chatData) {
            String filePath = "";

            if (chatData.isSender() && chatData.getIsMultiDeviceMessage() == 0) {
                filePath = chatData.getUploadFilePath();
            } else {
                filePath = chatData.getDownloadFilePath();
            /*if (chatData.getMessageType() == CSConstants.E_IMAGE) {
                if (chatData.getFileName().equalsIgnoreCase("")) {
                    filePath = ChatConstants.CHAT_IMAGES_DIRECTORY + "/" + chatData.getDownloadFilePath();
                } else {
                    filePath = ChatConstants.CHAT_IMAGES_DIRECTORY + "/" + chatData.getFileName();
                }
            } else if (chatData.getMessageType() == CSConstants.E_VIDEO) {
                filePath = ChatConstants.CHAT_VIDEOS_DIRECTORY + "/" + chatData.getFileName();
            }

            switch (chatData.getMessageType()) {
                case CSConstants.E_IMAGE:
                    if (chatData.getFileName().equalsIgnoreCase("")) {
                        filePath = ChatConstants.CHAT_IMAGES_DIRECTORY + "/" + chatData.getDownloadFilePath();
                    } else {
                        filePath = ChatConstants.CHAT_IMAGES_DIRECTORY + "/" + chatData.getFileName();
                    }
                    break;
                case CSConstants.E_VIDEO:
                    filePath = ChatConstants.CHAT_VIDEOS_DIRECTORY + "/" + chatData.getFileName();
                    break;
                case CSConstants.E_DOCUMENT:
                    filePath = ChatConstants.CHAT_DOCUMENTS_DIRECTORY + "/" + chatData.getFileName();
                    break;
                case CSConstants.E_AUDIO:
                    filePath = ChatConstants.CHAT_AUDIO_DIRECTORY + "/" + chatData.getFileName();
                    break;

            }*/

            }
            LOG.info("File Path: " + chatData.getUploadFilePath());
            if ((chatData.getMessageType() == CSConstants.E_IMAGE || chatData.getMessageType() == CSConstants.E_VIDEO || chatData.getMessageType() == CSConstants.E_DOCUMENT || chatData.getMessageType() == CSConstants.E_AUDIO)&& chatData.getMediaUploadingOrDownloadingPercentage() == 100) {
                ChatMethodHelper.openPlayer(mContext, filePath);
            }
        }

        //=========================== Audio player methods========================//

        /**
         * This method initialises the audio player
         *
         * @param path
         */
        private void initialisePlayer(String path) {
            Uri myUri = Uri.parse(path);

            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(mContext, myUri);
                mMediaPlayer.prepare(); // might take long! (for buffering, etc)
                mMediaPlayer.start();
                mSeekBar.setMax(mMediaPlayer.getDuration());
                mSeekBar.setClickable(false);

                mTimeElapsed = mMediaPlayer.getCurrentPosition();
                mSeekBar.setProgress((int) mTimeProgressUpdate);
                mDurationHandler.postDelayed(mUpdateSeekBarTime, 1000);


                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                         @Override
                                                         public void onCompletion(MediaPlayer mp) {
                                                             LOG.info("Audio completed listener");
                                                             if (mMediaPlayer != null) {
                                                                 mMediaPlayer.stop();
                                                                 mMediaPlayer.reset();
                                                                 mMediaPlayer.release();
                                                                 mMediaPlayer = null;

                                                                 // abandon Audio focus
                                                                 abandonAudioFocus();
                                                             }
                                                             mSeekBar.setProgress(0);
                                                             mAudioPlayingMessageID = "";
                                                             notifyDataSetChanged();
                                                         }
                                                     }
                );

                // Requesting audio focus for music player
                requestAudioFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         * This method start or pause the player
         */
        private void pausePlaying() {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                } else {
                    mMediaPlayer.start();
                }
            }
        }

        //handler to change seekBarTime
        private Runnable mUpdateSeekBarTime = new Runnable() {
            public void run() {
                try {
                    if (mMediaPlayer != null) {
                        //get current position
                        mTimeElapsed = mMediaPlayer.getCurrentPosition();
                        mTimeProgressUpdate = (100 * mMediaPlayer.getCurrentPosition() / mMediaPlayer.getDuration());
                        //set seek bar progress
                        mSeekBar.setProgress((int) mTimeElapsed);
                        //set time remaining
                        double timeRemaining = mTimeElapsed - mFinalTime;
                        mAudioCountTextView.setText(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining), TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));
                        //repeat yourself that again in 100 milli seconds
                        mDurationHandler.postDelayed(this, 1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };


        /**
         * This method stops and release the media player
         */
        public void releaseMediaPlayer() {
            try {
                if (mMediaPlayer != null) {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;

                    // abandon Audio focus
                    abandonAudioFocus();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void requestAudioFocus() {
            AudioManager am = (AudioManager) mContext
                    .getSystemService(Context.AUDIO_SERVICE);
            // Request audio focus for play back
            int result = am.requestAudioFocus(mOnAudioFocusChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN);
        }

        private void abandonAudioFocus() {
            AudioManager am = (AudioManager) mContext
                    .getSystemService(Context.AUDIO_SERVICE);
            int result = am.abandonAudioFocus(mOnAudioFocusChangeListener);
        }

        /**
         * This method gets the duration of the audio
         *
         * @param path
         * @return duration of audio
         */
        public String getDurationOfAudio(String path) {
            String durationTime = "";
            try {
                LOG.info("Audio local path: " + path);
                MediaPlayer mp = new MediaPlayer();
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.setDataSource(mContext, Uri.parse(path));
                mp.prepare();
                int duration = mp.getDuration();
                durationTime = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes((long) duration),
                        TimeUnit.MILLISECONDS.toSeconds((long) duration) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) duration)));
                mp.release();
                LOG.info("audio duration: " + durationTime);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return durationTime;
        }

        /**
         * This method is used for required date format
         *
         * @param dateStr date in milliseconds
         * @return returns date
         */
        private String getFormattedDate(long dateStr) {

            try {


                return new SimpleDateFormat("dd-MM-yyyy").format(dateStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        /**
         * This method is used to get yesterday string to provide date
         *
         * @param date this is milliseconds
         * @return which returns boolean value
         */

        public  boolean isYesterday(long date) {
            Calendar now = Calendar.getInstance();
            Calendar cdate = Calendar.getInstance();
            cdate.setTimeInMillis(date);

            now.add(Calendar.DATE, -1);

            return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                    && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                    && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
        }

        private String getFileNameWithoutExtension(String fileName) {
            try {
                if (fileName.indexOf(".") > 0)
                    fileName = fileName.substring(0, fileName.lastIndexOf("."));
                return fileName;
            } catch (Exception e) {
                e.printStackTrace();
                return fileName;
            }
        }

        /**
         * This method selects the chat messages.
         *
         * @param chatData chat message data.
         */
        void chatMessageSelection(ChatData chatData) {
            if (mIsChatSelectionEnabled) {
                if (chatData.isChatSelected()) {
                    chatData.setChatSelected(false);
                } else {
                    chatData.setChatSelected(true);
                }
                mChatInterface.updateChatSelection(chatData.getChatId());

                notifyDataSetChanged();
            }
        }
    }
    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


}
