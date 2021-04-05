package com.app.ekottel.utils;

public class IAmLiveDbDataProvider {

	public static final String PROVIDER_NAME = "com.app.vxsmvoip.db";
	
	private static final String DATABASE_NAME = "ca.firstcall.db";
	private static final int DATABASE_VERSION = 1;
	public static final String KEY_ID= "_id";

	public static final String KEY_CONTACT_ID = "contact_id";
	public static final String KEY_CONTACT_NAME = "contact_name";
	public static final String KEY_CONTACT_NUMBER = "contact_number";
	public static final String KEY_IS_APP_CONTACT = "isAppContact";
	public static final String KEY_CONTACT_RAW_NUMBER = "rawnumber";

	public static final String KEY_CALLLOG_NAME = "calllog_name";
	public static final String KEY_CALLLOG_NUMBER = "calllog_number";
	public static final String KEY_CALLLOG_DIR = "calllog_dir";
	public static final String KEY_CALLLOG_TIME = "calllog_time";
	public static final String KEY_CALLLOG_DURATION = "calllog_duration";
	public static final String KEY_CALLLOG_COST = "calllog_cost";


	public static final String KEY_GROUP_ID = "group_id";
	public static final String KEY_GROUP_NAME = "group_name";
	public static final String KEY_GROUP_DESC = "group_dec";
	public static final String KEY_GROUP_ADMIN = "GroupAdmin";
	public static final String KEY_GROUP_PROFILE_PIC = "group_profile_pic";


	public static final String KEY_setings_isalreadysignedup = "setings_isalreadysignedup";
	public static final String KEY_setings_phoneNumber = "setings_phoneNumber";
	public static final String KEY_setings_region = "setings_region";
	public static final String KEY_setings_contactsread = "setings_contactsread";
	public static final String KEY_setings_fcmid = "setings_fcmid";
	public static final String KEY_setings_randomid = "setings_randomid";
	public static final String KEY_setings_otp = "setings_otp";
	public static final String KEY_setings_sipserverip = "setings_sipserverip";
	public static final String KEY_setings_sipserverport = "setings_sipserverport";
	public static final String KEY_setings_username = "setings_username";
	public static final String KEY_setings_password = "setings_password";
	public static final String KEY_setings_sippin = "setings_sippin";
	public static final String KEY_setings_balance = "setings_balance";
	public static final String KEY_setings_sipsignstatus = "setings_sipsignstatus";
	public static final String KEY_setings_appforgroundstatus = "setings_appforgroundstatus";
	public static final String KEY_setings_balanceurl = "setings_balanceurl";
	public static final String KEY_setings_brandpin = "setings_brandpin";
	public static final String KEY_setings_server = "setings_server";
	public static final String KEY_setings_serverport = "setings_serverport";
	public static final String KEY_setings_signintype = "setings_signintype";
	public static final String KEY_setings_loginstatus = "setings_loginstatus";

	public static final String KEY_cameraid = "cameraid";
	public static final String KEY_camerapreviewwidth = "camerapreviewwidth";
	public static final String KEY_camerapreviewheight = "camerapreviewheight";

	public static final String KEY_mediasettings_prefaudiocodec = "mediasettings_prefaudiocodec";
	public static final String KEY_mediasettings_prefvideocodec = "mediasettings_prefvideocodec";
	public static final String KEY_mediasettings_prefvideowidth = "mediasettings_prefvideowidth";
	public static final String KEY_mediasettings_prefvideolength = "mediasettings_prefvideolength";


	public static final String KEY_sUserName = "sUserName";
	public static final String KEY_sPresenceStatusMsg = "sPresenceStatusMsg";
	public static final String KEY_sProfilePicId = "sProfilePicId";
	public static final String KEY_sUserId = "sUserId";

	public static final String KEY_imageid = "imageid";
	public static final String KEY_imagedata = "imagedata";

	public static final String KEY_allstrxnid = "allstrxnid";
	public static final String KEY_allsmobilenumber = "allsmobilenumber";
	public static final String KEY_allsUserName = "allsUserName";
	public static final String KEY_allsPresenceStatusMsg = "allsPresenceStatusMsg";
	public static final String KEY_allsProfilePicId = "allsProfilePicId";
	public static final String KEY_allsUserId = "allsUserId";

	public static final String KEY_brand_sAdminUserName = "sAdminUserName";
	public static final String KEY_brand_sAdminPassword = "sAdminPassword";
	public static final String KEY_brand_sBrandPin = "sBrandPin";
	public static final String KEY_brand_sBrandUserName = "sBrandUserName";
	public static final String KEY_brand_sBrandPassword = "sBrandPassword";
	public static final String KEY_brand_sBrandName = "sBrandName";
	public static final String KEY_brand_sSipServerAddress = "sSipServerAddress";
	public static final String KEY_brand_sSipServerPort = "sSipServerPort";
	public static final String KEY_brand_sCurrentCallsLimit = "sCurrentCallsLimit";
	public static final String KEY_brand_sBrandPinActive = "sBrandPinActive";
	public static final String KEY_brand_sAndroidActive = "sAndroidActive";
	public static final String KEY_brand_sIosActive = "sIosActive";
	public static final String KEY_brand_balanceurl = "brandbalanceurl";

	public static final String KEY_packages_id = "packages_id";
	public static final String KEY_packages_packagename = "packages_packagename";
	public static final String KEY_packages_cost = "packages_cost";
	public static final String KEY_packages_validity = "packages_validity";
	public static final String KEY_packages_maxminutes = "packages_maxminutes";
	public static final String KEY_packages_creationdate = "packages_creationdate";


	public static final String KEY_mypackages_id = "mypackages_id";
	public static final String KEY_mypackages_packagename = "mypackages_packagename";
	public static final String KEY_mypackages_cost = "mypackages_cost";
	public static final String KEY_mypackages_validity = "mypackages_validity";
	public static final String KEY_mypackages_maxminutes = "mypackages_maxminutes";
	public static final String KEY_mypackages_creationdate = "mypackages_creationdate";

	public static final String KEY_CHAT_ID = "chat_id";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_FILEPATH = "upload_file_path";
	public static final String KEY_TIME = "time";
	public static final String KEY_CHAT_STATUS = "status";
	public static final String KEY_IS_SENDER = "isSender";
	public static final String KEY_DESTINATION_NUMBER = "destination_number";
	public static final String KEY_MSG_TYPE = "message_type";
	public static final String KEY_DESTINATION_NAME = "destination_name";
	public static final String KEY_FILE_KEY= "filekey";
	//public static final String KEY_FILE_PATH = "filepath"; // column doesn't exists
	public static final String KEY_IS_GROUP_MESSAGE = "isGroupMessage";
	public static final String KEY_DESTINATION_GROUPID = "destination_groupid";

	public static final String KEY_PMNTHX_TRNXID= "pmnthx_trnxid";
	public static final String KEY_PMNTHX_TYPE = "pmnthx_type";
	public static final String KEY_PMNTHX_TIME = "pmnthx_time";
	public static final String KEY_PMNTHX_MODE = "pmnthx_mode";
	public static final String KEY_PMNTHX_USER = "pmnthx_user";
	public static final String KEY_PMNTHX_AMOUNT = "pmnthx_amount";
	public static final String KEY_PMNTHX_REMARKS = "pmnthx_remarks";

	public static final String KEY_PROMOTIONAL_MESSAGEID = "promotionalmessageid";
	public static final String KEY_PROMOTIONAL_MESSAGEAPI = "promotionalmessageapi";
	public static final String KEY_PROMOTIONAL_MESSAGETYPE = "promotionalmessagetype";
	public static final String KEY_PROMOTIONAL_MESSAGEMESSAGE = "promotionalmessagemessage";
	public static final String KEY_PROMOTIONAL_MESSAGEUSERNOTIFIED = "promotionalmessageusernotified";
	public static final String KEY_PROMOTIONAL_MESSAGEREAD = "promotionalmessageread";


	public static final String KEY_payment_gateway = "payment_gateway";
	public static final String KEY_payment_type = "payment_type";
	public static final String KEY_payment_tranxid = "payment_tranxid";
	public static final String KEY_payment_verified = "payment_verified";


}
