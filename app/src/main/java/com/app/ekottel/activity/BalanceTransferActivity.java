package com.app.ekottel.activity;

import android.app.Activity;
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
import android.text.InputFilter;
import android.text.Spanned;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.HTTPUtils;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.wrapper.CSChat;
import com.ca.wrapper.CSDataProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This activity is used to transfer balance.
 *
 * @author Ramesh U
 * @version 2017
 */
public class BalanceTransferActivity extends AppCompatActivity {

    private EditText mMobileNumberET;
    private EditText mAmountET;
    private ImageView mIvAppContacts;
    private TextView mTvBalTransferHeader, mTvBalTransTitle;
    ProgressDialog progressDialog;
    Handler h = new Handler();
    int delay = 10000;
    Runnable RunnableObj;
    AlertDialog dismissSuccessfullyLogin;
    androidx.appcompat.app.AlertDialog.Builder successfullyLogin;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance_transfer);
        TAG = getString(R.string.bal_trans_tag);
        mMobileNumberET = (EditText) findViewById(R.id.et_balance_transfer_number);
        mAmountET = (EditText) findViewById(R.id.et_balance_transfer_amount);
        mIvAppContacts = (ImageView) findViewById(R.id.iv_app_contacts);
        mTvBalTransferHeader = (TextView) findViewById(R.id.tv_bal_transfer_header);
        mTvBalTransTitle = (TextView) findViewById(R.id.tv_bal_trans_title);
        Typeface webTypeFace = Utils.getTypeface(getApplicationContext());
        TextView back = (TextView) findViewById(R.id.balance_transfer_back);
        LinearLayout ll_back = (LinearLayout) findViewById(R.id.ll_balance_transfer_back);
        if (ll_back != null) {
            ll_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mMobileNumberET.getWindowToken(), 0);
                    finish();
                }
            });
        }

        TextView currency = (TextView) findViewById(R.id.tv_bal_trans_currency_icon);
        if (currency != null)
            currency.setTypeface(webTypeFace);
        TextView mobile = (TextView) findViewById(R.id.tv_bal_trans_mobile_icon);
        if (back != null)
            back.setTypeface(webTypeFace);
        if (mobile != null)
            mobile.setTypeface(webTypeFace);
        final TextView transfer = (TextView) findViewById(R.id.tv_bal_transfer);
        if (transfer != null) {
            transfer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    transfer();
                }
            });
        }


        mAmountET.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 3)});

        successfullyLogin = new androidx.appcompat.app.AlertDialog.Builder(BalanceTransferActivity.this);

        mIvAppContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), AppContactsActivity.class);
                intent.putExtra(getString(R.string.bal_trans_pref_balance), true);
                startActivityForResult(intent, 999);
            }
        });
    }

    public class DecimalDigitsInputFilter implements InputFilter {

        Pattern mPattern;

        public DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
            mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            Matcher matcher = mPattern.matcher(dest);
            if (!matcher.matches())
                return "";
            return null;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 999) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra(getString(R.string.bal_trans_pref_app_compat));

                mMobileNumberET.setText(result);
                mMobileNumberET.setSelection(mMobileNumberET.getText().toString().length());

            }

        }
    }


    /**
     * This is handle request for transfer mAmountET
     */

    private void transfer() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mMobileNumberET.getWindowToken(), 0);
            if (mMobileNumberET.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.bal_trans_mobile_not_empty), Toast.LENGTH_SHORT).show();
            } else if (mAmountET.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.bal_trans_credits_not_empty), Toast.LENGTH_SHORT).show();
            } else if (mMobileNumberET.getText().toString().contains("\n")) {
                Toast.makeText(getApplicationContext(), getString(R.string.bal_trans_mobile_valid), Toast.LENGTH_SHORT).show();
            } else if (mAmountET.getText().toString().contains("\n")) {
                Toast.makeText(getApplicationContext(), getString(R.string.bal_trans_credits_valid), Toast.LENGTH_SHORT).show();
            } else if (Float.parseFloat(mAmountET.getText().toString()) <= 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.bal_trans_credits_valid), Toast.LENGTH_SHORT).show();
            } else {

                if (!Utils.getNetwork(BalanceTransferActivity.this)) {
                    Toast.makeText(BalanceTransferActivity.this, getString(R.string.bal_trans_network_unavail), Toast.LENGTH_LONG).show();
                    return;
                }

                if (!HomeScreenActivity.status.equalsIgnoreCase(getString(R.string.registered))) {

                    Toast.makeText(BalanceTransferActivity.this, getString(R.string.registered_message), Toast.LENGTH_LONG).show();
                    return;
                }
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                String username = mMobileNumberET.getText().toString();
                String password = mAmountET.getText().toString();

               /* if (username != null && !username.isEmpty() && !username.startsWith("+")) {
                    username = "+" + username;
                }*/


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
                obj.put("username", username1);
                obj.put("password", actualPassword);
                obj.put("tousername", username);
                obj.put("credit", password);
                new APITask(Constants.BALANCE_TRANSFER_URL, obj).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This is class gets the balance transfer information from server.
     */
    class APITask extends AsyncTask<Void, Void, Void> {
        private String url;
        String response = "";
        JSONObject api;

        APITask(String url, JSONObject api) {
            this.url = url;
            this.api = api;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar();
        }

        @Override
        protected Void doInBackground(Void... params) {
            response = HTTPUtils.getResponseFromURLPOST(url, api);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dismissProgressBar();
            try {
                //LOG.info("Balance tranfer response " + response);
                JSONArray jsonArray = new JSONArray(response);
                JSONObject jsonObj = jsonArray.getJSONObject(0);
                String message;
                try {
                    message = jsonObj.getJSONObject(getString(R.string.bal_trans_response_message_key)).getString(getString(R.string.bal_trans_response_message_value));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    message = jsonObj.getJSONObject(getString(R.string.bal_trans_response_error_message_key)).getString(getString(R.string.bal_trans_response_message_value));

                }
                LOG.info("message:" + message);
                if (message.contains(getString(R.string.bal_trans_success)) && mMobileNumberET.getText().toString() != null) {
                    PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
                    pf.setPrefString("activeDestination", mMobileNumberET.getText().toString());
                    //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(getApplicationContext().getString(R.string.balance_transfered_successful)));


                    CSChat csChat=new CSChat();
                    //csChat.sendMessage(mMobileNumberET.getText().toString(), "I have transferred" + " " + mAmountET.getText().toString() + " $" + " " + "to your account", false,"","");
                    showAlert(getString(R.string.bal_trans_success_message1) + " " + mMobileNumberET.getText().toString() + " " + getString(R.string.bal_trans_success_message2) + " " + mAmountET.getText().toString() + " $");
                    mMobileNumberET.setText("");
                    mAmountET.setText("");

                } else {
                    showAlert(message);
                }
            } catch (Exception e) {
                showAlert(getString(R.string.balance_transfer_failed));
                e.printStackTrace();
            }
        }
    }

    /**
     * This is handle call backs for network related and balance transfer
     */
    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                LOG.info("Yes Something receieved in RecentReceiver");
                if (intent.getAction().equals(getString(R.string.network_message))) {
                    dismissProgressBar();
                    showAlert(getString(R.string.network_message), null);
                } else if (intent.getAction().equals(getString(R.string.error))) {
                    dismissProgressBar();
                    showAlert(getString(R.string.error), null);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                dismissProgressBar();
                showAlert(getString(R.string.error), null);
            }
        }
    }

    /**
     * @param result this is a message for showing alert dialog
     * @return which returns boolean value
     */
    public boolean showAlert(String result) {
        try {
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alertinfo, null);
            successfullyLogin.setView(dialogView);
            Button yes = (Button) dialogView.findViewById(R.id.button6);
            TextView msg = (TextView) dialogView.findViewById(R.id.editText2);
            msg.setText(result);

            yes.setOnClickListener(new View.OnClickListener() {
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

    /**
     * @param result this is a message after getting response
     * @param valid  this is a message to handle valid response
     * @return which returns boolean value
     */
    public boolean showAlert(String result, final String valid) {
        try {
            final android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(BalanceTransferActivity.this);
            successfullyLogin.setMessage(result);
            successfullyLogin.setPositiveButton(getString(R.string.bal_trans_ok_message),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog,
                                            int which) {


                            mMobileNumberET.setText("");
                            mAmountET.setText("");

                            Timer theTimer1 = new Timer();
                            theTimer1.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        PreferenceProvider pf = new PreferenceProvider(getApplicationContext());

                                        if (dialog != null)
                                            dialog.dismiss();

                                        if (valid != null) {
                                            finish();
                                        }


                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    this.cancel();
                                }
                            }, ((4) * 1000));
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

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(BalanceTransferActivity.this).unregisterReceiver(MainActivityReceiverObj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * This is display progress dialog
     */
    public void showProgressBar() {
        try {
            progressDialog = new ProgressDialog(BalanceTransferActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.bal_trans_wait_message));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgress(0);
            progressDialog.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This is dismiss progress dialog
     */
    public void dismissProgressBar() {
        try {

            if (progressDialog != null) {
                progressDialog.dismiss();

            }
            if (h != null) {
                h.removeCallbacks(RunnableObj);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String username = CSDataProvider.getLoginID();
        PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
        pf.setPrefString("activeDestination", username);
        if (progressDialog != null)
            progressDialog.dismiss();
    }
}
