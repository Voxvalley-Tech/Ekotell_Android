package com.app.ekottel.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.app.ekottel.utils.GlobalVariables.LOG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.activity.DialpadActivity;
import com.app.ekottel.activity.HomeScreenActivity;
import com.app.ekottel.activity.ProfileImageActivity;
import com.app.ekottel.activity.StatusActivity;
import com.app.ekottel.adapter.DisplayMyPackagesAdapter;
import com.app.ekottel.model.MyPackagesList;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.HTTPUtils;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.wrapper.CSDataProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ramesh.u on 11/13/2017.
 */

public class ProfileFragment extends Fragment {

    private static String TAG = "ProfileFragment";
    private TextView mTvEdit, mTvProfileNumber, mTvProfileName, mTvProfileStatus, mTvProfileNumberImg;
    private CircleImageView mIvProfilePic;
    public TextView mTvRegStatus, mTvProfilePackages;
    private ImageView mIvProfileStatus;
    private EditText mEtProfilePackOne, mEtProfilePackTwo;

    private ListView mListView;
    private TextView noData, tv_send_logs;
    private View mProfileView;
    public static TextView mTvProfileBalance;
    private boolean isProfilePicAvailable = false;
    PreferenceProvider pf;
    private BalanceTransferReceiver balanceTransferReceiver = new BalanceTransferReceiver();
    private List<MyPackagesList> myPackagesLists = new ArrayList<MyPackagesList>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LOG.info("Profile page onCreateView");

        if (mProfileView == null) {
            pf = new PreferenceProvider(getContext());
            mProfileView = inflater.inflate(R.layout.fragment_profile, container, false);
            mTvEdit = mProfileView.findViewById(R.id.tv_profile_edit);
            mTvProfilePackages = mProfileView.findViewById(R.id.tv_profile_packages);
            mTvProfileNumber = mProfileView.findViewById(R.id.tv_profile_number);
            mTvProfileNumberImg = mProfileView.findViewById(R.id.tv_profile_number_img);
            mTvRegStatus = mProfileView.findViewById(R.id.tv_profile_reg_status);
            mTvProfileName = mProfileView.findViewById(R.id.tv_profile_name);
            mTvProfileBalance = mProfileView.findViewById(R.id.tv_profile_balance_text);
            mTvProfileName.setSelected(true);
            tv_send_logs = mProfileView.findViewById(R.id.tv_send_logs);
            mTvProfileStatus = mProfileView.findViewById(R.id.tv_profile_status);
            mTvProfileStatus.setSelected(true);
            mEtProfilePackOne = mProfileView.findViewById(R.id.et_profile_packages_one);
            mEtProfilePackTwo = mProfileView.findViewById(R.id.et_profile_packages_two);
            mIvProfileStatus = mProfileView.findViewById(R.id.iv_profile_status);

            mIvProfilePic = mProfileView.findViewById(R.id.iv_profile);
            balanceTransferReceiver = new BalanceTransferReceiver();
            IntentFilter balanceFilter = new IntentFilter(getString(R.string.balance_transfered_successful));
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(balanceTransferReceiver, balanceFilter);
            mTvRegStatus.setSelected(true);
            mTvProfileNumber.setSelected(true);
            mTvProfileBalance.setText("$" + pf.getPrefString(getString(R.string.bal_trans_pref_avail_message)));
            Typeface text_font = Utils.getTypeface(getActivity());
            mTvEdit.setTypeface(text_font);
            mTvProfileNumberImg.setTypeface(text_font);

            mListView = mProfileView.findViewById(R.id.list);
            noData = mProfileView.findViewById(R.id.tv_profile_nodate);

            noData.setVisibility(View.VISIBLE);


            mTvEdit.setText(getResources().getString(R.string.contacts_details_edit_contact));
            mTvProfileNumberImg.setText(getResources().getString(R.string.signup_mobile));

            mIvProfilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isProfilePicAvailable) {


                        Bitmap bitmap = ((BitmapDrawable) mIvProfilePic.getDrawable()).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        Intent profileIntent = new Intent(getActivity(), ProfileImageActivity.class);
                        profileIntent.putExtra("profileContactNumber", CSDataProvider.getLoginID());
                        profileIntent.putExtra("isSelfProfile", true);
                        profileIntent.putExtra("image", byteArray);
                        startActivity(profileIntent);
                    } else {
                        Toast.makeText(getActivity(), "Profile image not available", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            tv_send_logs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShareViaEmail();
                }
            });
            LOG.info("Mobile Number" + GlobalVariables.phoneNumber);
            mTvProfileNumber.setEnabled(false);
            updateData();
            mTvEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), StatusActivity.class);
                    intent.putExtra(getString(R.string.profile_intent_profile), getString(R.string.profile_intent_profile));
                    startActivityForResult(intent, 933);
                }
            });

            try {
                MainActivityReceiverObj = new MainActivityReceiver();
                IntentFilter filter1 = new IntentFilter(CSEvents.CSCLIENT_SETPROFILE_RESPONSE);
                IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_IMAGESDBUPDATED);

                LocalBroadcastManager.getInstance(getActivity()).registerReceiver(MainActivityReceiverObj, filter1);
                LocalBroadcastManager.getInstance(getActivity()).registerReceiver(MainActivityReceiverObj, filter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Inflate the layout for this fragment
        return mProfileView;
    }

    private void ShareViaEmail() {
        try {
            File Root = getActivity().getExternalFilesDir(null);
            //String filelocation=Root.getAbsolutePath() + folder_name + "/" + file_name;
            String filelocation = getActivity().getExternalFilesDir(null) + "/ekottel.log";
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            String message = "Please find the attached logs..";
            intent.putExtra(Intent.EXTRA_SUBJECT, "ekottel Log File");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filelocation));
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.setData(Uri.parse("mailto:" + "deepika.n@voxvalley.com"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            LOG.info("Profile Broadcast called onStart");
            IntentFilter refreshRegisterIntentFilter = new IntentFilter();
            refreshRegisterIntentFilter.addAction("com.app.ekottel.RegistrationStatus");
            getActivity().registerReceiver(registrationStatuserviceReceiver, refreshRegisterIntentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            LOG.info("Profile Broadcast called onStop");
            getActivity().unregisterReceiver(registrationStatuserviceReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private BroadcastReceiver registrationStatuserviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LOG.info("Profile Broadcast called");

            try {
                PreferenceProvider pf = new PreferenceProvider(getActivity());

                boolean myPackage = pf.getPrefBoolean(getString(R.string.subscribe_package_buy_package_pref_my_packages));
                if (myPackage) {
                    pf.setPrefboolean(getString(R.string.subscribe_package_buy_package_pref_my_packages), false);
                    myPackages();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    };


    private void myPackages() {

        if (!Utils.getNetwork(getActivity())) {

            return;
        }

        if (!HomeScreenActivity.status.equalsIgnoreCase(getString(R.string.registered))) {

            return;
        }
        String user_name = "";
        String password = "";
        user_name = CSDataProvider.getLoginID();
        user_name = user_name.replace("+", "");
        String username1 = CSDataProvider.getLoginID();
        String password1 = CSDataProvider.getPassword();

        String pwd = username1 + password1;

        LOG.debug("Password" + pwd);
        String actualPassword = "";
        try {
            actualPassword = Utils.generateSHA256(pwd);
            LOG.debug("Password after SHA" + actualPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = Constants.MY_PACKAGES_URL + "%2B" + user_name + "?&password=" + actualPassword;
        new APITask(url, getString(R.string.profile_mypackages_api_key)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    /**
     * This AsyncTask will handle the API calls
     */
    class APITask extends AsyncTask<Void, Void, Void> {
        private String url;
        String api;
        String response = "";

        APITask(String url, String api) {
            this.url = url;
            this.api = api;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                response = HTTPUtils.getResponseFromURLGET(url);
            } catch (Exception e) {
                response = null;
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (response != null) {
                processResponse(api, response);
            }

        }
    }

    /**
     * This method is used for update UI  whatever response comes
     *
     * @param api        this is used for get data form given api
     * @param returndata this is used for what ever need of given api
     */
    private void processResponse(String api, String returndata) {

        if (!isAdded()) {
            return;
        }

        if (api.equals(getString(R.string.profile_mypackages_api_key))) {
            try {
                myPackagesLists.clear();

                //LOG.info("getmypackages returndata:" + returndata);
                JSONObject jsonObj = new JSONObject(returndata);
                JSONArray array = jsonObj.getJSONArray(getString(R.string.voucher_http_api_data_key));
                //LOG.info("getmypackages length:" + array.length());


                for (int i = 0; i < array.length(); i++) {
                    MyPackagesList myPackagesList = new MyPackagesList();

                    LOG.info("packagename:" + array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_pkgname)));
                    LOG.info("cost:" + array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_cost)));
                    LOG.info("expiry:" + array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_expiry)));
                    LOG.info("creationdate:" + array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_date)));
                    String id = array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_id));
                    String packagename = array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_pkgname));
                    String cost = array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_cost));
                    String expiry = array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_expiry));
                    String creationdate = array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_date));
                    String remainMinutes = array.getJSONObject(i).getString(getString(R.string.profile__packages_remaining_minutes));

                    if (id == null) {
                        id = "";
                    }
                    if (packagename == null) {
                        packagename = "";
                    }
                    if (cost == null) {
                        cost = "";
                    }
                    if (expiry == null) {
                        expiry = "";
                    }
                    if (creationdate == null) {
                        creationdate = "";
                    }
                    myPackagesList.setId(id);
                    myPackagesList.setCost(cost);
                    myPackagesList.setCreationDate(creationdate);
                    myPackagesList.setRemainingMinutes(remainMinutes);
                    myPackagesList.setPackageName(packagename);
                    myPackagesList.setExpiry(expiry);

                    myPackagesLists.add(myPackagesList);


                }


            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (myPackagesLists.size() > 0) {
                mTvProfilePackages.setVisibility(View.VISIBLE);
            } else {
                mTvProfilePackages.setVisibility(View.GONE);
            }

            PreferenceProvider pf = new PreferenceProvider(getActivity());

            pf.setPrefboolean(getString(R.string.profile_pref_my_packages), false);

            DisplayMyPackagesAdapter displayPackagesAdapter = new DisplayMyPackagesAdapter(getActivity(), myPackagesLists, 0);
            mListView.setAdapter(displayPackagesAdapter);
        } else if (api.equals(getString(R.string.dialpad_balance_api_message))) {

            try {
                JSONObject jsonObj = new JSONObject(returndata);
                String balance = "";

                JSONArray array = jsonObj.getJSONObject(getString(R.string.dialpad_response_message_key)).getJSONArray(getString(R.string.dialpad_response_message_value));
                for (int i = 0; i < array.length(); i++) {
                    balance = array.getJSONObject(i).getString(getString(R.string.dialpad_balance_api_message));
                }

                Log.i("mTvBalance", "mTvBalance--->" + balance);
                if (balance != null && !balance.isEmpty()) {
                    pf.setPrefString(getString(R.string.dialpad_avail_bal), balance);
                    mTvProfileBalance.setText("$" + balance);
                } else {
                    pf.setPrefString(getString(R.string.dialpad_avail_bal), "0");
                    mTvProfileBalance.setText("$" + "0");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 933) {
                new Handler().postDelayed(new Runnable() { // Tried new Handler(Looper.myLopper()) also
                    @Override
                    public void run() {
                        LOG.info("Data Updated");
                        //  updateData();
                    }
                }, 3000);


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will update the data on UI
     */
    private void updateData() {

        try {
            Cursor cur = CSDataProvider.getSelfProfileCursor();

            LOG.info("Data Updated cur");
            if (cur.getCount() > 0) {
                LOG.info("Data Updated getCount()" + cur.getCount());
                cur.moveToNext();
                String imageid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_SELF_PROFILE_PROFILEPICID));
                String username = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_SELF_PROFILE_USERNAME));
                String presence = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_SELF_PROFILE_DESCRIPTION));
                LOG.info("Finally image id in prfile username:" + username);
                LOG.info("Finally image id in prfile presence:" + presence);

                PreferenceProvider pf = new PreferenceProvider(getActivity());
                String profileName = pf.getPrefString(getString(R.string.profile__pref_profile_name));
                String profilePresence = pf.getPrefString(getString(R.string.profile__pref_profile_presence));

                if (username != null && !username.isEmpty()) {

                    LOG.info("Data Updated getCount() username  " + username);
                    mTvProfileName.setText(username);
                } else if (profileName != null && !profileName.isEmpty()) {
                    mTvProfileName.setText(profileName);
                }
                if (presence != null && !presence.isEmpty()) {

                    LOG.info("Data Updated getCount() presence " + presence);

                    updatePresence(presence);


                } else if (profilePresence != null && !profilePresence.isEmpty()) {
                    updatePresence(profilePresence);

                } else {
                    mIvProfileStatus.setImageResource(R.drawable.available);
                    mTvProfileStatus.setText(getString(R.string.profile_available_message));
                }
                String imagePath = CSDataProvider.getImageFilePath(imageid);
                Bitmap mybitmap = CSDataProvider.getImageBitmap(imageid);
                if (mybitmap != null) {
                    try {
                        LOG.info("bitmap image :" + mybitmap);
                        mIvProfilePic.setImageBitmap(null);
                        mIvProfilePic.setImageBitmap(mybitmap);
                        isProfilePicAvailable = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mIvProfilePic.setImageResource(R.drawable.ic_status_profile_avathar);
                    isProfilePicAvailable = false;
                }
            }
            cur.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used for update status
     *
     * @param presence this is used for update current presence
     */
    private void updatePresence(String presence) {
        if (presence.contains(getString(R.string.profile_available_message))) {
            mIvProfileStatus.setImageResource(R.drawable.available);
        } else if (presence.contains(getString(R.string.profile_available_busy))) {
            mIvProfileStatus.setImageResource(R.drawable.busy);
        } else if (presence.contains(getString(R.string.profile_available_send_me_message))) {
            mIvProfileStatus.setImageResource(R.drawable.busy);
        } else {
            mIvProfileStatus.setImageResource(R.drawable.offline);
        }
        mTvProfileStatus.setText(presence);

    }

    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (!getUserVisibleHint()) {
                return;
            }
            updateData();
            mTvProfileBalance.setText("$" + pf.getPrefString(getString(R.string.bal_trans_pref_avail_message)));
            String username = CSDataProvider.getLoginID();

            if (username != null && !username.isEmpty()) {
                mTvProfileNumberImg.setVisibility(View.VISIBLE);
                mTvProfileNumber.setText(username);
            } else {
                mTvProfileNumberImg.setVisibility(View.GONE);
            }

            String username1 = CSDataProvider.getLoginID();
            String password1 = CSDataProvider.getPassword();
            String userName = CSDataProvider.getLoginID();
            userName = userName.replace("+", "");
            String pwd = username1 + password1;

            LOG.debug("Password" + pwd);
            String actualPassword = "";
            try {
                actualPassword = Utils.generateSHA256(pwd);
                LOG.debug("Password after SHA" + actualPassword);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String url = Constants.BALANCE_URL + "%2B" + userName + "?password=" + actualPassword;
            LOG.info("Balance URL: " + url);
            new APITask(url, getString(R.string.dialpad_balance_api_message)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            if (HomeScreenActivity.status != null && !HomeScreenActivity.status.isEmpty()) {
                mTvRegStatus.setText(HomeScreenActivity.status);
            }
            PreferenceProvider pf = new PreferenceProvider(getActivity());
            String profilePresence = pf.getPrefString(getString(R.string.profile_pref_profile_presence));
            if (profilePresence != null && !profilePresence.isEmpty()) {
                if (profilePresence.contains(getString(R.string.profile_available_message))) {
                    mIvProfileStatus.setImageResource(R.drawable.available);
                } else {
                    mIvProfileStatus.setImageResource(R.drawable.offline);
                }
                mTvProfileStatus.setText(profilePresence);
            } else {
                mIvProfileStatus.setImageResource(R.drawable.available);
                mTvProfileStatus.setText(getString(R.string.profile_available_message));
            }

            try {
                boolean myPackage = pf.getPrefBoolean(getString(R.string.subscribe_package_buy_package_pref_my_packages));

                if (myPackage) {
                    pf.setPrefboolean(getString(R.string.subscribe_package_buy_package_pref_my_packages), false);
                    myPackages();
                }
                LOG.info("Mainactivity on resume is called Profile");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle all call backs
     */
    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                LOG.info("Yes Something receieved in RecentReceiver");
                LOG.info("uploadFile images receive1" + intent.getAction());
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_IMAGESDBUPDATED)) {
                    updateData();
                }


            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }

    private class BalanceTransferReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equals(getString(R.string.balance_transfered_successful))) {
                    // mTvBalance.setText("$" + mPreferenceProvider.getPrefString(getString(R.string.dialpad_avail_bal)));
                    String username1 = CSDataProvider.getLoginID();
                    String password1 = CSDataProvider.getPassword();
                    String userName = CSDataProvider.getLoginID();
                    userName = userName.replace("+", "");
                    String pwd = username1 + password1;

                    LOG.debug("Password dialpad:" + pwd);
                    String actualPassword = "";
                    try {
                        actualPassword = Utils.generateSHA256(pwd);
                        LOG.debug("Password after SHA dialpad:" + actualPassword);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String url = Constants.BALANCE_URL + "+" + userName + "?loginusername%2B" + userName + "&password=" + actualPassword;
                    new APITask(url, getString(R.string.dialpad_balance_api_message)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(registrationStatuserviceReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(MainActivityReceiverObj);
        try {
            if (balanceTransferReceiver != null) {
                LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(balanceTransferReceiver);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
