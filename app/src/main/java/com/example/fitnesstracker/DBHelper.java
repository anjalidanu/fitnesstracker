package com.example.fitnesstracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fitness.db";
    private static final int DATABASE_VERSION = 2;

    // User Profile Table
    private static final String TABLE_PROFILE = "user_profile";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_WEIGHT = "weight";
    private static final String COL_HEIGHT = "height";

    // Daily Steps Table
    private static final String TABLE_STEPS = "daily_steps";
    private static final String COL_STEP_ID = "id";
    private static final String COL_DATE = "date";
    private static final String COL_STEPS = "steps";
    private static final String COL_CALORIES = "calories";
    private static final String COL_DISTANCE = "distance";

    // Settings Table
    private static final String TABLE_SETTINGS = "settings";
    private static final String COL_SETTING_KEY = "setting_key";
    private static final String COL_SETTING_VALUE = "setting_value";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create User Profile Table
        String createProfileTable = "CREATE TABLE " + TABLE_PROFILE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_WEIGHT + " REAL, " +
                COL_HEIGHT + " REAL)";
        db.execSQL(createProfileTable);

        // Create Daily Steps Table
        String createStepsTable = "CREATE TABLE " + TABLE_STEPS + " (" +
                COL_STEP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DATE + " TEXT UNIQUE, " +
                COL_STEPS + " INTEGER, " +
                COL_CALORIES + " REAL, " +
                COL_DISTANCE + " REAL)";
        db.execSQL(createStepsTable);

        // Create Settings Table
        String createSettingsTable = "CREATE TABLE " + TABLE_SETTINGS + " (" +
                COL_SETTING_KEY + " TEXT PRIMARY KEY, " +
                COL_SETTING_VALUE + " TEXT)";
        db.execSQL(createSettingsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STEPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        onCreate(db);
    }

    // ==================== USER PROFILE METHODS ====================

    public boolean insertOrUpdateProfile(String name, double weight, double height) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete old profile (we only keep one profile)
        db.delete(TABLE_PROFILE, null, null);

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_WEIGHT, weight);
        values.put(COL_HEIGHT, height);

        long result = db.insert(TABLE_PROFILE, null, values);
        return result != -1;
    }

    public Cursor getProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PROFILE + " LIMIT 1", null);
    }

    // ==================== DAILY STEPS METHODS ====================

    public boolean insertOrUpdateSteps(int steps, double calories, double distance) {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getCurrentDate();

        ContentValues values = new ContentValues();
        values.put(COL_DATE, today);
        values.put(COL_STEPS, steps);
        values.put(COL_CALORIES, calories);
        values.put(COL_DISTANCE, distance);

        // Try to update first
        int rowsAffected = db.update(TABLE_STEPS, values, COL_DATE + "=?", new String[]{today});

        // If no rows updated, insert new record
        if (rowsAffected == 0) {
            long result = db.insert(TABLE_STEPS, null, values);
            return result != -1;
        }
        return true;
    }

    public Cursor getTodaySteps() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getCurrentDate();
        return db.rawQuery("SELECT * FROM " + TABLE_STEPS + " WHERE " + COL_DATE + "=?",
                new String[]{today});
    }

    public Cursor getAllSteps() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_STEPS + " ORDER BY " + COL_DATE + " DESC", null);
    }

    public Cursor getRecentSteps(int days) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_STEPS + " ORDER BY " + COL_DATE +
                " DESC LIMIT ?", new String[]{String.valueOf(days)});
    }

    // ==================== SETTINGS METHODS ====================

    public boolean saveSetting(String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SETTING_KEY, key);
        values.put(COL_SETTING_VALUE, value);

        // Try to update first
        int rowsAffected = db.update(TABLE_SETTINGS, values, COL_SETTING_KEY + "=?",
                new String[]{key});

        // If no rows updated, insert new record
        if (rowsAffected == 0) {
            long result = db.insert(TABLE_SETTINGS, null, values);
            return result != -1;
        }
        return true;
    }

    public String getSetting(String key, String defaultValue) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_SETTING_VALUE + " FROM " + TABLE_SETTINGS +
                " WHERE " + COL_SETTING_KEY + "=?", new String[]{key});

        if (cursor.moveToFirst()) {
            String value = cursor.getString(0);
            cursor.close();
            return value;
        }
        cursor.close();
        return defaultValue;
    }

    public boolean deleteSetting(String key) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_SETTINGS, COL_SETTING_KEY + "=?", new String[]{key});
        return result > 0;
    }

    // ==================== UTILITY METHODS ====================

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public void resetAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STEPS, null, null);
        db.delete(TABLE_SETTINGS, null, null);
    }
}