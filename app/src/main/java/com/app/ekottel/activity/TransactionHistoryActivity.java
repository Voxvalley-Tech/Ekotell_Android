package com.app.ekottel.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.model.RechargeHistoryPojo;
import com.app.ekottel.model.TransactionHistoryDB;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.HTTPUtils;
import com.app.ekottel.utils.Utils;
import com.ca.wrapper.CSDataProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This activity is used to display transaction history information.
 *
 * @author Ramesh U
 * @version 2017
 */
public class TransactionHistoryActivity extends AppCompatActivity {
    private TextView mTvTransactionHistoryHeader;
    private TextView mTvNoHistory;

    TableLayout tl;
    int numberOfComments = 0;
    List<RechargeHistoryPojo> rechargeHistoryList = new ArrayList<RechargeHistoryPojo>();
    List<RechargeHistoryPojo> transactionHistoryList = new ArrayList<RechargeHistoryPojo>();
    List<RechargeHistoryPojo> actualTransactionHistoryList = new ArrayList<RechargeHistoryPojo>();
    LinearLayout mLlHeader;
    private String userName = "";
    private String password = "";
    private String TAG;
    ProgressBar pb_transaction_history;
    private LinearLayout mLlDropDown;
    private TextView mTvType, mTvDropArrow;
    private RelativeLayout mRlDropArrow;
    PopupWindow popUpWindowObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        TAG = getString(R.string.transaction_tag);
        mTvTransactionHistoryHeader = (TextView) findViewById(R.id.tv_transaction_history_header);
        mTvNoHistory = (TextView) findViewById(R.id.tv_transaction_history_loading);
        mTvNoHistory.setVisibility(View.GONE);
        tl = (TableLayout) findViewById(R.id.tl_recharge_history);
        mTvType = (TextView) findViewById(R.id.tv_filter_type);
        mTvDropArrow = (TextView) findViewById(R.id.tv_drop_arrow);
        mLlDropDown = (LinearLayout) findViewById(R.id.ll_drop_down);
        mRlDropArrow = (RelativeLayout) findViewById(R.id.rl_drop_down);

        numberOfComments = getIntent().getIntExtra(getString(R.string.transaction_intent_count), 0);

        mLlHeader = (LinearLayout) findViewById(R.id.ll_header_items);
        pb_transaction_history = (ProgressBar) findViewById(R.id.pb_transaction_history);
        pb_transaction_history.setVisibility(View.GONE);


        userName = CSDataProvider.getLoginID();
        userName = userName.replace("+", "");
        password = CSDataProvider.getPassword();

        TextView history_display_date = (TextView) findViewById(R.id.tv_trns_date);
        TextView history_display_credits = (TextView) findViewById(R.id.tv_trns_credit);
        TextView history_display_type = (TextView) findViewById(R.id.tv_trns_type);
        TextView history_display_amount = (TextView) findViewById(R.id.tv_trns_cost);

        history_display_date.setText(getString(R.string.transaction_header_date));

        history_display_credits.setText(getString(R.string.transaction_header_type));
        history_display_type.setText(getString(R.string.transaction_header_mode));

        history_display_amount.setText(getString(R.string.transaction_header_amount));

        Typeface webTypeFace = Utils.getTypeface(getApplicationContext());
        LinearLayout ll_back = (LinearLayout) findViewById(R.id.ll_transaction_history_back_arrow);
        TextView back = (TextView) findViewById(R.id.tv_transaction_history_back_arrow);
        back.setTypeface(webTypeFace);
        mTvDropArrow.setTypeface(webTypeFace);
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        mLlHeader.setVisibility(View.GONE);
        //mTvNoHistory.setVisibility(View.VISIBLE);
        mTvNoHistory.setText(getString(R.string.transaction_loading_message));

        mLlDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUpWindowObj = dialog_Transaction(TransactionHistoryActivity.this, mLlDropDown);
                popUpWindowObj.showAsDropDown(v);
            }
        });

        // additems();

        //addData();
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
        String url = Constants.TRANSACTION_HISTORY_URL + "%2B" + userName + "?password=" + actualPassword + "&offSet=0&size=100";
        new APITask(url, getString(R.string.more_activity_transaction_api_value)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //Update local database whatever response comes
    private void addLocalDB(List<RechargeHistoryPojo> rechargeHistoryList) {

        TransactionHistoryDB transactionHistoryDB = new TransactionHistoryDB(
                getApplicationContext());

        transactionHistoryDB.deleteTable();
        if (rechargeHistoryList != null && rechargeHistoryList.size() > 0) {
            transactionHistoryDB.addContact(rechargeHistoryList);
        }
        if (pb_transaction_history != null)
            pb_transaction_history.setVisibility(View.GONE);

        TransactionHistoryDB rechargeHistoryDB = new TransactionHistoryDB(getApplicationContext());
        transactionHistoryList = rechargeHistoryDB.getAllRecords();

        addData(transactionHistoryList);

    }

    //This is used for update UI
    public void addData(final List<RechargeHistoryPojo> transactionHistoryList) {


        if (transactionHistoryList != null && transactionHistoryList.size() > 0) {
            mLlHeader.setVisibility(View.VISIBLE);

            mRlDropArrow.setVisibility(View.VISIBLE);
            tl.setVisibility(View.VISIBLE);
            tl.removeAllViews();
            mTvNoHistory.setVisibility(View.GONE);

            for (int i = 0; i < transactionHistoryList.size(); i++) {
                /** Create a TableRow dynamically **/

                final View tableRow = LayoutInflater.from(this).inflate(R.layout.trxnhistory, null, false);

                TextView history_display_date = (TextView) tableRow.findViewById(R.id.tv_trns_item_date);
                TextView history_display_credits = (TextView) tableRow.findViewById(R.id.tv_trns_item_credit);
                TextView history_display_type = (TextView) tableRow.findViewById(R.id.tv_trns_item_type);
                TextView history_display_cost = (TextView) tableRow.findViewById(R.id.tv_trns_item_cost);
                TableRow tr_row = (TableRow) tableRow.findViewById(R.id.tr_transaction_history);


                String actualDate = "";
                SimpleDateFormat currentdf = new SimpleDateFormat(getString(R.string.transaction_curent_date_format));

                try {
                    SimpleDateFormat of = new SimpleDateFormat(getString(R.string.transaction_require_format));
                    actualDate = of.format(currentdf.parse(transactionHistoryList.get(i).getDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                history_display_date.setText(actualDate);
                history_display_type.setText(transactionHistoryList.get(i).getType());

                try {
                    String cost = new BigDecimal(transactionHistoryList.get(i).getCost()).toPlainString();
                    history_display_cost.setText(cost);
                } catch (Exception e) {
                    history_display_cost.setText(transactionHistoryList.get(i).getCost());
                    e.printStackTrace();
                }

                if (transactionHistoryList.get(i).getCost() != null && transactionHistoryList.get(i).getCost().startsWith("-")) {
                    history_display_credits.setText(getString(R.string.transaction_json_array_key_debit));
                } else {
                    history_display_credits.setText(getString(R.string.transaction_json_array_key_credit));
                }

                tr_row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {

                            int clicked_id = tl.indexOfChild(tableRow);


                            String finalstring = "";

                            LOG.info("Row ID" + clicked_id);


                            String cost = transactionHistoryList.get(clicked_id).getCost();

                            String date = transactionHistoryList.get(clicked_id).getDate();
                            String mode = transactionHistoryList.get(clicked_id).getType();
                            String from = transactionHistoryList.get(clicked_id).getFromuser();
                            String to = transactionHistoryList.get(clicked_id).getTouser();
                            String type = "";
                            if (cost != null && cost.startsWith("-")) {
                                type = getString(R.string.transaction_json_array_key_debit);
                            } else {
                                type = getString(R.string.transaction_json_array_key_credit);
                            }
                            String txn_cost = "";
                            try {
                                txn_cost = new BigDecimal(cost).toPlainString();

                            } catch (Exception e) {
                                txn_cost = cost;
                                e.printStackTrace();
                            }

                            if (from != null && !from.isEmpty()) {
                                finalstring = type + " : " + txn_cost + "\n" + getString(R.string.transaction_popup_date) + date + "\n" + getString(R.string.transaction_popup_mode) + mode + "\n" + getString(R.string.transaction_popup_from) + from + "\n" + getString(R.string.transaction_popup_to) + to;
                            } else {
                                finalstring = type + " : " + txn_cost + "\n" + getString(R.string.transaction_popup_date) + date + "\n" + getString(R.string.transaction_popup_mode) + mode + "\n" + getString(R.string.transaction_popup_to) + to;
                            }


                            showalert(finalstring);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                tl.addView(tableRow);


            }
        } else {
            mLlHeader.setVisibility(View.GONE);
            if (actualTransactionHistoryList != null && actualTransactionHistoryList.size() > 0) {
                mRlDropArrow.setVisibility(View.VISIBLE);
            } else {
                mRlDropArrow.setVisibility(View.GONE);
            }

            tl.setVisibility(View.INVISIBLE);
            mTvNoHistory.setVisibility(View.VISIBLE);
            mTvNoHistory.setText(getString(R.string.transaction_no_transaction_found));
        }


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
            if (pb_transaction_history != null)
                pb_transaction_history.setVisibility(View.VISIBLE);

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

        if (api.equals(getString(R.string.transaction_api_key))) {

            String id = "";
            String username = "";
            String from = "";
            String type = "";
            String amount = "";
            String creationdate = "";
            String mode = "";
            String remarks = "";


            try {
                JSONObject jsonObj = new JSONObject(returndata);
                JSONArray array = jsonObj.getJSONObject(getString(R.string.http_json_response_data_key)).getJSONArray(getString(R.string.http_json_response_message_key));
                //LOG.info("array length:" + array.length());

                rechargeHistoryList.clear();
                actualTransactionHistoryList.clear();

                for (int i = 0; i < array.length(); i++) {

                    try {

                        RechargeHistoryPojo rechargeHistoryPojo = new RechargeHistoryPojo();

                        id = array.getJSONObject(i).getString(getString(R.string.transaction_json_array_key_id));
                        username = array.getJSONObject(i).getString(getString(R.string.transaction_json_array_key_username));
                        from = array.getJSONObject(i).getString(getString(R.string.transaction_json_array_key_from_name));
                        type = getString(R.string.transaction_json_array_key_debit);
                        amount = array.getJSONObject(i).getString(getString(R.string.transaction_json_array_key_from_cost));
                                        /*if(Integer.parseInt(amount)>0){
                                            type="credit";
                                        }*/
                        creationdate = array.getJSONObject(i).getString(getString(R.string.transaction_json_array_key_creation_date));
                        mode = array.getJSONObject(i).getString(getString(R.string.transaction_json_array_key_type));
                        remarks = "";

                        rechargeHistoryPojo.setCost(amount);
                        rechargeHistoryPojo.setDate(creationdate);
                        rechargeHistoryPojo.setType(mode);
                        rechargeHistoryPojo.setFromuser(from);
                        rechargeHistoryPojo.setTouser(username);
                        rechargeHistoryList.add(rechargeHistoryPojo);
                        actualTransactionHistoryList.add(rechargeHistoryPojo);


                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    LOG.info("Username is " + username + " amount " + amount + " time is " + creationdate);

                    if (id == null) {
                        id = "";
                    }
                    if (username == null) {
                        username = "";
                    }

                    if (amount == null) {
                        amount = "";
                    }
                    if (creationdate == null) {
                        creationdate = "";
                    }
                    if (mode == null) {
                        mode = "";
                    }
                    if (remarks == null) {
                        remarks = "";
                    }

                    LOG.info("id:" + id);
                    LOG.info("username:" + username);
                    LOG.info("type:" + type);
                    LOG.info("amount:" + amount);
                    LOG.info("creationdate:" + creationdate);
                    LOG.info("mode:" + mode);
                    LOG.info("remarks:" + remarks);

                    //IAmLiveDB.insertPaymenthistory(id, type, creationdate, mode, username, amount, remarks);


                }

            } catch (Exception ex) {
                if (pb_transaction_history != null)
                    pb_transaction_history.setVisibility(View.GONE);
                ex.printStackTrace();
            }

            tl.removeAllViews();
            Collections.sort(rechargeHistoryList);
            Collections.reverse(rechargeHistoryList);

            addLocalDB(rechargeHistoryList);

        }
    }


    /**
     * Handle individual transaction details
     *
     * @param result this is used for display message
     * @return which returns boolean value
     */
    public boolean showalert(String result) {
        try {
            android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(TransactionHistoryActivity.this);
            successfullyLogin.setTitle(getString(R.string.transaction_popup_title));
            successfullyLogin.setMessage(result);
            successfullyLogin.setPositiveButton(getString(R.string.splash_network_alert_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            if (dialog != null)
                                dialog.dismiss();
                        }
                    });


            successfullyLogin.show();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }


    //This is used to share app and details of specific contact
    private PopupWindow dialog_Transaction(final Context context, LinearLayout lin) {

        final PopupWindow dialog_Select = new PopupWindow(context);

        View v = View.inflate(context, R.layout.transaction_popup, null);


        dialog_Select.setContentView(v);
        dialog_Select.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        dialog_Select.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        dialog_Select.setFocusable(true);
        dialog_Select.setBackgroundDrawable(new BitmapDrawable(
                context.getApplicationContext().getResources(),
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)));


        LinearLayout ll_all = (LinearLayout) v.findViewById(R.id.ll_transaction_all);
        LinearLayout ll_transfer = (LinearLayout) v.findViewById(R.id.ll_transaction_transfer);
        LinearLayout ll_recharge = (LinearLayout) v.findViewById(R.id.ll_transaction_recharge);
        LinearLayout ll_package = (LinearLayout) v.findViewById(R.id.ll_transaction_package);
        LinearLayout ll_paypal = (LinearLayout) v.findViewById(R.id.ll_transaction_paypal);

        TextView tv_all = (TextView) v.findViewById(R.id.tv_transaction_all);
        TextView tv_transfer = (TextView) v.findViewById(R.id.tv_transaction_transfer);
        TextView tv_recharge = (TextView) v.findViewById(R.id.tv_transaction_recharge);
        TextView tv_package = (TextView) v.findViewById(R.id.tv_transaction_package);
        TextView tv_paypal = (TextView) v.findViewById(R.id.tv_transaction_paypal);


        ll_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null)
                    dialog_Select.dismiss();
                mTvType.setText("All");
                transactionHistoryList.clear();
                TransactionHistoryDB rechargeHistoryDB = new TransactionHistoryDB(getApplicationContext());
                transactionHistoryList = rechargeHistoryDB.getAllRecords();

                addData(transactionHistoryList);

            }
        });

        ll_transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null)
                    dialog_Select.dismiss();
                mTvType.setText("Transfer");
                transactionHistoryList.clear();
                TransactionHistoryDB rechargeHistoryDB = new TransactionHistoryDB(getApplicationContext());
                transactionHistoryList = rechargeHistoryDB.getFilteredRecords("transfer");

                addData(transactionHistoryList);


            }
        });
        ll_recharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null)
                    dialog_Select.dismiss();
                mTvType.setText("Recharge");
                transactionHistoryList.clear();
                TransactionHistoryDB rechargeHistoryDB = new TransactionHistoryDB(getApplicationContext());
                transactionHistoryList = rechargeHistoryDB.getFilteredRecords("recharge");

                addData(transactionHistoryList);


            }
        });

        ll_package.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null)
                    dialog_Select.dismiss();
                mTvType.setText("Package");
                transactionHistoryList.clear();
                TransactionHistoryDB rechargeHistoryDB = new TransactionHistoryDB(getApplicationContext());
                transactionHistoryList = rechargeHistoryDB.getFilteredRecords("package");

                addData(transactionHistoryList);


            }
        });

        ll_paypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null)
                    dialog_Select.dismiss();

                mTvType.setText("Paypal");
                transactionHistoryList.clear();
                TransactionHistoryDB rechargeHistoryDB = new TransactionHistoryDB(getApplicationContext());
                transactionHistoryList = rechargeHistoryDB.getFilteredRecords("paypal");

                addData(transactionHistoryList);

            }
        });


        Rect location = locateView(lin);

        dialog_Select.showAtLocation(v, Gravity.TOP | Gravity.RIGHT, 0, location.bottom);

        // Getting a reference to Close button, and close the popup when clicked.

        return dialog_Select;

    }

    //This is used for specific place show popup view
    public static Rect locateView(View v) {
        int[] loc_int = new int[2];
        if (v == null) return null;
        try {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
