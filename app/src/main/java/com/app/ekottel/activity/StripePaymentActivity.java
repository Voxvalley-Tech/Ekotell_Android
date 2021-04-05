package com.app.ekottel.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.app.ekottel.R;
import com.app.ekottel.Retrofit.ApiClient;
import com.app.ekottel.Retrofit.ApiInterface;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.HTTPUtils;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.wrapper.CSDataProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentAuthConfig;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.Address;
import com.stripe.android.model.Card;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StripePaymentActivity extends Activity {
    LinearLayout saveButton, cancelButton;
    static EditText cardNumber, mEditEmail;
    String chargeAmount, stripeToken;
    MaterialSpinner expMonth;
    MaterialSpinner expYear;
    static EditText cvc;
    MaterialSpinner currency;
    private static final String CURRENCY_UNSPECIFIED = "usd";
    public static  String PUBLISHABLE_KEY = "pk_live_e9y25Up6LWb6mDQ6TOCrC9eY00xcSkxYJf";

    private String exp_month = "", eEmail;
    private String exp_year = "";
    // private String actual_currency = "USD";
    private LinearLayout mRlBack;
    private Typeface webTypeFace;
    private TextView mPaymentTitle;
    androidx.appcompat.app.AlertDialog dismissSuccessFullyLogin;
    androidx.appcompat.app.AlertDialog.Builder successFullyLogin;
    private Stripe mStripe;
    Card card;
    CardInputWidget cardInputWidget;
    ProgressDialog mdialog;
    public static String clientsecrete = "";
    String email;

    PreferenceProvider preferenceProvider;
    private String loginID;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe_payment);
        this.saveButton = (LinearLayout) findViewById(R.id.ll_submit);
        cancelButton = (LinearLayout) findViewById(R.id.ll_cancel);
        mRlBack = (LinearLayout) findViewById(R.id.ll_stripe_back_arrow);
        mPaymentTitle = findViewById(R.id.iv_title_payment);
        cardNumber = (EditText) findViewById(R.id.number);
        cvc = (EditText) findViewById(R.id.sp_cvc);
        expMonth = (MaterialSpinner) findViewById(R.id.sp_material_month);
        expYear = (MaterialSpinner) findViewById(R.id.sp_material_year);
        webTypeFace = Utils.getTypefaceMedium(getApplicationContext());
        mPaymentTitle.setTypeface(webTypeFace);
        chargeAmount = getIntent().getStringExtra("selected");
        preferenceProvider = new PreferenceProvider(getApplicationContext());
        cardInputWidget = new CardInputWidget(this);
        final PaymentAuthConfig.Stripe3ds2UiCustomization uiCustomization =
                new PaymentAuthConfig.Stripe3ds2UiCustomization.Builder().build();
        PaymentAuthConfig.init(new PaymentAuthConfig.Builder()
                .set3ds2Config(new PaymentAuthConfig.Stripe3ds2Config.Builder()
                        // set a 5 minute timeout for challenge flow
                        .setTimeout(5)
                        // customize the UI of the challenge flow
                        .setUiCustomization(uiCustomization)
                        .build())
                .build());
        PUBLISHABLE_KEY = getIntent().getStringExtra(getString(R.string.stripe_publishable_key));
        PaymentConfiguration.init(getApplicationContext(), PUBLISHABLE_KEY);
        mStripe = new Stripe(this, PaymentConfiguration.getInstance(getApplicationContext()).getPublishableKey());

        Log.i("Payment Activity ", "mStripe:" + mStripe);
        email = preferenceProvider.getPrefString("emailid");
        //mEditEmail.setText(email);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (Utils.getNetwork(getApplicationContext())) {
                    if (cardNumber.getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), " Please Enter Valid Card Number", Toast.LENGTH_SHORT).show();
                    } else if (expMonth.getText().toString().isEmpty() || expMonth.getText().toString().equalsIgnoreCase("Month")) {

                        Toast.makeText(getApplicationContext(), " Please select expiry month", Toast.LENGTH_SHORT).show();
                    } else if (expYear.getText().toString().isEmpty() || expYear.getText().toString().equalsIgnoreCase("Year")) {
                        Toast.makeText(getApplicationContext(), " Please select expiry year", Toast.LENGTH_SHORT).show();

                    } else if (cvc.getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), " Please enter valid cvc", Toast.LENGTH_SHORT).show();

                    } else {

                        Card card = Card.create(getCardNumber(), getExpMonth(), getExpYear(), getCvc());
                        cardInputWidget.setCardNumber(card.getNumber());
                        cardInputWidget.setExpiryDate(card.getExpMonth(), card.getExpYear());
                        cardInputWidget.setCvcCode(card.getCVC());
                        loginID = CSDataProvider.getLoginID();

                        createStripeIntent(loginID, email, chargeAmount, CURRENCY_UNSPECIFIED);
                        //createPaymentIntent(chargeAmount, "usd", SECURABLE_KEY);//This should be a server call. Remove from app and call switch provided api
                        showProgressBar("Payment in process");
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Please check your internet Connection", Toast.LENGTH_SHORT).show();
                }


            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });


        webTypeFace = Utils.getTypeface(getApplicationContext().getApplicationContext());
        String[] EXPMONTH = getResources().getStringArray(R.array.month_array);
        ArrayAdapter<String> month_adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line, EXPMONTH);
        expMonth.setAdapter(month_adapter);
        String[] EXPYEAR = getResources().getStringArray(R.array.year_array);
        ArrayAdapter<String> year_adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line, EXPYEAR);
        expYear.setAdapter(year_adapter);


        if (mRlBack != null) {
            mRlBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }


        expMonth.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                exp_month = expMonth.getText().toString();

            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

            }
        });

        expYear.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                exp_year = expYear.getText().toString();

            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

            }
        });


        expMonth.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                Log.d("PaymentFormFragment", "onFocusChange: called HasFocus: " + hasFocus);

                if (hasFocus) {
                    hideKeypad();

                }

            }
        });

    }

    private void createStripeIntent(String loginID, String email, String chargeAmount, String currency) {


        ApiClient.getRestClient().create(ApiInterface.class).
                createStripIntent(Constants.STRIPE_CREATE_STRIPE_PAYMENT_INTENT,loginID, email, chargeAmount, currency).enqueue(new Callback<JsonElement>() {


            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {

                    JsonObject result = response.body().getAsJsonObject();
                    cancelProgressBar();

                    try {
                        verificationId = result.getAsJsonObject("data").getAsJsonObject("message").get("client_secret").toString();
                        verificationId = verificationId.replaceAll("\"", "");
//                        Log.i("StripePaymentActivity", "client_secret: " + verificationId);
                        if (verificationId != null && !verificationId.isEmpty()) {

                            // sendVerificartion(, email, chargeAmount, verificationId);
                            PaymentMethod.BillingDetails billingDetails = new PaymentMethod.BillingDetails.Builder().setAddress(new Address.Builder().setCity("Hyderabad").setCountry("IN").setLine1("Line1").setLine2("Line2").setPostalCode("500018").setState("Telegana").build()).setEmail(email).setName("srr").setPhone("+919848012345").build();
                            Log.i("StripePaymentActivity", "client_secret: " + verificationId);
                            confirmPayment(createConfirmPaymentIntentParams(verificationId, billingDetails));
                        } else {
                            Toast.makeText(StripePaymentActivity.this, "invalid response", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
//                    Log.i("", " " + response.body());

                    cancelProgressBar();

                    try{
                        JSONObject errorResponse = new JSONObject(response.errorBody().string());
                        showalert(errorResponse.getJSONObject("error").getString("message").toString());
                    }catch(Exception e){
                        e.printStackTrace();
                        showalert("Opps some thing went wrong, Please try later");
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(StripePaymentActivity.this, "invalid Details", Toast.LENGTH_SHORT).show();
                cancelProgressBar();
            }
        });

    }


    private void hideKeypad() {

        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null)
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

    }

    private ConfirmPaymentIntentParams createConfirmPaymentIntentParams(
            @NonNull String clientSecret,
            @NonNull PaymentMethod.BillingDetails billingDetails) {
        try {
            final PaymentMethodCreateParams.Card paymentMethodParamsCard =
                    cardInputWidget.getCard().toPaymentMethodParamsCard();
            final PaymentMethodCreateParams paymentMethodCreateParams =
                    PaymentMethodCreateParams.create(paymentMethodParamsCard,
                            billingDetails);
            return ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
                    paymentMethodCreateParams, clientSecret,
                    "https://www.google.com");//yourapp://post-authentication-return-url
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void confirmPayment(@NonNull ConfirmPaymentIntentParams params) {
        if (params != null) {

            Log.i("Yes confirm payment", "params" + params);
            mStripe.confirmPayment(this, params);
        } else {
            Toast.makeText(getApplicationContext(), "Invalid details", Toast.LENGTH_SHORT).show();
            cancelProgressBar();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mStripe.onPaymentResult(requestCode, data,
                new ApiResultCallback<PaymentIntentResult>() {
                    @Override
                    public void onSuccess(@NonNull PaymentIntentResult result) {
                        Log.i("StripePaymentActivity", "onActivity");
                        // If authentication succeeded, the PaymentIntent will
                        // have user actions resolved; otherwise, handle the
                        // PaymentIntent status as appropriate (e.g. the
                        // customer may need to choose a new payment method)

                        final PaymentIntent paymentIntent = result.getIntent();
                        final PaymentIntent.Status status = paymentIntent.getStatus();

                        Log.i("Payment Activity", "status: " + status);

                        if (status == PaymentIntent.Status.Succeeded) {
                            // show success UI
                            Log.i("Payment Activity", "status: " + PaymentIntent.Status.Succeeded);
                            //if(verificationId!=null){
                            sendVerificartion(loginID, email, chargeAmount, result.getIntent().getId());
                            // }


                        } else if (PaymentIntent.Status.RequiresPaymentMethod
                                == status) {
                            // attempt authentication again or
                            // ask for a new Payment Method
                            showalert("Payment faild");
                            Log.i("Payment Activity", "status: " + PaymentIntent.Status.RequiresPaymentMethod);

                        }
                    }

                    @Override
                    public void onError(@NonNull Exception e) {
                        Log.i("Payment Activity", "onError "+e);
                        e.printStackTrace();
                        // handle error
                    }
                });
    }

    private void sendVerificartion(String loginID, String email, String chargeAmount, String verificationId) {
        ApiClient.getRestClient().create(ApiInterface.class).
                verifyStripe(Constants.STRIPE_VERIFICATION_URL,loginID, email, chargeAmount, verificationId).enqueue(new Callback<JSONObject>() {

            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                if (response.isSuccessful()) {
                    cancelProgressBar();
                    showalert("Payment Successfully");

                } else {
                    Log.i("", " " + response.body());
                    Toast.makeText(StripePaymentActivity.this, "invalid id", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {

            }
        });

    }

    public void createPaymentIntent(String amount, final String currency, String authtoken) {
//        showProgressBar();

//        ApiClient.getClientWithAuth(authtoken).create(ApiInterface.class).createPaymentIntentReq(amount, currency).enqueue(new Callback<CreatePaymentIntentRes>() {
//            @Override
//            public void onResponse(Call<CreatePaymentIntentRes> call, Response<CreatePaymentIntentRes> response) {
//                Log.i("onResponse:", " " + response.toString());
//                if (response.isSuccessful()) {
//                    Log.i(" onResponse success", "CreatePaymentIntentRes onResponse success" + clientsecrete);
//                    clientsecrete = response.body().getClientSecret();
//                    Log.i("clientsecrete: ", " " + clientsecrete);
//                    PaymentMethod.BillingDetails billingDetails = new PaymentMethod.BillingDetails.Builder().setAddress(new Address.Builder().setCity("Hyderabad").setCountry("IN").setLine1("Line1").setLine2("Line2").setPostalCode("500018").setState("Telegana").build()).setEmail("testpayment@gmail.com").setName("srr").setPhone("+919848012345").build();
//
//                    confirmPayment(createConfirmPaymentIntentParams(clientsecrete, billingDetails));
//
//                    // Payment verification webservice call
//                    try {
//                        String data = URLEncoder.encode("username", "UTF-8") + "="
//                                + URLEncoder.encode(CSDataProvider.getLoginID(), "UTF-8");
//                        data += "&" + URLEncoder.encode("paymentid", "UTF-8") + "="
//                                + URLEncoder.encode(clientsecrete, "UTF-8");
//                        data += "&" + URLEncoder.encode("emailid", "UTF-8") + "="
//                                + URLEncoder.encode(email, "UTF-8");
//
//                        data += "&" + URLEncoder.encode("amount", "UTF-8") + "="
//                                + URLEncoder.encode(chargeAmount, "UTF-8");
//                        data += "&" + URLEncoder.encode("currency", "UTF-8") + "="
//                                + URLEncoder.encode(CURRENCY_UNSPECIFIED, "UTF-8");
//
//
//                        System.out.println("Response input" + data);
//
//                        new APITask(Constants.STRIPE_VERIFICATION_URL, data).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                        cancelProgressBar();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                    // TODO verify API call
//                } else {
//                    cancelProgressBar();
//                    Log.i("onResponse failure", "");
//                    Toast.makeText(getApplicationContext(), "Invalid Details", Toast.LENGTH_LONG).show();
//                }
//                cancelProgressBar();
//            }
//
//            @Override
//            public void onFailure(Call<CreatePaymentIntentRes> call, Throwable t) {
//                Log.i("Unable to submit", "onFailure" + t.getStackTrace());
//                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
//                cancelProgressBar();
//            }
//
//        });
    }


    public void showProgressBar(String message) {
        try {
            mdialog = new ProgressDialog(StripePaymentActivity.this);
            mdialog.setMessage(message);
            mdialog.setCancelable(false);
            mdialog.getWindow().setDimAmount(0.0f);
            mdialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void cancelProgressBar() {
        try {
            if (mdialog != null) {
                mdialog.dismiss();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getCardNumber() {
        return this.cardNumber.getText().toString();
    }

   /* public String getEmail() {
        return this.mEditEmail.getText().toString();
    }*/

    public String getCvc() {
        return this.cvc.getText().toString();
    }


    public Integer getExpMonth() {
        return getIntegerMonth(this.expMonth);
    }

    public Integer getExpYear() {
        return getIntegerYear(expYear);
    }

    private Integer getIntegerMonth(MaterialSpinner spinner) {
        try {
            return Integer.parseInt(exp_month);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Integer getIntegerYear(MaterialSpinner spinner) {
        try {
            return Integer.parseInt(exp_year);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * This is class gets the balance transfer information from server.
     */
    class APITask extends AsyncTask<Void, Void, Void> {
        private String url;
        String response = "";
        String api;

        APITask(String url, String api) {
            this.url = url;
            this.api = api;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar("Please wait");
        }

        @Override
        protected Void doInBackground(Void... params) {
            String mbody = api.toString();
            // Log.i("StripePaymentActivity", "URL: " + url + " , body: " + mbody);
            response = HTTPUtils.getStripeResponseFromURLPOST(url, mbody);


//            Toast.makeText(getApplicationContext(),response,Toast.LENGTH_SHORT).show();
            //System.out.println("Stripe Response: " + response);

            Log.d("PaymentActivity", "doInBackground: called and Stripe Response:" + response + ", URL: " + url);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            cancelProgressBar();

            try {

                if (cardNumber != null)
                    cardNumber.setText("");

                if (cvc != null)
                    cvc.setText("");
                Log.i("Payment", "amount:" + response);
                JSONObject json = new JSONObject(response);
                JSONObject result_json = null;

                if (HTTPUtils.statusCode == 200) {

                    result_json = json.getJSONObject("data");


                } else {
                    result_json = json.getJSONObject("error");
                }

                if (result_json != null) {

                    String status = result_json.getString("code");
                    String message = result_json.getString("message");

                    if (status != null && status.equalsIgnoreCase("200")) {

                        PreferenceProvider pf = new PreferenceProvider(getApplicationContext());
                        pf.setPrefboolean("balHit", true);

                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                        finish();

                    } else {
                        if (message != null && message.contains("User DeActivated")) {
                            showalert("User DeActivated");
                        } else {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }
                }

            } catch (Exception e) {
                if (response != null && response.contains("User DeActivated")) {
                    showalert("User DeActivated");
                }
                e.printStackTrace();
            }
        }
    }

    /**
     * This is class gets the balance transfer information from server.
     */


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
                    finish();
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
}







