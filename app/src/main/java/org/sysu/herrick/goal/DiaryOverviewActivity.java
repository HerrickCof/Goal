package org.sysu.herrick.goal;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by herrick on 2016/12/20.
 */

public class DiaryOverviewActivity extends AppCompatActivity {

    private static final String TABLE_NAME = "Diary_Data";
    private DiaryDB diaryDB = null;
    private ListView diary_list = null;
    private TextView diary_list_date = null;
    private ImageView add = null;
    private TextView back = null;
    private SwipeRefreshLayout refreshLayout = null;

    private Cursor cursor = null;
    private int i = 0;

    private int new_diary = -2000;

    private List<Map<String, Object>> list_content = new ArrayList<>();
    private static String[] month = new String[] {
            "JANUARY", "FEBRUARY", "MARCH",
            "APRIL", "MAY", "JUNE", "JULY",
            "AUGUST", "SEPTEMBER", "OCTOBER",
            "NOVEMBER", "DECEMBER"
    };
    private static String[] weekday = new String[] {
            "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"
    };
    private int load_num = 8;

    private SimpleAdapter simpleAdapter = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_overview);

        diary_list = (ListView) findViewById(R.id.diary_overview_list);
        diary_list_date = (TextView) findViewById(R.id.diary_overview_date);
        add = (ImageView) findViewById(R.id.diary_add);
        back = (TextView) findViewById(R.id.diary_overview_back);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.diary_overview_swipe);

        //refreshLayout = (SwipeRefreshLayout) findViewById(R.id.diary_swiperefresh);
        simpleAdapter = new SimpleAdapter(DiaryOverviewActivity.this, list_content,
                R.layout.diary_item, new String[] {"weekday", "content", "dayofmonth", "prime"},
                new int[] {R.id.diary_item_weekday, R.id.diary_item_diaryFrag,
                        R.id.diary_item_dayofmonth, R.id.diary_item_primeValue});
        diary_list.setAdapter(simpleAdapter);
        diaryDB = DiaryDB.getInstance(DiaryOverviewActivity.this);

        cursor = diaryDB.query("SELECT * from " + TABLE_NAME + " ORDER BY datePrime ASC");
        if (cursor.getCount() != 0) {
            cursor.moveToLast();
        }



        Calendar calendar = Calendar.getInstance();
        diary_list_date.setText("[ " + month[calendar.get(Calendar.MONTH)] + " | "
                + calendar.get(Calendar.DAY_OF_MONTH) + " | " + calendar.get(Calendar.YEAR) + " ]");
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent();
                Bundle bundle = new Bundle();
                bundle.putInt("which_diary", new_diary);
                in.putExtras(bundle);
                in.setClassName(DiaryOverviewActivity.this, "org.sysu.herrick.goal.DiaryDetailActivity");
                startActivity(in);
                finish();
            }
        });
        back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intent = new Intent();
                intent.setClassName(DiaryOverviewActivity.this, "org.sysu.herrick.goal.NoteActivity");
                startActivity(intent);
                finish();
                return true;
            }
        });
        refreshLayout.setColorSchemeColors(R.color.color_steel_blue);
        refreshLayout.setProgressViewOffset(false, -200, -200);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load_num += 6;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        fill_list();
                        //refreshLayout.setRefreshing(false);
                    }
                }).start();
            }
        });


        fill_list();
        diary_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int ppp = (int) list_content.get(position).get("prime");
                //TextView p = (TextView) findViewById(R.id.diary_item_primeValue);
                Intent in = new Intent();
                Bundle bundle = new Bundle();
                //bundle.putInt("which_diary", diaryDB.getID("datePrime", p.getText().toString()));
                bundle.putInt("which_diary", diaryDB.getID("datePrime", ppp + ""));
                //Toast.makeText(DiaryOverviewActivity.this, "primvalue "+  ppp + "\nbundle extras " + diaryDB.getID("datePrime", ppp + ""), Toast.LENGTH_SHORT).show();
                in.putExtras(bundle);
                in.setClassName(DiaryOverviewActivity.this, "org.sysu.herrick.goal.DiaryDetailActivity");
                startActivity(in);
                finish();
            }
        });
        diary_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DiaryOverviewActivity.this);
                TextView t = (TextView) view.findViewById(R.id.diary_item_diaryFrag);
                final TextView prime = (TextView) view.findViewById(R.id.diary_item_primeValue);
                String tt = t.getText().toString();
                if (tt.length() > 12)
                    tt = tt.substring(0, 11) + "...";
                builder.setTitle("Are you sure about this?");
                builder.setMessage("Delete this Diary?\n    \"  " + tt + "  \"");
                builder.setPositiveButton("Yes, I'm sure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        diaryDB.delete(diaryDB.getID("datePrime", prime.getText().toString()));
                        list_content.remove(position);
                        simpleAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("No, I'll keep it    ", new DialogInterface.OnClickListener() {
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
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClassName(DiaryOverviewActivity.this, "org.sysu.herrick.goal.NoteActivity");
        startActivity(intent);
        finish();
    }
    @Override
    public void onPause() {
        super.onPause();
        cursor.close();
    }
    private void fill_list() {

        if (cursor.getCount() != 0) {
            for (; i < cursor.getCount() && i < load_num; i++, cursor.moveToPrevious()) {
                Map<String, Object> s = new LinkedHashMap<>();
                s.put("weekday", weekday[cursor.getInt(cursor.getColumnIndex("weekday")) - 1]);
                s.put("content", cursor.getString(cursor.getColumnIndex("diary_content")));
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(cursor.getLong(cursor.getColumnIndex("date")));
                s.put("dayofmonth", c.get(Calendar.DAY_OF_MONTH));
                s.put("prime", cursor.getInt(cursor.getColumnIndex("datePrime")));
                list_content.add(s);
            }
        }
        Collections.sort(list_content, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> stringObjectMap, Map<String, Object> t1) {
                return Integer.parseInt(stringObjectMap.get("prime").toString())
                        - Integer.parseInt(t1.get("prime").toString());
            }
        });
        simpleAdapter.notifyDataSetChanged();
    }

}
;