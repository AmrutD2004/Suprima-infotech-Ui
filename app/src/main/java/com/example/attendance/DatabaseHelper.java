package com.example.attendance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String databasename = "Signup.db";

    public DatabaseHelper(@Nullable Context context) {
        super(context, "Signup.db", null, 2); // Increased version to trigger onUpgrade
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE allUser(email TEXT primary key, password TEXT, fullname TEXT, empId TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add new columns to existing table
            try {
                sqLiteDatabase.execSQL("ALTER TABLE allUser ADD COLUMN fullname TEXT");
                sqLiteDatabase.execSQL("ALTER TABLE allUser ADD COLUMN empId TEXT");
            } catch (Exception e) {
                // If alter table fails, recreate the table
                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS allUser");
                onCreate(sqLiteDatabase);
            }
        }
    }

    // Original method kept for backward compatibility
    public Boolean insertData(String email, String password) {
        return insertData("", "", email, password);
    }

    // New method that includes fullname and empId
    public Boolean insertData(String fullname, String empId, String email, String password) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("password", password);
        contentValues.put("fullname", fullname);
        contentValues.put("empId", empId);

        long result = sqLiteDatabase.insert("allUser", null, contentValues);
        sqLiteDatabase.close();

        return result != -1;
    }

    public Boolean checkEmail(String email) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase(); // Changed to getReadableDatabase
        Cursor cursor = sqLiteDatabase.rawQuery(
                "SELECT * FROM allUser WHERE email = ?",
                new String[]{email}
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        sqLiteDatabase.close();
        return exists;
    }

    public Boolean checkEmailPassword(String email, String password) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase(); // Changed to getReadableDatabase
        Cursor cursor = sqLiteDatabase.rawQuery(
                "SELECT * FROM allUser WHERE email = ? AND password = ?",
                new String[]{email, password}
        );

        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        sqLiteDatabase.close();
        return isValid;
    }

    // Optional: Helper method to get user data
    public Cursor getUserData(String email) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return sqLiteDatabase.rawQuery(
                "SELECT * FROM allUser WHERE email = ?",
                new String[]{email}
        );
    }
}