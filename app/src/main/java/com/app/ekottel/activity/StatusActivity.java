package com.app.ekottel.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.PermissionUtils;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.LAYER_TYPE_SOFTWARE;

import static com.app.ekottel.utils.Utils.webTypeFace;

/**
 * This activity is used to set profile picture,mUsername and status.
 *
 * @author Ramesh U
 * @version 2017
 */
public class StatusActivity extends AppCompatActivity {
    private ImageView mTvCamera;
    private TextView mTvArrowDown;
    private LinearLayout mLlStatus;
    private TextView mTvStatusDone;
    private EditText mEtNameStatus;
    private  CircleImageView mIvStatusPic;
    private String filePath = "";
    private RelativeLayout mRlProfilePic;
    private ProgressDialog progressBar;
    private Handler h = new Handler();
    private int delay = 50000;
    private Runnable runnableObj;
    private ArrayAdapter<String> adapter;
    private String emptyPresence = null;
    private PreferenceProvider mPreferenceProvider;
    private Dialog popupCloseDialog;
    private String customStatus = "";
    private String TAG;
    private String fileName;
    private TextView mTvProfileStatus;
    private PopupWindow popUpWindowObj;
    private CSClient CSClientObj = new CSClient();
    private boolean isTempFileCreated = false;
    private String mLoginNumber = "";
    private String mUsername = "";
    private String mImageID = "";
    private String mPresence = "";
    private String tempPath = "";
    boolean mIsFreshLogin = false;
    boolean mIsProfileLoaded = false;
    private String presence = "";
    private String imageID = "";
    private String name;
    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        TAG = getString(R.string.status_activity_tag);
        mTvCamera = findViewById(R.id.iv_status_camera);
        mTvArrowDown = findViewById(R.id.tv_status_arrow_down);
        mLlStatus = findViewById(R.id.ll_status_status);
        mTvProfileStatus = findViewById(R.id.tv_profile_status);
        mTvProfileStatus.setSelected(true);
        mEtNameStatus = findViewById(R.id.et_name_status);
        mIvStatusPic = findViewById(R.id.iv_status_pic);
        mRlProfilePic = findViewById(R.id.rl_profile_pic);
        mTvStatusDone = findViewById(R.id.tv_status_done);
        mPreferenceProvider = new PreferenceProvider(getApplicationContext());
        Typeface text_font = Utils.getTypeface(StatusActivity.this);
        mTvArrowDown.setTypeface(text_font);
        mTvArrowDown.setText(getString(R.string.signup_arrow_down));

        Intent intent = getIntent();
        mIsFreshLogin = intent.getBooleanExtra("isFreshLogin", false);

        mLoginNumber = CSDataProvider.getLoginID();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "hasPermissions: " + PermissionUtils.hasPermissions(StatusActivity.this, PermissionUtils.PERMISSIONS));
            if (!PermissionUtils.hasPermissions(StatusActivity.this, PermissionUtils.PERMISSIONS)) {
                PermissionUtils.requestForAllPermission(StatusActivity.this);
            }
        }

        try {
            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter1 = new IntentFilter(CSEvents.CSCLIENT_SETPROFILE_RESPONSE);
            IntentFilter filter2 = new IntentFilter(CSEvents.CSPROFILE_UPLOADPROGRESS);
            IntentFilter filter3 = new IntentFilter(CSEvents.CSPROFILE_UPLOADFILEFAILED);
            IntentFilter filter4 = new IntentFilter(CSEvents.CSPROFILE_UPLOADSUCCESS);

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter2);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter3);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter4);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Cursor cur = null;
        try {

            /*if (mIsFreshLogin) {
                CSClientObj.getProfile(mLoginNumber);
                LOG.info("getProfile called, login number: " + mLoginNumber);
            } else {*/

                LOG.info("Fetching profile Details");
                cur = CSDataProvider.getSelfProfileCursor();

            if (cur != null && cur.getCount() > 0) {
                cur.moveToNext();
                String imageid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_SELF_PROFILE_PROFILEPICID));
                String username = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_SELF_PROFILE_USERNAME));
                presence = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_SELF_PROFILE_DESCRIPTION));


                if (presence.isEmpty()) {
                    emptyPresence = getString(R.string.profile_available_message);
                }
                if (username != null) {
                    mEtNameStatus.setText(username);
                    mEtNameStatus.setSelection(mEtNameStatus.getText().length());

                }

                if (presence != null && !presence.isEmpty()) {
                    mTvProfileStatus.setText(presence);
                } else if (emptyPresence != null && !emptyPresence.isEmpty()) {
                    mTvProfileStatus.setText(emptyPresence);
                } else {
                    mTvProfileStatus.setText(getString(R.string.status_activity_set_status));
                }
                Log.i(TAG, "Image ID:" + imageid);
                imageID = imageid;
                filePath = CSDataProvider.getImageFilePath(imageid);
                tempPath = filePath;
                name = username;
                Bitmap mybitmap = CSDataProvider.getImageBitmap(imageid);

                Log.i("Status Activity", "Status Profile= server:: " + imageid + " Bitmap= " + mybitmap);
                if (mybitmap != null) {
                    try {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1
                                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            mIvStatusPic.setLayerType(LAYER_TYPE_SOFTWARE, null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mIvStatusPic.setImageBitmap(BitmapFactory.decodeFile(filePath));
                   // mIvStatusPic.setImageBitmap(mybitmap);
                }
            }
            //}
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null)
                cur.close();
        }


        adapter = new ArrayAdapter<String>(StatusActivity.this, R.layout.spinner_item) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);

                if (position == getCount()) {
                    ((TextView) v.findViewById(R.id.status_title_tv)).setText("");
                    ((TextView) v.findViewById(R.id.status_title_tv)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }


            @Override
            public int getCount() {
                return super.getCount() - 1; // you dont display last item. It is used as hint.
            }

        };

        adapter.setDropDownViewResource(R.layout.spinner_item);
        adapter.add(getString(R.string.status_activity_add_status));
        adapter.add(getString(R.string.profile_available_message));
        adapter.add(getString(R.string.profile_available_busy));
        adapter.add(getString(R.string.profile_available_send_me_message));
        adapter.add(getString(R.string.status_activity_in_meeting));
        if (mPresence != null && !mPresence.isEmpty()) {
            adapter.add(mPresence); //This is the text that will be displayed as hint.
        } else if (emptyPresence != null && !emptyPresence.isEmpty()) {
            adapter.add(getString(R.string.status_activity_set_status)); //This is the text that will be displayed as hint.
        } else {
            adapter.add(getString(R.string.status_activity_set_status));
        }


        mRlProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                popupusertoselectimagesource();
            }
        });

        mIvStatusPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popupusertoselectimagesource();
            }
        });

        mTvStatusDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {


                    String string = getIntent().getStringExtra(getString(R.string.profile_intent_profile));

                    if (string != null && !string.isEmpty()) {
                        String tempName = mEtNameStatus.getText().toString();
                        String tempPresence = mTvProfileStatus.getText().toString();
                        LOG.info("onClick: previous detyails filepath " + tempPath + " pname " + mUsername + " pPresence " + mPresence);
                        LOG.info("onClick: current detyails filepath " + filePath + " cname " + tempName + " cPresence " + tempPresence);
                        if (tempPath.equalsIgnoreCase(filePath) && tempName.equalsIgnoreCase(mUsername) && tempPresence.equalsIgnoreCase(mPresence)) {
                            LOG.info("onClick: closing the activuty");
                            Intent intent = new Intent();
                            setResult(933, intent);
                            finish();
                            return;
                        }


                    }

                    mUsername = mEtNameStatus.getText().toString();
                    mPresence = mTvProfileStatus.getText().toString();
                    LOG.info("Spinner Value " + mPresence);

                    if (mPresence != null && !mPresence.isEmpty() && mPresence.equalsIgnoreCase(getString(R.string.status_activity_set_status))) {
                        mPresence = getString(R.string.profile_available_message);
                    }

                  //  mPresence = customStatus;

                    if (mUsername == null || mUsername.isEmpty()) {
                        mEtNameStatus.setError(getString(R.string.status_activity_enter_name));
                        return;
                    }

                    if (!Utils.getNetwork(StatusActivity.this)) {
                        Toast.makeText(StatusActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
                        return;
                    }

                    showprogressbar();

                    {

                        LOG.info("Status Activity", "Status Profile= without:: " + filePath);
                        if (new File(filePath).exists()) {
                            LOG.info("onClick: file path is exists");
                        } else {
                            LOG.info("onClick: filepath not exist");
                        }
                        LOG.info("mPresence " + filePath + mPresence);
                        CSClientObj.setProfile(mUsername, mPresence, filePath);
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        mLlStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUpWindowObj = dialog_Select(StatusActivity.this, mTvProfileStatus);
                popUpWindowObj.showAsDropDown(v);
            }
        });


    }

    /**
     * This is used to share app and details of specific contact
     *
     * @param context
     * @param lin
     * @return
     */
    private PopupWindow dialog_Select(final Context context, TextView lin) {

        final PopupWindow dialog_Select = new PopupWindow(context);

        View v = View.inflate(context, R.layout.status_drop_down_list_item, null);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        dialog_Select.setContentView(v);
        dialog_Select.setWidth(width - (int) getResources().getDimension(R.dimen._110dp));
        dialog_Select.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        dialog_Select.setFocusable(true);
        dialog_Select.setBackgroundDrawable(new BitmapDrawable(
                context.getApplicationContext().getResources(),
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)));


        TextView mTvAddStatus = (TextView) v.findViewById(R.id.tv_add_status);
        TextView mTvAvailable = (TextView) v.findViewById(R.id.tv_available);
        TextView mTvBusy = (TextView) v.findViewById(R.id.tv_busy);
        TextView mTvSendMeNumber = (TextView) v.findViewById(R.id.tv_send_me_number);
        TextView mTvInMeeting = (TextView) v.findViewById(R.id.tv_in_meeting);
        LinearLayout mAddStatus = (LinearLayout) v.findViewById(R.id.ll_add_status);
        LinearLayout mAvailable = (LinearLayout) v.findViewById(R.id.ll_available);
        LinearLayout mBusy = (LinearLayout) v.findViewById(R.id.ll_busy);
        LinearLayout mSendMeMessage = (LinearLayout) v.findViewById(R.id.ll_send_me_message);
        LinearLayout mMeeting = (LinearLayout) v.findViewById(R.id.ll_meeting);
        mAddStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null) ;
                dialog_Select.dismiss();
                showAddStatusDialog();
            }
        });

        mAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null) ;
                dialog_Select.dismiss();
                mTvProfileStatus.setText(getString(R.string.profile_available_message));
            }
        });
        mBusy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null) ;
                dialog_Select.dismiss();
                mTvProfileStatus.setText(getString(R.string.profile_available_busy));
            }
        });

        mSendMeMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null) ;
                dialog_Select.dismiss();
                mTvProfileStatus.setText(getString(R.string.profile_available_send_me_message));
            }
        });
        mMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null) ;
                dialog_Select.dismiss();
                mTvProfileStatus.setText(getString(R.string.status_activity_in_meeting));
            }
        });

        Rect location = locateView(lin);

        dialog_Select.showAtLocation(v, Gravity.TOP | Gravity.CENTER, 0, location.bottom);

        // Getting a reference to Close button, and close the popup when clicked.

        return dialog_Select;

    }

    /**
     * This is used for specific place show popup view
     *
     * @param v
     * @return
     */
    public static Rect locateView(View v) {
        int[] loc_int = new int[2];
        if (v == null) return null;
        try {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe) {
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
     * Handle camera and gallery using alert dialog
     *
     * @return
     */
    public boolean popupusertoselectimagesource() {
        try {


            final Dialog dialog = new Dialog(StatusActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.status_popup);

            TextView tv_camera = (TextView) dialog.findViewById(R.id.tv_profile_camera);
            TextView tv_gallery = (TextView) dialog.findViewById(R.id.tv_profile_gallery);
            TextView tv_remove = dialog.findViewById(R.id.tv_profile_remove);
            LinearLayout remove_layout = dialog.findViewById(R.id.remove_layout);
            Typeface text_medium = Utils.getTypefaceMedium(getApplicationContext());
            tv_camera.setTypeface(text_medium);
            tv_gallery.setTypeface(text_medium);
            tv_remove.setTypeface(text_medium);
            Bitmap mybitmap = CSDataProvider.getImageBitmap(imageID);
            if (mybitmap == null) {
                remove_layout.setVisibility(View.GONE);
            }
            tv_camera.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!PermissionUtils.checkCameraPermission(StatusActivity.this) || !PermissionUtils.checkReadExternalStoragePermission(StatusActivity.this)) {
                        ActivityCompat.requestPermissions(StatusActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                        return;
                    }
                    String directoryPath = Environment.getExternalStorageDirectory() + Constants.TRINGY_DIRECTORY;
                    String fileName = "IMG_"
                            + new SimpleDateFormat(Utils.getTimeFormatForChatScreen(getApplicationContext())).format(new Date())
                            + ".jpg";
                    File mImagePath = checkFileExistence(directoryPath, fileName);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", mImagePath));
                    } else {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImagePath));
                    }
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(intent, 221);
                    filePath = mImagePath.getAbsolutePath();
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });

            tv_gallery.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!PermissionUtils.checkReadExternalStoragePermission(StatusActivity.this)) {
                        ActivityCompat.requestPermissions(StatusActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                        return;
                    }
                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    i.setType("image/*");
                    startActivityForResult(i, 999);
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
            tv_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showprogressbar();
                    filePath = "";
                    name = mEtNameStatus.getText().toString();
                    presence = mTvProfileStatus.getText().toString();
                    CSClientObj.setProfile(name, presence, filePath);

                    if (dialog != null) {
                        dialog.dismiss();
                    }
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
     * Handle gallery and camera paths
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 999 || requestCode == 221) {

                if (requestCode == 221) {
                    LOG.info("requestCode" + requestCode);
                } else {
                    Uri selectedImageURI = data.getData();
                    filePath = getRealPathFromURI(selectedImageURI);
                }

                LOG.info("File path:" + filePath);
                LOG.info("orifinal File length:" + new File(filePath).length());
                if (filePath.equals("")) {
                    Toast.makeText(StatusActivity.this, getString(R.string.chat_screen_no_image_message), Toast.LENGTH_SHORT).show();
                } else if (filePath != null && (filePath.contains("https:") || filePath.contains("http:"))) {
                    // imageDownload
                    try {
                        URI uri = new URI(filePath);
                        String[] segments = uri.getPath().split("/");
                        fileName = segments[segments.length - 1];
                        imageDownload image = new imageDownload(StatusActivity.this, mIvStatusPic);
                        image.execute(filePath);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else {
                    if (new File(filePath).length() > 10000000) {
                        filePath = "";
                        Toast.makeText(StatusActivity.this, getString(R.string.chat_screen_size_exceed_message), Toast.LENGTH_SHORT).show();
                    } else {


                        int orientation = checkOrientation(filePath);
                        LOG.info("checkOrientation:" + orientation);
                        if (orientation != 0) {
                            try {
                                isTempFileCreated = true;
                                File tempFilePath = createImageFile();
                                copyFile(new File(filePath), tempFilePath);
                                filePath = tempFilePath.toString();
                                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                                bitmap = rotateImage(bitmap, orientation);
                                if (new File(filePath).exists()) {
                                    new File(filePath).delete();
                                }
                                OutputStream os1 = new BufferedOutputStream(new FileOutputStream(filePath));
                               bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os1);
                                os1.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                        bitmap = Utils.modifyOrientation(bitmap, filePath);
                        bitmap = Bitmap.createScaledBitmap(bitmap, 1024, 1280, true);
                        if (bitmap != null)
                            mIvStatusPic.setImageBitmap(bitmap);
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    class imageDownload extends AsyncTask<String, Integer, Bitmap> {
        Context context;
        ImageView imageView;
        Bitmap bitmap;
        InputStream in = null;
        int responseCode = -1;

        //constructor.
        public imageDownload(Context context, ImageView imageView) {
            this.context = context;
            this.imageView = imageView;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.connect();
                responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    in = httpURLConnection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(in);
                    in.close();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap data) {
            imageView.setImageBitmap(data);
            saveImage(data);
        }

        private void saveImage(Bitmap data) {

            File createFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "");
            createFolder.mkdir();
            File saveImage = new File(createFolder, fileName);
            try {
                OutputStream outputStream = new FileOutputStream(saveImage);
                data.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();

                filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + fileName;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * This is used for get absolute path given uri
     *
     * @param contentURI get real path
     * @return which returns absolute path
     */
    private String getRealPathFromURI(Uri contentURI) {
        String result = "";
        try {
            Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
            if (cursor == null) { // Source is Dropbox or other similar local file path
                result = contentURI.getPath();
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
                cursor.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return result;
    }


    /**
     * Update UI whatever call back comes
     *
     * @param str this is used for display message
     */
    public void updateUI(String str, Intent i) {

        try {
            if (str.equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                LOG.info("NetworkError receieved");
                dismissprogressbar();
            } else if (str.equals(CSEvents.CSCLIENT_SETPROFILE_RESPONSE)) {

                if (i.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                    dismissprogressbar();
                    LOG.info("setProfileRessuccess");


                    mPreferenceProvider.setPrefString(getString(R.string.profile__pref_profile_name), mUsername);

                    mPreferenceProvider.setPrefString(getString(R.string.profile__pref_profile_presence), mPresence);


                    LOG.info("setProfileRessuccess after ");

                    Intent intent_new = getIntent();

                    String string = intent_new.getStringExtra(getString(R.string.profile_intent_profile));
                    if (isTempFileCreated && new File(filePath).exists()) {
                        new File(filePath).delete();
                        isTempFileCreated = false;

                    }
                    if (string != null && !string.isEmpty()) {
                        Intent intent = new Intent();
                        setResult(933, intent);
                        finish();

                    } else {
                        Intent intent = new Intent(StatusActivity.this, HomeScreenActivity.class);
                        startActivity(intent);
                        finish();
                    }


                } else {
                    Toast.makeText(StatusActivity.this, "setProfile Failed", Toast.LENGTH_SHORT).show();
                    dismissprogressbar();

                }
            } else if (str.equals(CSEvents.CSPROFILE_UPLOADPROGRESS)) {
                int imageUploadProgress = i.getIntExtra("transferpercentage", 0);
                Log.i(TAG,"image upload progress: "+imageUploadProgress);
                if (progressBar != null && progressBar.isShowing()) {
                    progressBar.setProgress(imageUploadProgress);
                }
            } else if (str.equals(CSEvents.CSPROFILE_UPLOADFILEFAILED)) {
                dismissprogressbar();
                Toast.makeText(StatusActivity.this, "setProfile Failed", Toast.LENGTH_SHORT).show();
            } else if (str.equals(CSEvents.CSPROFILE_UPLOADSUCCESS)) {
                dismissprogressbar();



            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Handle all call backs
     */
    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                Log.i(TAG, "Yes Something receieved in RecentReceiver");
                Log.i(TAG, "uploadFile images receive1" + intent.getAction());
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    updateUI(CSEvents.CSCLIENT_NETWORKERROR, intent);
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_SETPROFILE_RESPONSE)) {
                    updateUI(CSEvents.CSCLIENT_SETPROFILE_RESPONSE, intent);
                } else if (intent.getAction().equals(CSEvents.CSPROFILE_UPLOADPROGRESS)) {
                    updateUI(CSEvents.CSPROFILE_UPLOADPROGRESS, intent);
                } else if (intent.getAction().equals(CSEvents.CSPROFILE_UPLOADFILEFAILED)) {
                    updateUI(CSEvents.CSPROFILE_UPLOADFILEFAILED, intent);
                } else if (intent.getAction().equals(CSEvents.CSPROFILE_UPLOADSUCCESS)) {
                    updateUI(CSEvents.CSPROFILE_UPLOADSUCCESS, intent);
                }


            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

  /*  MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    public void onResume() {
        super.onResume();

        try {
            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter1 = new IntentFilter(CSEvents.CSCLIENT_SETPROFILE_RESPONSE);
            IntentFilter filter2 = new IntentFilter(CSEvents.CSPROFILE_UPLOADPROGRESS);
            IntentFilter filter3 = new IntentFilter(CSEvents.CSPROFILE_UPLOADFILEFAILED);
            IntentFilter filter4 = new IntentFilter(CSEvents.CSPROFILE_UPLOADSUCCESS);

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter2);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter3);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter4);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }*/

   /* @Override
    public void onPause() {
        super.onPause();

        try {

            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }*/

    /**
     * Display progress dialog
     */
    public void showprogressbar() {
        try {
            progressBar = new ProgressDialog(StatusActivity.this);
            progressBar.setCancelable(false);
            progressBar.setMessage(getString(R.string.bal_trans_wait_message));
            progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressBar.setProgress(0);
            progressBar.show();

          /*  h = new Handler();
            runnableObj = new Runnable() {

                public void run() {
                    h.postDelayed(this, delay);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            dismissprogressbar();
                            Toast.makeText(StatusActivity.this, "Request Timeout please try again later", Toast.LENGTH_SHORT).show();


                        }
                    });
                }
            };
            h.postDelayed(runnableObj, delay);*/


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Dismiss progress dialog
     */
    public void dismissprogressbar() {
        try {
            LOG.info("dismissProgressBar");
            if (progressBar != null) {
                progressBar.dismiss();

            }
            /*if (h != null) {
                h.removeCallbacks(runnableObj);
            }*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Handle set status
     */
    public void showAddStatusDialog() {
        try {

            popupCloseDialog = new Dialog(StatusActivity.this);

            popupCloseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            popupCloseDialog.setContentView(R.layout.add_status_alert);
            popupCloseDialog.setCancelable(false);
            popupCloseDialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));


            Button yes = (Button) popupCloseDialog
                    .findViewById(R.id.btn_callrates_alert_ok);
            Button no = (Button) popupCloseDialog
                    .findViewById(R.id.btn_callrates_alert_cancel);


            webTypeFace = Utils.getTypeface(StatusActivity.this);
            TextView mobile = (TextView) popupCloseDialog.findViewById(R.id.tv_callrates_mobile_icon);
            mobile.setTypeface(webTypeFace);
            final EditText et_number = (EditText) popupCloseDialog
                    .findViewById(R.id.et_callrates_number);

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customStatus = et_number.getText().toString();
                    if (customStatus.length() == 0) {
                        Toast.makeText(getApplicationContext(), getString(R.string.status_activity_enter_status_message), Toast.LENGTH_LONG).show();
                        return;
                    }
                    mTvProfileStatus.setText(customStatus);
                    popupCloseDialog.dismiss();
                    popupCloseDialog = null;

                }
            });
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupCloseDialog.dismiss();
                    popupCloseDialog = null;


                }
            });

            if (popupCloseDialog != null)
                popupCloseDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace();
        }

    }

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
                    LOG.info("checkOrientation: orientation is invalid");
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
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    /**
     * copies content from source file to destination file
     *
     * @param sourceFile
     * @param destFile
     * @throws IOException
     */
    private void copyFile(File sourceFile, File destFile) {
        try {

            if (!sourceFile.exists()) {
                return;
            }
            FileChannel source = null;
            FileChannel destination = null;
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            if (destination != null && source != null) {
                destination.transferFrom(source, 0, source.size());
            }
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private File createImageFile() {
        //LOG.info("test place2");
        // Create an image file mUsername
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = new File(Utils.getSentImagesDirectory());

        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return image;
    }
  /*  @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "requestCode is" + requestCode);
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.checkReadContactsPermission(StatusActivity.this)) {
                Utils.addContactsToSdk(getApplicationContext());
            }

        }
    }*/
    /**
     * Method is used to check file exists or not
     *
     * @param path     this is used for file path exist or not
     * @param fileName this is used for given file mUsername exists or not
     * @return which returns file
     */
    public static File checkFileExistence(String path, String fileName) {
        if (!new File(path).exists()) {
            new File(path).mkdirs();
        }
        File file = new File(path, fileName);
        return file;
    }

   /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "requestCode is" + requestCode);
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.checkReadContactsPermission(StatusActivity.this)) {
                Utils.addContactsToSdk(getApplicationContext());
            }

        }
    }*/

}
