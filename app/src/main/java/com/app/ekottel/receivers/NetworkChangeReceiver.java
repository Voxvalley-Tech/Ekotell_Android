package com.app.ekottel.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import static com.app.ekottel.utils.GlobalVariables.LOG;

import com.app.ekottel.R;
import com.app.ekottel.activity.HomeScreenActivity;
import com.app.ekottel.interfaces.NetworkChangeCallback;

/**
 * This activity is used to receive network related call backs.
 *
 * @author Ramesh U
 * @version 2017
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final int NO_CONNECTION_TYPE = -1;
    private static int sLastType = NO_CONNECTION_TYPE;
    private String TAG1;
    private NetworkChangeCallback callback;

    /*public NetworkChangeReceiver(NetworkChangeCallback callback) {
        this.callback = callback;
    }*/

    @Override
    public void onReceive(final Context context, final Intent intent) {

        Log.e("CSEvents","CSEventsnetwork--->"+context);
        try {
            TAG1 = context.getString(R.string.network_change_receiver_tag);
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);


            NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();

            final int currentType = activeNetworkInfo != null
                    ? activeNetworkInfo.getType() : NO_CONNECTION_TYPE;

            // Avoid handling multiple broadcasts for the same connection type
            LOG.info(TAG1, "OnnetworkCb last type=" + sLastType + "currentType=" + currentType);
            if (sLastType != currentType) {

                sLastType = currentType;
                if (activeNetworkInfo != null) {

                    if (activeNetworkInfo.isConnected()) {

                        LOG.info(TAG1, "OnnetworkCb NetworkChangeReceiver" + context.getPackageName());
                        Intent networkintent = new Intent(HomeScreenActivity.packageName + "."+context.getString(R.string.network_change_receiver_intent_status_key));
                        networkintent.putExtra(context.getString(R.string.network_change_receiver_intent_status_key), true);
                        networkintent.putExtra(context.getString(R.string.network_change_receiver_intent_change_status_key), true);
                        context.sendBroadcast(networkintent);


                    } else {

                        Intent networkintent = new Intent(HomeScreenActivity.packageName + "."+context.getString(R.string.network_change_receiver_intent_status_key));
                        networkintent.putExtra(context.getString(R.string.network_change_receiver_intent_status_key), false);
                        context.sendBroadcast(networkintent);

                    }

                } else {
                    LOG.info(TAG1, "OnnetworkCb NetworkChangeReceiver" + context.getPackageName());
                    Intent networkintent = new Intent(HomeScreenActivity.packageName + "."+context.getString(R.string.network_change_receiver_intent_status_key));
                    networkintent.putExtra(context.getString(R.string.network_change_receiver_intent_status_key), false);
                    context.sendBroadcast(networkintent);
                }


            } else {
                if (activeNetworkInfo == null) {


                    Intent networkintent = new Intent(HomeScreenActivity.packageName + "."+context.getString(R.string.network_change_receiver_intent_status_key));
                    networkintent.putExtra(context.getString(R.string.network_change_receiver_intent_status_key), false);
                    context.sendBroadcast(networkintent);

                }


            }

            if(isNetworkAvailable(context)){


            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
            return (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting());
        } catch (NullPointerException e) {
            showLog(e.getLocalizedMessage());
            return false;
        }
    }

    private void showLog(String message) {
        Log.e("NetworkChangeReceiver", "" + message);
    }


}
