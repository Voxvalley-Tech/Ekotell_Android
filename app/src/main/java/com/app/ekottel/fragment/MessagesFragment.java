package com.app.ekottel.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import static com.app.ekottel.utils.GlobalVariables.LOG;
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
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.activity.AppContactsActivity;
import com.app.ekottel.activity.ChatAdvancedActivity;
import com.app.ekottel.activity.ProfileImageActivity;
import com.app.ekottel.adapter.CallLogsSectionListAdapter;
import com.app.ekottel.model.MessagesModel;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.Utils.CSExplicitEvents;
import com.ca.wrapper.CSChat;
import com.ca.wrapper.CSDataProvider;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.app.ekottel.activity.HomeScreenActivity.mUnreadCount;

/**
 * This class loads Chat list with unread counts.
 *
 * @author Ramesh
 */

public class MessagesFragment extends Fragment {

    private static String TAG = "MessagesFragment";
    public static Dialog mPopupCloseDialog = null;
    private FragmentActivity mActivity;
    private ListView mListView;
    private TextView mMessagesSearchClear;
    private TextView mTextView, mTvMessagesSearch;
    private EditText mEtMessagesSearch;
    private LinearLayout mllMessagesHeader;
    private FirstCallChatAdapter appContactsAdapter;
    private ArrayList<String> secHeader = new ArrayList<>();
    private InputMethodManager mInputMethodManager;
    private ImageView mImageView;

    private ArrayList<MessagesModel> allMessagesList = new ArrayList<MessagesModel>();
    private ArrayList<MessagesModel> messagesModelFilteredList = new ArrayList<MessagesModel>();
    private View mMessagesView;
    private PreferenceProvider mPreferenceProvider;

    public static boolean isMessageClick = false;
    private int CHAT_ACTIVITY_REQUEST_CODE = 11;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //if (mMessagesView == null) {
        mMessagesView = inflater.inflate(R.layout.fragment_messages, container, false);
        mListView = mMessagesView.findViewById(R.id.appcontacts1);
        mTextView = mMessagesView.findViewById(R.id.tv_no_messages);
        mMessagesSearchClear = mMessagesView.findViewById(R.id.tv_messages_search_cancel);
        mllMessagesHeader = mMessagesView.findViewById(R.id.ll_messages_header);
        mImageView = mMessagesView.findViewById(R.id.iv_messages_add_new);

        mTvMessagesSearch = mMessagesView.findViewById(R.id.tv_messages_search);
        mEtMessagesSearch = mMessagesView.findViewById(R.id.search_messages);

        if (mActivity != null) {
            Typeface text_font = Utils.getTypeface(mActivity);
            mTvMessagesSearch.setTypeface(text_font);
            mMessagesSearchClear.setTypeface(text_font);
            mPreferenceProvider = new PreferenceProvider(mActivity);
            mInputMethodManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        }

        int count = getCount();
        mUnreadCount.setText(count + "");
        mUnreadCount.invalidate();
        if (count > 0)
            mUnreadCount.setVisibility(View.VISIBLE);
        else
            mUnreadCount.setVisibility(View.INVISIBLE);


        loadAllMessages(true);

        if (allMessagesList.size() <= 0) {
            mllMessagesHeader.setVisibility(View.GONE);
        } else {
            mllMessagesHeader.setVisibility(View.VISIBLE);
        }

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivity != null) {
                    Intent intent = new Intent(mActivity, AppContactsActivity.class);
                    startActivity(intent);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {


                    if (true || !isMessageClick) {
                        isMessageClick = true;

                        TextView c = (TextView) view.findViewById(R.id.tv_chat_conversation_number);
                        String number = c.getText().toString();

                        if (!number.equals("")) {

                            try {

                                final View kjhkj = view;
                                if (kjhkj != null) {
                                    InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(kjhkj.getWindowToken(), 0);
                                } else {
                                    LOG.info("but view is null hideKeyboard");
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            String contact_id = "";
                            Cursor ccfr = CSDataProvider.getContactCursorByNumber(number);
                            if (ccfr.getCount() > 0) {
                                ccfr.moveToNext();
                                contact_id = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
                            }
                            if (ccfr != null)
                                ccfr.close();

                            Intent intent = new Intent(mActivity, ChatAdvancedActivity.class);
                            intent.putExtra(Constants.INTENT_CHAT_CONTACT_NUMBER, number);
                            intent.putExtra(Constants.IS_VIDEO_CALL_RUNNING, false);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivityForResult(intent, CHAT_ACTIVITY_REQUEST_CODE);

                        }
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    TextView c = (TextView) view.findViewById(R.id.tv_chat_conversation_number);
                    String number = c.getText().toString();

                    PreferenceProvider pf = new PreferenceProvider(mActivity);
                    pf.setPrefString(number, "");

                    confirmationDialog(number);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            }
        });

        mMessagesSearchClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEtMessagesSearch.getText().clear();
            }
        });
        mEtMessagesSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (mActivity != null) {

                    if (charSequence.length() > 0) {
                        mMessagesSearchClear.setVisibility(View.VISIBLE);
                    } else {
                        mMessagesSearchClear.setVisibility(View.INVISIBLE);
                    }
                    LOG.info("onTextChanged search text:" + mEtMessagesSearch.getText().toString());
                    if (!mEtMessagesSearch.getText().toString().equals("")) {
                        loadAllMessages(false);
                    } else {
                        loadAllMessages(true);
                    }

                    updateUI(getString(R.string.contact_update_no_logs));
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });
        //}

        return mMessagesView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        LOG.info("Attached ContactsFragment");
        if (context instanceof Activity) {
            mActivity = (FragmentActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }


    /**
     * Method is to load call logs and update UI
     */
    private void loadAllMessages(boolean searchAll) {

        try {
            allMessagesList.clear();
            secHeader.clear();
            Cursor chatCursor;
            if (searchAll) {
                chatCursor = CSDataProvider.getChatCursorGroupedByRemoteId();
            } else {
                chatCursor = CSDataProvider.getSearchInChatMessagesCursor(mEtMessagesSearch.getText().toString().trim());
            }
            LOG.info("Mainactivity chat updated called MessagesFragment chatCursor before");
            if (chatCursor.getCount() > 0) {
                LOG.info("Mainactivity chat updated called MessagesFragment chatCursor after");
                Calendar calendar = Calendar.getInstance();
                for (int i = 0; i < chatCursor.getCount(); i++) {

                    MessagesModel messagesModel = new MessagesModel();

                    chatCursor.moveToNext();

                    messagesModel.setName(chatCursor.getString(chatCursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_NAME)));
                    messagesModel.setNumber(chatCursor.getString(chatCursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_LOGINID)));
                    messagesModel.setLastmessage(chatCursor.getString(chatCursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MESSAGE)));
                    messagesModel.setMsg_type(chatCursor.getString(chatCursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_MSG_TYPE)));
                    messagesModel.setFileName(chatCursor.getString(chatCursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_FILE_NAME)));
                    Long dateStr = chatCursor.getLong(chatCursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_TIME));
                    if (dateStr != null) {
                        messagesModel.setTimeStr(String.valueOf(dateStr));
                    }
                    calendar.setTimeInMillis(dateStr);
                    try {


                        secHeader.add(getDate(dateStr, getString(R.string.call_logs_date_format)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String currentDate = getFormattedTime(dateStr);
                    messagesModel.setDateStr(currentDate);

                    allMessagesList.add(messagesModel);

                }

                if (chatCursor != null)
                    chatCursor.close();
            }

            LOG.info("Mainactivity chat updated called MessagesFragment chatCursor after" + allMessagesList.size());

            appContactsAdapter = new FirstCallChatAdapter(mActivity, secHeader, allMessagesList);
            CallLogsSectionListAdapter sectionAdapter = new CallLogsSectionListAdapter(mActivity, mActivity.getLayoutInflater(), appContactsAdapter);
            mListView.setAdapter(sectionAdapter);
            appContactsAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    private void loadFilteredMessgaesLogs(ArrayList<MessagesModel> filteredList) {

        loadFilteredMessages(filteredList);
        appContactsAdapter = new FirstCallChatAdapter(mActivity, secHeader, messagesModelFilteredList);
        CallLogsSectionListAdapter sectionAdapter = new CallLogsSectionListAdapter(mActivity, mActivity.getLayoutInflater(), appContactsAdapter);
        mListView.setAdapter(sectionAdapter);
        appContactsAdapter.notifyDataSetChanged();
    }

    /**
     * Method is to load list of section views
     *
     * @param filteredList This is list which handles section view
     * @return list of  section views data
     */
    private ArrayList<MessagesModel> loadFilteredMessages(ArrayList<MessagesModel> filteredList) {
        messagesModelFilteredList.clear();
        secHeader.clear();

        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < filteredList.size(); i++) {

            MessagesModel messagesModel = new MessagesModel();


            messagesModel.setName(filteredList.get(i).getName());
            messagesModel.setNumber(filteredList.get(i).getNumber());
            messagesModel.setLastmessage(filteredList.get(i).getLastmessage());
            messagesModel.setMsg_type(filteredList.get(i).getMsg_type());
            messagesModel.setDateStr(filteredList.get(i).getDateStr());
            long date_l = Long.valueOf(filteredList.get(i).getTimeStr());
            calendar.setTimeInMillis(date_l);
            try {
                secHeader.add(getDate(date_l, getString(R.string.call_logs_date_format)));
            } catch (Exception e) {
                e.printStackTrace();
            }


            messagesModelFilteredList.add(messagesModel);


        }


        return messagesModelFilteredList;
    }

    /**
     * This method is used for get count value
     *
     * @return
     */
    private int getCount() {
        Cursor cursor = CSDataProvider.getChatCursorGroupedByRemoteId();
        int badgeCount = 0;
        if (cursor.moveToNext()) {
            do {
                String destNumber = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_DESTINATION_LOGINID));
                int isGroupMessage = cursor.getInt(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CHAT_IS_GROUP_MESSAGE));
                Cursor ccr = null;
                if (isGroupMessage == 0) {
                    ccr = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(destNumber);
                } else {
                    ccr = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(destNumber);
                }
                if (ccr.getCount() > 0) {
                    badgeCount++;
                }
                ccr.close();
            } while (cursor.moveToNext());
        }
        return badgeCount;
    }

    /**
     * This method is used for update UI whatever call backs comes
     *
     * @param str
     */
    public void updateUI(String str) {

        try {

            if (str.equals(CSEvents.CSCLIENT_NETWORKERROR)) {
            } else if (str.equals(getString(R.string.contact_update_no_logs))) {
                Cursor ccr = null;
                try {
                    LOG.info(getString(R.string.contact_update_no_logs));

                    if (!mEtMessagesSearch.getText().toString().equals("")) {
                        ccr = CSDataProvider.getSearchInChatMessagesCursor(mEtMessagesSearch.getText().toString());
                    } else {
                        ccr = CSDataProvider.getChatCursorGroupedByRemoteId();
                    }
                    LOG.info("Chats Message" + ccr.getCount());
                    if (ccr.getCount() <= 0) {
                        mTextView.setVisibility(View.VISIBLE);

                    } else {
                        mTextView.setVisibility(View.INVISIBLE);
                    }
                    if (ccr != null)
                        ccr.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (ccr != null)
                        ccr.close();
                }

            } else if (str.equals(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE) || str.equals(CSEvents.CSCONTACTS_CONTACTSUPDATED) || str.equals(CSEvents.CSCHAT_CHATUPDATED) || str.equals(CSEvents.CSCLIENT_USERPROFILECHANGED) || str.equals(CSEvents.CSCLIENT_IMAGESDBUPDATED)) {
                LOG.info("isAppContactRessuccess or imagesdbupdated or contactsupdated");
                int count = getCount();
                mUnreadCount.setText(count + "");
                mUnreadCount.invalidate();
                if (count > 0)
                    mUnreadCount.setVisibility(View.VISIBLE);
                else
                    mUnreadCount.setVisibility(View.INVISIBLE);


                loadAllMessages(true);

                if (allMessagesList.size() <= 0) {
                    mllMessagesHeader.setVisibility(View.GONE);
                } else {
                    mllMessagesHeader.setVisibility(View.VISIBLE);
                }

                updateUI(getString(R.string.contact_update_no_logs));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * This class is handle call backs
     */
    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {


                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    updateUI(CSEvents.CSCLIENT_NETWORKERROR);
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_IMAGESDBUPDATED)) {
                    updateUI(CSEvents.CSCLIENT_IMAGESDBUPDATED);
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_USERPROFILECHANGED)) {
                    updateUI(CSEvents.CSCLIENT_USERPROFILECHANGED);
                } else if (intent.getAction().equals(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE)) {

                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        updateUI(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE);
                    }
                } else if (intent.getAction().equals(CSEvents.CSCONTACTS_CONTACTSUPDATED)) {
                    updateUI(CSEvents.CSCONTACTS_CONTACTSUPDATED);
                } else if (intent.getAction().equals(CSEvents.CSCHAT_CHATUPDATED) || intent.getAction().equals(CSExplicitEvents.CSChatReceiver)) {
                    updateUI(CSEvents.CSCHAT_CHATUPDATED);
                } else if (intent.getAction().equals(CSEvents.CSGROUPS_PULLGROUPDETAILS_RESPONSE)) {
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        loadAllMessages(true);
                        if (allMessagesList.size() <= 0) {
                            mllMessagesHeader.setVisibility(View.GONE);
                        } else {
                            mllMessagesHeader.setVisibility(View.VISIBLE);
                        }
                        updateUI(getString(R.string.contact_update_no_logs));

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
        LOG.info("OnResume called");
        try {
            if (!getUserVisibleHint()) {
                return;
            }
            if (mInputMethodManager != null) {
                mInputMethodManager.hideSoftInputFromWindow(
                        mEtMessagesSearch.getWindowToken(), 0);
            }
            mEtMessagesSearch.setText("");


            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter5 = new IntentFilter(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE);
            IntentFilter filter6 = new IntentFilter(CSEvents.CSCLIENT_IMAGESDBUPDATED);
            IntentFilter filter7 = new IntentFilter(CSEvents.CSCONTACTS_CONTACTSUPDATED);
            IntentFilter filter8 = new IntentFilter(CSEvents.CSCHAT_CHATUPDATED);
            IntentFilter filter9 = new IntentFilter(CSExplicitEvents.CSChatReceiver);
            IntentFilter filter10 = new IntentFilter(CSEvents.CSCLIENT_USERPROFILECHANGED);
            IntentFilter filter11 = new IntentFilter(CSEvents.CSGROUPS_PULLGROUPDETAILS_RESPONSE);

            if (mActivity != null) {
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(MainActivityReceiverObj, filter);
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(MainActivityReceiverObj, filter5);
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(MainActivityReceiverObj, filter6);
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(MainActivityReceiverObj, filter7);
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(MainActivityReceiverObj, filter8);
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(MainActivityReceiverObj, filter9);
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(MainActivityReceiverObj, filter10);
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(MainActivityReceiverObj, filter11);
            }

            boolean isChatUpdated = false;
            if (mPreferenceProvider != null)
                isChatUpdated = mPreferenceProvider.getPrefBoolean("chatUpdated");

            if (isChatUpdated) {
                if (mPreferenceProvider != null)
                    mPreferenceProvider.setPrefboolean("chatUpdated", false);
                int count = getCount();
                mUnreadCount.setText(count + "");
                mUnreadCount.invalidate();
                if (count > 0)
                    mUnreadCount.setVisibility(View.VISIBLE);
                else
                    mUnreadCount.setVisibility(View.INVISIBLE);

                loadAllMessages(true);

                if (allMessagesList.size() <= 0) {
                    mllMessagesHeader.setVisibility(View.GONE);
                } else {
                    mllMessagesHeader.setVisibility(View.VISIBLE);
                }
            }
            updateUI(getString(R.string.contact_update_no_logs));
            LOG.info("onResume is called Messages");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            if (mActivity != null) {
                LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(MainActivityReceiverObj);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    public class FirstCallChatAdapter extends BaseAdapter implements Filterable {
        private Activity context;
        ArrayList<MessagesModel> messagesList;
        LayoutInflater inflater = null;
        ArrayList<String> sections;

        FirstCallChatAdapter.MessagesFilter listfilter = new FirstCallChatAdapter.MessagesFilter();

        public FirstCallChatAdapter(Activity context,
                                    ArrayList<String> sections, ArrayList<MessagesModel> messagesList) {

            this.context = context;
            this.messagesList = messagesList;
            this.sections = sections;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        public class Holder {

            TextView tv_name, tv_message, tv_time, tv_unread_count, tv_arrow_right, tv_chat_conversation_number;
            CircleImageView image;
        }

        @Override
        public View getView(int position, View rowView, ViewGroup parent) {

            final FirstCallChatAdapter.Holder holder;

            if (rowView == null) {
                holder = new FirstCallChatAdapter.Holder();
                rowView = inflater.inflate(R.layout.firstcall_chats_row_layout, null);
                holder.tv_name = (TextView) rowView
                        .findViewById(R.id.tv_chat_name);
                holder.tv_chat_conversation_number = (TextView) rowView
                        .findViewById(R.id.tv_chat_conversation_number);
                holder.tv_message = (TextView) rowView
                        .findViewById(R.id.tv_chat_message);
                holder.tv_message.setSelected(true);
                holder.tv_time = (TextView) rowView
                        .findViewById(R.id.time);
                holder.tv_unread_count = (TextView) rowView
                        .findViewById(R.id.unreadcount);
                holder.tv_arrow_right = (TextView) rowView
                        .findViewById(R.id.tv_arrow_right);
                holder.image = (CircleImageView) rowView
                        .findViewById(R.id.iv_chat_message);


                rowView.setTag(holder);
            } else {
                holder = (FirstCallChatAdapter.Holder) rowView.getTag();
            }
            Typeface typeface = Utils.getTypeface(context);
            holder.tv_arrow_right.setTypeface(typeface);

            LOG.info("messagesList" + messagesList.size());
            LOG.info("messagesList position " + position);
            LOG.info("messagesList section " + sections.size());

            final MessagesModel cModel = messagesList.get(position);

            String name = cModel.getName();
            String number = cModel.getNumber();
            String lastmessage = cModel.getLastmessage();
            String msg_type = cModel.getMsg_type();
            String currentDate = cModel.getDateStr();

            holder.tv_chat_conversation_number.setText(number);
            LOG.info("Time in message fragment " + currentDate);
            holder.tv_time.setText(currentDate);
            String id = "";

            Cursor ccr = CSDataProvider.getChatCursorFilteredByNumberAndUnreadMessages(number);
            if (ccr.getCount() > 0) {
                holder.tv_unread_count.setVisibility(View.VISIBLE);
                holder.tv_unread_count.setText(String.valueOf(ccr.getCount()));
            }
            if (ccr != null)
                ccr.close();
            if (name.equals("")) {
                name = number;
            }

            Cursor ccfr = CSDataProvider.getContactCursorByNumber(number);
            if (ccfr.getCount() > 0) {
                ccfr.moveToNext();
                id = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
            }
            if (ccfr != null)
                ccfr.close();

            if (msg_type.equalsIgnoreCase("6")) {
                holder.tv_message.setText("Contact");
            } else if (lastmessage != null && (lastmessage.trim().endsWith("png") || lastmessage.trim().endsWith("jpg") || lastmessage.trim().endsWith("jpeg") || lastmessage.trim().endsWith("gif"))) {
                holder.tv_message.setText(getString(R.string.message_activity_message_photo));
            } else if (lastmessage.toString().contains(".doc") || lastmessage.toString().contains(".docx") || lastmessage.toString().contains(".pdf")
                    || lastmessage.toString().contains(".ppt") || lastmessage.toString().contains(".pptx") ||
                    lastmessage.toString().contains(".zip") || lastmessage.toString().contains(".rar") ||
                    lastmessage.toString().contains(".txt") ||
                    lastmessage.toString().contains(".ppt") || lastmessage.toString().contains(".pptx") ||
                    lastmessage.toString().contains(".xls") || lastmessage.toString().contains(".xlsx") || lastmessage.toString().contains(".apk")) {
                String fileName = cModel.getFileName();
                if (fileName.indexOf(".") > 0)
                    fileName = fileName.substring(0, fileName.lastIndexOf("."));
                holder.tv_message.setText(fileName);
            } else if (lastmessage.toString().contains(".wav") || lastmessage.toString().contains(".mp3")) {
                holder.tv_message.setText(getString(R.string.message_activity_message_audio));
            } else if (lastmessage.toString().contains(".3gp") || lastmessage.toString().contains(".mpg") || lastmessage.toString().contains(".mpeg") || lastmessage.toString().contains(".mpe") || lastmessage.toString().contains(".mp4") || lastmessage.toString().contains(".avi")) {
                holder.tv_message.setText(getString(R.string.message_activity_message_video));
            } else if (lastmessage.toString().contains(".aac")) {
                holder.tv_message.setText(getString(R.string.message_activity_message_audio_record));
            } else if (msg_type != null && msg_type.equalsIgnoreCase("3")) {

                holder.tv_message.setText(getString(R.string.message_activity_message_location));

            } else if (lastmessage != null && lastmessage.contains("$$$")) {
                holder.tv_message.setText(getString(R.string.message_activity_message_location));
            } else {
                holder.tv_message.setText(lastmessage);
            }
            holder.tv_name.setText(name);

            holder.image.setImageResource(R.mipmap.ic_contact_avatar);
            holder.image.setTag(number);
            if (id != null && !id.isEmpty()) {
                Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, number);

                if (cur.getCount() > 0) {

                    cur.moveToNext();
                    String picid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
                    Bitmap mybitmap = CSDataProvider.getImageBitmap(picid);

                    if (mybitmap != null) {
                        cModel.setProfilePicAvailable(true);
                        holder.image.setImageBitmap(mybitmap);
                    } else {
                        new ImageDownloaderTask(holder.image, cModel).execute(id);
                    }


                } else {
                    new ImageDownloaderTask(holder.image, cModel).execute(id);
                }
                cur.close();
            }
            final boolean isProfilePicAvailable = cModel.isProfilePicAvailable();
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isProfilePicAvailable) {
                        Intent profileIntent = new Intent(context, ProfileImageActivity.class);
                        profileIntent.putExtra("profileContactNumber", number);
                        context.startActivity(profileIntent);
                    } else {
                        Toast.makeText(context, "Profile image not available", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return rowView;
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

        public class MessagesFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                // NOTE: this function is *always* called from a background
                // thread,

                FilterResults result = new FilterResults();

                if (constraint != null && constraint.toString().length() > 0) {

                    String filterString = constraint.toString().toLowerCase();

                    ArrayList<MessagesModel> Items = new ArrayList<MessagesModel>();
                    ArrayList<MessagesModel> filterList = new ArrayList<MessagesModel>();

                    ArrayList<MessagesModel> filteredList = new ArrayList<MessagesModel>();

                    synchronized (this) {

                        Items = messagesList;
                    }
                    for (int i = 0; i < Items.size(); i++) {

                        MessagesModel item = Items.get(i);

                        if (item.getName().toString() != null && !item.getName().toString().isEmpty()) {
                            if (item.getName().toString().toLowerCase()
                                    .contains(filterString.toLowerCase())) {

                                filterList.add(item);


                            }
                        } else {
                            if (item.getNumber().toString().toLowerCase()
                                    .contains(filterString.toLowerCase())) {

                                filterList.add(item);


                            }
                        }


                    }

                    result.count = filterList.size();
                    result.values = filterList;

                } else {
                    synchronized (this) {

                        result.count = messagesList.size();
                        result.values = messagesList;

                    }

                }
                return result;
            }

            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                @SuppressWarnings("unchecked")
                ArrayList<MessagesModel> filtered = (ArrayList<MessagesModel>) results.values;

                if (filtered != null) {
                    loadFilteredMessgaesLogs(filtered);

                }

            }

        }
    }


    /**
     * This class is used for load bitmap image and update UI
     */
    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        boolean scaleit = false;
        String number = "";
        MessagesModel messagesModel;

        public ImageDownloaderTask(ImageView imageView, MessagesModel cModel) {
            number = imageView.getTag().toString();
            messagesModel = cModel;
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap photo = null;
            try {
                photo = Utils.loadContactPhoto(mActivity.getApplicationContext(), Long.parseLong(params[0]));
                if (photo == null) {
                } else {
                    scaleit = true;
                }

            } catch (Exception e) {
                e.printStackTrace();
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
                        messagesModel.setProfilePicAvailable(true);
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setImageResource(R.mipmap.ic_contact_avatar);
                    }
                }
            }
        }
    }

    /**
     * This method is handle required time format
     *
     * @param dateStr
     * @return
     */
    private String getFormattedTime(long dateStr) {

        try {
            return new SimpleDateFormat(Utils.getTimeFormatForCallLogs(getActivity())).format(dateStr);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }


    /**
     * This method is handle single chat delete alert dialog
     *
     * @param number
     */
    public void confirmationDialog(final String number) {

        try {

            mPopupCloseDialog = new Dialog(mActivity);

            mPopupCloseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mPopupCloseDialog.setContentView(R.layout.alertdialog_close);
            mPopupCloseDialog.setCancelable(false);
            mPopupCloseDialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            TextView tv_title = (TextView) mPopupCloseDialog
                    .findViewById(R.id.tv_alert_title);
            TextView tv_message = (TextView) mPopupCloseDialog
                    .findViewById(R.id.tv_alert_message);
            tv_title.setText(getString(R.string.signup_popup_confirm_title));
            tv_message.setText(getString(R.string.message_activity_delete_chat_message));


            Button yes = (Button) mPopupCloseDialog
                    .findViewById(R.id.btn_alert_ok);
            final Button no = (Button) mPopupCloseDialog
                    .findViewById(R.id.btn_alert_cancel);

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    CSChat csChat = new CSChat();
                    csChat.deleteChatMessagebyfilter(CSDbFields.KEY_CHAT_DESTINATION_LOGINID, number);
                    loadAllMessages(true);
                    int count = getCount();
                    mUnreadCount.setText(count + "");
                    mUnreadCount.invalidate();
                    if (count > 0)
                        mUnreadCount.setVisibility(View.VISIBLE);
                    else
                        mUnreadCount.setVisibility(View.INVISIBLE);
                    if (appContactsAdapter.getCount() <= 0) {
                        mllMessagesHeader.setVisibility(View.GONE);
                        mTextView.setVisibility(View.VISIBLE);

                    } else {
                        mllMessagesHeader.setVisibility(View.VISIBLE);
                        mTextView.setVisibility(View.INVISIBLE);
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

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }


}
