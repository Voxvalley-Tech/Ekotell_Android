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
import android.os.Bundle;
import android.os.Handler;
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

import java.util.Locale;
import java.util.Map;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


/**
 * This activity is used to new user register.
 *
 * @author Ramesh U
 * @version 2017
 */
public class ExistingUserOrNotActivity extends AppCompatActivity {
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
    LinearLayout ll_signup,ll_login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.user_existing_ornot_signup);
        TAG = getString(R.string.signup_tag);
        // If OS version is more than Lollipop we ask all below permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "hasPermissions: " + PermissionUtils.hasPermissions(ExistingUserOrNotActivity.this, PermissionUtils.PERMISSIONS));
            PermissionUtils.requestForAllPermission(ExistingUserOrNotActivity.this);
        }
        ll_login=findViewById(R.id.ll_login);
        ll_signup=findViewById(R.id.ll_signup);
        ll_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(ExistingUserOrNotActivity.this,SignupActivity.class);
                startActivity(intent);

            }
        });

        ll_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ExistingUserOrNotActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(ExistingUserOrNotActivity.this,PrivacyPolicyActivity.class);
        startActivity(intent);
    }
}
