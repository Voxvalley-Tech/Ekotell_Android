package com.app.ekottel.activity;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.utils.Utils;

/**
 * This activity is used to display my packages information.
 *
 * @author Ramesh U
 * @version 2017
 */
public class ViewMyPackageActivity extends AppCompatActivity {
    private TextView mTvSubscribePackage;
    ProgressDialog progressBar;
    Handler h = new Handler();
    Runnable runnableObj;
    int delay = 30000;
    String brandPin;
    TextView mTvPackageName, mTvCost, mTvExpiry, mTvRemMin;
    private String TAG;
    private String packageID, packageName, packageCost, packageExpiry, packageRemMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewmypackagedetails);

        try {
            TAG = getString(R.string.my_packages_tag);

            Typeface webTypeFace = Utils.getTypeface(getApplicationContext());
            LinearLayout ll_back = (LinearLayout) findViewById(R.id.ll_mypackages_back_arrow);
            TextView back = (TextView) findViewById(R.id.tv_mypackages_back_arrow);
            if (ll_back != null) {
                ll_back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                back.setTypeface(webTypeFace);
            }
            mTvSubscribePackage = (TextView) findViewById(R.id.tv_my_package_header);


            brandPin = getIntent().getStringExtra(getString(R.string.my_packages_package_id));
            mTvPackageName = (TextView) findViewById(R.id.tv_my_package_details_package_name);
            mTvCost = (TextView) findViewById(R.id.tv_my_package_details_cost);
            mTvExpiry = (TextView) findViewById(R.id.tv_my_package_details_expiry);
            mTvRemMin = (TextView) findViewById(R.id.tv_my_package_details_remaining_min);

            Intent intent = getIntent();
            packageID = intent.getStringExtra("packageid");
            packageName = intent.getStringExtra("packageName");
            packageCost = intent.getStringExtra("packageCost");
            packageExpiry = intent.getStringExtra("packageExpiry");
            packageRemMinutes = intent.getStringExtra("packageRemMinutes");

            mTvPackageName.setText(packageName);
            mTvCost.setText(packageCost);
            mTvExpiry.setText(packageExpiry);
            mTvRemMin.setText(packageRemMinutes);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Update UI
     *
     * @param str update actual message
     */
    public void updateUI(String str) {

        try {

            if (str.equals(getString(R.string.network_message))) {
                LOG.info("NetworkError receieved");

                dismissProgressbar();

                showAlert(getString(R.string.network_message));


            } else if (str.equals(getString(R.string.error))) {
                LOG.info("Error receieved");
                dismissProgressbar();
                showAlert(getString(R.string.error));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    //Handle after valid response comes to this dialog
    public boolean showAlert(final String result) {
        try {
            android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(ViewMyPackageActivity.this);
            successfullyLogin.setTitle(result);
            successfullyLogin.setPositiveButton(getString(R.string.status_alert_btn_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            if (dialog != null)
                                dialog.dismiss();
                            if (result.equals(getString(R.string.my_packages_api_success_response))) {

                                finish();
                            }
                        }
                    });


            successfullyLogin.show();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    //Display progress dialog
    public void showProgressbar() {
        try {
            progressBar = new ProgressDialog(ViewMyPackageActivity.this);
            progressBar.setCancelable(false);
            progressBar.setMessage(getString(R.string.bal_trans_wait_message));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.show();

            h = new Handler();
            runnableObj = new Runnable() {

                public void run() {
                    h.postDelayed(this, delay);

                    ViewMyPackageActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            dismissProgressbar();
                            Toast.makeText(ViewMyPackageActivity.this, getString(R.string.network_message), Toast.LENGTH_SHORT).show();


                        }
                    });
                }
            };
            h.postDelayed(runnableObj, delay);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //Dismiss progress dialog
    public void dismissProgressbar() {
        try {
            LOG.info("dismissProgressBar");
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
}
