package org.sysu.herrick.goal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class KeyDB extends SQLiteOpenHelper {

    private static final String DB_NAME = "Key";
    private static final String TABLE_KEY = "Diary_key";
    private static final int DB_VERSION = 1;

    private static KeyDB mInstance = null;

    public KeyDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE if not exists " + TABLE_KEY + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, key INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public int queryKey() {
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor cursor = rdb.rawQuery("SELECT * from " + TABLE_KEY, null);
        int key = -1;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            key = cursor.getInt(cursor.getColumnIndex("key"));
        }
        rdb.close();
        cursor.close();
        return key;
    }
    public void setKey(int key) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("key", key);
        db.insert(TABLE_KEY, null, cv);
        db .close();
    }
    public void updateKey(int newkey) {
        ContentValues cv = new ContentValues();
        cv.put("key", newkey);
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_KEY, cv, "_id=?", new String[] {"" + 1} );
        db.close();
    }
    public static KeyDB getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new KeyDB(ctx.getApplicationContext());
        }
        return mInstance;
    }
}
