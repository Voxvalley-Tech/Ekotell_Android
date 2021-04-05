package com.app.ekottel.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.Editable;
import android.text.TextWatcher;

import static com.app.ekottel.utils.GlobalVariables.LOG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.activity.DialpadActivity;
import com.app.ekottel.adapter.ContactsAdapter;
import com.app.ekottel.adapter.ContactsSectionAdapter;
import com.app.ekottel.model.ContactsDetailsModel;
import com.app.ekottel.utils.PermissionUtils;
import com.app.ekottel.utils.PreferenceProvider;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;

/**
 * This class loads device and app contacts on UI.
 *
 * @author Ramesh and Ramesh Reddy
 */

public class ContactsFragment extends Fragment {
    private String TAG = "ContactsFragment";
    private FragmentActivity mActivity;
    private TextView mTvSearch, mTvAllContacts, mTvTringyContacts, mTvNoContacts;
    public EditText mEtSearch;
    private TextView mSearchCancel;
    private ImageView mIvDialpad;
    private InputMethodManager mInputMethodManager;
    private ListView mListView;
    private boolean mIsAppContact = false;
    private ContactsAdapter appContactsAdapter;
    private View mContactsView;
    private ArrayList<ContactsDetailsModel> mContactList = new ArrayList<>();
    private ArrayList<ContactsDetailsModel> mDeviceContacts = new ArrayList<>();
    private ArrayList<ContactsDetailsModel> mAppContacts = new ArrayList<>();
    private ArrayList<String> secHeader = new ArrayList<>();
    private boolean isContactsListUpdate = false;

    public static boolean isContactClick = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (mContactsView == null) {
            mContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
            mTvSearch = mContactsView.findViewById(R.id.tv_contacts_search);
            mEtSearch = mContactsView.findViewById(R.id.et_contacts_search);
            mTvNoContacts = mContactsView.findViewById(R.id.tv_nocontacts);
            mTvAllContacts = mContactsView.findViewById(R.id.tv_all_contacts);
            mTvTringyContacts = mContactsView.findViewById(R.id.tv_tringy_contacts);
            mIvDialpad = mContactsView.findViewById(R.id.iv_contacts_dialpad);
            mListView = mContactsView.findViewById(R.id.lv_contacts);
            mSearchCancel = mContactsView.findViewById(R.id.tv_contacts_search_cancel);
            getActivity().registerReceiver(loadContactsReceiver, new IntentFilter("LoadContacts"));
            if (mActivity != null) {
                mInputMethodManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                mTvAllContacts.setSelected(true);
               /* mInputMethodManager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                Typeface text_font = Utils.getTypeface(mActivity.getApplicationContext());
                mTvSearch.setTypeface(text_font);
                mTvAllContacts.setSelected(true);
                mSearchCancel.setTypeface(text_font);*/
            }
            mTvNoContacts.setVisibility(View.INVISIBLE);


            // This method loads device contacts and app contacts.


            LOG.info("Load Contacts repeate");
            isContactsListUpdate = true;
            loadContacts(true);

            new Handler().postDelayed(new Runnable() { // This handler written to update contacts UI first time login
                @Override
                public void run() {

                    try {
                        updateUI(getString(R.string.contact_update_no_logs));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 2000);

            mTvSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                    mEtSearch.requestFocus();
                    mEtSearch.setCursorVisible(true);
                }
            });

            mIvDialpad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (mActivity != null) {
                        Intent intent_dialpad = new Intent(mActivity.getApplicationContext(), DialpadActivity.class);
                        startActivity(intent_dialpad);
                    }

                }
            });

            mTvAllContacts.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mTvAllContacts.setSelected(true);
                    mTvTringyContacts.setSelected(false);
                    if (!PermissionUtils.checkReadContactsPermission(getActivity())) {
                        mTvNoContacts.setVisibility(View.VISIBLE);
                        mTvNoContacts.setText(getString(R.string.contacts_permission_not_available));

                    } else {
                        if (mIsAppContact) {
                            mIsAppContact = false;
                            mEtSearch.setText("");


                            if (mDeviceContacts != null && mDeviceContacts.size() > 0) {
                                loadContacts(false);
                            } else {
                                loadContacts(true);
                            }
                        }
                    }

                }
            });

            mTvTringyContacts.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mTvTringyContacts.setSelected(true);
                    mTvAllContacts.setSelected(false);
                    if (!PermissionUtils.checkReadContactsPermission(getActivity())) {
                        mTvNoContacts.setVisibility(View.VISIBLE);
                        mTvNoContacts.setText(getString(R.string.contacts_permission_not_available));
                    } else {
                        if (!mIsAppContact) {
                            mIsAppContact = true;
                            mEtSearch.setText("");
                            if (mAppContacts != null && mAppContacts.size() > 0) {
                                loadContacts(false);
                            } else {
                                loadContacts(true);
                            }
                        }
                    }


                }
            });

            mSearchCancel.setOnClickListener(new View.OnClickListener() {
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
                        mSearchCancel.setVisibility(View.VISIBLE);
                        mTvSearch.setVisibility(View.INVISIBLE);
                    } else {
                        mTvSearch.setVisibility(View.VISIBLE);
                        mSearchCancel.setVisibility(View.INVISIBLE);
                    }
                    LOG.info("Search TextChangeListener search text:" + mEtSearch.getText().toString());
                    loadContacts(true);
                    if (appContactsAdapter != null)
                        appContactsAdapter.notifyDataSetChanged();
                    updateUI(getString(R.string.contact_update_no_logs));

                }

                @Override
                public void afterTextChanged(Editable editable) {


                }
            });

            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_NETWORKERROR);
            IntentFilter filter5 = new IntentFilter(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE);
            IntentFilter filter6 = new IntentFilter(CSEvents.CSCLIENT_IMAGESDBUPDATED);
            IntentFilter filter7 = new IntentFilter(CSEvents.CSCONTACTS_CONTACTSUPDATED);
            IntentFilter filter8 = new IntentFilter(CSEvents.CSCLIENT_USERPROFILECHANGED);
            IntentFilter filter9 = new IntentFilter(CSEvents.CSCONTACTSANDGROUPS_CANDGUPDATED);

            if (mActivity != null) {
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(MainActivityReceiverObj, filter);
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(MainActivityReceiverObj, filter5);
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(MainActivityReceiverObj, filter6);
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(MainActivityReceiverObj, filter7);
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(MainActivityReceiverObj, filter8);
                LocalBroadcastManager.getInstance(mActivity).registerReceiver(MainActivityReceiverObj, filter9);
            }

        }

        return mContactsView;
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            if (mActivity != null)
                LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(MainActivityReceiverObj);
            if (loadContactsReceiver != null) {
                getActivity().unregisterReceiver(loadContactsReceiver);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * This method is handle to update UI whatever call back comes
     *
     * @param str this is used for display message
     */

    public void updateUI(String str) {

        try {
            if (str.equals(CSEvents.CSCLIENT_NETWORKERROR)) {


            } else if (str.equals(getString(R.string.contact_update_no_logs))) {

                Cursor ccr = null;
                try {
                    LOG.info(getString(R.string.contact_update_no_logs));

                    if (!mEtSearch.getText().toString().equals("")) {
                        ccr = CSDataProvider.getSearchContactsCursor(mEtSearch.getText().toString());
                    } else {
                        ccr = CSDataProvider.getContactsCursor();
                    }

                    if (ccr.getCount() <= 0) {
                        mTvNoContacts.setVisibility(View.VISIBLE);
                        if (PermissionUtils.checkReadContactsPermission(getActivity())) {
                            mTvNoContacts.setText(getString(R.string.contact_no_contacts_found));
                        } else {
                            mTvNoContacts.setText(getString(R.string.contacts_permission_not_available));
                        }
                    } else {
                        if (!PermissionUtils.checkReadContactsPermission(getActivity())) {
                            mTvNoContacts.setVisibility(View.VISIBLE);
                            mTvNoContacts.setText(getString(R.string.contacts_permission_not_available));
                        } else {
                            mTvNoContacts.setVisibility(View.INVISIBLE);
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (ccr != null)
                        ccr.close();
                }

            } else if (str.equals(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE) || str.equals(CSEvents.CSCONTACTS_CONTACTSUPDATED) || str.equals(CSEvents.CSCLIENT_IMAGESDBUPDATED) || str.equals(CSEvents.CSCLIENT_USERPROFILECHANGED)) {
                LOG.info("isAppContactRessuccess or imagesdbupdated or contactsupdated");

                loadContacts(true);

                if (appContactsAdapter != null)
                    appContactsAdapter.notifyDataSetChanged();
                updateUI(getString(R.string.contact_update_no_logs));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    public void onResume() {
        super.onResume();

        if (!getUserVisibleHint()) {
            return;
        }
        LOG.info("onResume Called");
        PreferenceProvider pf = new PreferenceProvider(getActivity());
        if (mInputMethodManager != null) {
            mInputMethodManager.hideSoftInputFromWindow(
                    mEtSearch.getWindowToken(), 0);
        }
    }

    /**
     * This method is used to handle call backs
     */
    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {

                if (!isAdded() || mActivity == null) {
                    return;
                }

                LOG.info("We got callBack to update contacts UI,  Action:" + intent.getAction());

                if (intent.getAction().equals(CSEvents.CSCLIENT_NETWORKERROR)) {
                    updateUI(CSEvents.CSCLIENT_NETWORKERROR);
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_IMAGESDBUPDATED)) {
                    updateUI(CSEvents.CSCLIENT_IMAGESDBUPDATED);
                } else if (intent.getAction().equals(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE)) {
                    if (intent.getStringExtra(CSConstants.RESULT).equals(CSConstants.RESULT_SUCCESS)) {
                        updateUI(CSEvents.CSCONTACTS_ISAPPCONTACT_RESPONSE);
                    }
                } else if (intent.getAction().equals(CSEvents.CSCONTACTSANDGROUPS_CANDGUPDATED)) {
                    updateUI(CSEvents.CSCONTACTSANDGROUPS_CANDGUPDATED);
                } else if (intent.getAction().equals(CSEvents.CSCONTACTS_CONTACTSUPDATED)) {
                    updateUI(CSEvents.CSCONTACTS_CONTACTSUPDATED);
                } else if (intent.getAction().equals(CSEvents.CSCLIENT_USERPROFILECHANGED)) {
                    updateUI(CSEvents.CSCLIENT_USERPROFILECHANGED);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }


    /**
     * This method is used to load all contacts and app contacts
     */
    private void loadContacts(boolean loadFreshData) {

        LOG.info("loadContacts, loadFreshData: " + loadFreshData);
        try {

            secHeader.clear();
            String searchText = mEtSearch.getText().toString();

            if (loadFreshData) {
                mContactList.clear();

                Cursor cursor;
                if (mIsAppContact) {
                    if (searchText.length() > 0) {
                        cursor = CSDataProvider.getSearchAppContactsCursor(searchText);
                    } else {
                        cursor = CSDataProvider.getAppContactsCursor();
                    }
                } else {
                    if (searchText.length() > 0) {
                        cursor = CSDataProvider.getSearchContactsCursor(searchText);
                    } else {
                        cursor = CSDataProvider.getContactsCursor();
                    }
                }
                try {
                    for (int i = 0; i < cursor.getCount(); i++) {
                        Log.e("cursor","Exception--->"+cursor.getCount());
                        cursor.moveToNext();
                        ContactsDetailsModel contactDetails = new ContactsDetailsModel();

                        contactDetails.setContactID(cursor.getString(cursor.getColumnIndex(CSDbFields.KEY_CONTACT_ID)));
                        contactDetails.setContactName(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME)));
                        contactDetails.setContactNumber(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NUMBER)));
                        contactDetails.setIsAppContact(cursor.getString(cursor.getColumnIndex(CSDbFields.KEY_CONTACT_IS_APP_CONTACT)));

                        //LOG.info("Contacts Id Home" + cursor.getString(cursor.getColumnIndex(CSDbFields.KEY_CONTACT_ID)));
                        mContactList.add(contactDetails);

                        secHeader.add(contactDetails.getContactName());

                    }
                }catch (Exception e){
                   // Log.e("Exception","Exception--->"+e);
                }

                if (cursor != null)
                    cursor.close();

                if (mIsAppContact) {
                    mAppContacts = mContactList;

                } else {
                    mDeviceContacts = mContactList;
                }
            } else {
                if (mIsAppContact) {
                    mContactList = mAppContacts;
                } else {
                    mContactList = mDeviceContacts;
                }
                // Adding fresh contacts to Section view.
                for (int i = 0; i < mContactList.size(); i++) {
                    secHeader.add(mContactList.get(i).getContactName());
                }
            }

            appContactsAdapter = new ContactsAdapter(mActivity, mContactList, 0, mIsAppContact);
            ContactsSectionAdapter sectionAdapter = new ContactsSectionAdapter(
                    mActivity.getApplicationContext(), mActivity.getLayoutInflater(), appContactsAdapter, secHeader);
            mListView.setAdapter(sectionAdapter);

            if (mContactList.size() > 0) {
                mTvNoContacts.setVisibility(View.GONE);
            } else {
                mTvNoContacts.setVisibility(View.VISIBLE);
                if (PermissionUtils.checkReadContactsPermission(getActivity())) {
                    mTvNoContacts.setText(getString(R.string.contact_no_contacts_found));
                } else {
                    mTvNoContacts.setText(getString(R.string.contacts_permission_not_available));
                }
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

    BroadcastReceiver loadContactsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase("LoadContacts")) {
                if (PermissionUtils.checkReadContactsPermission(getActivity())) {
                    if (mContactList.size() == 0) {
                        PreferenceProvider preferenceProvider = new PreferenceProvider(getActivity());
                        String countryCode = preferenceProvider.getPrefString(PreferenceProvider.USER_REGISTRED_COUNTRY_CODE);
                        if (countryCode != null && countryCode.length() == 0) {
                            countryCode = getCountryCode();
                        }
                        if (countryCode != null && countryCode.length() > 0) {
                            try {
                                CSClient csClient = new CSClient();
                                csClient.enableNativeContacts(true, Integer.parseInt(countryCode));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                    }
                } else {
                    mListView.setVisibility(View.GONE);
                    mTvNoContacts.setVisibility(View.VISIBLE);
                    mTvNoContacts.setText(getString(R.string.contacts_permission_not_available));
                }
            }
        }
    };

    private String getCountryCode() {
        int countryCode;
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            // phone must begin with '+'
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(CSDataProvider.getLoginID(), "");
            countryCode = numberProto.getCountryCode();
            return "" + countryCode;
        } catch (Exception e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }
        return "";
    }
}
