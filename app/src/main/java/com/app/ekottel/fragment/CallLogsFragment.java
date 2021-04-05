package com.app.ekottel.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;

import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import static com.app.ekottel.utils.GlobalVariables.LOG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;

import com.app.ekottel.activity.ChatAdvancedActivity;
import com.app.ekottel.activity.DialpadActivity;
import com.app.ekottel.activity.ProfileImageActivity;
import com.app.ekottel.activity.ShowCallLogHistory;
import com.app.ekottel.adapter.CallLogsSectionListAdapter;
import com.app.ekottel.model.CallLogsModel;
import com.app.ekottel.utils.CallMethodHelper;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.wrapper.CSDataProvider;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by ramesh.u on 11/13/2017.
 */

public class CallLogsFragment extends Fragment {
    private TextView mTvSearch;
    private TextView mTvAllCalls;
    private TextView mTvMissedCalls;
    private TextView mCallLogsSearchCancel;
    private TextView mTvNoCallLogs;
    private EditText mEtSearch;
    private ImageView mIvRecentsDialpad;
    private LinearLayout ll_call_logs_header;
    private ListView mListView;
    private CallLogsAdapter recentsAdapter;
    private ArrayList<String> secHeader = new ArrayList<>();
    private RelativeLayout mRlRemoveCallLog;
    private TextView mRemoveAllCallLog;
    public static Dialog PopupCloseDialog = null;
    private InputMethodManager mInputMethodManager;

    private boolean mCurrentPage = true;
    PreferenceProvider mPreferenceProvider;
    ArrayList<CallLogsModel> callLogsModelAllCallList = new ArrayList<>();
    ArrayList<CallLogsModel> callLogsModelMissedCallsList = new ArrayList<>();
    ArrayList<CallLogsModel> callLogsModelAllCallsList = new ArrayList<>();
    ArrayList<CallLogsModel> callLogsModelFilteredCallsList = new ArrayList<>();
    private String TAG;
    private View mCallLogsView;
    public static Dialog callLogDetailsDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mCallLogsView == null) {
            mCallLogsView = inflater.inflate(R.layout.fragment_call_logs, container, false);
            TAG = getString(R.string.call_logs_tag);
            mTvSearch = mCallLogsView.findViewById(R.id.tv_call_logs_search);
            mEtSearch = mCallLogsView.findViewById(R.id.et_call_logs_search);
            mTvNoCallLogs = mCallLogsView.findViewById(R.id.tv_call_logs_nocontacts);
            mTvAllCalls = mCallLogsView.findViewById(R.id.tv_call_logs_all_calls);
            mCallLogsSearchCancel = mCallLogsView.findViewById(R.id.tv_calllogs_search_cancel);
            mRemoveAllCallLog = mCallLogsView.findViewById(R.id.removeallcallog);
            mIvRecentsDialpad = mCallLogsView.findViewById(R.id.iv_recents_dialpad);
            mRlRemoveCallLog = mCallLogsView.findViewById(R.id.rl_removecalllog);
            ll_call_logs_header = mCallLogsView.findViewById(R.id.ll_call_logs_header);
            mListView = mCallLogsView.findViewById(R.id.lv_calllogs);
            mTvMissedCalls = mCallLogsView.findViewById(R.id.tv_call_logs_missed_calls);
            Typeface text_font = Utils.getTypeface(getActivity());
            mTvSearch.setTypeface(text_font);
            mRemoveAllCallLog.setTypeface(text_font);
            mCallLogsSearchCancel.setTypeface(text_font);
            mRemoveAllCallLog.setText(getResources().getString(R.string.recents_delete));
            mTvAllCalls.setSelected(true);
            mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            mPreferenceProvider = new PreferenceProvider(getActivity());

            mPreferenceProvider.setPrefboolean(getString(R.string.call_logs_pref_make_call), false);


            mIvRecentsDialpad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent_dialpad = new Intent(getActivity(), DialpadActivity.class);

                    startActivity(intent_dialpad);

                }
            });

            mTvAllCalls.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    mCurrentPage = true;
                    mTvAllCalls.setSelected(true);
                    mTvMissedCalls.setSelected(false);
                    Cursor c = CSDataProvider.getCallLogCursorGroupedByNumber();
                    LOG.info("Recents Cout All" + c.getCount());
                    if (c.getCount() <= 0) {
                        mRlRemoveCallLog.setVisibility(View.GONE);
                    } else {
                        mRlRemoveCallLog.setVisibility(View.VISIBLE);
                    }
                    if (c != null)
                        c.close();

                    loadCallLogs(CSDataProvider.getCallLogCursorGroupedByNumber());
                }
            });

            mListView.setEmptyView(mTvNoCallLogs);
            loadCallLogs(CSDataProvider.getCallLogCursorGroupedByNumber());

            mRlRemoveCallLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mCurrentPage) {
                        Cursor ccr = CSDataProvider.getCallLogCursorGroupedByNumber();
                        if (ccr.getCount() > 0) {
                            showAlert(getString(R.string.call_logs_all_message));
                        }
                        ccr.close();
                    } else {
                        Cursor ccr = CSDataProvider.getCallLogCursorGroupedByNumber();
                        if (ccr.getCount() > 0) {
                            showAlert(getString(R.string.call_logs_missed_message));
                        }
                        ccr.close();
                    }


                }
            });

            mCallLogsSearchCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mEtSearch.getText().clear();
                }
            });
            mEtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.length() > 0) {
                        mCallLogsSearchCancel.setVisibility(View.VISIBLE);
                    } else {
                        mCallLogsSearchCancel.setVisibility(View.INVISIBLE);
                    }
                    LOG.info("Yes on touch up:" + mEtSearch.getText().toString());
                    if (mCurrentPage) {
                        recentsAdapter = new CallLogsAdapter(getActivity(), callLogsModelAllCallList, secHeader, mCurrentPage);
                        recentsAdapter.getFilter().filter(charSequence.toString());
                        recentsAdapter.notifyDataSetChanged();
                    } else {
                        recentsAdapter = new CallLogsAdapter(getActivity(), callLogsModelMissedCallsList, secHeader, mCurrentPage);
                        recentsAdapter.getFilter().filter(charSequence.toString());
                        recentsAdapter.notifyDataSetChanged();
                    }
                    updateUI(getString(R.string.call_logs_no_call_logs_message));
                }

                @Override
                public void afterTextChanged(Editable editable) {


                }
            });
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    TextView c = (TextView) view.findViewById(R.id.tv_number);
                    String number = c.getText().toString();
                    invokeCloseDialog(getString(R.string.call_logs_all_message), number);

                    return true;
                }
            });
        }
        return mCallLogsView;
    }


    /**
     * Method is to load list of section views
     *
     * @param c This is cursor which handles section view
     * @return list of  section views data
     */
    private ArrayList<CallLogsModel> loadContacts(Cursor c) {
        callLogsModelAllCallsList.clear();
        callLogsModelAllCallList.clear();
        callLogsModelMissedCallsList.clear();
        secHeader.clear();
        if (c.getCount() > 0) {
            Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < c.getCount(); i++) {

                CallLogsModel callLogsModel = new CallLogsModel();

                c.moveToNext();

                callLogsModel.setName(c.getString(c.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_NAME)));
                callLogsModel.setNumber(c.getString(c.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_NUMBER)));
                callLogsModel.setDirection(c.getString(c.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_DIR)));
                callLogsModel.setTime(c.getString(c.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_TIME)));
                callLogsModel.setDuration(c.getString(c.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_DURATION)));
                long date_l = Long.valueOf(c.getString(c.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_TIME)));
                calendar.setTimeInMillis(date_l);
                try {
                    secHeader.add(getDate(date_l, getString(R.string.call_logs_date_format)));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //LOG.info("Call direction" + c.getString(c.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_DIR)));

                callLogsModelAllCallsList.add(callLogsModel);

                if (mCurrentPage) {
                    callLogsModelAllCallList.add(callLogsModel);
                } else {
                    callLogsModelMissedCallsList.add(callLogsModel);
                }
            }
        }

        if (c != null)
            c.close();

        return callLogsModelAllCallsList;
    }


    /**
     * Method is to load list of section views
     *
     * @param filteredList This is list which handles section view
     * @return list of  section views data
     */
    private ArrayList<CallLogsModel> loadFilteredContacts(ArrayList<CallLogsModel> filteredList) {
        callLogsModelFilteredCallsList.clear();
        secHeader.clear();

        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < filteredList.size(); i++) {

            CallLogsModel callLogsModel = new CallLogsModel();


            callLogsModel.setName(filteredList.get(i).getName());
            callLogsModel.setNumber(filteredList.get(i).getNumber());
            callLogsModel.setDirection(filteredList.get(i).getDirection());
            callLogsModel.setTime(filteredList.get(i).getTime());
            callLogsModel.setDuration(filteredList.get(i).getDuration());
            long date_l = Long.valueOf(filteredList.get(i).getTime());
            calendar.setTimeInMillis(date_l);
            try {


                secHeader.add(getDate(date_l, getString(R.string.call_logs_date_format)));
            } catch (Exception e) {
                e.printStackTrace();
            }

            callLogsModelFilteredCallsList.add(callLogsModel);


        }


        return callLogsModelFilteredCallsList;
    }

    /**
     * This method is to get date required format
     *
     * @param milliSeconds this is given date in milliseconds
     * @param dateFormat   this is required date format
     * @return which returns date
     */
    private String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified
        // format.
        // Create a calendar object that will convert the date and time value in

        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(milliSeconds);
        Calendar now = Calendar.getInstance();
        String day;
        if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR) && now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) && now.get(Calendar.MONTH) == smsTime.get(Calendar.MONTH)) {
            LOG.info("Month today:" + now.get(Calendar.MONTH));
            return day = "Today ";

        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR) && now.get(Calendar.MONTH) == smsTime.get(Calendar.MONTH) && now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return day = "Yesterday ";

        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            String format = formatter.format(smsTime.getTime());
            //day = format;
            return format;
        }
    }

    /**
     * Method is used to display alert for delete call history
     *
     * @param message this is message to show alert dialog
     */
    public void showAlert(final String message) {

        try {

            PopupCloseDialog = new Dialog(getActivity());

            PopupCloseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            PopupCloseDialog.setContentView(R.layout.alertdialog_close);
            PopupCloseDialog.setCancelable(false);
            PopupCloseDialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            TextView tv_title = (TextView) PopupCloseDialog
                    .findViewById(R.id.tv_alert_title);
            TextView tv_message = (TextView) PopupCloseDialog
                    .findViewById(R.id.tv_alert_message);
            tv_title.setText(getString(R.string.call_logs_confirmation_message));
            if (message != null && message.equalsIgnoreCase(getString(R.string.call_logs_all_message))) {
                tv_message.setText(getString(R.string.call_logs_delete_all_message));
            } else {
                tv_message.setText(getString(R.string.call_logs_missed_call_message));
            }
            Button yes = (Button) PopupCloseDialog
                    .findViewById(R.id.btn_alert_ok);
            final Button no = (Button) PopupCloseDialog
                    .findViewById(R.id.btn_alert_cancel);

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (message != null && message.equalsIgnoreCase(getString(R.string.call_logs_all_message))) {
                        CSDataProvider.deleteAllCallLog();
                        loadCallLogs(CSDataProvider.getCallLogCursorGroupedByNumber());
                        updateUI(getString(R.string.call_logs_no_call_logs_message));
                    }

                    PopupCloseDialog.dismiss();
                    PopupCloseDialog = null;

                }
            });
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PopupCloseDialog.dismiss();
                    PopupCloseDialog = null;

                }
            });

            if (PopupCloseDialog != null)
                PopupCloseDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * This is used to handle call backs
     */
    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (!isAdded()) {
                    return;
                }

                LOG.info("CallBack Received, Action: " + intent.getAction());

                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    updateUI(CSEvents.CSCLIENT_NETWORKERROR);
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_IMAGESDBUPDATED)) {
                    updateUI(CSEvents.CSCLIENT_IMAGESDBUPDATED);
                } else if (intent.getAction().equals(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE)) {

                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        updateUI(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE);
                    }
                } else if (intent.getAction().equals(CSEvents.CSCONTACTS_CONTACTSUPDATED)) {
                    updateUI(CSEvents.CSCONTACTS_CONTACTSUPDATED);
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_USERPROFILECHANGED)) {
                    updateUI(CSEvents.CSCLIENT_USERPROFILECHANGED);
                } else if (intent.getAction().equals(CSEvents.CSCALL_CALLLOGUPDATED)) {
                    loadCallLogs(CSDataProvider.getCallLogCursorGroupedByNumber());
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

            if (!getUserVisibleHint()) {
                return;
            }
            LOG.info("Call Logs Resume");

            if (mInputMethodManager != null) {
                mInputMethodManager.hideSoftInputFromWindow(
                        mEtSearch.getWindowToken(), 0);
            }

            mEtSearch.setText("");

            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter1 = new IntentFilter(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE);
            IntentFilter filter2 = new IntentFilter(CSEvents.CSCLIENT_IMAGESDBUPDATED);
            IntentFilter filter3 = new IntentFilter(CSEvents.CSCONTACTS_CONTACTSUPDATED);
            IntentFilter filter4 = new IntentFilter(CSEvents.CSCALL_CALLLOGUPDATED);
            IntentFilter filter5 = new IntentFilter(CSEvents.CSCLIENT_USERPROFILECHANGED);

            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(MainActivityReceiverObj, filter);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(MainActivityReceiverObj, filter1);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(MainActivityReceiverObj, filter2);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(MainActivityReceiverObj, filter3);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(MainActivityReceiverObj, filter4);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(MainActivityReceiverObj, filter5);

            boolean isMakeCall = mPreferenceProvider.getPrefBoolean(getString(R.string.call_logs_pref_make_call));
            boolean isIncomingCall = mPreferenceProvider.getPrefBoolean("isIncomingCall");

            if (isMakeCall || isIncomingCall) {
                mPreferenceProvider.setPrefboolean(getString(R.string.call_logs_pref_make_call), false);
                mPreferenceProvider.setPrefboolean("isIncomingCall", false);
                if (mCurrentPage) {
                    loadCallLogs(CSDataProvider.getCallLogCursorGroupedByNumber());
                }
            }

            updateUI(getString(R.string.call_logs_no_call_logs_message));
            LOG.info("Mainactivity on resume is called Call Logs");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(MainActivityReceiverObj);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    /**
     * This is used to handle update UI for whatever call back is getting
     *
     * @param str this is used to display message
     */
    public void updateUI(String str) {

        try {

            if (str.equals(CSEvents.CSCLIENT_NETWORKERROR)) {
            } else if (str.equals(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE) || str.equals(CSEvents.CSCONTACTS_CONTACTSUPDATED) || str.equals(CSEvents.CSCHAT_CHATUPDATED) || str.equals(CSEvents.CSCLIENT_USERPROFILECHANGED) || str.equals(CSEvents.CSCLIENT_IMAGESDBUPDATED)) {

                if (mCurrentPage) {
                    loadCallLogs(CSDataProvider.getCallLogCursorGroupedByNumber());
                }

            } else if (str.equals(getString(R.string.call_logs_no_call_logs_message))) {
                LOG.info(getString(R.string.call_logs_no_call_logs_message));

                try {

                    if (mCurrentPage) {
                        Cursor ccr = CSDataProvider.getCallLogCursorGroupedByNumber();
                        if (ccr.getCount() <= 0) {
                            mRlRemoveCallLog.setVisibility(View.GONE);
                            mTvNoCallLogs.setVisibility(View.VISIBLE);
                        } else {
                            mRlRemoveCallLog.setVisibility(View.VISIBLE);
                            mTvNoCallLogs.setVisibility(View.INVISIBLE);
                        }
                        ccr.close();

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    /**
     * Method to handle to display single call history
     *
     * @param message this is a message to show alert dialog
     * @param number  this is a number to handle if contact existing or not  to display add contact
     */
    private void invokeCloseDialog(final String message, final String number) {


        try {

            PopupCloseDialog = new Dialog(getActivity());

            PopupCloseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            PopupCloseDialog.setContentView(R.layout.contacts_popup);
            PopupCloseDialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));


            LinearLayout ll_delete = (LinearLayout) PopupCloseDialog.findViewById(R.id.ll_contact_details);
            LinearLayout ll_add_contact = (LinearLayout) PopupCloseDialog.findViewById(R.id.ll_contact_invite);
            TextView tv_add = (TextView) PopupCloseDialog.findViewById(R.id.tv_contacts_invite);
            TextView tv_delete = (TextView) PopupCloseDialog.findViewById(R.id.tv_contacts_details);

            TextView tv_add_text = (TextView) PopupCloseDialog.findViewById(R.id.tv_contact_invite);
            TextView tv_delete_text = (TextView) PopupCloseDialog.findViewById(R.id.tv_contact_details);

            Typeface typeface = Utils.getTypeface(getActivity());
            tv_delete.setTypeface(typeface);
            tv_add.setTypeface(typeface);

            tv_add.setText(getResources().getString(R.string.dialpad_add_contact));
            tv_delete.setText(getResources().getString(R.string.recents_delete));

            tv_delete_text.setText(getString(R.string.call_logs_delete_single_history_message));


            Cursor csr = CSDataProvider.getContactCursorByNumber(number.trim());
            if (csr.getCount() > 0) {
                csr.moveToNext();
                String idd = csr.getString(csr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
                if (!iscontactexists(idd, number)) {
                    ll_add_contact.setVisibility(View.VISIBLE);
                    tv_add_text.setText(getString(R.string.call_logs_add_contact_message));
                } else {
                    ll_add_contact.setVisibility(View.GONE);
                }

            } else {
                LOG.info("Number is:" + number);

                String name = Utils.getCallLogContactName(number, getActivity());


                if (name != null) {
                    ll_add_contact.setVisibility(View.GONE);

                } else {
                    ll_add_contact.setVisibility(View.VISIBLE);
                    tv_add_text.setText(getString(R.string.call_logs_add_contact_message));
                }

            }
            csr.close();

            ll_add_contact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
                    startActivity(intent);

                    PopupCloseDialog.dismiss();
                    PopupCloseDialog = null;

                }
            });
            ll_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (message != null && message.equalsIgnoreCase(getString(R.string.call_logs_all_message))) {
                        CSDataProvider.deleteCallLogByFilter(CSDbFields.KEY_CALLLOG_NUMBER, number);
                        loadCallLogs(CSDataProvider.getCallLogCursorGroupedByNumber());
                        updateUI(getString(R.string.call_logs_no_call_logs_message));
                    }
                    PopupCloseDialog.dismiss();
                    PopupCloseDialog = null;

                }
            });

            if (PopupCloseDialog != null)
                PopupCloseDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * Method is to handle if given number is app contact or not
     *
     * @param id     this is a id to get contact exists or not
     * @param number this is a number to exists or not
     * @return which returns boolean value
     */
    public boolean iscontactexists(String id, String number) {
        boolean retvalue = false;
        String phone = "";
        try {

            Cursor cursor = getActivity().getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{id}, null);

            while (cursor.moveToNext()) {
                phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                break;
            }

            cursor.close();
            if (!phone.equals("")) {
                phone = phone.replaceAll("[^0-9+]", "");
                if (phone.equals(number)) {
                    retvalue = true;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return retvalue;
    }

    /**
     * Method is to load call logs and update UI
     *
     * @param c This is a cursor which contains list of call logs
     */
    private void loadCallLogs(Cursor c) {

        loadContacts(c);

        if (callLogsModelAllCallsList.size() <= 0) {
            ll_call_logs_header.setVisibility(View.GONE);
        } else {
            ll_call_logs_header.setVisibility(View.GONE);
        }

        recentsAdapter = new CallLogsAdapter(getActivity(), callLogsModelAllCallsList, secHeader, mCurrentPage);
        CallLogsSectionListAdapter sectionAdapter = new CallLogsSectionListAdapter(getActivity(), getActivity().getLayoutInflater(), recentsAdapter);
        mListView.setAdapter(sectionAdapter);
        recentsAdapter.notifyDataSetChanged();
    }

    private void loadFilteredCallLogs(ArrayList<CallLogsModel> filteredList) {

        loadFilteredContacts(filteredList);

        recentsAdapter = new CallLogsAdapter(getActivity(), callLogsModelFilteredCallsList, secHeader, mCurrentPage);
        CallLogsSectionListAdapter sectionAdapter = new CallLogsSectionListAdapter(getActivity(), getActivity().getLayoutInflater(), recentsAdapter);
        mListView.setAdapter(sectionAdapter);
        recentsAdapter.notifyDataSetChanged();
    }

    public class CallLogsAdapter extends BaseAdapter implements Filterable {
        private Activity context;
        String isApp = "0";
        ArrayList<CallLogsModel> callLogsList;
        ArrayList<String> sections;
        LayoutInflater inflater = null;
        ArrayList<String> secHeader = new ArrayList<>();
        CallLogsAdapter.CallLogsFilter listfilter = new CallLogsAdapter.CallLogsFilter();
        private boolean current_page;
        private long mLastClickTime = 0;

        public CallLogsAdapter(Activity context,
                               ArrayList<CallLogsModel> callLogsList, ArrayList<String> sections, boolean current_page) {

            this.context = context;
            this.callLogsList = callLogsList;
            this.sections = sections;
            this.current_page = current_page;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }


        public class Holder {

            TextView tv_number, tv_chat, tv_recents_video;
            ImageView tv_call,tv_info;
            TextView tv_call_logs_time;
            TextView tv_contacts_name;
            TextView tv_direction;
            CircleImageView image;
            LinearLayout ll_name;
            ImageView tringyContactImageView;

        }

        @Override
        public View getView(int position, View rowView, ViewGroup parent) {
            final CallLogsAdapter.Holder holder;

            if (rowView == null) {
                holder = new CallLogsAdapter.Holder();
                rowView = inflater.inflate(R.layout.recents_adapter, null);

                holder.tv_number = (TextView) rowView
                        .findViewById(R.id.tv_number);
                holder.tv_call = (ImageView) rowView
                        .findViewById(R.id.tv_contacts_call);
                holder.tv_direction = (TextView) rowView
                        .findViewById(R.id.tv_direction);
                holder.tv_chat = (TextView) rowView
                        .findViewById(R.id.tv_contacts_chat);
                holder.tv_recents_video = (TextView) rowView
                        .findViewById(R.id.tv_recents_video);
                holder.tv_info = (ImageView) rowView
                        .findViewById(R.id.tv_contacts_info);
                holder.tv_contacts_name = (TextView) rowView.findViewById(R.id.tv_call_logs_name);
                holder.tv_contacts_name.setSelected(true);
                holder.tv_call_logs_time = (TextView) rowView.findViewById(R.id.tv_call_logs_time);
                holder.image = (CircleImageView) rowView
                        .findViewById(R.id.iv_call_logs);
                holder.ll_name = (LinearLayout) rowView
                        .findViewById(R.id.ll_name);
                holder.tringyContactImageView = rowView.findViewById(R.id.iv_contact_details_app_contact);

                rowView.setTag(holder);
            } else {
                holder = (CallLogsAdapter.Holder) rowView.getTag();
            }

            Typeface text_font = Utils.getTypeface(context);

            holder.tv_chat.setTypeface(text_font);
            holder.tv_recents_video.setTypeface(text_font);
           // holder.tv_info.setTypeface(text_font);


            holder.tv_chat.setText(context.getString(R.string.contact_chat));
            holder.tv_recents_video.setText(context.getString(R.string.video_call_icon));
           // holder.tv_info.setText(context.getString(R.string.info));

            final CallLogsModel cModel = callLogsList.get(position);

            String name = cModel.getName();
            String number = cModel.getNumber();
            String directionn = cModel.getDirection();
            holder.tv_direction.setText(directionn);
            LOG.info("Call Log Direction" + directionn);

            Cursor cr = CSDataProvider.getContactCursorByNumber(number);
            String id = "";
            if (cr.getCount() > 0) {
                cr.moveToNext();
                isApp = cr.getString(cr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_IS_APP_CONTACT));
                id = cr.getString(cr.getColumnIndex(CSDbFields.KEY_CONTACT_ID));
            } else {
                isApp = "0";
            }


            cr.close();

            if ((isApp != null && isApp.equalsIgnoreCase("1"))) {
                holder.tv_chat.setVisibility(View.GONE);
                holder.tv_recents_video.setVisibility(View.GONE);
                holder.tringyContactImageView.setVisibility(View.GONE);
            } else {
                holder.tv_chat.setVisibility(View.GONE);
                holder.tv_recents_video.setVisibility(View.GONE);
                holder.tringyContactImageView.setVisibility(View.GONE);
            }
            Cursor cs = null;
            if (current_page) {
                cs = CSDataProvider.getCallLogCursorByFilter(CSDbFields.KEY_CALLLOG_NUMBER, number);
            }

            String count = String.valueOf(cs.getCount());
            cs.close();

            final String number1 = number;

            number = number.replaceAll("[^0-9+]", "");
            holder.tv_number.setText(number);

            if (name.equals("")) {
                holder.tv_contacts_name.setText(Html.fromHtml(number + "<font color=" + ContextCompat.getColor(context, R.color.theme_color) + ">" + "(" + count + ")" + "</font>"));
            } else {
                holder.tv_contacts_name.setText(Html.fromHtml(name + "<font color=" + ContextCompat.getColor(context, R.color.theme_color) + ">" + "(" + count + ")" + "</font>"));
            }


            String time = "";

            String formattedDate = cModel.getTime();
            long timme = Long.valueOf(formattedDate);

            time = new SimpleDateFormat(Utils.getTimeFormatForCallLogs(context)).format(timme);

            String ds1 = cModel.getDuration();
            String mysecondary = time;
            holder.tv_call_logs_time.setText(mysecondary);
            String direction = cModel.getDirection();
            LOG.info("Call Log direction " + direction);
            /*if (direction.contains(getString(R.string.call_logs_out_going_message))) {
                holder.tv_call.setTextColor(context.getResources().getColor(R.color.recents_outgoing_call));
            } else if (direction.contains(getString(R.string.call_logs_in_coming_message))) {
                holder.tv_call.setTextColor(context.getResources().getColor(R.color.recents_incoming_call));
            } else if (direction.contains("MISSED")) {
                holder.tv_call.setTextColor(context.getResources().getColor(R.color.recents_missed_call));
            } else {
                holder.tv_call.setTextColor(context.getResources().getColor(R.color.recents_outgoing_call));
            }*/

            holder.image.setImageResource(R.drawable.ic_status_profile_avathar);
            holder.image.setTag(number);
            if (isApp != null && isApp.equalsIgnoreCase("1")) {

                Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, number);

                if (cur.getCount() > 0) {

                    cur.moveToNext();
                    String picid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
                    Bitmap mybitmap = CSDataProvider.getImageBitmap(picid);
                    Log.e("imagebit","imagebit-->"+mybitmap);

                    if (mybitmap != null) {
                        cModel.setProfilePicAvailable(true);
                        holder.image.setImageBitmap(mybitmap);
                    } else {
                        new ImageDownloaderTask(holder.image, cModel).execute(number);
                    }

                } else {
                    new ImageDownloaderTask(holder.image, cModel).execute(number);
                }

                cur.close();

            } else {
                new ImageDownloaderTask(holder.image, cModel).execute(number);
            }


            final String mydirection = direction;
            final String numberToDial = number;

            final boolean isProfilePicAvailable = cModel.isProfilePicAvailable();
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isApp != null && isApp.equalsIgnoreCase("1")) {
                        if (isProfilePicAvailable) {
                            Intent profileIntent = new Intent(context, ProfileImageActivity.class);
                            profileIntent.putExtra("profileContactNumber", numberToDial);
                            context.startActivity(profileIntent);
                        } else {
                            Toast.makeText(context, "Profile image not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            LOG.info("Calllog typeiff " + isApp);
            holder.tv_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    Cursor cr = CSDataProvider.getContactCursorByNumber(cModel.getNumber());
                    String id = "";
                    if (cr.getCount() > 0) {
                        cr.moveToNext();
                        isApp = cr.getString(cr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_IS_APP_CONTACT));
                        id = cr.getString(cr.getColumnIndex(CSDbFields.KEY_CONTACT_ID));
                    } else {
                        isApp = "0";
                    }
                    cr.close();
                    LOG.info("Calllog typeiff " + isApp);
                    if(isApp.equals("0")){
                        CallMethodHelper.processAudioCall(context, numberToDial, "PSTN");
                    }
                    else{
                        showCallTypeDia(context, numberToDial, mydirection);
                    }

                }
            });


            final String name1 = name;
            String finalId = id;
            holder.tv_info.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        Intent intentt = new Intent(context, ShowCallLogHistory.class);
                        intentt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intentt.putExtra("number", number1);
                        intentt.putExtra("name", name);
                        intentt.putExtra("id", finalId);
                        intentt.putExtra("direction", mydirection);
                        context.startActivity(intentt);
                        // showCallDetailsDialog(name1, number1, mydirection, current_page, holder);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            holder.tv_chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    Intent intent = new Intent(context.getApplicationContext(), ChatAdvancedActivity.class);
                    intent.putExtra(Constants.INTENT_CHAT_CONTACT_NUMBER, numberToDial);
                    intent.putExtra(Constants.INTENT_CHAT_CONTACT_NAME, name);
                    intent.putExtra(Constants.IS_VIDEO_CALL_RUNNING, false);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.getApplicationContext().startActivity(intent);
                }
            });

            holder.tv_recents_video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                    CallMethodHelper.placeVideoCall(context, numberToDial);
                }
            });


            return rowView;
        }


        /**
         * This is used for loading image
         */
        class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
            private final WeakReference<ImageView> imageViewReference;
            String number = "";
            CallLogsModel callLogsModel;

            public ImageDownloaderTask(ImageView imageView, CallLogsModel cModel) {
                number = imageView.getTag().toString();
                callLogsModel = cModel;
                imageViewReference = new WeakReference<ImageView>(imageView);
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                Bitmap photo;
                try {
                    photo = getIMAGEPhoto(params[0]);
                    if (photo == null) {
                        photo = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_status_profile_avathar);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    photo = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_avatar_status);
                }
                return photo;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (isCancelled()) {
                    bitmap = null;
                }
                if (imageViewReference != null) {
                    ImageView imageView = imageViewReference.get();
                    if (imageView != null) {
                        if (imageView.getTag() == null || !imageView.getTag().toString().equals(number)) {
               /* The phoneNumber is not same. This means that this
                  image view is handled by some other async task.
                  We don't do anything and return. */
                            return;
                        }


                        if (bitmap != null) {
                            callLogsModel.setProfilePicAvailable(true);
                            imageView.setImageBitmap(bitmap);
                        } else {
                            imageView.setImageResource(R.drawable.ic_status_profile_avathar);
                        }
                    }
                }
            }
        }

        /**
         * This is used for loading bitmap image
         *
         * @param phoneNumber This is used for get image
         * @return which returns bitmap
         */
        public Bitmap getIMAGEPhoto(String phoneNumber) {
            Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNumber));
            Uri photoUri = null;
            ContentResolver cr = context.getContentResolver();

            Cursor contact = null;
            try {
                contact = cr.query(phoneUri,
                        new String[]{ContactsContract.Contacts._ID}, null, null,
                        null);
                if (contact != null)
                    if (contact.moveToFirst()) {
                        long userId = contact.getLong(contact
                                .getColumnIndex(ContactsContract.Contacts._ID));
                        photoUri = ContentUris.withAppendedId(
                                ContactsContract.Contacts.CONTENT_URI, userId);


                    } else {

                        return null;
                    }
                if (photoUri != null) {
                    InputStream input = ContactsContract.Contacts
                            .openContactPhotoInputStream(cr, photoUri, true);
                    if (input != null) {
                        return BitmapFactory.decodeStream(input);
                    }
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (contact != null)
                    contact.close();
            }

            return null;
        }

        /*  *//**
         * This is used for display call log details
         *
         * @param name                display actual name
         * @param managecontactnumber display number
         * @param managedirection     this is used for display count
         * @param holder
         *//*
        private void showCallDetailsDialog(String name, String managecontactnumber, String managedirection, boolean current_page, Holder holder) {
            if (callLogDetailsDialog == null) {
                View view = context.getLayoutInflater().inflate(R.layout.activity_call_logs_details, null);
                TextView number = (TextView) view.findViewById(R.id.number);
                TextView nameView = (TextView) view.findViewById(R.id.name);
                TextView exit = (TextView) view.findViewById(R.id.exit);
                if (name != null && name.length() == 0) {
                    nameView.setVisibility(View.GONE);
                    number.setText(managecontactnumber);
                } else {
                    String str = managecontactnumber;
                    String actualName;

                    str = str.replaceAll("[^0-9]", "").trim();
                    actualName = name.replaceAll("[^0-9]", "").trim();


                    if (!actualName.equalsIgnoreCase(str)) {
                        nameView.setVisibility(View.VISIBLE);
                        nameView.setText(name);
                        number.setText(managecontactnumber);
                    } else {
                        nameView.setVisibility(View.GONE);
                        number.setText(managecontactnumber);
                    }

                }

                ListView mListView = (ListView) view.findViewById(R.id.callDetailsList);
                if (current_page) {
                    loadContactsForDetails(CSDataProvider.getCallLogCursorByFilter(CSDbFields.KEY_CALLLOG_NUMBER, managecontactnumber));
                }
                FirstCallRecentsDetailLogAdapter appContactsAdapter = null;
                if (current_page) {
                    appContactsAdapter = new FirstCallRecentsDetailLogAdapter(context, CSDataProvider.getCallLogCursorByFilter(CSDbFields.KEY_CALLLOG_NUMBER, managecontactnumber), 0, secHeader, managecontactnumber);
                }
                CallLogsSectionListAdapter sectionAdapter = new CallLogsSectionListAdapter(context, context.getLayoutInflater(), appContactsAdapter);
                mListView.setAdapter(sectionAdapter);
                callLogDetailsDialog = new Dialog(context);
                callLogDetailsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                callLogDetailsDialog.setContentView(view);
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(callLogDetailsDialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = 500;
                lp.gravity = Gravity.BOTTOM;
                callLogDetailsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        callLogDetailsDialog = null;
                    }
                });
                exit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callLogDetailsDialog.dismiss();
                        callLogDetailsDialog = null;
                    }
                });
                if (callLogDetailsDialog != null)
                    callLogDetailsDialog.show();
            }
        }

        private ArrayList<CallLogsModel> loadContactsForDetails(Cursor c) {
            ArrayList<CallLogsModel> callLogsModelAllCallsList = new ArrayList<CallLogsModel>();
            secHeader.clear();
            if (c.getCount() > 0) {


                Calendar calendar = Calendar.getInstance();
                for (int i = 0; i < c.getCount(); i++) {

                    CallLogsModel callLogsModel = new CallLogsModel();

                    c.moveToNext();

                    callLogsModel.setName(c.getString(c.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_NAME)));
                    callLogsModel.setNumber(c.getString(c.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_NUMBER)));
                    callLogsModel.setDirection(c.getString(c.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_DIR)));
                    callLogsModel.setTime(c.getString(c.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_TIME)));
                    callLogsModel.setDuration(c.getString(c.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_DURATION)));
                    long date_l = Long.valueOf(c.getString(c.getColumnIndexOrThrow(CSDbFields.KEY_CALLLOG_TIME)));
                    calendar.setTimeInMillis(date_l);
                    try {


                        secHeader.add(getDate(date_l, getString(R.string.call_logs_date_format)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    callLogsModelAllCallsList.add(callLogsModel);


                }


            }

            return callLogsModelAllCallsList;
        }*/

        /**
         * This is used for getting specific date format
         *
         * @param milliSeconds this is used for seconds
         * @param dateFormat   this  is used for actual date
         * @return this is returns date whatever you require
         */
        private String getDate(long milliSeconds, String dateFormat) {
            // Create a DateFormatter object for displaying date in specified
            // format.
            // Create a calendar object that will convert the date and time value in

            Calendar smsTime = Calendar.getInstance();
            smsTime.setTimeInMillis(milliSeconds);
            Calendar now = Calendar.getInstance();
            String day;
            if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR) && now.get(Calendar.DATE) == smsTime.get(Calendar.DATE) && now.get(Calendar.MONTH) == smsTime.get(Calendar.MONTH)) {
                LOG.info("Month today:" + now.get(Calendar.MONTH));
                return day = "Today ";

            } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR) && now.get(Calendar.MONTH) == smsTime.get(Calendar.MONTH) && now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
                return day = "Yesterday ";

            } else {
                SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
                String format = formatter.format(smsTime.getTime());
                //day = format;
                return format;
            }
        }


        @Override
        public int getCount() {
            return sections.size();
        }

        @Override
        public Object getItem(int position) {
            return sections.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public Filter getFilter() {
            return listfilter;
        }

        class CallLogsFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                // NOTE: this function is *always* called from a background
                // thread,

                // constraint = search.getText().toString();
                FilterResults result = new FilterResults();

                if (constraint != null && constraint.toString().length() > 0) {

                    String filterString = constraint.toString().toLowerCase();

                    ArrayList<CallLogsModel> Items = new ArrayList<CallLogsModel>();
                    ArrayList<CallLogsModel> filterList = new ArrayList<CallLogsModel>();


                    synchronized (this) {

                        Items = callLogsList;
                    }
                    for (int i = 0; i < Items.size(); i++) {

                        CallLogsModel item = Items.get(i);

                        if (item.getName().toString().toLowerCase()
                                .contains(filterString.toLowerCase())) {

                            filterList.add(item);


                        }

                    }

                    result.count = filterList.size();
                    result.values = filterList;

                } else {
                    synchronized (this) {

                        result.count = callLogsList.size();
                        result.values = callLogsList;

                    }

                }
                return result;
            }

            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                @SuppressWarnings("unchecked")
                ArrayList<CallLogsModel> filtered = (ArrayList<CallLogsModel>) results.values;

                if (filtered != null) {
                    loadFilteredCallLogs(filtered);

                }

            }

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
    public static void showCallTypeDia(Context context, String numberToDial, String mydirection) {
        try {
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.call_type_dialog);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (dialog != null) {
                dialog.show();
            }
            LinearLayout pstnLayout=dialog.findViewById(R.id.pstnCallLayout);
            LinearLayout appCallLayout=dialog.findViewById(R.id.appCallLayout);
            appCallLayout.setOnClickListener(view -> {
                dialog.dismiss();
                CallMethodHelper.processAudioCall(context, numberToDial, "PSTN");
            });
            pstnLayout.setOnClickListener(view -> {
                dialog.dismiss();
                CallMethodHelper.processAudioCall(context, numberToDial, "PSTN");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
