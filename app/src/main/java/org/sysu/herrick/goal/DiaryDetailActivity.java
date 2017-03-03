package org.sysu.herrick.goal;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

/**
 * Created by herrick on 2016/12/20.
 */

public class DiaryDetailActivity extends AppCompatActivity {

    private int new_diary = -2000;
    private int which_diary;
    private static final String TABLE_NAME = "Diary_Data";

    private static String[] month = new String[] {
            "JANUARY", "FEBRUARY", "MARCH",
            "APRIL", "MAY", "JUNE", "JULY",
            "AUGUST", "SEPTEMBER", "OCTOBER",
            "NOVEMBER", "DECEMBER"
    };
    private static String[] weekday = new String[] {
            "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"
    };

    private DiaryDB diaryDB = DiaryDB.getInstance(DiaryDetailActivity.this);

    private TextView diary_date_title   = null;
    private TextView edit_stamp         = null;
    private TextView done               = null;
    private EditText content_edit       = null;
    private ImageView weather           = null;
    private ImageView date              = null;
    private ImageView weather_big       = null;
    private ImageView mail              = null;

    private String weather_selected = "Sunny";
    private Calendar date_selected;
    private int prime_value_of_date_selected = 0;// YYYYMMDD

    private boolean isDiaryModified = false;
    private String toAdd;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            if (data.getBoolean("isSuccess"))
                Toast.makeText(DiaryDetailActivity.this, "BACKUP FILE IS SENT TO " + toAdd, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(DiaryDetailActivity.this, "FAILED TO SEND BACKUP FILE TO " + toAdd, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary);

        diary_date_title = (TextView) findViewById(R.id.diary_date_display);
        edit_stamp = (TextView) findViewById(R.id.diary_editStamp);
        date = (ImageView) findViewById(R.id.diary_date);
        done = (TextView) findViewById(R.id.diary_done);
        content_edit = (EditText) findViewById(R.id.diary_editText);
        weather = (ImageView) findViewById(R.id.diary_weather);
        mail = (ImageView) findViewById(R.id.diary_mail);
        weather_big = (ImageView) findViewById(R.id.diary_weather_big);

        date_selected = Calendar.getInstance();
        prime_value_of_date_selected = date_selected.get(Calendar.YEAR) * 10000
                + (date_selected.get(Calendar.MONTH) + 1) * 100 + date_selected.get(Calendar.DAY_OF_MONTH);

        which_diary = this.getIntent().getExtras().getInt("which_diary");


        date.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(DiaryDetailActivity.this);
                    View view = View.inflate(DiaryDetailActivity.this, R.layout.date_time_dialog, null);
                    final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
                    builder.setView(view);

                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(System.currentTimeMillis());
                    datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
                    datePicker.setMaxDate(System.currentTimeMillis());

                    builder.setTitle("Pick a Date");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            date_selected.set(datePicker.getYear(), datePicker.getMonth(),
                                    datePicker.getDayOfMonth());
                            setDiaryTitle(date_selected);
                            prime_value_of_date_selected = date_selected.get(Calendar.YEAR) * 10000
                                    + (date_selected.get(Calendar.MONTH) + 1) * 100 + date_selected.get(Calendar.DAY_OF_MONTH);
                            Cursor c =
                                    diaryDB.query("SELECT * from " + TABLE_NAME + " WHERE datePrime=" + prime_value_of_date_selected);
                            if (c.getCount() != 0) {
                                c.moveToFirst();
                                fetchDiary(diaryDB.getID("datePrime", prime_value_of_date_selected + ""));
                            } else {
                                weather_big.setVisibility(View.GONE);
                                content_edit.setText("");
                            }
                            c.close();
                            dialog.cancel();
                        }
                    });

                    Dialog dialog = builder.create();
                    dialog.show();
                }
                return true;
            }
        });
        weather.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DiaryDetailActivity.this);
                    View view = View.inflate(DiaryDetailActivity.this, R.layout.pick_weather_dialog, null);
                    builder.setView(view);

                    final int[] wid = {0};

                    TableRow sunny = (TableRow) view.findViewById(R.id.weatherpicker_sunny);
                    final CheckBox sunny_check = (CheckBox) view.findViewById(R.id.weatherpicker_sunny_checkbox);
                    TableRow cloudy = (TableRow) view.findViewById(R.id.weatherpicker_cloudy);
                    final CheckBox cloudy_check = (CheckBox) view.findViewById(R.id.weatherpicker_cloudy_checkbox);
                    TableRow rainy = (TableRow) view.findViewById(R.id.weatherpicker_rainy);
                    final CheckBox rainy_check = (CheckBox) view.findViewById(R.id.weatherpicker_rainy_checkbox);

                    sunny.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sunny_check.setChecked(true);
                            cloudy_check.setChecked(false);
                            rainy_check.setChecked(false);
                            wid[0] = 1;
                        }
                    });
                    cloudy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sunny_check.setChecked(false);
                            cloudy_check.setChecked(true);
                            rainy_check.setChecked(false);
                            wid[0] = 2;
                        }
                    });
                    rainy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sunny_check.setChecked(false);
                            cloudy_check.setChecked(false);
                            rainy_check.setChecked(true);
                            wid[0] = 3;
                        }
                    });

                    builder.setTitle("Weather");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (wid[0]) {
                                case 1 :
                                    weather_selected = "Sunny";
                                    weather_big.setImageResource(R.drawable.ic_sunny_big);
                                    break;
                                case 2 :
                                    weather_selected = "Cloudy";
                                    weather_big.setImageResource(R.drawable.ic_cloudy_big);
                                    break;
                                case 3 :
                                    weather_selected = "Rainy";
                                    weather_big.setImageResource(R.drawable.ic_umbrella_big);
                                    break;
                                default:
                                    break;
                            }
                            weather_big.setVisibility(View.VISIBLE);
                            dialog.cancel();
                        }
                    });

                    Dialog dialog = builder.create();
                    dialog.show();
                }
                return true;
            }
        });
        content_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDiaryModified = true;
                date.setVisibility(View.VISIBLE);
                weather.setVisibility(View.VISIBLE);
                mail.setVisibility(View.GONE);
            }
        });
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDiaryModified) {
                    Cursor c =
                            diaryDB.query("SELECT _id from " + TABLE_NAME + " WHERE datePrime=" + prime_value_of_date_selected);
                    if (c.getCount() == 0) {
                        prime_value_of_date_selected = date_selected.get(Calendar.YEAR) * 10000
                                + (date_selected.get(Calendar.MONTH) + 1) * 100 + date_selected.get(Calendar.DAY_OF_MONTH);
                        diaryDB.insert(date_selected.getTimeInMillis(), prime_value_of_date_selected,
                                System.currentTimeMillis(), date_selected.get(Calendar.DAY_OF_WEEK),
                                content_edit.getText().toString(), weather_selected);
                        Toast.makeText(DiaryDetailActivity.this, "New Diary added", Toast.LENGTH_SHORT).show();
                        c.close();
                        Intent intent = new Intent();
                        intent.setClassName(DiaryDetailActivity.this, "org.sysu.herrick.goal.DiaryOverviewActivity");
                        startActivity(intent);
                        finish();
                    } else { //exists update
                        if (!content_edit.getText().toString().equals("")) {
                            int id = diaryDB.getID("datePrime", prime_value_of_date_selected + "");
                            ContentValues cv = new ContentValues();
                            cv.put("editTime", System.currentTimeMillis());
                            cv.put("diary_content", content_edit.getText().toString());
                            cv.put("weather", weather_selected);
                            diaryDB.update(id, cv);
                            Toast.makeText(DiaryDetailActivity.this, "Diary Updated", Toast.LENGTH_SHORT).show();
                            c.close();
                            Intent intent = new Intent();
                            intent.setClassName(DiaryDetailActivity.this, "org.sysu.herrick.goal.DiaryOverviewActivity");
                            startActivity(intent);
                            finish();
                        }
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setClassName(DiaryDetailActivity.this, "org.sysu.herrick.goal.DiaryOverviewActivity");
                    startActivity(intent);
                    finish();
                }

            }
        });
        mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SharedPreferences pref = getSharedPreferences("backup_mailbox", Context.MODE_APPEND);
                toAdd = pref.getString("mailbox", "");
                AlertDialog.Builder builder = new AlertDialog.Builder(DiaryDetailActivity.this);
                if (toAdd.equals("")) {
                    builder.setTitle("Set your email address.");
                    final View v = View.inflate(DiaryDetailActivity.this, R.layout.set_email_dialog, null);
                    builder.setView(v);
                    final Dialog dialog = builder.create();
                    v.findViewById(R.id.set_email_cancel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    v.findViewById(R.id.set_email_confirm).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            EditText et = (EditText) v.findViewById(R.id.email_edit);
                            if (MailUtils.isEmail(et.getText().toString())) {
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("mailbox", et.getText().toString());
                                editor.commit();
                                sendEmail();
                                Toast.makeText(DiaryDetailActivity.this, "EMAIL ADDRESS IS SAVED", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(DiaryDetailActivity.this, "WRONG EMAIL ADDRESS", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    dialog.show();
                } else {
                    builder.setTitle("Back up through e-mail?");
                    builder.setMessage("Your email address: " + toAdd);
                    builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendEmail();
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    Dialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        /*logic*/
        if (which_diary == new_diary) {
            //mode add diary
            setDiaryTitle(date_selected);
        } else {
            //mode view than edit
            Cursor cursor = diaryDB.query("SELECT * from " + TABLE_NAME + " WHERE _id=" + which_diary);
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                date_selected.setTimeInMillis(cursor.getLong(cursor.getColumnIndex("date")));
                prime_value_of_date_selected = date_selected.get(Calendar.YEAR) * 10000
                        + (date_selected.get(Calendar.MONTH) + 1) * 100 + date_selected.get(Calendar.DAY_OF_MONTH);
                weather_selected = cursor.getString(cursor.getColumnIndex("weather"));
            }
            cursor.close();
            date.setVisibility(View.GONE);
            weather.setVisibility(View.GONE);
            mail.setVisibility(View.VISIBLE);
            fetchDiary(which_diary);
        }
    }
    private void sendEmail() {
        SharedPreferences pref = getSharedPreferences("backup_mailbox", Context.MODE_APPEND);
        toAdd = pref.getString("mailbox", "");
        new Thread(new Runnable() {
            @Override
            public void run() {
                MailUtils mailman = new MailUtils(DiaryDetailActivity.this, which_diary);
                Message msg = new Message();
                Bundle data = new Bundle();
                try {
                    data.putBoolean("isSuccess", mailman.sendMail(toAdd));
                } catch (IOException e) {
                    //Toast.makeText(DiaryDetailActivity.this, "CRAZY STUFF HAPPENED", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (MessagingException e) {
                    //Toast.makeText(DiaryDetailActivity.this, "CRAZY STUFF HAPPENED", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                msg.setData(data);
                handler.sendMessage(msg);
            }
        }).start();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClassName(DiaryDetailActivity.this, "org.sysu.herrick.goal.DiaryOverviewActivity");
        startActivity(intent);
        finish();
    }

    private void fetchDiary(int which) {
        Cursor cursor = diaryDB.query("SELECT * from " + TABLE_NAME + " WHERE _id=" + which);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();

            /*prime value*/
            TextView prime = (TextView) findViewById(R.id.diary_prime);
            prime.setText(cursor.getInt(cursor.getColumnIndex("datePrime")) + "  " + which_diary + "  " + prime_value_of_date_selected);

            /*content*/
            content_edit.setText(cursor.getString(cursor.getColumnIndex("diary_content")));

            /*stamp*/
            Calendar stamp = Calendar.getInstance();
            stamp.setTimeInMillis(cursor.getLong(cursor.getColumnIndex("editTime")));
            setStamp(stamp);

            /*title*/
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(cursor.getLong(cursor.getColumnIndex("date")));
            setDiaryTitle(c);

            /*weather bg*/
            String w = cursor.getString(cursor.getColumnIndex("weather"));
            setWeatherBg(w);
        }
        cursor.close();
    }
    private void setWeatherBg(String w) {
        if (w.equals("Sunny"))
            weather_big.setImageResource(R.drawable.ic_sunny_big);
        else if (w.equals("Cloudy"))
            weather_big.setImageResource(R.drawable.ic_cloudy_big);
        else if (w.equals("Rainy"))
            weather_big.setImageResource(R.drawable.ic_umbrella_big);
        else
            return;
        weather_big.setVisibility(View.VISIBLE);
    }
    private void setStamp(Calendar stamp) {
        Date currentTime = new Date(stamp.getTimeInMillis());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String t = formatter.format(currentTime);
        edit_stamp.setText("Last edited : " + t);
        edit_stamp.setVisibility(View.VISIBLE);
    }
    private void setDiaryTitle(Calendar date) {
        diary_date_title.setText(weekday[date.get(Calendar.DAY_OF_WEEK) - 1] + " / " +
                month[date.get(Calendar.MONTH)] + " " + date.get(Calendar.DAY_OF_MONTH) + " / "
                + date.get(Calendar.YEAR));
    }
}
