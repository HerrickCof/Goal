package org.sysu.herrick.goal;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import javax.mail.MessagingException;

/**
 * Created by herrick on 2016/12/20.
 */

public class SettingActivity extends AppCompatActivity {

    private static String REMINDER_ACTION = "org.sysu.herrick.goal.ReminderAlarmReceiver";


    private TableRow bound_weibo    = null;
    private TextView bound_webo_text= null;
    private TableRow email          = null;
    private TableRow backup_all     = null;
    private TableRow cancel_reminder = null;
    private RelativeLayout back     = null;

    private boolean isBound = false;

    private AuthInfo mAuthInfo;
    private SsoHandler mSsoHandler;
    private Oauth2AccessToken mAccessToken;

    private String toAdd;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            if (data.getBoolean("isSuccess"))
                Toast.makeText(SettingActivity.this, "BACKUP FILE IS SENT TO " + toAdd, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(SettingActivity.this, "FAILED TO SEND BACKUP FILE TO " + toAdd, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        back            = (RelativeLayout) findViewById(R.id.setting_back_button);
        bound_weibo     = (TableRow) findViewById(R.id.setting_bound_weibo);
        bound_webo_text = (TextView) findViewById(R.id.setting_bound_weibo_text);
        email           = (TableRow) findViewById(R.id.setting_email);
        backup_all      = (TableRow) findViewById(R.id.setting_backup_all);
        cancel_reminder = (TableRow) findViewById(R.id.setting_cancel_reminders);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent();
                in.setClassName(SettingActivity.this, "org.sysu.herrick.goal.NoteActivity");
                startActivity(in);
                finish();
            }
        });

        bound_weibo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Sina Weibo");
                builder.setMessage(bound_webo_text.getText() + "?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isBound)
                            AccessTokenKeeper.clear(SettingActivity.this);
                        else {
                            mAuthInfo = new AuthInfo(SettingActivity.this, WeiboConstants.APP_KEY, WeiboConstants.REDIRECT_URL, WeiboConstants.SCOPE);
                            mSsoHandler = new SsoHandler(SettingActivity.this, mAuthInfo);
                            mSsoHandler.authorize(new WeiboAuthListener() {
                                @Override
                                public void onComplete(Bundle values) {
                                    mAccessToken = Oauth2AccessToken.parseAccessToken(values);
                                    if (mAccessToken.isSessionValid()) {
                                        // 保存 Token 到 SharedPreferences
                                        AccessTokenKeeper.writeAccessToken(SettingActivity.this, mAccessToken);
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
                                        Toast.makeText(SettingActivity.this, message, Toast.LENGTH_LONG).show();
                                    }
                                    returnToSetting();
                                }

                                @Override
                                public void onCancel() {
                                    /*Toast.makeText(SettingActivity.this,
                                            "weibosdk_auth_canceled", Toast.LENGTH_LONG).show();*/
                                    returnToSetting();
                                }

                                @Override
                                public void onWeiboException(WeiboException e) {
                                    /*Toast.makeText(SettingActivity.this,
                                            "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();*/
                                    returnToSetting();
                                }
                            });
                        }
                        refresh();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
            }
        });
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SharedPreferences pref = getSharedPreferences("backup_mailbox", Context.MODE_APPEND);
                String toAdd = pref.getString("mailbox", "");
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Set your email address.");
                final View v = View.inflate(SettingActivity.this, R.layout.set_email_dialog, null);
                final EditText et = (EditText) v.findViewById(R.id.email_edit);
                if (!toAdd.equals("")) {
                    et.setHint(toAdd);
                }
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
                        if (et.getText().toString().equals("")) {
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("mailbox", "");
                            editor.commit();
                            Toast.makeText(SettingActivity.this, "EMAIL ADDRESS IS CLEAR", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else if (MailUtils.isEmail(et.getText().toString())) {
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("mailbox", et.getText().toString());
                            editor.commit();
                            Toast.makeText(SettingActivity.this, "EMAIL ADDRESS IS SAVED", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(SettingActivity.this, "WRONG EMAIL ADDRESS", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.show();
            }
        });
        backup_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SharedPreferences pref = getSharedPreferences("backup_mailbox", Context.MODE_APPEND);
                toAdd = pref.getString("mailbox", "");
                if (toAdd.equals("")) {
                    Toast.makeText(SettingActivity.this, "Set your Email first", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                    builder.setTitle("Back up all your diaries through e-mail?");
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
        cancel_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("Cancel all reminders?");
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(SettingActivity.this, ReminderAlarmReceiver.class);
                        intent.setAction(REMINDER_ACTION);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(SettingActivity.this, 3333, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
                        if (am != null)
                            am.cancel(pendingIntent);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
            }
        });
        refresh();
    }
    private void sendEmail() {
        SharedPreferences pref = getSharedPreferences("backup_mailbox", Context.MODE_APPEND);
        toAdd = pref.getString("mailbox", "");
        new Thread(new Runnable() {
            @Override
            public void run() {
                MailUtils mailman = new MailUtils(SettingActivity.this, -5555);
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
    private void returnToSetting() {
        Intent in = new Intent(SettingActivity.this,SettingActivity.class);
        startActivity(in);
    }
    private void refresh() {
        Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(SettingActivity.this);
        if (token.getToken().equals("")) {
            bound_webo_text.setText("Bound Sina Weibo");
            isBound = false;
        } else {
            bound_webo_text.setText("Unbound Sina Weibo");
            isBound = true;
        }
    }
    @Override
    public void onBackPressed() {
        Intent in = new Intent();
        in.setClassName(SettingActivity.this, "org.sysu.herrick.goal.NoteActivity");
        startActivity(in);
        finish();
    }
}
