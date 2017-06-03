package com.flamebase.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by efraespada on 23/05/2017.
 */

public class Database extends SQLiteOpenHelper {

    public static final String COLUMN_LOCATION_ID = "location_hash";
    public static final String COLUMN_LOCATION_INFO = "location_info";

    public String databaseName;
    public String table;

    public Database(Context context, String name, String table, int version) {
        super(context, name, null, version);
        this.databaseName = name;
        this.table = table;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + table + " (" +
                        COLUMN_LOCATION_ID + " TEXT PRIMARY KEY, " +
                        COLUMN_LOCATION_INFO + " TEXT);";
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + table;
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}