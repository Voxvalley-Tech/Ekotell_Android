package com.app.ekottel.adapter;

/**
 * Created by Dell on 01-02-2019.
 */

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.Spannable;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.app.ekottel.R;
import com.app.ekottel.activity.ChatAdvancedActivity;
import com.app.ekottel.activity.ContactViewActivity;
import com.app.ekottel.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.dao.CSLocation;
import com.ca.wrapper.CSChat;
import com.ca.wrapper.CSDataProvider;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class ChatAdvancedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("chatadapter");
    private static int selectedForWatchPos;

    private enum Actions {DEFAULT, AUDIOPLAY, AUDIOPAUSE, UPLOAD, UPLOADCANCEL, DOWNLOAD, DOWNLOADCANCEL, BACKGROUND}


    String TAG = ChatAdvancedAdapter.class.getSimpleName();

    CSChat CSChatObj = new CSChat();

    int defaultpreviewidth = 100;
    int defaultprevieheight = 100;
    ArrayList<Integer> selectedpositions = new ArrayList<Integer>();
    public static ArrayList<String> filedownloadinitiatedchatids = new ArrayList<String>();
    public static ArrayList<String> uploadfailedchatids = new ArrayList<String>();
    //public static ArrayList<String> audioplaychatids = new ArrayList<String>();
    public String audioplaychatid = "";
    public int PLAYING_PROGRESS = 0;
    private MediaPlayer mMediaPlayer;
    private Handler mDurationHandler = new Handler();
    String destination = "";
    Context context;
    Cursor cursor;
    String searchstring = "";
    boolean insearchmode = false;
    public int mAudioPlayingMessagePosition;
    AudioManager am;// = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

    private final int DEFAULT = 0,
            SENDER_TEXT_HTML_CONTACT = 1, SENDER_LOCATION_IMAGE_VIDEO_AUDIO = 2, RECIEVER_TEXT_HTML_CONTACT = 3,
            RECIEVER_LOCATION_IMAGE_VIDEO_AUDIO = 4, SENDER_MULTI_LOCATION_IMAGE_VIDEO_AUDIO = 5, CALL_EVENT = 6;
    /*
    RelativeLayout sendLayout;
    RelativeLayout receiveLayout;
    TextView dateView;
    TextView sendTime;
    TextView receiveTime;
    TextView sendMessage;
    TextView receiveMessage;
    TextView time;
    ImageView sentIcon;
    ImageView deliveredIcon;
    ImageView sentIcon1;
    ImageView deliveredIcon1;
    ImageView failedtext;
    RelativeLayout sent_imagelayout;
    ImageView sent_imageview;
    RelativeLayout receive_image_layout;
    ImageView recv_imageview;
    TextView recvimagetext;
    TextView sentimagetext;
    RelativeLayout statusandtime_layout;
    com.github.lzyzsd.circleprogress.DonutProgress progressBar;
    ImageView recv_downloadimage;
    com.github.lzyzsd.circleprogress.DonutProgress progressBar1;
    ImageView sent_uploadimage;
    int position;
*/


    public ChatAdvancedAdapter(Context context, Cursor c, String mydestination) {
        //this.sentimagedirectory = utils.getSentImagesDirectory();
        //this.recvimagedirectory = utils.getReceivedImagesDirectory();
        this.destination = mydestination;
        this.cursor = c;
        this.context = context;
        //setHasStableIds(true);
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void swapCursorAndNotifyDataSetChanged(Cursor newcursor, boolean insearchmode, String searchstring) {
        try {
            this.searchstring = searchstring;
            this.insearchmode = insearchmode;

            Cursor oldCursor = cursor;
            try {
                if (cursor == newcursor) {
                    return;// null;
                }
                this.cursor = newcursor;
                if (this.cursor != null) {
                    this.notifyDataSetChanged();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (oldCursor != null) {
                oldCursor.close();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        //Log.i(TAG,"cursor Count:" + cursor.getCount());
        return (cursor == null) ? 0 : cursor.getCount();
    }





    /*
    @Override
    public int getViewTypeCount() {
        return 6;
    }
    */

    @Override
    public int getItemViewType(int position) {

        cursor.moveToPosition(position);
        int chattype = cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
        int issender = cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_IS_SENDER));
        int ismultidevice = cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE));
        int returnvalue = 0;
        if (chattype == CSConstants.E_TEXTPLAIN || chattype == CSConstants.E_TEXTHTML || chattype == CSConstants.E_CONTACT) {
            if (issender == 1) {
                returnvalue = SENDER_TEXT_HTML_CONTACT;
            } else {
                returnvalue = RECIEVER_TEXT_HTML_CONTACT;
            }
        } else if (chattype == CSConstants.E_LOCATION || chattype == CSConstants.E_IMAGE || chattype == CSConstants.E_VIDEO || chattype == CSConstants.E_DOCUMENT || chattype == CSConstants.E_AUDIO) {
            if (issender == 1) {

                if (ismultidevice == 0) {
                    returnvalue = SENDER_LOCATION_IMAGE_VIDEO_AUDIO;
                } else {
                    returnvalue = SENDER_MULTI_LOCATION_IMAGE_VIDEO_AUDIO;
                }

            } else {
                returnvalue = RECIEVER_LOCATION_IMAGE_VIDEO_AUDIO;
            }
        } else if (chattype == CSConstants.E_GROUP_ACTIVITY || chattype == CSConstants.E_CALLLOG_ACTIVITY || chattype == CSConstants.E_USER_ACTIVITY) {
            returnvalue = CALL_EVENT;
        } else {
            returnvalue = DEFAULT;
        }
        //Log.i(TAG,"ItemViewType:" + returnvalue);
        return returnvalue;

    }


    public class MyVH_Default extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {


        public MyVH_Default(View view) {
            super(view);

            //Log.i(TAG,"MyVH_Default is called");
        }

        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();
            /*
            if (v.getId() == address.getId()){
                processClick(0,adapterPosition,0);
            } else if (v.getId() == pincode.getId()) {
                processClick(0,adapterPosition,0);
            }
            */

        }

        @Override
        public boolean onLongClick(View v) {
 /*
            int adapterPosition = getAdapterPosition();
            if (v.getId() == address.getId()){
                processClick(0,adapterPosition,0);
            } else if (v.getId() == pincode.getId()) {
                processClick(0,adapterPosition,0);
            }
            */
            return true;
        }
    }


    public class MyVH_SenderTextHtmlContact extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        //common variables for all viewholders
        LinearLayout unreadMessage_Layout;
        TextView unreadMessage_tv, top_dateView_tv, timestamp_tv;
        ImageView sender_message_status_img;

        //viehHolderSpecific variables
        ImageView contactProfile_img;
        TextView sender_message_tv;

        public MyVH_SenderTextHtmlContact(View view) {
            super(view);
            //common variables binding
            unreadMessage_Layout = view.findViewById(R.id.unread_messages_layout);
            unreadMessage_tv = view.findViewById(R.id.unread_messages_tv);
            top_dateView_tv = view.findViewById(R.id.top_dateView_tv);
            timestamp_tv = view.findViewById(R.id.msg_timestamp_tv);

            //viewholder specific variables
            contactProfile_img = view.findViewById(R.id.img_contact_pic);
            sender_message_tv = view.findViewById(R.id.sender_message_tv);
            sender_message_status_img = view.findViewById(R.id.sender_message_status_img);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "MyVH_SenderTextHtmlContact item clicked");
            int adapterPosition = getAdapterPosition();
            processClick(0, adapterPosition, 1, v, Actions.BACKGROUND);

        }

        @Override
        public boolean onLongClick(View v) {

            int adapterPosition = getAdapterPosition();
            processClick(1, adapterPosition, 1, v, Actions.BACKGROUND);
            /*
            if (v.getId() == address.getId()){
                processClick(0,adapterPosition,0);
            } else if (v.getId() == pincode.getId()) {
                processClick(0,adapterPosition,0);
            }
            */
            return true;
        }
    }

    public class MyVh_SenderLocImageVideoAudio extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, SeekBar.OnSeekBarChangeListener {
        private final ImageView uploaddownloadcloseimageview;
        ConstraintLayout itemRoot;
        //common variables for all viewholders
        LinearLayout unreadMessage_Layout;
        TextView unreadMessage_tv, top_dateView_tv, timestamp_tv;
        ImageView sender_message_status_img;


        //vieholder specific variables
        RelativeLayout senderDocumentLayout, senderLocation_layout, senderImage_layout;
        ImageView senderDocument_img,
                senderLocation_img,
                senderMedia_img,
                sender_video_play_icon,
                senderAudioPlayPause_img;
        TextView senderDocument_tv, senderDocument_file_type, senderLocationAddress_tv, senderAudioTimer_tv, senderMediaName_tv;

        LinearLayout senderAudio_layout;
        SeekBar audioSeekBar;
        com.github.lzyzsd.circleprogress.DonutProgress progressBar;

        int position;
        private boolean mIsChatSelectionEnabled;
        ImageView uploaddownloadimageview;
        RelativeLayout uploaddownloadlayoutview;
        TextView filesizeview;


        public MyVh_SenderLocImageVideoAudio(View view) {
            super(view);
            //common variables binding
            unreadMessage_Layout = view.findViewById(R.id.unread_messages_layout);
            unreadMessage_tv = view.findViewById(R.id.unread_messages_tv);
            top_dateView_tv = view.findViewById(R.id.top_dateView_tv);
            timestamp_tv = view.findViewById(R.id.msg_timestamp_tv);
            sender_message_status_img = view.findViewById(R.id.sender_message_status_img);
            itemRoot = view.findViewById(R.id.sender_layout);

            progressBar = (com.github.lzyzsd.circleprogress.DonutProgress) view.findViewById(R.id.progressBar);
            uploaddownloadimageview = (ImageView) view.findViewById(R.id.uploaddownloadimage);
            uploaddownloadlayoutview = (RelativeLayout) view.findViewById(R.id.uploaddownloadlayout);
            filesizeview = (TextView) view.findViewById(R.id.filesize);
            uploaddownloadcloseimageview = (ImageView) view.findViewById(R.id.uploaddownloadcloseimage);

            //vieHolderSpecific variables

            senderDocumentLayout = view.findViewById(R.id.sender_documents_layout);
            senderDocument_img = view.findViewById(R.id.sender_document_img);
            senderDocument_tv = view.findViewById(R.id.sender_document_tv);
            senderDocument_file_type = view.findViewById(R.id.sender_document_file_type);

            senderLocation_layout = view.findViewById(R.id.sender_location_layout);
            senderLocation_img = view.findViewById(R.id.sender_location_image_view);
            senderLocationAddress_tv = view.findViewById(R.id.sender_location_address_tv);


            senderImage_layout = view.findViewById(R.id.sender_image_layout);
            senderMedia_img = view.findViewById(R.id.sender_media_img);
            sender_video_play_icon = view.findViewById(R.id.sender_video_play_icon);


            senderAudio_layout = view.findViewById(R.id.sender_audio_layout);
            senderAudioPlayPause_img = view.findViewById(R.id.sender_audio_play_pause_img);
            audioSeekBar = view.findViewById(R.id.sender_audio_seekBar);
            senderAudioTimer_tv = view.findViewById(R.id.sender_audio_timer_tv);

            if (senderAudioPlayPause_img.getTag() == null)
                senderAudioPlayPause_img.setTag(0);


            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            itemRoot.setOnClickListener(this);
            itemRoot.setOnLongClickListener(this);

            senderAudioPlayPause_img.setOnClickListener(this);
            uploaddownloadimageview.setOnClickListener(this);
            uploaddownloadimageview.setOnLongClickListener(this);
            uploaddownloadcloseimageview.setOnClickListener(this);
            uploaddownloadcloseimageview.setOnLongClickListener(this);


            audioSeekBar.setOnSeekBarChangeListener(this);

            senderDocument_tv.setSelected(true);
        }

        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();

            if (v.getId() == senderAudioPlayPause_img.getId() && mAudioPlayingMessagePosition != adapterPosition) {
                mAudioPlayingMessagePosition = adapterPosition;
                processClick(0, adapterPosition, SENDER_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.AUDIOPLAY);
            } else if (v.getId() == senderAudioPlayPause_img.getId() && mAudioPlayingMessagePosition == adapterPosition) {
                mAudioPlayingMessagePosition = -1;
                processClick(0, adapterPosition, SENDER_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.AUDIOPAUSE);
            } else if (v.getId() == uploaddownloadimageview.getId()) {
                processClick(0, adapterPosition, SENDER_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.UPLOAD);
            } else if (v.getId() == uploaddownloadcloseimageview.getId()) {
                processClick(0, adapterPosition, SENDER_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.UPLOADCANCEL);
            } else if (v.getId() == itemRoot.getId()) {
                processClick(0, adapterPosition, SENDER_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.DEFAULT);
            } else {
                processClick(0, adapterPosition, SENDER_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.BACKGROUND);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int adapterPosition = getAdapterPosition();
            if (v.getId() == itemRoot.getId()) {
                processClick(1, adapterPosition, 2, v, Actions.DEFAULT);
            } else {
                processClick(1, adapterPosition, 2, v, Actions.BACKGROUND);
            }

            return true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.i(TAG, "SeekBar changed progress: " + progress + " , position: " + position);
            int adapterPosition = getAdapterPosition();
            PLAYING_PROGRESS = progress;
            if (mMediaPlayer != null && mMediaPlayer.isPlaying() && fromUser) {
                mMediaPlayer.seekTo(progress);
            }
        }
    }

    public class MyVh_RecieverTextHtmlContact extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        //common variables for all viewholders
        LinearLayout unreadMessage_Layout;
        TextView unreadMessage_tv, top_dateView_tv, timestamp_tv;


        //viehHolderSpecific variables
        ImageView contactProfile_img;
        TextView reciever_message_tv;

        public MyVh_RecieverTextHtmlContact(View view) {
            super(view);
            //common variables binding
            unreadMessage_Layout = view.findViewById(R.id.unread_messages_layout);
            unreadMessage_tv = view.findViewById(R.id.unread_messages_tv);
            top_dateView_tv = view.findViewById(R.id.top_dateView_tv);
            timestamp_tv = view.findViewById(R.id.msg_timestamp_tv);

            //viewholder specific variables
            contactProfile_img = view.findViewById(R.id.img_contact_pic);
            reciever_message_tv = view.findViewById(R.id.reciever_message_tv);
            //this tick marks are not required for the reciivers messages


            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "MyVH_SenderTextHtmlContact item clicked");
            int adapterPosition = getAdapterPosition();
            processClick(0, adapterPosition, RECIEVER_TEXT_HTML_CONTACT, v, Actions.BACKGROUND);

        }

        @Override
        public boolean onLongClick(View v) {
            int adapterPosition = getAdapterPosition();
            processClick(1, adapterPosition, 1, v, Actions.BACKGROUND);
            return true;
        }
    }

    public class MyVh_RecieverLocImageVideoAudio extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, SeekBar.OnSeekBarChangeListener {

        private final TextView filesizeview;
        ConstraintLayout itemRoot;
        //common variables for all viewholders
        LinearLayout unreadMessage_Layout;
        TextView unreadMessage_tv, top_dateView_tv, timestamp_tv;


        //vieholder specific variables
        RelativeLayout recieverDocumentLayout, recieverLocation_layout, recieverImage_layout, uploaddownloadlayoutview;
        ImageView recieverDocument_img,
                recieverLocation_img,
                recieverMedia_img,
                reciever_video_play_icon,
                recieverAudioPlayPause_img,
                uploaddownloadcloseimageview,
                uploaddownloadimageview;

        TextView recieverDocument_tv, recieverDocument_file_type, recieverLocationAddress_tv, recieverAudioTimer_tv, recieverMediaName_tv;
        LinearLayout recieverAudio_layout;
        SeekBar audioSeekBar;
        com.github.lzyzsd.circleprogress.DonutProgress progressBar;


        int position;

        public MyVh_RecieverLocImageVideoAudio(View view) {
            super(view);

            //common variables binding
            unreadMessage_Layout = view.findViewById(R.id.unread_messages_layout);
            unreadMessage_tv = view.findViewById(R.id.unread_messages_tv);
            top_dateView_tv = view.findViewById(R.id.top_dateView_tv);
            timestamp_tv = view.findViewById(R.id.msg_timestamp_tv);
            itemRoot = view.findViewById(R.id.recieve_layout);
            uploaddownloadimageview = (ImageView) view.findViewById(R.id.uploaddownloadimage);
            progressBar = (com.github.lzyzsd.circleprogress.DonutProgress) view.findViewById(R.id.progressBar);
            uploaddownloadlayoutview = (RelativeLayout) view.findViewById(R.id.uploaddownloadlayout);
            uploaddownloadcloseimageview = (ImageView) view.findViewById(R.id.uploaddownloadcloseimage);
            filesizeview = (TextView) view.findViewById(R.id.filesize);


            //vieHolderSpecific variables

            recieverDocumentLayout = view.findViewById(R.id.reciever_documents_layout);
            recieverDocument_img = view.findViewById(R.id.reciever_document_img);
            recieverDocument_tv = view.findViewById(R.id.reciever_document_tv);
            recieverDocument_file_type = view.findViewById(R.id.reciever_document_file_type);


            recieverLocation_layout = view.findViewById(R.id.reciever_location_layout);
            recieverLocation_img = view.findViewById(R.id.reciever_location_image_view);
            recieverLocationAddress_tv = view.findViewById(R.id.reciever_location_address_tv);


            recieverImage_layout = view.findViewById(R.id.reciever_image_layout);
            recieverMedia_img = view.findViewById(R.id.reciever_media_img);
            reciever_video_play_icon = view.findViewById(R.id.reciever_video_play_icon);
//            recieverMediaName_tv = view.findViewById(R.id.reciever_media_name_tv);


            recieverAudio_layout = view.findViewById(R.id.reciever_audio_layout);
            recieverAudioPlayPause_img = view.findViewById(R.id.reciever_audio_play_pause_img);
            audioSeekBar = view.findViewById(R.id.reciever_audio_seekBar);
            recieverAudioTimer_tv = view.findViewById(R.id.reciever_audio_timer_tv);

            if (recieverAudioPlayPause_img.getTag() == null)
                recieverAudioPlayPause_img.setTag(0);

            itemRoot.setOnClickListener(this);
            itemRoot.setOnLongClickListener(this);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            recieverAudioPlayPause_img.setOnClickListener(this);
            uploaddownloadimageview.setOnClickListener(this);
            uploaddownloadimageview.setOnLongClickListener(this);
            uploaddownloadcloseimageview.setOnClickListener(this);
            uploaddownloadcloseimageview.setOnLongClickListener(this);

            //  reciever_video_play_icon.setOnClickListener(this);

            audioSeekBar.setOnSeekBarChangeListener(this);
            recieverDocument_tv.setSelected(true);

        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            if (v.getId() == recieverAudioPlayPause_img.getId() && mAudioPlayingMessagePosition != adapterPosition) {
                mAudioPlayingMessagePosition = adapterPosition;
                processClick(0, adapterPosition, RECIEVER_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.AUDIOPLAY);
            } else if (v.getId() == recieverAudioPlayPause_img.getId() && mAudioPlayingMessagePosition == adapterPosition) {
                mAudioPlayingMessagePosition = -1;
                processClick(0, adapterPosition, RECIEVER_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.AUDIOPAUSE);
            } else if (v.getId() == uploaddownloadimageview.getId()) {
                processClick(0, adapterPosition, RECIEVER_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.DOWNLOAD);
            } else if (v.getId() == uploaddownloadcloseimageview.getId()) {
                processClick(0, adapterPosition, RECIEVER_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.DOWNLOADCANCEL);
            } else if (v.getId() == itemRoot.getId()) {
                processClick(0, adapterPosition, RECIEVER_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.DEFAULT);
            } else {
                processClick(0, adapterPosition, RECIEVER_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.BACKGROUND);
            }
        }

        @Override
        public boolean onLongClick(View v) {

            int adapterPosition = getAdapterPosition();
            if (v.getId() == itemRoot.getId()) {
                processClick(1, adapterPosition, 4, v, Actions.DEFAULT);
            } else {
                processClick(1, adapterPosition, 4, v, Actions.BACKGROUND);
            }

            return true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.i(TAG, "SeekBar changed progress: " + progress + " , position: " + position);
            PLAYING_PROGRESS = progress;
            if (mMediaPlayer != null && mMediaPlayer.isPlaying() && fromUser) {
                mMediaPlayer.seekTo(progress);
            }
        }
    }

    public class MyVh_SenderMultiLocImageVideoAudio extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, SeekBar.OnSeekBarChangeListener {

        //common variables for all viewholders
        LinearLayout unreadMessage_Layout;
        TextView unreadMessage_tv, top_dateView_tv, timestamp_tv;
        ImageView sender_message_status_img;
        ConstraintLayout itemRoot;

        //vieholder specific variables
        RelativeLayout senderDocumentLayout, senderLocation_layout, senderImage_layout, uploaddownloadlayoutview;
        ImageView senderDocument_img,
                senderLocation_img,
                senderMedia_img,
                sender_video_play_icon,
                uploaddownloadimageview,
                uploaddownloadcloseimageview,
                senderAudioPlayPause_img;

        TextView senderDocument_tv, senderDocument_file_type, senderLocationAddress_tv, senderAudioTimer_tv, senderMediaName_tv, filesizeview;

        LinearLayout senderAudio_layout;
        SeekBar audioSeekBar;
        com.github.lzyzsd.circleprogress.DonutProgress progressBar1;


        int position;
        private boolean mIsChatSelectionEnabled;


        public MyVh_SenderMultiLocImageVideoAudio(View view) {
            super(view);
            //common variables binding
            unreadMessage_Layout = view.findViewById(R.id.unread_messages_layout);
            unreadMessage_tv = view.findViewById(R.id.unread_messages_tv);
            top_dateView_tv = view.findViewById(R.id.top_dateView_tv);
            timestamp_tv = view.findViewById(R.id.msg_timestamp_tv);
            sender_message_status_img = view.findViewById(R.id.sender_message_status_img);
            itemRoot = view.findViewById(R.id.sender_layout);

            progressBar1 = (com.github.lzyzsd.circleprogress.DonutProgress) view.findViewById(R.id.progressBar);
            uploaddownloadimageview = (ImageView) view.findViewById(R.id.uploaddownloadimage);
            uploaddownloadlayoutview = (RelativeLayout) view.findViewById(R.id.uploaddownloadlayout);
            filesizeview = (TextView) view.findViewById(R.id.filesize);
            uploaddownloadcloseimageview = (ImageView) view.findViewById(R.id.uploaddownloadcloseimage);

            //vieHolderSpecific variables

            senderDocumentLayout = view.findViewById(R.id.sender_documents_layout);
            senderDocument_img = view.findViewById(R.id.sender_document_img);
            senderDocument_tv = view.findViewById(R.id.sender_document_tv);
            senderDocument_file_type = view.findViewById(R.id.sender_document_file_type);


            senderLocation_layout = view.findViewById(R.id.sender_location_layout);
            senderLocation_img = view.findViewById(R.id.sender_location_image_view);
            senderLocationAddress_tv = view.findViewById(R.id.sender_location_address_tv);


            senderImage_layout = view.findViewById(R.id.sender_image_layout);
            senderMedia_img = view.findViewById(R.id.sender_media_img);
            sender_video_play_icon = view.findViewById(R.id.sender_video_play_icon);

//            senderMediaName_tv = view.findViewById(R.id.sender_media_name_tv);


            senderAudio_layout = view.findViewById(R.id.sender_audio_layout);
            senderAudioPlayPause_img = view.findViewById(R.id.sender_audio_play_pause_img);
            audioSeekBar = view.findViewById(R.id.sender_audio_seekBar);
            senderAudioTimer_tv = view.findViewById(R.id.sender_audio_timer_tv);

            if (senderAudioPlayPause_img.getTag() == null)
                senderAudioPlayPause_img.setTag(0);

            itemRoot.setOnClickListener(this);
            itemRoot.setOnLongClickListener(this);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            senderAudioPlayPause_img.setOnClickListener(this);
            uploaddownloadimageview.setOnClickListener(this);
            uploaddownloadimageview.setOnLongClickListener(this);
            uploaddownloadcloseimageview.setOnClickListener(this);
            uploaddownloadcloseimageview.setOnLongClickListener(this);


            //  sender_video_play_icon.setOnClickListener(this);

            audioSeekBar.setOnSeekBarChangeListener(this);
            senderDocument_tv.setSelected(true);

        }

        @Override
        public void onClick(View v) {

            int adapterPosition = getAdapterPosition();

            if (v.getId() == senderAudioPlayPause_img.getId() && mAudioPlayingMessagePosition != adapterPosition) {
                mAudioPlayingMessagePosition = adapterPosition;
                processClick(0, adapterPosition, SENDER_MULTI_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.AUDIOPLAY);

            } else if (v.getId() == senderAudioPlayPause_img.getId() && mAudioPlayingMessagePosition == adapterPosition) {
                mAudioPlayingMessagePosition = -1;
                processClick(0, adapterPosition, SENDER_MULTI_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.AUDIOPAUSE);
            } else if (v.getId() == uploaddownloadimageview.getId()) {
                processClick(0, adapterPosition, SENDER_MULTI_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.DOWNLOAD);
            } else if (v.getId() == uploaddownloadcloseimageview.getId()) {
                processClick(0, adapterPosition, SENDER_MULTI_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.DOWNLOADCANCEL);
            } else if (v.getId() == itemRoot.getId()) {
                processClick(0, adapterPosition, SENDER_MULTI_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.DEFAULT);
            } else {
                processClick(0, adapterPosition, SENDER_MULTI_LOCATION_IMAGE_VIDEO_AUDIO, v, Actions.BACKGROUND);
            }

        }

        @Override
        public boolean onLongClick(View v) {
            int adapterPosition = getAdapterPosition();
            if (v.getId() == itemRoot.getId()) {
                processClick(1, adapterPosition, 2, v, Actions.DEFAULT);
            } else {
                processClick(1, adapterPosition, 2, v, Actions.BACKGROUND);
            }

            return true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.i(TAG, "SeekBar changed progress: " + progress + " , position: " + position);
            int adapterPosition = getAdapterPosition();
            PLAYING_PROGRESS = progress;
            if (mMediaPlayer != null && mMediaPlayer.isPlaying() && fromUser) {
                mMediaPlayer.seekTo(progress);
            }
        }

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View defaultitemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_empty, parent, false);
        //Log.i(TAG,"ItemViewType in onCreateViewHolder:" + viewType);
        try {
            switch (viewType) {
                case DEFAULT:
                    View itemView0 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_empty, parent, false);
                    return new MyVH_Default(itemView0);
                case SENDER_TEXT_HTML_CONTACT:
                    View itemView1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item1, parent, false);
                    return new MyVH_SenderTextHtmlContact(itemView1);
                case SENDER_LOCATION_IMAGE_VIDEO_AUDIO:
                    View itemView2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item2, parent, false);
                    return new MyVh_SenderLocImageVideoAudio(itemView2);
                case RECIEVER_TEXT_HTML_CONTACT:
                    View itemView3 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item3, parent, false);
                    return new MyVh_RecieverTextHtmlContact(itemView3);
                case RECIEVER_LOCATION_IMAGE_VIDEO_AUDIO:
                    View itemView4 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item4, parent, false);
                    return new MyVh_RecieverLocImageVideoAudio(itemView4);
                case SENDER_MULTI_LOCATION_IMAGE_VIDEO_AUDIO:
                    View itemView5 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item2_multidevice_chat, parent, false);
                    return new MyVh_SenderMultiLocImageVideoAudio(itemView5);

                case CALL_EVENT:
                    View itemView6 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_grp_item6, parent, false); //to displaly group/call/user/other activity
                    return new MyViewHolder6(itemView6);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new MyVH_Default(defaultitemView);

    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder viewHolder) {
        super.onViewRecycled(viewHolder);

        //Log.i(TAG,"ItemViewType in onViewRecycled:" + viewHolder.getItemViewType());


        switch (viewHolder.getItemViewType()) {
            case DEFAULT:
                MyVH_Default myVH_default = (MyVH_Default) viewHolder;
                break;
            case SENDER_TEXT_HTML_CONTACT:
                MyVH_SenderTextHtmlContact myVH_senderTextHtmlContact = (MyVH_SenderTextHtmlContact) viewHolder;

                break;
            case SENDER_LOCATION_IMAGE_VIDEO_AUDIO:
                MyVh_SenderLocImageVideoAudio myVh_senderLocImageVideoAudio = (MyVh_SenderLocImageVideoAudio) viewHolder;
                Glide.with(context).clear(myVh_senderLocImageVideoAudio.senderMedia_img);

                break;
            case RECIEVER_TEXT_HTML_CONTACT:
                MyVh_RecieverTextHtmlContact myVh_recieverTextHtmlContact = (MyVh_RecieverTextHtmlContact) viewHolder;
                break;
            case RECIEVER_LOCATION_IMAGE_VIDEO_AUDIO:
                MyVh_RecieverLocImageVideoAudio myVh_recieverLocImageVideoAudio = (MyVh_RecieverLocImageVideoAudio) viewHolder;
                Glide.with(context).clear(myVh_recieverLocImageVideoAudio.recieverMedia_img);

                break;
            case SENDER_MULTI_LOCATION_IMAGE_VIDEO_AUDIO:
                MyVh_SenderMultiLocImageVideoAudio myVh_senderMultiLocImageVideoAudio = (MyVh_SenderMultiLocImageVideoAudio) viewHolder;
                Glide.with(context).clear(myVh_senderMultiLocImageVideoAudio.senderMedia_img);
                break;

            case CALL_EVENT:
                break;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewholder, final int position) {
        Log.e(ChatAdvancedActivity.class.getSimpleName(), "Onbind calld +++" + viewholder.getClass().getSimpleName() + "    " + position);
        try {
            //Log.i(TAG,"ItemViewType in onBindViewHolder:" + viewholder.getItemViewType());V
            //String prevDate = null;

            if (selectedpositions.size() > 0 && selectedpositions.contains(position)) {
                viewholder.itemView.setBackgroundColor(context.getResources().getColor(R.color.chat_item_selected_color));
            } else {
                viewholder.itemView.setBackgroundColor(context.getResources().getColor(R.color.chat_item_normal_color));
            }

            viewholder.itemView.setTag(viewholder);
            cursor.moveToPosition(position);
            MyVH_Default viewHolder0;
            MyVH_SenderTextHtmlContact viewHolder1;
            MyVh_SenderLocImageVideoAudio viewHolder2;
            MyVh_RecieverTextHtmlContact viewHolder3;
            MyVh_RecieverLocImageVideoAudio viewHolder4;
            MyVh_SenderMultiLocImageVideoAudio viewHolder5;

            String prevDate = null;

            Long dateStr = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TIME));
            String currentDate = getFormattedDate(dateStr);
            String shortTimeStr = getFormattedTime1(dateStr);
            if (cursor.getPosition() > 0 && cursor.moveToPrevious()) {
                prevDate = getFormattedDate(cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TIME)));
                cursor.moveToNext();
            }
            int messageType = cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
            //view Holder specific binding started

            switch (viewholder.getItemViewType()) {
                case DEFAULT:
                    viewHolder0 = (MyVH_Default) viewholder;
                    break;
                case SENDER_TEXT_HTML_CONTACT:
                    viewHolder1 = (MyVH_SenderTextHtmlContact) viewholder;
                    viewHolder1.timestamp_tv.setText(shortTimeStr);

                    //shows the date wise messages
                    bindTopDateView(viewHolder1.top_dateView_tv, dateStr, prevDate);
                    //binding  status image
                    int messageStatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS));
                    //binding the status icon
                    bindMessageStatusIcon(viewHolder1.sender_message_status_img, messageStatus, viewHolder1.timestamp_tv);


                    if (messageType == CSConstants.E_TEXTPLAIN || messageType == CSConstants.E_TEXTHTML || messageType == CSConstants.E_UNKNOWN_CHAT_TYPE) {
                        String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        viewHolder1.sender_message_tv.setText(message);
                        viewHolder1.contactProfile_img.setVisibility(View.GONE);

                        if (insearchmode) {
                            setSearchTextColor(viewHolder1.sender_message_tv, message);
                        }
                    } else if (messageType == CSConstants.E_CONTACT) {
                        viewHolder1.contactProfile_img.setVisibility(View.VISIBLE);
                        String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        JSONObject jsonObj = new JSONObject(message);
                        JSONArray array = jsonObj.getJSONArray("numbers");
                        JSONArray array1 = jsonObj.getJSONArray("labels");
                        String name = jsonObj.getString("name");

                        String finalmessage = name;
                        viewHolder1.sender_message_tv.setText(finalmessage);
                        if (insearchmode) {
                            setSearchTextColor(viewHolder1.sender_message_tv, message);
                        }
                    }

                    break;

                //case is binding the data for the sender side messages for the types of (location,image,video,audio)
                //note: for image and video used the same layout file
                case SENDER_LOCATION_IMAGE_VIDEO_AUDIO:

                    viewHolder2 = (MyVh_SenderLocImageVideoAudio) viewholder;

                    viewHolder2.senderLocation_layout.setVisibility(View.GONE);
                    viewHolder2.senderImage_layout.setVisibility(View.GONE);
                    viewHolder2.senderAudio_layout.setVisibility(View.GONE);
                    viewHolder2.senderDocumentLayout.setVisibility(View.GONE);
                    viewHolder2.uploaddownloadlayoutview.setVisibility(View.GONE);
                    //binding top date view
                    bindTopDateView(viewHolder2.top_dateView_tv, dateStr, prevDate);
                    //binding  status image
                    messageStatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS));
                    //binding the status icon
                    bindMessageStatusIcon(viewHolder2.sender_message_status_img, messageStatus, viewHolder2.timestamp_tv);
                    viewHolder2.timestamp_tv.setText(shortTimeStr);

                    if (messageType == CSConstants.E_LOCATION) {
                        viewHolder2.senderLocation_layout.setVisibility(View.VISIBLE);

                        String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        JSONObject jsonObj = new JSONObject(message);
                        String lat = jsonObj.getString("lat");
                        String lon = jsonObj.getString("lon");
                        try {
                            String address = jsonObj.getString("address");
                            if (address != null) {
                                viewHolder2.senderLocationAddress_tv.setText(address);
                                if (insearchmode) {
                                    setSearchTextColor(viewHolder2.senderLocationAddress_tv, address);
                                }
                            }
                        } catch (Exception ex) {
                        }

                        String locationurl = "https://maps.google.com/maps/api/staticmap?center=" + lat + "," + lon + "&zoom=16&size=200x200&sensor=false&markers=color:blue%" + lat + "," + lon + "&key=AIzaSyBGqiHDk9Qy-akUPC3yrzTDAtGzzadmghs";
                        Log.i(TAG, "onBindViewHolder: location URL " + locationurl);
                        loadImage(viewHolder2.senderLocation_img, locationurl);

                    } else if (messageType == CSConstants.E_IMAGE) {

                        viewHolder2.senderImage_layout.setVisibility(View.VISIBLE);
                        viewHolder2.sender_video_play_icon.setVisibility(View.GONE);

                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        //String mainkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        int thumbstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));
                        String uploadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                        int uploadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));


                        if (new File(uploadfilepath).exists() && uploadpercentage != 100) {
                            viewHolder2.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize / 100) * uploadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder2.filesizeview.setText(filesizestr + "/" + filesizestr1);
                            viewHolder2.progressBar.setProgress(uploadpercentage);

                            int autosend = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILEAUTOSEND));
                            if (autosend == 1 || uploadfailedchatids.contains(chatid)) {
                                uploadfailedchatids.remove(chatid);
                                viewHolder2.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder2.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);

                            } else {
                                viewHolder2.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder2.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }

                        }


                        if (new File(uploadfilepath).exists()) {
                            loadImage(viewHolder2.senderMedia_img, uploadfilepath);
                        } else {
                            if (thumbkey != null && !thumbkey.equals("") && thumbstatus == 1) {
                                String thumbfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMB_FILE_PATH));

                                if (new File(thumbfilepath).exists()) {
                                    loadImage(viewHolder2.senderMedia_img, thumbfilepath);
                                }
                            }
                        }
                    } else if (messageType == CSConstants.E_VIDEO) {

                        viewHolder2.senderImage_layout.setVisibility(View.VISIBLE);
                        viewHolder2.sender_video_play_icon.setVisibility(View.VISIBLE);

                        String filepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));

                        String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        int thumbstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));

                        String uploadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                        int uploadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));

                        if (new File(uploadfilepath).exists() && uploadpercentage != 100) {

                            viewHolder2.sender_video_play_icon.setVisibility(View.GONE);
                            viewHolder2.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize / 100) * uploadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder2.filesizeview.setText(filesizestr + "/" + filesizestr1);
                            viewHolder2.progressBar.setProgress(uploadpercentage);

                            int autosend = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILEAUTOSEND));
                            if (autosend == 1 || uploadfailedchatids.contains(chatid)) {
                                uploadfailedchatids.remove(chatid);
                                viewHolder2.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder2.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);

                            } else {
                                viewHolder2.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder2.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        }

                        if (new File(filepath).exists()) {
                            loadImage(viewHolder2.senderMedia_img, Uri.fromFile(new File(filepath)));
                        } else {
                            if (thumbkey != null && !thumbkey.equals("") && thumbstatus == 1) {
                                //to do new ImageDownloaderTask(holder.position, holder, holder.sent_imageview).execute("app", thumbkey, "");
                                //String thumbfilepath = CSDataProvider.getImageThumbnailFilePath(thumbkey);
                                String thumbfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMB_FILE_PATH));

                                if (new File(thumbfilepath).exists()) {
                                    loadImage(viewHolder2.senderMedia_img, Uri.fromFile(new File(thumbfilepath)));
                                }
                            }
                        }

                    } else if (messageType == CSConstants.E_DOCUMENT) {

                        //viewHolder2.sent_imageview.setImageResource(R.drawable.defaultocicon);
                        viewHolder2.senderDocumentLayout.setVisibility(View.VISIBLE);

                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String uploadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                        int uploadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));

                        if (new File(uploadfilepath).exists() && uploadpercentage != 100) {

                            viewHolder2.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize / 100) * uploadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder2.filesizeview.setText(filesizestr + "/" + filesizestr1);
                            viewHolder2.progressBar.setProgress(uploadpercentage);
                            int autosend = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILEAUTOSEND));
                            if (autosend == 1 || uploadfailedchatids.contains(chatid)) {
                                uploadfailedchatids.remove(chatid);
                                viewHolder2.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder2.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);

                            } else {
                                viewHolder2.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder2.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        }
/*
                        if(cursor.getInt(cursor.getColumnInedex(CSDbFields.KEY_CHAT_ISMULTIDEVICE_MESSAGE)) == 1) {
                            String downloadpath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                            if (!downloadpath.equals("")) {
                                viewHolder2.sent_uploadimage.setVisibility(View.INVISIBLE);
                            } else {
                                viewHolder2.sent_uploadimage.setVisibility(View.VISIBLE);
                            }
                        }
*/
                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        viewHolder2.senderDocument_tv.setText(contenttype);
                        if (insearchmode) {
                            setSearchTextColor(viewHolder2.senderDocument_tv, contenttype);
                        }
                        //String filename = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        //LOG.info("filename from sent app document:"+filename);


                    } else if (messageType == CSConstants.E_AUDIO) {

                        //viewHolder2.sent_imageview.setImageResource(R.drawable.defaultauioicon);

                        viewHolder2.senderAudio_layout.setVisibility(View.VISIBLE);
                        viewHolder2.senderAudioPlayPause_img.setVisibility(View.VISIBLE);

                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String uploadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                        int uploadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));

                        if (new File(uploadfilepath).exists() && uploadpercentage != 100) {

                            viewHolder2.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize / 100) * uploadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder2.filesizeview.setText(filesizestr + "/" + filesizestr1);
                            viewHolder2.progressBar.setProgress(uploadpercentage);
                            int autosend = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILEAUTOSEND));
                            if (autosend == 1 || uploadfailedchatids.contains(chatid)) {
                                uploadfailedchatids.remove(chatid);
                                viewHolder2.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder2.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);

                            } else {
                                viewHolder2.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder2.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }

                        }

                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                        if (new File(uploadfilepath).exists()) {
                            viewHolder2.senderAudioTimer_tv.setText(getDuration(uploadfilepath));

                            int max = getDurationInMS(uploadfilepath);
                            viewHolder2.audioSeekBar.setMax(max);

                            if (audioplaychatid.equals(chatid)) {
                                viewHolder2.senderAudioPlayPause_img.setImageResource(R.drawable.ic_video_pause);
                                //viewHolder2.audiopausebuttonview.setVisibility(View.VISIBLE);
                                if (mMediaPlayer != null) {
                                    viewHolder2.audioSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                                    viewHolder2.senderAudioTimer_tv.setText(formateMilliSeccond(mMediaPlayer.getCurrentPosition()));
                                }
                            } else {
                                viewHolder2.senderAudioPlayPause_img.setImageResource(R.drawable.ic_video_play);
                                viewHolder2.audioSeekBar.setProgress(0);
                            }

                        } else {
                            viewHolder2.senderAudioTimer_tv.setText("00:00");
                            viewHolder2.audioSeekBar.setMax(0);
                        }

                        //viewHolder2.sentimagetext.setText(contenttype);
                        //  viewHolder2.audioimagetextview.setText(contenttype);
//                        if (insearchmode) {
//                            setSearchTextColor(viewHolder2.sentimagetext, contenttype);
//                        }
                    }
                    break;

                //this case is used for binding the data for the reciver message for type(Text,html,contact)
                case RECIEVER_TEXT_HTML_CONTACT:
                    viewHolder3 = (MyVh_RecieverTextHtmlContact) viewholder;
                    viewHolder3.timestamp_tv.setText(shortTimeStr);

                    bindTopDateView(viewHolder3.top_dateView_tv, dateStr, prevDate);
                    // this code is binds the uread messages view
                    if (ChatAdvancedActivity.readCount >= 0 && position == ChatAdvancedActivity.readCount) {
                        viewHolder3.unreadMessage_Layout.setVisibility(View.VISIBLE);
                        viewHolder3.unreadMessage_tv.setText(cursor.getCount() - ChatAdvancedActivity.readCount + " Unread messages");
                    } else {
                        viewHolder3.unreadMessage_Layout.setVisibility(View.GONE);
                    }
                    //////////

                    if (messageType == CSConstants.E_TEXTPLAIN || messageType == CSConstants.E_TEXTHTML || messageType == CSConstants.E_UNKNOWN_CHAT_TYPE) {
                        String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        viewHolder3.reciever_message_tv.setText(message);
                        viewHolder3.contactProfile_img.setVisibility(View.GONE);
                        if (insearchmode) {
                            setSearchTextColor(viewHolder3.reciever_message_tv, message);
                        }
                    } else if (messageType == CSConstants.E_CONTACT) {
                        viewHolder3.contactProfile_img.setVisibility(View.VISIBLE);
                        String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        JSONObject jsonObj = new JSONObject(message);
                        JSONArray array = jsonObj.getJSONArray("numbers");
                        JSONArray array1 = jsonObj.getJSONArray("labels");
                        String name = jsonObj.getString("name");


                        String finalmessage = name;
                        viewHolder3.reciever_message_tv.setText(finalmessage);
                        if (insearchmode) {
                            setSearchTextColor(viewHolder3.reciever_message_tv, message);
                        }
                    }

                    if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_RECEIVED || cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED_ACK || cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED) {
                        CSChatObj.sendReadReceipt(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID)));
                        //to do  this.changeCursor(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination));
                        //notifyDataSetChanged(); //doesnt work like this. use swapCursorAndNotifyDataSetChanged
                        return;
                    }

                    break;
                //this case binding the data for the reciever of types(location,image,viedo,audio)
                case RECIEVER_LOCATION_IMAGE_VIDEO_AUDIO:
                    viewHolder4 = (MyVh_RecieverLocImageVideoAudio) viewholder;

                    viewHolder4.timestamp_tv.setText(shortTimeStr);

                    viewHolder4.recieverLocation_layout.setVisibility(View.GONE);
                    viewHolder4.recieverImage_layout.setVisibility(View.GONE);
                    viewHolder4.recieverAudio_layout.setVisibility(View.GONE);
                    viewHolder4.recieverDocumentLayout.setVisibility(View.GONE);
                    viewHolder4.uploaddownloadlayoutview.setVisibility(View.GONE);
                    //binding top date view
                    bindTopDateView(viewHolder4.top_dateView_tv, dateStr, prevDate);

                    // this code is binds the uread messages view
                    if (ChatAdvancedActivity.readCount >= 0 && position == ChatAdvancedActivity.readCount) {
                        viewHolder4.unreadMessage_Layout.setVisibility(View.VISIBLE);
                        viewHolder4.unreadMessage_tv.setText(cursor.getCount() - ChatAdvancedActivity.readCount + " Unread messages");
                    } else {
                        viewHolder4.unreadMessage_Layout.setVisibility(View.GONE);
                    }
                    //////////

                    if (messageType == CSConstants.E_LOCATION) {

                        viewHolder4.recieverLocation_layout.setVisibility(View.VISIBLE);

                        String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        JSONObject jsonObj = new JSONObject(message);
                        String lat = jsonObj.getString("lat");
                        String lon = jsonObj.getString("lon");
                        try {
                            String address = jsonObj.getString("address");
                            if (address != null) {
                                viewHolder4.recieverLocationAddress_tv.setText(address);
                                if (insearchmode) {
                                    setSearchTextColor(viewHolder4.recieverLocationAddress_tv, address);
                                }
                            }
                        } catch (Exception ex) {
                        }

                        String locationurl = "https://maps.google.com/maps/api/staticmap?center=" + lat + "," + lon + "&zoom=16&size=200x200&sensor=false&markers=color:blue%" + lat + "," + lon + "&key=AIzaSyBGqiHDk9Qy-akUPC3yrzTDAtGzzadmghs";
                        Log.i(TAG, "onBindViewHolder: location URL " + locationurl);
                        loadImage(viewHolder4.recieverLocation_img, locationurl);

                    } else if (messageType == CSConstants.E_IMAGE) {


                        viewHolder4.recieverImage_layout.setVisibility(View.VISIBLE);
                        viewHolder4.reciever_video_play_icon.setVisibility(View.GONE);

                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        //String mainkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        int thumstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));
                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));


                        if (!new File(downloadfilepath).exists() || downloadpercentage != 100) {
                            viewHolder4.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize / 100) * downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder4.filesizeview.setText(filesizestr + "/" + filesizestr1);
                            viewHolder4.progressBar.setProgress(downloadpercentage);


                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                viewHolder4.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder4.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder4.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder4.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }


                        } else {
                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);
                            }

                        }

                        if (new File(downloadfilepath).exists() && downloadpercentage == 100) {

                            loadImage(viewHolder4.recieverMedia_img, Uri.fromFile(new File(downloadfilepath)));
                        } else if (thumstatus == 1) {
                            //LOG.info("TEST THUMBAINAL2");
                            if (thumbkey != null && !thumbkey.equals("")) {
                                //LOG.info("TEST THUMBAINAL3");
                                viewHolder4.recieverMedia_img.setVisibility(View.VISIBLE);
                                //to do new ImageDownloaderTask(viewHolder4.position,holder,viewHolder4.recv_imageview).execute("app",thumbkey,"");
                                //String thumbfilepath = CSDataProvider.getImageThumbnailFilePath(thumbkey);
                                String thumbfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMB_FILE_PATH));

                                if (new File(thumbfilepath).exists()) {
                                    loadImage(viewHolder4.recieverMedia_img, Uri.fromFile(new File(thumbfilepath)));
                                }

                            }

                        }
                    } else if (messageType == CSConstants.E_VIDEO) {

                        viewHolder4.recieverImage_layout.setVisibility(View.VISIBLE);
                        viewHolder4.reciever_video_play_icon.setVisibility(View.VISIBLE);

                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        int thumstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));

                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        if (!new File(downloadfilepath).exists() || downloadpercentage != 100) {
                            viewHolder4.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize / 100) * downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder4.filesizeview.setText(filesizestr + "/" + filesizestr1);
                            viewHolder4.progressBar.setProgress(downloadpercentage);
                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                viewHolder4.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder4.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder4.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder4.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }

                        } else {
                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);
                            }


                        }

                        if (new File(downloadfilepath).exists() && downloadpercentage == 100) {
                            loadImage(viewHolder4.recieverMedia_img, Uri.fromFile(new File(downloadfilepath)));
                        } else if (thumstatus == 1) {
                            if (thumbkey != null && !thumbkey.equals("")) {
                                //to do new ImageDownloaderTask(viewHolder4.position,holder,viewHolder4.recv_imageview).execute("app",thumbkey,"");
                                //String thumbfilepath = CSDataProvider.getImageThumbnailFilePath(thumbkey);
                                String thumbfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMB_FILE_PATH));
                                if (new File(thumbfilepath).exists()) {
                                    loadImage(viewHolder4.recieverMedia_img, Uri.fromFile(new File(thumbfilepath)));
                                }
                            } else {
                                //viewHolder4.recv_imageview.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            //viewHolder4.recv_imageview.setVisibility(View.INVISIBLE);
                        }

                    } else if (messageType == CSConstants.E_DOCUMENT) {
                        viewHolder4.recieverDocumentLayout.setVisibility(View.VISIBLE);

                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));

                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        if (!new File(downloadfilepath).exists() || downloadpercentage != 100) {

                            viewHolder4.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize / 100) * downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder4.filesizeview.setText(filesizestr + "/" + filesizestr1);
                            viewHolder4.progressBar.setProgress(downloadpercentage);
                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                viewHolder4.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder4.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder4.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder4.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        } else {

                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);

                            }
                        }


                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                       /* if(contenttype.length()<21) {  //temp change. remove later
                            contenttype = contenttype+"\n";
                        }*/
                        viewHolder4.recieverDocument_tv.setText(contenttype);

                        if (insearchmode) {
                            setSearchTextColor(viewHolder4.recieverDocument_tv, contenttype);
                        }

                    } else if (messageType == CSConstants.E_AUDIO) {

                        viewHolder4.recieverAudioPlayPause_img.setVisibility(View.VISIBLE);

                        viewHolder4.recieverAudio_layout.setVisibility(View.VISIBLE);
                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));

                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));

                        if (!new File(downloadfilepath).exists() || downloadpercentage != 100) {
                            viewHolder4.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize / 100) * downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder4.filesizeview.setText(filesizestr + "/" + filesizestr1);
                            viewHolder4.progressBar.setProgress(downloadpercentage);
                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                viewHolder4.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder4.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder4.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder4.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        } else {

                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);
                            }
                        }

                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));

                        if (new File(downloadfilepath).exists() && downloadpercentage == 100) {
                            viewHolder4.recieverAudioTimer_tv.setText(getDuration(downloadfilepath));
                            int max = getDurationInMS(downloadfilepath);
                            viewHolder4.audioSeekBar.setMax(max);
                            if (audioplaychatid.equals(chatid)) {
                                viewHolder4.recieverAudioPlayPause_img.setImageResource(R.drawable.ic_video_pause);
                                //  viewHolder4.audiopausebuttonview.setVisibility(View.VISIBLE);
                                if (mMediaPlayer != null) {
                                    viewHolder4.audioSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                                    //viewHolder4.recieverAudioTimer_tv.setText(formateMilliSeccond(mMediaPlayer.getCurrentPosition()) + "/" + getDuration(downloadfilepath));
                                    viewHolder4.recieverAudioTimer_tv.setText(formateMilliSeccond(mMediaPlayer.getCurrentPosition()));
                                }
                            } else {
                                viewHolder4.recieverAudioPlayPause_img.setImageResource(R.drawable.ic_video_play);
                                viewHolder4.audioSeekBar.setProgress(0);
                            }
                        } else {
                            viewHolder4.recieverAudioTimer_tv.setText("00:00");
                            viewHolder4.audioSeekBar.setMax(0);
                        }

                        if (insearchmode) {
                            // setSearchTextColor(viewHolder4.au, contenttype);
                        }

                    }

                    /*if(viewHolder4.recv_downloadimage!=null) {
                        viewHolder4.recv_downloadimage.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                try {


                                    //LOG.info(" viewHolder4.position:" + viewHolder4.position);
                                    //LOG.info(" cursor.position:" + cursor.getPosition());

                                    Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination);
                                    cur.moveToPosition(viewHolder4.position);
                                    String mainkey = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                                    String chatid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                                    //LOG.info(" chatid from adaptor1:" + chatid);
                                    cur.close();

                                    //viewHolder4.progressBar.setVisibility(View.VISIBLE);
                                    viewHolder4.recv_downloadimage.setVisibility(View.INVISIBLE);
                                    CSChatObj.downloadFile(chatid,  utils.getReceivedImagesDirectory() + "/" + mainkey);
                                } catch (Exception ex) {
                                    utils.logStacktrace(ex);
                                }
                            }
                        });
                    }
                    */
                    //if(cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS))== CSConstants.MESSAGE_RECEIVED   || cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS))== CSConstants.MESSAGE_DELIVERED_ACK) {
                    if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_RECEIVED || cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED_ACK || cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED) {
                        CSChatObj.sendReadReceipt(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID)));
                        //to do  this.changeCursor(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination));
                        //notifyDataSetChanged();  //doesnt work like this. use swapCursorAndNotifyDataSetChanged
                        return;
                    }
                    break;

                case SENDER_MULTI_LOCATION_IMAGE_VIDEO_AUDIO:
                    viewHolder5 = (MyVh_SenderMultiLocImageVideoAudio) viewholder;
                    viewHolder5.timestamp_tv.setText(shortTimeStr);

                    viewHolder5.uploaddownloadlayoutview.setVisibility(View.GONE);


                    viewHolder5.senderLocation_layout.setVisibility(View.GONE);
                    viewHolder5.senderImage_layout.setVisibility(View.GONE);
                    viewHolder5.senderAudio_layout.setVisibility(View.GONE);
                    viewHolder5.senderDocumentLayout.setVisibility(View.GONE);
                    //binding top date view
                    bindTopDateView(viewHolder5.top_dateView_tv, dateStr, prevDate);
                    //binding  status image
                    messageStatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS));
                    //binding the status icon
                    bindMessageStatusIcon(viewHolder5.sender_message_status_img, messageStatus, viewHolder5.timestamp_tv);

                    if (messageType == CSConstants.E_LOCATION) {

                        viewHolder5.senderLocation_layout.setVisibility(View.VISIBLE);

                        String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        JSONObject jsonObj = new JSONObject(message);
                        String lat = jsonObj.getString("lat");
                        String lon = jsonObj.getString("lon");
                        try {
                            String address = jsonObj.getString("address");
                            if (address != null) {
                                viewHolder5.senderLocationAddress_tv.setText(address);
                                if (insearchmode) {
                                    setSearchTextColor(viewHolder5.senderLocationAddress_tv, address);
                                }
                            }
                        } catch (Exception ex) {
                        }

                        String locationurl = "https://maps.google.com/maps/api/staticmap?center=" + lat + "," + lon + "&zoom=16&size=200x200&sensor=false&markers=color:blue%" + lat + "," + lon + "&key=AIzaSyBGqiHDk9Qy-akUPC3yrzTDAtGzzadmghs";
                        Log.i(TAG, "onBindViewHolder: location URL " + locationurl);
                        loadImage(viewHolder5.senderLocation_img, locationurl);

                    } else if (messageType == CSConstants.E_IMAGE) {
                        viewHolder5.senderImage_layout.setVisibility(View.VISIBLE);
                        viewHolder5.sender_video_play_icon.setVisibility(View.GONE);

                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        //String mainkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        int thumbstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));
                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));


                        if (!new File(downloadfilepath).exists() || downloadpercentage != 100) {
                            viewHolder5.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize / 100) * downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder5.filesizeview.setText(filesizestr + "/" + filesizestr1);
                            viewHolder5.progressBar1.setProgress(downloadpercentage);
                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                viewHolder5.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);
                            }
                        }

                        if (new File(downloadfilepath).exists() && downloadpercentage == 100) {
                            loadImage(viewHolder5.senderMedia_img, Uri.fromFile(new File(downloadfilepath)));
                        } else if (thumbstatus == 1) {
                            //LOG.info("TEST THUMBAINAL2");
                            if (thumbkey != null && !thumbkey.equals("")) {
                                //LOG.info("TEST THUMBAINAL3");
                                viewHolder5.senderMedia_img.setVisibility(View.VISIBLE);
                                //to do new ImageDownloaderTask(viewHolder4.position,holder,viewHolder4.recv_imageview).execute("app",thumbkey,"");
                                //String thumbfilepath = CSDataProvider.getImageThumbnailFilePath(thumbkey);
                                String thumbfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMB_FILE_PATH));

                                if (new File(thumbfilepath).exists()) {
                                    loadImage(viewHolder5.senderMedia_img, Uri.fromFile(new File(thumbfilepath)));
                                }

                            }

                        }
                    } else if (messageType == CSConstants.E_VIDEO) {

                        viewHolder5.senderImage_layout.setVisibility(View.VISIBLE);
                        viewHolder5.sender_video_play_icon.setVisibility(View.VISIBLE);

                        String filepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));

                        String thumbkey = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALKEY));
                        int thumbstatus = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMBAINALSTATUS));

                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        if (!new File(downloadfilepath).exists() || downloadpercentage != 100) {
                            viewHolder5.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize / 100) * downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder5.filesizeview.setText(filesizestr + "/" + filesizestr1);
                            viewHolder5.progressBar1.setProgress(downloadpercentage);
                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                viewHolder5.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }

                        } else {
                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);
                            }
                        }

                        if (new File(downloadfilepath).exists() && downloadpercentage == 100) {
                            loadImage(viewHolder5.senderMedia_img, Uri.fromFile(new File(downloadfilepath)));
                        } else if (thumbstatus == 1) {
                            if (thumbkey != null && !thumbkey.equals("")) {
                                String thumbfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_THUMB_FILE_PATH));
                                if (new File(thumbfilepath).exists()) {
                                    loadImage(viewHolder5.senderMedia_img, Uri.fromFile(new File(thumbfilepath)));
                                }
                            } else {
                                //viewHolder4.recv_imageview.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            //viewHolder4.recv_imageview.setVisibility(View.INVISIBLE);
                        }

                    } else if (messageType == CSConstants.E_DOCUMENT) {
                        //viewHolder2.sent_imageview.setImageResource(R.drawable.defaultocicon);
                        viewHolder5.senderDocumentLayout.setVisibility(View.VISIBLE);

                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
                        if (!new File(downloadfilepath).exists() || downloadpercentage != 100) {

                            viewHolder5.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize / 100) * downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder5.filesizeview.setText(filesizestr + "/" + filesizestr1);
                            viewHolder5.progressBar1.setProgress(downloadpercentage);
                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                viewHolder5.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        } else {

                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);

                            }
                        }


                        String contenttype = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME));
                       /* if(contenttype.length()<21) {  //temp change. remove later
                            contenttype = contenttype+"\n";
                        }*/
                        viewHolder5.senderDocument_tv.setText(contenttype);

                        if (insearchmode) {
                            setSearchTextColor(viewHolder5.senderDocument_tv, contenttype);
                        }

                    } else if (messageType == CSConstants.E_AUDIO) {

                        viewHolder5.senderAudio_layout.setVisibility(View.VISIBLE);
                        viewHolder5.senderAudioPlayPause_img.setVisibility(View.VISIBLE);

                        String chatid = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID));
                        String downloadfilepath = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
                        int downloadpercentage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));

                        if (!new File(downloadfilepath).exists() || downloadpercentage != 100) {
                            viewHolder5.uploaddownloadlayoutview.setVisibility(View.VISIBLE);
                            long filesize = cursor.getLong(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_SIZE));
                            String filesizestr = getFileSizeasString((filesize / 100) * downloadpercentage);
                            String filesizestr1 = getFileSizeasString(filesize);
                            viewHolder5.filesizeview.setText(filesizestr + "/" + filesizestr1);
                            viewHolder5.progressBar1.setProgress(downloadpercentage);
                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                viewHolder5.uploaddownloadimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder5.uploaddownloadcloseimageview.setVisibility(View.GONE);
                                viewHolder5.uploaddownloadimageview.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (filedownloadinitiatedchatids.contains(chatid)) {
                                filedownloadinitiatedchatids.remove(chatid);
                            }
                        }


                        if (new File(downloadfilepath).exists() && downloadpercentage == 100) {
                            viewHolder5.senderAudioTimer_tv.setText(getDuration(downloadfilepath));
                            int max = getDurationInMS(downloadfilepath);
                            viewHolder5.audioSeekBar.setMax(max);
                            if (audioplaychatid.equals(chatid)) {
                                viewHolder5.senderAudioPlayPause_img.setImageResource(R.drawable.ic_video_pause);
                                //  viewHolder4.audiopausebuttonview.setVisibility(View.VISIBLE);
                                if (mMediaPlayer != null) {
                                    viewHolder5.audioSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                                    //viewHolder4.recieverAudioTimer_tv.setText(formateMilliSeccond(mMediaPlayer.getCurrentPosition()) + "/" + getDuration(downloadfilepath));
                                    viewHolder5.senderAudioTimer_tv.setText(formateMilliSeccond(mMediaPlayer.getCurrentPosition()));
                                }
                            } else {
                                viewHolder5.senderAudioPlayPause_img.setImageResource(R.drawable.ic_video_play);
                                viewHolder5.audioSeekBar.setProgress(0);
                            }
                        } else {
                            viewHolder5.senderAudioTimer_tv.setText("00:00");
                            viewHolder5.audioSeekBar.setMax(0);
                        }

                    }

                    if (cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_RECEIVED || cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED_ACK || cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_STATUS)) == CSConstants.MESSAGE_DELIVERED) {
                        CSChatObj.sendReadReceipt(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_ID)));
                        //to do  this.changeCursor(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID,destination));
                        //notifyDataSetChanged();  //doesnt work like this. use swapCursorAndNotifyDataSetChanged
                        return;
                    }
                    break;

                case 6:
                    MyViewHolder6 viewHolder6 = (MyViewHolder6) viewholder;
                    if (prevDate == null || !prevDate.equals(currentDate)) {
                        viewHolder6.dateView.setVisibility(View.VISIBLE);
                        if (DateUtils.isToday(dateStr)) {
                            viewHolder6.dateView.setText("Today");
                        } else if (isYesterday(dateStr)) {
                            viewHolder6.dateView.setText("Yesterday");
                        } else {
                            viewHolder6.dateView.setText(currentDate);
                        }
                    } else {
                        viewHolder6.dateView.setVisibility(View.GONE);
                    }

                    if (cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_GROUP_ACTIVITY || cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_CALLLOG_ACTIVITY || cursor.getInt(cursor.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE)) == CSConstants.E_USER_ACTIVITY) {
                        String message = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));
                        viewHolder6.sendMessage.setText(message);
                        if (insearchmode) {
                            setSearchTextColor(viewHolder6.sendMessage, message);
                        }
                    }
                    break;
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public void processClick(int clicktype, int cursorposition, int viewtype, View view, Actions action) {
        try {
            //View view can be null
            Log.i(TAG, "clicktype:" + clicktype);
            Log.i(TAG, "cursorposition:" + cursorposition);
            Log.i(TAG, "action:" + action);
            Log.i(TAG, "viewtype:" + viewtype);
            Cursor cur = CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, this.destination); //this can be sensitive and cause issues on notify date set changed when on long click. take care(delete is fine since after delete we are exiting from onlong click slection )
            cur.moveToPosition(cursorposition);
            int chattype = cur.getInt(cur.getColumnIndex(CSDbFields.KEY_CHAT_MSG_TYPE));
            String chatid = cur.getString(cur.getColumnIndex(CSDbFields.KEY_CHAT_ID));
            String contenttype = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_CONTENT_TYPE));
            String uploadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_UPLOAD_FILE_PATH));
            String downloadfilepath = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DOWNLOAD_FILE_PATH));
            int downloadpercentage = cur.getInt(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TRANSFER_PERCENTAGE));
            String mainkey = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE));

            String finalfilepath = "";
            Uri finalfilepathuri = null;
            boolean downloadcompleted = false;

            if (viewtype == RECIEVER_LOCATION_IMAGE_VIDEO_AUDIO || viewtype == SENDER_MULTI_LOCATION_IMAGE_VIDEO_AUDIO) {
                finalfilepath = downloadfilepath;
                if (downloadpercentage >= 100) {
                    downloadcompleted = true;
                }
            } else {
                finalfilepath = uploadfilepath;
                downloadcompleted = true;
            }


            if (!finalfilepath.equals("")) {
                if (new File(finalfilepath).exists() && downloadcompleted) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        finalfilepathuri = FileProvider.getUriForFile(
                                context.getApplicationContext(),
                                context.getApplicationContext()
                                        .getPackageName() + ".provider", new File(finalfilepath));
                    } else {
                        finalfilepathuri = Uri.fromFile(new File(finalfilepath));
                    }
                }
            }


            Log.i(TAG, "chattype:" + chattype);

            if (selectedpositions.size() >= 1) {
                clicktype = 1;
            }


            if (selectedpositions.contains(cursorposition)) {
                selectedpositions.remove((Object) cursorposition);
                if (clicktype == 1 && action == Actions.BACKGROUND)
                    view.setBackgroundColor(context.getResources().getColor(R.color.chat_item_normal_color));
                else if (clicktype == 1 && action == Actions.DEFAULT) {
                    ((View) view.getParent()).setBackgroundColor(context.getResources().getColor(R.color.chat_item_normal_color));
                }
            } else {

                if (selectedpositions.size() < 20) {
                    if (clicktype == 1 && action == Actions.BACKGROUND) {
                        selectedpositions.add(cursorposition);
                        view.setBackgroundColor(context.getResources().getColor(R.color.chat_item_selected_color));
                    } else if (clicktype == 1 && action == Actions.DEFAULT) {
                        selectedpositions.add(cursorposition);
                        ((View) view.getParent()).setBackgroundColor(context.getResources().getColor(R.color.chat_item_selected_color));
                    }
                } else {
                    Toast.makeText(context, "Can send 20 messages only", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            Log.i(TAG, "selectedpositions size:" + selectedpositions.size());
            ChatAdvancedActivity.handlelongclick(selectedpositions);


            if (clicktype == 0 && (viewtype == SENDER_TEXT_HTML_CONTACT || viewtype == RECIEVER_TEXT_HTML_CONTACT)) {
                if (chattype == CSConstants.E_TEXTPLAIN) {
                } else if (chattype == CSConstants.E_CONTACT) {
                    Intent conatctViewIntent = new Intent(this.context, ContactViewActivity.class);
                    conatctViewIntent.putExtra("chatId", chatid);
                    this.context.startActivity(conatctViewIntent);
                }
            } else if (clicktype == 0 && (viewtype == SENDER_LOCATION_IMAGE_VIDEO_AUDIO || viewtype == RECIEVER_LOCATION_IMAGE_VIDEO_AUDIO || viewtype == SENDER_MULTI_LOCATION_IMAGE_VIDEO_AUDIO)) {
                if (chattype == CSConstants.E_LOCATION) {
                    if (action == Actions.DEFAULT) {
                        CSLocation location = CSChatObj.getLocationFromChatID(chatid);
                        Double lat = location.getLat();
                        Double lng = location.getLng();
                        String geoUri = "https://maps.google.com/maps?q=loc:" + lat + "," + lng + "&zoom=14&markers=color:blue%7C" + lat + "," + lng;
                        LOG.info("geoUri to show on map:" + geoUri);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        selectedForWatchPos = cursorposition;
                    }
                } else if (chattype == CSConstants.E_IMAGE) {

                    if (action == Actions.DEFAULT) {
                        openFile(finalfilepathuri, contenttype, cursorposition);
                    } else {
                        uploaddownloadhelper(action, chatid);
                    }
                } else if (chattype == CSConstants.E_VIDEO) {

                    if (action == Actions.DEFAULT) {
                        openFile(finalfilepathuri, contenttype, cursorposition);
                    } else {
                        uploaddownloadhelper(action, chatid);
                    }

                } else if (chattype == CSConstants.E_DOCUMENT) {

                    if (action == Actions.DEFAULT) {
                        openFile(finalfilepathuri, contenttype, cursorposition);
                    } else {
                        uploaddownloadhelper(action, chatid);
                    }

                } else if (chattype == CSConstants.E_AUDIO) {


                    if (action == Actions.DEFAULT) {
                        //openFile(finalfilepathuri,contenttype);
                    } else if (action == Actions.AUDIOPLAY) {
                        if (finalfilepathuri != null) {
                            releaseMediaPlayer();
                            audioplaychatid = chatid;
                            startPlaying(finalfilepathuri);
                            swapCursorAndNotifyDataSetChanged(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination), false, "");
                        } else {
                            Toast.makeText(context, "Media doesn't exist on internal storage!", Toast.LENGTH_SHORT).show();
                        }
                    } else if (action == Actions.AUDIOPAUSE) {
                        releaseMediaPlayer();
                    } else {
                        uploaddownloadhelper(action, chatid);
                    }
                }
            }

            cur.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void bindTopDateView(TextView top_dateView_tv, long dateStr, String prevDate) {

        String currentDate = getFormattedDate(dateStr);

        if (prevDate == null || !prevDate.equals(currentDate)) {
            top_dateView_tv.setVisibility(View.VISIBLE);
            if (DateUtils.isToday(dateStr)) {
                top_dateView_tv.setText("Today");
            } else if (isYesterday(dateStr)) {
                top_dateView_tv.setText("Yesterday");
            } else {
                top_dateView_tv.setText(currentDate);
            }
        } else {
            top_dateView_tv.setVisibility(View.GONE);
        }
    }

    //this method is used for showing and changing the status tick mark icon
    private void bindMessageStatusIcon(ImageView status_icon, int messageStatus, TextView messageDate) {
        switch (messageStatus) {
            case CSConstants.MESSAGE_SENT:
                status_icon.setBackgroundResource(R.drawable.mytick);
                break;
            case CSConstants.MESSAGE_DELIVERED:
                status_icon.setBackgroundResource(R.drawable.tick1);
                break;
            case CSConstants.MESSAGE_READ:
                status_icon.setBackgroundResource(R.drawable.tick);
                break;
            case CSConstants.MESSAGE_NOT_SENT:
                status_icon.setBackgroundResource(R.drawable.message);
                break;
            case CSConstants.MESSAGE_SENT_FAILED:
                messageDate.setText("failed");
                break;
            default:
                //  chatViewHolder.senderDocumentProgress.setVisibility(View.VISIBLE);
                break;
        }
    }


    ////start of helper methods
    private String getFormattedDate1(String dateStr) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        Date date = null;
        try {
            date = sdf1.parse(dateStr);
        } catch (java.text.ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String shortDate = sdf.format(date);
        return shortDate;
    }

    private String getFormattedTime(long dateStr) {
        try {
            return new SimpleDateFormat("hh:mm:ss a").format(dateStr);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    private String getFormattedDate(long dateStr) {
        try {
            return new SimpleDateFormat("dd-MM-yyyy").format(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getFormattedTime1(long dateStr) {

        try {
            return new SimpleDateFormat("hh:mm a").format(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isYesterday(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);
        now.add(Calendar.DATE, -1);
        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }


/*
    private Bitmap decodeFile(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE=70;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (Exception e) {}
        return null;
    }
*/


    private void setSearchTextColor(TextView view, String actualstring) {
        try {
            //if(searchstring != null && !searchstring.equals("") && actualstring != null && !actualstring.equals("")) {
            //int startPos = actualstring.indexOf(searchstring);
            if (actualstring.toLowerCase().contains(searchstring.toLowerCase())) {
                int startPos = actualstring.toLowerCase(Locale.US).indexOf(searchstring.toLowerCase(Locale.US));
                int endPos = startPos + searchstring.length();

                if (startPos != -1) {
                    Spannable spanText = Spannable.Factory.getInstance().newSpannable(actualstring);
                    spanText.setSpan(new ForegroundColorSpan(Color.RED), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    view.setText(spanText, TextView.BufferType.SPANNABLE);
                    //view.setBackgroundColor(Color.parseColor("#BDBDBD"));//getting recycled. see later
                }
            }
            //}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private String getDuration(String filepath) {
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(filepath);
            String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return formateMilliSeccond(Long.parseLong(durationStr));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "00:00";
    }

    private int getDurationInMS(String filepath) {
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(filepath);
            String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return Integer.parseInt(durationStr);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    public static String formateMilliSeccond(long milliseconds) {
        String finalTimerString = "";
        try {
            String secondsString = "";
            String minutesString = "";
            // Convert total duration into time
            int hours = (int) (milliseconds / (1000 * 60 * 60));
            int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
            int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);


            // Add hours if there
            if (hours > 0) {
                finalTimerString = hours + ":";
            }

            // Prepending 0 to seconds if it is one digit
            if (seconds < 10) {
                secondsString = "0" + seconds;
            } else {
                secondsString = "" + seconds;
            }
            if (minutes < 10) {
                minutesString = "0" + minutes;
            } else {
                minutesString = "" + minutes;
            }

            finalTimerString = finalTimerString + minutesString + ":" + secondsString;

            //      return  String.format("%02d Min, %02d Sec",
            //                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
            //                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
            //                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));

            // return timer string
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return finalTimerString;
    }

    public void clearselecteditems() {
        try {
            selectedpositions.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openFile(Uri finalfilepathuri, String contenttype, int cursorPosition) {
        try {
            if (finalfilepathuri != null) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(finalfilepathuri, contenttype);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                selectedForWatchPos = cursorPosition;
            } else {
                Toast.makeText(context, "Media doesn't exist on internal storage!", Toast.LENGTH_SHORT).show();
            }
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
            Toast.makeText(context, "No Application found to open the file<" + contenttype + ">.", Toast.LENGTH_SHORT).show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void uploaddownloadhelper(Actions action, String chatid) {
        try {
            if (action == Actions.UPLOAD) {
                CSChatObj.resumeTransfer(chatid);
            } else if (action == Actions.UPLOADCANCEL) {
                CSChatObj.cancelTransfer(chatid);

            } else if (action == Actions.DOWNLOAD) {

                if (!Utils.getNetwork(context)) {
                    Toast.makeText(context, "No interent", Toast.LENGTH_SHORT).show();
                    return;
                }
             /*   if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(context, "Write external storage permission needed", Toast.LENGTH_LONG).show();

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        ArrayList<String> allpermissions = new ArrayList<String>();
                        allpermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        ActivityCompat.requestPermissions((Activity) context, allpermissions.toArray(new String[allpermissions.size()]), 101);
                    }
                    return;
                }*/
                filedownloadinitiatedchatids.add(chatid);
                CSChatObj.downloadFile(chatid);

            } else if (action == Actions.DOWNLOADCANCEL) {
                CSChatObj.cancelTransfer(chatid);
                if (filedownloadinitiatedchatids.contains(chatid)) {
                    filedownloadinitiatedchatids.remove(chatid);
                }
            }

            swapCursorAndNotifyDataSetChanged(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination), false, "");

            /*int position = ChatAdvancedActivity.getpositionfromchatid(chatid);
            if(position>=0) {
                notifyItemChanged(position);
            }
            */

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getFileSizeasString(long size) {
        try {
            DecimalFormat df = new DecimalFormat("0.00");

            float sizeKb = 1024.0f;
            float sizeMb = sizeKb * sizeKb;
            float sizeGb = sizeMb * sizeKb;
            float sizeTerra = sizeGb * sizeKb;


            if (size < sizeMb)
                return df.format(size / sizeKb) + " Kb";
            else if (size < sizeGb)
                return df.format(size / sizeMb) + " Mb";
            else if (size < sizeTerra)
                return df.format(size / sizeGb) + " Gb";

            return "";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }


    private void startPlaying(Uri myUri) {
        //Uri myUri = Uri.parse(path);

        try {

            am.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);


            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(context, myUri);
            mMediaPlayer.prepare(); // might take long! (for buffering, etc)
            mMediaPlayer.start();

            mDurationHandler.postDelayed(mUpdateSeekBarTime, 1000);

           /*
            mTimeElapsed = mMediaPlayer.getCurrentPosition();
            mSeekBar.setProgress((int) mTimeProgressUpdate);
            mDurationHandler.postDelayed(mUpdateSeekBarTime, 1000);
*/

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                     @Override
                                                     public void onCompletion(MediaPlayer mp) {
                                                         Log.i(TAG, "Audio completed listener");
                                                         releaseMediaPlayer();

                                                         //mSeekBar.setProgress(0);
                                                         //mAudioPlayingMessageID = "";

                                                     }
                                                 }
            );


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show();

        }

    }

    private void pauseOrresumePlaying() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            } else {
                mMediaPlayer.start();
            }
        }
    }


    public void releaseMediaPlayer() {
        try {
            swapCursorAndNotifyDataSetChanged(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination), false, "");
            audioplaychatid = "";
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;

                // abandon Audio focus
                //abandonAudioFocus();
                am.abandonAudioFocus(mOnAudioFocusChangeListener);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.i(TAG, "AUDIOFOCUS_GAIN");
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT");
                    break;
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    Log.i(TAG, "AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.i(TAG, "AUDIOFOCUS_LOSS");
                    releaseMediaPlayer();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.i(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                    releaseMediaPlayer();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.i(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    break;
                case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                    Log.i(TAG, "AUDIOFOCUS_REQUEST_FAILED");
                    break;
                default:
                    //
            }
        }
    };

    private Runnable mUpdateSeekBarTime = new Runnable() {
        public void run() {
            try {
                if (mMediaPlayer != null) {
                    //get current position

                    swapCursorAndNotifyDataSetChanged(CSDataProvider.getChatCursorByFilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, destination), false, "");
                    mDurationHandler.postDelayed(this, 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * this method loads the image into the image view using glide
     */

    protected void loadImage(ImageView imageView, Object imageSource) {
        Glide.with(context)
                .load(imageSource)
                .apply(new RequestOptions().error(R.drawable.imageplaceholder))
                .into(imageView);
    }


    public class MyViewHolder6 extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        RelativeLayout sendLayout;
        TextView dateView;
        TextView sendMessage;
        int position;


        public MyViewHolder6(View view) {
            super(view);

            dateView = (TextView) view.findViewById(R.id.dateView);
            sendLayout = (RelativeLayout) view.findViewById(R.id.send_layout);
            sendMessage = (TextView) view.findViewById(R.id.chat_send_text);

            //view.setOnClickListener(this);
            //view.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "MyViewHolder6 item clicked");
            //int adapterPosition = getAdapterPosition();
            //processClick(0,adapterPosition,1,v, Actions.DEFAULT);

        }

        @Override
        public boolean onLongClick(View v) {

            //int adapterPosition = getAdapterPosition();
            //processClick(1,adapterPosition,1,v, Actions.DEFAULT);

            return true;
        }
    }
}