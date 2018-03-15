package com.flamebase.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.flamebase.database.interfaces.FlamebaseService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.flamebase.database.Database.COLUMN_DATA;
import static com.flamebase.database.Database.COLUMN_ID;

/**
 * Created by efraespada on 29/06/2017.
 */

public class ReferenceUtils {

    private static int VERSION = 1;
    private static String TABLE_NAME = "ref";

    public static Database database;
    private static Context context;

    private ReferenceUtils(Context context) {
        // nothing to do here
    }

    public static void initialize(Context context) {
        ReferenceUtils.context = context;
    }

    private static String string2Hex(byte[] data) {
        return new BigInteger(1, data).toString(16);
    }

    public static String hex2String(String value) {
        return new String(new BigInteger(value, 16).toByteArray());
    }

    public static String SHA1(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] textBytes = text.getBytes("iso-8859-1");
            md.update(textBytes, 0, textBytes.length);
            byte[] sha1hash = md.digest();

            return string2Hex(sha1hash).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * updates stored path
     * @param path
     * @param info
     */
    public static void addElement(final String path, String info) {
        if (database == null) {
            String name = "RealtimeDatabase.db";
            database = new Database(context, name, TABLE_NAME, VERSION);
        }
        try {
            String enId = SC.encryptString(path);
            SQLiteDatabase db = database.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, enId);
            values.put(COLUMN_DATA, SC.encryptString(info));

            if (exist(path)) {
                String selection = COLUMN_ID + " = ?";
                String[] selectionArgs = { enId };
                long newRowId = db.update(database.table, values, selection, selectionArgs);
            } else {
                long newRowId = db.insert(database.table, null, values);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    private static boolean exist(String path) {
        if (database == null) {
            String name = "RealtimeDatabase.db";
            database = new Database(context, name, TABLE_NAME, VERSION);
        }
        String enPath = SC.encryptString(path);
        try {
            SQLiteDatabase db = database.getReadableDatabase();

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    COLUMN_ID,
                    COLUMN_DATA
            };

            // Filter results WHERE "title" = hash
            String selection = COLUMN_ID + " = ?";
            String[] selectionArgs = {enPath};

            Cursor cursor = db.query(
                    database.table,                             // The table to query
                    projection,                               // The columns to return
                    selection,                                // The columns for the WHERE clause
                    selectionArgs,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    null                                      // The sort order
            );

            boolean exists = cursor.getCount() > 0;
            cursor.close();
            //database.close();

            return exists;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void removeElement(String path) {
        if (database == null) {
            String name = "RealtimeDatabase.db";
            database = new Database(context, name, TABLE_NAME, VERSION);
        }
        String enPath = SC.encryptString(path);
        try {
            SQLiteDatabase db = database.getReadableDatabase();

            String selection = COLUMN_ID + " = ?";
            String[] selectionArgs = {enPath};

            int result = db.delete(
                    database.table,                             // The table to query
                    selection,                                // The columns for the WHERE clause
                    selectionArgs                            // The values for the WHERE clause
            );

        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }


    /**
     * returns stored object
     * @param path
     * @return String
     */
    public static String getElement(String path) {
        if (database == null) {
            String name = "RealtimeDatabase.db";
            database = new Database(context, name, TABLE_NAME, VERSION);
        }
        String enPath = SC.encryptString(path);
        try {
            SQLiteDatabase db = database.getReadableDatabase();
            String[] projection = {
                    COLUMN_ID,
                    COLUMN_DATA
            };

            String selection = COLUMN_ID + " = ?";
            String[] selectionArgs = { enPath };

            Cursor cursor = db.query(
                    database.table,                             // The table to query
                    projection,                               // The columns to return
                    selection,                                // The columns for the WHERE clause
                    selectionArgs,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    null                                      // The sort order
            );

            String info = null;
            while (cursor.moveToNext()) {
                info = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATA));
            }
            cursor.close();

            if (info != null) {
                return SC.decryptString(info);
            } else {
                return null;
            }
        } catch (SQLiteException e) {
            return null;
        }
    }

    public static FlamebaseService service(String url) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        // TODO intercept time out exceptions
                        Response response = chain.proceed(chain.request());
                        return response;
                    }
                });

        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.client(httpClient.build()).build();

        return retrofit.create(FlamebaseService.class);
    }

}
