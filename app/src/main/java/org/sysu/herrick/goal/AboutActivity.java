package org.sysu.herrick.goal;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by Herrick on 2017/1/3.
 */

public class AboutActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_goal);

        RelativeLayout back = (RelativeLayout) findViewById(R.id.about_back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent();
                in.setClassName(AboutActivity.this, "org.sysu.herrick.goal.NoteActivity");
                startActivity(in);
                finish();
            }
        });
    }
    @Override
    public void onBackPressed() {
        Intent in = new Intent();
        in.setClassName(AboutActivity.this, "org.sysu.herrick.goal.NoteActivity");
        startActivity(in);
        finish();
    }
}
