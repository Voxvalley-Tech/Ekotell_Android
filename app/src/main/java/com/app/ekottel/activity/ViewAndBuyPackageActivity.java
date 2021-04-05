package com.app.ekottel.activity;


import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.HTTPUtils;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.wrapper.CSDataProvider;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This activity is used to subscribe new package information.
 *
 * @author Ramesh U
 * @version 2017
 */
public class ViewAndBuyPackageActivity extends AppCompatActivity {
    private TextView mTvSubscribePackage;
    ProgressDialog progressBar;
    Handler h = new Handler();
    Runnable runnableObj;
    int delay = 30000;
    String brandPin;
    private TextView buyItBtn;
    private TextView mTvPackageName, mTvCost, mTvValidity, mTvMaxMinutes;
    static String finalNameOfPackage = "";
    static String costOfPackage1 = "";
    androidx.appcompat.app.AlertDialog.Builder successfullyLogin;
    AlertDialog dismissSuccessfullyLogin;
    private String TAG;
    NotificationManager notificationManager;
    private String packageID, packageName, packageCost, packageValidity, packageMaxMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewdetails);
        successfullyLogin = new androidx.appcompat.app.AlertDialog.Builder(this);


        try {
            TAG = getString(R.string.subscribe_package_tag_buy_package);
            String nameofpackage = "";
            brandPin = getIntent().getStringExtra(getString(R.string.subscribe_package_buy_package_intent_packageid));
            mTvPackageName = (TextView) findViewById(R.id.tv_details_package_name);
            mTvCost = (TextView) findViewById(R.id.tv_details_cost);
            mTvValidity = (TextView) findViewById(R.id.tv_details_validity);
            mTvMaxMinutes = (TextView) findViewById(R.id.tv_max_minutes);
            buyItBtn = (TextView) findViewById(R.id.tv_subscribe);

            Typeface webTypeFace = Utils.getTypeface(getApplicationContext());
            LinearLayout ll_back = (LinearLayout) findViewById(R.id.ll_subscribe_package_buy_back_arrow);
            TextView back = (TextView) findViewById(R.id.tv_subscribe_package_buy_back_arrow);
            if (ll_back != null) {
                ll_back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                back.setTypeface(webTypeFace);
            }
            mTvSubscribePackage = (TextView) findViewById(R.id.tv_subscribe_package_buy_header);
            Intent intent = getIntent();
            packageID = intent.getStringExtra("packageid");
            packageName = intent.getStringExtra("packageName");
            packageCost = intent.getStringExtra("packageCost");
            packageValidity = intent.getStringExtra("packageValidity");
            packageMaxMinutes = intent.getStringExtra("packageMaxMinutes");


            finalNameOfPackage = packageName;
            costOfPackage1 = packageCost;
            mTvPackageName.setText(packageName);
            mTvCost.setText(packageCost);
            mTvValidity.setText(packageValidity);
            mTvMaxMinutes.setText(packageMaxMinutes);

            if (!packageCost.equals("")) {
                LOG.info("costofpackage:" + costOfPackage1);
                buyItBtn.setText(getString(R.string.subscribe_package_buy_package_btn_message) + " " + "$" + costOfPackage1);
            } else {
                buyItBtn.setEnabled(false);
            }


            buyItBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {


                        String username = CSDataProvider.getLoginID();
                        username = username.replace("+", "");
                        String password1 = CSDataProvider.getPassword();
                        String username1 = CSDataProvider.getLoginID();
                        String pwd = username1 + password1;

                        LOG.debug("Password" + pwd);
                        String actualPassword = "";
                        try {
                            actualPassword = Utils.generateSHA256(pwd);
                            LOG.debug("Password after SHA" + actualPassword);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String url = Constants.MY_PACKAGES_URL + "%2B" + username + "?&password=" + actualPassword;
                        new APITask(url, getString(R.string.subscribe_package_buy_package_api_key)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    @Override
    protected void onDestroy() {
        SubscriberPackagesActivity.isSubscribePkgClick = false;
        super.onDestroy();
    }

    public boolean showAlertToSubscribe(String message, final String finalnameofpackage) {
        try {

            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alertdialog_close, null);
            successfullyLogin.setView(dialogView);
            TextView title = (TextView) dialogView.findViewById(R.id.tv_alert_title);
            title.setText(getString(R.string.subscribe_package_buy_package_alert_title));
            TextView msg = (TextView) dialogView.findViewById(R.id.tv_alert_message);
            msg.setText(message);
            Button yes = (Button) dialogView.findViewById(R.id.btn_alert_ok);
            Button no = (Button) dialogView.findViewById(R.id.btn_alert_cancel);

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (dismissSuccessfullyLogin != null) {
                            dismissSuccessfullyLogin.cancel();
                        }


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
                        JSONObject obj = new JSONObject();
                        obj.put(getString(R.string.subscribe_package_buy_package_intent_username), username1);
                        obj.put(getString(R.string.subscribe_package_buy_package_intent_pwd), password1);
                        obj.put(getString(R.string.subscribe_package_buy_package_intent_package), finalnameofpackage);
                        new PostAPITask(Constants.SUBSCRIBE_PACKAGE_URL, obj).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dismissSuccessfullyLogin != null) {
                        dismissSuccessfullyLogin.cancel();
                    }
                }
            });

            successfullyLogin.setCancelable(false);
            dismissSuccessfullyLogin = successfullyLogin.show();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

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
            showProgressbar();
        }

        @Override
        protected Void doInBackground(Void... params) {
            response = HTTPUtils.getResponseFromURLGET(url);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dismissProgressbar();
            processResponse(api, response);
        }
    }

    private void processResponse(String api, String returndata) {
        try {
            if (api.equals(getString(R.string.subscribe_package_buy_package_api_subscribe_pkg))) {
                showAlert(getString(R.string.my_packages_api_success_response));
            } else if (api.equals(getString(R.string.subscribe_package_buy_package_api_get_pkgs))) {
                try {
                    //LOG.info("getmypackages returndata:" + returndata);


                    // IAmLiveDB.deleteallmypackages();


                    JSONObject jsonObj = new JSONObject(returndata);
                    JSONArray array = jsonObj.getJSONArray(getString(R.string.http_json_response_data_key));
                    LOG.info("getmypackages length:" + array.length());

                    for (int i = 0; i < array.length(); i++) {


                        LOG.info("packagename:" + array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_pkgname)));
                        LOG.info("cost:" + array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_cost)));
                        LOG.info("expiry:" + array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_expiry)));
                        LOG.info("creationdate:" + array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_date)));


                        String id = array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_id));
                        String packagename = array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_pkgname));
                        String cost = array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_cost));
                        String expiry = array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_expiry));
                        String creationdate = array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_date));

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
                    }

                    showAlertToSubscribe(getString(R.string.subscribe_package_buy_package_btn_message) + " " + "$" + costOfPackage1 + "?", finalNameOfPackage);


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                JSONObject jsonObj = new JSONObject(returndata).getJSONObject(getString(R.string.http_json_response_error_key));
                String errormsg = jsonObj.getString(getString(R.string.http_json_response_message_key));
                showAlert(errormsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class PostAPITask extends AsyncTask<Void, Void, Void> {
        private String url;
        JSONObject api;
        String response = "";

        PostAPITask(String url, JSONObject api) {
            this.url = url;
            this.api = api;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressbar();
        }

        @Override
        protected Void doInBackground(Void... params) {
            response = HTTPUtils.getResponseFromURLPOST(url, api);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dismissProgressbar();
            JSONObject jsonObj;
            try {
                if (response.contains(getString(R.string.http_json_response_error_key))) {
                    jsonObj = new JSONObject(response).getJSONObject(getString(R.string.http_json_response_error_key));
                } else {
                    jsonObj = new JSONObject(response).getJSONObject(getString(R.string.http_json_response_data_key));
                }
                String msg = jsonObj.getString(getString(R.string.http_json_response_message_key));
                showAlert(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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

    public boolean showAlert(final String result) {
        try {
            LayoutInflater inflater = getLayoutInflater();
            successfullyLogin = new androidx.appcompat.app.AlertDialog.Builder(this);
            View dialogView = inflater.inflate(R.layout.alertinfo, null);
            successfullyLogin.setView(dialogView);
            Button yes = (Button) dialogView.findViewById(R.id.button6);
            TextView msg = (TextView) dialogView.findViewById(R.id.editText2);
            msg.setText(result);
            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PreferenceProvider pf = new PreferenceProvider(getApplicationContext());

                    pf.setPrefboolean(getString(R.string.subscribe_package_buy_package_pref_my_packages), true);
                    if (dismissSuccessfullyLogin != null) {
                        dismissSuccessfullyLogin.cancel();
                    }
                }
            });
            successfullyLogin.setCancelable(false);
            dismissSuccessfullyLogin = successfullyLogin.show();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void showProgressbar() {
        try {
            progressBar = new ProgressDialog(ViewAndBuyPackageActivity.this);
            progressBar.setCancelable(false);
            progressBar.setMessage(getString(R.string.bal_trans_wait_message));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.show();

            h = new Handler();
            runnableObj = new Runnable() {

                public void run() {
                    h.postDelayed(this, delay);

                    ViewAndBuyPackageActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            dismissProgressbar();
                            Toast.makeText(ViewAndBuyPackageActivity.this, getString(R.string.network_message), Toast.LENGTH_SHORT).show();


                        }
                    });
                }
            };
            h.postDelayed(runnableObj, delay);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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
