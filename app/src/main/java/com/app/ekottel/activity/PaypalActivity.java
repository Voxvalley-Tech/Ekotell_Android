package com.app.ekottel.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.HTTPUtils;
import com.app.ekottel.utils.Utils;
import com.ca.wrapper.CSDataProvider;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalProfileSharingActivity;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * THIS FILE IS OVERWRITTEN BY `androidSDK/src/<general|partner>sampleAppJava.
 * ANY UPDATES TO THIS FILE WILL BE REMOVED IN RELEASES.
 * <p>
 * Basic sample using the SDK to make a payment or consent to future payments.
 * <p>
 * For sample mobile backend interactions, see
 * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
 */

/**
 * This activity is used to display top up information.
 *
 * @author Ramesh U
 * @version 2017
 */

public class PaypalActivity extends Activity {
    private static final String TAG = "paymentExample";
    /**
     * - Set to PayPalConfiguration.ENVIRONMENT_PRODUCTION to move real money.
     * <p>
     * - Set to PayPalConfiguration.ENVIRONMENT_SANDBOX to use your test credentials
     * from https://developer.paypal.com
     * <p>
     * - Set to PayPalConfiguration.ENVIRONMENT_NO_NETWORK to kick the tires
     * without communicating to PayPal's servers.
     */
  // private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;

    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
    private static final int REQUEST_CODE_PROFILE_SHARING = 3;
    String final_amount = "";

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(GlobalVariables.CONFIG_CLIENT_ID)
            // The following are only used in PayPalFuturePaymentActivity.
            .merchantName(Constants.PAYPAL_MERCHANT_NAME)
            .merchantPrivacyPolicyUri(Uri.parse(Constants.PAYPAL_PRIVACY_POLICY_URL))
            .merchantUserAgreementUri(Uri.parse(Constants.PAYPAL_LEGAL_URL));




    private TextView mEtAmount;
    private EditText edit_amount;
    Typeface textRegular;
    Typeface textLight;
    Typeface textMedium;
    private TextView mTvTopupHeader;

    Handler h = new Handler();
    int delay = 80000;
    Runnable runnableObj;
    ProgressDialog progressBar;
    AlertDialog.Builder successfullyLogin;
    AlertDialog dismissSuccessFullyLogin;
    private String username = "";
    private String TAG1;
    private ImageView img_payement_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topup);
        TAG1 = getString(R.string.paypal_tag);
        //this is common layout for both in_app and paypall hiding the inapp amount selection layout
        //findViewById(R.id.lyt_in_app_amount_selection).setVisibility(View.GONE);
        Log.e("config client","onfig client id:--"+GlobalVariables.CONFIG_CLIENT_ID);
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        username = CSDataProvider.getLoginID();
        username = username.replace("+", "");


        img_payement_icon = findViewById(R.id.img_payment_mode_icon);
        img_payement_icon.setImageResource(R.mipmap.paypal_text_icon);



        mEtAmount = findViewById(R.id.et_amount);
        edit_amount = findViewById(R.id.edit_amount);
        ImageView tv_status_arrow_down=(ImageView)findViewById(R.id.tv_status_arrow_down);
        mTvTopupHeader = (TextView) findViewById(R.id.tv_topup_header);
        mEtAmount.setVisibility(View.GONE);
        edit_amount.setVisibility(View.VISIBLE);
        tv_status_arrow_down.setVisibility(View.GONE);
        Typeface webTypeFace = Utils.getTypeface(getApplicationContext());
        LinearLayout ll_back = (LinearLayout) findViewById(R.id.ll_paypal_back);
        ImageView back = findViewById(R.id.back);


        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edit_amount.getWindowToken(), 0);
                finish();
            }
        });
        textRegular = Utils.getTypefaceRegular(getApplicationContext());
        textMedium = Utils.getTypefaceMedium(getApplicationContext());
        mTvTopupHeader.setTypeface(textRegular);
        mTvTopupHeader.setText(R.string.pay_tag);
        mEtAmount.setTypeface(textRegular);

        final TextView transfer = (TextView) findViewById(R.id.transfer);
        transfer.setTypeface(textMedium);

        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBuyPressed(v);
            }
        });
        ImageView currency =  findViewById(R.id.currency_icon1);
        //currency.setTypeface(webTypeFace);
        //back.setTypeface(webTypeFace);

    }

    public void onBuyPressed(View pressed) {
        /*
         * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
         * Change PAYMENT_INTENT_SALE to
         *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
         *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
         *     later via calls from your server.
         *
         * Also, to include additional payment details and an item list, see getStuffToBuy() below.
         */
        try {
            final_amount = edit_amount.getText().toString().trim();
            if (final_amount.equals("")) {
                Toast.makeText(PaypalActivity.this, getString(R.string.paypal_enter_amount), Toast.LENGTH_SHORT).show();
                return;
            }
            if (Float.parseFloat(final_amount) <= 0) {
                Toast.makeText(PaypalActivity.this, getString(R.string.paypal_valid_amount), Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(PaypalActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
            return;
        }



        PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(PaypalActivity.this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
        startActivityForResult(intent, REQUEST_CODE_PAYMENT);


    }

    private PayPalPayment getThingToBuy(String paymentIntent) {
        Log.i(TAG1, "final_amount:" + final_amount);
        return new PayPalPayment(new BigDecimal(final_amount), getString(R.string.paypal_usd), getString(R.string.paypal_my_package),
                paymentIntent);
    }


    protected void displayResultText(String result) {
        Toast.makeText(
                getApplicationContext(),
                result, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));

                        String returndata = confirm.toJSONObject().toString(4);

                        Log.i(TAG1, "returndata" + returndata);

                        JSONObject jsonObj = new JSONObject(returndata);
                        //JSONArray array = jsonObj.getJSONArray("data");
                        String message;
                        try {
                            message = jsonObj.getJSONObject(getString(R.string.paypal_json_key_response)).getString(getString(R.string.paypal_json_key_id));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            message = "";
                        }

                        Log.i(TAG1, "payment id" + message);

                        //IAmLiveDB.insertPaymentDetails(getString(R.string.paypal_db_value_paypal), Constants.PAYPAL_DB_PAYMENT, message, "0");


                        String url = Constants.PAYPAL_VERIFYPAYMENT_URL + "paymentid=" + message + "&username=%2B" + username;
                        new APITask(url, getString(R.string.paypal_api_value)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Exception occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        TAG,
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        } else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PayPalAuthorization auth =
                        data.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
                if (auth != null) {
                    try {
                        Log.i("FuturePaymentExample", auth.toJSONObject().toString(4));

                        String authorization_code = auth.getAuthorizationCode();
                        Log.i("FuturePaymentExample", authorization_code);

                        sendAuthorizationToServer(auth);
                        displayResultText(getString(R.string.paypal_alert_message));

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("FuturePaymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("FuturePaymentExample", "The user canceled.");
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        "FuturePaymentExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
            }
        } else if (requestCode == REQUEST_CODE_PROFILE_SHARING) {
            if (resultCode == Activity.RESULT_OK) {
                PayPalAuthorization auth =
                        data.getParcelableExtra(PayPalProfileSharingActivity.EXTRA_RESULT_AUTHORIZATION);
                if (auth != null) {
                    try {
                        Log.i("ProfileSharingExample", auth.toJSONObject().toString(4));

                        String authorization_code = auth.getAuthorizationCode();
                        Log.i("ProfileSharingExample", authorization_code);

                        sendAuthorizationToServer(auth);
                        displayResultText(getString(R.string.paypal_alert_profile_sharing_message));

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("ProfileSharingExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("ProfileSharingExample", "The user canceled.");
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        "ProfileSharingExample",
                        "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
            }
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
            showprogressbar();
        }

        @Override
        protected Void doInBackground(Void... params) {
            response = HTTPUtils.getResponseFromURLGET(url);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dismissprogressbar();
            String[] strs = response.split("\"");
            String tranxid = strs[1];
            Log.i(TAG1, "tranxid to be removed:" + tranxid);
            showalert("Success");
        }
    }

    public void showprogressbar() {
        try {
            progressBar = new ProgressDialog(PaypalActivity.this);
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
                            Toast.makeText(PaypalActivity.this, getString(R.string.network_message), Toast.LENGTH_SHORT).show();


                        }
                    });
                }
            };
            h.postDelayed(runnableObj, delay);


        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    public void dismissprogressbar() {
        try {
            Log.i(TAG1, "dismissProgressBar");
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

    private void sendAuthorizationToServer(PayPalAuthorization authorization) {

        /**
         *
         * exchange the authorization code for OAuth access and refresh tokens.
         *
         * Your server must then store these tokens, so that your server code
         * can execute payments for this user in the future.
         *
         * A more complete example that includes the required app-server to
         * PayPal-server integration is available from
         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
         */

    }


    @Override
    public void onDestroy() {
        // Stop service when done
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    public boolean showalert(String result) {
        try {
            LayoutInflater inflater = getLayoutInflater();
            successfullyLogin = new AlertDialog.Builder(this);
            View dialogView = inflater.inflate(R.layout.alertinfo, null);
            successfullyLogin.setView(dialogView);
            Button yes = (Button) dialogView.findViewById(R.id.button6);
            TextView msg = (TextView) dialogView.findViewById(R.id.editText2);
            msg.setText(result);
            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dismissSuccessFullyLogin != null) {
                        dismissSuccessFullyLogin.cancel();
                    }

                    mEtAmount.setText("");

                }
            });
            successfullyLogin.setCancelable(false);
            dismissSuccessFullyLogin = successfullyLogin.show();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }
}
