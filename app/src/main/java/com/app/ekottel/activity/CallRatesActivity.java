package com.app.ekottel.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.model.CallRatesDB;
import com.app.ekottel.model.CallRatesModel;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.HTTPUtils;
import com.app.ekottel.utils.Utils;
import com.ca.wrapper.CSDataProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ramesh.u on 3/29/2018.
 */

public class CallRatesActivity extends AppCompatActivity {
    private TextView mTvCallRatesHeader, mTvNoCallRates;
    private ProgressBar pBCallRates;
    LinearLayout mLlHeader;
    AlertDialog dismissSuccessfullyLogin;
    androidx.appcompat.app.AlertDialog.Builder successfullyLogin;
    private String TAG;

    List<CallRatesModel> callRatesList = new ArrayList<CallRatesModel>();
    List<CallRatesModel> callRatesModelsList = new ArrayList<CallRatesModel>();
    TableLayout tl;
    private String username = "";
    private String password = "";
    private String value = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_rates_activity);
        TAG = "CallRatesActivity";
        mTvCallRatesHeader = (TextView) findViewById(R.id.tv_connection_fee_header);
        mTvNoCallRates = (TextView) findViewById(R.id.tv_call_rates_loading);
        mTvNoCallRates.setVisibility(View.GONE);
        mLlHeader = (LinearLayout) findViewById(R.id.ll_call_rates_header_items);
        pBCallRates = (ProgressBar) findViewById(R.id.pb_call_rates);
        pBCallRates.setVisibility(View.GONE);

        tl = (TableLayout) findViewById(R.id.tl_call_rates);


        TextView tv_prefix = (TextView) findViewById(R.id.tv_prefix);
        TextView tv_call_rate = (TextView) findViewById(R.id.tv_call_rate_text);
        TextView tv_connection_fee = (TextView) findViewById(R.id.tv_connection_fee);

        Intent intent = getIntent();
        value = intent.getStringExtra("CallRateValue");

        mLlHeader.setVisibility(View.GONE);
        mTvNoCallRates.setText(getString(R.string.no_call_rates));
        Typeface webTypeFace = Utils.getTypeface(getApplicationContext());
        LinearLayout ll_back = (LinearLayout) findViewById(R.id.ll_call_rates_back_arrow);
        TextView back = (TextView) findViewById(R.id.tv_call_rates_back_arrow);
        back.setTypeface(webTypeFace);
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if (!Utils.getNetwork(CallRatesActivity.this)) {
            Toast.makeText(CallRatesActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
            return;
        }

        if (!HomeScreenActivity.status.equalsIgnoreCase(getString(R.string.registered))) {
            Toast.makeText(CallRatesActivity.this, getString(R.string.registered_message), Toast.LENGTH_LONG).show();

            return;
        }

        username = CSDataProvider.getLoginID();
        //username = username.replace("+", "");
        password = CSDataProvider.getPassword();


        /*if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {*/

        String pwd = username+password;

        LOG.debug(TAG,"Password"+pwd);
        String actualPassword="";
        try {
            actualPassword = Utils.generateSHA256(pwd);
            LOG.debug(TAG,"Password after SHA"+actualPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        username = username.replace("+", "");
            //String url = "http://182.72.244.91:9294/api/countrycost/"+ value + "?username=" + "dileep" + "&password=" + "dileep"+"&list=1";
            String url = Constants.CALL_RATES_URL + value + "?username=%2B" + username + "&password=" + actualPassword + "&list=1";
            new APITask(url, getString(R.string.dialpad_call_rates_api_message)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
       /* }*/


    }


    //Handle all rest API calls
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
            if (pBCallRates != null)
                pBCallRates.setVisibility(View.VISIBLE);

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
     * This is used for update UI whatever response comes
     *
     * @param api        this is used for get data
     * @param returndata this is used for provide api needed information
     */
    private void processResponse(String api, String returndata) {


        String prefix = "";
        String callCost = "";
        String connectionFee = "";


        try {
            JSONObject jsonObj = new JSONObject(returndata);
            JSONArray array = jsonObj.getJSONObject(getString(R.string.http_json_response_data_key)).getJSONArray(getString(R.string.http_json_response_message_key));
            LOG.info("array length:" + array.length());

            callRatesList.clear();

            for (int i = 0; i < array.length(); i++) {

                try {

                    CallRatesModel callRatesModelPojo = new CallRatesModel();

                    prefix = array.getJSONObject(i).getString("country");
                    callCost = array.getJSONObject(i).getString("cost");
                    connectionFee = array.getJSONObject(i).getString("connectionfee");


                    callRatesModelPojo.setPrefix(prefix);
                    callRatesModelPojo.setCallRate(callCost);
                    callRatesModelPojo.setConnectionFee(connectionFee);
                    callRatesList.add(callRatesModelPojo);


                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            }

        } catch (Exception ex) {
            if (pBCallRates != null)
                pBCallRates.setVisibility(View.GONE);

            if (returndata != null && returndata.contains("User DeActivated")) {
                showAlert("User DeActivated");
            } else {
                showAlert("Please try again later");
            }
            ex.printStackTrace();
        }

        tl.removeAllViews();

        //Collections.reverse(callRatesList);
        addLocalDB(callRatesList);


    }

    //Update local database whatever response comes
    private void addLocalDB(List<CallRatesModel> callRatesModelList) {

        CallRatesDB callRatesDB = new CallRatesDB(
                getApplicationContext());

        callRatesDB.deleteTable();
        if (callRatesModelList != null && callRatesModelList.size() > 0) {
            callRatesDB.addContact(callRatesModelList);
        }
        if (pBCallRates != null)
            pBCallRates.setVisibility(View.GONE);

        addData();

    }

    //This is used for update UI
    public void addData() {


        CallRatesDB callRatesDB = new CallRatesDB(getApplicationContext());
        callRatesModelsList = callRatesDB.getAllRecords();

        if (callRatesModelsList != null && callRatesModelsList.size() > 0) {
            mLlHeader.setVisibility(View.VISIBLE);
            tl.setVisibility(View.VISIBLE);
            mTvNoCallRates.setVisibility(View.GONE);

            for (int i = 0; i < callRatesModelsList.size(); i++) {
                /** Create a TableRow dynamically **/

                final View tableRow = LayoutInflater.from(this).inflate(R.layout.call_rates_items, null, false);

                TextView mTvPrefix = (TextView) tableRow.findViewById(R.id.tv_item_prefix);
                TextView mTvCallRate = (TextView) tableRow.findViewById(R.id.tv_item_call_rate);
                TextView mTvConnectionFee = (TextView) tableRow.findViewById(R.id.tv_item_connection_fee);
                mTvPrefix.setText(callRatesModelsList.get((i)).getPrefix());
                mTvCallRate.setText("$" + callRatesModelsList.get(i).getCallRate());
                mTvConnectionFee.setText(callRatesModelsList.get(i).getConnectionFee());

                tl.addView(tableRow);


            }
        } else {
            mLlHeader.setVisibility(View.GONE);
            tl.setVisibility(View.INVISIBLE);
            mTvNoCallRates.setVisibility(View.VISIBLE);
            mTvNoCallRates.setText(getString(R.string.no_call_rates));
        }


    }

    /**
     * @param result this is a message for showing alert dialog
     * @return which returns boolean value
     */
    public boolean showAlert(String result) {
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
                    if (dismissSuccessfullyLogin != null) {
                        dismissSuccessfullyLogin.cancel();
                    }
                    finish();
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

}
