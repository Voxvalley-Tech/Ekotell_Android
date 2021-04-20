package com.app.ekottel.utils;

public class Constants {
    public static final Float USD_XCD_DIFF = 2.67F ;
    public static boolean IS_IMPLICIT_ACTIVITY_OPEN = false;
    public static String APP_NAME = "Ekottel";

    //urls for APIs
    public static String BALANCE_URL = "";
    public static String VOUCHER_RECHARGE_URL = "";
    public static String BALANCE_TRANSFER_URL = "";
    public static String PACKAGES_URL = "";
    public static String MY_PACKAGES_URL = "";
    public static String TRANSACTION_HISTORY_URL = "";


    public static String SUBSCRIBE_PACKAGE_URL = "";
    public static String CALL_RATES_URL = "";
    public static String PAYPAL_GETCLIENTID_URL = "";
    public static String PAYPAL_VERIFYPAYMENT_URL = "";
    public static String STRIPE_VERIFICATION_URL = "";
    public static String STRIPE_PUBLISHABLE_KEY_URL = "";
    public static String STRIPE_CREATE_STRIPE_PAYMENT_INTENT = "";

    //Support URL'S
    public static String FAQ_URL = "http://162.255.116.199/tringy/faq.php";
  //  public static String TERM_AND_CONDITION_URL = "http://voxvalley.com/privacy-policy.html";
    public static String TERM_AND_CONDITION_URL = "http://162.255.116.199/tringy/privacy-policy.php";

    //	banatelecom directory
    public static String TRINGY_DIRECTORY = "/ekottel/";

    //    Paypal URLs
    public static String PAYPAL_PRIVACY_POLICY_URL = "https://www.example.com/privacy";
    public static String PAYPAL_LEGAL_URL = "https://www.example.com/legal";
    public static String PAYPAL_MERCHANT_NAME = "ekottel";
    public static final String INTENT_CHAT_CONTACT_NUMBER = "contactNumber";
    public static final String INTENT_CHAT_CONTACT_NAME = "contactName";
    public static final String IS_VIDEO_CALL_RUNNING = "videoCallRunning";
    public static final String CLOSE_RETURN_TO_VIDEO_CALL_TEXT = "clsoeTextView";
    public static final String ACTION_CLEAR_ALL_CHAT = "clear_all_chat";

    //    Contacts sms related urls
    public static String CONTACTS_SMS_TYPE_URL = "vnd.android-dir/mms-sms";
    public static String CONTACTS_SMS_BODY_KEY_URL = "sms_body";
    public static String CONTACTS_SMS_BODY_VALUE_URL = "https://play.google.com/store/apps/details?id=com.app.ekottel&hl=en";

    public static final String TIME_FORMAT_24HR_FOR_LAST_SEEN = "HH:mm:ss";
    public static final String TIME_FORMAT_NORMAL_FOR_LAST_SEEN = "hh:mm:ss a";
    public static final String TIME_FORMAT_24HR_FOR_CALL_LOGS = "HH:mm";
    public static final String TIME_FORMAT_NORMAL_FOR_CALL_LOGS = "hh:mm a";
    public static final String TIME_FORMAT_24HR_FOR_CHAT_SCREEN = "yyyyMMdd_HHmmss";
    public static final String TIME_FORMAT_NORMAL_FOR_CHAT_SCREEN = "yyyyMMdd_hhmmss a";
    public static final String TIME_FORMAT_FOR_LAST_SEEN = "dd-MM-yyyy hh:mm:ss a";
    public static final String TIME_FORMAT_FOR_24_LAST_SEEN = "dd-MM-yyyy HH:mm:ss";
    public static int videoCallTimeValue;
}
