package com.example.animatedbottombar;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _bar = (AnimatedBottomBar) findViewById(R.id.bar);

        {
            float ps = getResources().getDisplayMetrics().density / 2.75f;
            AnimatedBottomBar.Config cfg = _bar.getConfig();

            cfg.topLineY = (int)(100 * ps);
            cfg.bubbleSize = (int)(175 * ps);
            cfg.bubbleContentSize = (int)(80 * ps);
            cfg.bubbleY = (int)(10 * ps);
            cfg.bubbleY1 = (int)(165 * ps);

            cfg.itemsMarginH = (int)(0 * ps);
            cfg.itemsMarginTop = (int)(25 * ps);
            cfg.itemsHeight = (int)(80 * ps);

            cfg.itemHideDistance0 = 1.0f;
            cfg.itemHideDistance1 = 0.65f;
            cfg.itemsAnimationDuration = 350;

            cfg.itemsDY = (int)(20 * ps);

            cfg.curveWidth0 = 340.0f * ps;
            cfg.curveWidth1 = 180.0f * ps;
            cfg.curveDepth0 = 120.0f * ps;
            cfg.curveDepth1 = 75.0f * ps;
            cfg.curveControlA0 = 80.0f * ps;
            cfg.curveControlA1 = 40.0f * ps;
            cfg.curveControlB0 = 115.0f * ps;
            cfg.curveControlB1 = 50.0f * ps;

            cfg.baseAnimationDuration = 350;

            cfg.apply();
        }

        View[] contents = new View[5];
        AnimatedBottomBar.AnimatedContent[] ac = new AnimatedBottomBar.AnimatedContent[5];
        AnimatedBottomBar.ItemButton[] buttons = new AnimatedBottomBar.ItemButton[5];

        for (int i = 0; i < 5; ++i) {
            contents[i] = new View(this);

            DummyAnimatedContent dac = new DummyAnimatedContent();
            dac.setView(contents[i]);
            ac[i] = dac;

            Button btn = new Button(this);
            btn.setBackground(null);
            btn.setPadding(0, 0, 0, 0);
            btn.setTextColor(0xFFAAAAAA);
            btn.setTextSize(24.0f);
            btn.setText(Integer.toString(i));
            btn.setGravity(Gravity.CENTER);

            buttons[i] = new AnimatedBottomBar.ItemButton();
            buttons[i].setButton(btn);
        }

        contents[0].setBackground(new ColorDrawable(0xFFAAAA33));
        contents[1].setBackground(new ColorDrawable(0xFF33AA33));
        contents[2].setBackground(new ColorDrawable(0xFF333333));
        contents[3].setBackground(new ColorDrawable(0xFF3333AA));
        contents[4].setBackground(new ColorDrawable(0xFF33AAAA));

        _bar.setContents(buttons, ac);
        _bar.setSelection(0);
    }

    private AnimatedBottomBar _bar;
}
