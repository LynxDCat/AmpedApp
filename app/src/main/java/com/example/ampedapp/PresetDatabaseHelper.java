// PresetDatabaseHelper.java
package com.example.ampedapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PresetDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "presets.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "presets";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EFFECTS = "effects";

    public PresetDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EFFECTS + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean savePreset(String name, String effectsCsv) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EFFECTS, effectsCsv);
        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    // Inside PresetDatabaseHelper.java

    public ArrayList<Preset> getAllPresets() {
        ArrayList<Preset> presetList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM presets", null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String effectsStr = cursor.getString(cursor.getColumnIndexOrThrow("effects"));
                List<String> effects = Arrays.asList(effectsStr.split(",")); // Assuming comma-separated
                presetList.add(new Preset(name, effects));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return presetList;
    }

    public boolean deletePreset(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete("presets", "name = ?", new String[]{name});
        db.close();
        return rowsDeleted > 0;
    }
}
