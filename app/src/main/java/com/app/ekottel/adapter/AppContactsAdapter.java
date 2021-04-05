
package com.app.ekottel.adapter;

import android.app.Activity;
import android.content.ContentUris;
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
import androidx.cursoradapter.widget.CursorAdapter;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import com.app.ekottel.R;

import com.app.ekottel.activity.ChatAdvancedActivity;
import com.app.ekottel.activity.ContactDetailsActivity;
import com.app.ekottel.activity.HomeScreenActivity;
import com.app.ekottel.activity.PSTNCallActivity;

import com.app.ekottel.utils.Constants;
import com.app.ekottel.utils.GlobalVariables;
import com.app.ekottel.utils.PreferenceProvider;
import com.app.ekottel.utils.Utils;
import com.ca.Utils.CSDbFields;
import com.ca.wrapper.CSDataProvider;

import java.io.InputStream;
import java.lang.ref.WeakReference;

/**z z
 * This activity is used to display App Contacts.
 *
 * @author Ramesh U
 * @version 2017
 */

public class AppContactsAdapter extends CursorAdapter {

    private static Activity context;
    PopupWindow popupWindow_Obj;
    boolean isBalanceTransfer;
    private String TAG1;
    private  String receivedFilePath = "";
    private  String receivedFileType = "";

    public AppContactsAdapter(Activity context, Cursor c, int flags, boolean isBalTransfer,String receivedFileType,String receivedFilePath) {
        super(context, c, flags);
        this.context = context;
        this.isBalanceTransfer = isBalTransfer;

        this.receivedFilePath = receivedFilePath;
        this.receivedFileType = receivedFileType;
    }

    @Override
    public void bindView(View convertView, final Context context, Cursor cursor) {

        ImageView image = (ImageView) convertView.findViewById(R.id.iv_contact_details);

        ImageView iv_app_contact = (ImageView) convertView.findViewById(R.id.iv_contact_details_app_contact);
        TextView tv_contacts_name = (TextView) convertView.findViewById(R.id.tv_contacts_name);
        tv_contacts_name.setSelected(true);
        TextView tv_contacts_number = (TextView) convertView.findViewById(R.id.tv_contacts_number);

        final ImageView tv_call = (ImageView) convertView.findViewById(R.id.tv_contacts_call);
        TextView tv_chat = (TextView) convertView.findViewById(R.id.tv_contacts_chat);
        final TextView tv_info = (TextView) convertView.findViewById(R.id.tv_contacts_info);

        final LinearLayout ll_info = (LinearLayout) convertView.findViewById(R.id.ll_contacts_info);

        tv_info.setVisibility(View.GONE);
        ll_info.setVisibility(View.GONE);

        final LinearLayout ll_name = (LinearLayout) convertView.findViewById(R.id.ll_name);

        Typeface text_font = Utils.getTypeface(context);
        tv_chat.setTypeface(text_font);
        tv_info.setTypeface(text_font);
 

        tv_chat.setText(context.getString(R.string.contact_chat));
        tv_info.setText(context.getString(R.string.contact_more));

        TAG1=context.getString(R.string.messages_tag);


        final String id = cursor.getString(cursor.getColumnIndex(CSDbFields.KEY_CONTACT_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
        final String number = cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NUMBER));

        final String isAppContact = cursor.getString(cursor.getColumnIndex(CSDbFields.KEY_CONTACT_IS_APP_CONTACT));

        LOG.info(TAG1,"App Contact" + isAppContact);

        if (isAppContact != null && isAppContact.equalsIgnoreCase("1")) {
            iv_app_contact.setVisibility(View.VISIBLE);
            tv_chat.setVisibility(View.VISIBLE);
            tv_call.setPadding(0, 10, 40, 10);
            tv_chat.setPadding(0, 10, 40, 10);
        } else {
            tv_chat.setVisibility(View.GONE);
            tv_call.setPadding(0, 10, 40, 10);

            iv_app_contact.setVisibility(View.GONE);
        }

        tv_contacts_name.setText(name);
        tv_contacts_number.setText(number);
        LOG.info(TAG1,"Image ID Contacts" + id);

        String image_id = "";
        Cursor ccfr = CSDataProvider.getContactCursorByNumber(number);
        if (ccfr.getCount() > 0) {
            ccfr.moveToNext();
            image_id = ccfr.getString(ccfr.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_ID));
        }
        if (ccfr != null)
            ccfr.close();

        if (image_id != null && !image_id.isEmpty()) {
            Cursor cur = CSDataProvider.getProfileCursorByFilter(CSDbFields.KEY_PROFILE_MOBILENUMBER, number);

            if (cur.getCount() > 0) {

                cur.moveToNext();
                String picid = cur.getString(cur.getColumnIndexOrThrow(CSDbFields.KEY_PROFILE_PROFILEPICID));
                Bitmap mybitmap = CSDataProvider.getImageBitmap(picid);

                if (mybitmap != null) {

                    image.setImageBitmap(mybitmap);
                } else {
                    new ImageDownloaderTask(image).execute(image_id);
                    //new MessagesFragment.ImageDownloaderTask(holder.image, cModel).execute(id);
                }


            } else {
              //  new MessagesFragment.ImageDownloaderTask(holder.image, cModel).execute(id);
            }
            cur.close();

        }

        tv_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popupWindow_Obj = dialog_Select(context, tv_info, id, isAppContact, number);
                popupWindow_Obj.showAsDropDown(v);


            }
        });
        tv_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context.getApplicationContext(), ChatAdvancedActivity.class);


                intent.putExtra(Constants.INTENT_CHAT_CONTACT_NUMBER, number);
                intent.putExtra(Constants.INTENT_CHAT_CONTACT_NAME, name);
                intent.putExtra(Constants.IS_VIDEO_CALL_RUNNING, false);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(intent);



               // context.getApplicationContext().startActivity(intent);
            }
        });
        ll_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isBalanceTransfer) {

                    Intent intent = new Intent();
                    intent.putExtra(context.getString(R.string.bal_trans_pref_app_compat), number);
                    ((Activity) context).setResult(Activity.RESULT_OK, intent);
                    ((Activity) context).finish();
                } else {

                    PreferenceProvider pf=new PreferenceProvider(context);

                    boolean homePage=pf.getPrefBoolean("RegisterHome");

                    if (false) {
                        Intent intent = new Intent(context.getApplicationContext(), HomeScreenActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(context.getString(R.string.call_logs_intent_sender_key), number);
                        intent.putExtra(context.getString(R.string.call_logs_intent_is_group_key), false);
                        intent.putExtra("ImageShare", "ImageShare");

                        if (!receivedFileType.equals("")) {
                            PreferenceProvider preferenceProvider = new PreferenceProvider(context);
                            preferenceProvider.setPrefboolean(PreferenceProvider.IS_FILE_SHARE_AVAILABLE, true);
                            intent.putExtra("isShareFileAvailable", true);
                            intent.putExtra("receivedFileType", receivedFileType);
                            intent.putExtra("receivedFilePath", receivedFilePath);
                        }
                        context.getApplicationContext().startActivity(intent);
                        ((Activity) context).finish();
                    }else{
                        Intent intent = new Intent(context.getApplicationContext(), ChatAdvancedActivity.class);
                        intent.putExtra(Constants.INTENT_CHAT_CONTACT_NUMBER, number);
                        intent.putExtra(Constants.INTENT_CHAT_CONTACT_NAME, name);
                        intent.putExtra(Constants.IS_VIDEO_CALL_RUNNING, false);
                         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        if (!receivedFileType.equals("")) {
                            PreferenceProvider preferenceProvider = new PreferenceProvider(context);
                            preferenceProvider.setPrefboolean(PreferenceProvider.IS_FILE_SHARE_AVAILABLE, true);
                            intent.putExtra("isShareFileAvailable", true);
                            intent.putExtra("receivedFileType", receivedFileType);
                            intent.putExtra("receivedFilePath", receivedFilePath);
                        }

                        context.getApplicationContext().startActivity(intent);
                        ((Activity) context).finish();
                    }

                }


            }
        });
        tv_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                call(number, isAppContact);

            }
        });

    }

    /**
     * This is used for initiate call
     * @param number this is used for initiate call
     * @param isAppContact check whether app contact or not
     */
    private void call(String number, String isAppContact) {
        if (isAppContact != null && isAppContact.equalsIgnoreCase("1")) {

            if (!Utils.getNetwork(context)) {
                Toast.makeText(context, context.getString(R.string.network_unavail_message), Toast.LENGTH_LONG).show();
                return;
            }

            if (!HomeScreenActivity.status.equalsIgnoreCase(context.getString(R.string.registered))) {
                Toast.makeText(context, context.getString(R.string.registered_message), Toast.LENGTH_LONG).show();
                return;
            }

            PreferenceProvider pf = new PreferenceProvider(context);

            boolean isInCall = pf.getPrefBoolean(context.getString(R.string.call_logs_incall_message));
            if (isInCall) {
                Toast.makeText(context, context.getString(R.string.call_running_error_message), Toast.LENGTH_LONG).show();
                return;
            }


            PSTNCallActivity.isCallLive = true;
            GlobalVariables.destinationnumbettocall = number;
            dovoicecall(number);


        } else {
            if (!Utils.getNetwork(context)) {
                Toast.makeText(context, context.getString(R.string.network_unavail_message), Toast.LENGTH_LONG).show();
                return;
            }

            if (!HomeScreenActivity.status.equalsIgnoreCase(context.getString(R.string.registered))) {
                Toast.makeText(context, context.getString(R.string.registered_message), Toast.LENGTH_LONG).show();
                return;
            }
            PreferenceProvider pf = new PreferenceProvider(context);
            boolean isInCall = pf.getPrefBoolean(context.getString(R.string.call_logs_incall_message));
            if (isInCall) {
                Toast.makeText(context, context.getString(R.string.call_running_error_message), Toast.LENGTH_LONG).show();
                return;
            }

            String phoneNumber;
            phoneNumber = number.replaceAll("[^0-9]", "");
            GlobalVariables.destinationnumbettocall = phoneNumber;

            LOG.info(TAG1,"Call Number Contacts" + phoneNumber);


            /*if (!GlobalVariables.ispeerconnectionclosed) {
                GlobalVariables.peerConnectionClient.close();
                GlobalVariables.ispeerconnectionclosed = true;
            }
            GlobalVariables.ispeerconnectionclosed = false;
            Intent intent = new Intent(context, PlaySipCallActivity.class);
            intent.putExtra(context.getString(R.string.chat_screen_intent_dntnum_key), GlobalVariables.destinationnumbettocall);
            intent.putExtra(context.getString(R.string.chat_screen_intent_stream_subscr_key), "");
            intent.putExtra(context.getString(R.string.chat_screen_intent_initiator_key), true);
            context.startActivity(intent);*/


        }
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View convertView = inflater.inflate(R.layout.contacts_adapter, parent, false);

        return convertView;
    }

    /**
     * Method that is handle app to app call is initiate
     *
     * @param numbertodial this is mobile number to initiate call
     */
    public void dovoicecall(String numbertodial) {
        try {
            if (!numbertodial.equals("") && !numbertodial.equals(GlobalVariables.phoneNumber)) {
                /*if (!GlobalVariables.ispeerconnectionclosed) {
                    GlobalVariables.peerConnectionClient.close();
                    GlobalVariables.ispeerconnectionclosed = true;
                }
                GlobalVariables.destinationnumbettocall = numbertodial;
                GlobalVariables.ispeerconnectionclosed = false;
                Intent intent = new Intent(context.getApplicationContext(), PlayAudioCallActivity.class);
                intent.putExtra(context.getString(R.string.chat_screen_intent_dntnum_key), numbertodial);
                intent.putExtra(context.getString(R.string.chat_screen_intent_stream_subscr_key), "");
                intent.putExtra(context.getString(R.string.chat_screen_intent_initiator_key), true);
                context.startActivityForResult(intent, 954);*/
            } else {
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.call_logs_invalid_number), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

//This is used for load bitmap image
    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        boolean scaleit = false;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap photo = null;
            try {
                photo = loadContactPhoto(Long.parseLong(params[0]));
                if (photo == null) {
                    photo = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile_avatar);
                } else {
                    scaleit = true;
                }

            } catch (Exception e) {
                photo = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile_avatar);
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
                    if (bitmap != null) {


                        Bitmap image_bitmap = Utils.getRoundedCornerBitmap(bitmap, 800);


                        imageView.setImageBitmap(image_bitmap);

                    } else {

                    }
                }
            }
        }
    }

    /**
     * This is used for load bitmap image
     * @param id this is used for load image
     * @return which returns bitmap
     */
    public static Bitmap loadContactPhoto(long id) {
        try {
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
            InputStream stream = ContactsContract.Contacts.openContactPhotoInputStream(
                    context.getContentResolver(), uri, true);
            return BitmapFactory.decodeStream(stream);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }


    }


    private PopupWindow dialog_Select(final Context context, TextView lin, final String contactId, final String isAppContact, final String contactNumber) {

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
                        smsIntent.putExtra(context.getString(R.string.contact_intent_address_key), contactNumber);
                        if (smsIntent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(smsIntent);
                        } else {
                            Toast.makeText(context, context.getString(R.string.contact_no_sms_app_available), Toast.LENGTH_SHORT).show();
                        }


                        // Older versions
                    } else {
                        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                        smsIntent.setType(Constants.CONTACTS_SMS_TYPE_URL);
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
//This is used for display specific position
    public static Rect locateView(View v) {
        int[] loc_int = new int[2];
        if (v == null) return null;
        try {
            v.getLocationOnScreen(loc_int);
        } catch (Exception  npe) {
            npe.printStackTrace();
            //Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }


}