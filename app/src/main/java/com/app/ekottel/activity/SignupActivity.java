package com.app.ekottel.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Map;
import java.util.Random;

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


/**
 * This activity is used to new user register.
 *
 * @author Ramesh U
 * @version 2017
 */
public class SignupActivity extends AppCompatActivity {
    private TextView mTvArrowDown, mTvMobile, mTvSignupLogin, mTvSelectCountry, mTvSignupButton;
    private EditText mEtMobileNumber;
    private LinearLayout mLlSignUp;
    private RelativeLayout mLlSelectCountry;
    private String countryCode = "";
    private String select_country_name = "";
    private String countryCodeNumber = "";
    private String phoneNumber = "";
    private String mPassword = "";
    private ProgressDialog progressDialog;
    private Handler h = new Handler();
    private int delay = 20000;
    private Runnable runnableObj;
    private ImageView mIvSignUpCountry;
    private String TAG;
    private boolean isErrorShown = false;
    private CSClient CSClientObj = new CSClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_signup);
        TAG = getString(R.string.signup_tag);
        // If OS version is more than Lollipop we ask all below permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "hasPermissions: " + PermissionUtils.hasPermissions(SignupActivity.this, PermissionUtils.PERMISSIONS));
            PermissionUtils.requestForAllPermission(SignupActivity.this);
        }
        mTvArrowDown = (TextView) findViewById(R.id.tv_signup_arrow_down);
        mTvMobile = (TextView) findViewById(R.id.tv_signup_mobile);
        mTvSignupLogin = (TextView) findViewById(R.id.tv_signup_login);
        mTvSignupButton = (TextView) findViewById(R.id.tv_signup_button);
        mTvSelectCountry = (TextView) findViewById(R.id.tv_signup_select_country);
        mTvSelectCountry.setSelected(true);
        mEtMobileNumber = (EditText) findViewById(R.id.et_signup_mobile_number);

        mIvSignUpCountry = (ImageView) findViewById(R.id.iv_signup_country);


        mIvSignUpCountry.setVisibility(View.VISIBLE);
        mLlSelectCountry = (RelativeLayout) findViewById(R.id.ll_select_country);
        mLlSignUp = (LinearLayout) findViewById(R.id.ll_signup);

        Typeface text_regular = Utils.getTypefaceRegular(getApplicationContext());
        Typeface text_medium = Utils.getTypefaceMedium(getApplicationContext());

        mTvSelectCountry.setTypeface(text_regular);
        mEtMobileNumber.setTypeface(text_regular);


        Typeface text_font = Utils.getTypeface(getApplicationContext());
        mTvArrowDown.setTypeface(text_font);
        mTvMobile.setTypeface(text_font);
        mTvSignupLogin.setTypeface(text_medium);
        mTvSignupButton.setTypeface(text_medium);

        mTvArrowDown.setText(getString(R.string.signup_arrow_down));
        mTvMobile.setText(getString(R.string.signup_mobile));

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
                String country_name = l.getDisplayCountry();

                countryCodeNumber = getTwoCharCountryCode(countryCodeNumber.toUpperCase());
                countryCode = "+" + countryCodeNumber;

                Log.i(TAG, "country code signup" + countryCode);

                mTvSelectCountry.setText(country_name + "(" + countryCode + ")");
                select_country_name = country_name;

            } else {

                countryCodeNumber = getString(R.string.signup_default_country_code);
                countryCode = "+" + countryCodeNumber;
                mTvSelectCountry.setText(getString(R.string.signup_default_country) + "(" + countryCode + ")");
                select_country_name = getString(R.string.signup_default_country);
            }

        } catch (Throwable ex) {
            countryCodeNumber = getString(R.string.signup_default_country_code);
            countryCode = "+" + countryCodeNumber;
            mTvSelectCountry.setText(getString(R.string.signup_default_country) + "(" + countryCode + ")");
            select_country_name = getString(R.string.signup_default_country);
            ex.printStackTrace();

        }
        int resorceID = getResources().getIdentifier(getString(R.string.dialpad_kodatel_message) + countryCodeNumber, getString(R.string.dialpad_drawable_message), getPackageName());
        Log.e("resorceID","resorceID---->"+resorceID);
        mIvSignUpCountry.setImageResource(resorceID);

        mEtMobileNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isErrorShown) {
                    isErrorShown = false;
                    mEtMobileNumber.setError(null);
                }
            }
        });

        mLlSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (mEtMobileNumber.getText().toString() != null && !mEtMobileNumber.getText().toString().isEmpty()) {
                    if (mEtMobileNumber.getText().toString().length() > 5) {
                        phoneNumber = Utils.validateNumberForSingup(mEtMobileNumber.getText().toString(), countryCode);

                        PreferenceProvider pf = new PreferenceProvider(getApplicationContext());


                        if (phoneNumber != null) {
                            try {
                                phoneNumber = phoneNumber.replaceAll("[^0-9+]", "");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            pf.setPrefString(getString(R.string.signup_pref_username), phoneNumber);

                            String countrycode = countryCode.replace("+", "");
                            if (countrycode != null) {
                                CSClientObj.enableNativeContacts(true, Integer.parseInt(countrycode));
                            }
                            showalert();
                        } else {
                            isErrorShown = true;
                            mEtMobileNumber.setError(getString(R.string.signup_valid_mobile_number));
                        }

                    } else {
                        isErrorShown = true;
                        mEtMobileNumber.setError(getString(R.string.signup_valid_mobile_number));
                    }

                } else {
                    isErrorShown = true;
                    mEtMobileNumber.setError(getString(R.string.signup_enter_mobile_number));
                }


            }
        });
        mTvSignupLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        mLlSelectCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), ZoneSelectActivity.class);
                startActivityForResult(intent, 999);
            }
        });


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

    /*
    This is handle country code after select country
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999) {
            if (null != data && data.getExtras().containsKey(getString(R.string.zone_intent_zone))) {
                String countryName = "";
                String CurrentString = data.getStringExtra(getString(R.string.zone_intent_zone));
                String[] separated = CurrentString.split("\\(");
                countryName = separated[0]; // this will contain "Fruit"
                select_country_name = countryName;
                countryCode = getZipCodeByName(countryName);
                mTvSelectCountry.setText(countryName != null && !countryName.isEmpty() ? countryName + "(" + countryCode + ")" : "Country");


                mIvSignUpCountry.setVisibility(View.VISIBLE);
                String country_code = countryCode;
                Log.i(TAG, "Country Code before" + country_code);

                country_code = country_code.replaceAll("[^0-9]", "");


                Log.i(TAG, "Country Code after" + country_code);

                if (countryName.contains("Canada")) {
                    country_code = "1587";
                }
                if (countryName.contains("Australia")) {
                    country_code = "61";
                }

                int resorceID = getResources().getIdentifier(getString(R.string.dialpad_kodatel_message) + country_code, getString(R.string.dialpad_drawable_message), getPackageName());

                mIvSignUpCountry.setImageResource(resorceID);

            }
        } else if (requestCode == 998 && resultCode == Activity.RESULT_OK) {
            Intent intentt = new Intent(getApplicationContext(), StatusActivity.class);
            intentt.putExtra("isFreshLogin", true);
            startActivity(intentt);
            finish();
        }
    }

    /**
     * @param countryName This is country name used to get country code
     * @return country code
     */
    public String getZipCodeByName(String countryName) {
        String CountryZipCode = "";
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[2].trim().equalsIgnoreCase(countryName.trim())) {
                CountryZipCode = g[1];
                break;
            }
        }
        return "+" + CountryZipCode;
    }
    /*
    This is used to show popup for verification code sent given number
     */

    public boolean showalert() {
        try {
            AlertDialog.Builder successfullyLogin = new AlertDialog.Builder(SignupActivity.this);
            successfullyLogin.setTitle(getString(R.string.signup_popup_confirm_title));

            successfullyLogin.setMessage(getString(R.string.signup_popup_message) + phoneNumber);

            successfullyLogin.setPositiveButton(getString(R.string.splash_network_alert_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {

                            if (dialog != null)
                                dialog.dismiss();
                            // phoneNumber = phoneNumber.replace("+", "");
                            GlobalVariables.phoneNumber = phoneNumber;
                            if (mPassword == null || mPassword.length() == 0) {
                                Random rand = new Random();
                                GlobalVariables.randomNumber = rand.nextInt(1000000);
                                mPassword = String.valueOf(GlobalVariables.randomNumber);
                                GlobalVariables.password = mPassword;

                            }

                            showprogressbar();

                            CSAppDetails csAppDetails = new CSAppDetails(GlobalVariables.SDK_APP_NAME, "iamlivedbnew"); // Tringy
                            /* CSAppDetails  csAppDetails=new CSAppDetails("iamLive","aid_8656e0f6_c982_4485_8ca7_656780b53d34"); // Konverz*/
                            CSClientObj.initialize(GlobalVariables.server, GlobalVariables.port, csAppDetails);

                        }
                    });

            successfullyLogin.setNegativeButton(getString(R.string.signup_popup_cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                        }
                    });
            successfullyLogin.show();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    /*
    Display Progress dialog
     */
    public void showprogressbar() {
        try {
            if (SignupActivity.this != null) {
                progressDialog = new ProgressDialog(SignupActivity.this);
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
                                dismissprogressbar();
                                Toast.makeText(SignupActivity.this, getString(R.string.request_timeout), Toast.LENGTH_SHORT).show();


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

    /*
    Dismiss progress dialog
     */
    public void dismissprogressbar() {
        try {
            Log.i(TAG, "dismissProgressBar");
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


    /**
     * This is handle call backs
     */
    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    //Toast.makeText(getApplicationContext(), "NetworkError", Toast.LENGTH_SHORT).show();
                    dismissprogressbar();
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_SIGNUP_RESPONSE)) {

                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        dismissprogressbar();
                        PreferenceProvider preferenceProvider = new PreferenceProvider(SignupActivity.this);
                        preferenceProvider.setPrefString(PreferenceProvider.USER_REGISTRED_COUNTRY_CODE, countryCode.replace("+", ""));
                        Intent intentt = new Intent(getApplicationContext(), PasswordActivity.class);
                        intentt.putExtra("phoneNumber", GlobalVariables.phoneNumber);
                        intentt.putExtra("region", countryCode);
                        startActivityForResult(intentt, 998);
                    } else {
                        dismissprogressbar();
                        int retcode = intent.getIntExtra("retcode", 0);
                        if (retcode == CSConstants.E_422_UNPROCESSABLE_ENTITY) {
                            Toast.makeText(SignupActivity.this, "Invalid Number", Toast.LENGTH_SHORT).show();
                        } else {
                            //Some times getting E_403_FORBIDDEN
                            Toast.makeText(SignupActivity.this, "SignUp Failure", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_INITILIZATION_RESPONSE)) {
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        Log.i(TAG, "Activation Success Values signup" + GlobalVariables.phoneNumber + " " + GlobalVariables.password);
                        CSClientObj.registerForPSTNCalls();
                        String countrycode = countryCode.replace("+", "");
                        if (countrycode != null) {
                            CSClientObj.enableNativeContacts(true, Integer.parseInt(countrycode));
                        }
                        if (!CSDataProvider.getSignUpstatus()) {
                            CSClientObj.signUp(GlobalVariables.phoneNumber, GlobalVariables.password, false);
                        }
                    } else {
                        dismissprogressbar();
                        int retcode = intent.getIntExtra(CSConstants.RESULTCODE, 0);
                        if (retcode == CSConstants.E_409_NOINTERNET) {
                            Toast.makeText(getApplicationContext(), getString(R.string.internet_error), Toast.LENGTH_SHORT).show();
                        } else {
                            // Some times we are getting E_202_OK error
                            Toast.makeText(getApplicationContext(), getString(R.string.initialisation_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    MyReceiver MyReceiverObj_signup = new MyReceiver();

    @Override
    public void onResume() {
        super.onResume();

        try {
            mEtMobileNumber.setError(null);
            MyReceiverObj_signup = new MyReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter1 = new IntentFilter(CSEvents.CSCLIENT_SIGNUP_RESPONSE);
            IntentFilter filter2 = new IntentFilter(CSEvents.CSCLIENT_INITILIZATION_RESPONSE);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MyReceiverObj_signup, filter);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MyReceiverObj_signup, filter1);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MyReceiverObj_signup, filter2);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * @param result This is string to display message
     * @return boolean variable
     */
    public boolean showalert(String result) {
        try {
            android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(SignupActivity.this);
            successfullyLogin.setMessage(result);
            successfullyLogin.setPositiveButton(getString(R.string.signup_alert_settings_button),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            if (dialog != null)
                                dialog.dismiss();
                            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                        }
                    });
            successfullyLogin.setNegativeButton(getString(R.string.splash_network_alert_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            if (dialog != null)
                                dialog.dismiss();

                        }
                    });

            successfullyLogin.show();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MyReceiverObj_signup);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

}
