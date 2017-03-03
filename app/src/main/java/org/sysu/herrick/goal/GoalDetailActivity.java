package org.sysu.herrick.goal;

import android.animation.ArgbEvaluator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.BoolRes;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseRequest;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.openapi.ShareWeiboApi;

import java.util.Calendar;
import java.util.Objects;

import static android.view.View.GONE;

/**
 * Created by herrick on 2016/12/19.
 */

public class GoalDetailActivity extends AppCompatActivity implements IWeiboHandler.Response {

    /*database constant*/
    private static final String DB_NAME = "Note_Goal";
    private static final String TABLE_NAME = "Note_Data";
    private static final int DB_VERSION = 1;

    private int which_goal;
    private NoteDB noteDB = null;
    private int new_goal = -1000;
    private long edit_startDate = 0;
    private long edit_endDate = 0;
    private boolean saveToInsert_1 = false;
    private boolean saveToInsert_2 = false;
    private boolean op_menu_toggle = false;

    private TextView text_Goal = null;
    private TextView detail_goal_des = null;
    private TextView detail_goal_days = null;
    private TextView detail_goal_achieved = null;
    private RelativeLayout detail_op_delete = null;
    private RelativeLayout detail_op_edit = null;
    private RelativeLayout detail_op_statistics = null;
    private RelativeLayout detail_op_share = null;
    private RelativeLayout detail_op_calendar = null;
    private RelativeLayout detail_op_achieved = null;
    private RelativeLayout detail_op = null;
    private RelativeLayout detail_bg = null;
    private LinearLayout detail_reset = null;
    private RelativeLayout detail_edit = null;
    private RelativeLayout detail_op_menu = null;
    private Button goal_edit_cancel = null;
    private Button goal_edit_confirm = null;
    private EditText goal_edit_startDate = null;
    private EditText goal_edit_endDate = null;
    private EditText goal_edit_goal = null;


    /*weibo share*/
    private AuthInfo mAuthInfo;
    private Oauth2AccessToken mAccessToken;
    private SsoHandler mSsoHandler;
    private IWeiboShareAPI mWeiboShareAPI;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goaldetail);

        /*find views by ids*/
        text_Goal = (TextView) findViewById(R.id.text_goal);
        detail_goal_days = (TextView) findViewById(R.id.detail_goal_days);
        detail_goal_des = (TextView) findViewById(R.id.detail_goal_des);
        detail_goal_achieved = (TextView) findViewById(R.id.detail_goal_days_achieved);
        detail_op_delete = (RelativeLayout) findViewById(R.id.detail_op_1);
        detail_op_edit = (RelativeLayout) findViewById(R.id.detail_op_2);
        detail_op_statistics = (RelativeLayout) findViewById(R.id.detail_op_3);
        detail_op_calendar = (RelativeLayout) findViewById(R.id.detail_op_4);
        detail_op_share = (RelativeLayout) findViewById(R.id.detail_op_5);
        detail_op_achieved = (RelativeLayout) findViewById(R.id.detail_op_6);
        detail_op = (RelativeLayout) findViewById(R.id.detail_op);
        detail_reset = (LinearLayout) findViewById(R.id.detail_reset);
        detail_edit = (RelativeLayout) findViewById(R.id.detail_edit);
        detail_op_menu = (RelativeLayout) findViewById(R.id.detail_op_menu);
        detail_bg = (RelativeLayout) findViewById(R.id.goal_detail_bg);
        goal_edit_cancel = (Button) findViewById(R.id.goal_edit_cancel);
        goal_edit_confirm = (Button) findViewById(R.id.goal_edit_confirm);
        goal_edit_startDate = (EditText) findViewById(R.id.goal_edit_startDate);
        goal_edit_endDate = (EditText) findViewById(R.id.goal_edit_endDate);
        goal_edit_goal = (EditText) findViewById(R.id.goal_edit_goal);

        noteDB = NoteDB.getInstance(GoalDetailActivity.this);
        SharedPreferences pref = getSharedPreferences("which_goal", Context.MODE_APPEND);
        which_goal = pref.getInt("which_goal", new_goal);
        if (which_goal == new_goal) {
            detail_goal_des.setText("设定一个目标");
            detail_goal_days.setText("0");
            detail_edit.setVisibility(View.VISIBLE);
            detail_reset.setVisibility(GONE);
            detail_op.setVisibility(GONE);
        } else {
            initGoal();
            detail_edit.setVisibility(GONE);
            detail_reset.setVisibility(View.VISIBLE);
            detail_op.setVisibility(View.VISIBLE);
        }
        /*weibo share api*/
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(GoalDetailActivity.this, WeiboConstants.APP_KEY);
        mWeiboShareAPI.registerApp();
        /*if (savedInstanceState != null) {
            mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
        }*/

        /*set Listeners*/
        text_Goal.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intent = new Intent();
                intent.setClassName(GoalDetailActivity.this, "org.sysu.herrick.goal.NoteActivity");
                startActivity(intent);
                finish();
                return true;
            }
        });
        goal_edit_startDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(GoalDetailActivity.this);
                    View view = View.inflate(GoalDetailActivity.this, R.layout.date_time_dialog, null);
                    final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
                    builder.setView(view);

                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(System.currentTimeMillis());
                    datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
                    if (edit_endDate != 0)
                        datePicker.setMaxDate(edit_endDate);


                    final int inType = goal_edit_startDate.getInputType();
                    goal_edit_startDate.setInputType(InputType.TYPE_NULL);
                    goal_edit_startDate.onTouchEvent(event);
                    goal_edit_startDate.setInputType(inType);
                    goal_edit_startDate.setSelection(goal_edit_startDate.getText().length());

                    builder.setTitle("Pick the Start Date");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            StringBuffer sb = new StringBuffer();
                            sb.append(String.format("%d-%02d-%02d",
                                    datePicker.getYear(),
                                    datePicker.getMonth() + 1,
                                    datePicker.getDayOfMonth()));
                            sb.append("  ");
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(datePicker.getYear(),
                                    datePicker.getMonth(),
                                    datePicker.getDayOfMonth(), 12, 0, 0);
                            edit_startDate = calendar.getTimeInMillis();
                            saveToInsert_1 = true;
                            goal_edit_startDate.setText(sb);
                            goal_edit_endDate.requestFocus();

                            dialog.cancel();
                        }
                    });

                    Dialog dialog = builder.create();
                    dialog.show();
                }
                return true;
            }
        });
        goal_edit_endDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(GoalDetailActivity.this);
                    View view = View.inflate(GoalDetailActivity.this, R.layout.date_time_dialog, null);
                    final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
                    builder.setView(view);

                    final Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(System.currentTimeMillis());
                    datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
                    if (edit_startDate != 0)
                        datePicker.setMinDate(edit_startDate);


                    final int inType = goal_edit_endDate.getInputType();
                    goal_edit_endDate.setInputType(InputType.TYPE_NULL);
                    goal_edit_endDate.onTouchEvent(event);
                    goal_edit_endDate.setInputType(inType);
                    goal_edit_endDate.setSelection(goal_edit_endDate.getText().length());

                    builder.setTitle("Pick the Date you expect to accomplish your Goal");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            StringBuffer sb = new StringBuffer();
                            sb.append(String.format("%d-%02d-%02d",
                                    datePicker.getYear(),
                                    datePicker.getMonth() + 1,
                                    datePicker.getDayOfMonth()));
                            sb.append("  ");
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(datePicker.getYear(),
                                    datePicker.getMonth(),
                                    datePicker.getDayOfMonth(), 12, 0, 0);
                            edit_endDate = calendar.getTimeInMillis();
                            saveToInsert_2 = true;
                            goal_edit_endDate.setText(sb);
                            goal_edit_goal.requestFocus();

                            dialog.cancel();
                        }
                    });

                    Dialog dialog = builder.create();
                    dialog.show();
                }
                return true;
            }
        });
        goal_edit_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (which_goal == new_goal) {
                    Intent in = new Intent();
                    in.setClassName(GoalDetailActivity.this, "org.sysu.herrick.goal.NoteActivity");
                    startActivity(in);
                    finish();
                } else {
                    detail_edit.setVisibility(GONE);
                    detail_reset.setVisibility(View.VISIBLE);
                    detail_op.setVisibility(View.VISIBLE);
                }
            }
        });
        goal_edit_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteDB = NoteDB.getInstance(GoalDetailActivity.this);
                if (which_goal == new_goal) {
                    if (saveToInsert_1 && saveToInsert_2 &&
                            !goal_edit_goal.getText().toString().equals("")) {
                        if (edit_startDate >= edit_endDate)
                            Toast.makeText(GoalDetailActivity.this, "Seriously?", Toast.LENGTH_SHORT).show();
                        else {
                            noteDB.insert(System.currentTimeMillis(), edit_startDate, edit_endDate, goal_edit_goal.getText().toString());
                            Toast.makeText(GoalDetailActivity.this, "New Goal Added!", Toast.LENGTH_SHORT).show();
                            saveToInsert_1 = false;
                            saveToInsert_2 = false;
                            Intent in = new Intent();
                            in.setClassName(GoalDetailActivity.this, "org.sysu.herrick.goal.NoteActivity");
                            startActivity(in);
                            finish();
                        }
                    } else {
                        Toast.makeText(GoalDetailActivity.this, "Please Complete the Settings First", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (saveToInsert_1 && saveToInsert_2) {
                        if (edit_startDate >= edit_endDate)
                            Toast.makeText(GoalDetailActivity.this, "Seriously?", Toast.LENGTH_SHORT).show();
                        else {
                            if (!goal_edit_goal.getText().toString().equals(""))
                                noteDB.update(which_goal, edit_startDate, edit_endDate, goal_edit_goal.getText().toString(), -1);
                            else
                                noteDB.update(which_goal, edit_startDate, edit_endDate, goal_edit_goal.getHint().toString(), -1);
                            initGoal();
                            saveToInsert_1 = false;
                            saveToInsert_2 = false;
                            Toast.makeText(GoalDetailActivity.this, "Goal Updated", Toast.LENGTH_SHORT).show();
                            detail_edit.setVisibility(GONE);
                            detail_reset.setVisibility(View.VISIBLE);
                            detail_op.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(GoalDetailActivity.this, "Please Complete the Settings First", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        detail_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GoalDetailActivity.this);
                builder.setTitle("OOOOPS!!!");
                builder.setMessage("Reset the goal?\n     \"  " + detail_goal_des.getText().toString() + "  \"");
                builder.setPositiveButton("  Yes, I would try \n  harder this time", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long end = 0;
                        int reset = 0;
                        Cursor cursor = noteDB.query("SELECT * from " + TABLE_NAME + " WHERE _id=" + which_goal);
                        if (cursor.getCount() != 0) {
                            cursor.moveToFirst();
                            end = cursor.getLong(cursor.getColumnIndex("endDate"));
                            reset = cursor.getInt(cursor.getColumnIndex("reset"));
                        }
                        if (end == 0)
                            Toast.makeText(GoalDetailActivity.this, "Fail to reset", Toast.LENGTH_SHORT).show();
                        else
                            noteDB.update(which_goal, System.currentTimeMillis(), end, detail_goal_des.getText().toString(), ++reset);
                        cursor.close();
                        initGoal();
                    }
                });
                builder.setNegativeButton("No, it's an \naccident", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
            }
        });
        detail_op.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (op_menu_toggle) {
                    detail_op_menu.setVisibility(GONE);
                    op_menu_toggle = false;
                } else {
                    detail_op_menu.setVisibility(View.VISIBLE);
                    op_menu_toggle = true;
                }
            }
        });
        detail_bg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                detail_op_menu.setVisibility(GONE);
                op_menu_toggle = false;
                return true;
            }
        });
        detail_op_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detail_op_menu.setVisibility(GONE);
                op_menu_toggle = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(GoalDetailActivity.this);
                builder.setTitle("Are you sure about this?");
                builder.setMessage("Delete this goal?\n    \"  " + detail_goal_des.getText().toString() + "  \"");
                builder.setPositiveButton("Yes, I failed it", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        noteDB.delete(noteDB.getID("goalDes", "'" + detail_goal_des.getText().toString() + "'"));
                        Intent in = new Intent();
                        in.setClassName(GoalDetailActivity.this, "org.sysu.herrick.goal.NoteActivity");
                        startActivity(in);
                        finish();
                    }
                });
                builder.setNegativeButton("No, I will keep it      ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
            }
        });
        detail_op_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detail_op_menu.setVisibility(GONE);
                op_menu_toggle = false;
                detail_op.setVisibility(GONE);
                detail_reset.setVisibility(GONE);
                detail_edit.setVisibility(View.VISIBLE);
                goal_edit_goal.setHint(detail_goal_des.getText().toString());
                goal_edit_startDate.setText("");
                goal_edit_endDate.setText("");
            }
        });
        detail_op_statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent();
                in.setClassName(GoalDetailActivity.this, "org.sysu.herrick.goal.StatisticsActivity");
                Bundle bundle = new Bundle();
                bundle.putString("type", "which_goal");
                bundle.putString("className", getLocalClassName());
                bundle.putInt("which_goal", which_goal);
                in.putExtras(bundle);
                startActivity(in);
                finish();
            }
        });
        detail_op_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent();
                in.setClassName(GoalDetailActivity.this, "org.sysu.herrick.goal.CalendarActivity");
                Bundle bundle = new Bundle();
                bundle.putString("type", "which_goal");
                bundle.putInt("which_goal", which_goal);
                bundle.putString("className", getLocalClassName());
                in.putExtras(bundle);
                startActivity(in);
                finish();
            }
        });
        detail_op_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AccessTokenKeeper.clear(GoalDetailActivity.this);
                Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(GoalDetailActivity.this);
                if (token.getToken().equals("")) { // no token exists, authentication first
                    weiboAuth();
                } else {
                    weiboShare();
                }
            }
        });
        detail_op_achieved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detail_op_menu.setVisibility(GONE);
                op_menu_toggle = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(GoalDetailActivity.this);
                builder.setTitle("Did you achieve this goal?");
                builder.setMessage("    \"  " + detail_goal_des.getText().toString() + "  \"");
                builder.setPositiveButton("Yes, I achieved it", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        noteDB.updateAchieved(which_goal, 1);
                        initGoal();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("No, I will try again      ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        noteDB.updateAchieved(which_goal, -1);
                        initGoal();
                        dialog.dismiss();
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
            }
        });
    }
    private void weiboAuth() {
        mAuthInfo = new AuthInfo(GoalDetailActivity.this, WeiboConstants.APP_KEY, WeiboConstants.REDIRECT_URL, WeiboConstants.SCOPE);
        mSsoHandler = new SsoHandler(GoalDetailActivity.this, mAuthInfo);
        /*call for auth*/
        mSsoHandler.authorize(new WeiboAuthListener() {
            @Override
            public void onComplete(Bundle values) {
                mAccessToken = Oauth2AccessToken.parseAccessToken(values);
                if (mAccessToken.isSessionValid()) {
                    // 保存 Token 到 SharedPreferences
                    AccessTokenKeeper.writeAccessToken(GoalDetailActivity.this, mAccessToken);
                } else {
                    // 以下几种情况，您会收到 Code：
                    // 1. 当您未在平台上注册的应用程序的包名与签名时；
                    // 2. 当您注册的应用程序包名与签名不正确时；
                    // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                    String code = values.getString("code");
                    String message = "weibosdk_auth_failed";
                    if (!TextUtils.isEmpty(code)) {
                        message = message + "\nObtained the code: " + code;
                    }
                    Toast.makeText(GoalDetailActivity.this, message, Toast.LENGTH_LONG).show();
                }
                weiboShare();
            }

            @Override
            public void onCancel() {
                returnToGoalDetail();
                /*Toast.makeText(GoalDetailActivity.this,
                        "weibosdk_auth_canceled", Toast.LENGTH_LONG).show();*/
            }

            @Override
            public void onWeiboException(WeiboException e) {
                returnToGoalDetail();
                /*Toast.makeText(GoalDetailActivity.this,
                        "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();*/
            }
        });
    }
    private Bitmap drawGoalDetail(Cursor cursor) {
        if (cursor.getCount() != 0)
            cursor.moveToFirst();
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.bg).copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bmp);
        TextPaint paint = new TextPaint();
        paint.setColor(Color.rgb(233, 233, 233));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(28f);
        paint.setAntiAlias(true);
        String t = cursor.getString(cursor.getColumnIndex("goalDes")) + "\r\n";
        long sdate = cursor.getLong(cursor.getColumnIndex("startDate"));
        long edate = cursor.getLong(cursor.getColumnIndex("endDate"));
        long now = System.currentTimeMillis();
        if (now > edate) {
            now = edate;
        }
        if (cursor.getInt(cursor.getColumnIndex("achieved")) == 1)
            now = cursor.getLong(cursor.getColumnIndex("achievedDate"));
        sdate /= 86400000;
        now /= 86400000;
        if (now - sdate < 0)
            t += "0\r\nDAY";
        else
            t += now - sdate + "\r\nDAYS";
        canvas.translate(115f, 50f);
        StaticLayout layout = new StaticLayout(t, paint, 300,
                Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
        layout.draw(canvas);
        return bmp;
    }
    private void weiboShare() {
        WeiboMultiMessage message = new WeiboMultiMessage();
        Cursor c = noteDB.query("SELECT * from " + TABLE_NAME + " WHERE _id=" + which_goal);
        message.textObject = genShareContent(c);
        ImageObject i = new ImageObject();
        i.setImageObject(drawGoalDetail(c));
        c.close();
        message.imageObject = i;
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = message;
        AuthInfo authInfo = new AuthInfo(GoalDetailActivity.this, WeiboConstants.APP_KEY, WeiboConstants.REDIRECT_URL, WeiboConstants.SCOPE);
        Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(getApplicationContext());
        String token = "";
        if (accessToken != null) {
            token = accessToken.getToken();
        }
        mWeiboShareAPI.sendRequest(GoalDetailActivity.this, request, authInfo, token, new WeiboAuthListener() {

            @Override
            public void onWeiboException( WeiboException arg0 ) {
                returnToGoalDetail();
            }

            @Override
            public void onComplete( Bundle bundle ) {
                Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
                AccessTokenKeeper.writeAccessToken(getApplicationContext(), newToken);
                /*Toast.makeText(getApplicationContext(), "onAuthorizeComplete token = " + newToken.getToken(), Toast.LENGTH_SHORT).show();*/
                returnToGoalDetail();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Share Cancelled", Toast.LENGTH_SHORT).show();
                returnToGoalDetail();
            }
        });
    }
    private void returnToGoalDetail() {
        /*return*/
        Intent i = new Intent(GoalDetailActivity.this, GoalDetailActivity.class);
        SharedPreferences pref = getSharedPreferences("which_goal", Context.MODE_APPEND);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putInt("which_goal", which_goal);
        editor.commit();
        startActivity(i);
        finish();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClassName(GoalDetailActivity.this, "org.sysu.herrick.goal.NoteActivity");
        startActivity(intent);
        finish();
    }
    private TextObject genShareContent(Cursor cursor) {
        TextObject contentToShare = new TextObject();
        String t = "我在";
        String s = "";
        Calendar tmp = Calendar.getInstance();
        if (cursor.getCount() != 0)
            cursor.moveToFirst();
        long sdate = cursor.getLong(cursor.getColumnIndex("startDate"));
        tmp.setTimeInMillis(sdate);
        t += "" + tmp.get(Calendar.YEAR) + "年" + (tmp.get(Calendar.MONTH) +1) + "月"
                + tmp.get(Calendar.DAY_OF_MONTH) + "日设立的目标："
                + cursor.getString(cursor.getColumnIndex("goalDes")) + ",";
        long edate = cursor.getLong(cursor.getColumnIndex("endDate"));
        long now = System.currentTimeMillis();
        if (now > edate) {
            now = edate;
        }
        if (cursor.getInt(cursor.getColumnIndex("achieved")) == 1) {
            now = cursor.getLong(cursor.getColumnIndex("achievedDate"));
            tmp.setTimeInMillis(now);
            s = "并且在" + tmp.get(Calendar.YEAR) + "年" + tmp.get(Calendar.MONTH) + "月"
                    + tmp.get(Calendar.DAY_OF_MONTH) + "日达成！";
        }
        sdate /= 86400000;
        now /= 86400000;
        if (now - sdate < 0)
            t += "已经坚持了0天了。";
        else
            t += "已经坚持了" +(now - sdate)+ "天了。";
        contentToShare.text = t + s + " (分享来自GOAL-小巧的管理目标APP~~)";
        return contentToShare;
    }
    private void initGoal() {
        Cursor cursor = noteDB.query("SELECT * from " + TABLE_NAME + " WHERE _id=" + which_goal);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            detail_goal_des.setText(cursor.getString(cursor.getColumnIndex("goalDes")));
            long sdate = cursor.getLong(cursor.getColumnIndex("startDate"));
            long edate = cursor.getLong(cursor.getColumnIndex("endDate"));
            long now = System.currentTimeMillis();
            if (now > edate) {
                now = edate;
            }
            if (cursor.getInt(cursor.getColumnIndex("achieved")) == 1) {
                detail_goal_achieved.setText("DAYS\nACHIEVED");
                now = cursor.getLong(cursor.getColumnIndex("achievedDate"));
            } else {
                detail_goal_achieved.setText("DAYS");
            }
            sdate /= 86400000;
            now /= 86400000;
            if (now - sdate < 0)
                detail_goal_days.setText(0 + "");
            else
                detail_goal_days.setText(now - sdate + "");
        }
        cursor.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // SSO 授权回调
        // 重要：发起 SSO 登陆的 Activity 必须重写 onActivityResults
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }

    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
        // 来接收微博客户端返回的数据；执行成功，返回 true，并调用
        // {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
        mWeiboShareAPI.handleWeiboResponse(intent, this);
        returnToGoalDetail();
    }
    @Override
    public void onResponse(BaseResponse baseResp) {
        if(baseResp!= null){
            switch (baseResp.errCode) {
                case WBConstants.ErrorCode.ERR_OK:
                    Toast.makeText(this, "weibosdk_share_success", Toast.LENGTH_LONG).show();
                    break;
                case WBConstants.ErrorCode.ERR_CANCEL:
                    Toast.makeText(this, "weibosdk_share_canceled", Toast.LENGTH_LONG).show();
                    break;
                case WBConstants.ErrorCode.ERR_FAIL:
                    Toast.makeText(this, "weibosdk_share_failed" + "Error Message: " + baseResp.errMsg,
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}
