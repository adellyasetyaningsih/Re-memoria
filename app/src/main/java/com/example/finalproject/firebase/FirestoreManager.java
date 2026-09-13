package com.example.finalproject.firebase;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class FirestoreManager {

    private final FirebaseFirestore db;

    public FirestoreManager() {
        this.db = FirebaseFirestore.getInstance();
    }

    // Save user profile / onboarding data
    public void saveUser(String uid, Map<String, Object> data, OnCompleteListener<Void> listener) {
        db.collection("users")
                .document(uid)
                .set(data)
                .addOnCompleteListener(listener);
    }

    // Save tape metadata
    public void saveTape(String uid, Map<String, Object> data, OnCompleteListener<DocumentReference> listener) {
        db.collection("users")
                .document(uid)
                .collection("tapes")
                .add(data)
                .addOnCompleteListener(listener);
    }

    // Get all tapes for user
    public void getAllTapes(String uid, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("users")
                .document(uid)
                .collection("tapes")
                .get()
                .addOnCompleteListener(listener);
    }

    // Get tapes by specific date
    public void getTapesByDate(String uid, String date, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("users")
                .document(uid)
                .collection("tapes")
                .whereEqualTo("date", date)
                .get()
                .addOnCompleteListener(listener);
    }

    // Update user profile fields
    public void updateUser(String uid, Map<String, Object> updates, OnCompleteListener<Void> listener) {
        db.collection("users")
                .document(uid)
                .update(updates)
                .addOnCompleteListener(listener);
    }
}
