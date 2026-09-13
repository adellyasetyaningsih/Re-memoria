package com.example.finalproject.firebase;


import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {

    private final FirebaseAuth auth;

    public AuthManager() {
        auth = FirebaseAuth.getInstance();
    }

    // REGISTER USER
    public void signup(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    // LOGIN USER
    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    // GET CURRENT USER
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    // CHECK IF USER LOGGED IN
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    // LOGOUT
    public void logout() {
        auth.signOut();
    }
}

