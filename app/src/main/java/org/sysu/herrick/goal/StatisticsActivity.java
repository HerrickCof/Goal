package org.sysu.herrick.goal;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.ArrayRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by herrick on 2016/12/20.
 */

public class StatisticsActivity extends AppCompatActivity {

    private ListView statList = null;
    private RelativeLayout back = null;
    private Bundle extra = null;
    private static final String TABLE_NAME = "Note_Data";
    private NoteDB noteDB = null;
    private List<Map<String, Object>> list_content = new ArrayList<>();

    private final String[] stat_clause_whole = new String[] {
    /*0*/   "STARTED COUNTING SINCE", // first goal's "date"
    /*1*/   "TOTAL GOALS",
    /*2*/   "TOTAL ACHIEVED GOALS",
    /*3*/   "AVERAGE STREAK",
    /*4*/   "LONGEST STREAK",
    /*5*/   "TOTAL RESET"
    };
    private final String[] stat_clause_which_goal = new String[] {
    /*0*/   "START DATE",
    /*1*/   "END DATE",
    /*2*/   "GOAL",
    /*3*/   "SET ON",
    /*4*/   "STATE",
    /*5*/   "STREAK",
    /*6*/   "RESET"
    };
    private static String[] month = new String[] {
            "JANUARY", "FEBRUARY", "MARCH",
            "APRIL", "MAY", "JUNE", "JULY",
            "AUGUST", "SEPTEMBER", "OCTOBER",
            "NOVEMBER", "DECEMBER"
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);

        extra = this.getIntent().getExtras();
        noteDB = NoteDB.getInstance(StatisticsActivity.this);
        statList = (ListView) findViewById(R.id.stat_list);
        back = (RelativeLayout) findViewById(R.id.stat_back_button);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent();
                in.setClassName(StatisticsActivity.this, "org.sysu.herrick.goal." + extra.getString("className"));
                if (extra.getString("type").equals("which_goal")) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("which_goal", extra.getInt("which_goal"));
                    in.putExtras(bundle);
                }
                startActivity(in);
                finish();
            }
        });
        fill_list();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClassName(StatisticsActivity.this, "org.sysu.herrick.goal."+ extra.getString("className"));
        if (extra.getString("type").equals("which_goal")) {
            Bundle bundle = new Bundle();
            bundle.putInt("which_goal", extra.getInt("which_goal"));
            intent.putExtras(bundle);
        }
        startActivity(intent);
        finish();
    }
    private void fill_list() {
        list_content.clear();
        if (extra.getString("type").equals("whole")) {
            Cursor c = noteDB.query("SELECT * from " + TABLE_NAME);
            Vector<Long> date_arr = new Vector<>();
            int total_goals = 0;
            int total_achieved_goals = 0;
            Vector<Integer> streak = new Vector<>();
            int total_reset = 0;

            if (c.getCount() != 0) {
                total_goals = c.getCount();
                c.moveToFirst();
                for (int i = 0; i < c.getCount(); i++, c.moveToNext()) {
                    date_arr.add(c.getLong(c.getColumnIndex("date")));
                    long sdate = c.getLong(c.getColumnIndex("startDate"));
                    long edate = c.getLong(c.getColumnIndex("endDate"));
                    long now = System.currentTimeMillis();
                    if (now > edate) {
                        now = edate;
                        total_achieved_goals++;
                    }
                    sdate /= 86400000;
                    now /= 86400000;
                    if (now - sdate < 0)
                        streak.add(0);
                    else
                        streak.add(new Integer((int) (now - sdate)));
                    total_reset += c.getInt(c.getColumnIndex("reset"));
                }
                if (!date_arr.isEmpty())
                    java.util.Collections.sort(date_arr, new Comparator<Long>() {
                        @Override
                        public int compare(Long aLong, Long t1) {
                            return (int) (aLong - t1);
                        }
                    });
                if (!streak.isEmpty())
                    java.util.Collections.sort(streak, new Comparator<Integer>() {
                        @Override
                        public int compare(Integer integer, Integer t1) {
                            return integer - t1;
                        }
                    });

                Calendar clause_1 = Calendar.getInstance();
                clause_1.setTimeInMillis(date_arr.firstElement());
                Map<String, Object> s1 = new LinkedHashMap<>();
                s1.put("name", stat_clause_whole[0]);
                s1.put("content", month[clause_1.get(Calendar.MONTH)] + " / "
                        + clause_1.get(Calendar.DAY_OF_MONTH) + " / " + clause_1.get(Calendar.YEAR));
                list_content.add(s1);

                Map<String, Object> s2 = new LinkedHashMap<>();
                s2.put("name", stat_clause_whole[1]);
                s2.put("content", total_goals + "");
                list_content.add(s2);

                Map<String, Object> s3 = new LinkedHashMap<>();
                s3.put("name", stat_clause_whole[2]);
                s3.put("content", total_achieved_goals + "");
                list_content.add(s3);

                int sum = 0;
                for (Integer a : streak)
                    sum += a;
                Map<String, Object> s4 = new LinkedHashMap<>();
                s4.put("name", stat_clause_whole[3]);
                s4.put("content", sum / total_goals + " DAYS");
                list_content.add(s4);

                Map<String, Object> s5 = new LinkedHashMap<>();
                s5.put("name", stat_clause_whole[4]);
                s5.put("content", streak.lastElement() + " DAYS");
                list_content.add(s5);

                Map<String, Object> s6 = new LinkedHashMap<>();
                s6.put("name", stat_clause_whole[5]);
                s6.put("content", total_reset + " TIMES");
                list_content.add(s6);
            } else {
                Map<String, Object> s0 = new LinkedHashMap<>();
                s0.put("name", "NOTHING TO SHOW");
                s0.put("content", "NO GOALS SET YET!");
                list_content.add(s0);
            }

        } else { // equals which_goal
            int which_goal = extra.getInt("which_goal");
            Cursor c = noteDB.query("SELECT * from " + TABLE_NAME + " WHERE _id=" + which_goal);
            if (c.getCount() != 0) {
                c.moveToFirst();
                long sdate = c.getLong(c.getColumnIndex("startDate"));
                long edate = c.getLong(c.getColumnIndex("endDate"));
                long now = System.currentTimeMillis();
                Calendar date = Calendar.getInstance();

                date.setTimeInMillis(sdate);
                Map<String, Object> s1 = new LinkedHashMap<>();
                s1.put("name", stat_clause_which_goal[0]);
                s1.put("content", date.get(Calendar.MONTH) + 1 + " / "
                        + date.get(Calendar.DAY_OF_MONTH) + " / " + date.get(Calendar.YEAR));
                list_content.add(s1);

                date.setTimeInMillis(edate);
                Map<String, Object> s2 = new LinkedHashMap<>();
                s2.put("name", stat_clause_which_goal[1]);
                s2.put("content", date.get(Calendar.MONTH) + 1 + " / "
                        + date.get(Calendar.DAY_OF_MONTH) + " / " + date.get(Calendar.YEAR));
                list_content.add(s2);

                Map<String, Object> s3 = new LinkedHashMap<>();
                s3.put("name", stat_clause_which_goal[2]);
                s3.put("content", c.getString(c.getColumnIndex("goalDes")));
                list_content.add(s3);

                date.setTimeInMillis(c.getLong(c.getColumnIndex("date")));
                Map<String, Object> s4 = new LinkedHashMap<>();
                s4.put("name", stat_clause_which_goal[3]);
                s4.put("content", date.get(Calendar.MONTH) + 1 + " / "
                        + date.get(Calendar.DAY_OF_MONTH) + " / " + date.get(Calendar.YEAR));
                list_content.add(s4);

                Map<String, Object> s5 = new LinkedHashMap<>();
                s5.put("name", stat_clause_which_goal[4]);
                if (c.getInt(c.getColumnIndex("achieved")) == 1) {
                    s5.put("content", "ACHIEVED");
                    list_content.add(s5);
                    Map<String, Object> s5_ = new LinkedHashMap<>();
                    s5_.put("name", "ACHIEVED DATE");
                    date.setTimeInMillis(c.getLong(c.getColumnIndex("achievedDate")));
                    s5_.put("content", date.get(Calendar.MONTH) + 1 + " / "
                            + date.get(Calendar.DAY_OF_MONTH) + " / " + date.get(Calendar.YEAR));
                    list_content.add(s5_);
                } else {
                    s5.put("content", "NOT ACHIEVED");
                    list_content.add(s5);
                }

                Map<String, Object> s6 = new LinkedHashMap<>();
                s6.put("name", stat_clause_which_goal[5]);
                if (now > edate) {
                    now = edate;
                }
                if (c.getInt(c.getColumnIndex("achieved")) == 1) {
                    now = c.getLong(c.getColumnIndex("achievedDate"));
                }
                sdate /= 86400000;
                now /= 86400000;
                if (now - sdate < 0)
                    s6.put("content", 0 + " DAYS");
                else
                    s6.put("content", now - sdate + " DAYS");
                list_content.add(s6);

                Map<String, Object> s7 = new LinkedHashMap<>();
                s7.put("name", stat_clause_which_goal[6]);
                s7.put("content", c.getInt(c.getColumnIndex("reset")) + " TIMES");
                list_content.add(s7);
            } else {
                Map<String, Object> s0 = new LinkedHashMap<>();
                s0.put("name", "NOTHING TO SHOW");
                s0.put("content", "STARNGE ERROR");
                list_content.add(s0);
            }

        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(StatisticsActivity.this, list_content,
                R.layout.stat_item, new String[] {"name", "content"}, new int[] {R.id.stat_item_name, R.id.stat_item_data});

        statList.setAdapter(simpleAdapter);
        simpleAdapter.notifyDataSetChanged();
    }
}
