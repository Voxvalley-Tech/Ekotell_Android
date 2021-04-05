package com.app.ekottel.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.utils.CallMethodHelper;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSDbFields;
import com.ca.wrapper.CSDataProvider;


public class UserProfileActivity extends AppCompatActivity {
    private TextView mProfileNameTv, mProfileContactTv, mProfileStatusTv, mAudioCallImg, mVideoCallImg, mChatImg;
    private ImageView mProfileBackImg, mUserProfileImg, mDefaultProfileImg;
    private RelativeLayout mProfileLayout;
    private String mProfileNumber = "";
    private String TAG1 = "UserProfileActivity";
    private boolean isProfileImageAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mProfileContactTv = findViewById(R.id.profile_contact_tv);
        mProfileNameTv = findViewById(R.id.profile_name_tv);
        mProfileStatusTv = findViewById(R.id.profile_status_tv);
        mAudioCallImg = findViewById(R.id.profile_audio_call_img);
        mVideoCallImg = findViewById(R.id.profile_video_call_img);
        mProfileBackImg = findViewById(R.id.profile_back_img);
        mUserProfileImg = findViewById(R.id.user_profile_pic_img);
        mDefaultProfileImg = findViewById(R.id.profile_default_img);
        mChatImg = findViewById(R.id.profile_chat_img);
        mProfileLayout = findViewById(R.id.profile_layout);
        mProfileNumber = getIntent().getStringExtra("profileNumber");
        mProfileContactTv.setText(mProfileNumber);
        Typeface text_font = Utils.getTypeface(getApplicationContext());
        mAudioCallImg.setTypeface(text_font);
        mVideoCallImg.setTypeface(text_font);
        mChatImg.setTypeface(text_font);


        mAudioCallImg.setText(getString(R.string.dialpad_call));
        mChatImg.setText(getString(R.string.contact_chat));
        mVideoCallImg.setText(getString(R.string.video_call_icon));
        getProfileName();
        getProfilePicture();

        mProfileBackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mAudioCallImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallMethodHelper.processAudioCall(UserProfileActivity.this, mProfileNumber, "PSTN");
            }
        });
        mVideoCallImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallMethodHelper.placeVideoCall(UserProfileActivity.this, mProfileNumber);
            }
        });
        mChatImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isProfileImageAvailable) {
                    Intent profileIntent = new Intent(UserProfileActivity.this, ProfileImageActivity.class);
                    profileIntent.putExtra("profileContactNumber", mProfileNumber);
                    startActivity(profileIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "Profile image not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getProfileName() {
        Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, mProfileNumber);
        String profile_presence_status = null;
        String profile_presence_name = null;
        if (cur.getCount() > 0) {
            cur.moveToNext();
            profile_presence_status = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_DESCRIPTION));
            profile_presence_name = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_USERNAME));

        }
        cur.close();
        if (profile_presence_name != null && !profile_presence_name.equals("")) {
            mProfileNameTv.setText(profile_presence_name);
        } else {
            mProfileNameTv.setText(mProfileNumber);
        }
        if (profile_presence_status != null && !profile_presence_status.equals("")) {
            mProfileStatusTv.setText(profile_presence_status);
        } else {
            mProfileStatusTv.setText(getString(R.string.profile_available_message));
        }


    }

    private void getProfilePicture() {

        try {
            Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, mProfileNumber);
            if (cur.getCount() > 0) {
                cur.moveToNext();
                String picid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
                Bitmap mybitmap = CSDataProvider.getImageBitmap(picid);
                LOG.info(TAG1, "profile bit map " + mybitmap);

                if (mybitmap != null) {
                    isProfileImageAvailable = true;
                    mUserProfileImg.setImageBitmap(mybitmap);

                } else {
                    Bitmap contactbitmap = Utils.getContactImage(getApplicationContext(),
                            mProfileNumber);
                    if (contactbitmap != null) {
                        isProfileImageAvailable = true;
                        mUserProfileImg.setImageBitmap(contactbitmap);
                    } else {
                        mUserProfileImg.setVisibility(View.GONE);
                        mDefaultProfileImg.setVisibility(View.VISIBLE);
                    }
                }

            } else {
                Bitmap contactbitmap = Utils.getContactImage(getApplicationContext(),
                        mProfileNumber);
                if (contactbitmap != null) {
                    isProfileImageAvailable = true;
                    mUserProfileImg.setImageBitmap(contactbitmap);
                } else {
                    mUserProfileImg.setVisibility(View.GONE);
                    mDefaultProfileImg.setVisibility(View.VISIBLE);
                }
            }
            cur.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
