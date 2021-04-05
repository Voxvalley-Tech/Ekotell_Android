package com.app.ekottel.activity;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.multidex.MultiDex;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.Menu;
import android.view.MenuItem;

import com.app.ekottel.R;

/**
 * This activity is used to launch calling screen through notification.
 *
 * @author Ramesh U
 * @version 2017
 */
public class EmptyActivity extends AppCompatActivity {

    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spare_layout);
        try {
            TAG = getString(R.string.empty_screen_tag);

            finish();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void updateUI(String str) {

        try {
            if (str.equals(getString(R.string.network_message))) {
                LOG.info(getString(R.string.network_message));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                LOG.info("Yes Something receieved in EmptyScreenReceiver");
                if (intent.getAction().equals(getString(R.string.network_message))) {
                    updateUI(getString(R.string.network_message));
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

            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter = new IntentFilter(getString(R.string.network_message));

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return true;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


}
