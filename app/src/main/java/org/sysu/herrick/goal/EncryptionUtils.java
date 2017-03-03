package org.sysu.herrick.goal;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by herrick on 2016/12/20.
 */

public class EncryptionUtils extends AppCompatActivity {

    private TextView hint = null;
    private ImageView input_1 = null;
    private ImageView input_2 = null;
    private ImageView input_3 = null;
    private ImageView input_4 = null;
    private Button keyboard_0 = null;
    private Button keyboard_1 = null;
    private Button keyboard_2 = null;
    private Button keyboard_3 = null;
    private Button keyboard_4 = null;
    private Button keyboard_5 = null;
    private Button keyboard_6 = null;
    private Button keyboard_7 = null;
    private Button keyboard_8 = null;
    private Button keyboard_9 = null;
    private Button keyboard_back = null;
    private RelativeLayout keyboard_clear = null;
    private GridLayout clipboard = null;

    private int num_input = 0;
    private String input = "";
    private String setkey = "";
    private String confirmKey = "";
    private KeyDB keyDB = null;
    private int mode_setKey = 5555;
    private int mode_authen = 4444;
    private boolean confirm = false;
    private boolean isfirstTime = false;

    private int mode;
    private String toClass;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encryption);

        mode = this.getIntent().getExtras().getInt("mode");
        toClass = this.getIntent().getExtras().getString("toClass");

        /*fin views by ids*/
        hint = (TextView) findViewById(R.id.encrypt_hint);
        input_1 = (ImageView) findViewById(R.id.encrypt_input_1);
        input_2 = (ImageView) findViewById(R.id.encrypt_input_2);
        input_3 = (ImageView) findViewById(R.id.encrypt_input_3);
        input_4 = (ImageView) findViewById(R.id.encrypt_input_4);
        keyboard_0 = (Button) findViewById(R.id.encrypt_keyboard_0);
        keyboard_1 = (Button) findViewById(R.id.encrypt_keyboard_1);
        keyboard_2 = (Button) findViewById(R.id.encrypt_keyboard_2);
        keyboard_3 = (Button) findViewById(R.id.encrypt_keyboard_3);
        keyboard_4 = (Button) findViewById(R.id.encrypt_keyboard_4);
        keyboard_5 = (Button) findViewById(R.id.encrypt_keyboard_5);
        keyboard_6 = (Button) findViewById(R.id.encrypt_keyboard_6);
        keyboard_7 = (Button) findViewById(R.id.encrypt_keyboard_7);
        keyboard_8 = (Button) findViewById(R.id.encrypt_keyboard_8);
        keyboard_9 = (Button) findViewById(R.id.encrypt_keyboard_9);
        keyboard_back = (Button) findViewById(R.id.encrypt_keyboard_back);
        keyboard_clear = (RelativeLayout) findViewById(R.id.encrypt_keyboard_clear);
        clipboard = (GridLayout) findViewById(R.id.encrypt_clipboard);


        keyboard_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == mode_setKey) {
                    if (confirm) {
                        confirmKey += "" + 0;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            if (setkey.equals(confirmKey))
                                setKeyToDB();
                            else
                                tryAgain();
                        }

                    } else {
                        setkey += "" + 0;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            confirm = true;
                            num_input = 0;
                            hint.setText("[ PLEASE CONFIRM THE KEY ]");
                        }
                    }
                } else if (mode == mode_authen) {
                    hint.setText("[ VERIFY KEY ]");
                    input += "" + 0;
                    num_input++;
                    fill_input();
                    if (num_input == 4) {
                        if (isCorrect())
                            pass();
                        else
                            tryAgain();
                    }
                }

            }
        });
        keyboard_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == mode_setKey) {
                    if (confirm) {
                        confirmKey += "" + 1;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            if (setkey.equals(confirmKey))
                                setKeyToDB();
                            else
                                tryAgain();
                        }

                    } else {
                        setkey += "" + 1;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            confirm = true;
                            num_input = 0;
                            hint.setText("[ PLEASE CONFIRM THE KEY ]");
                        }
                    }
                } else if (mode == mode_authen) {
                    hint.setText("[ VERIFY KEY ]");
                    input += "" + 1;
                    num_input++;
                    fill_input();
                    if (num_input == 4) {

                        if (isCorrect())
                            pass();
                        else
                            tryAgain();
                    }
                }
            }
        });
        keyboard_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == mode_setKey) {
                    if (confirm) {
                        confirmKey += "" + 2;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            if (setkey.equals(confirmKey))
                                setKeyToDB();
                            else
                                tryAgain();
                        }

                    } else {
                        setkey += "" + 2;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            confirm = true;
                            num_input = 0;
                            hint.setText("[ PLEASE CONFIRM THE KEY ]");
                        }
                    }
                } else if (mode == mode_authen) {
                    hint.setText("[ VERIFY KEY ]");
                    input += "" + 2;
                    num_input++;
                    fill_input();
                    if (num_input == 4) {
                        if (isCorrect())
                            pass();
                        else
                            tryAgain();
                    }
                }
            }
        });
        keyboard_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == mode_setKey) {
                    if (confirm) {
                        confirmKey += "" + 3;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            if (setkey.equals(confirmKey))
                                setKeyToDB();
                            else
                                tryAgain();
                        }

                    } else {
                        setkey += "" + 3;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            confirm = true;
                            num_input = 0;
                            hint.setText("[ PLEASE CONFIRM THE KEY ]");
                        }
                    }
                } else if (mode == mode_authen) {
                    hint.setText("[ VERIFY KEY ]");
                    input += "" + 3;
                    num_input++;
                    fill_input();
                    if (num_input == 4) {
                        if (isCorrect())
                            pass();
                        else
                            tryAgain();
                    }
                }
            }
        });
        keyboard_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == mode_setKey) {
                    if (confirm) {
                        confirmKey += "" + 4;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            if (setkey.equals(confirmKey))
                                setKeyToDB();
                            else
                                tryAgain();
                        }

                    } else {
                        setkey += "" + 4;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            confirm = true;
                            num_input = 0;
                            hint.setText("[ PLEASE CONFIRM THE KEY ]");
                        }
                    }
                } else if (mode == mode_authen) {
                    hint.setText("[ VERIFY KEY ]");
                    input += "" + 4;
                    num_input++;
                    fill_input();
                    if (num_input == 4) {
                        if (isCorrect())
                            pass();
                        else
                            tryAgain();
                    }
                }
            }
        });
        keyboard_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == mode_setKey) {
                    if (confirm) {
                        confirmKey += "" + 5;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            if (setkey.equals(confirmKey))
                                setKeyToDB();
                            else
                                tryAgain();
                        }

                    } else {
                        setkey += "" + 5;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            confirm = true;
                            num_input = 0;
                            hint.setText("[ PLEASE CONFIRM THE KEY ]");
                        }
                    }
                } else if (mode == mode_authen) {
                    hint.setText("[ VERIFY KEY ]");
                    input += "" + 5;
                    num_input++;
                    fill_input();
                    if (num_input == 4) {
                        if (isCorrect())
                            pass();
                        else
                            tryAgain();
                    }
                }
            }
        });
        keyboard_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == mode_setKey) {
                    if (confirm) {
                        confirmKey += "" + 6;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            if (setkey.equals(confirmKey))
                                setKeyToDB();
                            else
                                tryAgain();
                        }

                    } else {
                        setkey += "" + 6;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            confirm = true;
                            num_input = 0;
                            hint.setText("[ PLEASE CONFIRM THE KEY ]");
                        }
                    }
                } else if (mode == mode_authen) {
                    hint.setText("[ VERIFY KEY ]");
                    input += "" + 6;
                    num_input++;
                    fill_input();
                    if (num_input == 4) {
                        if (isCorrect())
                            pass();
                        else
                            tryAgain();
                    }
                }
            }
        });
        keyboard_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == mode_setKey) {
                    if (confirm) {
                        confirmKey += "" + 7;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            if (setkey.equals(confirmKey))
                                setKeyToDB();
                            else
                                tryAgain();
                        }

                    } else {
                        setkey += "" + 7;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            confirm = true;
                            num_input = 0;
                            hint.setText("[ PLEASE CONFIRM THE KEY ]");
                        }
                    }
                } else if (mode == mode_authen) {
                    hint.setText("[ VERIFY KEY ]");
                    input += "" + 7;
                    num_input++;
                    fill_input();
                    if (num_input == 4) {
                        if (isCorrect())
                            pass();
                        else
                            tryAgain();
                    }
                }
            }
        });keyboard_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == mode_setKey) {
                    if (confirm) {
                        confirmKey += "" + 8;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            if (setkey.equals(confirmKey))
                                setKeyToDB();
                            else
                                tryAgain();
                        }

                    } else {
                        setkey += "" + 8;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            confirm = true;
                            num_input = 0;
                            hint.setText("[ PLEASE CONFIRM THE KEY ]");
                        }
                    }
                } else if (mode == mode_authen) {
                    hint.setText("[ VERIFY KEY ]");
                    input += "" + 8;
                    num_input++;
                    fill_input();
                    if (num_input == 4) {
                        if (isCorrect())
                            pass();
                        else
                            tryAgain();
                    }
                }
            }
        });
        keyboard_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == mode_setKey) {
                    if (confirm) {
                        confirmKey += "" + 9;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            if (setkey.equals(confirmKey))
                                setKeyToDB();
                            else
                                tryAgain();
                        }

                    } else {
                        setkey += "" + 9;
                        num_input++;
                        fill_input();
                        if (num_input == 4) {
                            confirm = true;
                            num_input = 0;
                            hint.setText("[ PLEASE CONFIRM THE KEY ]");
                        }
                    }
                } else if (mode == mode_authen) {
                    hint.setText("[ VERIFY KEY ]");
                    input += "" + 9;
                    num_input++;
                    fill_input();
                    if (num_input == 4) {
                        if (isCorrect())
                            pass();
                        else
                            tryAgain();
                    }
                }
            }
        });
        keyboard_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName(EncryptionUtils.this, "org.sysu.herrick.goal.NoteActivity");
                startActivity(intent);
                finish();
            }
        });
        keyboard_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (num_input == 0) {
                    fill_input();
                    return;
                }
                num_input--;
                fill_input();
                if (mode == mode_setKey) {
                    if (confirm) {
                        confirmKey = confirmKey.substring(0, num_input);
                    } else {
                        setkey = setkey.substring(0, num_input);
                    }
                } else if (mode == mode_authen) {
                    input = input.substring(0, num_input);
                }

            }
        });

        keyDB = KeyDB.getInstance(EncryptionUtils.this);
        if (mode == mode_setKey) {
            int key = keyDB.queryKey();
            if (key == -1) {
                isfirstTime = true;
                hint.setText("[ PLEASE SET A NEW KEY ]");
            } else {
                isfirstTime = false;
                mode = mode_authen;
                hint.setText("[ VERIFY OLD KEY ]");
            }
        } else if (mode == mode_authen) {
            hint.setText("[ VERIFY KEY ]");
            int key = keyDB.queryKey();
            if (key == -1) {
                Toast.makeText(EncryptionUtils.this, "PLEASE SET KEY FIRST", Toast.LENGTH_SHORT).show();
                Intent in = new Intent();
                in.setClassName(EncryptionUtils.this, "org.sysu.herrick.goal.NoteActivity");
                startActivity(in);
                finish();
            }
        }

    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClassName(EncryptionUtils.this, "org.sysu.herrick.goal.NoteActivity");
        startActivity(intent);
        finish();
    }
    private void tryAgain() {
        setkey = "";
        confirmKey = "";
        input = "";
        num_input = 0;
        confirm = false;
        if (this.getIntent().getExtras().getInt("mode") == mode_setKey) {
            if (mode == mode_authen)
                hint.setText("[ WRONG! VERIFY OLD KEY ]");
            else
                hint.setText("[ NOT MATCHED TRY SETTING NEW KEY ]");

        } else {
            hint.setText("[ WRONG TRY AGAIN ]");
        }
    }
    private void setKeyToDB() {
        if (isfirstTime)
            keyDB.setKey(Integer.parseInt(setkey));
        else
            keyDB.updateKey(Integer.parseInt(setkey));
        Toast.makeText(EncryptionUtils.this, "NEW KEY IS SAVED", Toast.LENGTH_SHORT).show();
        Intent in = new Intent();
        in.setClassName(EncryptionUtils.this, "org.sysu.herrick.goal.NoteActivity");
        startActivity(in);
        finish();
    }
    private void fill_input() {
        switch (num_input) {
            case 0 :
                input_1.setImageResource(R.drawable.ic_media_record);
                input_2.setImageResource(R.drawable.ic_media_record);
                input_3.setImageResource(R.drawable.ic_media_record);
                input_4.setImageResource(R.drawable.ic_media_record);
                break;
            case 1 :
                input_1.setImageResource(R.drawable.ic_solid_circle);
                input_2.setImageResource(R.drawable.ic_media_record);
                input_3.setImageResource(R.drawable.ic_media_record);
                input_4.setImageResource(R.drawable.ic_media_record);
                break;
            case 2 :
                input_1.setImageResource(R.drawable.ic_solid_circle);
                input_2.setImageResource(R.drawable.ic_solid_circle);
                input_3.setImageResource(R.drawable.ic_media_record);
                input_4.setImageResource(R.drawable.ic_media_record);
                break;
            case 3 :
                input_1.setImageResource(R.drawable.ic_solid_circle);
                input_2.setImageResource(R.drawable.ic_solid_circle);
                input_3.setImageResource(R.drawable.ic_solid_circle);
                input_4.setImageResource(R.drawable.ic_media_record);
                break;
            case 4 :
                input_1.setImageResource(R.drawable.ic_solid_circle);
                input_2.setImageResource(R.drawable.ic_solid_circle);
                input_3.setImageResource(R.drawable.ic_solid_circle);
                input_4.setImageResource(R.drawable.ic_solid_circle);
                break;
            default:
                break;
        }
    }
    private boolean isCorrect() {
        int key = keyDB.queryKey();
        if (input.equals("" + key))
            return true;
        else {
            hint.setText("[ WRONG ]");
            return false;
        }
    }
    private void pass() {
        setkey = "";
        confirmKey = "";
        input = "";
        num_input = 0;
        confirm = false;
        Toast.makeText(EncryptionUtils.this, "PASS", Toast.LENGTH_SHORT).show();
        if (this.getIntent().getExtras().getInt("mode") == mode_setKey) {
            mode = mode_setKey;
            hint.setText("[ SET A NEW KEY ]");
        } else {
            Intent intent = new Intent();
            intent.setClassName(EncryptionUtils.this, toClass);
            startActivity(intent);
            finish();
        }

    }
}
