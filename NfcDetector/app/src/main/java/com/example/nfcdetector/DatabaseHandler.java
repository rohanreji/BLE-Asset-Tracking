package com.example.nfcdetector;

import java.util.ArrayList;
import java.util.List;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
 
public class DatabaseHandler extends SQLiteOpenHelper {
 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "empManager";
 
    // Contacts table name
    private static final String TABLE_CONTACTS = "employ";
 
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    static final String KEY_SI_NA= "site_name";

    static final String KEY_PR_CO = "project_code";
    static final String KEY_IM_UR = "img_url";
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " TEXT PRIMARY KEY," + KEY_NAME + " TEXT,"+KEY_SI_NA+" TEXT,"
                + KEY_PR_CO + " INTEGER,"+KEY_IM_UR+" TEXT" + ")";
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
    void addContact(EmpDetails contact) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_ID,contact.getID());
        values.put(KEY_NAME, contact.getName()); // Contact Name
        values.put(KEY_SI_NA, contact.getSiteName()); 
        values.put(KEY_PR_CO, contact.getProjectCode()); 
        values.put(KEY_IM_UR, contact.getImageUrl()); 
        values.put(KEY_PR_CO, contact.getProjectCode()); // Contact Phone
 
        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }
 
    // Getting single contact
    EmpDetails getContact(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
 
        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
                KEY_NAME,KEY_SI_NA, KEY_PR_CO,KEY_IM_UR }, KEY_ID + "=?",
                new String[] { id }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
 
        EmpDetails contact = new EmpDetails(cursor.getString(0),
                cursor.getString(1), cursor.getString(2),Integer.parseInt(cursor.getString(3)),cursor.getString(4));
        // return contact
        return contact;
    }
     
    // Getting All Contacts
    public List<EmpDetails> getAllContacts() {
        List<EmpDetails> contactList = new ArrayList<EmpDetails>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                EmpDetails contact = new EmpDetails();
                contact.setID(cursor.getString(0));
                contact.setName(cursor.getString(1));
                contact.setSiteName(cursor.getString(2));
                contact.setProjectCode(Integer.parseInt(cursor.getString(3)));
                contact.setImageUrl(cursor.getString(4));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }
 
        // return contact list
        return contactList;
    }
 
    // Updating single contact
    public int updateContact(EmpDetails contact) {
        SQLiteDatabase db = this.getWritableDatabase();
 
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName());
        values.put(KEY_SI_NA, contact.getSiteName());
        values.put(KEY_PR_CO, contact.getProjectCode());
        values.put(KEY_IM_UR, contact.getImageUrl());
 
        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(contact.getID()) });
    }
 
    // Deleting single contact
    public void deleteContact(EmpDetails contact) {
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
