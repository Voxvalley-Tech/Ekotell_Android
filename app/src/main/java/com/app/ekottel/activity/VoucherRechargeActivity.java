package com.app.ekottel.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.HTTPUtils;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.wrapper.CSDataProvider;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * This activity is used to handle voucher recharge information.
 *
 * @author Ramesh U
 * @version 2017
 */
public class VoucherRechargeActivity extends AppCompatActivity {
    private EditText mEtVoucherCode;
    private TextView mTvTitle;
    String message = "";
    ProgressDialog progressBar;
    Handler h = new Handler();
    int delay = 10000;
    Runnable runnableObj;
    AlertDialog dismissSuccessFullyLogin;
    androidx.appcompat.app.AlertDialog.Builder successFullyLogin;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_recharge);
        TAG = getString(R.string.voucher_tag);
        mEtVoucherCode = (EditText) findViewById(R.id.et_voucher_input);
        Typeface webTypeFace = Utils.getTypeface(getApplicationContext());
        TextView redeem = (TextView) findViewById(R.id.redeem);
        LinearLayout ll_back = (LinearLayout) findViewById(R.id.ll_voucher_back_arrow);
        TextView back = (TextView) findViewById(R.id.tv_voucher_back_arrow);

        mTvTitle = (TextView) findViewById(R.id.tv_title_message);

        if (ll_back != null) {
            back.setTypeface(webTypeFace);
            ll_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEtVoucherCode.getWindowToken(), 0);
                    finish();
                }
            });
        }
        redeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {
                    if (mEtVoucherCode.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.voucher_not_empty), Toast.LENGTH_SHORT).show();
                    } else if (mEtVoucherCode.getText().toString().contains("\n")) {
                        Toast.makeText(getApplicationContext(), getString(R.string.voucher_should_valid), Toast.LENGTH_SHORT).show();
                    } else {
                        if (!Utils.getNetwork(VoucherRechargeActivity.this)) {
                            Toast.makeText(VoucherRechargeActivity.this, getString(R.string.network_unavail_message), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (!HomeScreenActivity.status.equalsIgnoreCase(getString(R.string.registered))) {
                            Toast.makeText(VoucherRechargeActivity.this, getString(R.string.registered_message), Toast.LENGTH_LONG).show();
                            return;
                        }
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mEtVoucherCode.getWindowToken(), 0);
                        String username = mEtVoucherCode.getText().toString();

                        String username1 = CSDataProvider.getLoginID();
                        String password1 = CSDataProvider.getPassword();

                        String pwd = username1+password1;

                        LOG.debug(TAG,"Password"+pwd);
                        String actualPassword="";
                        try {
                            actualPassword = Utils.generateSHA256(pwd);
                            LOG.debug(TAG,"Password after SHA"+actualPassword);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        JSONObject obj = new JSONObject();
                        obj.put(getString(R.string.voucher_intent_username), username1);
                        obj.put(getString(R.string.voucher_intent_pwd), actualPassword);
                        obj.put(getString(R.string.voucher_intent_pinno), username);
                        new APITask(Constants.VOUCHER_RECHARGE_URL, obj).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    class APITask extends AsyncTask<Void, Void, Void> {
        private String url;
        JSONObject api;
        String response = "";

        APITask(String url, JSONObject api) {
            this.url = url;
            this.api = api;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showprogressbar();
        }

        @Override
        protected Void doInBackground(Void... params) {
            response = HTTPUtils.getResponseFromURLPOST(url, api);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dismissprogressbar();
            try {
                mEtVoucherCode.setText("");
                LOG.info("voucher response "+response);
                JSONObject jsonObj = new JSONObject(response);
                String message;
                try {
                    message = jsonObj.getJSONObject(getString(R.string.bal_trans_response_message_key)).getString(getString(R.string.bal_trans_response_message_value));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    message = jsonObj.getJSONObject(getString(R.string.bal_trans_response_error_message_key)).getString(getString(R.string.bal_trans_response_message_value));
                }

                LOG.info("message:" + message);
                if (!message.equals("")) {
                    showalert(message);
                } else {
                    showalert(getString(R.string.voucher_recharge_successfully));
                }
            } catch (Exception ex) {
                showalert(getString(R.string.voucher_not_recharged));
                ex.printStackTrace();
            }

        }
    }

    public boolean showalert(String result) {
        try {
            LayoutInflater inflater = getLayoutInflater();
            successFullyLogin = new androidx.appcompat.app.AlertDialog.Builder(this);
            View dialogView = inflater.inflate(R.layout.alertinfo, null);
            successFullyLogin.setView(dialogView);
            Button yes = (Button) dialogView.findViewById(R.id.button6);
            TextView msg = (TextView) dialogView.findViewById(R.id.editText2);
            msg.setText(result);
            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dismissSuccessFullyLogin != null) {
                        dismissSuccessFullyLogin.cancel();
                    }
                }
            });
            successFullyLogin.setCancelable(false);
            dismissSuccessFullyLogin = successFullyLogin.show();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    //Handle all call backs
    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                LOG.info("Yes Something receieved in RecentReceiver");
                if (intent.getAction().equals(getString(R.string.network_message))) {
                    showalert(getString(R.string.network_message), null);
                } else if (intent.getAction().equals(getString(R.string.error))) {
                    showalert(getString(R.string.error), null);
                } else if (intent.getAction().equals(getString(R.string.http_message))) {
                    LOG.info("httpreturn receieved");
                    String api = intent.getStringExtra(getString(R.string.voucher_http_api_message));
                    String response = intent.getStringExtra(getString(R.string.voucher_http_response_message));
                    String returndata = intent.getStringExtra(getString(R.string.voucher_http_data_message));
                    LOG.info("httpreturn receieved" + response);
                    if (api.equals(getString(R.string.voucher_http_api_key_message))) {

                        try {
                            JSONObject jsonObj = new JSONObject(returndata);
                            try {
                                message = jsonObj.getJSONObject(getString(R.string.voucher_http_api_data_key)).getString(getString(R.string.voucher_http_api_message_value));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                message = jsonObj.getJSONObject(getString(R.string.voucher_http_api_error_key)).getString(getString(R.string.voucher_http_api_message_value));
                            }

                            LOG.info("message:" + message);

                            if (!message.equals("")) {
                                new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    dismissprogressbar();


                                                    if (message != null && message.contains(getString(R.string.voucher_recharge_successfully))) {
                                                        showalert(message, getString(R.string.voucher_success_valid_message));
                                                    } else {
                                                        showalert(message, null);
                                                    }
                                                }
                                            });

                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }).start();


                            } else {
                                dismissprogressbar();
                                showalert(getString(R.string.voucher_not_recharged), null);
                            }
                        } catch (Exception ex) {
                            dismissprogressbar();
                            showalert(getString(R.string.voucher_not_recharged), null);
                            ex.printStackTrace();
                        }

                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This is used to update UI after balance response comes
     *
     * @param result Display message
     * @param valid  check already display
     * @return which returns boolean value
     */
    public boolean showalert(String result, final String valid) {
        try {
            final android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(VoucherRechargeActivity.this);
            successfullyLogin.setMessage(result);
            successfullyLogin.setPositiveButton(getString(R.string.bal_trans_ok_message),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (dialog != null)
                                dialog.dismiss();
                            if (valid != null) {
                                finish();
                            }
                            successfullyLogin.create().dismiss();
                            mEtVoucherCode.setText("");

                            Timer theTimer1 = new Timer();
                            theTimer1.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        PreferenceProvider pf = new PreferenceProvider(getApplicationContext());


                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    this.cancel();
                                }
                            }, ((5) * 1000));

                        }
                    });

            successfullyLogin.show();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    public void onResume() {
        super.onResume();
        try {

            IntentFilter filter = new IntentFilter(getString(R.string.network_message));
            IntentFilter filter1 = new IntentFilter(getString(R.string.error));
            IntentFilter filter2 = new IntentFilter(getString(R.string.http_message));

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter2);
            /*NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancelAll();*/
           // Utils.handleonresume();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(VoucherRechargeActivity.this).unregisterReceiver(MainActivityReceiverObj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //This is used for display progress dialog
    public void showprogressbar() {
        try {
            progressBar = new ProgressDialog(VoucherRechargeActivity.this);
            progressBar.setCancelable(false);
            progressBar.setMessage(getString(R.string.bal_trans_wait_message));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.show();

            h = new Handler();
            runnableObj = new Runnable() {

                public void run() {
                    h.postDelayed(this, delay);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            dismissprogressbar();
                            Toast.makeText(VoucherRechargeActivity.this, getString(R.string.bal_trans_time_out), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };
            h.postDelayed(runnableObj, delay);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    //This is used for dismiss progress dialog
    public void dismissprogressbar() {
        try {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
