package com.app.ekottel.activity;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.adapter.DisplayPackagesAdapter;
import com.app.ekottel.model.SubscribePackagesList;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.HTTPUtils;
import com.app.ekottel.utils.Utils;
import com.ca.wrapper.CSDataProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This activity is used to display list of packages.
 *
 * @author Ramesh U
 * @version 2017
 */
public class SubscriberPackagesActivity extends AppCompatActivity {
    private TextView mTvSubScribePackage;
    private String username = "";
    private String password = "";
    ProgressDialog progressBar;
    Handler h = new Handler();
    Runnable runnableObj;
    int delay = 30000;
    private String TAG;
    ListView packagesList;
    private TextView emptyView;
    ProgressBar pb_subscribe_history;
    List<SubscribePackagesList> subscribePackagesLists = new ArrayList<SubscribePackagesList>();
    public static boolean isSubscribePkgClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriber_packages);
        TAG = getString(R.string.subscribe_package_tag);
        mTvSubScribePackage = (TextView) findViewById(R.id.tv_subscribe_package_header);
        pb_subscribe_history = (ProgressBar) findViewById(R.id.pb_subscribe_history);
        Typeface webTypeFace = Utils.getTypeface(getApplicationContext());
        LinearLayout ll_back = (LinearLayout) findViewById(R.id.ll_subscribe_package_back_arrow);
        TextView back = (TextView) findViewById(R.id.tv_subscribe_package_back_arrow);
        if (back != null)
            back.setTypeface(webTypeFace);
        if (ll_back != null) {
            ll_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        packagesList = (ListView) findViewById(R.id.packages_list);
        emptyView = (TextView) findViewById(R.id.emptyView);
        emptyView.setVisibility(View.GONE);
        username = CSDataProvider.getLoginID();
        username = username.replace("+", "");
        password = CSDataProvider.getPassword();
        String username1 = CSDataProvider.getLoginID();
        String password1 = CSDataProvider.getPassword();
        //LOG.info("updated password " + password1);
        String pwd = username1 + password1;

        //LOG.debug("Password" + pwd);
        String actualPassword = "";
        try {
            actualPassword = Utils.generateSHA256(pwd);
            //LOG.debug("Password after SHA" + actualPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = Constants.PACKAGES_URL + "username=%2B" + username + "&password=" + actualPassword+"&packagename=NULL&offset=0&size=50";
        new APITask(url, getString(R.string.more_activity_get_packages_api_value)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //Handle API all calls
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
            if (pb_subscribe_history != null) {
                pb_subscribe_history.setVisibility(View.VISIBLE);
            }
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


    private void processResponse(String api, String returndata) {

        if (api.equals(getString(R.string.more_activity_get_packages_api_value))) {
            try {

                JSONObject jsonObj = new JSONObject(returndata);
                JSONArray array = jsonObj.getJSONArray(getString(R.string.voucher_http_api_data_key));
                if (array.length() <= 0) {
                    emptyView.setVisibility(View.VISIBLE);
                }

                for (int i = 0; i < array.length(); i++) {
                    SubscribePackagesList subscribePackagesList = new SubscribePackagesList();
                    //list.add(array.getJSONObject(i).getString("interestKey"));
                    LOG.info("id:" + array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_id)));
                    LOG.info("packagename:" + array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_pkgname)));
                    LOG.info("cost:" + array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_cost)));
                    LOG.info("validity:" + array.getJSONObject(i).getString(getString(R.string.more_activity_json_key_validity)));
                    LOG.info("maxminutes:" + array.getJSONObject(i).getString(getString(R.string.more_activity_json_key_max_minutes)));
                    LOG.info("creationdate:" + array.getJSONObject(i).getString(getString(R.string.transaction_json_array_key_creation_date)));


                    String id = array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_id));
                    String packagename = array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_pkgname));
                    String cost = array.getJSONObject(i).getString(getString(R.string.subscribe_package_buy_package_json_array_key_cost));
                    String validity = array.getJSONObject(i).getString(getString(R.string.more_activity_json_key_validity));
                    String maxminutes = array.getJSONObject(i).getString(getString(R.string.more_activity_json_key_max_minutes));
                    String creationdate = array.getJSONObject(i).getString(getString(R.string.transaction_json_array_key_creation_date));

                    if (id == null) {
                        id = "";
                    }
                    if (packagename == null) {
                        packagename = "";
                    }
                    if (cost == null) {
                        cost = "";
                    }
                    if (validity == null) {
                        validity = "";
                    }
                    if (maxminutes == null) {
                        maxminutes = "";
                    }
                    if (creationdate == null) {
                        creationdate = "";
                    }

                    subscribePackagesList.setId(id);
                    subscribePackagesList.setCost(cost);
                    subscribePackagesList.setCreationDate(creationdate);
                    subscribePackagesList.setMaxMinutes(maxminutes);
                    subscribePackagesList.setPackageName(packagename);
                    subscribePackagesList.setValidity(validity);

                    subscribePackagesLists.add(subscribePackagesList);

                }


            } catch (Exception ex) {
                if (pb_subscribe_history != null) {
                    pb_subscribe_history.setVisibility(View.GONE);
                }
                if (subscribePackagesLists.size() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                }
                ex.printStackTrace();
            }

            DisplayPackagesAdapter displayPackagesAdapter = new DisplayPackagesAdapter(SubscriberPackagesActivity.this, subscribePackagesLists, 0);
            if (packagesList != null)
                packagesList.setAdapter(displayPackagesAdapter);

            if (pb_subscribe_history != null) {
                pb_subscribe_history.setVisibility(View.GONE);
            }

        }
    }

}
