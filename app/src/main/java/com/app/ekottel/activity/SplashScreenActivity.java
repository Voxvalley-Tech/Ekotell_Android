package com.app.ekottel.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.utils.PreferenceProvider;

import static com.app.ekottel.utils.GlobalVariables.LOG;


/**
 * This activity is used to display default launcher.
 *
 * @author Ramesh U
 * @version 2017
 */
public class SplashScreenActivity extends AppCompatActivity {
    private final String TAG = "SplashScreenActivity";
    private TextView mTvVersion;
    private String mVersionName = "";
    private int SPLASH_TIME_OUT = 1000;
    private PreferenceProvider mPreferenceProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }

        //android O fix bug orientation
        if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash_screen);


        mTvVersion = findViewById(R.id.tv_splash_version);

        mPreferenceProvider = new PreferenceProvider(getApplicationContext());

        mPreferenceProvider.setPrefString("selectCountryCode", "");
        mPreferenceProvider.setPrefString("selectCountryName", "");
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(
                    getPackageName(), 0);

            mVersionName = pinfo.versionName;

        } catch (Exception e) {
            e.printStackTrace();
            mVersionName = getString(R.string.splash_version_number);

        }

        mTvVersion.setText(getString(R.string.splash_version_number_single) + mVersionName);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                  showNextScreen();


            }
        }, 1000);



      /*  CoreApis ca=new CoreApis();

        System.out.println("SDK Version" + ca.getVersion());*/

        /*new Handler().postDelayed(new Runnable() {


         *//* * Showing splash screen with a timer. This will be useful when you
         * want to show case your app logo / company*//*


            @Override
            public void run() {
                showNextScreen();
            }
        }, SPLASH_TIME_OUT);*/




    }


// This method is used to navigate next screen

    private void showNextScreen() {


        Intent i;
        try {

            boolean register = mPreferenceProvider.getPrefBoolean(getString(R.string.splash_pref_is_already_login));

            if (register) {
                i = new Intent(getApplicationContext(), HomeScreenActivity.class);

                Intent intent = getIntent();

                if (intent != null) {
                    String str = intent.getStringExtra("Notification");
                    String channelid = intent.getStringExtra("Sender");

                    LOG.info("Notification Testing Splash");

                    if (str != null && !str.isEmpty() && channelid != null) {
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.putExtra("Sender", channelid);
                        i.putExtra("Notification", "Notification");
                    }
                }

                Intent intent_promotional_notification = getIntent();

                if (intent_promotional_notification != null) {
                    String str = intent_promotional_notification.getStringExtra("PromotionalNotification");

                    LOG.info("Notification Testing Splash");

                    if (str != null && !str.isEmpty()) {
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.putExtra("PromotionalNotification", "PromotionalNotification");
                    }
                }

                startActivity(i);
                finish();
            } else {
                i = new Intent(getApplicationContext(), PrivacyPolicyActivity.class);

                startActivity(i);
                finish();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        String locale;
        locale = getResources().getConfiguration().locale.getCountry();
        LOG.info("Country code" + locale);
    }

}
