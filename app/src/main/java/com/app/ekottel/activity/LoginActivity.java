package com.app.ekottel.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import android.telephony.TelephonyManager;
import android.text.method.PasswordTransformationMethod;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.app.ekottel.R;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.PermissionUtils;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSEvents;
import com.ca.dao.CSAppDetails;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;
import com.ca.wrapper.CSGroups;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;
import java.util.Map;

import static com.app.ekottel.utils.GlobalVariables.LOG;

public class LoginActivity extends AppCompatActivity {


    private EditText mEtUsername, mEtPassword;
    private LinearLayout mLlLogin, mParentLayout;
    private AppCompatCheckBox mIvShowPassword;
    boolean showPassword = false;
    private String username = "";
    private String password = "";
    private CSClient CSClientObj = new CSClient();
    private ProgressDialog progressDialog;
    private Handler h = new Handler();
    private int delay = 20000;
    private Runnable runnableObj;
    private String countryCodeNumber;
    private String countryCode;
    private final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_login);

        // If OS version is more than Lollipop we ask all below permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LOG.info("hasPermissions: " + PermissionUtils.hasPermissions(LoginActivity.this, PermissionUtils.PERMISSIONS));
            PermissionUtils.requestForAllPermission(LoginActivity.this);
        }
        mEtUsername = findViewById(R.id.et_login_username);
        mEtPassword = findViewById(R.id.et_login_password);
        mParentLayout = findViewById(R.id.parent_layout_login);

        mIvShowPassword = findViewById(R.id.showpwd_check);


        mLlLogin = findViewById(R.id.ll_login_submit);

        Typeface text_font = Utils.getTypeface(getApplicationContext());


        mParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(mParentLayout.getWindowToken(), 0);
            }
        });
        mIvShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (showPassword) {
                    mIvShowPassword.setButtonDrawable(R.drawable.ic_password_hide);
                    mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                  //  mIvShowPassword.setText(getResources().getString(R.string.login_password_hide));
                    mEtPassword.setSelection(mEtPassword.getText().length());
                    showPassword = false;
                } else {


                    mIvShowPassword.setButtonDrawable(R.drawable.ic_password_show);
                  //  mIvShowPassword.setText(getResources().getString(R.string.login_password_show));
                    mEtPassword.setTransformationMethod(null);
                    mEtPassword.setSelection(mEtPassword.getText().length());
                    showPassword = true;
                }
            }
        });


        mLlLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mEtUsername.getText().toString().equals("")) {
                        Toast.makeText(LoginActivity.this, getString(R.string.login_username_not_empty), Toast.LENGTH_SHORT).show();
                    } else if (mEtPassword.getText().toString().equals("")) {
                        Toast.makeText(LoginActivity.this, getString(R.string.login_password_not_empty), Toast.LENGTH_SHORT).show();
                    } else if (mEtUsername.getText().toString().contains("\n")) {
                        Toast.makeText(LoginActivity.this, getString(R.string.login_username_should_valid), Toast.LENGTH_SHORT).show();
                    } else if (mEtPassword.getText().toString().contains("\n")) {
                        Toast.makeText(LoginActivity.this, getString(R.string.login_password_should_valid), Toast.LENGTH_SHORT).show();
                    } else {


                        if (!Utils.getNetwork(LoginActivity.this)) {

                            Toast.makeText(LoginActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();

                            return;
                        }

                        username = mEtUsername.getText().toString();
                        password = mEtPassword.getText().toString();

                        GlobalVariables.phoneNumber = username;
                        GlobalVariables.password = password;
                        showProgressbar();

                        CSAppDetails csAppDetails = new CSAppDetails("ekottel", "iamlivedbnew"); // Tringy
                        /* CSAppDetails  csAppDetails=new CSAppDetails("iamLive","aid_8656e0f6_c982_4485_8ca7_656780b53d34"); // Konverz*/
                        CSClientObj.initialize(GlobalVariables.server, GlobalVariables.port, csAppDetails);

                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(LoginActivity.this, getString(R.string.login_failed_message), Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    /**
     * Display Progress dialog
     */
    public void showProgressbar() {
        try {
            if (LoginActivity.this != null) {
                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setMessage(getString(R.string.bal_trans_wait_message));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setProgress(0);
                progressDialog.show();

                h = new Handler();
                runnableObj = new Runnable() {

                    public void run() {
                        h.postDelayed(this, delay);

                        runOnUiThread(new Runnable() {
                            public void run() {
                                dismissProgressbar();
                                Toast.makeText(LoginActivity.this, getString(R.string.request_timeout), Toast.LENGTH_SHORT).show();


                            }
                        });
                    }
                };
                h.postDelayed(runnableObj, delay);


            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Dismiss progress dialog
     */
    public void dismissProgressbar() {
        try {
            if (progressDialog != null) {
                progressDialog.dismiss();

            }
            if (h != null) {
                h.removeCallbacks(runnableObj);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Log.i(TAG, "onReceive: "+(intent!=null? intent.getAction():""));
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    dismissProgressbar();
                    //Toast.makeText(getApplicationContext(), "NetworkError", Toast.LENGTH_SHORT).show();

                } else if (intent.getAction().equals(CSEvents.CSCLIENT_INITILIZATION_RESPONSE)) {
                    dismissProgressbar();
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {

                        String countryCode = getStatusCode(username);

                        countryCode = countryCode.replace("+", "");
                        System.out.println("Contacts Sync" + countryCode);
                        PreferenceProvider preferenceProvider = new PreferenceProvider(LoginActivity.this);
                        preferenceProvider.setPrefString(PreferenceProvider.USER_REGISTRED_COUNTRY_CODE, countryCode.replace("+", ""));

                        CSClientObj.registerForPSTNCalls();
                        CSClientObj.enableNativeContacts(true, Integer.parseInt(countryCode));
                        CSClientObj.login(username, password);

                    } else {

                        int retcode = intent.getIntExtra(CSConstants.RESULTCODE, 0);
                        if (retcode == CSConstants.E_409_NOINTERNET) {
                            Toast.makeText(getApplicationContext(), getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.initialisation_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
                    dismissProgressbar();
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {

                        PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
                        pf.setPrefboolean(getString(R.string.splash_pref_is_already_login), true);

                        CSGroups CSGroupsObj = new CSGroups();
                        CSGroupsObj.pullMyGroupsList();

                        Intent intentt = new Intent(getApplicationContext(), StatusActivity.class);
                        intentt.putExtra("isFreshLogin", true);
                        startActivityForResult(intentt, 933);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid username/password", Toast.LENGTH_SHORT).show();

                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }


    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    public void onResume() {
        super.onResume();

        try {
            GlobalVariables.loginretries = 0;
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter1 = new IntentFilter(CSEvents.CSCLIENT_SIGNUP_RESPONSE);
            IntentFilter filter2 = new IntentFilter(CSEvents.CSCLIENT_LOGIN_RESPONSE);
            IntentFilter filter3 = new IntentFilter(CSEvents.CSCLIENT_INITILIZATION_RESPONSE);

            LocalBroadcastManager.getInstance(LoginActivity.this).registerReceiver(MainActivityReceiverObj, filter);
            LocalBroadcastManager.getInstance(LoginActivity.this).registerReceiver(MainActivityReceiverObj, filter1);
            LocalBroadcastManager.getInstance(LoginActivity.this).registerReceiver(MainActivityReceiverObj, filter2);
            LocalBroadcastManager.getInstance(LoginActivity.this).registerReceiver(MainActivityReceiverObj, filter3);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    @Override
    public void onBackPressed() {

        //This is handle to navigate previous screen if requires
        Intent i = new Intent(getApplicationContext(),
                ExistingUserOrNotActivity.class);
        startActivity(i);
        finish();
    }

    private String getStatusCode(String phoneNumber) {

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            // phone must begin with '+'
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(CSDataProvider.getLoginID(), "");
            countryCode = "" + numberProto.getCountryCode();
            return "" + countryCode;
        } catch (Exception e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }

        // If above case fails we get region from telephone manager.

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String devicestatus = chkStatus();
        try {
            if (devicestatus.equals(getString(R.string.signup_network_type_wifi))) {
                countryCodeNumber = telephonyManager.getNetworkCountryIso();
            } else if (devicestatus.equals(getString(R.string.signup_network_type_data))) {
                countryCodeNumber = telephonyManager.getSimCountryIso();
            }

            if (!countryCodeNumber.isEmpty()) {
                Locale l = new Locale("", countryCodeNumber);
                countryCodeNumber = getTwoCharCountryCode(countryCodeNumber.toUpperCase());
                countryCode = countryCodeNumber;
            } else {
                countryCodeNumber = getString(R.string.signup_default_country_code);
                countryCode = countryCodeNumber;
            }

        } catch (Throwable ex) {
            countryCodeNumber = getString(R.string.signup_default_country_code);
            countryCode = countryCodeNumber;
            ex.printStackTrace();

        }
        return countryCode;
    }

    private String chkStatus() {
        String dataType = getString(R.string.signup_network_type_no_internet);
        try {

            final ConnectivityManager connMgr = (ConnectivityManager)
                    this.getSystemService(Context.CONNECTIVITY_SERVICE);
            final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifi.isConnectedOrConnecting()) {
                //Toast.makeText(this, "Wifi", Toast.LENGTH_LONG).show();
                dataType = getString(R.string.signup_network_type_wifi);
                return dataType;
            } else if (mobile.isConnectedOrConnecting()) {
                dataType = getString(R.string.signup_network_type_data);
                return dataType;
            } else {
                dataType = getString(R.string.signup_network_type_no_internet);
                return dataType;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataType;
    }


    /**
     * This is used for retrieve country code
     *
     * @param code this is used for get country code
     * @return which returns country code
     */
    private String getTwoCharCountryCode(String code) {
        for (Map.Entry<String, String> item : ZoneSelectActivity.country2Phone2.entrySet()) {
            if (item.getKey().equalsIgnoreCase(code)) {
                return item.getValue();
            }
        }
        return "";
    }

}
