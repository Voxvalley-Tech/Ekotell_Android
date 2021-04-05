package com.app.ekottel.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.multidex.MultiDex;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.adapter.FirstCallRecentsDetailLogAdapter;
import com.app.ekottel.utils.CallMethodHelper;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;

import java.lang.ref.WeakReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowCallLogHistory extends AppCompatActivity {
    String managecontactnumber = "";
    String managedirection = "";

    ListView mListView;
    Toolbar toolbar;
    //FloatingActionButton fab;ff
    //TextView subtitle;
    CSClient CSClientObj = new CSClient();
    FirstCallRecentsDetailLogAdapter appContactsAdapter;
    private TextView mCallLogsNameTv;
    private CircleImageView mCallLogsImage;
    private String TAG = "ShowUserLogActivity";
    String isApp = "0";
    AppCompatImageView appinfo_profile_call;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_call_log_history);
        try {

            //ImageView profileimage = (ImageView) findViewById(R.id.grpimg);
            //AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);

            //subtitle = (TextView) findViewById(R.id.subtitle);
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mCallLogsNameTv = findViewById(R.id.call_logs_name_tv);
            mCallLogsImage = findViewById(R.id.user_profle_image);
            appinfo_profile_call = findViewById(R.id.appinfo_profile_call);

            managecontactnumber = getIntent().getStringExtra("number");
            managedirection = getIntent().getStringExtra("direction");
            String name = getIntent().getStringExtra("name");
            String id = getIntent().getStringExtra("id");
            if (name.equals("")) {
                name = managecontactnumber;
            }
            LOG.info("ShowUserLogActivity", "call log number " + managecontactnumber);


            Cursor cr = CSDataProvider.getContactCursorByNumber(managecontactnumber);

            if (cr.getCount() > 0) {
                cr.moveToNext();
                isApp = cr.getString(cr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_IS_APP_CONTACT));
                id = cr.getString(cr.getColumnIndex(CSDbFields.KEY_CONTACT_ID));
            } else {
                isApp = "0";
            }
            cr.close();
            LOG.info("Calllog type " + isApp);


            getProfilePicture();
            getSupportActionBar().setTitle("Call Details");
            //getSupportActionBar().setSubtitle(managecontactnumber);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mCallLogsNameTv.setText(name);
            //subtitle.setText(managecontactnumber);

            PreferenceProvider preferenceProvider = new PreferenceProvider(getApplicationContext());
            preferenceProvider.setPrefString(managecontactnumber + "MissedData", "");
            //new ImageDownloaderTask(profileimage).execute(id);


            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();

                }
            });


            mListView = (ListView) findViewById(R.id.appcontacts1);
            LOG.info("managecontactnumber:" + managecontactnumber);
            LOG.info("managedirection:" + managedirection);
            appContactsAdapter = new FirstCallRecentsDetailLogAdapter(ShowCallLogHistory.this, CSDataProvider.getCallLogCursorByFilter(CSDbFields.KEY_CALLLOG_NUMBER, managecontactnumber), 0, managecontactnumber);
            //setListviewheight();
            mListView.setAdapter(appContactsAdapter);

            Cursor cur = CSDataProvider.getCallLogCursorByFilter(CSDbFields.KEY_CALLLOG_NUMBER, managecontactnumber);
            cur.close();

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CallMethodHelper.processAudioCall(ShowCallLogHistory.this, managecontactnumber, "PSTN");
                }
            });

            appinfo_profile_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LOG.info("onItemClick: direction of call is ");
                    TextView c = (TextView) view.findViewById(R.id.text1);
                    String direction = c.getText().toString();
                    LOG.info("onItemClick: direction of call is "+direction);
                    if (direction.contains("pstn")) {
                        CallMethodHelper.processAudioCall(ShowCallLogHistory.this, managecontactnumber, "PSTN");
                    } else if (direction.contains("video")) {
                        CallMethodHelper.placeVideoCall(ShowCallLogHistory.this, managecontactnumber);
                    } else {
                        CallMethodHelper.processAudioCall(ShowCallLogHistory.this, managecontactnumber, "PSTN");
                    }

                }
            });
/*
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if(verticalOffset == 0){
                        //LOG.info("Offset 0");
                        subtitle.setPadding(72,0,0,0);
                    }else {
                        subtitle.setPadding(112,0,0,0);
                        //LOG.info("Offset 1");
                    }
                }
            });
*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 888) {
            LOG.info("onActivityResult called here");

        }
    }


    public void updateUI(String str) {

        try {
            if (str.equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                LOG.info("NetworkError receieved");
                //Toast.makeText(this, "NetworkError", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {
        }
    }


    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                LOG.info("Yes Something receieved in RecentReceiver ShowUserLogActivity");
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    updateUI(CSEvents.CSCLIENT_NETWORKERROR);
                }
            } catch (Exception ex) {
            }
        }
    }

    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    public void onResume() {
        super.onResume();
        try {


            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            appContactsAdapter.changeCursor(CSDataProvider.getCallLogCursorByFilter(CSDbFields.KEY_CALLLOG_NUMBER, managecontactnumber));
            appContactsAdapter.notifyDataSetChanged();

            Cursor cur = CSDataProvider.getCallLogCursorByFilter(CSDbFields.KEY_CALLLOG_NUMBER, managecontactnumber);
            cur.close();

        } catch (Exception ex) {
        }

    }

    private void getProfilePicture() {
        String nativecontactid = "";
        Cursor cur = CSDataProvider.getContactCursorByNumber(managecontactnumber);
        if (cur.getCount() > 0) {
            cur.moveToNext();
            nativecontactid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
        }
        cur.close();

        String picid = "";
        Cursor cur1 = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, managecontactnumber);
        if (cur1.getCount() > 0) {
            cur1.moveToNext();
            picid = cur1.getString(cur1.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
        }
        cur1.close();

        new ImageDownloaderTask(mCallLogsImage).execute("app", picid, nativecontactid);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.exitmenu, menu);
        getMenuInflater().inflate(R.menu.chatmenu1, menu);
        if(isApp.equalsIgnoreCase("0" )){
            return false;
        }else {
            return true;
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.audiocall:
                CallMethodHelper.processAudioCall(ShowCallLogHistory.this, managecontactnumber, "PSTN");
                return true;

            case R.id.videocall:
                CallMethodHelper.placeVideoCall(ShowCallLogHistory.this, managecontactnumber);
                return true;

            case R.id.chat:

                Intent intent = new Intent(getApplicationContext(), ChatAdvancedActivity.class);
                intent.putExtra(Constants.INTENT_CHAT_CONTACT_NUMBER, managecontactnumber);
                intent.putExtra(Constants.IS_VIDEO_CALL_RUNNING, false);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        try {

            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
        } catch (Exception ex) {
        }


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            finish();
        } catch (Exception ex) {

        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap photo = null;
            try {
                if (params[0].equals("app")) {
                    photo = CSDataProvider.getImageBitmap(params[1]);
                } else {
                    photo = Utils.loadContactPhoto(getApplicationContext(), Long.parseLong(params[1]));
                }
                if (params[0].equals("app") && photo == null) {
                    photo = Utils.loadContactPhoto(getApplicationContext(), Long.parseLong(params[2]));
                }

                if (photo == null) {
                    photo = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_status_profile_avathar);
                }
            } catch (Exception e) {
                photo = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_status_profile_avathar);
                //utils.logStacktrace(e);
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
                        //imageView.setImageBitmap(getRoundedCornerBitmap(bitmap, 10));
                    } else {
						/*TextDrawable drawable2 = TextDrawable.builder()
				                .buildRound("A", Color.RED);*/
						/*Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.placeholder);
						imageView.setImageDrawable(placeholder);*/
                    }
                }
            }
        }
    }


}