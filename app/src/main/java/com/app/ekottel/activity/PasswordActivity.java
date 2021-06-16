package com.app.ekottel.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSEvents;
import com.ca.dao.CSAppDetails;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;
import com.ca.wrapper.CSGroups;

/**
 * This activity is used to display after receive otp to read.
 *
 * @author Ramesh U
 * @version 2017
 */
public class PasswordActivity extends AppCompatActivity {

    private TextView mTvTimer, mTvResendCode, mTvSetShowPwd;
    private LinearLayout mllResendPassword;
    public static EditText mEtOneTimePassword, mEtSetPassword;
    private LinearLayout mllNext,mParentLayout;
    // The primary interface we will using for the IM service
    final Handler h1 = new Handler();
    private ProgressDialog progressBar;
    private Handler h = new Handler();
    private int delay = 20000;
    private Runnable runnableObj;
    private Context context;
    private String TAG;
    private CSClient CSClientObj = new CSClient();
    private boolean showPassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        TAG = getString(R.string.password_activity_tag);
        context = this;
        mTvTimer = findViewById(R.id.tv_timer);
        mTvResendCode = findViewById(R.id.tv_resend_password);
        mllNext = findViewById(R.id.ll_next);
        mllResendPassword = findViewById(R.id.ll_resend_password);
        mllResendPassword.setVisibility(View.GONE);
        mEtOneTimePassword = findViewById(R.id.et_one_time_password);
        mEtSetPassword = findViewById(R.id.et_set_password);
        mTvSetShowPwd = findViewById(R.id.showpwd_check);
        mParentLayout=findViewById(R.id.parent_layout_activation);
        startTimer();
        Typeface text_font = Utils.getTypeface(getApplicationContext());
        if (mTvSetShowPwd != null)
            mTvSetShowPwd.setTypeface(text_font);
        mTvSetShowPwd.setText(getResources().getString(R.string.login_password_hide));


        mllNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!mEtOneTimePassword.getText().toString().isEmpty()) {
                    showProgressBar();
                    CSClientObj.activate(getIntent().getStringExtra("phoneNumber"), mEtOneTimePassword.getText().toString());


                } else {
                    mEtOneTimePassword.setError(getString(R.string.password_activity_enter_valid_number));
                }
            }
        });
        mParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(mParentLayout.getWindowToken(), 0);
            }
        });
        mTvResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBar();
                    CSAppDetails csAppDetails = new CSAppDetails( "Ekottel","iamlivedbnew"); // Tringy
                /*CSAppDetails  csAppDetails=new CSAppDetails("iamLive","aid_8656e0f6_c982_4485_8ca7_656780b53d34"); // Konverz*/
                CSClientObj.initialize(GlobalVariables.server, GlobalVariables.port, csAppDetails);
            }
        });
        mTvSetShowPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (showPassword) {
                    mEtSetPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mTvSetShowPwd.setText(getResources().getString(R.string.login_password_hide));
                    mEtSetPassword.setSelection(mEtSetPassword.getText().length());
                    showPassword = false;
                } else {


                    mTvSetShowPwd.setText(getResources().getString(R.string.login_password_show));
                    mEtSetPassword.setTransformationMethod(null);
                    mEtSetPassword.setSelection(mEtSetPassword.getText().length());
                    showPassword = true;
                }
            }
        });

    }


    //This method is used for display progress dialog
    public void showProgressBar() {
        try {
            progressBar = new ProgressDialog(PasswordActivity.this);
            progressBar.setCancelable(false);
            progressBar.setMessage(getString(R.string.bal_trans_wait_message));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.show();
/*

            h = new Handler();
            runnableObj = new Runnable() {

                public void run() {
                    h.postDelayed(this, delay);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            dismissProgressbar();
                            Toast.makeText(PasswordActivity.this, getString(R.string.request_timeout), Toast.LENGTH_SHORT).show();


                        }
                    });
                }
            };
            h.postDelayed(runnableObj, delay);
*/


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //This method is used for handle mTimerTv task
    public void startTimer() {
        CountDownTimer cT = new CountDownTimer(120000, 1000) {

            public void onTick(long millisUntilFinished) {


                String v = String.format("%02d", millisUntilFinished / 60000);
                int va = (int) ((millisUntilFinished % 60000) / 1000);
                mTvTimer.setText(v + ":" + String.format("%02d", va));
            }

            public void onFinish() {

                mllResendPassword.setVisibility(View.VISIBLE);
                mTvTimer.setVisibility(View.GONE);

            }
        };
        cT.start();
    }

    /**
     * This method is used for dismiss progress dialog
     */
    public void dismissProgressbar() {
        try {
            Log.i(TAG, "dismissProgressBar");
            if (progressBar != null) {
                progressBar.dismiss();

            }
            if (h != null) {
                h.removeCallbacks(runnableObj);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Handle all call backs whatever comes
     */

    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                Log.i(TAG, "Yes Something receieved in RecentReceiver " + intent.getAction());
                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    dismissProgressbar();
                    //Toast.makeText(getApplicationContext(), "NetworkError", Toast.LENGTH_SHORT).show();

                } else if (intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
                    Log.i(TAG, "Activation Login result" + intent.getStringExtra(CSConstants.RESULT));
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        if (mEtSetPassword.getText().toString().isEmpty()) {

                            dismissProgressbar();

                            PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
                            pf.setPrefboolean(PreferenceProvider.IS_PASSWORD_UPDATED, true);
                            pf.setPrefboolean(getString(R.string.splash_pref_is_already_login), true);
                            CSGroups CSGroupsObj = new CSGroups();
                            CSGroupsObj.pullMyGroupsList();
                            closeActivity();
                        } else {
                            CSClientObj.updatePassword(CSDataProvider.getPassword(), mEtSetPassword.getText().toString());
                        }
                    } else {
                        dismissProgressbar();
                        Toast.makeText(getApplicationContext(), getString(R.string.request_timeout), Toast.LENGTH_SHORT).show();
                    }
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_ACTIVATION_RESPONSE)) {
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        String region = getIntent().getStringExtra("region");
                        Log.i(TAG, "Activation Success");
                        if (region == null || region.equals("")) {
                            region = "+91";
                        }
                        Log.i(TAG, "Activation Success Values" + GlobalVariables.phoneNumber + " " + GlobalVariables.password);
                        CSClientObj.login(GlobalVariables.phoneNumber, GlobalVariables.password);
                    } else {
                        Log.i(TAG, "Activation Failure");
                        dismissProgressbar();
                        Toast.makeText(getApplicationContext(), "Wrong Code.", Toast.LENGTH_SHORT).show();
                    }

                } else if (intent.getAction().equals(CSEvents.CSCLIENT_SIGNUP_RESPONSE)) {
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        dismissProgressbar();
                    } else {
                        dismissProgressbar();
                        Toast.makeText(PasswordActivity.this, "SignUp failure", Toast.LENGTH_SHORT).show();

                    }
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_INITILIZATION_RESPONSE)) {
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        if (!CSDataProvider.getSignUpstatus()) {
                            CSClientObj.signUp(GlobalVariables.phoneNumber, GlobalVariables.password, false);
                            mllResendPassword.setVisibility(View.GONE);
                            mTvTimer.setVisibility(View.VISIBLE);
                            startTimer();
                        }
                    } else {
                        dismissProgressbar();
                        Toast.makeText(getApplicationContext(), getString(R.string.initialisation_failed), Toast.LENGTH_SHORT).show();
                    }
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_UPDATEPASSWORD_RESPONSE)) {

                    GlobalVariables.password = mEtSetPassword.getText().toString();

                    dismissProgressbar();
                    PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
                    pf.setPrefboolean(PreferenceProvider.IS_PASSWORD_UPDATED, true);
                    pf.setPrefboolean(getString(R.string.splash_pref_is_already_login), true);
                    CSGroups CSGroupsObj = new CSGroups();
                    CSGroupsObj.pullMyGroupsList();
                    closeActivity();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void closeActivity() {
        Intent intentToClose = new Intent();
        setResult(Activity.RESULT_OK, intentToClose);
        finish();
    }

    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    public void onResume() {
        super.onResume();

        try {

            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter1 = new IntentFilter(CSEvents.CSCLIENT_LOGIN_RESPONSE);
            IntentFilter filter2 = new IntentFilter(CSEvents.CSCLIENT_ACTIVATION_RESPONSE);
            IntentFilter filter3 = new IntentFilter(CSEvents.CSCLIENT_SIGNUP_RESPONSE);
            IntentFilter filter4 = new IntentFilter(CSEvents.CSCLIENT_INITILIZATION_RESPONSE);
            IntentFilter filter5 = new IntentFilter(CSEvents.CSCLIENT_UPDATEPASSWORD_RESPONSE);
            LocalBroadcastManager.getInstance(PasswordActivity.this).registerReceiver(MainActivityReceiverObj, filter);
            LocalBroadcastManager.getInstance(PasswordActivity.this).registerReceiver(MainActivityReceiverObj, filter1);
            LocalBroadcastManager.getInstance(PasswordActivity.this).registerReceiver(MainActivityReceiverObj, filter2);
            LocalBroadcastManager.getInstance(PasswordActivity.this).registerReceiver(MainActivityReceiverObj, filter3);
            LocalBroadcastManager.getInstance(PasswordActivity.this).registerReceiver(MainActivityReceiverObj, filter4);
            LocalBroadcastManager.getInstance(PasswordActivity.this).registerReceiver(MainActivityReceiverObj, filter5);
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
    public void onDestroy() {
        super.onDestroy();

    }


    /**
     * This is handle to open settings using alert dialog
     *
     * @param result display actual message
     * @return which returns boolean value
     */
    public boolean showalert(String result) {
        try {
            android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(PasswordActivity.this);
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
    public void onBackPressed() {

        try {
            Log.i(TAG,"resetting SDK DB");
            CSClientObj.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //This is handle to navigate previous screen if requires
        finish();
    }
}
