package org.sysu.herrick.goal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



/**
 * Created by herrick on 2016/12/19.
 */
public class NoteDB extends SQLiteOpenHelper {

    private static final String DB_NAME = "Note_Goal";
    private static final String TABLE_NAME = "Note_Data";
    private static final int DB_VERSION = 1;

    private static NoteDB mInstance = null;


    public NoteDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE if not exists " + TABLE_NAME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, startDate INTEGER, date INTEGER, endDate INTEGER, goalDes TEXT, reset INTEGER, achieved INTEGER, achievedDate INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public void insert(long date, long startDate, long endDate, String goalDes) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("startDate", startDate);
        cv.put("date", date);
        cv.put("endDate", endDate);
        cv.put("goalDes", goalDes);
        cv.put("reset", 0);
        cv.put("achieved", -1);
        cv.put("achievedDate", 0);
        db.insert(TABLE_NAME, null, cv);
        db.close();
    }
    public void update(int id, long startDate, long endDate, String goalDes, int reset) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("startDate", startDate);
        cv.put("endDate", endDate);
        cv.put("goalDes", goalDes);
        if (reset >= 0)
            cv.put("reset", reset);
        db.update(TABLE_NAME, cv, "_id=?", new String[] {"" + id} );
        db.close();
    }
    public void updateAchieved(int id, int achieved) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("achieved", achieved);
        if (achieved == 1) {
            cv.put("endDate", System.currentTimeMillis());
            cv.put("achievedDate", System.currentTimeMillis());
        }
        db.update(TABLE_NAME, cv, "_id=?", new String[] {"" + id} );
        db.close();
    }
    public int getID(String n, String v) {
        int id;
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor cursor = rdb.rawQuery("SELECT _id from " + TABLE_NAME + " WHERE " + n + "=" + v, null);
        cursor.moveToFirst();
        id = cursor.getInt(cursor.getColumnIndex("_id"));
        cursor.close();
        rdb.close();
        return id;
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
    public static NoteDB getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new NoteDB(ctx.getApplicationContext());
        }
        return mInstance;
    }
}
