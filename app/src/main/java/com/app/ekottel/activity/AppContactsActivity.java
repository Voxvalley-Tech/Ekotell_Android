package com.app.ekottel.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.adapter.AppContactsAdapter;
import com.app.ekottel.utils.ChatConstants;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.wrapper.CSDataProvider;

/**
 * This activity is used to display App Contacts.
 *
 * @author Ramesh U
 * @version 2017
 */
public class AppContactsActivity extends AppCompatActivity {
    private static final String TAG = "AppCompatActivity";
    private ListView mLvAddContacts;
    private TextView mTvAddMessageNoContacts;
    AppContactsAdapter appContactsAdapter;
    boolean balanceTransfer = false;
    String filepath = null;
    private TextView mTvAddSearch;
    private EditText mEtAddSearch;
    private boolean mFilteredClicked = false;
    PreferenceProvider mPreferenceProvider;
    private String receivedFileType ="";
    private String receivedFilePath ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOG.info("AppCompatActivity", " is chata ctive " + ChatConstants.IS_CHAT_SCREEN_ACTIVE);
        if (ChatConstants.IS_CHAT_SCREEN_ACTIVE) {
            finish();
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_add_message);

        Typeface webTypeFace = Utils.getTypeface(getApplicationContext());
        LinearLayout ll_back = (LinearLayout) findViewById(R.id.ll_add_message_back);
        TextView back = (TextView) findViewById(R.id.add_message_back);
        TextView tv_location_header = (TextView) findViewById(R.id.tv_add_message_header);
        mTvAddSearch = (TextView) findViewById(R.id.tv_add_contacts_search);
        mEtAddSearch = (EditText) findViewById(R.id.et_add_contacts_search);



        if (ll_back != null) {
            ll_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        if (back != null)
            back.setTypeface(webTypeFace);
        mTvAddSearch.setText(getResources().getString(R.string.country_search));
        mTvAddSearch.setTypeface(webTypeFace);
        mEtAddSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String searchText = mEtAddSearch.getText().toString();
                Cursor cursor;
                if (searchText.length() > 0) {

                    cursor = CSDataProvider.getSearchAppContactsCursor(searchText);
                } else {
                    cursor = CSDataProvider.getAppContactsCursor();
                }

                if (cursor.getCount() > 0) {
                    mTvAddMessageNoContacts.setVisibility(View.GONE);
                    mLvAddContacts.setVisibility(View.VISIBLE);
                    appContactsAdapter = new AppContactsAdapter(AppContactsActivity.this, cursor, 0, balanceTransfer, receivedFileType,receivedFilePath);
                    mLvAddContacts.setAdapter(appContactsAdapter);
                } else {
                    mTvAddMessageNoContacts.setVisibility(View.VISIBLE);
                    mLvAddContacts.setVisibility(View.GONE);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });


        mLvAddContacts = (ListView) findViewById(R.id.lv_add_contacts);
        mTvAddMessageNoContacts = (TextView) findViewById(R.id.tv_add_message_no_contacts);
        Intent intent = getIntent();

        if (intent.getBooleanExtra(getString(R.string.add_message_pref_balance), false)) {
            balanceTransfer = intent.getBooleanExtra(getString(R.string.add_message_pref_balance), false);
        }

        Intent i = getIntent();
        String action = i.getAction();
        String type = i.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                receivedFileType = "text";
                receivedFilePath = intent.getStringExtra(Intent.EXTRA_TEXT);
                Log.i(TAG, "text received " + receivedFileType);
            } else if (type.startsWith("image/")) {
                receivedFileType = "image";
                receivedFilePath = intent.getParcelableExtra(Intent.EXTRA_STREAM).toString();
                Log.i(TAG, "iamge received");
            } else if (type.startsWith("audio/")) {
                Log.i(TAG, "audio received");
                receivedFileType = "audio";
                receivedFilePath = intent.getParcelableExtra(Intent.EXTRA_STREAM).toString();
            } else if (type.startsWith("video/")) {
                Log.i(TAG, "video received");
                receivedFileType = "video";
                receivedFilePath = intent.getParcelableExtra(Intent.EXTRA_STREAM).toString();
            }

        }



        Cursor c = CSDataProvider.getAppContactsCursor();
        if (c.getCount() > 0) {
            mTvAddMessageNoContacts.setVisibility(View.GONE);
            mLvAddContacts.setVisibility(View.VISIBLE);
            appContactsAdapter = new AppContactsAdapter(AppContactsActivity.this, c, 0, balanceTransfer, receivedFileType,receivedFilePath);
            mLvAddContacts.setAdapter(appContactsAdapter);
        } else {
            mTvAddMessageNoContacts.setVisibility(View.VISIBLE);
            mLvAddContacts.setVisibility(View.GONE);
        }



    }


    /**
     * This method to get absolute path
     *
     * @param contentURI get actual path
     * @return which returns path
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

    @Override
    public void onBackPressed() {
        finish();
    }
}
