package com.app.ekottel.model;

/**
 * Created by ramesh.u on 3/23/2018.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ramesh.u on 5/23/2017.
 */
public class ChatConversationDB extends SQLiteOpenHelper {

    private final static String DB_NAME = "database_chat_conversation";
    private final static int DB_VERSION = 1;

    private final static String TABLE_NAME = "database_table_chat_conversation";
    private final static String TABLE_ROW_TIMESTAMP = "timeStamp";
    private final static String TABLE_ROW_IS_SENDER = "isSender";
    private final static String TABLE_ROW_MESSAGE = "message";
    private final static String TABLE_ROW_FILE_PATH = "filePath";
    private final static String TABLE_ROW_MESSAGE_TYPE = "messageType";
    private final static String TABLE_ROW_CHATID = "chatID";
    private final static String TABLE_ROW_CHAT_STATUS = "chatStatus";
    private final static String TABLE_ROW_DESTINATION_NUMBER = "destinationNumber";
    long value;

    public ChatConversationDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String newTableQueryString = "CREATE TABLE " + TABLE_NAME + " ("
                + TABLE_ROW_TIMESTAMP + " text," + TABLE_ROW_IS_SENDER + " text,"
                + TABLE_ROW_MESSAGE + " text," + TABLE_ROW_FILE_PATH + " text," + TABLE_ROW_MESSAGE_TYPE + " text," + TABLE_ROW_CHATID + " text," + TABLE_ROW_CHAT_STATUS + " text," +TABLE_ROW_DESTINATION_NUMBER + " text" + ");";
        db.execSQL(newTableQueryString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public void addContact(List<ChatConversationModel> chatConversationList) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < chatConversationList.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(TABLE_ROW_TIMESTAMP, chatConversationList.get(i).getTimeStamp());
            values.put(TABLE_ROW_IS_SENDER, chatConversationList.get(i)
                    .getIsSender());
            values.put(TABLE_ROW_MESSAGE, chatConversationList.get(i)
                    .getMessage());

            values.put(TABLE_ROW_FILE_PATH, chatConversationList.get(i)
                    .getFilePath());
            values.put(TABLE_ROW_MESSAGE_TYPE, chatConversationList.get(i)
                    .getMessageType());
            values.put(TABLE_ROW_CHATID, chatConversationList.get(i)
                    .getChatID());
            values.put(TABLE_ROW_CHAT_STATUS, chatConversationList.get(i)
                    .getChatStatus());
            values.put(TABLE_ROW_DESTINATION_NUMBER, chatConversationList.get(i)
                    .getDestinationNumber());
            value = db.insert(TABLE_NAME, null, values);
        }

        // Inserting Row

        db.close(); // Closing database connection
    }

    public Cursor getAllRecords() {
        List<RechargeHistoryPojo> rechargeHistoryPojoList = new ArrayList<RechargeHistoryPojo>();
        // Select All Query
        String selectQuery = "SELECT rowid _id,* FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor != null) {
            cursor.moveToFirst();
        }


        // return contact list
        return cursor;
    }


    public Cursor getLatRecordRecords() {
        List<RechargeHistoryPojo> rechargeHistoryPojoList = new ArrayList<RechargeHistoryPojo>();
        // Select All Query
        String selectQuery = "SELECT rowid _id,* FROM " + TABLE_NAME +" "+"ORDER BY column DESC LIMIT 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor != null) {
            cursor.moveToFirst();
        }


        // return contact list
        return cursor;
    }





    public void deleteTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NAME);
    }


}
