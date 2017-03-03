package org.sysu.herrick.goal;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Herrick on 2017/1/3.
 */

public class CalendarActivity extends AppCompatActivity {

    private GridView calendar_grid      = null;
    private TextView month_year         = null;
    private ImageView prev              = null;
    private ImageView next              = null;
    private RelativeLayout back         = null;
    private TextView click_show_text    = null;
    private RelativeLayout setYear      = null;

    private ArrayList<HashMap<String, Object>> gridItem = null;
    private Calendar today = Calendar.getInstance();
    private Calendar date_display = Calendar.getInstance();

    private DiaryDB diaryDB;
    private static final String TABLE_NAME_DIARY = "Diary_Data";

    private NoteDB noteDB = NoteDB.getInstance(CalendarActivity.this);
    private static final String TABLE_NAME_GOAL = "Note_Data";

    private static String[] month = new String[] {
            "JANUARY", "FEBRUARY", "MARCH",
            "APRIL", "MAY", "JUNE", "JULY",
            "AUGUST", "SEPTEMBER", "OCTOBER",
            "NOVEMBER", "DECEMBER"
    };
    private int[] days_of_month = new int[] {
            31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };
    private int day_cal_last_selected = -1;

    private boolean mode = false;

    private SimpleAdapter simpleAdapter = null;
    private SimpleAdapter simpleAdapter_forWhich = null;

    private Drawable selected = null;
    private Drawable unselected = null;

    private Bundle extra = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        /*find views by ids*/
        calendar_grid   = (GridView) findViewById(R.id.calendar_grid);
        month_year      = (TextView) findViewById(R.id.calendar_date_display);
        prev            = (ImageView) findViewById(R.id.calendar_prev_button);
        next            = (ImageView) findViewById(R.id.calendar_next_button);
        back            = (RelativeLayout) findViewById(R.id.calendar_back_button);
        click_show_text = (TextView) findViewById(R.id.calendar_grid_click_text);
        setYear         = (RelativeLayout) findViewById(R.id.calendar_set_year);

        diaryDB = DiaryDB.getInstance(CalendarActivity.this);
        today.setTimeInMillis(System.currentTimeMillis());
        selected = CalendarActivity.this.getResources().getDrawable(R.mipmap.ic_calendar_selected, null);
        unselected = CalendarActivity.this.getResources().getDrawable(R.mipmap.ic_calendar_unselected, null);

        gridItem = new ArrayList<>();

        extra = this.getIntent().getExtras();
        if (extra.getString("type").equals("which_goal")) {
            mode = true;
            ((TableLayout) findViewById(R.id.calendar_info)).setVisibility(View.GONE);
            init_grid_for_which(today, -1);
        } else {
            mode = false;
            init_grid(today, -1);
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent();
                in.setClassName(CalendarActivity.this, "org.sysu.herrick.goal." + extra.getString("className"));
                if (extra.getString("type").equals("which_goal")) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("which_goal", extra.getInt("which_goal"));
                    in.putExtras(bundle);
                }
                startActivity(in);
                finish();
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevMonth(-1);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextMonth(-1);
            }
        });

        setYear.setOnClickListener(new View.OnClickListener() {

            private ArrayList<HashMap<String, Object>> setYear_gridItem = null;
            private GridView setYear_grid = null;
            private int year_dis = 0;
            private int year_selected = today.get(Calendar.YEAR);
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
                View v = View.inflate(CalendarActivity.this, R.layout.calendar_set_year_dialog, null);
                setYear_grid   = (GridView) v.findViewById(R.id.calendar_setYear_grid);
                RelativeLayout prev     = (RelativeLayout) v.findViewById(R.id.calendar_setYear_prev);
                RelativeLayout next     = (RelativeLayout) v.findViewById(R.id.calendar_setYear_next);
                setYear_gridItem = new ArrayList<>();
                fill_setYear_grid(today.get(Calendar.YEAR) - 4);
                prev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fill_setYear_grid(year_dis - 9);
                    }
                });
                next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fill_setYear_grid(year_dis + 9);
                    }
                });
                setYear_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        year_selected = year_dis + i;
                        Drawable a = CalendarActivity.this.getResources().getDrawable(R.color.shallow_grey_deeper, null);
                        Drawable b = CalendarActivity.this.getResources().getDrawable(R.color.colorWhite, null);
                        for (int j = 0; j < 9; j++) {
                            RelativeLayout v = (RelativeLayout) adapterView.getChildAt(j).findViewById(R.id.calendar_setYear_dialog_grid_item_bg);
                            if (j == i)
                                v.setBackground(a);
                            else
                                v.setBackground(b);
                        }
                    }
                });
                builder.setView(v);
                builder.setTitle("SELECT A YEAR");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Calendar y = Calendar.getInstance();
                        y.set(year_selected, today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
                        init_grid(y, -1);
                        click_show_text.setVisibility(View.GONE);
                        dialog.dismiss();
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
            }
            private void fill_setYear_grid(int year) {
                setYear_gridItem.clear();
                if (year < 1970)
                    year = 1970;
                else if (year > today.get(Calendar.YEAR) + 100)
                    year = today.get(Calendar.YEAR) + 100;
                year_dis = year;
                for (int i = year; i < year + 9; i++) {
                    HashMap<String, Object> item = new HashMap<>();
                    item.put("yearNum", i);
                    setYear_gridItem.add(item);
                }
                SimpleAdapter s = new SimpleAdapter(
                        CalendarActivity.this,
                        setYear_gridItem,
                        R.layout.calendar_set_year_dialog_grid_item,
                        new String[] {"yearNum"},
                        new int[] {R.id.calendar_setYear_dialog_grid_item_yearNum});
                setYear_grid.setAdapter(s);
                s.notifyDataSetChanged();
            }
        });

    }
    @Override
    public void onBackPressed() {
        Intent in = new Intent();
        in.setClassName(CalendarActivity.this, "org.sysu.herrick.goal."+ extra.getString("className"));
        if (extra.getString("type").equals("which_goal")) {
            Bundle bundle = new Bundle();
            bundle.putInt("which_goal", extra.getInt("which_goal"));
            in.putExtras(bundle);
        }
        startActivity(in);
        finish();
    }
    private void prevMonth(int dd) {
        day_cal_last_selected = -1;
        int year = date_display.get(Calendar.YEAR);
        int month = date_display.get(Calendar.MONTH);
        int day = date_display.get(Calendar.DAY_OF_MONTH);
        if (year == 1970 && month == 0)
            return;
        Calendar previous_month = Calendar.getInstance();
        previous_month.set(month - 1 >= 0 ? year : year - 1, month - 1 >= 0 ? month - 1 : 11, day);
        if (mode) {
            init_grid_for_which(previous_month, dd);
        } else {
            init_grid(previous_month, dd);
            click_show_text.setVisibility(View.GONE);
        }
    }
    private void nextMonth(int dd) {
        day_cal_last_selected = -1;
        int year = date_display.get(Calendar.YEAR);
        int month = date_display.get(Calendar.MONTH);
        int day = date_display.get(Calendar.DAY_OF_MONTH);
        if (year == today.get(Calendar.YEAR) && month == 11)
            return;
        Calendar next_month = Calendar.getInstance();
        next_month.set(month + 1 <= 11 ? year : year + 1, month + 1 <= 11 ? month + 1 : 0, day);
        if (mode) {
            init_grid_for_which(next_month, dd);
        } else {
            init_grid(next_month, dd);
            click_show_text.setVisibility(View.GONE);
        }
    }
    private boolean isLeapYear(int year) {
        if (year % 400 == 0)
            return true;
        if (year % 4 == 0 && year % 100 != 0)
            return true;
        return false;
    }
    private void preProcess(Calendar d) {
        /*pre process*/
        date_display.setTimeInMillis(d.getTimeInMillis());

        gridItem.clear();

        if (isLeapYear(d.get(Calendar.YEAR)))
            days_of_month[1] = 29;
        else
            days_of_month[1] = 28;

        month_year.setText(month[d.get(Calendar.MONTH)] + " " + d.get(Calendar.YEAR));
    }
    private int prime(Calendar tmp) {
        return tmp.get(Calendar.YEAR) * 10000 + (tmp.get(Calendar.MONTH) + 1) * 100 + tmp.get(Calendar.DAY_OF_MONTH);
    }
    private void init_grid_for_which(Calendar d, int dd) {
        preProcess(d);
        Cursor c = noteDB.query("SELECT * from " + TABLE_NAME_GOAL + " WHERE _id=" + extra.getInt("which_goal"));
        if (c.getCount() != 0) {
            c.moveToFirst();
            Calendar tmp = Calendar.getInstance();
            tmp.setTimeInMillis(c.getLong(c.getColumnIndex("startDate")));
            int startDatePrime = prime(tmp);
            tmp.clear();
            tmp.setTimeInMillis(c.getLong(c.getColumnIndex("endDate")));
            int endDatePrime = prime(tmp);
            tmp.clear();
            int achievedDatePrime = -1;
            if (c.getInt(c.getColumnIndex("achieved")) != -1 ) {// achieved
                tmp.setTimeInMillis(c.getLong(c.getColumnIndex("achievedDate")));
                achievedDatePrime = prime(tmp);
            }
            /*fill 42 grids*/
            int offset = 7 - (d.get(Calendar.DAY_OF_MONTH) + 7 - d.get(Calendar.DAY_OF_WEEK)) % 7;
            if (offset == 7)
                offset = 0;

            if (dd == -1) //use day_of_month in d
                day_cal_last_selected = d.get(Calendar.DAY_OF_MONTH) + offset - 1;
            else
                day_cal_last_selected = dd + offset - 1;

            for (int i = 0 ; i < offset; i++) {
                HashMap<String, Object> item = new HashMap<>();
                int y_ = d.get(Calendar.YEAR);
                int d_ = 0;
                int prevmonth = d.get(Calendar.MONTH) - 1;
                if (prevmonth < 0) {
                    prevmonth = 11;
                    y_ = d.get(Calendar.YEAR) - 1;
                }
                d_ = days_of_month[prevmonth] - offset + i + 1;
                item.put("daynum", d_ + "");
                item.put("numColor", false);
                int dddPrime = y_ * 10000 + (prevmonth + 1) * 100 + d_;
                if (achievedDatePrime != -1 && achievedDatePrime == dddPrime) {
                    item.put("check", true);
                    item.put("cross", false);
                    item.put("circle", false);
                } else {
                    item.put("check", false);
                    if (dddPrime >= startDatePrime && dddPrime <= endDatePrime && dddPrime <= prime(today))
                        item.put("cross", true);
                    else
                        item.put("cross", false);
                    if (dddPrime == endDatePrime)
                        item.put("circle", true);
                    else
                        item.put("circle", false);
                }

                gridItem.add(item);
            }
            for (int i = 0; i < days_of_month[d.get(Calendar.MONTH)]; i++) {
                HashMap<String, Object> item = new HashMap<>();
                item.put("daynum", i + 1 + "");
                item.put("numColor", true);
                int y_ = d.get(Calendar.YEAR);
                int d_ = i + 1;
                int m_ = d.get(Calendar.MONTH);
                int dddPrime = y_ * 10000 + (m_ + 1)* 100 + d_;
                if (achievedDatePrime != -1 && achievedDatePrime == dddPrime) {
                    item.put("check", true);
                    item.put("cross", false);
                    item.put("circle", false);
                } else {
                    item.put("check", false);
                    if (dddPrime >= startDatePrime && dddPrime <= endDatePrime && dddPrime <= prime(today))
                        item.put("cross", true);
                    else
                        item.put("cross", false);
                    if (dddPrime == endDatePrime)
                        item.put("circle", true);
                    else
                        item.put("circle", false);
                }
                gridItem.add(item);
            }
            int offset_ = 42 - days_of_month[d.get(Calendar.MONTH)] - offset;
            for (int i = 0; i < offset_; i++) {
                HashMap<String, Object> item = new HashMap<>();
                item.put("daynum", i + 1 + "");
                item.put("numColor", false);
                int y_ = d.get(Calendar.YEAR);
                int d_ = i + 1;
                int m_ = d.get(Calendar.MONTH) + 2;
                if (m_ == 13) {
                    m_ = 1;
                    y_++;
                }
                int dddPrime = y_ * 10000 + m_ * 100 + d_;
                if (achievedDatePrime != -1 && achievedDatePrime == dddPrime) {
                    item.put("check", true);
                    item.put("cross", false);
                    item.put("circle", false);
                } else {
                    item.put("check", false);
                    if (dddPrime >= startDatePrime && dddPrime <= endDatePrime && dddPrime <= prime(today))
                        item.put("cross", true);
                    else
                        item.put("cross", false);
                    if (dddPrime == endDatePrime)
                        item.put("circle", true);
                    else
                        item.put("circle", false);
                }
                gridItem.add(item);
            }
            simpleAdapter_forWhich = new SimpleAdapter(
                    CalendarActivity.this,
                    gridItem,
                    R.layout.calendar_item,
                    new String[]{"daynum","numColor","check", "cross", "circle"},
                    new int[]{R.id.calendar_item_daynum, R.id.calendar_item_daynum, R.id.calendar_check_mark, R.id.calendar_cross_mark, R.id.calendar_circle_mark});
            simpleAdapter_forWhich.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object o, String s) {
                    if (view instanceof TextView && o instanceof String) {
                        TextView v = (TextView) view;
                        v.setText(o.toString());
                    } else if (view instanceof TextView && o instanceof Boolean) {
                        TextView v = (TextView) view;
                        if ((Boolean) o)
                            v.setTextColor(getResources().getColor(R.color.color_font, null));
                        else
                            v.setTextColor(getResources().getColor(R.color.color_base, null));
                        return true;
                    } else if (view instanceof ImageView && o instanceof Boolean) {
                        ImageView v = (ImageView) view;
                        if ((Boolean) o)
                            v.setVisibility(View.VISIBLE);
                        return true;
                    }
                    return false;
                }
            });
            calendar_grid.setAdapter(simpleAdapter_forWhich);
            simpleAdapter_forWhich.notifyDataSetChanged();
            calendar_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    TextView textView = (TextView) view.findViewById(R.id.calendar_item_daynum);
                    ImageView bg = (ImageView) view.findViewById(R.id.calendar_item_bg);
                    int day = Integer.parseInt(textView.getText().toString());
                    if (day <= i + 1 && i + 1 - day < 7) { //which means day is in this month
                        if (day_cal_last_selected != i) {
                            bg.setImageResource(R.mipmap.ic_calendar_selected);
                            ImageView v = (ImageView) adapterView.getChildAt(day_cal_last_selected).findViewById(R.id.calendar_item_bg);
                            v.setImageResource(R.mipmap.ic_calendar_unselected);
                            day_cal_last_selected = i;
                        }
                    } else if (day > i + 1) { //which means day is in last month
                        prevMonth(day);
                    } else if (i + 1 - day >= 7) { //which means day is in next month
                        nextMonth(day);
                    }
                }
            });

        }
    }
    private void init_grid(Calendar d, int dd) {
        preProcess(d);

        /*query diary this month*/
        int this_month_primeVal = d.get(Calendar.YEAR) * 10000 + (d.get(Calendar.MONTH) + 1) * 100;
        int next_month_primeVal = this_month_primeVal + 100;
        /*if (next_month_primeVal % 10000 == 1300)
            next_month_primeVal = next_month_primeVal + 10000 - 1200;*/
        Cursor c1 = diaryDB.query("SELECT datePrime from " + TABLE_NAME_DIARY + " WHERE datePrime BETWEEN " + this_month_primeVal + " AND " + next_month_primeVal);
        Vector<Integer> diary = new Vector<>();
        if (c1.getCount() != 0) {
            c1.moveToFirst();
            for (int i = 0; i < c1.getCount(); i++, c1.moveToNext()) {
                diary.add(c1.getInt(c1.getColumnIndex("datePrime")) % 100 );
            }
        }

        /*query goal this month*/
        Calendar tmp = Calendar.getInstance();
        tmp.set(d.get(Calendar.YEAR), d.get(Calendar.MONTH), 1);
        long head = tmp.getTimeInMillis() - 86400000;
        tmp.clear();
        tmp.set(d.get(Calendar.MONTH) + 1 > 11 ? d.get(Calendar.YEAR) + 1 : d.get(Calendar.YEAR), d.get(Calendar.MONTH) + 1 > 11 ? 0 : d.get(Calendar.MONTH) + 1, 1);
        long rear = tmp.getTimeInMillis();
        Cursor c2 = noteDB.query("SELECT endDate, goalDes from " + TABLE_NAME_GOAL + " WHERE endDate BETWEEN " + head + " AND " + rear);
        final Vector<Integer> goal = new Vector<>();
        final Vector<String> goalDes = new Vector<>();
        if (c2.getCount() != 0) {
            c2.moveToFirst();
            for (int i = 0; i < c2.getCount(); i++, c2.moveToNext()) {
                tmp.clear();
                tmp.setTimeInMillis(c2.getLong(c2.getColumnIndex("endDate")));
                goal.add(tmp.get(Calendar.DAY_OF_MONTH));
                goalDes.add(c2.getString(c2.getColumnIndex("goalDes")));
            }
        }


        /*fill 42 grids, day of month*/
        int offset = 7 - (d.get(Calendar.DAY_OF_MONTH) + 7 - d.get(Calendar.DAY_OF_WEEK)) % 7;
        if (offset == 7)
            offset = 0;

        if (dd == -1) //use day_of_month in d
            day_cal_last_selected = d.get(Calendar.DAY_OF_MONTH) + offset - 1;
        else
            day_cal_last_selected = dd + offset - 1;

        for (int i = 0 ; i < offset; i++) {
            HashMap<String, Object> item = new HashMap<>();
            int prevmonth = d.get(Calendar.MONTH) - 1;
            if (prevmonth < 0)
                prevmonth = 11;
            item.put("daynum", days_of_month[prevmonth] - offset + i + 1 + "");
            item.put("numcolor", false);
            item.put("diary", false);
            item.put("goal", "false");
            item.put("highlight", unselected);
            gridItem.add(item);
        }
        for (int i = 0; i < days_of_month[d.get(Calendar.MONTH)]; i++) {
            HashMap<String, Object> item = new HashMap<>();
            item.put("daynum", i + 1 + "");
            item.put("numcolor", true);
            if (diary.indexOf(i + 1) != -1)
                item.put("diary", true);
            else
                item.put("diary", false);
            if (goal.indexOf(i + 1) != -1)
                item.put("goal", "true");
            else
                item.put("goal", "false");
            if (day_cal_last_selected - offset == i)
                item.put("highlight", selected);
            else
                item.put("highlight", unselected);
            gridItem.add(item);
        }
        int offset_ = 42 - days_of_month[d.get(Calendar.MONTH)] - offset;
        for (int i = 0; i < offset_; i++) {
            HashMap<String, Object> item = new HashMap<>();
            item.put("daynum", i + 1 + "");
            item.put("numcolor", false);
            item.put("diary", false);
            item.put("goal", "false");
            item.put("highlight", unselected);
            gridItem.add(item);
        }
        simpleAdapter = new SimpleAdapter(
                CalendarActivity.this,
                gridItem,
                R.layout.calendar_item,
                new String[] {"daynum", "numcolor", "diary", "goal", "highlight"},
                new int[] {R.id.calendar_item_daynum, R.id.calendar_item_daynum, R.id.calendar_diary_mark, R.id.calendar_endDate_mark, R.id.calendar_item_bg});
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof TextView && data instanceof Boolean) {
                    TextView v = (TextView) view;
                    if ((Boolean) data)
                        v.setTextColor(getResources().getColor(R.color.color_font, null));
                    else
                        v.setTextColor(getResources().getColor(R.color.color_base, null));
                    return true;
                } else if (view instanceof ImageView && data instanceof Boolean) {
                    ImageView v = (ImageView) view;
                    if ((Boolean)data)
                        v.setVisibility(View.VISIBLE);
                    return true;
                } else if (view instanceof ImageView && data instanceof String) {
                    ImageView v = (ImageView) view;
                    if (data.equals("true"))
                        v.setVisibility(View.VISIBLE);
                    return true;
                } else if (view instanceof ImageView && data instanceof Drawable) {
                    ImageView v = (ImageView) view;
                    v.setImageDrawable((Drawable)data);
                    return true;
                }
                return false;
            }
        });
        calendar_grid.setAdapter(simpleAdapter);
        simpleAdapter.notifyDataSetChanged();
        calendar_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView textView = (TextView) view.findViewById(R.id.calendar_item_daynum);
                ImageView bg = (ImageView) view.findViewById(R.id.calendar_item_bg);
                int day = Integer.parseInt(textView.getText().toString());
                int index = goal.indexOf(day);
                int lines = 0;
                String content = "Your Goal(s):\n";
                lines++;
                if (index != -1) {
                    click_show_text.setVisibility(View.VISIBLE);
                    content += " @ " + goalDes.get(index);
                    lines++;
                    index = goal.indexOf(day, index + 1);
                    while (index != -1) {
                        if (goalDes.get(index).length() > 9)
                            content += "\n @ " + goalDes.get(index).substring(0, 9) + "...";
                        else
                            content += "\n @ " + goalDes.get(index);
                        lines++;
                        index = goal.indexOf(day, index + 1);
                        if (lines > 3) {
                            content += "\n                         and more...";
                            break;
                        }
                    }
                    click_show_text.setText(content);
                } else {
                    click_show_text.setVisibility(View.GONE);
                    click_show_text.setText("");
                }
                if (day <= i + 1 && i + 1 - day < 7) { //which means day is in this month
                    if (day_cal_last_selected != i) {
                        bg.setImageResource(R.mipmap.ic_calendar_selected);
                        ImageView v = (ImageView) adapterView.getChildAt(day_cal_last_selected).findViewById(R.id.calendar_item_bg);
                        v.setImageResource(R.mipmap.ic_calendar_unselected);
                        day_cal_last_selected = i;
                    }
                } else if (day > i + 1) { //which means day is in last month
                    prevMonth(day);
                } else if (i + 1 - day >= 7) { //which means day is in next month
                    nextMonth(day);
                }
            }
        });
    }
}
