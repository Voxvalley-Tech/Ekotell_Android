package com.app.ekottel.activity;

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
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.fragment.ContactsFragment;
import com.app.ekottel.model.ContactsDetailsModel;
import com.app.ekottel.utils.CallMethodHelper;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSDbFields;
import com.ca.Utils.CSEvents;
import com.ca.wrapper.CSDataProvider;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.InputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * This activity is used to display each contact details information.
 *
 * @author Ramesh U
 * @version 2017
 */
public class ContactDetailsActivity extends AppCompatActivity {
    ListView contactDetailsListView;
    private TextView mTvName, mTvStatus, mTvBackScreen, mTvEditContact, mTvContactDetailsName;
    private ImageView mIvStatus, mIvContactDetailsAppContact;
    private RelativeLayout mRlEditContact;
    String contactID;
    String isAppContact;
    private CircleImageView mIvContactImage;
    ArrayList<ContactsDetailsModel> contactsDetailsArray = new ArrayList<ContactsDetailsModel>();

    Bitmap mPhoto = null;
    ContactsDetailsAdapter contactDetailsAdapter;
    private String TAG="ContactDetailsActivity";
    private String contactNumber;
    private String chatPage = null;
    private boolean duplicateContact = false;
    private String profileName = null;
    private String name = null;
    private boolean isProfileImageAvailable = false;
    private boolean iscameFromAllContacts = true;
    private String contactName = "";
    private PreferenceProvider prefprovider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        prefprovider = new PreferenceProvider(ContactDetailsActivity.this);
        contactDetailsListView = (ListView) findViewById(R.id.contactdetails_listview);
        mTvName = (TextView) findViewById(R.id.tv_contact_details_name);
        mTvStatus = (TextView) findViewById(R.id.tv_contact_details_status);
        mTvStatus.setSelected(true);
        LinearLayout mLLBackScreen = (LinearLayout) findViewById(R.id.ll_contact_details_back);
        mTvBackScreen = (TextView) findViewById(R.id.tv_contact_details_back);
        mTvEditContact = (TextView) findViewById(R.id.tv_edit_contact);
        mTvContactDetailsName = (TextView) findViewById(R.id.tv_contact_details_name);

        mIvContactImage = (CircleImageView) findViewById(R.id.iv_contact_details);
        mIvContactDetailsAppContact = (ImageView) findViewById(R.id.iv_contact_details_app_contact);

        mIvStatus = (ImageView) findViewById(R.id.iv_contact_details_status);

        mRlEditContact = (RelativeLayout) findViewById(R.id.rl_edit_contact);

        Typeface text_font = Utils.getTypeface(getApplicationContext());
        mTvBackScreen.setTypeface(text_font);
        mTvEditContact.setTypeface(text_font);

        mTvBackScreen.setText(getResources().getString(R.string.contacts_details_back_arrow));
        mTvEditContact.setText(getResources().getString(R.string.contacts_details_edit_contact));

        Bundle data = getIntent().getExtras();

        contactID = data.getString(getString(R.string.chat_screen_intent_contact_id));
        isAppContact = data.getString(getString(R.string.chat_screen_intent_is_app_contact));
        contactNumber = data.getString("contactNumber");
        contactName = data.getString("contactName");
        chatPage = data.getString("ChatPage");
        iscameFromAllContacts = getIntent().getBooleanExtra(getString(R.string.iscameFromAllContacts), false);
        if (contactName != null && !contactName.equals("")) {
            mTvContactDetailsName.setText(contactName);
        } else {
            mTvContactDetailsName.setText(contactNumber);
        }
        LOG.info("Contact ID: "+contactID);
        LOG.info("isAppContact " + isAppContact + " contactNumber " + contactNumber + " iscameFromAllContacts " + iscameFromAllContacts);

        /*if (isAppContact != null && isAppContact.equalsIgnoreCase("1")) {
            mIvContactDetailsAppContact.setVisibility(View.VISIBLE);
        } else {
            mIvContactDetailsAppContact.setVisibility(View.INVISIBLE);
        }*/
        final PreferenceProvider pf = new PreferenceProvider(getApplicationContext());

        try {

            MainActivityReceiverObj = new MainActivityReceiver();
            IntentFilter filter = new IntentFilter(CSEvents.CSCLIENT_USERPROFILECHANGED);
            IntentFilter filter7 = new IntentFilter(CSEvents.CSCONTACTS_CONTACTSUPDATED);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(MainActivityReceiverObj, filter7);

        } catch (Exception e) {
            e.printStackTrace();
        }

        updateContactsView();

        mIvContactImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isProfileImageAvailable) {
                    Intent profileIntent = new Intent(ContactDetailsActivity.this, ProfileImageActivity.class);
                    profileIntent.putExtra("profileContactNumber", contactNumber);
                    startActivity(profileIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "Profile image not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mRlEditContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    if (contactID != null) {
                        pf.setPrefboolean("isEditClick", true);
                        Intent intent = new Intent(Intent.ACTION_EDIT);
                        Uri uri = Uri.withAppendedPath(
                                ContactsContract.Contacts.CONTENT_URI, contactID);
                        intent.setData(uri);
                        startActivity(intent);
                    } else {
                        Intent in = new Intent(Intent.ACTION_INSERT);
                        in.setType(ContactsContract.Contacts.CONTENT_TYPE);
                        in.putExtra(ContactsContract.Intents.Insert.PHONE, contactNumber);
                        if (profileName != null)
                            in.putExtra(ContactsContract.Intents.Insert.NAME, profileName);
                        startActivity(in);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mLLBackScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    /**
     * Method is used to get list of contact details
     *
     * @param contactID this is used to get list of contacts
     */
    private void GetNumberDetails(String contactID) {
        try {
            contactsDetailsArray.clear();
            ArrayList<ContactsDetailsModel> contactsdetailsEmailarray = new ArrayList<ContactsDetailsModel>();
            ContentResolver cr = getContentResolver();
            Cursor phone = getApplicationContext().getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{contactID}, null);


            if (phone != null) {

                while (phone.moveToNext()) {

                    String phoneNo = phone
                            .getString(phone
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    String type = phone
                            .getString(phone
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));


                    String contactName = phone
                            .getString(phone
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                    try {
                        Uri contactPhotoUri = ContentUris.withAppendedId(
                                ContactsContract.Contacts.CONTENT_URI, Integer.parseInt(contactID));
                        InputStream photoDataStream = ContactsContract.Contacts
                                .openContactPhotoInputStream(
                                        getApplicationContext()
                                                .getContentResolver(),
                                        contactPhotoUri, true);
                        if (photoDataStream != null) {
                            mPhoto = BitmapFactory.decodeStream(photoDataStream);
                        }

                        if (isAppContact != null && isAppContact.equalsIgnoreCase("1")) {

                            Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, contactNumber);
                            String profile_presence_status = null;
                            String profile_presence_name = null;
                            if (cur.getCount() > 0) {
                                cur.moveToNext();
                                profile_presence_status = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_DESCRIPTION));
                                profile_presence_name = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_USERNAME));
                            }
                            cur.close();


                            mTvStatus.setVisibility(View.VISIBLE);
                            if (profile_presence_status != null) {
                                mTvStatus.setText(profile_presence_status);
                            } else {
                                mTvStatus.setVisibility(View.GONE);
                            }

                            Cursor cur1 = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, contactNumber);

                            if (cur1.getCount() > 0) {

                                cur1.moveToNext();
                                String picid = cur1.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
                                Bitmap mybitmap = CSDataProvider.getImageBitmap(picid);
                                if (mybitmap != null) {
                                    isProfileImageAvailable = true;
                                    mIvContactImage.setImageBitmap(mybitmap);
                                } else {
                                    if (mPhoto != null) {
                                        isProfileImageAvailable = true;
                                        mIvContactImage.setImageBitmap(mPhoto);
                                    } else {
                                        mIvContactImage.setImageResource(R.drawable.ic_status_profile_avathar);
                                    }

                                }

                            } else {
                                Bitmap contactbitmap = Utils.getContactImage(getApplicationContext(),
                                        contactNumber);
                                if (contactbitmap != null) {
                                    isProfileImageAvailable = true;
                                    mIvContactImage.setImageBitmap(contactbitmap);
                                } else {
                                    mIvContactImage.setImageResource(R.drawable.ic_status_profile_avathar);
                                }
                            }
                            cur1.close();
                        } else {
                            Bitmap contactbitmap = Utils.getContactImage(getApplicationContext(),
                                    contactNumber);
                            if (contactbitmap != null) {
                                isProfileImageAvailable = true;
                                mIvContactImage.setImageBitmap(contactbitmap);
                            } else {
                                mIvContactImage.setImageResource(R.drawable.ic_status_profile_avathar);
                            }
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ContactsDetailsModel CDModel = new ContactsDetailsModel();
                    String countryCode = prefprovider.getPrefString(PreferenceProvider.USER_REGISTRED_COUNTRY_CODE);
                    if (countryCode != null && countryCode.length() == 0) {
                        countryCode = getCountryCode();
                    }
                    String number = Utils.getInternationFormatNumber(phoneNo, countryCode);
                    LOG.info("getInternationFormatNumber result " + number);
                    if (number != null && number.length() > 0) {
                        CDModel.setContactNumber(number);
                    } else {
                        CDModel.setContactNumber(phoneNo);
                    }
                    CDModel.setContactType(type);

                    String phoneNumber = null;
                    String actualNumber = null;
                    if (contactsDetailsArray.size() == 0) {
                        contactsDetailsArray.add(CDModel);
                    } else if (contactsDetailsArray.size() > 0) {
                        for (int i = 0; i < contactsDetailsArray.size(); i++) {
                            phoneNumber = phoneNo.replaceAll("[^0-9]", "");
                            actualNumber = contactsDetailsArray.get(i).getContactNumber().replaceAll("[^0-9]", "");

                            if (phoneNumber != null && actualNumber.contains(phoneNumber)) {
                                duplicateContact = true;
                            }
                        }
                        if (!duplicateContact) {
                            duplicateContact = false;
                            contactsDetailsArray.add(CDModel);
                        }
                    }


                }

                Cursor emailCur = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{contactID}, null);
                String email;
                if (emailCur != null) {
                    while (emailCur.moveToNext()) {
                        // This would allow you get several email addresses
                        // if the email addresses were stored in an array
                        email = emailCur.getString(
                                emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

                        ContactsDetailsModel CDModel = new ContactsDetailsModel();
                        if (email != null) {
                            CDModel.setContactEmail(email);
                            contactsdetailsEmailarray.add(CDModel);

                        }


                    }
                    emailCur.close();
                }


                phone.close();


                contactsDetailsArray.addAll(contactsdetailsEmailarray);

                // If contact not exists adding number which is given by SDK.
                if(contactsDetailsArray.size()==0){
                    ContactsDetailsModel contactsDetailsModel=new ContactsDetailsModel();
                    contactsDetailsModel.setContactNumber(contactNumber);
                    contactsDetailsArray.add(contactsDetailsModel);
                }

                contactDetailsAdapter = new ContactsDetailsAdapter(
                        ContactDetailsActivity.this, contactsDetailsArray);

                contactDetailsListView.setAdapter(contactDetailsAdapter);
                if (contactDetailsAdapter != null)
                    contactDetailsAdapter.notifyDataSetChanged();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showDataWithoutID(String phoneNo) {


        try {
            contactsDetailsArray.clear();

            if (isAppContact != null && isAppContact.equalsIgnoreCase("1")) {

                Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, contactNumber);
                String profile_presence_status = null;
                String profile_presence_name = null;
                //String profile_pic_id = null;
                if (cur.getCount() > 0) {
                    cur.moveToNext();
                    profile_presence_status = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_DESCRIPTION));
                    profile_presence_name = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_USERNAME));

                }
                cur.close();


                mTvStatus.setVisibility(View.VISIBLE);
                if (profile_presence_status != null) {
                    mTvStatus.setText(profile_presence_status);
                } else {
                    mTvStatus.setVisibility(View.GONE);
                }

                Cursor cur1 = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, contactNumber);

                if (cur1.getCount() > 0) {

                    cur1.moveToNext();
                    String picid = cur1.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
                    Bitmap mybitmap = CSDataProvider.getImageBitmap(picid);
                    if (mybitmap != null) {
                        isProfileImageAvailable = true;

                        mIvContactImage.setImageBitmap(mybitmap);
                    } else {
                        Bitmap contactbitmap = Utils.getContactImage(getApplicationContext(),
                                contactNumber);
                        if (contactbitmap != null) {
                            isProfileImageAvailable = true;
                            mIvContactImage.setImageBitmap(contactbitmap);
                        } else {
                            mIvContactImage.setImageResource(R.drawable.ic_status_profile_avathar);
                        }
                    }

                } else {
                    Bitmap contactbitmap = Utils.getContactImage(getApplicationContext(),
                            contactNumber);
                    if (contactbitmap != null) {
                        isProfileImageAvailable = true;
                        mIvContactImage.setImageBitmap(contactbitmap);
                    } else {
                        mIvContactImage.setImageResource(R.drawable.ic_status_profile_avathar);
                    }
                }
                cur1.close();
            } else if (mPhoto != null) {
                LOG.info("image_details");
                mIvContactImage.setImageBitmap(mPhoto);
                isProfileImageAvailable = true;
            } else {
                mIvContactImage.setImageResource(R.drawable.ic_status_profile_avathar);

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        ContactsDetailsModel CDModel = new ContactsDetailsModel();
        CDModel.setContactNumber(phoneNo);

        String phoneNumber = null;
        String actualNumber = null;
        if (contactsDetailsArray.size() == 0) {
            contactsDetailsArray.add(CDModel);
        } else if (contactsDetailsArray.size() > 0) {
            for (int i = 0; i < contactsDetailsArray.size(); i++) {
                phoneNumber = phoneNo.replaceAll("[^0-9]", "");
                actualNumber = contactsDetailsArray.get(i).getContactNumber().replaceAll("[^0-9]", "");

                if (phoneNumber != null && actualNumber.contains(phoneNumber)) {
                    duplicateContact = true;
                }
            }
            if (!duplicateContact) {
                duplicateContact = false;
                contactsDetailsArray.add(CDModel);
            }
        }

        contactDetailsAdapter = new ContactsDetailsAdapter(
                ContactDetailsActivity.this, contactsDetailsArray);

        contactDetailsListView.setAdapter(contactDetailsAdapter);
        if (contactDetailsAdapter != null)
            contactDetailsAdapter.notifyDataSetChanged();

    }


    ArrayList<ContactsDetailsModel> contactsdetailsList;
    Context mcontext;
    private LayoutInflater inflater = null;

    ConnectivityManager manager;

    public class ContactsDetailsAdapter extends BaseAdapter {


        public ContactsDetailsAdapter(Context context,
                                      ArrayList<ContactsDetailsModel> contactsdetailsarray) {

            mcontext = context;
            contactsdetailsList = contactsdetailsarray;
            prefprovider = new PreferenceProvider(context);

            inflater = (LayoutInflater) mcontext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            manager = (ConnectivityManager) getSystemService(mcontext.CONNECTIVITY_SERVICE);


        }

        @Override
        public int getCount() {

            if (contactsdetailsList != null && contactsdetailsList.size() > 0) {

                return contactsdetailsList.size();

            } else {

                return 0;
            }

        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class Holder {

            TextView phonenumber_tv;
            TextView contacttype_tv;
            LinearLayout ll_contact;

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Holder holder = new Holder();

            View rowView;

            rowView = inflater.inflate(R.layout.contactdetails_list_item, null);

            holder.phonenumber_tv = (TextView) rowView
                    .findViewById(R.id.contacts_dtails_phonenumber_tv);
            holder.contacttype_tv = (TextView) rowView
                    .findViewById(R.id.textView_remote_image);

            holder.ll_contact = (LinearLayout) rowView
                    .findViewById(R.id.listitem_linear);



            Typeface typeface = Utils.getTypeface(mcontext);

            holder.contacttype_tv.setTypeface(typeface);


            final ContactsDetailsModel cModel = contactsdetailsList.get(position);

            if (cModel.getContactNumber() != null && !cModel.getContactNumber().isEmpty()) {
                holder.ll_contact.setVisibility(View.VISIBLE);
                holder.contacttype_tv.setText(getResources().getString(R.string.signup_mobile));
                holder.phonenumber_tv.setText(cModel.getContactNumber());
            } else if (cModel.getContactEmail() != null && !cModel.getContactEmail().isEmpty()) {
                holder.ll_contact.setVisibility(View.VISIBLE);
                holder.contacttype_tv.setText(getResources().getString(R.string.contact_details_email));
                holder.phonenumber_tv.setText(cModel.getContactEmail());
            } else {
                holder.ll_contact.setVisibility(View.GONE);
            }


            holder.ll_contact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (prefprovider.getPrefBoolean(getString(R.string.call_logs_pref_gsm_key))) {

                        Toast.makeText(getApplicationContext(),
                                getString(R.string.gsm_message),
                                Toast.LENGTH_SHORT).show();

                        return;
                    }

                    if (cModel.getContactNumber() != null && !cModel.getContactNumber().isEmpty()) {
                        if (iscameFromAllContacts && isAppContact != null && isAppContact.equalsIgnoreCase("1")) {
                            CallMethodHelper.processAudioCall(ContactDetailsActivity.this, cModel.getContactNumber().trim(), "PSTN");
                        } else {
                            CallMethodHelper.processAudioCall(ContactDetailsActivity.this, cModel.getContactNumber().trim(), "PSTN");
                        }
                    }


                }
            });


            return rowView;
        }
    }

    /**
     * Method that is handle app to app call is initiate
     *
     * @param numbertodial this is mobile number to initiate call
     */

    MainActivityReceiver MainActivityReceiverObj = new MainActivityReceiver();

    @Override
    protected void onResume() {
        super.onResume();

        duplicateContact = false;
    }

    /**
     * This method is used to handle call backs i.e., network chat receive,chat update and presence
     */
    public class MainActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                LOG.info("SDK callback called, Action: "+intent.getAction());
                if (intent.getAction().equals(CSEvents.CSCLIENT_USERPROFILECHANGED)) {
                    try {
                        Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, contactNumber);
                        String profile_presence_status = null;
                        String profile_presence_name = null;
                        if (cur.getCount() > 0) {
                            cur.moveToNext();
                            profile_presence_status = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_SELF_PROFILE_DESCRIPTION));
                            profile_presence_name = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_SELF_PROFILE_USERNAME));

                        }
                        cur.close();
                        mTvStatus.setVisibility(View.VISIBLE);
                        if (profile_presence_status != null) {
                            mTvStatus.setText(profile_presence_status);
                        } else {
                            mTvStatus.setVisibility(View.GONE);
                        }

                        Cursor cur1 = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, contactNumber);

                        if (cur1.getCount() > 0) {

                            cur1.moveToNext();
                            String picid = cur1.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
                            Bitmap mybitmap = CSDataProvider.getImageBitmap(picid);
                            if (mybitmap != null) {

                                isProfileImageAvailable = true;
                                mIvContactImage.setImageBitmap(mybitmap);
                            } else {
                                Bitmap contactbitmap = Utils.getContactImage(getApplicationContext(),
                                        contactNumber);
                                if (contactbitmap != null) {
                                    isProfileImageAvailable = true;
                                    mIvContactImage.setImageBitmap(contactbitmap);
                                } else {
                                    mIvContactImage.setImageResource(R.drawable.ic_status_profile_avathar);
                                }

                            }

                        } else {
                            Bitmap contactbitmap = Utils.getContactImage(getApplicationContext(),
                                    contactNumber);
                            if (contactbitmap != null) {
                                isProfileImageAvailable = true;
                                mIvContactImage.setImageBitmap(contactbitmap);
                            } else {
                                mIvContactImage.setImageResource(R.drawable.ic_status_profile_avathar);
                            }
                        }
                        cur1.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else if(intent.getAction().equals(CSEvents.CSCONTACTS_CONTACTSUPDATED)){
                    if(prefprovider.getPrefBoolean("isEditClick")) {
                        updateContactsView();
                    }
                }


            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ContactsFragment.isContactClick = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(MainActivityReceiverObj);
    }

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

    private void updateContactsView() {

        try {
            if (chatPage != null) {
                if (contactID != null && !contactID.isEmpty()) {
                    GetNumberDetails(contactID);
                } else {

                    showDataWithoutID(contactNumber);
                }
            } else {
                if (!iscameFromAllContacts) {
                    GetNumberDetails(contactID);
                } else {
                    showDataWithoutID(contactNumber);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
