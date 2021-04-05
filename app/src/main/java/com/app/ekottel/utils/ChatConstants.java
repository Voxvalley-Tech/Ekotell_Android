package com.app.ekottel.utils;

import android.os.Environment;

public class ChatConstants {

    public static  String CHAT_ACTIVITY_DESTINATION_NUMBER;
    public static boolean IS_AUDIO_CALL_RUNNING = false;
    public static boolean IS_CHAT_SCREEN_ACTIVE = false;
    public static final String STOP_MEDIA_PLAYER = "stopMediaPlayer";
    public static final String INTENT_CHAT_ID = "chatid";


    // Below is the Folder structure for Chat
    public static final String extStorageDirectory = Environment
            .getExternalStorageDirectory().toString();
    public static final String CHAT_IMAGES_DIRECTORY = extStorageDirectory
            + "/"+Constants.APP_NAME+"/Images/Received";
    public static final String CHAT_VIDEOS_DIRECTORY = extStorageDirectory
            + "/"+Constants.APP_NAME+"/Videos/Received";
    public static final String CHAT_AUDIO_DIRECTORY = extStorageDirectory
            + "/"+Constants.APP_NAME+"/Audios/Received";
    public static final String CHAT_AUDIO_DIRECTORY_SENT = extStorageDirectory
            + "/"+Constants.APP_NAME+"/Audios/Sent";
    public static final String CHAT_DOCUMENTS_DIRECTORY = extStorageDirectory
            + "/"+Constants.APP_NAME+"/Documents/Received";


    public static final String INTENT_DESTINATION_NUMBER = "destinationnumber";
    public static final String INTENT_LOCATION_LATITUDE = "latitude";
    public static final String INTENT_LOCATION_LONGITUDE = "longitude";
    public static final String INTENT_LOCATION_ADDRESS = "address";
    // Chat Constants
    public static final String INTENT_CHAT_CONTACT_NUMBER = "contactNumber";
    public static final String INTENT_CHAT_CONTACT_NAME = "contactName";

    public static final String TIME_FORMAT = "hh:mm a";
    public static final String TIME_24_FORMAT="HH:mm";




    public static String chatappname = "";
    //image
    public static String imagedirectory = chatappname+"/Images";
    public static String imagedirectorysent = chatappname+"/Images/Sent";//used for location and doc
    public static String imagedirectoryreceived = chatappname+"/Images/Received"; //used for location and thumbainal internally

    //video
    public static String videodirectory = chatappname+"/Videos";
    public static String videodirectorysent = chatappname+"/Videos/Sent";//used for location and doc
    public static String videodirectoryreceived = chatappname+"/Videos/Received"; //used for location and thumbainal internally

    //audio
    public static String audiodirectory = chatappname+"/Audios";
    public static String audiodirectorysent = chatappname+"/Audios/Sent";//used for location and doc
    public static String audiodirectoryreceived = chatappname+"/Audios/Received"; //used for location and thumbainal internally

    //Documents
    public static String docsdirectory = chatappname+"/Documents";
    public static String docsdirectorysent = chatappname+"/Documents/Sent";//used for location and doc
    public static String docsdirectoryreceived = chatappname+"/Documents/Received"; //used for location and thumbainal internally

    //profiles
    public static String profilesdirectory = chatappname+"/Profile Photos";
    //public static String profilesdirectorysent = chatappname+"/Profile Photos/Sent";//used for location and doc
    //public static String profilesdirectoryreceived = chatappname+"/Profile Photos/Received"; //used for location and thumbainal internally

    public static String thumbnailsdirectory = chatappname+"/Thumbnails";
    public static String recordingsdirectory = chatappname+"/Recordings";

}
