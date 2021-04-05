package com.app.ekottel.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.activity.BalanceTransferActivity;
import com.app.ekottel.activity.CallRatesActivity;
import com.app.ekottel.activity.DialpadActivity;
import com.app.ekottel.activity.HomeScreenActivity;
import com.app.ekottel.activity.PrivacyPolicyActivity;
import com.app.ekottel.activity.ResetPasswordActivity;
import com.app.ekottel.activity.SubscriberPackagesActivity;
import com.app.ekottel.activity.SupportActivity;
import com.app.ekottel.activity.TopUpActivity;
import com.app.ekottel.activity.TransactionHistoryActivity;
import com.app.ekottel.activity.ViewMyPromotionsActivity;
import com.app.ekottel.activity.VoucherRechargeActivity;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.HTTPUtils;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ramesh.u on 11/13/2017.
 */

public class MoreFragment extends Fragment {

    private Typeface webTypeFace;
    private int[] icons = { R.drawable.ic_subscribe_packages, R.drawable.ic_voucher_recharge,R.drawable.ic_balance_transfer, R.drawable.ic_transaction_history, R.drawable.ic_support, R.drawable.ic_notifications, R.drawable.ic_invite_friend, R.drawable.ic_reset_password, R.drawable.ic_sign_out};
    private String[] titles;
    private MoreAdapter adapter;
    private ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();
    private Runnable progressBarRunnable;
    int progressBarTimerDelay = 30000;
    private InputMethodManager mInputMethodManager;
    public static Dialog mPopupCloseDialog = null;
    public static boolean mCallRatesCalled = false;
    private String username = "";
    private String password = "";
    private String TAG;
    private View mMoreView;
    public static boolean isAlertClick = false;
    private long mLastClickTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (mMoreView == null) {
            mMoreView = inflater.inflate(R.layout.fragment_more, container, false);
            TAG = getString(R.string.more_activity_tag);
            titles = new String[]{ getString(R.string.more_activity_list_subscribe_package),getString(R.string.more_activity_list_voucher_recharge), getString(R.string.more_activity_list_balance_transfer), getString(R.string.more_activity_list_transaction_history), getString(R.string.more_activity_list_support), getString(R.string.more_activity_list_notifications), getString(R.string.more_activity_list_invite_friend), getString(R.string.more_activity_list_reset_password), getString(R.string.more_activity_list_logout)};
            ListView moreList = mMoreView.findViewById(R.id.more_list);
            ImageView dialpad = mMoreView.findViewById(R.id.dialpad);
            webTypeFace = Utils.getTypeface(getActivity());
            adapter = new MoreAdapter(titles, icons);
            moreList.setAdapter(adapter);

            username = CSDataProvider.getLoginID();
            username = username.replace("+", "");
            password = CSDataProvider.getPassword();

            mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            moreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0:
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            startActivity(new Intent(getActivity(), SubscriberPackagesActivity.class));


                            break;

                        case 1:
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            startActivity(new Intent(getActivity(), VoucherRechargeActivity.class));


                            break;
                        case 2:
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            startActivity(new Intent(getActivity(), BalanceTransferActivity.class));

                           /* if (!Utils.getNetwork(getActivity())) {

                                Toast.makeText(getActivity(), getString(R.string.network_unavail_message), Toast.LENGTH_LONG).show();

                                return;
                            }

                            if (!HomeScreenActivity.status.equalsIgnoreCase(getString(R.string.registered))) {
                                Toast.makeText(getActivity(), getString(R.string.registered_message), Toast.LENGTH_LONG).show();

                                return;
                            }
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            showCallRates();*/
                            break;
                        case 3:
                            if (!Utils.getNetwork(getActivity())) {

                                Toast.makeText(getActivity(), getString(R.string.network_unavail_message), Toast.LENGTH_LONG).show();

                                return;
                            }

                            if (!HomeScreenActivity.status.equalsIgnoreCase(getString(R.string.registered))) {
                                Toast.makeText(getActivity(), getString(R.string.registered_message), Toast.LENGTH_LONG).show();

                                return;
                            }

                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            startActivity(new Intent(getActivity(), TransactionHistoryActivity.class));


                            break;
                        case 4:
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            startActivity(new Intent(getActivity(), SupportActivity.class));


                            break;
                        case 5:
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            startActivity(new Intent(getActivity(), ViewMyPromotionsActivity.class));


                            break;
                        case 6:


                            if (!Utils.getNetwork(getActivity())) {

                                Toast.makeText(getActivity(), getString(R.string.internet_error), Toast.LENGTH_LONG).show();

                                return;
                            }
                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            List<Intent> targetShareIntents = new ArrayList<Intent>();
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            PackageManager pm = getActivity().getPackageManager();
                            List<ResolveInfo> resInfos = pm.queryIntentActivities(shareIntent, 0);
                            if (!resInfos.isEmpty() && !isAlertClick) {

                                isAlertClick = true;
                                System.out.println("Have package");
                                for (ResolveInfo resInfo : resInfos) {
                                    String packageName = resInfo.activityInfo.packageName;
                                    LOG.info("Package Name", packageName);

                                    if (packageName.contains("com.twitter.android") || packageName.contains("com.facebook.katana")
                                            || packageName.contains("com.whatsapp") || packageName.contains("com.google.android.apps.plus")
                                            || packageName.contains("com.google.android.talk") || packageName.contains("com.slack")
                                            || packageName.contains("com.google.android.gm") || packageName.contains("com.facebook.orca")
                                            || packageName.contains("com.yahoo.mobile") || packageName.contains("com.skype.raider")
                                            || packageName.contains("com.android.mms") || packageName.contains("com.linkedin.android")
                                            || packageName.contains("com.google.android.apps.messaging")) {
                                        Intent intent = new Intent();

                                        intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                                        intent.putExtra("AppName", resInfo.loadLabel(pm).toString());
                                        intent.setAction(Intent.ACTION_SEND);
                                        intent.setType("text/plain");
                                        intent.putExtra(Intent.EXTRA_TEXT, "Check out jibicall application for your smartphone. which allows to offer end-to-end mobile VoIP communication services such as voice, video and text. Download it today from  https://play.google.com/store/apps/details?id=com.app.ekotell");
                                        intent.putExtra(Intent.EXTRA_SUBJECT, "Android + iPhone ( Ultimate Calling Experience )");
                                        intent.setPackage(packageName);
                                        targetShareIntents.add(intent);
                                    }
                                }
                                if (!targetShareIntents.isEmpty()) {
                                    Collections.sort(targetShareIntents, new Comparator<Intent>() {
                                        @Override
                                        public int compare(Intent o1, Intent o2) {
                                            return o1.getStringExtra("AppName").compareTo(o2.getStringExtra("AppName"));
                                        }
                                    });
                                    Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), "Invite a friend via...");
                                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                                    startActivity(chooserIntent);
                                } else {
                                    Toast.makeText(getActivity(), "No app to invite a friend.", Toast.LENGTH_LONG).show();
                                }


                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        isAlertClick = false;
                                    }
                                }, 2000);
                            }
                            break;
                        case 7:

                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            startActivity(new Intent(getActivity(), ResetPasswordActivity.class));


                            break;
                        case 8:

                            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                return;
                            }
                            mLastClickTime = SystemClock.elapsedRealtime();
                            invokeCloseDialog("Do you really want to SignOut");


                            break;
                    }
                }
            });
            dialpad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), DialpadActivity.class));
                }
            });

        }
        return mMoreView;
    }

    /**
     * This is used for loading featured messages
     */
    class MoreAdapter extends ArrayAdapter<String> {

        private String[] titles;
        private int[] icons;

        public MoreAdapter(String[] titles, int[] icons) {
            super(getActivity(), R.layout.more_list_item, titles);
            this.titles = titles;
            this.icons = icons;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.more_list_item, null);
            }
            ImageView icon = convertView.findViewById(R.id.icon);
            TextView title = convertView.findViewById(R.id.title);
            TextView go = convertView.findViewById(R.id.next);
            Typeface text_regular = Utils.getTypeface(getActivity());
            go.setTypeface(webTypeFace);
            icon.setImageResource(icons[position]);
            title.setText(titles[position]);
            return convertView;
        }
    }

    /**
     * This AsyncTask will handle the API calls
     */
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
            if (!api.equals(getString(R.string.more_activity_transaction_api_value))) {
                dismissProgressbar();
            }
            processResponse(api, response);
        }
    }

    private void processResponse(String api, String returndata) {

        if (!isAdded()) {
            return;
        }

        if (api.equals(getString(R.string.more_activity_paypal_api_value))) {
            LOG.info("returndata:" + returndata);

            try {
                String[] strs = returndata.split("\"");
               // GlobalVariables.CONFIG_CLIENT_ID = strs[3];
                GlobalVariables.CONFIG_CLIENT_ID = strs[3];
            } catch (Exception ex) {
                ex.printStackTrace();
               // GlobalVariables.CONFIG_CLIENT_ID = getString(R.string.more_activity_testid);
            }
           /* if (GlobalVariables.CONFIG_CLIENT_ID == null || GlobalVariables.CONFIG_CLIENT_ID.equals("")) {
                GlobalVariables.CONFIG_CLIENT_ID = getString(R.string.more_activity_testid);
            }*/
            LOG.info("Config client id:" + GlobalVariables.CONFIG_CLIENT_ID);
            Intent intent = new Intent(getActivity(),TopUpActivity.class);
            intent.putExtra("PaymentMode","Paypal");
            startActivity(intent);


        } else if (api.equals(getString(R.string.dialpad_call_rates_api_message))) {
            try {

                mCallRatesCalled = false;
                LOG.info("returndata:" + returndata);
                JSONObject jsonObj = new JSONObject(returndata);
                String message = "";
                JSONArray array = jsonObj.getJSONObject(getString(R.string.dialpad_response_message_key)).getJSONArray(getString(R.string.dialpad_response_message_value));
                LOG.info("array length:" + array.length());
                for (int i = 0; i < array.length(); i++) {
                    message = array.getJSONObject(i).getString(getString(R.string.dialpad_response_cost_key));
                }

                if (message.equals("")) {
                    showAlert(getString(R.string.more_activity_no_call_rates_found));
                } else {
                    showAlert("$" + message);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            mCallRatesCalled = false;
            showAlert(getString(R.string.login_failed_message));
        }
    }


    /**
     * Handle to display progress dialog
     */
    public void showProgressbar() {
        try {
            progressBarStatus = 0;
            progressBar = new ProgressDialog(getActivity());
            progressBar.setCancelable(false);
            progressBar.setMessage(getString(R.string.bal_trans_wait_message));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setProgress(0);
            progressBar.show();

            progressBarHandler = new Handler();
            progressBarRunnable = new Runnable() {

                public void run() {
                    progressBarHandler.postDelayed(this, progressBarTimerDelay);

                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            dismissProgressbar();
                            Toast.makeText(getActivity(), getString(R.string.network_message), Toast.LENGTH_SHORT).show();


                        }
                    });
                }
            };
            progressBarHandler.postDelayed(progressBarRunnable, progressBarTimerDelay);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Handle to dismiss progress dialog
     */
    public void dismissProgressbar() {
        try {
            LOG.info("dismissProgressBar");
            if (progressBar != null) {
                progressBar.dismiss();

            }
            if (progressBarHandler != null) {
                progressBarHandler.removeCallbacks(progressBarRunnable);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        try {
            if (!getUserVisibleHint()) {
                return;
            }
            LOG.info("Mainactivity on resume is called More");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * This is used for update UI whatever call back comes
     *
     * @param str
     */
    public void updateUI(String str) {

        try {

            if (str.equals(getString(R.string.network_message))) {
                LOG.info("NetworkError received");

                dismissProgressbar();

            } else if (str.equals(getString(R.string.error))) {
                LOG.info("Error receieved");
                dismissProgressbar();
                Toast.makeText(getActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public boolean showAlert(String result) {
        try {
            android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(getActivity());
            successfullyLogin.setTitle(result);
            successfullyLogin.setPositiveButton(getString(R.string.splash_network_alert_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {

                            if (dialog != null)
                                dialog.dismiss();

                        }
                    });

            successfullyLogin.setNegativeButton(getString(R.string.status_alert_btn_cancel),
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

    private void invokeCloseDialog(final String message) {

        if (mPopupCloseDialog == null) {
            try {
                mPopupCloseDialog = new Dialog(getActivity());
                mPopupCloseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mPopupCloseDialog.setContentView(R.layout.alertdialog_close);
                mPopupCloseDialog.setCancelable(false);
                mPopupCloseDialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));


                TextView yes = (TextView) mPopupCloseDialog
                        .findViewById(R.id.btn_alert_ok);
                TextView no = (TextView) mPopupCloseDialog
                        .findViewById(R.id.btn_alert_cancel);


                TextView tv_title = (TextView) mPopupCloseDialog
                        .findViewById(R.id.tv_alert_title);
                TextView tv_message = (TextView) mPopupCloseDialog
                        .findViewById(R.id.tv_alert_message);
                tv_message.setText(message);




                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        PreferenceProvider pf = new PreferenceProvider(getActivity());
                        pf.setPrefboolean(getString(R.string.splash_pref_is_already_login), false);

                        try {

                            CSClient mCSClient = new CSClient();
                            mCSClient.reset();
                           /* if (PermissionUtils.checkReadContactsPermission(getActivity())) {
                                Utils.addContactsToSdk(getContext().getApplicationContext());
                            }*/
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        mPopupCloseDialog.dismiss();
                        mPopupCloseDialog = null;
                        Intent intent = new Intent(getActivity(), PrivacyPolicyActivity.class);
                        startActivity(intent);
                        getActivity().finish();


                    }
                });
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mPopupCloseDialog.dismiss();
                        mPopupCloseDialog = null;

                    }
                });

                if (mPopupCloseDialog != null)
                    mPopupCloseDialog.show();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * This method is handle call rates provide mobile number
     */
    public void showCallRates() {
        try {

            if (mPopupCloseDialog == null) {
                mPopupCloseDialog = new Dialog(getActivity());

                mPopupCloseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mPopupCloseDialog.setContentView(R.layout.call_rates_alert);
                mPopupCloseDialog.setCancelable(false);
                mPopupCloseDialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));


                Button yes = (Button) mPopupCloseDialog
                        .findViewById(R.id.btn_callrates_alert_ok);
                Button no = (Button) mPopupCloseDialog
                        .findViewById(R.id.btn_callrates_alert_cancel);
                final EditText et_number = (EditText) mPopupCloseDialog
                        .findViewById(R.id.et_callrates_number);


                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String value = et_number.getText().toString();
                        LOG.info("value:" + value);



                        if (value != null && value.length() > 0) {
                            mPopupCloseDialog.dismiss();
                            mPopupCloseDialog = null;
                            Intent intent = new Intent(getActivity(), CallRatesActivity.class);
                            intent.putExtra("CallRateValue", value);
                            startActivity(intent);

                        } else {
                            Toast.makeText(getActivity(), "Please enter a number", Toast.LENGTH_SHORT).show();
                        }
                       /* if (value.length() >= 6) {

                            if (mInputMethodManager != null && et_number != null) {
                                mInputMethodManager.hideSoftInputFromWindow(
                                        et_number.getWindowToken(), 0);
                            }

                            if (!Utils.getNetwork(getActivity())) {
                                Toast.makeText(getActivity(), getString(R.string.network_unavail_message), Toast.LENGTH_LONG).show();
                                return;
                            }

                            if (!HomeScreenActivity.status.equalsIgnoreCase(getString(R.string.registered))) {
                                Toast.makeText(getActivity(), getString(R.string.registered_message), Toast.LENGTH_LONG).show();

                                return;
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
                            String url = Constants.CALL_RATES_URL + value + "?username=%2B" + username + "&password=" + actualPassword;
                            new APITask(url, getString(R.string.dialpad_call_rates_api_message)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(et_number.getWindowToken(), 0);
                            Toast.makeText(getActivity(), getString(R.string.more_activity_enter_min_six_digits), Toast.LENGTH_SHORT).show();
                        }*/

/*                        mPopupCloseDialog.dismiss();
                        mPopupCloseDialog = null;*/

                    }
                });
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(et_number.getWindowToken(), 0);
                        mPopupCloseDialog.dismiss();
                        mPopupCloseDialog = null;

                    }
                });

                if (mPopupCloseDialog != null)
                    mPopupCloseDialog.show();

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }
    /**
     * This method shows the bottom dialog for selecting the payment methods
     */
    private void showBottomSheet(LayoutInflater inflater) {

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setCancelable(true);
        View dialogView = inflater.inflate(R.layout.payment_selection_dialog, null);
        dialog.setContentView(dialogView);

        LinearLayout lyt_paypall = dialogView.findViewById(R.id.lyt_paypal_mode);
        LinearLayout lyt_inapp = dialogView.findViewById(R.id.lyt_in_app_mode);
        lyt_paypall.setOnClickListener(v -> {
            dialog.dismiss();
            new APITask(Constants.PAYPAL_GETCLIENTID_URL, getString(R.string.more_activity_paypal_api_value)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        });
        lyt_inapp.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(getActivity(), TopUpActivity.class);
            intent.putExtra("PaymentMode","stripe");
            startActivity(intent);
        });
        dialog.show();
    }
}
