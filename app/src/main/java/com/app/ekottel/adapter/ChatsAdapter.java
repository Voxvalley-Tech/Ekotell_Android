package com.app.ekottel.adapter;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;


import com.app.ekottel.R;
import com.app.ekottel.activity.ChatActivity;
import com.app.ekottel.activity.ContactViewActivity;
import com.app.ekottel.interfaces.ChatInterface;
import com.app.ekottel.model.ChatData;
import com.app.ekottel.utils.ChatConstants;
import com.app.ekottel.utils.ChatMethodHelper;
import com.app.ekottel.utils.CircleProgressBar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSEvents;
import com.ca.dao.CSChatLocation;
import com.ca.wrapper.CSChat;
import com.ca.wrapper.CSDataProvider;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
    public static int PLAYING_PROGRESS = 0;
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
        if (holder instanceof ChatViewHolder) {

           Log.e("CSEvents","CSEventsnetwork--->"+ CSEvents.CSCLIENT_NETWORKERROR);
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
                            new ImageDownloaderTask(chatViewHolder.position, chatViewHolder, chatViewHolder.senderMediaImageView, chatData.getThumbnail(), false).execute();
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
                            new ImageDownloaderTask(chatViewHolder.position, chatViewHolder, chatViewHolder.receiverMediaImageView, chatData.getThumbnail(), false).execute();
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
                            new ImageDownloaderTask(chatViewHolder.position, chatViewHolder, chatViewHolder.receiverMediaImageView, chatData.getThumbnail(), false).execute();
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
                    if (mIsChatSelectionEnabled) {
                        chatMessageSelection(chatData);
                    } else {
                        getPathAndOpenPlayer(chatData);
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
        private ChatViewHolder mHolder;
        private String mFilePath;
        private final WeakReference<ImageView> imageViewReference;
        boolean mIsVideo;

        public ImageDownloaderTask(int position, ChatViewHolder holder, ImageView imageView, String filePath, boolean isVideo) {
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

    public static boolean isYesterday(long date) {
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
