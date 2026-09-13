package com.example.finalproject.ui.onboarding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.ui.home.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private OnboardingPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_activity);

        viewPager = findViewById(R.id.viewPagerOnboarding);

        adapter = new OnboardingPagerAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.setUserInputEnabled(true);
    }

    public void nextPage() {
        int current = viewPager.getCurrentItem();
        if (current < adapter.getItemCount() - 1) {
            viewPager.setCurrentItem(current + 1, true);
        } else {
            finishOnboarding();
        }
    }

    public void finishOnboarding() {

        Log.d("CHECK", "finishOnboarding() STARTED");   // LOG 2

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e("CHECK", "ERROR: UserId = NULL");
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("callName", OnboardingData.callName);
        data.put("goals", OnboardingData.goals);
        data.put("reminderTime", OnboardingData.reminderTime);
        data.put("about", OnboardingData.about);
        data.put("onboardingDone", true);

        db.collection("users")
                .document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Log.d("CHECK", "FIRESTORE SUCCESS");    // LOG 3
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("CHECK", "FIRESTORE ERROR: " + e.getMessage()); // LOG 4
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
