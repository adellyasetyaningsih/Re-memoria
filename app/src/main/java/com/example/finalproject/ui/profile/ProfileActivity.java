package com.example.finalproject.ui.profile;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.example.finalproject.R;
import com.example.finalproject.ui.auth.LoginActivity;
import com.example.finalproject.utils.LoadingOverlay;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class ProfileActivity extends AppCompatActivity {

    // UI basic info
    private ImageView btnBackProfile;
    private TextView tvProfileName, tvProfileEmail;
    private EditText etProfileName, etProfileAbout;
    private Button btnSaveProfile;

    // Goals
    private FlexboxLayout layoutGoalChips;
    private final String[] ALL_GOALS = {
            "Daily Memory Tracking",
            "Mental Health Reflection",
            "Self-Growth Progress",
            "Free Voice Diary",
            "Stress Management",
            "Positivity Building"
    };
    private final List<String> selectedGoals = new ArrayList<>();

    // Reminder & dark mode
    private TextView tvReminderTime;
    private Switch switchReminder, switchDarkMode;

    private String reminderTime = "15:00";

    // Password
    private View layoutPasswordForm;
    private Button btnTogglePasswordForm, btnDoChangePassword;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;

    // Account
    private Button btnDeleteAccount, btnLogout;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userId = auth.getCurrentUser().getUid();

        initViews();
        initListeners();
        loadUserData();
        setupDarkModeInitial();
    }

    // -----------------------------------------------------
    // INIT VIEWS
    // -----------------------------------------------------
    private void initViews() {
        btnBackProfile = findViewById(R.id.btnBackProfile);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        etProfileName = findViewById(R.id.etProfileName);
        etProfileAbout = findViewById(R.id.etProfileAbout);
        etProfileAbout.setTextSize(13f);   // ❗ ABOUT FONT FIX
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        layoutGoalChips = findViewById(R.id.layoutGoalChips);

        tvReminderTime = findViewById(R.id.tvReminderTime);
        switchReminder = findViewById(R.id.switchReminder);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        layoutPasswordForm = findViewById(R.id.layoutPasswordForm);
        btnTogglePasswordForm = findViewById(R.id.btnTogglePasswordForm);
        btnDoChangePassword = findViewById(R.id.btnDoChangePassword);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnLogout = findViewById(R.id.btnLogout);

        // Email
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) tvProfileEmail.setText(user.getEmail());

        generateGoalChips();
    }

    // -----------------------------------------------------
    // LISTENERS
    // -----------------------------------------------------
    private void initListeners() {
        btnBackProfile.setOnClickListener(v -> finish());

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        btnTogglePasswordForm.setOnClickListener(v -> {
            layoutPasswordForm.setVisibility(
                    layoutPasswordForm.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
            );
        });

        btnDoChangePassword.setOnClickListener(v -> changePassword());

        // Time picker (only if reminder ON)
        tvReminderTime.setOnClickListener(v -> {
            if (!switchReminder.isChecked()) {
                Toast.makeText(this, "Enable reminder first", Toast.LENGTH_SHORT).show();
                return;
            }
            showTimePicker();
        });

        switchReminder.setOnCheckedChangeListener((button, enabled) ->
                saveReminderSettings(enabled, reminderTime)
        );

        // Dark Mode
        switchDarkMode.setOnCheckedChangeListener((b, isDark) -> {
            com.example.finalproject.utils.ThemeHelper.setDarkMode(this, isDark);
        });

        // LOGOUT FIX → langsung ke LoginActivity
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        btnDeleteAccount.setOnClickListener(v -> confirmDeleteAccount());
    }

    // -----------------------------------------------------
    // LOAD USER DATA
    // -----------------------------------------------------
    private void loadUserData() {
        LoadingOverlay.show(this);
        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    LoadingOverlay.hide();
                    if (!doc.exists()) return;

                    String callName = doc.getString("callName");
                    String about = doc.getString("about");
                    String rt = doc.getString("reminderTime");
                    Boolean reminderEnabled = doc.getBoolean("reminderEnabled");

                    // goals
                    selectedGoals.clear();
                    Object goalsObj = doc.get("goals");
                    if (goalsObj instanceof List<?>) {
                        for (Object o : (List<?>) goalsObj)
                            if (o instanceof String) selectedGoals.add((String) o);
                    }

                    if (!TextUtils.isEmpty(callName)) {
                        tvProfileName.setText(callName);
                        etProfileName.setText(callName);
                    }

                    if (!TextUtils.isEmpty(about)) etProfileAbout.setText(about);

                    if (!TextUtils.isEmpty(rt)) {
                        reminderTime = rt;
                        tvReminderTime.setText(rt);
                    }

                    if (reminderEnabled != null)
                        switchReminder.setChecked(reminderEnabled);

                    refreshGoalChipsSelection();
                });
    }

    // -----------------------------------------------------
    // GOALS (CHIPS)
    // -----------------------------------------------------
    private void generateGoalChips() {
        layoutGoalChips.removeAllViews();

        for (String g : ALL_GOALS) {
            TextView chip = new TextView(this);
            chip.setText(g);
            chip.setTextSize(14f);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            chip.setPadding(40, 22, 40, 22);
            chip.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_unselected));

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(10, 10, 10, 10);
            chip.setLayoutParams(params);

            chip.setOnClickListener(v -> toggleGoal(g, chip));
            layoutGoalChips.addView(chip);
        }
    }

    private void refreshGoalChipsSelection() {
        int count = layoutGoalChips.getChildCount();
        for (int i = 0; i < count; i++) {
            TextView chip = (TextView) layoutGoalChips.getChildAt(i);
            boolean isSelected = selectedGoals.contains(chip.getText().toString());
            chip.setBackground(ContextCompat.getDrawable(
                    this,
                    isSelected ? R.drawable.chip_selected : R.drawable.chip_unselected
            ));
            chip.setTextColor(isSelected ? 0xFFFFFFFF : ContextCompat.getColor(this, R.color.text_primary));
        }
    }

    private void toggleGoal(String goal, TextView chip) {
        if (selectedGoals.contains(goal)) {
            selectedGoals.remove(goal);
            chip.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_unselected));
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        } else {
            if (selectedGoals.size() >= 3) {
                Toast.makeText(this, "Choose max 3 goals", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedGoals.add(goal);
            chip.setBackground(ContextCompat.getDrawable(this, R.drawable.chip_selected));
            chip.setTextColor(0xFFFFFFFF);
        }
    }

    // -----------------------------------------------------
    // SAVE PROFILE
    // -----------------------------------------------------
    private void saveProfile() {
        String callName = etProfileName.getText().toString().trim();
        String about = etProfileAbout.getText().toString().trim();

        if (callName.isEmpty()) {
            etProfileName.setError("Required");
            return;
        }

        tvProfileName.setText(callName);

        Map<String, Object> data = new HashMap<>();
        data.put("callName", callName);
        data.put("about", about);
        data.put("goals", selectedGoals);
        data.put("reminderTime", reminderTime);
        data.put("reminderEnabled", switchReminder.isChecked());

        LoadingOverlay.show(this);
        firestore.collection("users")
                .document(userId)
                .update(data)
                .addOnSuccessListener(v -> {
                    LoadingOverlay.hide();
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                });

    }

    // -----------------------------------------------------
    // REMINDER (TIME PICKER)
    // -----------------------------------------------------
    private void showTimePicker() {
        String[] parts = reminderTime.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);

        TimePickerDialog picker = new TimePickerDialog(
                this,
                (view, hour, minute) -> {
                    reminderTime = String.format("%02d:%02d", hour, minute);
                    tvReminderTime.setText(reminderTime);
                    saveReminderSettings(switchReminder.isChecked(), reminderTime);
                },
                h, m, true
        );
        picker.show();
    }

    private void saveReminderSettings(boolean enabled, String time) {
        firestore.collection("users")
                .document(userId)
                .update("reminderEnabled", enabled, "reminderTime", time);
    }

    // -----------------------------------------------------
    // DARK MODE INIT
    // -----------------------------------------------------
    private void setupDarkModeInitial() {
        switchDarkMode.setChecked(com.example.finalproject.utils.ThemeHelper.isDarkMode(this));
    }

    // -----------------------------------------------------
    // PASSWORD CHANGE
    // -----------------------------------------------------
    private void changePassword() {
        String curr = etCurrentPassword.getText().toString();
        String np = etNewPassword.getText().toString();
        String cp = etConfirmPassword.getText().toString();

        if (curr.isEmpty() || np.isEmpty() || cp.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!np.equals(cp)) {
            Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), curr))
                .addOnSuccessListener(unused ->
                        user.updatePassword(np)
                                .addOnSuccessListener(s ->
                                        Toast.makeText(this, "Password changed", Toast.LENGTH_SHORT).show()
                                )
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Wrong current password", Toast.LENGTH_SHORT).show()
                );
    }

    // -----------------------------------------------------
// DELETE ACCOUNT (SAFE MODE)
// -----------------------------------------------------
    private void confirmDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Delete account?")
                .setMessage("All your memories, tapes, transcripts, and settings will be deleted permanently.")
                .setPositiveButton("Delete", (d, w) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        LoadingOverlay.show(this);

        // Step 1: delete all user tapes
        firestore.collection("users")
                .document(userId)
                .collection("tapes")
                .get()
                .addOnSuccessListener(snaps -> {

                    // batch delete tapes
                    for (var doc : snaps) {
                        doc.getReference().delete();
                    }

                    // Step 2: delete user profile doc
                    firestore.collection("users")
                            .document(userId)
                            .delete()
                            .addOnSuccessListener(v -> {

                                // Step 3: delete firebase auth user
                                user.delete()
                                        .addOnSuccessListener(u -> {
                                            LoadingOverlay.hide();
                                            Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();

                                            // Step 4: go login page
                                            Intent i = new Intent(this, LoginActivity.class);
                                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(i);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            LoadingOverlay.hide();
                                            Toast.makeText(this, "Re-login required before deleting account", Toast.LENGTH_LONG).show();
                                        });

                            })
                            .addOnFailureListener(e -> {
                                LoadingOverlay.hide();
                                Toast.makeText(this, "Failed to delete user data", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    LoadingOverlay.hide();
                    Toast.makeText(this, "Failed to remove tapes", Toast.LENGTH_SHORT).show();
                });
    }

}
