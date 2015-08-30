package com.sensomate.quickswypes;

/**
 * Created by rohan on 14/5/15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AttendanceDbHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "attendanceManager";

    // Contacts table name
    private static final String TABLE_CONTACTS = "attendance";

    // Contacts Table Columns names
    private static final String KEY_ID = "uid";
    private static final String KEY_NAME = "name";
    static final String KEY_SI_NA= "sitename";

    static final String KEY_PR_CO = "projcode";
    static final String KEY_CNT = "count";
    static final String KEY_CA = "capturedAt";
    static final String KEY_CI = "checkedin";
    static final String KEY_CO = "checkedout";
    static final String KEY_TI = "time";

    public AttendanceDbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " TEXT PRIMARY KEY," + KEY_NAME + " TEXT,"+KEY_SI_NA+" TEXT,"
                + KEY_PR_CO + " INTEGER,"+KEY_CNT+" DOUBLE,"+KEY_CA+" INTEGER,"+KEY_CI +" INTEGER,"+KEY_CO+" INTEGER,"+KEY_TI+" DOUBLE"+")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }
    public void deletetab(SQLiteDatabase db)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new contact
    void addContact(AttendanceDetails contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID,contact.getID());
        values.put(KEY_NAME, contact.getName());
        values.put(KEY_SI_NA, contact.getSiteName());
        values.put(KEY_PR_CO, contact.getProjCode());
        values.put(KEY_CA, contact.getCapturedAt());
        values.put(KEY_CNT, contact.getCount());
        int myInt = (contact.getCheckedIn()) ? 1 : 0;
        values.put(KEY_CI, myInt);
        myInt = (contact.getCheckedOut()) ? 1 : 0;
        values.put(KEY_CO, myInt);
        values.put(KEY_TI,contact.getTime());

        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single contact
    AttendanceDetails getContact(String id) {
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS+" WHERE uid="+id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                boolean ci = (Integer.parseInt(cursor.getString(6)) != 0);
                boolean co = (Integer.parseInt(cursor.getString(7)) != 0);

                AttendanceDetails contact = new AttendanceDetails(cursor.getString(0), Long.valueOf(cursor.getString(4)).longValue(), Integer.parseInt(cursor.getString(3)),
                        cursor.getString(2), cursor.getString(1), Integer.parseInt(cursor.getString(5)), ci, co,  Long.valueOf(cursor.getString(8)).longValue());
                 return contact;
            } while (cursor.moveToNext());

        }
        return null;
    }

    // Getting All Contacts
    public List<AttendanceDetails> getAllContacts() {
        List<AttendanceDetails> contactList = new ArrayList<AttendanceDetails>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                AttendanceDetails contact = new AttendanceDetails();
                contact.setID(cursor.getString(0));
                contact.setName(cursor.getString(1));
                contact.setSiteName(cursor.getString(2));
                contact.setProjCode(cursor.getInt(3));
                contact.setCapturedAt(Long.valueOf(cursor.getString(4)).longValue());

                contact.setCount(cursor.getInt(5));
                boolean ci = (Integer.parseInt(cursor.getString(6)) != 0);
                boolean co = (Integer.parseInt(cursor.getString(7)) != 0);
                contact.setCheckedIn(ci);
                contact.setCheckedOut(co);
                contact.setTime(Long.valueOf(cursor.getString(4)).longValue());
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    // Updating single contact
    public int updateContact(AttendanceDetails contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NAME, contact.getName());
        values.put(KEY_SI_NA, contact.getSiteName());
        values.put(KEY_PR_CO, contact.getProjCode());
        values.put(KEY_CA, contact.getCapturedAt());
        values.put(KEY_CNT, contact.getCount());
        int myInt = (contact.getCheckedIn()) ? 1 : 0;
        values.put(KEY_CI, myInt);
        myInt = (contact.getCheckedOut()) ? 1 : 0;
        values.put(KEY_CO, myInt);
        values.put(KEY_TI, contact.getTime());


        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(contact.getID()) });
    }

    // Deleting single contact
    public void deleteContact(AttendanceDetails contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[] { String.valueOf(contact.getID()) });
        db.close();
    }


    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int a=cursor.getCount();
        cursor.close();
        // return count
        return a;

    }

}
