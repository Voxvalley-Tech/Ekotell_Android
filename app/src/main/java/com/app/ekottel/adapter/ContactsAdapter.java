package com.app.ekottel.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.activity.ChatAdvancedActivity;
import com.app.ekottel.activity.ContactDetailsActivity;
import com.app.ekottel.activity.PlayAudioCallActivity;
import com.app.ekottel.activity.ProfileImageActivity;
import com.app.ekottel.fragment.ContactsFragment;
import com.app.ekottel.model.ContactsDetailsModel;
import com.app.ekottel.utils.CallMethodHelper;
import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSDbFields;
import com.ca.wrapper.CSDataProvider;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.app.ekottel.R.id.ll_name;

/**
 * This activity is used to display contacts.
 *
 * @author Ramesh U
 * @version 2017
 */
public class ContactsAdapter extends BaseAdapter {

    private static Activity context;
    PopupWindow popUpWindowObj;
    ArrayList<ContactsDetailsModel> mContactList;
    ArrayList<ContactsDetailsModel> contactsDetailsArray = new ArrayList<>();
    boolean appContact;
    private String TAG1 = "ContactsAdapter";
    Bitmap mybitmap = null;

    public ContactsAdapter(Activity context, ArrayList<ContactsDetailsModel> contactsList, int flags, boolean appContact) {

        this.context = context;
        this.mContactList = contactsList;
        this.appContact = appContact;
    }

    class ContactsViewHolder {
        CircleImageView image;
        ImageView iv_app_contact;
        TextView tv_contacts_name;
        TextView tv_contacts_number;
        ImageView tv_call;
        TextView tv_chat;
        TextView tv_video;
        TextView tv_info;
        LinearLayout ll_name;

    }

    @Override
    public int getCount() {
        return mContactList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ContactsViewHolder contactsViewHolder;
        if (convertView == null) {
            contactsViewHolder = new ContactsViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.contacts_adapter, parent, false);

            contactsViewHolder.image = (CircleImageView) convertView.findViewById(R.id.iv_contact_details);
            contactsViewHolder.iv_app_contact = (ImageView) convertView.findViewById(R.id.iv_contact_details_app_contact);
            contactsViewHolder.tv_contacts_name = (TextView) convertView.findViewById(R.id.tv_contacts_name);
            contactsViewHolder.tv_contacts_number = (TextView) convertView.findViewById(R.id.tv_contacts_number);
            contactsViewHolder.tv_call = (ImageView) convertView.findViewById(R.id.tv_contacts_call);
            contactsViewHolder.tv_chat = (TextView) convertView.findViewById(R.id.tv_contacts_chat);
            contactsViewHolder.tv_video = (TextView) convertView.findViewById(R.id.tv_contacts_video);
            contactsViewHolder.tv_info = (TextView) convertView.findViewById(R.id.tv_contacts_info);
            contactsViewHolder.ll_name = (LinearLayout) convertView.findViewById(ll_name);

            Typeface text_font = Utils.getTypeface(context);
          //  contactsViewHolder.tv_call.setTypeface(text_font);
            contactsViewHolder.tv_chat.setTypeface(text_font);
            contactsViewHolder.tv_video.setTypeface(text_font);
            contactsViewHolder.tv_info.setTypeface(text_font);

           // contactsViewHolder.tv_call.setText(context.getString(R.string.dialpad_call));
            contactsViewHolder.tv_chat.setText(context.getString(R.string.contact_chat));
            contactsViewHolder.tv_info.setText(context.getString(R.string.contact_more));
            contactsViewHolder.tv_video.setText(context.getString(R.string.video_call_icon));

            convertView.setTag(contactsViewHolder);
        } else {
            contactsViewHolder = (ContactsViewHolder) convertView.getTag();
        }

        ContactsDetailsModel contactsDetails = mContactList.get(position);

        final String id = contactsDetails.getContactID();
        String name = contactsDetails.getContactName();
        final String number = contactsDetails.getContactNumber();
        final String isAppContact = contactsDetails.getIsAppContact();
        contactsDetails.setProfilePicAvailable(false);

        LOG.info(TAG1, "App Contact" + isAppContact);
        contactsViewHolder.tv_contacts_name.setSelected(true);

        if (isAppContact != null && isAppContact.equalsIgnoreCase("1") && appContact) {
            contactsViewHolder.iv_app_contact.setVisibility(View.GONE);
            contactsViewHolder.tv_chat.setVisibility(View.GONE);
            contactsViewHolder.tv_video.setVisibility(View.GONE);
        } else {
            contactsViewHolder.tv_chat.setVisibility(View.GONE);
            contactsViewHolder.tv_video.setVisibility(View.GONE);
            //contactsViewHolder.iv_app_contact.setVisibility(View.GONE);
        }

        contactsViewHolder.tv_contacts_name.setText(name);
        contactsViewHolder.tv_contacts_number.setText(number);
        LOG.info(TAG1, "Image ID Contacts" + id);

        contactsViewHolder.image.setImageResource(R.drawable.ic_status_profile_avathar);
        contactsViewHolder.image.setTag(number);
        if (isAppContact != null && isAppContact.equalsIgnoreCase("1") && appContact) {
            try {
                Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, number);
                if (cur.getCount() > 0) {

                    cur.moveToNext();
                    String picid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
                    mybitmap = CSDataProvider.getImageBitmap(picid);
                    if (mybitmap != null) {
                        contactsDetails.setProfilePicAvailable(true);
                        contactsViewHolder.image.setImageBitmap(mybitmap);
                    } else {
                        new ImageDownloaderTask(contactsViewHolder.image, contactsDetails).execute(id);
                    }

                } else {
                    new ImageDownloaderTask(contactsViewHolder.image, contactsDetails).execute(id);
                }
                cur.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            new ImageDownloaderTask(contactsViewHolder.image, contactsDetails).execute(id);
        }

        LOG.info(TAG1, "isProfilePicAvailable out of click " + contactsDetails.isProfilePicAvailable());
        boolean isProfilePicAvailable = contactsDetails.isProfilePicAvailable();
        contactsViewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LOG.info(TAG1, "isProfilePicAvailable " + isProfilePicAvailable);
                if (isAppContact != null && isAppContact.equalsIgnoreCase("1") && appContact) {

                    if (isProfilePicAvailable) {
                        Intent profileIntent = new Intent(context, ProfileImageActivity.class);
                        profileIntent.putExtra("profileContactNumber", number);
                        context.startActivity(profileIntent);
                    } else {
                        Toast.makeText(context, "Profile image not available", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        contactsViewHolder.tv_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    popUpWindowObj = dialog_Select(context, contactsViewHolder.tv_info, id, isAppContact, number, name);
                    popUpWindowObj.showAsDropDown(v);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        contactsViewHolder.tv_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PreferenceProvider pf = new PreferenceProvider(context);
                boolean chatPageLaunch = pf.getPrefBoolean("ChatPageLaunch");
                LOG.info(TAG1, "ChatPage2" + chatPageLaunch);
                //if (!chatPageLaunch) {
                pf.setPrefboolean("ChatPageLaunch", true);

                Intent intent = new Intent(context.getApplicationContext(), ChatAdvancedActivity.class);
                intent.putExtra(Constants.INTENT_CHAT_CONTACT_NUMBER, number);
                intent.putExtra(Constants.INTENT_CHAT_CONTACT_NAME, name);
                intent.putExtra(Constants.IS_VIDEO_CALL_RUNNING, false);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(intent);
                //   }

            }
        });
        contactsViewHolder.ll_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                GetNumberDetails(id);

                if (contactsDetailsArray != null
                        && contactsDetailsArray.size() > 1) {

                    if (!ContactsFragment.isContactClick) {
                        ContactsFragment.isContactClick = true;
                        Intent intent = new Intent(context, ContactDetailsActivity.class);
                        intent.putExtra(context.getString(R.string.chat_screen_intent_contact_id), id);
                        intent.putExtra(context.getString(R.string.chat_screen_intent_is_app_contact), isAppContact);
                        intent.putExtra(context.getString(R.string.iscameFromAllContacts), appContact);
                        intent.putExtra("contactNumber", number);
                        intent.putExtra("contactName", name);
                        context.startActivity(intent);
                    }


                } else {
                    if (appContact) {
                        CallMethodHelper.processAudioCall(context, number, "PSTN");
                    } else {
                        CallMethodHelper.processAudioCall(context, number, "PSTN");
                    }
                }


            }
        });
        contactsViewHolder.tv_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appContact) {
                    CallMethodHelper.processAudioCall(context, number, "PSTN");
                } else {
                    CallMethodHelper.processAudioCall(context, number, "PSTN");
                }
            }
        });

        contactsViewHolder.tv_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CallMethodHelper.placeVideoCall(context, number);
            }
        });

        return convertView;
    }


    /**
     * Method that is handle app to app call is initiate
     *
     * @param numbertodial this is mobile number to initiate call
     */
    private void dovoicecall(String numbertodial) {
        try {
            if (!numbertodial.equals("") && !numbertodial.equals(GlobalVariables.phoneNumber)) {
                /*if (!GlobalVariables.ispeerconnectionclosed) {
                    GlobalVariables.peerConnectionClient.close();
                    GlobalVariables.ispeerconnectionclosed = true;
                }*/
                GlobalVariables.destinationnumbettocall = numbertodial;
                GlobalVariables.ispeerconnectionclosed = false;

                if (!ContactsFragment.isContactClick) {
                    ContactsFragment.isContactClick = true;
                    Intent intent = new Intent(context.getApplicationContext(), PlayAudioCallActivity.class);
                    //intent.putExtra("isAudioVideoCall", true);
                    intent.putExtra(context.getString(R.string.call_logs_intent_dntnum_key), numbertodial);
                    intent.putExtra(context.getString(R.string.call_logs_intent_stream_subscr_key), "");
                    intent.putExtra(context.getString(R.string.call_logs_intent_initiator_key), true);
                    context.startActivityForResult(intent, 954);

                }

            } else {
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.call_logs_invalid_number), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }


    //This is used for loading image and update UI
    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        boolean scaleit = false;
        String number = "";
        ContactsDetailsModel contactsDetailsModel;

        public ImageDownloaderTask(ImageView imageView, ContactsDetailsModel contactsDetails) {
            number = imageView.getTag().toString();
            contactsDetailsModel = contactsDetails;
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap photo;
            try {
                photo = Utils.loadContactPhoto(context, Long.parseLong(params[0]));
                if (photo == null) {
                    photo = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_status_profile_avathar);
                } else {
                    scaleit = true;
                }

            } catch (Exception e) {
                photo = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_status_profile_avathar);
                e.printStackTrace();
            }
            return photo;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            try {
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
                            contactsDetailsModel.setProfilePicAvailable(true);
                            imageView.setImageBitmap(bitmap);
                        } else {
                            imageView.setImageResource(R.drawable.ic_status_profile_avathar);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //This is used to share app and details of specific contact
    private PopupWindow dialog_Select(final Context context, TextView lin, final String contactId, final String isAppContact, final String contactNumber, final String contactName) {

        final PopupWindow dialog_Select = new PopupWindow(context);

        View v = View.inflate(context, R.layout.contacts_popup, null);


        dialog_Select.setContentView(v);
        dialog_Select.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        dialog_Select.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        dialog_Select.setFocusable(true);
        dialog_Select.setBackgroundDrawable(new BitmapDrawable(
                context.getApplicationContext().getResources(),
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)));


        LinearLayout ll_details = (LinearLayout) v.findViewById(R.id.ll_contact_details);
        LinearLayout ll_invite = (LinearLayout) v.findViewById(R.id.ll_contact_invite);
        TextView tv_contact_invite = (TextView) v.findViewById(R.id.tv_contact_invite);
        TextView tv_contact_details = (TextView) v.findViewById(R.id.tv_contact_details);

        if (isAppContact != null && isAppContact.equalsIgnoreCase("1")) {
            ll_invite.setVisibility(View.GONE);
        } else {
            ll_invite.setVisibility(View.VISIBLE);
        }


        TextView tv_details = (TextView) v.findViewById(R.id.tv_contacts_details);
        TextView tv_invite = (TextView) v.findViewById(R.id.tv_contacts_invite);

        Typeface typeface = Utils.getTypeface(context);
        tv_details.setTypeface(typeface);
        tv_invite.setTypeface(typeface);

        tv_details.setText(context.getResources().getString(R.string.contact_details));
        tv_invite.setText(context.getResources().getString(R.string.contact_invite));


        ll_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null)
                    dialog_Select.dismiss();
                Intent intent = new Intent(context, ContactDetailsActivity.class);
                intent.putExtra(context.getString(R.string.chat_screen_intent_contact_id), contactId);
                intent.putExtra(context.getString(R.string.chat_screen_intent_is_app_contact), isAppContact);
                intent.putExtra(context.getString(R.string.iscameFromAllContacts), appContact);
                intent.putExtra("contactNumber", contactNumber);
                intent.putExtra("contactName", contactName);
                context.startActivity(intent);
            }
        });

        ll_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_Select != null)
                    dialog_Select.dismiss();

                try {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                        smsIntent.setType(Constants.CONTACTS_SMS_TYPE_URL);
                        smsIntent.putExtra(Constants.CONTACTS_SMS_BODY_KEY_URL, Constants.CONTACTS_SMS_BODY_VALUE_URL);
                        smsIntent.putExtra(context.getString(R.string.contact_intent_address_key), contactNumber);
                        smsIntent.setData(Uri.parse("sms:"+contactNumber));
                        if (smsIntent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(smsIntent);
                        } else {
                            Toast.makeText(context, context.getString(R.string.contact_no_sms_app_available), Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                        smsIntent.setType(Constants.CONTACTS_SMS_TYPE_URL);
                        smsIntent.putExtra(Constants.CONTACTS_SMS_BODY_KEY_URL, Constants.CONTACTS_SMS_BODY_VALUE_URL);
                        smsIntent.putExtra(context.getString(R.string.contact_intent_address_key), contactNumber);
                        if (smsIntent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(smsIntent);
                        } else {
                            Toast.makeText(context, context.getString(R.string.contact_no_sms_app_available), Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


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


    //This is used for check if given contact contains single or more contacts
    private void GetNumberDetails(String contactID) {
        contactsDetailsArray.clear();
        ArrayList<ContactsDetailsModel> contactsdetailsEmailarray = new ArrayList<ContactsDetailsModel>();
        ContentResolver cr = context.getContentResolver();
        Cursor phone = context.getApplicationContext().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactID}, null);

        int i = 0;


        if (phone != null) {

            while (phone.moveToNext()) {

                String phoneNo = phone
                        .getString(phone
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                String type = phone
                        .getString(phone
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));


                ContactsDetailsModel CDModel = new ContactsDetailsModel();
                CDModel.setContactNumber(phoneNo);
                CDModel.setContactType(type);

                contactsDetailsArray.add(CDModel);


            }

            Cursor emailCur = cr.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    new String[]{contactID}, null);
            String email = "";
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

            phone.close();


            contactsDetailsArray.addAll(contactsdetailsEmailarray);


        }
    }

}