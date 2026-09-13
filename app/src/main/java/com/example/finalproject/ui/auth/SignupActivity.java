package com.example.finalproject.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.example.finalproject.firebase.AuthManager;
import com.example.finalproject.ui.onboarding.OnboardingActivity;
import com.example.finalproject.utils.LoadingOverlay;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_signup);

        authManager = new AuthManager();

        EditText etEmail = findViewById(R.id.etSignupEmail);
        EditText etPassword = findViewById(R.id.etSignupPassword);
        EditText etConfirm = findViewById(R.id.etSignupConfirm);
        Button btnSignup = findViewById(R.id.btnSignup);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        View layoutGoLogin = findViewById(R.id.layoutGoLogin);
        if (layoutGoLogin != null) {
            layoutGoLogin.setOnClickListener(v -> {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            });
        } else if (tvGoLogin != null) {
            tvGoLogin.setOnClickListener(v -> {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            });
        }

        btnSignup.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            String confirm = etConfirm.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pass.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            LoadingOverlay.show(this);

            authManager.signup(email, pass, task -> {
                LoadingOverlay.hide();
                if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                    String userId = task.getResult().getUser().getUid();

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("callName", "");
                    data.put("goal", "");
                    data.put("reminderTime", "");
                    data.put("about", "");
                    data.put("onboardingDone", false);

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .set(data);

                    startActivity(new Intent(SignupActivity.this, OnboardingActivity.class));
                    finish();
                } else {
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                    Toast.makeText(this, "Signup failed: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
