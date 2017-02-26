package com.jameswolfeoliver.pigeon.Activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.jameswolfeoliver.pigeon.Managers.PageCacheManager;
import com.jameswolfeoliver.pigeon.R;
import com.jameswolfeoliver.pigeon.Server.Rest.RestServer;
import com.wang.avi.AVLoadingIndicatorView;

public class SplashActivity extends AppCompatActivity {

    private ImageView logo;
    private AVLoadingIndicatorView spinner;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = (ImageView) findViewById(R.id.app_logo);
        spinner = (AVLoadingIndicatorView) findViewById(R.id.spinner);
        status = (TextView) findViewById(R.id.status);

        logo.setImageResource(R.drawable.app_icon);
        logo.setVisibility(View.VISIBLE);
        status.setText(R.string.status_updating);
        status.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PageCacheManager.maybeUpdateLocalPageCache(new RestServer.RestCallback<Boolean>() {
            @Override
            public void onResult(Boolean success) {
                Log.d("James", success.toString());
                transitionToInbox();
            }
        });
    }

    private void transitionToInbox() {
        status.setText(R.string.status_loading);
        spinner.setVisibility(View.INVISIBLE);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ObjectAnimator animation = ObjectAnimator.ofFloat(logo, "rotationY", 0.0f, 180f);
                animation.setDuration(1000);
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
                        SplashActivity.this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
        };
        handler.postDelayed(runnable, 1000);
    }
}
