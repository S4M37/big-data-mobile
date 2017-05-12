package gl4.insat.tn.bigdatamobile.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import gl4.insat.tn.bigdatamobile.R;

public class SplashScreenActivity extends AppCompatActivity {

    protected Boolean loadFinish = false;
    boolean isActivitySrated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_layout);

        final RelativeLayout splash_layout = (RelativeLayout) findViewById(R.id.splash_layout);
        ImageView splashLogo = (ImageView) findViewById(R.id.splash_logo);
        final View splashHint = findViewById(R.id.splash_hint);
        splashHint.setVisibility(View.GONE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.splash_alpha);
        animation.reset();
        splash_layout.clearAnimation();
        animation = AnimationUtils.loadAnimation(this, R.anim.splash_alpha);
        animation.reset();
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        Animation animationTmp = AnimationUtils.loadAnimation(SplashScreenActivity.this, R.anim.splash_alpha);
        animation.reset();
        splashHint.clearAnimation();
        splashHint.startAnimation(animationTmp);
        splashHint.setVisibility(View.VISIBLE);
        splashLogo.clearAnimation();
        splashLogo.startAnimation(animation);
        Thread splashThread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;

                    while (waited < 3000) {
                        if (loadFinish) {
                            break;
                        }
                        sleep(500);
                        waited += 500;
                    }

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                    isActivitySrated = true;
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    if (!isActivitySrated) {

                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                }
            }
        };
        splashThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
