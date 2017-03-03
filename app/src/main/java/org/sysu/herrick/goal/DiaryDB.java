package org.sysu.herrick.goal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



/**
 * Created by herrick on 2016/12/19.
 */
public class DiaryDB extends SQLiteOpenHelper {

    private static final String DB_NAME = "Diary";
    private static final String TABLE_NAME = "Diary_Data";
    private static final int DB_VERSION = 1;
    private static DiaryDB mInstance = null;

    public DiaryDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE if not exists " + TABLE_NAME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, date INTEGER, datePrime INTEGER, weekday INTEGER, editTime INTEGER, diary_content TEXT, weather TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(long date, int datePrime, long editTime, int weekday, String diary_content, String weather) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("date", date);
        cv.put("datePrime", datePrime);
        cv.put("editTime", editTime);
        cv.put("weekday", weekday);
        cv.put("diary_content", diary_content);
        cv.put("weather", weather);
        db.insert(TABLE_NAME, null, cv);
        db.close();
    }
    public void update(int id, ContentValues cv) {
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_NAME, cv, "_id=?", new String[] {"" + id} );
        db.close();
    }
    // return the first id, better call this method using unique value
    public int getID(String n, String v) {
        int id;
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor cursor = rdb.rawQuery("SELECT _id from " + TABLE_NAME + " WHERE " + n + "=" + v, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            id = cursor.getInt(cursor.getColumnIndex("_id"));
            cursor.close();
            rdb.close();
            return id;
        } else
            return -1;
    }
    public Cursor query(String raw) {
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor cursor = rdb.rawQuery(raw, null);
        return cursor;
    }
    public void delete(int id) {
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor cursor = rdb.rawQuery("SELECT * from " + TABLE_NAME + " WHERE _id=" + id, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            if (id == cursor.getInt(cursor.getColumnIndex("_id"))) {
                SQLiteDatabase db = getWritableDatabase();
                db.delete(TABLE_NAME, "_id=?", new String[] {"" + id});
                db.close();
            }
        }
        cursor.close();
        rdb.close();
    }
    public static DiaryDB getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new DiaryDB(ctx.getApplicationContext());
        }
        return mInstance;
    }
}
