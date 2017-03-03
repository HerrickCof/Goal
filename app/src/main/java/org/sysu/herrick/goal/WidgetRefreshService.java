package org.sysu.herrick.goal;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Herrick on 2017/1/7.
 */

public class WidgetRefreshService extends Service {

    public final MyBinder binder = new MyBinder();
    private int new_goal = -1000;
    private static final String TABLE_NAME = "Note_Data";
    private NoteDB noteDB = null;
    private static String UPDATE_FROM_SERVICE = "org.sysu.herrick.goal.WidgetRefreshService";
    public class MyBinder extends Binder {
        WidgetRefreshService getService() {
            return WidgetRefreshService.this;
        }
    }
    public void sendBroadcastWithUpdateData() {
        Intent inRet = new Intent();
        inRet.setAction(UPDATE_FROM_SERVICE);
        Bundle bundleRet = new Bundle();
        SharedPreferences preferences = getSharedPreferences("widget_goal_ids", Context.MODE_APPEND);
        for (int i = 0; i < 3; i++) {
            int id = preferences.getInt("id_" + i, -2);
            Cursor cursor = noteDB.query("SELECT * from " +  TABLE_NAME + " WHERE _id=" + id);
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                bundleRet.putString("des_" + i, cursor.getString(cursor.getColumnIndex("goalDes")));
                long sdate = cursor.getLong(cursor.getColumnIndex("startDate"));
                long edate = cursor.getLong(cursor.getColumnIndex("endDate"));
                long now = System.currentTimeMillis();
                if (now > edate) {
                    now = edate;
                }
                if (cursor.getInt(cursor.getColumnIndex("achieved")) == 1) {
                    bundleRet.putInt("achieved_" + i, cursor.getInt(cursor.getColumnIndex("achieved")));
                    now =  cursor.getLong(cursor.getColumnIndex("achievedDate"));
                }
                sdate /= 86400000;
                now /= 86400000;
                if (now - sdate < 0)
                    bundleRet.putLong("days_" + i, 0);
                else
                    bundleRet.putLong("days_" + i, now - sdate);
            } else {
                bundleRet.putString("des_" + i, "");
                bundleRet.putInt("achieved_" + i, -1);
                bundleRet.putLong("days_" + i, -1);
            }
            cursor.close();
        }
        Cursor c = noteDB.query("SELECT _id, endDate from " + TABLE_NAME + " ORDER BY endDate ASC");
        if (c.getCount() != 0) {
            c.moveToFirst();
            long now = System.currentTimeMillis();
            for (int i = 0; i < c.getCount(); i++, c.moveToNext()) {
                if (c.getLong(c.getColumnIndex("endDate")) >= now) {
                    bundleRet.getInt("next_ddl_id",c.getInt(c.getColumnIndex("_id")));
                    Date ddl = new Date(c.getLong(c.getColumnIndex("endDate")));
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    String t = formatter.format(ddl);
                    bundleRet.putString("next_ddl", t);
                    break;
                }
            }
        }
        c.close();
        inRet.putExtras(bundleRet);
        sendBroadcast(inRet);
    }
    /*private void openGoalDetail(int which) {
        SharedPreferences pref_goal = getSharedPreferences("which_goal", Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref_goal.edit();
        SharedPreferences pref_widget = getSharedPreferences("widget_goal_ids", Context.MODE_APPEND);
        switch (which) {
            case 1:
                editor.clear();
                editor.putInt("which_goal", pref_widget.getInt("id_1", new_goal));
                break;
            case 2:
                editor.clear();
                editor.putInt("which_goal", pref_widget.getInt("id_2", new_goal));
                break;
            case 3:
                editor.clear();
                editor.putInt("which_goal", pref_widget.getInt("id_3", new_goal));
                break;
            default:
                break;
        }
        editor.commit();
        Intent in = new Intent(this, GoalDetailActivity.class);
        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);
    }*/
    @Override
    public int onStartCommand(Intent intent, int flag, int id) {
        //Log.i("ggggg", "SERVICE REQUEST :" + intent.getExtras().getString("widget_request"));
        if (intent.getExtras().getString("widget_request").equals("require_update")) {
            sendBroadcastWithUpdateData();
        }/* else if (intent.getExtras().getString("widget_request").equals("open_detail")) {
            openGoalDetail(intent.getExtras().getInt("which_goal"));
            Log.i("ggggg", "SERVICE OPENDETAIL WHICH_GOAL :"
                    + "\n" + intent.getExtras().getInt("which_goal"));
        }*/

        super.onStartCommand(intent, flag, id);
        return START_STICKY;
    }
    public WidgetRefreshService() {
        noteDB = new NoteDB(WidgetRefreshService.this);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
