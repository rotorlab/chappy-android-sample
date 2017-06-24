package com.flamebase.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by efraespada on 23/05/2017.
 */

public class Database extends SQLiteOpenHelper {

    public static final String COLUMN_ID = "id_hash";
    public static final String COLUMN_DATA = "data";

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
                        COLUMN_ID + " TEXT PRIMARY KEY, " +
                        COLUMN_DATA + " TEXT);";
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + table;
            db.execSQL(SQL_DELETE_ENTRIES);
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            onCreate(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}