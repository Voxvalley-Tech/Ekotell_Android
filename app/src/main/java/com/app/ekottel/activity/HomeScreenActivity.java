package com.app.ekottel.activity;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.ekottel.R;
import com.app.ekottel.fragment.CallLogsFragment;
import com.app.ekottel.fragment.ContactsFragment;
import com.app.ekottel.fragment.MessagesFragment;
import com.app.ekottel.fragment.MoreFragment;
import com.app.ekottel.fragment.ProfileFragment;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.HTTPUtils;
import com.app.ekottel.utils.MyService;
import com.app.ekottel.utils.PermissionUtils;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.Utils.CSExplicitEvents;
import com.ca.custom.CSCustomApis;
import com.ca.dao.CSAppDetails;
import com.ca.dao.CSExplicitEventReceivers;
import com.ca.wrapper.CSCall;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.app.ekottel.utils.GlobalVariables.LOG;

/**
 * Created by ramesh.u on 11/13/2017.
 */

public class HomeScreenActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SET_DEFAULT_DIALER = 111;
    public TabLayout tabLayout;
    public static ViewPager viewPager;
    public static TextView mUnreadCount;
    public static Context context;
    private String username = "";
    private String password = "";
    private String TAG;
    private String filePath = null;
    private String senderNumber = null;
    private String ContactID = null;
    public static boolean canCall = false;
    private Handler h1 = new Handler();
    private Runnable runnableObj1;
    public static String status = "Not Registered";
    private NotificationManager notificationManager;
    public static Dialog mPopupCloseDialog = null;
    Dialog mAutoLogoutDialog = null;
    private ServicePhoneStateReceiver phoneConnectivityReceiver;
    private TelephonyManager telephonyManager;
    public static String lastDialedNumber = "";
    public static String packageName;
    private ContactsFragment mContactFragment;
    private CallLogsFragment mCallLogsFragment;
    private MessagesFragment mMessagesFragment;
    private ProfileFragment mProfileFragment;
    private MoreFragment mMoreFragment;
    private boolean showRegNotif = true;
    private CSClient CSClientObj = new CSClient();
    private CallBackReceiver mSDKCallBacks = new CallBackReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen_activity);
        TAG = getString(R.string.home_activity_tag);
        viewPager = findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(5);
        tabLayout = findViewById(R.id.tabs);
        context = HomeScreenActivity.this;

        //new CSCall().setPreferredAudioCodec(CSConstants.PreferredAudioCodec.G729);
        // If OS version is more than Lollipop we ask all below permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LOG.info("hasPermissions: " + PermissionUtils.hasPermissions(HomeScreenActivity.this, PermissionUtils.PERMISSIONS));
            PermissionUtils.requestForAllPermission(HomeScreenActivity.this);
        }
        mContactFragment = new ContactsFragment();
        mCallLogsFragment = new CallLogsFragment();
        //  mMessagesFragment = new MessagesFragment();
        mMoreFragment = new MoreFragment();
        mProfileFragment = new ProfileFragment();


        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorHeight(0);
        int position = viewPager.getCurrentItem();
        viewPager.setCurrentItem(position);
        updateTabs(position);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        packageName = getApplicationContext().getPackageName();
        if (phoneConnectivityReceiver == null) {
            phoneConnectivityReceiver = new ServicePhoneStateReceiver();
            telephonyManager.listen(phoneConnectivityReceiver, PhoneStateListener.LISTEN_CALL_STATE);
        }

        username = CSDataProvider.getLoginID();
        PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
        Log.e("HomeAcivity", "count-->" + com.ca.app.App.getActivityStackCount());


        pf.setPrefboolean(getString(R.string.call_logs_incall_message), false);


        //This is used to check if  not allow second call if already call is there

        Boolean bl = pf.getPrefBoolean(getApplicationContext().getString(R.string.call_logs_incall_message));
        Log.e("HomeAcivity", "HomeAcivity-->" + bl);


        //This is used to avoid multiple calls ringing state
        pf.setPrefboolean(getString(R.string.play_video_call_pref_ringing_key), false);

        //This is used to avoid multiple calls
        pf.setPrefboolean(getString(R.string.play_video_call_pref_already_login), false);

        //this is used to update mypackes if required
        pf.setPrefboolean(getString(R.string.profile_pref_my_packages), true);

        pf.setPrefString("activeDestination", username);
        pf.setPrefboolean("RegisterHome", true);
        pf.setPrefboolean("isIncomingCall", false);
        pf.setPrefString("DialpadNumber", null);
        pf.setPrefboolean("isEditClick", false);
        pf.setPrefString("DialpadNumberCountry", null);
        pf.setPrefboolean("CallRunning", false);
        pf.setPrefboolean("chatUpdated", false);
        pf.setPrefboolean("clearBackground", false);
        pf.setPrefboolean("ChatPageLaunch", false);


        if (!CSDataProvider.getLoginstatus()) {
            CSAppDetails csAppDetails = new CSAppDetails("Ekottel", "iamlivedbnew"); // Tringy
            /*  CSAppDetails  csAppDetails=new CSAppDetails("iamLive","aid_8656e0f6_c982_4485_8ca7_656780b53d34"); // Konverz*/
            CSClientObj.initialize(GlobalVariables.server, GlobalVariables.port, csAppDetails);
        } else {
            updateRegisterStatus();
        }


        if (!pf.getPrefBoolean("OfflineUsers")) {

            boolean status = CSClientObj.registerExplicitEventReceivers(new CSExplicitEventReceivers("", "com.app.ekottel.receivers.CallReceiver", "com.app.ekottel.receivers.ChatReceiver", "", "com.app.ekottel.receivers.CSCallMissed", "com.app.ekottel.receivers.PromotionalMessageReceiver", "com.app.ekottel.receivers.LoginSomeWhereElseReceiver"));
            pf.setPrefboolean("OfflineUsers", status);
            Log.e("OfflineUsers", "OfflineUsers-->" + pf.getPrefBoolean("OfflineUsers"));
        }
        CSCustomApis csPstnExtentions = new CSCustomApis();
        Cursor cur = csPstnExtentions.getallurls();
        LOG.info("onCreate: url cursor " + cur.getCount());
        while (cur.moveToNext()) {
            String urlname = cur.getString(cur.getColumnIndexOrThrow("urlname"));
            String url = cur.getString(cur.getColumnIndexOrThrow("urlurl"));
            //LOG.info("urlname:" + urlname + "\nurl:" + url + "\n");
            if ("url_balance".equalsIgnoreCase(urlname)) {
                Constants.BALANCE_URL = url;
            } else if ("url_voucherrecharge".equalsIgnoreCase(urlname)) {
                Constants.VOUCHER_RECHARGE_URL = url;
            } else if ("url_transactionhistory".equalsIgnoreCase(urlname)) {
                Constants.TRANSACTION_HISTORY_URL = url;
            } else if ("url_subscribepackage".equalsIgnoreCase(urlname)) {
                Constants.SUBSCRIBE_PACKAGE_URL = url;
            } else if ("url_getmypackages".equalsIgnoreCase(urlname)) {
                Constants.MY_PACKAGES_URL = url;
            } else if ("url_callrates".equalsIgnoreCase(urlname)) {
                Constants.CALL_RATES_URL = url;
            } else if ("url_balancetransfer".equalsIgnoreCase(urlname)) {
                Constants.BALANCE_TRANSFER_URL = url;
                Log.e("BALANCE_TRANSFER_URL", "BALANCE_TRANSFER_URL-->" + url);
            } else if ("url_getpackages".equalsIgnoreCase(urlname)) {
                Constants.PACKAGES_URL = url;
            } else if ("url_paypalgetclientid".equalsIgnoreCase(urlname)) {
                Constants.PAYPAL_GETCLIENTID_URL = url;
                Log.e("PAYPAL_GETCLIENTID_URL", "PAYPAL_GETCLIENTID_URL-->" + url);
            } else if ("url_paypalverifypayment".equalsIgnoreCase(urlname)) {
                Constants.PAYPAL_VERIFYPAYMENT_URL = url;
                Log.e("PAYPAL_VERIFY", "PAYPAL_VERIFYPAYMENT_URL-->" + url);

            } else if ("createstripepaymentintent".equalsIgnoreCase(urlname)) {
                Constants.STRIPE_CREATE_STRIPE_PAYMENT_INTENT = url;
                Log.e("PAYPAL_VERIFY", "STRIPE_CREATE_STRIPE_PAYMENT_INTENT-->" + url);
            } else if ("url_stripepayment".equalsIgnoreCase(urlname) || "veifystripepayment".equalsIgnoreCase(urlname)) {
                Constants.STRIPE_VERIFICATION_URL = url;
            } else if ("url_getstripekey".equalsIgnoreCase(urlname)) {
                Constants.STRIPE_PUBLISHABLE_KEY_URL = url;
            }
        }
        cur.close();

        Intent notification_intent = getIntent();
        if (notification_intent != null) {
            LOG.info("Notification Testing Home");
            String str = notification_intent.getStringExtra("Notification");
            String channelid = notification_intent.getStringExtra("Sender");
            if (str != null && !str.isEmpty() && channelid != null) {
                LOG.info("Notification notification " + str);

                Intent intent_image_share = new Intent(context.getApplicationContext(), ChatAdvancedActivity.class);
                intent_image_share.putExtra(Constants.INTENT_CHAT_CONTACT_NUMBER, senderNumber);
                intent_image_share.putExtra(Constants.IS_VIDEO_CALL_RUNNING, false);
                intent_image_share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        }

        if (notification_intent != null) {
            String str = notification_intent.getStringExtra("PromotionalNotification");
            if (str != null && !str.isEmpty()) {
                LOG.info("Notification notification " + str);
                Intent intent1 = new Intent(this, ViewMyPromotionsActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
            }
        }
        Intent intent_share = getIntent();
        String imageShare = intent_share.getStringExtra("ImageShare");

        if (intent_share != null && imageShare != null && imageShare.equalsIgnoreCase("ImageShare")) {
            ContactID = getIntent().getStringExtra("ContactID");
            senderNumber = getIntent().getStringExtra(getString(R.string.call_logs_intent_sender_key));

            String receivedFileType = getIntent().getStringExtra("receivedFileType");
            String receivedFilePath = getIntent().getStringExtra("receivedFilePath");

            String name = "";
            Cursor contactcursor = CSDataProvider.getContactCursorByNumber(senderNumber);
            if (contactcursor.getCount() > 0) {
                contactcursor.moveToNext();
                name = contactcursor.getString(contactcursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
            }
            contactcursor.close();
            Intent intent_image_share = new Intent(context.getApplicationContext(), ChatAdvancedActivity.class);
            intent_image_share.putExtra(Constants.INTENT_CHAT_CONTACT_NUMBER, senderNumber);
            intent_image_share.putExtra(Constants.INTENT_CHAT_CONTACT_NAME, name);
            intent_image_share.putExtra(Constants.IS_VIDEO_CALL_RUNNING, false);
            intent_image_share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);


            if (receivedFileType != null && !receivedFileType.equals("")) {
                PreferenceProvider preferenceProvider = new PreferenceProvider(context);
                preferenceProvider.setPrefboolean(PreferenceProvider.IS_FILE_SHARE_AVAILABLE, true);
                intent_image_share.putExtra("isShareFileAvailable", true);
                intent_image_share.putExtra("receivedFileType", receivedFileType);
                intent_image_share.putExtra("receivedFilePath", receivedFilePath);
            }

            startActivity(intent_image_share);

        }

        username = CSDataProvider.getLoginID();
        username = username.replace("+", "");
        password = CSDataProvider.getPassword();
        new Thread(new Runnable() {
            public void run() {
                try {

                    // Handle balance using rest API
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
                    String url = Constants.BALANCE_URL + "%2B" + username + "?password=" + actualPassword;
                    new APITask(url, getString(R.string.dialpad_balance_api_message)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        startService(new Intent(this, MyService.class));
        //startLogCapture();
        getOptimizerPermissions();

        try {

            LOG.info("Mainactivity on resume is called");

            GlobalVariables.loginretries = 0;

            LocalBroadcastManager.getInstance(HomeScreenActivity.this).unregisterReceiver(mSDKCallBacks);

            mSDKCallBacks = new CallBackReceiver();

            IntentFilter filter1 = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter2 = new IntentFilter(CSEvents.CSCLIENT_LOGIN_RESPONSE);
            IntentFilter filter3 = new IntentFilter(CSEvents.CSCALL_CALLLOGUPDATED);
            IntentFilter filter4 = new IntentFilter(CSExplicitEvents.CSChatReceiver);
            IntentFilter filter5 = new IntentFilter(CSEvents.CSCLIENT_INITILIZATION_RESPONSE);
            IntentFilter filter6 = new IntentFilter(CSEvents.CSCLIENT_PSTN_REGISTRATION_RESPONSE);

            LocalBroadcastManager.getInstance(HomeScreenActivity.this).registerReceiver(mSDKCallBacks, filter1);
            LocalBroadcastManager.getInstance(HomeScreenActivity.this).registerReceiver(mSDKCallBacks, filter2);
            LocalBroadcastManager.getInstance(HomeScreenActivity.this).registerReceiver(mSDKCallBacks, filter3);
            LocalBroadcastManager.getInstance(HomeScreenActivity.this).registerReceiver(mSDKCallBacks, filter4);
            LocalBroadcastManager.getInstance(HomeScreenActivity.this).registerReceiver(mSDKCallBacks, filter5);
            LocalBroadcastManager.getInstance(HomeScreenActivity.this).registerReceiver(mSDKCallBacks, filter6);


            /*IntentFilter autoLogoutFilter = new IntentFilter();
            autoLogoutFilter.addAction("com.app.banatelecom.autoLogout");
            registerReceiver(loginSomeWhereElseReceiver, autoLogoutFilter);*/

            LOG.info("notification status " + pf.getPrefBoolean(getString(R.string.home_activity_intent_is_missed_call_notif)));
            if (pf.getPrefBoolean(getString(R.string.home_activity_intent_is_missed_call_notif))) {
                //tabHost.setCurrentTab(1);

                pf.setPrefboolean(getString(R.string.home_activity_intent_is_missed_call_notif), false);

                viewPager.setCurrentItem(1);
            }
            LOG.info("Mainactivity on resume is called home");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //making this APP as a default appliccation for the outling calls from the third parti applications.
        // makeDefaultDialerApp();

        handleDialedNumberFromAnotherApp();
    }


    /**
     * this method handles the phone number coming from the 3rd party aps (Whats app or any other)
     * place this phone number into the dialer Activity.
     */
    private void handleDialedNumberFromAnotherApp() {
        Intent dataIntent = getIntent();

        if (dataIntent != null && dataIntent.getData() != null) {
            String phoneNumber = dataIntent.getData().toString();
            phoneNumber = phoneNumber.substring(4);
            phoneNumber = Uri.decode(phoneNumber);


            Log.e(TAG, "phoneNumber from 3rd paty app " + phoneNumber);

            Intent intent_dialpad = new Intent(this, DialpadActivity.class);
            intent_dialpad.putExtra("DialNumberFromAnotherApp", phoneNumber);
            startActivity(intent_dialpad);

        }
    }


    private void makeDefaultDialerApp() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        PreferenceProvider mPreferenceProvider = new PreferenceProvider(this);

        if (mPreferenceProvider.getPrefBoolean(PreferenceProvider.DONT_SHOW_DEFAULT_APP_SETTINGS))
            return;

        TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);

        if (telecomManager.getDefaultDialerPackage().equals(getPackageName())) return;

        Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER);
            mPreferenceProvider.setPrefboolean(PreferenceProvider.DONT_SHOW_DEFAULT_APP_SETTINGS, true);
        } else {
            Log.e(TAG, "No activity found for TelecomManager.ACTION_CHANGE_DEFAULT_DIALER");
        }
        Log.e(TAG, " PackageName " + getPackageName() + "  default phone packege name " + telecomManager.getDefaultDialerPackage());

    }


    public void getOptimizerPermissions() {
        try {
            PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
            boolean dontshowagain = pf.getPrefBoolean("dontshowagain");


            if (!dontshowagain) {
                //boolean checkpermission = false;


                String manufacturer = android.os.Build.MANUFACTURER;
                int apilevel = Build.VERSION.SDK_INT;

                LOG.info("manufacturer:" + manufacturer);
                LOG.info("apilevel:" + apilevel);

                Intent intent = new Intent();
                if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
                } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                } else if ("oneplus".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListAct‌​ivity"));
                } else if ("huawei".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                } else if ("samsung".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity"));
                } else if ("asus".equalsIgnoreCase(manufacturer)) {

/*
                    PackageManager pm = getPackageManager();
                    List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                    for (ApplicationInfo packageInfo : packages)
                    {
                        LOG.info("Installed package :" + packageInfo.packageName);
                        LOG.info("Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
                    }*/


                } else if ("sony".equalsIgnoreCase(manufacturer)) {

                } else if ("htc".equalsIgnoreCase(manufacturer)) {

                } else if ("lenovo".equalsIgnoreCase(manufacturer)) {

                } else if (apilevel >= 23) {

                }


                List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (list.size() <= 0) {
                    //utils.showSettingsAlert("Enable autostart permission to receive notifications",MainActivity.this);
                    invokePermissionDialog("Enable autostart permission to receive notifications", null);
                    //return;
                } else {
                    //utils.showIntentAlert("Enable autostart permission to receive notifications", MainActivity.this, intent);
                    invokePermissionDialog("Enable autostart permission to receive notifications", intent);
                }

                //if(!checkpermission) {


                PreferenceProvider pff = new PreferenceProvider(getApplicationContext());
                pff.setPrefboolean("dontshowagain", true);
                //}

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * This is handle to display delete notification
     */
    private void invokePermissionDialog(final String message, final Intent intent) {

        if (mPopupCloseDialog == null) {
            try {
                mPopupCloseDialog = new Dialog(HomeScreenActivity.this);

                mPopupCloseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mPopupCloseDialog.setContentView(R.layout.alertdialog_signup);
                mPopupCloseDialog.setCancelable(false);
                mPopupCloseDialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));


                Button yes = (Button) mPopupCloseDialog
                        .findViewById(R.id.btn_alert_ok);
                Button no = (Button) mPopupCloseDialog
                        .findViewById(R.id.btn_alert_cancel);


                TextView tv_title = (TextView) mPopupCloseDialog
                        .findViewById(R.id.tv_alert_title);
                TextView tv_message = (TextView) mPopupCloseDialog
                        .findViewById(R.id.tv_alert_message);
                //  tv_message.setText(message);
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {

                            try {
                                //Open the specific App Info page:
                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                startActivity(intent);

                            } catch (ActivityNotFoundException e) {
                                //e.printStackTrace();

                                //Open the generic Apps page:
                                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                startActivity(intent);

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mPopupCloseDialog.dismiss();
                        mPopupCloseDialog = null;


                    }
                });
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mPopupCloseDialog.dismiss();
                        mPopupCloseDialog = null;

                    }
                });

                if (mPopupCloseDialog != null)
                    mPopupCloseDialog.show();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void startLogCapture() {
        try {
            System.out.println("startLogCapture");
            File filename = new File(context.getExternalFilesDir(null) + "/ekottel.log");
            filename.createNewFile();
            String cmd = "logcat -d -f " + filename.getAbsolutePath();
            Runtime.getRuntime().exec(cmd);
            System.out.println("startLogCapture done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*private void showNotification(final String status) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            notificationManager.cancelAll();

                            Utils.NotifyAppInBackground(getString(R.string.notification_title), status, "", "", 0, getApplicationContext());

                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }*/


    private void updateTabs(int currenttab) {

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorHeight(0);
        switch (currenttab) {
            case 0:

                tabLayout.getTabAt(0).setCustomView(customContactsView(currenttab));
                tabLayout.getTabAt(1).setCustomView(customCallLogsView(-1));
                tabLayout.getTabAt(2).setCustomView(customProfileView(-1));
                tabLayout.getTabAt(3).setCustomView(customMoreView(-1));
                //   tabLayout.getTabAt(4).setCustomView(customMoreView(-1));

                break;
            case 1:
                tabLayout.getTabAt(0).setCustomView(customContactsView(-1));
                tabLayout.getTabAt(1).setCustomView(customCallLogsView(currenttab));
                tabLayout.getTabAt(2).setCustomView(customProfileView(-1));
                tabLayout.getTabAt(3).setCustomView(customMoreView(-1));
                // tabLayout.getTabAt(4).setCustomView(customMoreView(-1));
                break;
            case 2:
                tabLayout.getTabAt(0).setCustomView(customContactsView(-1));
                tabLayout.getTabAt(1).setCustomView(customCallLogsView(-1));
                tabLayout.getTabAt(2).setCustomView(customProfileView(currenttab));
                tabLayout.getTabAt(3).setCustomView(customMoreView(-1));
                // tabLayout.getTabAt(4).setCustomView(customMoreView(-1));
                break;
            case 3:
                tabLayout.getTabAt(0).setCustomView(customContactsView(-1));
                tabLayout.getTabAt(1).setCustomView(customCallLogsView(-1));
                tabLayout.getTabAt(2).setCustomView(customProfileView(-1));
                tabLayout.getTabAt(3).setCustomView(customMoreView(currenttab));
                //  tabLayout.getTabAt(4).setCustomView(customMoreView(-1));
                break;
            /*case 4:

                tabLayout.getTabAt(0).setCustomView(customContactsView(-1));
                tabLayout.getTabAt(1).setCustomView(customCallLogsView(-1));
                tabLayout.getTabAt(2).setCustomView(customIMView(-1));
                tabLayout.getTabAt(3).setCustomView(customProfileView(-1));
                tabLayout.getTabAt(4).setCustomView(customMoreView(currenttab));
                break;*/

            default:
                tabLayout.getTabAt(0).setCustomView(customContactsView(currenttab));
                tabLayout.getTabAt(1).setCustomView(customCallLogsView(-1));
                tabLayout.getTabAt(2).setCustomView(customProfileView(-1));
                tabLayout.getTabAt(3).setCustomView(customMoreView(-1));
                //  tabLayout.getTabAt(4).setCustomView(customMoreView(-1));
                break;


        }
    }

    private View customContactsView(int currenttab) {
        int contacts_drawable = getTabDrawable(currenttab);

        View contacts_view = createTabView(HomeScreenActivity.this, "Contacts", currenttab == -1 ?
                R.drawable.ic_contacts_normal : R.drawable.ic_contacts_hover, currenttab);

        return contacts_view;
    }

    private View customCallLogsView(int currenttab) {
        int call_logs_drawable = getTabDrawable(currenttab);


        View call_logs_view = createTabView(HomeScreenActivity.this, "Calls", currenttab == -1 ? R.drawable.ic_call_logs_normal : R.drawable.ic_call_logs_hover, currenttab);

        return call_logs_view;
    }

   /* private View customIMView(int currenttab) {

        int im_drawable = getTabDrawable(currenttab);
        View im_view = createTabView(HomeScreenActivity.this, "IM", currenttab == -1 ? R.drawable.ic_chats_normal : R.drawable.ic_chats_hover, currenttab);

        return im_view;
    }*/

    private View customProfileView(int currenttab) {

        int profile_drawable = getTabDrawable(currenttab);
        View profile_view = createTabView(HomeScreenActivity.this, "Profile", currenttab == -1 ? R.drawable.ic_profile_normal : R.drawable.ic_profile_hover, currenttab);

        return profile_view;
    }

    private View customMoreView(int currenttab) {

        int more_drawable = getTabDrawable(currenttab);
        View more_view = createTabView(HomeScreenActivity.this, "More", currenttab == -1 ? R.drawable.ic_more_normal : R.drawable.ic_more_hover, currenttab);

        return more_view;
    }

    private int getTabDrawable(int currentTab) {
        final int current_drawable;
        if (currentTab > -1) {
            current_drawable = (Color.parseColor("#0063b2"));
        } else {
            current_drawable = (Color.parseColor("#a2a2a2"));
        }

        return current_drawable;
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(mContactFragment, "Contacts");
        adapter.addFragment(mCallLogsFragment, "Call Logs");
        // adapter.addFragment(mMessagesFragment, "IM");
        adapter.addFragment(mProfileFragment, "Profile");
        adapter.addFragment(mMoreFragment, "More");
        viewPager.setAdapter(adapter);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                System.out.println("position:" + position);
                updateTabs(position);

                switch (position) {
                    case 3:

                        //break;
                    case 4:

                        InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (mInputMethodManager != null && mContactFragment.mEtSearch != null) {
                            mInputMethodManager.hideSoftInputFromWindow(
                                    mContactFragment.mEtSearch.getWindowToken(), 0);
                        }

                        break;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }


    }

    /**
     * @param context    this is refers current object
     * @param tabText    this is used to display message
     * @param d          this is used to display drawable
     * @param currentTab
     * @return Which returns actual view
     */
    private View createTabView(Context context, String tabText, int color, int currentTab) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_custom,
                null, false);

        ImageView tv_icon = view.findViewById(R.id.tv_icon);
        LinearLayout layout = view.findViewById(R.id.tablelayout);
        Typeface text_font = Utils.getTypeface(getApplicationContext());
        tv_icon.setBackgroundResource(color);

        if (tabText != null && tabText.equalsIgnoreCase(getString(R.string.home_activity_tab_im))) {
            mUnreadCount = (TextView) view.findViewById(R.id.count);
            int count = getCount();
            mUnreadCount.setText(count + "");
            mUnreadCount.invalidate();
            if (count > 0)
                mUnreadCount.setVisibility(View.VISIBLE);
        }

        return view;
    }

    /**
     * @return this is returns integer value
     */
    private int getCount() {
        Cursor cursor = CSDataProvider.getChatCursorGroupedByRemoteId();
        int badgeCount = 0;
        if (cursor.moveToNext()) {
            do {
                String destNumber = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_LOGINID));
                int isGroupMessage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_IS_GROUP_MESSAGE));
                Cursor ccr = null;
                if (isGroupMessage == 0) {
                    ccr = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(destNumber);
                } else {
                    ccr = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(destNumber);
                }
                if (ccr.getCount() > 0) {
                    badgeCount++;
                }
                ccr.close();
            } while (cursor.moveToNext());
        }
        return badgeCount;
    }

    /**
     * This is used to handle Rest API call
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
            response = HTTPUtils.getResponseFromURLGET(url);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            processResponse(api, response);
        }
    }

    /**
     * This class checks version from play store based on that show popup for update the application.
     */

    public class AppVersionCheckAsyncTask extends AsyncTask<Void, String, String> {

        private Context mContext;
        private String mCurrentVersion;

        public AppVersionCheckAsyncTask(Context context, String currentVersion) {
            mContext = context;
            mCurrentVersion = currentVersion;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String newVersion = null;
            try {
                Document document = Jsoup.connect("https://play.google.com/store/apps/details?id=" + mContext.getPackageName() + "&hl=en")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get();
                if (document != null) {
                    Elements element = document.getElementsContainingOwnText("Current Version");
                    for (Element ele : element) {
                        if (ele.siblingElements() != null) {
                            Elements sibElemets = ele.siblingElements();
                            for (Element sibElemet : sibElemets) {
                                newVersion = sibElemet.text();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newVersion;
        }

        @Override
        protected void onPostExecute(String onlineVersion) {
            super.onPostExecute(onlineVersion);

            try {
                if (onlineVersion != null && onlineVersion.length() > 0 && mCurrentVersion != null && mCurrentVersion.length() > 0) {
                    LOG.info("AppVersionCheck", "currentVersion: " + mCurrentVersion + " , Play store version: " + onlineVersion);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param api        this is handle based on give response
     * @param returndata This is response code after api calls
     */
    private void processResponse(String api, String returndata) {

        if (api.equals(getString(R.string.dialpad_balance_api_message))) {

            try {
                JSONObject jsonObj = new JSONObject(returndata);
                String balance = "";
                JSONArray array = jsonObj.getJSONObject(getString(R.string.dialpad_response_message_key)).getJSONArray(getString(R.string.dialpad_response_message_value));
                for (int i = 0; i < array.length(); i++) {
                    balance = array.getJSONObject(i).getString(getString(R.string.home_activity_response_balance_key));
                }

                LOG.info("Balance " + balance);

                final String finalBalance = balance;
                runOnUiThread(new Runnable() {
                    public void run() {
                        String newbalance3;
                        if (finalBalance.equals("")) {
                            newbalance3 = "0";
                        } else {
                            newbalance3 = finalBalance;
                        }
                        LOG.info("ekottel balance is $" + newbalance3);

                        PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
                        pf.setPrefString(getString(R.string.bal_trans_pref_avail_message), newbalance3);
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * This is used to handle call backs for initialization, registration and network related
     */

    public class CallBackReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                LOG.info("Response=" + intent.getAction());
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
                    pf.setPrefboolean("balHit", true);
                    pf.setPrefboolean("RegistrationSuccess", false);
                    LOG.info("My NetworkError receieved:" + GlobalVariables.loginretries);
                    canCall = false;
                    showRegNotif = true;
                    boolean isCallRunning = pf.getPrefBoolean("CallRunning");
                    LOG.info("Home Call Running outside" + isCallRunning);
                    if (!isCallRunning) {
                        LOG.info("Home Call Running inside" + isCallRunning);
                        notificationManager.cancelAll();
                    }

                    if (Utils.getNetwork(getApplicationContext())) {
                        status = getString(R.string.home_activity_not_registered_message);
                    } else {
                        status = "Please Check Your Internet Connection";
                    }
                    if (mProfileFragment.mTvRegStatus != null)
                        mProfileFragment.mTvRegStatus.setText(status);
                    if (DialpadActivity.mTvDialpadRegStatus != null) {

                        DialpadActivity.mTvDialpadRegStatus.setText(status);
                        LOG.info("Dialpad Called network");
                    }

                    if (DialpadActivity.mTvBalance != null) {
                        DialpadActivity.mTvBalance.setVisibility(View.GONE);
                        LOG.info("Dialpad Called network invisible");
                    }
                    if (ProfileFragment.mTvProfileBalance != null) {
                        ProfileFragment.mTvProfileBalance.setVisibility(View.GONE);
                    }
                    reTry();
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_INITILIZATION_RESPONSE)) {
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_FAILURE)) {
                        reTry();
                    }
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        // LOG.info("Response=" + "RESULT_SUCCESS");
                        updateRegisterStatus();
                        GlobalVariables.phoneNumber = CSDataProvider.getLoginID();
                        GlobalVariables.password = CSDataProvider.getPassword();
                        GlobalVariables.loginretries = 0;
                        PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
                        if (!pf.getPrefBoolean(PreferenceProvider.IS_PASSWORD_UPDATED)) {
                            pf.setPrefboolean(PreferenceProvider.IS_PASSWORD_UPDATED, true);
                            CSClientObj.updatePassword(CSDataProvider.getPassword(), CSDataProvider.getPassword());
                        }
                    } else {
                        LOG.info("When login failed and receive 409 calling autoLogout()");
                        // When we receive response code 409 we get this response so we are redirecting to auto logout
                        autoLogout();
                    }
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_PSTN_REGISTRATION_RESPONSE)) {
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        updateRegisterStatus();
                    } else {
                        if (Utils.getNetwork(getApplicationContext())) {
                            status = getString(R.string.home_activity_not_registered_message);
                        } else {
                            status = getString(R.string.internet_error);
                        }
                        if (mProfileFragment.mTvRegStatus != null)
                            mProfileFragment.mTvRegStatus.setText(status);
                        if (DialpadActivity.mTvDialpadRegStatus != null) {

                            DialpadActivity.mTvDialpadRegStatus.setText(status);
                            LOG.info("Dialpad Called network");
                        }

                        if (DialpadActivity.mTvBalance != null) {
                            DialpadActivity.mTvBalance.setVisibility(View.INVISIBLE);
                            LOG.info("Dialpad Called network invisible");
                        }
                        if (ProfileFragment.mTvProfileBalance != null) {
                            ProfileFragment.mTvProfileBalance.setVisibility(View.INVISIBLE);
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /*private BroadcastReceiver loginSomeWhereElseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            LOG.info("CSLoginElseWhereNotification receiver called and calling autoLogout()");
            // Auto logout when user logged in another device.
            autoLogout();


        }
    };*/

    /**
     * This method log out the account with user confirmation
     */
    private void autoLogout() {
        try {
            if (mAutoLogoutDialog == null) {
                mAutoLogoutDialog = new Dialog(HomeScreenActivity.this);

                mAutoLogoutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mAutoLogoutDialog.setContentView(R.layout.dialog_auto_logout);
                mAutoLogoutDialog.setCancelable(false);
                mAutoLogoutDialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));


                Button yes = mAutoLogoutDialog
                        .findViewById(R.id.auto_logout_ok_btn);


                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //PreferenceProvider pf = new PreferenceProvider(HomeScreenActivity.this);
                        //pf.setPrefboolean(getString(R.string.splash_pref_is_already_login), false);

                        SharedPreferences sharedPreferences = context.getSharedPreferences(GlobalVariables.MyPREFERENCES,
                                Context.MODE_PRIVATE);
                        sharedPreferences.edit().clear().commit();

                        try {
                            CSClient mCSClient = new CSClient();
                            mCSClient.reset();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mAutoLogoutDialog.dismiss();
                        mAutoLogoutDialog = null;

                        Intent intent = new Intent(HomeScreenActivity.this, SignupActivity.class);
                        startActivity(intent);
                        finish();

                    }
                });
                mAutoLogoutDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This method will update the register status.
     */
    private void updateRegisterStatus() {
        PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
        pf.setPrefboolean("RegistrationSuccess", true);
        status = getString(R.string.registered);
        PreferenceProvider pf_call = new PreferenceProvider(getApplicationContext());
        boolean isCallRunning = pf_call.getPrefBoolean("CallRunning");
        if (showRegNotif && !isCallRunning) {
            showRegNotif = false;
            LOG.info("notification called and status " + status);
            //showNotification(status);
        }

        if (mProfileFragment.mTvRegStatus != null)
            mProfileFragment.mTvRegStatus.setText(getString(R.string.registered));
        if (DialpadActivity.mTvBalance != null) {
            DialpadActivity.mTvBalance.setVisibility(View.VISIBLE);
        }
        if (ProfileFragment.mTvProfileBalance != null) {
            ProfileFragment.mTvProfileBalance.setVisibility(View.VISIBLE);
        }
        if (DialpadActivity.mTvDialpadRegStatus != null)
            DialpadActivity.mTvDialpadRegStatus.setText(getString(R.string.registered));
        LOG.info("Profile Cursor Called Home");
        pf.setPrefboolean(getString(R.string.subscribe_package_buy_package_pref_my_packages), true);
        Intent registrationIntent = new Intent("com.app.ekottel.RegistrationStatus");
        sendBroadcast(registrationIntent);
    }

    public void reTry() {

        try {
            try {
                GlobalVariables.loginretries++;
                if (GlobalVariables.loginretries <= 5000) {

                    runnableObj1 = new Runnable() {
                        public void run() {

                            CSAppDetails csAppDetails = new CSAppDetails("ekottel", "iamlivedbnew"); // Tringy
                            /*  CSAppDetails  csAppDetails=new CSAppDetails("iamLive","aid_8656e0f6_c982_4485_8ca7_656780b53d34"); // Konverz*/
                            CSClientObj.initialize(GlobalVariables.server, GlobalVariables.port, csAppDetails);

                            h1.removeCallbacks(runnableObj1);
                        }
                    };

                    if (GlobalVariables.loginretries == 1) {
                        h1.postDelayed(runnableObj1, 1000);
                    } else if (GlobalVariables.loginretries <= 10) {
                        h1.postDelayed(runnableObj1, 3000);
                    } else if (GlobalVariables.loginretries == 11) {
                        h1.postDelayed(runnableObj1, 10000);
                    } else if (GlobalVariables.loginretries == 12) {
                        h1.postDelayed(runnableObj1, 15000);
                    } else if (GlobalVariables.loginretries >= 13) {
                        h1.postDelayed(runnableObj1, 30000);
                    } else {
                        h1.postDelayed(runnableObj1, 30000);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        PreferenceProvider preferenceProvider = new PreferenceProvider(getApplicationContext());
        if (preferenceProvider.getPrefBoolean("CallRunning")) {
            Toast.makeText(getApplicationContext(), "Call is in Progress. Please disconnect the call", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!doubleBackToExitPressedOnce) {
            Toast.makeText(HomeScreenActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            doubleBackToExitPressedOnce = true;
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            finish();
        }


    }

    /**
     * This is handle call states
     */
    private class ServicePhoneStateReceiver extends PhoneStateListener {
        @Override
        public void onCallStateChanged(final int state, final String incomingNumber) {
            try {
                PreferenceProvider prefereceProvider = new PreferenceProvider(getApplicationContext());
                LOG.info("Invoke onCallStateChanged in ServicePhoneStateReceiver init status=" + state);
                if (state == 2 || state == 1) {
                    prefereceProvider.setPrefboolean(getString(R.string.call_logs_pref_gsm_key), true);
                } else {
                    prefereceProvider.setPrefboolean(getString(R.string.call_logs_pref_gsm_key), false);
                }
                super.onCallStateChanged(state, incomingNumber);
            } catch (Throwable e) {
                e.printStackTrace();
                LOG.info("Invoke onCallStateChanged Exception " + e);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LOG.info("onNewIntent Method ");

        if (intent.getBooleanExtra(getString(R.string.home_activity_intent_is_missed_call_notif), false)) {
            PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
            pf.setPrefboolean(getString(R.string.home_activity_intent_is_missed_call_notif), false);
            viewPager.setCurrentItem(1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
        pf.setPrefboolean("RegisterHome", false);
        pf.setPrefboolean("OfflineUsers", false);

        if (notificationManager != null)
            notificationManager.cancelAll();

        LocalBroadcastManager.getInstance(HomeScreenActivity.this).unregisterReceiver(mSDKCallBacks);

        //unregisterReceiver(loginSomeWhereElseReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LOG.info("requestCode is" + requestCode);
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            Intent intent = new Intent("LoadContacts");
            sendBroadcast(intent);
        }
    }


}
