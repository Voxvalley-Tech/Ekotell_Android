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
public class TransactionHistoryDB extends SQLiteOpenHelper {

    private final static String DB_NAME = "database_transaction_history";
    private final static int DB_VERSION = 3;

    private final static String TABLE_NAME = "database_table_transaction_history";
    private final static String TABLE_ROW_ID = "id";
    private final static String TABLE_ROW_DATE = "date";
    private final static String TABLE_ROW_COST = "cost";
    private final static String TABLE_ROW_RECHARGE_TYPE = "type";
    private final static String TABLE_ROW_RECHARGE_FROM = "fromuser";
    private final static String TABLE_ROW_RECHARGE_TO = "touser";

    long value;

    public TransactionHistoryDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String newTableQueryString = "CREATE TABLE " + TABLE_NAME + " ("
                + TABLE_ROW_ID + " integer primary key autoincrement not null," + TABLE_ROW_DATE + " text," + TABLE_ROW_COST + " text,"
                + TABLE_ROW_RECHARGE_TYPE + " text," + TABLE_ROW_RECHARGE_FROM + " text," + TABLE_ROW_RECHARGE_TO + " text" + ");";
        db.execSQL(newTableQueryString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public void addContact(List<RechargeHistoryPojo> rechargeHistoryList) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < rechargeHistoryList.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(TABLE_ROW_DATE, rechargeHistoryList.get(i).getDate());
            values.put(TABLE_ROW_COST, rechargeHistoryList.get(i)
                    .getCost());
            values.put(TABLE_ROW_RECHARGE_TYPE, rechargeHistoryList.get(i)
                    .getType());

            values.put(TABLE_ROW_RECHARGE_FROM, rechargeHistoryList.get(i)
                    .getFromuser());
            values.put(TABLE_ROW_RECHARGE_TO, rechargeHistoryList.get(i)
                    .getTouser());

            value = db.insert(TABLE_NAME, null, values);
        }

        // Inserting Row

        db.close(); // Closing database connection
    }

    public List<RechargeHistoryPojo> getAllRecords() {
        List<RechargeHistoryPojo> rechargeHistoryPojoList = new ArrayList<RechargeHistoryPojo>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                RechargeHistoryPojo rechargeHistoryPojo = new RechargeHistoryPojo();
                rechargeHistoryPojo.setDate(cursor.getString(1));
                rechargeHistoryPojo.setCost(cursor.getString(2));
                rechargeHistoryPojo.setType(cursor.getString(3));
                rechargeHistoryPojo.setFromuser(cursor.getString(4));
                rechargeHistoryPojo.setTouser(cursor.getString(5));
                rechargeHistoryPojoList.add(rechargeHistoryPojo);
            } while (cursor.moveToNext());
        }

        cursor.close();

        // return contact list
        return rechargeHistoryPojoList;
    }


    public List<RechargeHistoryPojo> getFilteredRecords(String type) {
        List<RechargeHistoryPojo> rechargeHistoryPojoList = new ArrayList<RechargeHistoryPojo>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME+" WHERE"+" "+TABLE_ROW_RECHARGE_TYPE+" = "+"'"+type+"'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                RechargeHistoryPojo rechargeHistoryPojo = new RechargeHistoryPojo();
                rechargeHistoryPojo.setDate(cursor.getString(1));
                rechargeHistoryPojo.setCost(cursor.getString(2));
                rechargeHistoryPojo.setType(cursor.getString(3));
                rechargeHistoryPojo.setFromuser(cursor.getString(4));
                rechargeHistoryPojo.setTouser(cursor.getString(5));
                rechargeHistoryPojoList.add(rechargeHistoryPojo);
            } while (cursor.moveToNext());
        }

        cursor.close();

        // return contact list
        return rechargeHistoryPojoList;
    }


    public void deleteTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NAME);
    }


}
