package com.app.ekottel.activity;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.adapter.DisplayMyNotificationsAdapter;
import com.app.ekottel.utils.MainActivity;
import com.app.ekottel.utils.NotificationMethodHelper;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSDbFields;
import com.ca.custom.CSCustomApis;


/**
 * This activity is used to Display Promotional Messages.
 *
 * @author Ramesh U
 * @version 2017
 */
public class ViewMyPromotionsActivity extends AppCompatActivity {

    ListView mListView;
    DisplayMyNotificationsAdapter displayPackagesAdapter;
    private String TAG;
    public Dialog mPopupCloseDialog = null;
    private ImageView mIvDeleteAllNotifications;
    CSCustomApis cs = new CSCustomApis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_promotional);

        try {
            TAG = getString(R.string.promotional_tag);
            MainActivity.context = getApplicationContext();
            mListView = (ListView) findViewById(R.id.lv_promotional);
            mIvDeleteAllNotifications = (ImageView) findViewById(R.id.iv_notifications_delete_all);


            TextView noData = (TextView) findViewById(R.id.nodata);

            mListView.setEmptyView(noData);
            Typeface webTypeFace = Utils.getTypeface(getApplicationContext());
            LinearLayout ll_back = (LinearLayout) findViewById(R.id.ll_promotional_back_arrow);
            TextView back = (TextView) findViewById(R.id.tv_promotional_back_arrow);
            if (ll_back != null) {
                ll_back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        finish();
                    }
                });
                back.setTypeface(webTypeFace);
            }

            Cursor c = cs.getPromotionalMessageCursor();
            if (c.getCount() > 0) {
                mIvDeleteAllNotifications.setVisibility(View.VISIBLE);
            } else {
                mIvDeleteAllNotifications.setVisibility(View.GONE);
            }

            displayPackagesAdapter = new DisplayMyNotificationsAdapter(getApplicationContext(), c, 0);
            mListView.setAdapter(displayPackagesAdapter);

            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    invokeNotificationDialog(getString(R.string.promotional_alert_type_single), position);

                    return true;
                }
            });


            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {

                        Cursor cur = cs.getPromotionalMessageCursor();

                        cur.moveToPosition(position);

                        String promotionalmessagemessage = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROMOTIONAL_MESSAGEMESSAGE));
                        String row_id = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_ID));
                        LOG.info("promotionalmessagemessage to call:" + promotionalmessagemessage);
                        cs.updatePromotionalMessageByFilterByRowId(row_id, CSDbFields.KEY_PROMOTIONAL_MESSAGEREAD, "1");


                      /*  String promotionalmessagemessage = cur.getString(cur.getColumnIndexOrThrow("promotionalmessagemessage"));
                        String row_id = cur.getString(cur.getColumnIndexOrThrow("promotionalmessageid"));
                        LOG.info("promotionalmessagemessage to call:" + promotionalmessagemessage);
                        cs.updatePromotionalMessageByFilterByRowId(row_id, "promotionalmessageusernotified","1");*/
                        cur.close();

                        Cursor c = cs.getPromotionalMessageCursor();
                        displayPackagesAdapter = new DisplayMyNotificationsAdapter(getApplicationContext(), c, 0);
                        mListView.setAdapter(displayPackagesAdapter);

                        showalert(promotionalmessagemessage);


                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }


                }


            });

            mIvDeleteAllNotifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    invokeNotificationDialog(getString(R.string.promotional_alert_type_all), 0);
                }
            });


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * This is used to update UI whatever call back comes
     *
     * @param str Display actual message
     */
    public void updateUI(String str) {

        try {
            if (str.equals(getString(R.string.network_message))) {
                LOG.info("NetworkError receieved");
            } else if (str.equals(getString(R.string.promotional_intent_message_received))) {
                LOG.info("PromotionalMessageReceieved receieved");

                NotificationMethodHelper.cancelNotificationList(ViewMyPromotionsActivity.this);

                displayPackagesAdapter.changeCursor(cs.getPromotionalMessageCursor());
                displayPackagesAdapter.notifyDataSetChanged();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This is used to display message details using alert box
     *
     * @param result display message
     * @return which returns boolean value
     */
    public boolean showalert(String result) {
        try {
            android.app.AlertDialog.Builder successfullyLogin = new android.app.AlertDialog.Builder(ViewMyPromotionsActivity.this);
            successfullyLogin.setMessage(result);
            successfullyLogin.setPositiveButton(getString(R.string.splash_network_alert_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {


                        }
                    });


            successfullyLogin.show();

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    /**
     * This is used to receive all call backs
     */
    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                LOG.info("Yes Something receieved in RecentReceiver");
                if (intent.getAction().equals(getString(R.string.network_message))) {
                    updateUI(getString(R.string.network_message));
                } else if (intent.getAction().equals(getString(R.string.promotional_intent_message_received))) {
                    updateUI(getString(R.string.promotional_intent_message_received));
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
            IntentFilter filter1 = new IntentFilter(getString(R.string.promotional_intent_message_received));

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter);
            getApplicationContext().registerReceiver(MainActivityReceiverObj, filter1);
            /*NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.cancelAll();*/
            //Utils.handleonresume();
            NotificationMethodHelper.cancelNotificationList(ViewMyPromotionsActivity.this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
            getApplicationContext().unregisterReceiver(MainActivityReceiverObj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }


    /**
     * This is handle to display delete notification
     */
    private void invokeNotificationDialog(final String type, final int position) {

        if (mPopupCloseDialog == null) {

            try {

                mPopupCloseDialog = new Dialog(ViewMyPromotionsActivity.this);

                mPopupCloseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mPopupCloseDialog.setContentView(R.layout.alertdialog_close);
                mPopupCloseDialog.setCancelable(false);
                mPopupCloseDialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));


                Button yes = (Button) mPopupCloseDialog
                        .findViewById(R.id.btn_alert_ok);
                Button no = (Button) mPopupCloseDialog
                        .findViewById(R.id.btn_alert_cancel);


                TextView tv_title = (TextView) mPopupCloseDialog
                        .findViewById(R.id.tv_alert_title);
                TextView tv_message = (TextView) mPopupCloseDialog
                        .findViewById(R.id.tv_alert_message);
                if (type != null && type.equalsIgnoreCase(getString(R.string.promotional_alert_type_all))) {
                    tv_message.setText(getString(R.string.promotional_delete_all_notification_alert_message));
                } else {
                    tv_message.setText(getString(R.string.promotional_delete_single_notification_alert_message));
                }
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (type != null && type.equalsIgnoreCase(getString(R.string.promotional_alert_type_all))) {
                            //IAmLiveDB.deleteAllPromotionalMessages();
                            cs.deleteAllPromotionalMessages();
                            displayPackagesAdapter.changeCursor(cs.getPromotionalMessageCursor());
                            displayPackagesAdapter.notifyDataSetChanged();
                            mIvDeleteAllNotifications.setVisibility(View.GONE);

                        } else {
                            //IAmLiveDB.deletePromotionalMessageByPosition(position);
                            cs.deletePromotionalMessageByPosition(position);
                            displayPackagesAdapter.changeCursor(cs.getPromotionalMessageCursor());
                            displayPackagesAdapter.notifyDataSetChanged();
                            if (displayPackagesAdapter.getCount() == 0) {
                                mIvDeleteAllNotifications.setVisibility(View.GONE);
                            } else {
                                mIvDeleteAllNotifications.setVisibility(View.VISIBLE);
                            }
                        }

                        mPopupCloseDialog.dismiss();
                        mPopupCloseDialog = null;


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


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
