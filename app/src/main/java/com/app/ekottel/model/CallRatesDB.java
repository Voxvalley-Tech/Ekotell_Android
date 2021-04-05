package com.app.ekottel.model;

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
public class CallRatesDB extends SQLiteOpenHelper {

    private final static String DB_NAME = "database_call_rates";
    private final static int DB_VERSION = 1;

    private final static String TABLE_NAME = "database_table_call_rates";
    private final static String TABLE_ROW_PREFIX = "prefix";
    private final static String TABLE_ROW_CALLRATE = "callRate";
    private final static String TABLE_ROW_CONNECTIONFEE = "connectionFee";

    long value;

    public CallRatesDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String newTableQueryString = "CREATE TABLE " + TABLE_NAME + " ("
                + TABLE_ROW_PREFIX + " text," + TABLE_ROW_CALLRATE + " text," + TABLE_ROW_CONNECTIONFEE + " text"+ ");";
        db.execSQL(newTableQueryString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public void addContact(List<CallRatesModel> callRatesList) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < callRatesList.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(TABLE_ROW_PREFIX, callRatesList.get(i).getPrefix());
            values.put(TABLE_ROW_CALLRATE, callRatesList.get(i)
                    .getCallRate());
            values.put(TABLE_ROW_CONNECTIONFEE, callRatesList.get(i)
                    .getConnectionFee());


            value = db.insert(TABLE_NAME, null, values);
        }

        // Inserting Row

        db.close(); // Closing database connection
    }

    public List<CallRatesModel> getAllRecords() {
        List<CallRatesModel> callRatesPojoList = new ArrayList<CallRatesModel>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME+" DESC limit 10";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                CallRatesModel callRatesPojo = new CallRatesModel();
                callRatesPojo.setPrefix(cursor.getString(0));
                callRatesPojo.setCallRate(cursor.getString(1));
                callRatesPojo.setConnectionFee(cursor.getString(2));
                callRatesPojoList.add(callRatesPojo);
            } while (cursor.moveToNext());
        }

        cursor.close();

        // return contact list
        return callRatesPojoList;
    }


    public void deleteTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NAME);
    }


}
