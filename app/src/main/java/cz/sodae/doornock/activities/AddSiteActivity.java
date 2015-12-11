package cz.sodae.doornock.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import cz.sodae.doornock.R;

public class AddSiteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_site);
        setTitle(R.string.activity_add_site_title);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CheckBox checkbox = (CheckBox) findViewById(R.id.know_login);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onKnownLoginChecked(b);
            }
        });
        onKnownLoginChecked(checkbox.isChecked());

    }


    protected void onKnownLoginChecked(boolean isChecked)
    {
        final LinearLayout login_frame =  (LinearLayout) findViewById(R.id.login_frame);
        ((EditText) findViewById(R.id.login_username)).setEnabled(isChecked);
        ((EditText) findViewById(R.id.login_password)).setEnabled(isChecked);

        if (isChecked) {
            login_frame.setVisibility(View.VISIBLE);
        } else {
            Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.slidedown);

            slide_down.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationEnd(Animation animation) {
                    login_frame.setVisibility(View.GONE);
                }

                public void onAnimationRepeat(Animation animation) {}

                public void onAnimationStart(Animation animation) {}
            });

            login_frame.startAnimation(slide_down);
        }
    }

}
