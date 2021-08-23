package com.app.ekottel.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;

import static com.app.ekottel.utils.GlobalVariables.LOG;

import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.utils.CallMethodHelper;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.DialerUtils;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.HTTPUtils;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.wrapper.CSDataProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;

/**
 * This activity is used to display dial pad.
 *
 * @author Ramesh U
 * @version 2017
 */
public class DialpadActivity extends AppCompatActivity {
    private TextView mTvBackArrow, mTvFlag, mTvDownArrow, mTvCountryCode, mTvDialPadSelectCountry, mTvCallRates, mTvAvailCredit, mTvDialerButtonTextTextViewNum;
    public static TextView mTvBalance;
    EditText mEtDialpadNumber;
    ImageView mTvDialPadCall;
    public static TextView mTvDialpadRegStatus;
    ImageView mIvDeleteNumber;
    LinearLayout mllSelectCountry, mllCallRates;

    float size = 38, size1 = 45;
    int deviceWidth;
    int deviceHeight;
    String countryCode = "";
    ProgressDialog progressBar;
    Handler h = new Handler();
    int delay = 10000;
    Runnable runnableObj;
    long txnId = 0;
    static int clickPosition = 0;
    boolean isReturningFromCall = false;
    PreferenceProvider mPreferenceProvider;
    TextView mTvDialpadAddContact;
    ImageView mIvDiapladCountry, iv_dialpad_add_contact;
    private String userName = "";
    private String password = "";
    boolean checkBalance = true;
    private String TAG;
    private boolean isCountryCode = false;
    private BalanceTransferReceiver balanceTransferReceiver = new BalanceTransferReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialpad);
        TAG = getString(R.string.dialpad_tag);
        LinearLayout mLLBackArrow = (LinearLayout) findViewById(R.id.ll_dialpad_back_arrow);
        mTvBackArrow = (TextView) findViewById(R.id.tv_dialpad_back_arrow);
        mTvFlag = (TextView) findViewById(R.id.tv_dialpad_flag);
        mTvDownArrow = (TextView) findViewById(R.id.tv_dialpad_arrow_down);
        mTvDialPadCall = (ImageView) findViewById(R.id.tv_dialpad_call);
        balanceTransferReceiver = new BalanceTransferReceiver();
        IntentFilter filter1 = new IntentFilter(getString(R.string.balance_transfered_successful));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(balanceTransferReceiver, filter1);
        mIvDiapladCountry = (ImageView) findViewById(R.id.iv_diaplad_country);
        iv_dialpad_add_contact = (ImageView) findViewById(R.id.iv_dialpad_add_contact);

        mIvDiapladCountry.setVisibility(View.GONE);
        mTvFlag.setVisibility(View.VISIBLE);

        mTvCountryCode = (TextView) findViewById(R.id.tv_dialpad_country_code);

        // mTvDialpadAddContact = (TextView) findViewById(R.id.tv_dialpad_add_contact);

        userName = CSDataProvider.getLoginID();
        userName = userName.replace("+", "");
        password = CSDataProvider.getPassword();

        mTvDialerButtonTextTextViewNum = (TextView) findViewById(R.id.view_dialer_button_text_textView_num);

        mTvBalance = (TextView) findViewById(R.id.tv_balance);
        mTvCallRates = (TextView) findViewById(R.id.tv_call_rate);
        mTvAvailCredit = (TextView) findViewById(R.id.tv_avail_credit_text);

        mTvDialPadSelectCountry = (TextView) findViewById(R.id.tv_dialpad_select_country);

        mTvDialPadSelectCountry.setSelected(true);

        mTvDialpadRegStatus = (TextView) findViewById(R.id.tv_dialpad_reg_status);

        mTvDialpadRegStatus.setSelected(true);

        mEtDialpadNumber = (EditText) findViewById(R.id.et_dialpad_number);

        mIvDeleteNumber = (ImageView) findViewById(R.id.iv_backspace);

        mllSelectCountry = (LinearLayout) findViewById(R.id.ll_dialpad_select_country);

        mllCallRates = (LinearLayout) findViewById(R.id.ll_call_rates);

        mllCallRates.setVisibility(View.INVISIBLE);

        mPreferenceProvider = new PreferenceProvider(getApplicationContext());
        Typeface text_font = Utils.getTypeface(getApplicationContext());
        mTvFlag.setTypeface(text_font);
        mTvDownArrow.setTypeface(text_font);
        mTvBackArrow.setTypeface(text_font);
        //   mTvDialpadAddContact.setTypeface(text_font);

        mTvFlag.setText(getString(R.string.signup_flag));
        mTvDownArrow.setText(getString(R.string.signup_arrow_down));
        mTvBackArrow.setText(getString(R.string.contacts_details_back_arrow));

        //  mTvDialpadAddContact.setText(getString(R.string.dialpad_add_contact));

        String dialpadNum = mPreferenceProvider.getPrefString("DialpadNumber");

        String dialpadNumCountry = mPreferenceProvider.getPrefString("DialpadNumberCountry");

        String dialNumberFromAnotherApp = getIntent().getStringExtra("DialNumberFromAnotherApp");

        if (dialNumberFromAnotherApp != null) {
            fillDialPadNumber(dialNumberFromAnotherApp);
        } else if (dialpadNum != null && !dialpadNum.isEmpty()) {
            fillDialPadNumber(dialpadNum);
        }

        if (dialpadNumCountry != null && !dialpadNumCountry.isEmpty()) {
            countryCode = dialpadNumCountry;
            mPreferenceProvider.setPrefString("DialpadNumberCountry", null);
            mTvCountryCode.setText(dialpadNumCountry);
            getcountryname(mTvCountryCode.getText().toString().replaceAll("[^0-9]", ""));
        }

        iv_dialpad_add_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEtDialpadNumber.getText().length() != 0) {
                    Intent in = new Intent(Intent.ACTION_INSERT);
                    in.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    in.putExtra(ContactsContract.Intents.Insert.PHONE, mEtDialpadNumber.getText().toString());
                    startActivity(in);
                } else {

                    Toast.makeText(getApplicationContext(), getString(R.string.dialpad_enter_number), Toast.LENGTH_SHORT).show();
                }
            }
        });

        DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_0,
                "0", "+", DialerUtils.TAG_0, mOnDialerClick, size, size1, mOnDialerTextLongClick);
        DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_1,
                "1", "", DialerUtils.TAG_1, mOnDialerClick, size, size1);
        DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_2,
                "2", "", DialerUtils.TAG_2, mOnDialerClick, size, size1);
        DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_3,
                "3", "", DialerUtils.TAG_3, mOnDialerClick, size, size1);
        DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_4,
                "4", "", DialerUtils.TAG_4, mOnDialerClick, size, size1);
        DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_5,
                "5", "", DialerUtils.TAG_5, mOnDialerClick, size, size1);
        DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_6,
                "6", "", DialerUtils.TAG_6, mOnDialerClick, size, size1);
        DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_7,
                "7", "", DialerUtils.TAG_7, mOnDialerClick, size, size1);
        DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_8,
                "8", "", DialerUtils.TAG_8, mOnDialerClick, size, size1);
        DialerUtils.setDialerTextButton(this, R.id.screen_tab_dialer_button_9,
                "9", "", DialerUtils.TAG_9, mOnDialerClick, size, size1);
        DialerUtils.setDialerTextButton(this,
                R.id.screen_tab_dialer_button_star, "*", "",
                DialerUtils.TAG_STAR, mOnDialerClick, size, size1);
        DialerUtils.setDialerTextButton(this,
                R.id.screen_tab_dialer_button_sharp, "#", "",
                DialerUtils.TAG_SHARP, mOnDialerClick, size, size1);

        try {

            WindowManager wm = (WindowManager) getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            deviceWidth = metrics.widthPixels;
            deviceHeight = metrics.heightPixels;

            /*if (deviceWidth > 480 && deviceHeight > 800) {
                mTvCountryCode.setTextSize(30);


            } else {
                mTvCountryCode.setTextSize(20);
            }*/


        } catch (Exception e) {
            e.printStackTrace();
        }

        mllSelectCountry.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), ZoneSelectActivity.class);
                startActivityForResult(intent, 999);
            }
        });

        mIvDeleteNumber.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mEtDialpadNumber.setText("");
                mTvCountryCode.setText("");
                mTvDialPadSelectCountry.setText(getString(R.string.dialpad_select_country));
                countryCode = "";
                mllCallRates.setVisibility(View.INVISIBLE);

                mIvDiapladCountry.setVisibility(View.GONE);
                mTvFlag.setVisibility(View.VISIBLE);
                return true;
            }
        });

        mIvDeleteNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final int selStart = mEtDialpadNumber
                        .getSelectionStart();
                final String number = mEtDialpadNumber.getText()
                        .toString();

                if (selStart > 0) {
                    final StringBuffer sb = new StringBuffer(number);
                    sb.delete(selStart - 1, selStart);
                    mEtDialpadNumber.setText(sb.toString());
                    mEtDialpadNumber.setSelection(selStart - 1);
                }


            }
        });

        mEtDialpadNumber
                .setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        v.onTouchEvent(event);
                        InputMethodManager imm = (InputMethodManager) v
                                .getContext().getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                        return true;
                    }
                });


        mEtDialpadNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                String number = null;
                if (mEtDialpadNumber.getText().toString().length() > 0) {
                    s = s.toString().replaceAll("[^0-9]", "");
                }

                if (s.length() == 0 && !isCountryCode) {
                    if (mTvCountryCode.getText().toString().length() > 0) {
                        isCountryCode = true;
                    } else {
                        isCountryCode = false;
                        mIvDiapladCountry.setVisibility(View.GONE);
                        mTvFlag.setVisibility(View.VISIBLE);
                        mTvCountryCode.setText("");
                        mTvDialPadSelectCountry.setText(getString(R.string.dialpad_select_country));
                        countryCode = "";
                        mllCallRates.setVisibility(View.INVISIBLE);
                    }


                }
                if (mEtDialpadNumber.getText().toString().length() == 0) {
                    mllCallRates.setVisibility(View.INVISIBLE);
                    isCountryCode = false;
                    mIvDiapladCountry.setVisibility(View.GONE);
                    mTvFlag.setVisibility(View.VISIBLE);
                    mTvCountryCode.setText("");
                    mTvDialPadSelectCountry.setText(getString(R.string.dialpad_select_country));
                    countryCode = "";
                    mllCallRates.setVisibility(View.INVISIBLE);
                }
                LOG.info("onTextChanged: dialer country code tv  " + mTvCountryCode.getText().toString());
                if (mTvCountryCode.getText().toString().trim().length() < 1 && s.length() > 3) {
                    getcountryname(s.toString());
                }

                String actualNumber = null;

                if (!mTvCountryCode.getText().toString().isEmpty()) {
                    actualNumber = mTvCountryCode.getText().toString().replaceAll("\\D", "");
                    actualNumber = actualNumber + s;
                } else {
                    actualNumber = s.toString();
                }

                if (actualNumber.length() >= 6) {

                    String value = actualNumber;
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
                    String url = Constants.CALL_RATES_URL + value + "?username=%2B" + userName + "&password=" + actualPassword;
                    new APITask(url, getString(R.string.dialpad_call_rates_api_message)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                }

                handleNumberSize(s);


            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });


        mLLBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        new Thread(new Runnable() {
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mTvBalance.setText("$" + mPreferenceProvider.getPrefString(getString(R.string.dialpad_avail_bal)));
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
                            String url = Constants.BALANCE_URL + "%2B" + userName + "?password=" + actualPassword;
                            new APITask(url, getString(R.string.dialpad_balance_api_message)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();


        mTvDialPadCall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                try {
                    if (new PreferenceProvider(getApplicationContext()).getPrefBoolean(getString(R.string.call_logs_pref_gsm_key))) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.gsm_message),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (mEtDialpadNumber.getText().toString().equals("")) {
                        // Cursor reccur = IAmLiveDB.getcalllogcursor();
                        HomeScreenActivity.lastDialedNumber = mPreferenceProvider.getPrefString(PreferenceProvider.LAST_DIAL_NUMBER);
                        if (!HomeScreenActivity.lastDialedNumber.equals("")) {
                            mEtDialpadNumber.setText(HomeScreenActivity.lastDialedNumber);
                            String number = mEtDialpadNumber.getText().toString();
                            clickPosition = number.length();
                            mEtDialpadNumber.setSelection(clickPosition);

                        } else {
                            Toast.makeText(DialpadActivity.this, getString(R.string.dialpad_enter_number_message), Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        try {
                            isReturningFromCall = true;
                            CallMethodHelper.processAudioCall(DialpadActivity.this, mTvCountryCode.getText().toString().trim() + mEtDialpadNumber.getText().toString().trim(), "PSTN");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }


                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            }

        });


    }

    /**
     * this method fills the dial pad number from 3rd party apps and aslo for the already peviously entered number also
     * and calles the call rates fro the number
     *
     * @param dialpadNum
     */
    private void fillDialPadNumber(String dialpadNum) {
        mPreferenceProvider.setPrefString("DialpadNumber", null);
        mEtDialpadNumber.setText(dialpadNum);

        handleNumberSize(mEtDialpadNumber.getText().toString());

        getcountryname(mEtDialpadNumber.getText().toString().replaceAll("[^0-9]", ""));

        String value = dialpadNum;
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
        String url = Constants.CALL_RATES_URL + value + "?username=%2B" + userName + "&password=" + actualPassword;
        new APITask(url, getString(R.string.dialpad_call_rates_api_message)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void handleNumberSize(CharSequence s) {
        if (deviceWidth > 480 && deviceHeight > 800) {

            if (s.length() <= 13 && s.length() >= 0) {

                mEtDialpadNumber.setTextSize(30);
                mTvCountryCode.setTextSize(30);

            } else if (s.length() >= 14 && s.length() <= 15) {

                mEtDialpadNumber.setTextSize(20);
                mTvCountryCode.setTextSize(20);
            } else if (s.length() >= 16) {

                mEtDialpadNumber.setTextSize(14);
                mTvCountryCode.setTextSize(14);
            } else {

                mEtDialpadNumber.setTextSize(30);
                mTvCountryCode.setTextSize(30);
            }
        } else {

            if (s.length() <= 13 && s.length() >= 0) {

                mEtDialpadNumber.setTextSize(25);
                mTvCountryCode.setTextSize(25);

            } else if (s.length() >= 14 && s.length() <= 15) {

                mEtDialpadNumber.setTextSize(20);
                mTvCountryCode.setTextSize(20);
            } else if (s.length() >= 16) {

                mEtDialpadNumber.setTextSize(14);
                mTvCountryCode.setTextSize(14);
            } else {

                mTvCountryCode.setTextSize(25);
            }
        }
    }

    //Handle to api call for balance and call rates
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
        }

        @Override
        protected Void doInBackground(Void... params) {
            response = HTTPUtils.getResponseFromURLGET(url);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            processResponse(api, response);
        }
    }

    /**
     * This method is handle response and update UI
     *
     * @param api        this is used get whatever api calls
     * @param returndata this is used for required data using api
     */
    private void processResponse(String api, String returndata) {

        if (api.equals(getString(R.string.dialpad_call_rates_api_message))) {
            try {


                //LOG.info("returndata:" + returndata);
                JSONObject jsonObj = new JSONObject(returndata);
                String message = "";
                JSONArray array = jsonObj.getJSONObject(getString(R.string.dialpad_response_message_key)).getJSONArray(getString(R.string.dialpad_response_message_value));
                //LOG.info("array length:" + array.length());
                for (int i = 0; i < array.length(); i++) {
                    message = array.getJSONObject(i).getString(getString(R.string.dialpad_response_cost_key));
                }


                if (message.equals("")) {

                    mllCallRates.setVisibility(View.INVISIBLE);
                } else {
                    if (mEtDialpadNumber.getText().toString().trim().length() == 0) {
                        mllCallRates.setVisibility(View.INVISIBLE);
                    } else {
                        mllCallRates.setVisibility(View.VISIBLE);
                        mTvCallRates.setText("$" + message);
                    }

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (api.equals(getString(R.string.dialpad_balance_api_message))) {

            try {
                JSONObject jsonObj = new JSONObject(returndata);
                String balance = "";

                JSONArray array = jsonObj.getJSONObject(getString(R.string.dialpad_response_message_key)).getJSONArray(getString(R.string.dialpad_response_message_value));
                for (int i = 0; i < array.length(); i++) {
                    balance = array.getJSONObject(i).getString(getString(R.string.dialpad_balance_api_message));
                }

                Log.i("mTvBalance", "mTvBalance--->" + balance);
                if (balance != null && !balance.isEmpty()) {
                    mPreferenceProvider.setPrefString(getString(R.string.dialpad_avail_bal), balance);
                    mTvBalance.setText("$" + balance);
                } else {
                    mPreferenceProvider.setPrefString(getString(R.string.dialpad_avail_bal), "0");
                    mTvBalance.setText("$" + "0");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * this method is used to get country name
     *
     * @param code_num this is used for get country name
     */
    public void getcountryname(String code_num) {
        LOG.info("getcountryname: getCountry name request called");
        if (code_num != null && code_num.length() > 3) {
            code_num = code_num.substring(0, 3);
        }
        LOG.info("getcountryname: countryCode " + getTwoCharCountryCode(code_num));
        Locale obj = new Locale("", getTwoCharCountryCode(code_num));
        String countryName = obj.getDisplayCountry();
        String contryCode = "";
        boolean isName = true;
        if (countryName.equals("") && code_num != null && code_num.length() > 2) {
            contryCode = code_num.substring(0, 2);
            obj = new Locale("", getTwoCharCountryCode(code_num.substring(0, 2)));
            countryName = obj.getDisplayCountry();

            if (!countryName.equals(""))
                isName = false;


        }

        if (countryName.equals("") && code_num != null && code_num.length() > 1) {
            contryCode = code_num.substring(0, 1);
            obj = new Locale("", getTwoCharCountryCode(code_num.substring(0, 1)));
            countryName = obj.getDisplayCountry();
        } else {
            if (isName)
                contryCode = code_num;
        }
        if (!countryName.equals("")) {
            mTvDialPadSelectCountry.setText(countryName);
            LOG.info("country name is" + countryName);


            int resorceID = getResources().getIdentifier(getString(R.string.dialpad_tringy_message) + contryCode, getString(R.string.dialpad_drawable_message), getPackageName());

            mIvDiapladCountry.setVisibility(View.VISIBLE);
            mTvFlag.setVisibility(View.GONE);
            mIvDiapladCountry.setImageResource(resorceID);

        } else {
            mIvDiapladCountry.setVisibility(View.GONE);
            mTvFlag.setVisibility(View.VISIBLE);
            mTvCountryCode.setText("");
            mTvDialPadSelectCountry.setText(getString(R.string.dialpad_select_country));
        }


    }

    /**
     * This method is used for handle coutry code
     *
     * @param code this is used for get country code
     * @return returns country code
     */
    private String getTwoCharCountryCode(String code) {
        if (!code.contains("+")) {
            code = "+" + code;
        }


        Log.i(TAG, "country: " + code);
        for (Map.Entry<String, String> item : ZoneSelectActivity.country2Phone.entrySet()) {
            // Log.i(TAG, "getTwoCharCountryCode: itemvalue " + item.getValue());
            if (item.getValue().equalsIgnoreCase(code)) {
                return item.getKey();
            }
        }
        return "";
    }

    //This method is used for display progress dialog
    public void showProgressBar() {
        try {
            progressBar = new ProgressDialog(DialpadActivity.this);
            progressBar.setCancelable(false);
            progressBar.setMessage(getString(R.string.dialpad_dialing_message));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.show();

            h = new Handler();
            runnableObj = new Runnable() {

                public void run() {
                    h.postDelayed(this, delay);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            dismissProgressBar();
                            Toast.makeText(DialpadActivity.this, getString(R.string.bal_trans_time_out), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };
            h.postDelayed(runnableObj, delay);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }


//This is used for handle country code

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999) {
            if (null != data && data.getExtras().containsKey(getString(R.string.dialpad_intent_zone))) {

                String countryName = "";
                String CurrentString = data.getStringExtra(getString(R.string.dialpad_intent_zone));
                String[] separated = CurrentString.split("\\(");
                countryName = separated[0]; // this will contain "Fruit"

                countryCode = getZipCodeByName(countryName);
                mTvDialPadSelectCountry.setText(countryName != null && !countryName.isEmpty() ? countryName : getString(R.string.dialpad_country_message));

                mTvCountryCode.setText(countryCode);

                String actualNumber = null;
                actualNumber = countryCode.replaceAll("\\D", "");
                actualNumber = actualNumber + mEtDialpadNumber.getText().toString();
                if (actualNumber != null && actualNumber.length() >= 6) {
                    String value = actualNumber.toString();
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
                    String url = Constants.CALL_RATES_URL + value + "?username=%2B" + userName + "&password=" + actualPassword;
                    new APITask(url, getString(R.string.dialpad_call_rates_api_message)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }


                String country_code = countryCode;
                LOG.info("Country Code before" + country_code);

                country_code = country_code.replaceAll("[^0-9]", "");


                LOG.info("Country Code after" + country_code);

                if (countryName.contains("Canada")) {
                    country_code = "1587";
                }
                if (countryName.contains("Australia")) {
                    country_code = "61";
                }
                int resourceID = getResources().getIdentifier(getString(R.string.dialpad_kodatel_message) + country_code, getString(R.string.dialpad_drawable_message), getPackageName());

                mIvDiapladCountry.setVisibility(View.VISIBLE);
                mTvFlag.setVisibility(View.GONE);
                mIvDiapladCountry.setImageResource(resourceID);

            }
        }
    }


    //This method is used for get country code
    public String getZipCodeByName(String countryName) {
        String CountryZipCode = "";
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[2].trim().equalsIgnoreCase(countryName.trim())) {
                CountryZipCode = g[1];
                break;
            }
        }
        return "+" + CountryZipCode;
    }


    private final View.OnLongClickListener mOnDialerTextLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            int tag = Integer.parseInt(v.getTag().toString());
            if (tag == DialerUtils.TAG_0) {
                appendText("+");
            }
            return true;
        }
    };

    private final View.OnClickListener mOnDialerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            int tag = Integer.parseInt(v.getTag().toString());
            final String textToAppend = tag == DialerUtils.TAG_STAR ? "*"
                    : (tag == DialerUtils.TAG_SHARP ? "#" : Integer
                    .toString(tag));
            appendText(textToAppend);

        }
        // }
    };

    private void appendText(String textToAppend) {
        if (mEtDialpadNumber.getText().toString().length() <= 24) {
            final int selStart = mEtDialpadNumber.getSelectionStart();
            final StringBuffer sb = new StringBuffer(mEtDialpadNumber
                    .getText().toString());
            sb.insert(selStart, textToAppend);
            mEtDialpadNumber.setText(sb.toString());
            mEtDialpadNumber.setSelection(selStart + 1);
        }
    }

    //This method is used for update UI
    public void updateUI(String str) {

        try {

            if (str.equals(getString(R.string.network_message))) {
                LOG.info("NetworkError receieved");
                dismissProgressBar();

            } else if (str.equals(getString(R.string.dialpad_nointernet_message))) {
                LOG.info("NetworkError receieved");
                dismissProgressBar();

            } else if (str.equals(getString(R.string.dialpad_register_sip_user_resuccess1_message))) {
                LOG.info(getString(R.string.dialpad_register_sip_user_resuccess1_message));
            } else if (str.equals(getString(R.string.dialpad_register_sip_user_resuccess_message))) {
                LOG.info(getString(R.string.dialpad_register_sip_user_resuccess_message));

                String number = countryCode + mEtDialpadNumber.getText().toString().replaceAll(" ", "");
                GlobalVariables.destinationnumbettocall = number;

                GlobalVariables.destinationnumbettocall = GlobalVariables.destinationnumbettocall.replaceAll("\\+", "");

                HomeScreenActivity.lastDialedNumber = mEtDialpadNumber.getText().toString().replaceAll(" ", "");

            } else if (str.equals(getString(R.string.dialpad_register_sip_user_res_failure_message))) {

                LOG.info(getString(R.string.dialpad_register_sip_user_res_failure_message));
            } else if (str.equals(getString(R.string.dialpad_call_answer_req_failure_message))) {
                LOG.info(getString(R.string.dialpad_call_answer_req_failure_message));

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * This method used to dismiss progress bar.
     */
    public void dismissProgressBar() {
        try {
            if (progressBar != null) {
                progressBar.dismiss();

            }
            if (h != null) {
                h.removeCallbacks(runnableObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //This method is used handle call backs
    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                LOG.info("Yes Something received in RecentReceiver");


                if (intent.getAction().equals(getString(R.string.network_message))) {
                    updateUI(getString(R.string.network_message));
                } else if (intent.getAction().equals(getString(R.string.dialpad_nointernet_message))) {
                    updateUI(getString(R.string.dialpad_nointernet_message));
                } else if (intent.getAction().equals(getString(R.string.dialpad_register_sip_user_resuccess_message))) {

                    if (txnId == intent.getLongExtra(getString(R.string.dialpad_intent_txnid_message), 0)) {
                        updateUI(getString(R.string.dialpad_register_sip_user_resuccess_message));
                        dismissProgressBar();
                    } else {
                        updateUI(getString(R.string.dialpad_register_sip_user_resuccess1_message));
                    }

                } else if (intent.getAction().equals(getString(R.string.dialpad_register_sip_user_res_failure_message))) {
                    updateUI(getString(R.string.dialpad_register_sip_user_res_failure_message));
                    if (txnId == intent.getLongExtra(getString(R.string.dialpad_intent_txnid_message), 0)) {
                        Toast.makeText(DialpadActivity.this, getString(R.string.dialpad_failure_message), Toast.LENGTH_SHORT).show();
                        dismissProgressBar();
                    }
                } else if (intent.getAction().equals(getString(R.string.dialpad_call_answer_req_failure_message))) {
                    updateUI(getString(R.string.dialpad_call_answer_req_failure_message));
                } else if (intent.getAction().equals(getString(R.string.http_message))) {
                    LOG.info("httpreturn receieved");
                    dismissProgressBar();
                    String api = intent.getStringExtra(getString(R.string.dialpad_intent_api_message));
                    String response = intent.getStringExtra(getString(R.string.dialpad_intent_response_message));
                    String returnData = intent.getStringExtra(getString(R.string.dialpad_intent_data_message));

                    if (api.equals(getString(R.string.dialpad_call_rates_api_message)) && (response.contains("200") || response.contains("202"))) {
                        try {


                            //LOG.info("returnData:" + returnData);
                            JSONObject jsonObj = new JSONObject(returnData);
                            String message = "";
                            JSONArray array = jsonObj.getJSONObject(getString(R.string.dialpad_response_message_key)).getJSONArray(getString(R.string.dialpad_response_message_value));
                            LOG.info("array length:" + array.length());
                            for (int i = 0; i < array.length(); i++) {
                                message = array.getJSONObject(i).getString(getString(R.string.dialpad_response_cost_key));
                            }

                            if (message.equals("")) {
                                mllCallRates.setVisibility(View.INVISIBLE);
                            } else {
                                if (mEtDialpadNumber.getText().toString().trim().length() == 0) {
                                    mllCallRates.setVisibility(View.INVISIBLE);
                                } else {
                                    mllCallRates.setVisibility(View.VISIBLE);
                                    mTvCallRates.setText("$" + message);
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }

    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    public void onResume() {
        super.onResume();

        try {
            if (HomeScreenActivity.status != null && !HomeScreenActivity.status.isEmpty()) {
                mTvDialpadRegStatus.setText(HomeScreenActivity.status);
            }

            LOG.info("onResume: called");
            if (mEtDialpadNumber != null && mPreferenceProvider.getPrefBoolean(PreferenceProvider.IS_RETURNING_FROM_CALL)) {
                mPreferenceProvider.setPrefboolean(PreferenceProvider.IS_RETURNING_FROM_CALL, false);
                mEtDialpadNumber.setText("");
            }
            if (mPreferenceProvider.getPrefBoolean(PreferenceProvider.NEED_TO_SHOW_CALL_END_REASON)) {
                mPreferenceProvider.setPrefboolean(PreferenceProvider.NEED_TO_SHOW_CALL_END_REASON, false);
                String numberToPass = mPreferenceProvider.getPrefString(PreferenceProvider.LAST_DIAL_NUMBER);
                Utils.showAlertToUser("Sorry, You don't have enough credit to call this number " + numberToPass, DialpadActivity.this);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Updating balance after returning PSTN Call
        try {
            if (isReturningFromCall) {
                isReturningFromCall = false;
                mTvBalance.setText("$" + mPreferenceProvider.getPrefString(getString(R.string.dialpad_avail_bal)));
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

                String url = Constants.BALANCE_URL + "%2B" + userName + "?password=" + actualPassword;
                LOG.info("Balance URL: " + url);
                new APITask(url, getString(R.string.dialpad_balance_api_message)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class BalanceTransferReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equals(getString(R.string.balance_transfered_successful))) {
                    mTvBalance.setText("$" + mPreferenceProvider.getPrefString(getString(R.string.dialpad_avail_bal)));
                    String username1 = CSDataProvider.getLoginID();
                    String password1 = CSDataProvider.getPassword();

                    String pwd = username1 + password1;

                    LOG.debug("Password dialpad:" + pwd);
                    String actualPassword = "";
                    try {
                        actualPassword = Utils.generateSHA256(pwd);
                        LOG.debug("Password after SHA dialpad:" + actualPassword);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String url = Constants.BALANCE_URL + "+" + userName + "?loginusername%2B" + userName + "&password=" + actualPassword;
                    new APITask(url, getString(R.string.dialpad_balance_api_message)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if (mEtDialpadNumber.getText().toString().length() > 0 && mTvCountryCode.getText().toString().length() > 0) {
                mPreferenceProvider.setPrefString("DialpadNumber", mEtDialpadNumber.getText().toString());
                mPreferenceProvider.setPrefString("DialpadNumberCountry", mTvCountryCode.getText().toString());
            } else if (mEtDialpadNumber.getText().toString().length() > 0) {
                mPreferenceProvider.setPrefString("DialpadNumber", mEtDialpadNumber.getText().toString());
            } else if (mTvCountryCode.getText().toString().length() > 0) {
                mPreferenceProvider.setPrefString("DialpadNumberCountry", mTvCountryCode.getText().toString());
            } else {
                mPreferenceProvider.setPrefString("DialpadNumber", null);
                mPreferenceProvider.setPrefString("DialpadNumberCountry", null);
            }

            try {
                if (balanceTransferReceiver != null) {
                    LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(balanceTransferReceiver);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
