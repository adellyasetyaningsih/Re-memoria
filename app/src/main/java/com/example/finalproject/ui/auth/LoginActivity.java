package com.example.finalproject.ui.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.example.finalproject.firebase.AuthManager;
import com.example.finalproject.ui.home.HomeActivity;
import com.example.finalproject.ui.onboarding.OnboardingActivity;
import com.example.finalproject.utils.LoadingOverlay;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_login);

        authManager = new AuthManager();

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoSignup = findViewById(R.id.tvGoSignup);
        TextView tvForgot = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            LoadingOverlay.show(this);

            authManager.login(email, password, task -> {
                if (task.isSuccessful() && authManager.getCurrentUser() != null) {
                    String userId = authManager.getCurrentUser().getUid();

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .get()
                            .addOnSuccessListener(doc -> {
                                LoadingOverlay.hide();
                                Boolean done = doc.getBoolean("onboardingDone");
                                if (Boolean.TRUE.equals(done)) {
                                    startActivity(new Intent(this, HomeActivity.class));
                                } else {
                                    startActivity(new Intent(this, OnboardingActivity.class));
                                }
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                LoadingOverlay.hide();
                                Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                } else {
                    LoadingOverlay.hide();
                    String error = task.getException() != null ? task.getException().getMessage() : "Incorrect email or password";
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        View layoutGoSignup = findViewById(R.id.layoutGoSignup);
        if (layoutGoSignup != null) {
            layoutGoSignup.setOnClickListener(v ->
                    startActivity(new Intent(this, SignupActivity.class))
            );
        } else if (tvGoSignup != null) {
            tvGoSignup.setOnClickListener(v ->
                    startActivity(new Intent(this, SignupActivity.class))
            );
        }

        tvForgot.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        final EditText input = new EditText(this);
        input.setHint("Enter your email");
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            LoadingOverlay.show(this);

            FirebaseAuth.getInstance()
                    .fetchSignInMethodsForEmail(email)
                    .addOnSuccessListener(result -> {
                        if (result == null || result.getSignInMethods() == null || result.getSignInMethods().isEmpty()) {
                            LoadingOverlay.hide();
                            Toast.makeText(this, "Email not registered", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FirebaseAuth.getInstance()
                                .sendPasswordResetEmail(email)
                                .addOnSuccessListener(a -> {
                                    LoadingOverlay.hide();
                                    Toast.makeText(this, "Reset link sent! Check your inbox.", Toast.LENGTH_LONG).show();
                                })
                                .addOnFailureListener(e -> {
                                    LoadingOverlay.hide();
                                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        LoadingOverlay.hide();
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
