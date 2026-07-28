package com.mateus.geofence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "geofence.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE locations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "latitude REAL NOT NULL," +
                "longitude REAL NOT NULL," +
                "radius INTEGER NOT NULL DEFAULT 200," +
                "alarm_mode TEXT NOT NULL DEFAULT 'both'," +
                "enabled INTEGER NOT NULL DEFAULT 1)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS locations");
        onCreate(db);
    }

    public long addLocation(String name, double lat, double lng, int radius, String alarmMode) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("latitude", lat);
        values.put("longitude", lng);
        values.put("radius", radius);
        values.put("alarm_mode", alarmMode);
        values.put("enabled", 1);
        return db.insert("locations", null, values);
    }

    public void updateLocation(int id, String name, double lat, double lng, int radius, String alarmMode) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("latitude", lat);
        values.put("longitude", lng);
        values.put("radius", radius);
        values.put("alarm_mode", alarmMode);
        db.update("locations", values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void toggleLocation(int id, boolean enabled) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("enabled", enabled ? 1 : 0);
        db.update("locations", values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteLocation(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("locations", "id = ?", new String[]{String.valueOf(id)});
    }

    public List<GeofenceLocation> getAllLocations() {
        List<GeofenceLocation> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("locations", null, null, null, null, null, "id DESC");
        while (cursor.moveToNext()) {
            GeofenceLocation loc = new GeofenceLocation();
            loc.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            loc.name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            loc.latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
            loc.longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));
            loc.radius = cursor.getInt(cursor.getColumnIndexOrThrow("radius"));
            loc.alarmMode = cursor.getString(cursor.getColumnIndexOrThrow("alarm_mode"));
            loc.enabled = cursor.getInt(cursor.getColumnIndexOrThrow("enabled")) == 1;
            list.add(loc);
        }
        cursor.close();
        return list;
    }

    public List<GeofenceLocation> getEnabledLocations() {
        List<GeofenceLocation> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("locations", null, "enabled = 1", null, null, null, null);
        while (cursor.moveToNext()) {
            GeofenceLocation loc = new GeofenceLocation();
            loc.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            loc.name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            loc.latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
            loc.longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));
            loc.radius = cursor.getInt(cursor.getColumnIndexOrThrow("radius"));
            loc.alarmMode = cursor.getString(cursor.getColumnIndexOrThrow("alarm_mode"));
            loc.enabled = true;
            list.add(loc);
        }
        cursor.close();
        return list;
    }
}
