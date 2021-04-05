package com.app.ekottel.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.Retrofit.ApiClient;
import com.app.ekottel.Retrofit.ApiInterface;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.HTTPUtils;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.wrapper.CSDataProvider;
import com.google.gson.JsonObject;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONObject;

import java.math.BigDecimal;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.app.ekottel.activity.StatusActivity.locateView;

public class TopUpActivity  extends AppCompatActivity {

    private static final String TAG ="TopUpActivity" ;
    private ImageView mRlBack, mDollaer, mDownArrow, mIcon;
    RelativeLayout mRootLayout;
    private TextView mTvBack, mAmountEdit;
    private Typeface webTypeFace;
    private EditText mEditEmail;
    private TextView mTvPaypal, mTvTransfer, mTvHeader;
    Handler h = new Handler();
    Runnable runnableObj;
    int delay = 30000;
    ProgressDialog progressBar;
    String email;
    PreferenceProvider preferenceProvider;
    AlertDialog dismissSuccessFullyLogin;
    androidx.appcompat.app.AlertDialog.Builder successFullyLogin;
    private static PayPalConfiguration config;
    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;
   // private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
    private MaterialSpinner spinner_currency;
    private String[] currencyArray = new String[]{"XCD", "USD"};
    private String currency;
    private int selectedAmount = 0;

    private static final String CURRENCY_SYMBOL = "$";
    private AlertDialog.Builder errorAlertBuilder;
    private AlertDialog errorAlertDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        setContentView(R.layout.activity_topup);
        webTypeFace = Utils.getTypeface(getApplicationContext());
        mTvHeader = (TextView) findViewById(R.id.tv_topup_header);
        mAmountEdit = findViewById(R.id.et_amount);
        mEditEmail = findViewById(R.id.et_email);
        mAmountEdit.setSelected(true);
        mTvTransfer = (TextView) findViewById(R.id.transfer);
        mDownArrow = findViewById(R.id.tv_status_arrow_down);
        mRootLayout = findViewById(R.id.lyt_paypal_amount_selection);
        mIcon = findViewById(R.id.img_payment_mode_icon);
        // mTvPaypal = (TextView) findViewById(R.id.tv_payment_paypal);
        // mTvStripe = (TextView) findViewById(R.id.tv_payment_stripe);
        mRlBack = findViewById(R.id.back);
        //mTvBack = (TextView) findViewById(R.id.tv_payment_back);
        mDollaer = findViewById(R.id.currency_icon1);
        Typeface text_medium = Utils.getTypefaceMedium(getApplicationContext());
        //mTvPaypal.setTypeface(text_medium);
        mTvTransfer.setTypeface(text_medium);
        mTvHeader.setTypeface(text_medium);
        preferenceProvider = new PreferenceProvider(getApplicationContext());
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        //mDollaer.setTypeface(text_medium);
        // mDownArrow.setTypeface(text_medium);
//        mDollaer.setVisibility(View.VISIBLE);
        // mDownArrow.setText(getString(R.string.transaction_arrow_down));
        email = preferenceProvider.getPrefString("emailid");
        mEditEmail.setText(email);
        spinner_currency = findViewById(R.id.spinner_currency);

        currency = preferenceProvider.getPrefString("currency");
        String paymentMode = getIntent().getStringExtra("PaymentMode");
         Log.e("Paypal","Paypal--->"+GlobalVariables.CONFIG_CLIENT_ID);
        if (paymentMode.equals("Paypal")) {
            mTvHeader.setText(R.string.pay_tag);
            mIcon.setImageResource(R.mipmap.paypal_text_icon);
            config = new PayPalConfiguration()
                    .environment(CONFIG_ENVIRONMENT)
                    .clientId("AZfW4xcI2cEyH5GVuPTp_lr2jPoe3nwGhpYm_dQ2fZ32Y6RXRsUceG1hh1SQRyFSrfyd-I34-zpaaxB5")
                    // The following are only used in PayPalFuturePaymentActivity.
                    .merchantName(Constants.PAYPAL_MERCHANT_NAME)
                    .merchantPrivacyPolicyUri(Uri.parse(Constants.PAYPAL_PRIVACY_POLICY_URL))
                    .merchantUserAgreementUri(Uri.parse(Constants.PAYPAL_LEGAL_URL));
        } else {
            mTvHeader.setText(R.string.stripe_tag);
            mIcon.setImageResource(R.mipmap.visa_mastercard);
        }


        if (mTvBack != null)
            mTvBack.setTypeface(webTypeFace);

        if (mRlBack != null) {
            mRlBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        mRootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_Select(TopUpActivity.this, mAmountEdit);
            }
        });
        /*mTvPaypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utils.getNetwork(TopUpActivity.this)) {
                    Toast.makeText(TopUpActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
                    return;
                }
                CSClient CSClientObj = new CSClient();
                if (!CSClientObj.getLoginstatus()) {
                    Toast.makeText(TopUpActivity.this, getString(R.string.registered_message), Toast.LENGTH_LONG).show();
                    return;
                }
                *//*if (!HomeScreenActivity.status.equalsIgnoreCase(getString(R.string.registered))) {
                    Toast.makeText(TopUpActivity.this, getString(R.string.registered_message), Toast.LENGTH_LONG).show();
                    return;
                }*//*
                new APITask(Constants.PAYPAL_GETCLIENTID_URL, getString(R.string.more_activity_paypal_api_value)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }
        });*/

        mTvTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              String amount = mAmountEdit.getText().toString().replace("$","").replace("&","").trim();

             //   String amount = String.valueOf(selectedAmount / Constants.USD_XCD_DIFF);
                if (selectedAmount == 0 || amount.equals("0")) {
                    Toast.makeText(TopUpActivity.this, getString(R.string.paypal_enter_amount), Toast.LENGTH_SHORT).show();
                    return;
                }



                if (paymentMode.equals("stripe")) {

                    try {
                        Log.i("Amount bAfter", "Amount" + amount);
                        amount = String.valueOf(Float.parseFloat(amount) * 100);
                        Log.i("Amount After", "Amount" + amount);
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                    email = mEditEmail.getText().toString().trim();

                    if (email.matches(emailPattern)) {
                        preferenceProvider.setPrefString("emailid", email);
                    } else {
                        mEditEmail.setError("Invalid email address");
                        return;
                        //Toast.makeText(getApplicationContext(),"Invalid email address", Toast.LENGTH_SHORT).show();
                    }

                    mTvHeader.setText(R.string.stripe_tag);
                    if (!Utils.getNetwork(TopUpActivity.this)) {
                        Toast.makeText(TopUpActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
                        return;
                    }

                    getStripPublishableKey(amount);
//
//                    Intent intent = new Intent(getApplicationContext(), StripePaymentActivity.class);
//                    intent.putExtra("selected", amount);
////                    intent.putExtra(getResources().getString(R.string.stripe_publishable_key),publishableKey);
//                    startActivity(intent);


                } else {
                    mTvHeader.setText(R.string.pay_tag);
                    Float finalAmount = Float.parseFloat(amount);

                    try {
                        email = mEditEmail.getText().toString().trim();

                        if (email.matches(emailPattern)) {
                            preferenceProvider.setPrefString("emailid", email);
                        } else {
                            mEditEmail.setError("Invalid email address");
                            return;
                            //Toast.makeText(getApplicationContext(),"Invalid email address", Toast.LENGTH_SHORT).show();
                        }
                        if (amount.equals("")) {
                            Toast.makeText(TopUpActivity.this, getString(R.string.paypal_enter_amount), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (finalAmount <= 0) {
                            Toast.makeText(TopUpActivity.this, getString(R.string.paypal_valid_amount), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE, finalAmount);
                        Intent intent = new Intent(TopUpActivity.this, PaymentActivity.class);
                        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
                        startActivityForResult(intent, REQUEST_CODE_PAYMENT);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(TopUpActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });


//        spinner_currency.addTextChangedListener(new TextWatcher() {
//
//            public void afterTextChanged(Editable s) {
//                currency = spinner_currency.getText().toString();
//                mAmountEdit.setText("");
//            }
//
//            public void beforeTextChanged(CharSequence s, int start,
//                                          int count, int after) {
//            }
//
//            public void onTextChanged(CharSequence s, int start,
//                                      int before, int count) {
//
//            }
//        });
//
//        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(getApplicationContext(),
//                android.R.layout.simple_dropdown_item_1line,currencyArray );
//        spinner_currency.setAdapter(currencyAdapter);


    }

    /**
     * this method gets the publishable key from the server,
     * If request success then redirect to StripePaymentActivity activity
     * @param amount
     */
    private void getStripPublishableKey(String amount) {
        showProgressbar();

        ApiClient.getRestClient().create(ApiInterface.class).getStripePublishableKey(Constants.STRIPE_PUBLISHABLE_KEY_URL).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                hideProgressbar();
               // Log.e("response","response-->"+Constants.STRIPE_PUBLISHABLE_KEY_URL);
                try {
                    if (response.isSuccessful()) {
                        String publishableKey = response.body().get("value").getAsString();
                        if(publishableKey == null || publishableKey.isEmpty()) {
                            showErrorMessage("Oops some thing went wrong, try after some time");
                            return;
                        }
                        Intent intent = new Intent(getApplicationContext(), StripePaymentActivity.class);
                        intent.putExtra("selected", amount);
                        intent.putExtra(getResources().getString(R.string.stripe_publishable_key),publishableKey);
                        startActivity(intent);
                    } else {
                        showErrorMessage(response.errorBody().string());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorMessage("Oops some thing went wrong, try after some time");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
                hideProgressbar();
                showErrorMessage("Oops some thing went wrong, try after some time");
            }
        });
    }


    private PayPalPayment getThingToBuy(String paymentIntent, float final_amount) {
        //    Log.i(TAG1, "final_amount:" + final_amount);

        return new PayPalPayment(new BigDecimal(final_amount), getString(R.string.paypal_usd), getString(R.string.paypal_my_package),
                paymentIntent);
    }

    private PopupWindow dialog_Select(final Context context, TextView lin) {

        final PopupWindow dialog_Select = new PopupWindow(context);

        View v = View.inflate(context, R.layout.topup_dropdown_listview, null);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        dialog_Select.setContentView(v);
        dialog_Select.setWidth(width - (int) getResources().getDimension(R.dimen._110dp));
        dialog_Select.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        dialog_Select.setFocusable(true);
        dialog_Select.setBackgroundDrawable(new BitmapDrawable(
                context.getApplicationContext().getResources(),
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)));


        TextView mTvFive = (TextView) v.findViewById(R.id.tv_add_status);
        TextView mTv10 = (TextView) v.findViewById(R.id.tv_add_ten);
        TextView mTv20 = (TextView) v.findViewById(R.id.tv_available);
        TextView mTv50 = (TextView) v.findViewById(R.id.tv_busy);
        TextView mTv100 = (TextView) v.findViewById(R.id.tv_send_me_number);
        TextView mTv500 = (TextView) v.findViewById(R.id.tv_in_meeting);

        Typeface text_light = Utils.getTypefaceLight(getApplicationContext());

        mTvFive.setTypeface(text_light);
        mTv10.setTypeface(text_light);
        mTv20.setTypeface(text_light);
        mTv50.setTypeface(text_light);
        mTv100.setTypeface(text_light);
        mTv500.setTypeface(text_light);


        String fiveAmount = currency.equals("USD") ? Utils.getUsdFromXcd("5") : "5";
        mTvFive.setText(CURRENCY_SYMBOL + " " + fiveAmount);

        String TenAmount = currency.equals("USD") ? Utils.getUsdFromXcd("10") : "10";
        mTv10.setText(CURRENCY_SYMBOL + " " + TenAmount);

        String twentyAmount = currency.equals("USD") ? Utils.getUsdFromXcd("20") : "20";
        mTv20.setText(CURRENCY_SYMBOL + " " + twentyAmount);

        String fiftyAmount = currency.equals("USD") ? Utils.getUsdFromXcd("50") : "50";
        mTv50.setText(CURRENCY_SYMBOL + " " + fiftyAmount);

        String hundredAmount = currency.equals("USD") ? Utils.getUsdFromXcd("100") : "100";

        mTv100.setText(CURRENCY_SYMBOL + " " + hundredAmount);

        String fiveHundredAmount = currency.equals("USD") ? Utils.getUsdFromXcd("500") : "500";
        mTv500.setText(CURRENCY_SYMBOL + " " + fiveHundredAmount);


        LinearLayout mFive = (LinearLayout) v.findViewById(R.id.ll_add_status);
        LinearLayout m20 = (LinearLayout) v.findViewById(R.id.ll_available);
        LinearLayout m10 = (LinearLayout) v.findViewById(R.id.layout_tentext);
        LinearLayout m50 = (LinearLayout) v.findViewById(R.id.ll_busy);
        LinearLayout m100 = (LinearLayout) v.findViewById(R.id.ll_send_me_message);
        LinearLayout m500 = (LinearLayout) v.findViewById(R.id.ll_meeting);
        mFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null) ;
                dialog_Select.dismiss();
                selectedAmount = 5;
                mAmountEdit.setText(mTvFive.getText());
            }
        });
        m10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null) ;
                dialog_Select.dismiss();
                selectedAmount = 10;
                mAmountEdit.setText(mTv10.getText());
            }
        });
        m20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null) ;
                dialog_Select.dismiss();
                selectedAmount = 20;
                mAmountEdit.setText(mTv20.getText());
            }
        });
        m50.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null) ;
                dialog_Select.dismiss();
                selectedAmount = 50;
                mAmountEdit.setText(mTv50.getText());
            }
        });

        m100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null) ;
                dialog_Select.dismiss();
                selectedAmount = 100;
                mAmountEdit.setText(mTv100.getText());
            }
        });
        m500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null) ;
                dialog_Select.dismiss();
                selectedAmount = 500;
                mAmountEdit.setText(mTv500.getText());
            }
        });

        Rect location = locateView(lin);

        dialog_Select.showAtLocation(v, Gravity.TOP | Gravity.CENTER, 0, location.bottom);

        // Getting a reference to Close button, and close the popup when clicked.

        return dialog_Select;

    }

    private boolean showErrorMessage(String result) {
        try {
            LayoutInflater inflater = getLayoutInflater();
            errorAlertBuilder = new androidx.appcompat.app.AlertDialog.Builder(this);
            View dialogView = inflater.inflate(R.layout.alertinfo, null);
            errorAlertBuilder.setView(dialogView);
            Button yes = (Button) dialogView.findViewById(R.id.button6);
            TextView msg = (TextView) dialogView.findViewById(R.id.editText2);
            msg.setText(result);
            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (errorAlertDialog != null) {
                        errorAlertDialog.cancel();
                    }
                    finish();
                }
            });
            errorAlertBuilder.setCancelable(false);
            errorAlertDialog = errorAlertBuilder.show();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }



    public void showProgressbar() {
        if(progressBar == null){
            progressBar = new ProgressDialog(this);
            progressBar.setCancelable(false);
            progressBar.setMessage(getString(R.string.bal_trans_wait_message));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
        }
        progressBar.show();
    }

    private void hideProgressbar(){
        if(progressBar != null){
            progressBar.dismiss();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                      Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));

                        String returndata = confirm.toJSONObject().toString(4);

                       Log.i(TAG, "returndata" + returndata);

                        JSONObject jsonObj = new JSONObject(returndata);
                        //JSONArray array = jsonObj.getJSONArray("data");
                        String message;
                        try {
                            message = jsonObj.getJSONObject(getString(R.string.paypal_json_key_response)).getString(getString(R.string.paypal_json_key_id));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            message = "";
                        }

//                        Log.i(TAG, "payment id" + message);

                        String username = CSDataProvider.getLoginID();
                        username = username.replace("+", "");

                        verifyPaypal(message);

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
        }
    }
    public  void verifyPaypal(String paymentId){
        String username = CSDataProvider.getLoginID().toString().trim();

        ApiClient.getRestClient().create(ApiInterface.class).
                    verifyPaypal(Constants.PAYPAL_VERIFYPAYMENT_URL,paymentId, username,email,PayPalConfiguration.ENVIRONMENT_PRODUCTION).enqueue(new Callback<String>() {

                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Log.e("response","response---"+response);
                    Log.e("response","response---"+response.isSuccessful());
                    hideProgressbar();
                    if (response.isSuccessful()) {
                        showErrorMessage("Payment Successfully");
                        String[] strs = response.body().split("\"");

                        try{
                            if(strs.length > 0) {
                                String tranxid = strs[1];
                            }
                        }catch (Exception e){
    //                                        showErrorMessage("Payment Failed, Please contact support@kodatel.ca.");
                        }

                    } else {
                        showErrorMessage("Payment Failed, Please contact support@kodatel.ca.");

                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    hideProgressbar();
                    showErrorMessage("Payment Failed, Please contact support@kodatel.ca.");

                }
            });

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
            hideProgressbar();
            String[] strs = response.split("\"");
            try{
                if(strs.length > 0) {
                    String tranxid = strs[1];
//                Log.i(TAG, "tranxid to be removed:" + tranxid);
                    showErrorMessage("Payment Successful from the Bank, You will be need to wait for an payment approval. Please contact support@kodatel.ca.");
                }
            }catch (Exception e){
                showErrorMessage("Payment Failed, Please contact Signed out\n" +
                        "jibiyality.app@gmail.com\n");
            }
        }
    }

}
