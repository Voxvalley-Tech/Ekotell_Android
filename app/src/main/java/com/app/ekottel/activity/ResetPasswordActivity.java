package com.app.ekottel.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.app.ekottel.R;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSEvents;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextView mTvResetShowPwd;
    private EditText mEtResetPwd;
    private LinearLayout mLlResetDone;
    private boolean showPassword = false;
    private CSClient csClientObj;
    private ProgressDialog progressBar;
    private Handler progressBarHandler = new Handler();
    private int delayToShowProgressBar = 10000;
    private Runnable progressBarRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_reset_password);
        mTvResetShowPwd = findViewById(R.id.showpwd_check);
        mEtResetPwd = findViewById(R.id.et_new_password);
        mLlResetDone = findViewById(R.id.ll_password_done);
        csClientObj = new CSClient();
        Typeface text_font = Utils.getTypeface(getApplicationContext());
        if (mTvResetShowPwd != null)
            mTvResetShowPwd.setTypeface(text_font);
        mTvResetShowPwd.setText(getResources().getString(R.string.login_password_hide));
        mTvResetShowPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showPassword) {
                    mEtResetPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mTvResetShowPwd.setText(getResources().getString(R.string.login_password_hide));
                    mEtResetPwd.setSelection(mEtResetPwd.getText().length());
                    showPassword = false;
                } else {
                    mTvResetShowPwd.setText(getResources().getString(R.string.login_password_show));
                    mEtResetPwd.setTransformationMethod(null);
                    mEtResetPwd.setSelection(mEtResetPwd.getText().length());
                    showPassword = true;
                }
            }
        });

        mLlResetDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mEtResetPwd.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "Password  shouldn't be empty", Toast.LENGTH_SHORT).show();
                } else if (mEtResetPwd.getText().toString().trim().length() < 4) {
                    Toast.makeText(ResetPasswordActivity.this, "Password length should be more than 3 characters", Toast.LENGTH_LONG).show();
                } else {
                    if (!Utils.getNetwork(ResetPasswordActivity.this)) {
                        Toast.makeText(ResetPasswordActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!HomeScreenActivity.status.equalsIgnoreCase(getString(R.string.registered))) {
                        Toast.makeText(ResetPasswordActivity.this, getString(R.string.registered_message), Toast.LENGTH_LONG).show();
                        return;
                    }
                    showProgressbar();
                    csClientObj.updatePassword(CSDataProvider.getPassword(), mEtResetPwd.getText().toString());
                }


            }
        });


    }

    /**
     * This method will show the progressbar
     */
    public void showProgressbar() {
        try {
            progressBar = new ProgressDialog(ResetPasswordActivity.this);
            progressBar.setCancelable(false);
            progressBar.setMessage(getString(R.string.bal_trans_wait_message));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.show();

            progressBarHandler = new Handler();
            progressBarRunnable = new Runnable() {

                public void run() {
                    progressBarHandler.postDelayed(this, delayToShowProgressBar);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            dismissProgressbar();
                            Toast.makeText(ResetPasswordActivity.this, getString(R.string.bal_trans_time_out), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };
            progressBarHandler.postDelayed(progressBarRunnable, delayToShowProgressBar);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    /**
     * This is used for dismiss progress dialog
     */
    public void dismissProgressbar() {
        try {

            if (progressBar != null) {
                progressBar.dismiss();

            }
            if (progressBarHandler != null) {
                progressBarHandler.removeCallbacks(progressBarRunnable);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(ResetPasswordActivity.this).unregisterReceiver(MainActivityReceiverObj);
        MainActivityReceiverObj = new MainActivityReceiver();
        IntentFilter filter1 = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
        IntentFilter filter2 = new IntentFilter(CSEvents.CSCLIENT_UPDATEPASSWORD_RESPONSE);
        IntentFilter filter3 = new IntentFilter(CSEvents.CSCLIENT_LOGIN_RESPONSE);
        LocalBroadcastManager.getInstance(ResetPasswordActivity.this).registerReceiver(MainActivityReceiverObj, filter1);
        LocalBroadcastManager.getInstance(ResetPasswordActivity.this).registerReceiver(MainActivityReceiverObj, filter2);
        LocalBroadcastManager.getInstance(ResetPasswordActivity.this).registerReceiver(MainActivityReceiverObj, filter3);
    }


    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {


                } else if (intent.getAction().equals(CSEvents.CSCLIENT_UPDATEPASSWORD_RESPONSE)) {
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        GlobalVariables.password = mEtResetPwd.getText().toString();

                        csClientObj.login(GlobalVariables.phoneNumber, GlobalVariables.password);
                    } else {
                        Toast.makeText(getApplicationContext(), "Password Updated Failed", Toast.LENGTH_SHORT).show();
                    }

                } else if (intent.getAction().equals(CSEvents.CSCLIENT_LOGIN_RESPONSE)) {
                    dismissProgressbar();
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {

                        if(mEtResetPwd.getText().toString().length()>0) {
                            GlobalVariables.phoneNumber = CSDataProvider.getLoginID();
                            GlobalVariables.password = CSDataProvider.getPassword();
                            GlobalVariables.loginretries = 0;
                            mEtResetPwd.setText("");
                            LOG.info("Reset Password", "Username=" + GlobalVariables.phoneNumber);
                            LOG.info("Reset Password", "Password=" + GlobalVariables.password);
                            Toast.makeText(getApplicationContext(), "Password Updated", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Password Updated Failed", Toast.LENGTH_SHORT).show();
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(ResetPasswordActivity.this).unregisterReceiver(MainActivityReceiverObj);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
