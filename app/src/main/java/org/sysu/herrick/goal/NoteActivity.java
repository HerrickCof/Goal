package org.sysu.herrick.goal;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


import static java.lang.Thread.sleep;

/**
 * Created by herrick on 2016/12/19.
 */

public class NoteActivity extends AppCompatActivity {

    /*database constant*/

    private static final String TABLE_NAME = "Note_Data";

    private static String REMINDER_ACTION = "org.sysu.herrick.goal.ReminderAlarmReceiver";

    private int mode_setKey = 5555;
    private int mode_authen = 4444;
    private int new_goal = -1000;


    private Button add              = null;
    private GridView gridView       = null;
    private TableRow diary          = null;
    private TableRow diary_view     = null;
    private TableRow diary_key      = null;
    private TableRow reminder       = null;
    private TableRow reminder_new   = null;
    private TableRow reminder_set  = null;
    private TableRow statistics     = null;
    private TableRow calendar       = null;
    private TextView drawer_setting = null;
    private TextView drawer_about   = null;
    private TextView showText       = null;
    private ImageButton reminder_complete = null;
    private boolean diary_toggle    = false;
    private boolean reminder_toggle = false;
    private ArrayList<HashMap<String, Object>> gridItem = null;
    private SimpleAdapter simpleAdapter;
    private DrawerLayout drawerLayout = null;
    private RelativeLayout drawer_left = null;
    private NoteDB noteDB           = null;


    private boolean reminder_select_mode = false;
    private int reminder_select_amount = 0;
    private List<Integer> reminder_selected_ids = new ArrayList<>();
    private List<Integer> gridIndex = new ArrayList<>();

    private boolean set_reminder_mode = false;
    private int reminder_selected_id = -100;
    private int reminder_last_selected_index = -101;

    private boolean isBind = false;
    private WidgetRefreshService service;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = ((WidgetRefreshService.MyBinder)iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            service = null;
        }
    };
    private android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            //Toast.makeText(NoteActivity.this, "BIND SERVICE SUCCESS : " + data.getInt("times") + " TIMES", Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note);

        add = (Button) findViewById(R.id.add_goal);
        gridView = (GridView) findViewById(R.id.note_goal);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_note);
        drawer_left = (RelativeLayout) findViewById(R.id.note_drawer_left);
        diary = (TableRow) findViewById(R.id.drawer_diary);
        diary_view = (TableRow) findViewById(R.id.drawer_diary_view);
        diary_key = (TableRow) findViewById(R.id.drawer_diary_key);
        reminder = (TableRow) findViewById(R.id.drawer_reminder);
        reminder_new = (TableRow) findViewById(R.id.drawer_reminder_new);
        reminder_set = (TableRow) findViewById(R.id.drawer_reminder_view);
        statistics = (TableRow) findViewById(R.id.drawer_statistics);
        calendar = (TableRow) findViewById(R.id.drawer_calendar);
        drawer_setting = (TextView) findViewById(R.id.drawer_setting);
        drawer_about = (TextView) findViewById(R.id.drawer_about);
        showText = (TextView) findViewById(R.id.note_no_goal_show_text);
        noteDB = NoteDB.getInstance(NoteActivity.this);
        drawer_left = (RelativeLayout) findViewById(R.id.note_drawer_left);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_note);
        reminder_complete = (ImageButton) findViewById(R.id.reminder_select_complete);


        /*set listeners*/
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                TextView goalDes = (TextView) view.findViewById(R.id.note_goal_des);
                int dbid = noteDB.getID("goalDes", "'" + goalDes.getText().toString() + "'");
                if (reminder_select_mode) {
                    if (reminder_select_amount < 3) {
                        if (reminder_selected_ids.indexOf(dbid) != -1) {
                            view.findViewById(R.id.note_goal_selected).setVisibility(View.GONE);
                            reminder_selected_ids.remove(reminder_selected_ids.indexOf(dbid));
                            gridIndex.remove(gridIndex.indexOf(i));
                            reminder_select_amount--;
                        } else {
                            view.findViewById(R.id.note_goal_selected).setVisibility(View.VISIBLE);
                            reminder_selected_ids.add(dbid);
                            gridIndex.add(i);
                            reminder_select_amount++;
                        }
                    } else if (reminder_select_amount == 3) {
                        if (reminder_selected_ids.indexOf(dbid) != -1) {
                            view.findViewById(R.id.note_goal_selected).setVisibility(View.GONE);
                            reminder_selected_ids.remove(reminder_selected_ids.indexOf(dbid));
                            gridIndex.remove(gridIndex.indexOf(i));
                            reminder_select_amount--;
                        } else {
                            Toast.makeText(NoteActivity.this, "At most 3 goals", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (set_reminder_mode) {
                    if (reminder_last_selected_index >= 0)
                        parent.getChildAt(reminder_last_selected_index).findViewById(R.id.note_goal_selected).setVisibility(View.GONE);
                    reminder_selected_id = dbid;
                    view.findViewById(R.id.note_goal_selected).setVisibility(View.VISIBLE);
                    reminder_last_selected_index = i;
                } else {
                    Intent in = new Intent();
                    in.setClassName(NoteActivity.this, "org.sysu.herrick.goal.GoalDetailActivity");
                    SharedPreferences pref = getSharedPreferences("which_goal", Context.MODE_APPEND);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.clear();
                    editor.putInt("which_goal", dbid);
                    editor.commit();
                    startActivity(in);
                }
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final TextView goalDes = (TextView) view.findViewById(R.id.note_goal_des);
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                builder.setTitle("Are you sure about this?");
                builder.setMessage("Delete this goal?\n    \"  " + goalDes.getText().toString() + "  \"");
                builder.setPositiveButton("Yes, I failed it", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        noteDB.delete(noteDB.getID("goalDes", "'" + goalDes.getText().toString() + "'"));
                        gridItem.remove(position);
                        simpleAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("No, I will keep it", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });
        reminder_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reminder_select_mode) {
                    int i = 0;
                    SharedPreferences preferences = getSharedPreferences("widget_goal_ids", Context.MODE_APPEND);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    for (i = 0; i < reminder_select_amount; i++) {
                        gridView.getChildAt(gridIndex.get(i)).findViewById(R.id.note_goal_selected).setVisibility(View.GONE);
                        editor.putInt("id_" + i, reminder_selected_ids.get(i));
                    }
                    for (; i < 3; i++) {
                        editor.putInt("id_" + i, -1);
                    }
                    editor.commit();
                /*Toast.makeText(NoteActivity.this, reminder_selected_ids.get(0)
                        + "|" + reminder_selected_ids.get(1)
                        + "|" + reminder_selected_ids.get(2), Toast.LENGTH_SHORT).show();*/

                    reminder_select_mode = false;
                    reminder_select_amount = 0;
                    reminder_selected_ids.clear();
                    gridIndex.clear();
                    add.setVisibility(View.VISIBLE);
                    reminder_complete.setVisibility(View.GONE);

                /*Intent in = new Intent(NoteActivity.this, WidgetRefreshService.class);
                Bundle bundle = new Bundle();
                bundle.putString("widget_request", "update_require");
                in.putExtras(bundle);
                startService(in);*/
                    service.sendBroadcastWithUpdateData();
                } else if (set_reminder_mode) {
                    gridView.getChildAt(reminder_last_selected_index).findViewById(R.id.note_goal_selected).setVisibility(View.GONE);
                    gridIndex.clear();
                    add.setVisibility(View.VISIBLE);
                    reminder_complete.setVisibility(View.GONE);
                    set_reminder_mode = false;
                    Cursor cursor = noteDB.query("SELECT endDate from " + TABLE_NAME + " WHERE _id=" + reminder_selected_id);
                    reminder_last_selected_index = -101;
                    if (cursor.getCount() != 0)
                        cursor.moveToFirst();
                    final long ddl = cursor.getLong(cursor.getColumnIndex("endDate"));
                    cursor.close();
                    Calendar ddd = Calendar.getInstance();
                    ddd.setTimeInMillis(ddl);
                    int dPrime = ddd.get(Calendar.YEAR) * 10000 + (ddd.get(Calendar.MONTH) + 1) * 100
                            + ddd.get(Calendar.DAY_OF_MONTH);
                    ddd.setTimeInMillis(System.currentTimeMillis());
                    int nPrime = ddd.get(Calendar.YEAR) * 10000 + (ddd.get(Calendar.MONTH) + 1) * 100
                            + ddd.get(Calendar.DAY_OF_MONTH);
                    if (dPrime < nPrime) {
                        Toast.makeText(NoteActivity.this, "This goal is already out of date.", Toast.LENGTH_SHORT).show();
                        reminder_selected_id = -100;
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                    View layout = View.inflate(NoteActivity.this, R.layout.reminder_set_time, null);
                    final TimePicker timePicker = (TimePicker) layout.findViewById(R.id.time_picker);
                    builder.setView(layout);
                    builder.setTitle("Set the Time of Reminder");
                    builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int h = timePicker.getHour();
                            int min = timePicker.getMinute();
                            Calendar tt = Calendar.getInstance();
                            tt.setTimeInMillis(ddl);
                            tt.set(tt.get(Calendar.YEAR), tt.get(Calendar.MONTH),
                                    tt.get(Calendar.DAY_OF_MONTH), h, min, 0);
                            Intent intent = new Intent(NoteActivity.this, ReminderAlarmReceiver.class);
                            intent.setAction(REMINDER_ACTION);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(NoteActivity.this, 3333, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
                            am.set(AlarmManager.RTC_WAKEUP, tt.getTimeInMillis(), pendingIntent);
                            dialogInterface.dismiss();
                        }
                    });
                    Dialog dialog = builder.create();
                    dialog.show();
                    reminder_selected_id = -100;
                }
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*HashMap<String, Object> item = new HashMap<>();
                item.put("des", "一二三四五六七八九");
                item.put("days", "1000312");
                gridItem.add(item);
                simpleAdapter.notifyDataSetChanged();*/
                Intent in = new Intent();
                in.setClassName(NoteActivity.this, "org.sysu.herrick.goal.GoalDetailActivity");
                SharedPreferences pref = getSharedPreferences("which_goal", Context.MODE_APPEND);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.putInt("which_goal", new_goal);
                editor.commit();
                startActivity(in);
            }
        });
        diary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (diary_toggle) {
                    diary_toggle = false;
                    diary_view.setVisibility(View.GONE);
                    diary_key.setVisibility(View.GONE);
                } else {
                    diary_toggle = true;
                    diary_view.setVisibility(View.VISIBLE);
                    diary_key.setVisibility(View.VISIBLE);
                    reminder_toggle = false;
                    reminder_new.setVisibility(View.GONE);
                    reminder_set.setVisibility(View.GONE);
                }
            }
        });
        reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reminder_toggle) {
                    reminder_toggle = false;
                    reminder_new.setVisibility(View.GONE);
                    reminder_set.setVisibility(View.GONE);
                } else {
                    reminder_toggle = true;
                    reminder_new.setVisibility(View.VISIBLE);
                    reminder_set.setVisibility(View.VISIBLE);
                    diary_toggle = false;
                    diary_view.setVisibility(View.GONE);
                    diary_key.setVisibility(View.GONE);
                }
            }
        });
        diary_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent();
                in.setClassName(NoteActivity.this, "org.sysu.herrick.goal.EncryptionUtils");
                Bundle bundle = new Bundle();
                bundle.putInt("mode", mode_setKey);
                in.putExtras(bundle);
                startActivity(in);
                finish();
            }
        });
        diary_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent();
                in.setClassName(NoteActivity.this, "org.sysu.herrick.goal.EncryptionUtils");
                Bundle bundle = new Bundle();
                bundle.putInt("mode", mode_authen);
                bundle.putString("toClass", DiaryOverviewActivity.class.getName());
                in.putExtras(bundle);
                startActivity(in);
                finish();
            }
        });
        reminder_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reminder_select_mode = true;
                drawerLayout.closeDrawers();
                add.setVisibility(View.GONE);
                reminder_complete.setVisibility(View.VISIBLE);
                Toast.makeText(NoteActivity.this, "Choose at most 3 goals to fill the widget.", Toast.LENGTH_SHORT).show();
            }
        });
        reminder_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_reminder_mode = true;
                drawerLayout.closeDrawers();
                add.setVisibility(View.GONE);
                reminder_complete.setVisibility(View.VISIBLE);
                Toast.makeText(NoteActivity.this, "Choose one goal you want to be reminded.", Toast.LENGTH_SHORT).show();
            }
        });
        statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent();
                in.setClassName(NoteActivity.this, "org.sysu.herrick.goal.StatisticsActivity");
                Bundle bundle = new Bundle();
                bundle.putString("type", "whole");
                bundle.putString("className", getLocalClassName());
                in.putExtras(bundle);
                startActivity(in);
                finish();
            }
        });
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent();
                in.setClassName(NoteActivity.this, "org.sysu.herrick.goal.CalendarActivity");
                Bundle bundle = new Bundle();
                bundle.putString("type", "whole");
                bundle.putString("className", getLocalClassName());
                in.putExtras(bundle);
                startActivity(in);
                finish();
            }
        });
        drawer_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent();
                in.setClassName(NoteActivity.this, "org.sysu.herrick.goal.EncryptionUtils");
                Bundle bundle = new Bundle();
                bundle.putInt("mode", mode_authen);
                bundle.putString("toClass", SettingActivity.class.getName());
                in.putExtras(bundle);
                startActivity(in);
                finish();
            }
        });
        drawer_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent();
                in.setClassName(NoteActivity.this, "org.sysu.herrick.goal.AboutActivity");
                startActivity(in);
                finish();
            }
        });
        newThreadLoopBindService();
    }
    private void newThreadLoopBindService() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (!isBind) {
                    bindService();
                    i++;
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putInt("times", i);
                msg.setData(data);
                handler.sendMessage(msg);
            }
        }).start();
    }
    private void bindService() {
        /*SharedPreferences preferences = getSharedPreferences("widget_goal_ids", Context.MODE_APPEND);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();*/
        /*if (preferences.getInt("id_0", -10) == -10 )
            editor.putInt("id_0", 1);
        if (preferences.getInt("id_1", -10) == -10 )
            editor.putInt("id_1", 2);
        if (preferences.getInt("id_2", -10) == -10 )
            editor.putInt("id_2", 3);
        editor.commit();*/
        Intent serviceInt = new Intent(this, WidgetRefreshService.class);
        /*Bundle bundle = new Bundle();
        bundle.putString("widget_request", "update_require");
        serviceInt.putExtras(bundle);*/
        NoteActivity.this.getApplicationContext().bindService(serviceInt, connection , BIND_AUTO_CREATE);
        if (service != null) {
            service.sendBroadcastWithUpdateData();
            isBind = true;
        } else {
            //Toast.makeText(NoteActivity.this, "FAILED TO BIND SERVICE", Toast.LENGTH_SHORT).show();
            isBind = false;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        initGrid();
    }
    @Override
    public void onBackPressed() {
        NoteActivity.this.unbindService(connection);
        finish();
    }
    @Override
    public void onPause() {
        super.onPause();
        if (isBind)
            NoteActivity.this.getApplicationContext().unbindService(connection);
    }

    private void initGrid() {
        gridItem = new ArrayList<>();
        /*for (int i = 0; i < 15; i++) {
            HashMap<String, Object> item = new HashMap<>();
            item.put("des", "Goal描述" + i);
            item.put("days", i*i*i + "");
            gridItem.add(item);
        }*/
        Cursor cursor = noteDB.query("SELECT * from " + TABLE_NAME);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            showText.setVisibility(View.GONE);
            for(int i = 0; i < cursor.getCount(); i++) {
                HashMap<String, Object> item = new HashMap<>();
                item.put("des", cursor.getString(cursor.getColumnIndex("goalDes")));
                long sdate = cursor.getLong(cursor.getColumnIndex("startDate"));
                long edate = cursor.getLong(cursor.getColumnIndex("endDate"));
                long now = System.currentTimeMillis();
                if (now > edate) {
                    now = edate;
                }
                if (cursor.getInt(cursor.getColumnIndex("achieved")) == 1) {
                    item.put("achieved", "[ ACHIEVED ]");
                    now =  cursor.getLong(cursor.getColumnIndex("achievedDate"));
                }
                sdate /= 86400000;
                now /= 86400000;
                if (now - sdate < 0)
                    item.put("days", 0 + "");
                else
                    item.put("days", now - sdate + "");
                gridItem.add(item);
                cursor.moveToNext();
            }
        } else {
            showText.setVisibility(View.VISIBLE);
        }
        simpleAdapter = new SimpleAdapter(this,
                gridItem,
                R.layout.note_goal_item,
                new String[] {"des", "days", "achieved"},
                new int[] {R.id.note_goal_des, R.id.note_goal_days, R.id.note_goal_days_achieved});
        gridView.setAdapter(simpleAdapter);
        simpleAdapter.notifyDataSetChanged();
    }
}

