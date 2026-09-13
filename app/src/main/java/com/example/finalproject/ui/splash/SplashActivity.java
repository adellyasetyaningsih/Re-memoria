package com.example.finalproject.ui.splash;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.example.finalproject.ui.auth.LoginActivity;
import com.example.finalproject.ui.home.HomeActivity;
import com.example.finalproject.ui.onboarding.OnboardingActivity;
import com.example.finalproject.utils.ThemeHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.imgSplashLogo);

        ObjectAnimator fade = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
        fade.setDuration(800);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0.6f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0.6f, 1f);
        scaleX.setDuration(800);
        scaleY.setDuration(800);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(fade, scaleX, scaleY);
        set.start();

        new Handler().postDelayed(this::routeUser, 1500);
    }

    private void routeUser() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    Boolean done = doc.getBoolean("onboardingDone");

                    if (Boolean.TRUE.equals(done)) {
                        startActivity(new Intent(this, HomeActivity.class));
                    } else {
                        startActivity(new Intent(this, OnboardingActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                });
    }
}
