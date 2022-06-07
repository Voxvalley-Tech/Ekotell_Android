package com.app.ekottel.utils;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import static com.app.ekottel.utils.GlobalVariables.LOG;

import com.app.ekottel.activity.CallScreenActivity;
import com.app.ekottel.receivers.RingtonePlayingService;
import com.ca.Utils.CSConstants;
import com.ca.Utils.CSDbFields;
import com.ca.dao.CSContact;
import com.ca.wrapper.CSCall;
import com.ca.wrapper.CSClient;
import com.ca.wrapper.CSDataProvider;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;

public class Utils {

    private static String ROOT = "fonts/", FONTAWESOME = ROOT + "fontawesome-webfont.ttf";
    public static Typeface webTypeFace;
    public static Typeface webTypeFace_Bold;
    public static Typeface webTypeFace_Medium;
    public static Typeface webTypeFace_Regular;
    public static Typeface webTypeFace_Light;
    private static String callIdtoPass = "";
    private static String TAG = "utils";
    private static String FONTAWESOMEMEDIUM = ROOT + "fontawesome-webfont.ttf";
    private static String FONTAWESOMELIGHT = ROOT + "fontawesome-webfont.ttf";
    private static String FONTAWESOMEREGULAR = ROOT + "fontawesome-webfont.ttf";

    public static String getInternationFormatNumber1(String phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto;

            if (phoneNumber.startsWith("+")) {
                numberProto = phoneUtil.parse(phoneNumber, GlobalVariables.defaultRegion);
            } else {
                numberProto = phoneUtil.parse("+" + phoneNumber, GlobalVariables.defaultRegion);
            }

            boolean isValid = phoneUtil.isValidNumber(numberProto);

            if (!isValid) {
                phoneNumber = phoneNumber.replace("+", "");
                numberProto = phoneUtil.parse(GlobalVariables.defaultCountryCode + phoneNumber, GlobalVariables.defaultRegion);
                isValid = phoneUtil.isValidNumber(numberProto);
            }

            if (isValid) {
                return phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL).replace(" ", "").trim();
            }

        } catch (Exception e) {
//            e.printStackTrace();
        }

        return null;
    }


    public static String getInternationFormatNumber(String phNumber, String countryCode) {
        try {
            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode));
            Phonenumber.PhoneNumber phoneNumber = null;
            //phoneNumber = phoneNumberUtil.parse(phNumber, "IN");  //if you want to pass region code
            phoneNumber = phoneNumberUtil.parse(phNumber, isoCode);

            boolean isValid = phoneNumberUtil.isValidNumber(phoneNumber);
            if (isValid) {
                String internationalFormat = phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
                return internationalFormat;
            } else {
                return phNumber;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phNumber;
    }

    public static String getInternationFormatNumber_signout(String phoneNumber) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto;

            if (phoneNumber.startsWith("+")) {
                numberProto = phoneUtil.parse(phoneNumber, GlobalVariables.defaultRegion);
            } else {
                numberProto = phoneUtil.parse("+" + phoneNumber, GlobalVariables.defaultRegion);
            }

            boolean isValid = phoneUtil.isValidNumber(numberProto);

            if (!isValid) {
                phoneNumber = phoneNumber.replace("+", "");
                numberProto = phoneUtil.parse(GlobalVariables.defaultCountryCode + phoneNumber, GlobalVariables.defaultRegion);
                isValid = phoneUtil.isValidNumber(numberProto);
            }

            if (isValid) {
                return phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL).replace(" ", "").trim();
            }

        } catch (Exception e) {
//            e.printStackTrace();
        }

        return null;
    }

    public static Typeface getTypefaceRegular(Context context) {
        if (webTypeFace_Regular == null) {
            webTypeFace_Regular = Typeface.createFromAsset(context.getAssets(), FONTAWESOMEREGULAR);
        }
        return webTypeFace_Regular;
    }
    public static Typeface getTypefaceBold(Context context) {
        if (webTypeFace_Bold == null) {
            webTypeFace_Bold = Typeface.createFromAsset(context.getAssets(), FONTAWESOMEMEDIUM);
        }
        return webTypeFace_Bold;
    }

    /**
     * This is used for retrieve font awesome style
     *
     * @param context current object
     * @return which returns type of style
     */

    public static Typeface getTypefaceMedium(Context context) {
        if (webTypeFace_Medium == null) {
            webTypeFace_Medium = Typeface.createFromAsset(context.getAssets(), FONTAWESOMEMEDIUM);
        }
        return webTypeFace_Medium;
    }
    public static Typeface getTypefaceLight(Context context) {
        if (webTypeFace_Light == null) {
            webTypeFace_Light = Typeface.createFromAsset(context.getAssets(), FONTAWESOMELIGHT);
        }
        return webTypeFace_Light;
    }

    public static String validateNumberForSingup(String phoneNumber, String code) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            String defaultRegion = phoneUtil.getRegionCodeForCountryCode(Integer.parseInt(code.substring(1, code.length())));

            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNumber, defaultRegion);


            boolean isValid = phoneUtil.isValidNumber(numberProto);


            if (isValid)
                return phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL).replace(" ", "").trim();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        try {
            return BitmapFactory.decodeByteArray(image, 0, image.length);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
   /* public static void addContactsToSdk(Context context) {
        new Thread(() -> {
            PreferenceProvider preferenceProvider = new PreferenceProvider(context);
            Cursor contactsCursor = CSDataProvider.getContactsCursor();
            ArrayList<CSContact> nativeContacts = new ArrayList<CSContact>();


            ContentResolver cr = context.getContentResolver();
            CSClient csClient = new CSClient();
            Cursor nativeContactsCursor = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                    null,
                    null,
                    null
            );

            try {
                if (nativeContactsCursor != null) {
                    //LOG.info("test: pcur count:" + pCur.getCount());
                    if (nativeContactsCursor.getCount() > 0) {
                        while (nativeContactsCursor.moveToNext()) {
                            String id = nativeContactsCursor.getString(nativeContactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                            String phone = nativeContactsCursor.getString(nativeContactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String name = nativeContactsCursor.getString(nativeContactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));


                            //LOG.info("test: id:" + id);
                            //LOG.info("test: number:" + phone);
                            //LOG.info("test: name:" + name);
                            if (phone != null) {
                                if (!phone.equals("")) {
                                    phone = getInternationFormatNumber1(phone);

                                    if (phone != null) {
                                        phone = phone.replaceAll("[^0-9]", "");

                                        if (name == null || name.isEmpty()) {
                                            name = phone;
                                        }

                                        name = name.replace("+", "");

                                        if (!isAlreadyAdded(phone, contactsCursor)) {
                                            nativeContacts.add(new CSContact(name, phone, CSConstants.CONTACTTYPE_OTHER, id));
                                        }
                                    }
                                }
                            }
                        }

                        csClient.addContacts(nativeContacts);

                    }
                }

                //deleting the contacts from the sdk if  contact deleted from the native contacts .
                deleteContacts(context);

            } catch (Exception e) {
                e.printStackTrace();
            }

            contactsCursor.close();
            nativeContactsCursor.close();

        }).start();
    }*/
    private static boolean isAlreadyAdded(String phone, Cursor cursor) {

        if (cursor.moveToFirst()) {
            do {
                if (phone.equals(cursor.getString(cursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NUMBER)))) {

                    return true;
                }
            } while (cursor.moveToNext());
        }
        return false;
    }

    private static void deleteContacts(Context context) {
        Cursor csContactsCursor = CSDataProvider.getContactsCursor();
        CSClient csClient = new CSClient();
        ContentResolver cr = context.getContentResolver();
        if (csContactsCursor != null) {
            while (csContactsCursor.moveToNext()) {
                String id = csContactsCursor.getString(csContactsCursor.getColumnIndex(CSDbFields.KEY_CONTACT_ID));
                Cursor pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id}, null);

                if (pCur == null || pCur.getCount() == 0) {
                    csClient.deleteContact(id);
                }

                pCur.close();

            }

            csContactsCursor.close();
        }

    }
    /* Checking the Internet connection */
    public static boolean getNetwork(Context context) {

        try {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean is3g = manager.getNetworkInfo(
                    ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
            boolean isWifi = manager.getNetworkInfo(
                    ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

            if (!is3g && !isWifi) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * This is used handle font awesome icons
     *
     * @param context current object
     * @return which returns font awesome type
     */
    public static Typeface getTypeface(Context context) {
        if (webTypeFace == null) {
            webTypeFace = Typeface.createFromAsset(context.getAssets(), FONTAWESOME);
        }
        return webTypeFace;
    }
    /**
     * This is used for change orientation of image
     *
     * @param bitmap              change orientation
     * @param image_absolute_path path
     * @return which returns bitmap image
     * @throws IOException exception
     */
    public static Bitmap modifyOrientation(Bitmap bitmap, String image_absolute_path) throws
            IOException {
        ExifInterface ei = new ExifInterface(image_absolute_path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    /**
     * This is used for rotate image whatever you want
     *
     * @param bitmap  this is used for rotate image
     * @param degrees this is used for rotate image
     * @return which returns bitmap iamge
     */
    static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * This is used for flip image
     *
     * @param bitmap     flip image
     * @param horizontal this is used for flip image
     * @param vertical   this is used for flip image
     * @return which returns bitmap
     */
    static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * This is used for change image orientation
     *
     * @param imagePath image path
     * @return which returns bitmap image
     */

    public static Bitmap rotateImageIfRequired(String imagePath) {
        int degrees = 0;

        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degrees = 90;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    degrees = 180;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    degrees = 270;
                    break;
            }
        } catch (IOException e) {
            LOG.error("ImageError", "Error in reading Exif data of " + imagePath, e);
        }

        BitmapFactory.Options decodeBounds = new BitmapFactory.Options();
        decodeBounds.inJustDecodeBounds = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, decodeBounds);
        int numPixels = decodeBounds.outWidth * decodeBounds.outHeight;
        int maxPixels = 2048 * 1536; // requires 12 MB heap

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = (numPixels > maxPixels) ? 2 : 1;

        bitmap = BitmapFactory.decodeFile(imagePath, options);

        if (bitmap == null) {
            return null;
        }

        Matrix matrix = new Matrix();
        matrix.setRotate(degrees);

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);

        return bitmap;
    }

    /**
     * Method is used to dsiplay round image
     *
     * @param bitmap this is bitmap to handle rounded corners
     * @param pixels this is pixels handle rounded corners
     * @return which returns bitmap image
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * This is used for returns name of contact
     *
     * @param remoteParty This is used for retrieve name
     * @param mContext    current object
     * @return retrieves name
     */
    public static String getCallLogContactName(final String remoteParty, Context mContext) {
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(remoteParty));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return remoteParty;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    /**
     * This is used for retrieve type of network
     *
     * @param context this is used for current object
     * @return this is used for retrieve type of network value
     */

    public static boolean getNetworkType(Context context) {
        /* Checking the Internet connection */
        try {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            boolean is3g = manager.getNetworkInfo(
                    ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();

            boolean isWifi = manager.getNetworkInfo(
                    ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

            if (isWifi) {
                return true;
            } else if (is3g) {

                String network_type = Utils.getNetworkClass(context);
                if (network_type.equalsIgnoreCase("2G")) {
                    return false;
                } else {
                    return true;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return true;
    }

    /**
     * This is used for restrict 2g network using video calling
     *
     * @param context This is referes current object
     * @return Which returns network type
     */

    public static String getNetworkClass(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Unknown";
        }
    }

    /**
     * This method loads the contact image using contact id
     *
     * @param context application context
     * @param id      contact id
     * @return image bitmap
     */
    public static Bitmap loadContactPhoto(Context context, long id) {
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

    public static void startCall(Context context, Intent intent, boolean secondcall) {
        try {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    try {

                        MainActivity.context = context.getApplicationContext();
                        //if(!CSDataProvider.getUINotificationsMuteStatus()) {
                        MainActivity.context = context.getApplicationContext();
                        String sDstMobNu = intent.getStringExtra("sDstMobNu");
                        String callid = intent.getStringExtra("callid");
                        int callactive = intent.getIntExtra("callactive", 0);
                        //String sdp = intent.getStringExtra("sdp");
                        String callType = intent.getStringExtra("callType");
                        String srcnumber = intent.getStringExtra("srcnumber");
                        CSClient CSClientObj = new CSClient();
                        long callstarttime = intent.getLongExtra("callstarttime", CSClientObj.getTime());
                        LOG.info("isCall active " + callactive + " caller ID is " + callid + " callIdtopass " + callIdtoPass);
                        if (callid.equals(callIdtoPass)) {
                            return;
                        }

                        String Name = "";
                        Cursor contactcursor = CSDataProvider.getContactCursorByNumber(srcnumber);
                        if (contactcursor.getCount() > 0) {
                            contactcursor.moveToNext();
                            Name = contactcursor.getString(contactcursor.getColumnIndexOrThrow(CSDbFields.KEY_CONTACT_NAME));
                        }
                        contactcursor.close();

                        if (Name.equals("")) {
                            Name = srcnumber;
                        }
                        if (secondcall) {

                            callIdtoPass = callid;


                            String calldirection1 = "";
                            LOG.info("Utils", "run: Start call retunring " + calldirection1);
                            CSCall CSCallsObj = new CSCall();
                            if (callType.equals("video")) {
                                //LOG.info("Test here call id:"+callid);
                                calldirection1 = "MISSED VIDEO CALL";
                                CSCallsObj.endVoiceCall(srcnumber, callid, CSConstants.E_UserBusy, CSConstants.UserBusy);
                                //CSPaymentsObj.directAudioVideoCallEndReq(srcnumber,2, callid,CSClientObj.getTime());
                            } else if(callType.equals("audio")){
                                calldirection1 = "MISSED AUDIO CALL";
                                CSCallsObj.endVoiceCall(srcnumber, callid, CSConstants.E_UserBusy, CSConstants.UserBusy);
                                //CSPaymentsObj.directAudioVideoCallEndReq(srcnumber, 1, callid,CSClientObj.getTime());
                            }else {
                                calldirection1 = "MISSED PSTN CALL";
                                CSCallsObj.endPstnCall(srcnumber, callid);
                            }

                            NotificationMethodHelper.NotifyAppInMissedCall(context, srcnumber, Name, callid, calldirection1, "", 0);
                            return;
                        }
                        long presenttime = CSClientObj.getTime();
                        long timedifference = (presenttime - callstarttime) / 1000;

                        if (timedifference < 50) {
                            callIdtoPass = callid;
                            GlobalVariables.answeredcallcount = 0;
                            CallScreenActivity.isClosedIncomingCall = false;


                            Intent intent1 = new Intent(context.getApplicationContext(), CallScreenActivity.class);


                            if (App.getActivityStackCount() > 0) {
                                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            } else {
                                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            }

                            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent1.putExtra("secondcall", secondcall);
                            intent1.putExtra("isinitiatior", false);
                            intent1.putExtra("sDstMobNu", sDstMobNu);
                            intent1.putExtra("callactive", callactive);
                            intent1.putExtra("callType", callType);
                            intent1.putExtra("srcnumber", srcnumber);
                            intent1.putExtra("callid", callid);
                            intent1.putExtra("callstarttime", callstarttime);
                            intent1.putExtra("name", Name);


                            Bundle bundle = new Bundle();
                            bundle.putString("secondcall", String.valueOf(secondcall));
                            bundle.putBoolean("isinitiatior", false);
                            bundle.putString("sDstMobNu", sDstMobNu);
                            bundle.putString("callactive", String.valueOf(callactive));
                            bundle.putString("callType", callType);
                            bundle.putString("srcnumber", srcnumber);
                            bundle.putString("callid", callid);
                            bundle.putString("callstarttime", String.valueOf(callstarttime));
                            bundle.putString("name", Name);
                            intent1.putExtras(bundle);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && App.getActivityStackCount() == 0) {
                                Log.e(TAG, "android below Q version ");
                                Intent i = new Intent(context, RingtonePlayingService.class);
                                context.startService(i);
                                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                NotificationMethodHelper notificationMethodHelper = new NotificationMethodHelper();
                                notificationMethodHelper.getCallSensitiveNotofication(srcnumber, callid, callType, intent1, context);
                              /*  NotificationMethodHelper notificationMethodHelper=new NotificationMethodHelper();
                                notificationMethodHelper.getVideoCallSensitiveNotofication(callType,intent1,context);*/
                            } else {
                                Log.e(TAG, "android below 8 version ");
                                // context.registerReceiver(NotificationReceiver, new IntentFilter("NotificationHandleReceiver"));
                                context.startActivity(intent1);
                            }

                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This methods change the audio focus
     *
     * @param setFocus boolean variable
     */
    public static void setAudioFocus(AudioManager audioManager, PreferenceProvider prefProvider,
                                     boolean setFocus) {

        if (audioManager != null) {
            // This variable saves the last audio mode
            //  int savedAudioMode = audioManager.getMode();
            if (setFocus) {
                prefProvider.setPrefint(PreferenceProvider.SAVED_AUDIO_MODE, audioManager.getMode());
                // Request audio focus before making any device switch.
                audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

                /*
                 * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
                 * required to be in this mode when playout and/or recording starts for
                 * best possible VoIP performance. Some devices have difficulties with speaker mode
                 * if this is not set.
                 */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                } else {
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                }
            } else {
                LOG.info("Audiomanger current mode " + audioManager.getMode() + " saved mode is " + prefProvider.getPrefInt(PreferenceProvider.SAVED_AUDIO_MODE));
                audioManager.setMode(prefProvider.getPrefInt(PreferenceProvider.SAVED_AUDIO_MODE));
                if (audioManager.getMode() == AudioManager.MODE_IN_CALL || audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION) {
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                }
                audioManager.abandonAudioFocus(null);
                LOG.info("Audiomanger current mode " + audioManager.getMode() + " saved mode is " + prefProvider.getPrefInt(PreferenceProvider.SAVED_AUDIO_MODE));
            }
        }
    }

    public static String generateSHA256(String message) throws Exception {
        return hashString(message, "SHA-256");
    }


    private static String hashString(String message, String algorithm)
            throws Exception {

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashedBytes = digest.digest(message.getBytes("UTF-8"));

            return convertByteArrayToHexString(hashedBytes);
        } catch (Exception ex) {
            throw new Exception("Could not generate hash from String", ex);
        }
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString(
                    (arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuffer.toString();
    }

    /**
     * This method will return boolean value wheatehr front camera available or not in device
     *
     * @return
     */
    public static boolean isFrontCameraAvailable() {
        try {
            int numCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numCameras; i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (CAMERA_FACING_FRONT == info.facing) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    /**
     * This method will return the time format for call logs based on device time
     *
     * @param context
     * @return
     */
    public static String getTimeFormatForCallLogs(Context context) {
        try {
            boolean is24fromat = android.text.format.DateFormat.is24HourFormat(context);
            if (is24fromat) {
                return Constants.TIME_FORMAT_24HR_FOR_CALL_LOGS;
            } else {
                return Constants.TIME_FORMAT_NORMAL_FOR_CALL_LOGS;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Constants.TIME_FORMAT_NORMAL_FOR_CALL_LOGS;
        }

    }

    /**
     * This method will return the time format for Chat screen based on device time
     *
     * @param context
     * @return
     */
    public static String getTimeFormatForChatScreen(Context context) {
        try {
            boolean is24fromat = android.text.format.DateFormat.is24HourFormat(context);
            if (is24fromat) {
                return Constants.TIME_FORMAT_24HR_FOR_CHAT_SCREEN;
            } else {
                return Constants.TIME_FORMAT_NORMAL_FOR_CHAT_SCREEN;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Constants.TIME_FORMAT_NORMAL_FOR_CHAT_SCREEN;
        }

    }

    /**
     * This method will give the Bitmap image of given contact number
     *
     * @param context
     * @param phoneNumber
     * @return
     */
    public static Bitmap getContactImage(Context context, String phoneNumber) {

        Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Uri photoUri = null;
        ContentResolver cr = context.getContentResolver();
        try {
            Cursor contact = cr.query(phoneUri,
                    new String[]{ContactsContract.Contacts._ID}, null, null,
                    null);
            if (contact != null)
                if (contact.moveToFirst()) {
                    long userId = contact.getLong(contact
                            .getColumnIndex(ContactsContract.Contacts._ID));
                    photoUri = ContentUris.withAppendedId(
                            ContactsContract.Contacts.CONTENT_URI, userId);

                } else {
                    // Bitmap defaultPhoto =
                    // BitmapFactory.decodeResource(getResources(),
                    // R.drawable.incall_2_normal);
                    return null;
                }
            if (photoUri != null) {
                // This one is getting crashed in 2.3 version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    InputStream input = ContactsContract.Contacts
                            .openContactPhotoInputStream(cr, photoUri, true);
                    if (input != null) {
                        return BitmapFactory.decodeStream(input);
                    }
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method will return the date from the given time stamp
     *
     * @param dateStr
     * @return
     */
    public static String getTiemStamp(long dateStr) {
        try {
            return new SimpleDateFormat("hh:mm:ss a").format(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isYesterday(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);
        now.add(Calendar.DATE, -1);
        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }


    public static String getUsdFromXcd(String xcdAmount) {
        return String.format("%.2f", Float.parseFloat(xcdAmount) / Constants.USD_XCD_DIFF);
    }
    /**
     * This method will return sent image directory
     *
     * @return
     */
    public static String getSentImagesDirectory(Context context) {

        try {
            String imagedirectorysent = "Ekottel" + "/Images/Sent";
            return context.getExternalFilesDir(null).getAbsolutePath() + "/" + imagedirectorysent;

        } catch (Exception ex) {
            return "";
        }
    }

    public static void showAlertToUser(String alertMessage, Context context) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(alertMessage);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

   /* public static void addContactsToSdk(Context context) {
        new Thread(() -> {
            PreferenceProvider preferenceProvider = new PreferenceProvider(context);
            Cursor contactsCursor = CSDataProvider.getContactsCursor();
            ArrayList<CSContact> nativeContacts = new ArrayList<CSContact>();


            ContentResolver cr = context.getContentResolver();
            CSClient csClient = new CSClient();
            Cursor nativeContactsCursor = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                    null,
                    null,
                    null
            );

            try {
                if (nativeContactsCursor != null) {
                    //LOG.info("test: pcur count:" + pCur.getCount());
                    if (nativeContactsCursor.getCount() > 0) {
                        while (nativeContactsCursor.moveToNext()) {
                            String id = nativeContactsCursor.getString(nativeContactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                            String phone = nativeContactsCursor.getString(nativeContactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String name = nativeContactsCursor.getString(nativeContactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));


                            //LOG.info("test: id:" + id);
                            //LOG.info("test: number:" + phone);
                            //LOG.info("test: name:" + name);
                            if (phone != null) {
                                if (!phone.equals("")) {
                                    phone = getInternationFormatNumber_signout(phone);

                                    if (phone != null) {
                                        phone = phone.replaceAll("[^0-9]", "");

                                        if (name == null || name.isEmpty()) {
                                            name = phone;
                                        }

                                        name = name.replace("+", "");

                                        if (!isAlreadyAdded(phone, contactsCursor)) {
                                            nativeContacts.add(new CSContact(name, phone, CSConstants.CONTACTTYPE_OTHER, id));
                                        }
                                    }
                                }
                            }
                        }

                        csClient.addContacts(nativeContacts);

                    }
                }

                //deleting the contacts from the sdk if  contact deleted from the native contacts .
                deleteContacts(context);

            } catch (Exception e) {
                e.printStackTrace();
            }

            contactsCursor.close();
            nativeContactsCursor.close();

        }).start();
    }*/
    public static void logStacktrace(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
            LOG.warn(sStackTrace);
        } catch (Exception ex) {
        }
    }
}
