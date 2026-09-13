package com.example.finalproject.network;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.example.finalproject.BuildConfig;

public class SupabaseUploader {

    private static final String TAG = "SupabaseUploader";
    private static final OkHttpClient client = new OkHttpClient();

    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_KEY = BuildConfig.SUPABASE_KEY;
    private static final String BUCKET = "audio";

    public interface UploadCallback {
        void onSuccess(String url);
        void onError(String error);
    }

    public static void uploadAudio(String userId, File file, UploadCallback callback) {
        if (file == null || !file.exists()) {
            callback.onError("Audio file does not exist");
            return;
        }

        String path = "tapes/" + userId + "/" + file.getName();

        try {
            Log.d(TAG, "Uploading audio: " + file.getAbsolutePath() + " (" + file.length() + " bytes)");

            RequestBody requestBody = RequestBody.create(
                    file,
                    MediaType.parse("audio/m4a")
            );

            Request request = new Request.Builder()
                    .url(SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + path)
                    .header("apikey", SUPABASE_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_KEY)
                    .header("Content-Type", "audio/m4a")
                    .header("x-upsert", "true")
                    .put(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Upload request failed", e);
                    callback.onError("Upload failed: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (response) {
                        String responseBody = response.body() != null
                                ? response.body().string()
                                : "";

                        if (response.isSuccessful()) {
                            String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET + "/" + path;
                            Log.d(TAG, "Upload success. Public URL: " + publicUrl);
                            callback.onSuccess(publicUrl);
                        } else {
                            Log.e(TAG, "Upload failed with status code " + response.code() + ": " + responseBody);
                            callback.onError("Upload failed (HTTP " + response.code() + "): " + response.message());
                        }
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Unexpected upload exception", e);
            callback.onError("Unexpected error: " + e.getMessage());
        }
    }
}
