package com.jameswolfeoliver.pigeon.Activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Utilities.PigeonApplication;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ((ImageView) findViewById(R.id.app_logo)).setImageResource(R.drawable.app_icon_circle);
    }

    @Override
    protected void onStart(){
        super.onStart();

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                transitionToInbox();
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    private void transitionToInbox() {
        ObjectAnimator animation = ObjectAnimator.ofFloat(findViewById(R.id.app_logo), "rotationY", 0.0f, 360f);
        animation.setDuration(1500);
        animation.setInterpolator(new LinearInterpolator());
        animation.start();
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Intent inboxIntent = new Intent(SplashActivity.this, InboxActivity.class);
                inboxIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(inboxIntent);
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
}
