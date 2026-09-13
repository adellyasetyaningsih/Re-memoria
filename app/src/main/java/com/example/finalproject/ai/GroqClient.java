package com.example.finalproject.ai;

import com.example.finalproject.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroqClient {

    private static final String BASE_URL = "https://api.groq.com/openai/v1/";
    private static final OkHttpClient client = new OkHttpClient();

    private static final String API_KEY = BuildConfig.GROQ_API_KEY;

    public interface GroqCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public static void transcribeAudio(String audioPath, GroqCallback callback) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            callback.onError("GROQ_API_KEY missing");
            return;
        }

        File audioFile = new File(audioPath);
        if (!audioFile.exists()) {
            callback.onError("Audio file not found: " + audioPath);
            return;
        }

        RequestBody fileBody = RequestBody.create(
                audioFile,
                MediaType.parse("audio/mp4")
        );

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", audioFile.getName(), fileBody)
                .addFormDataPart("model", "whisper-large-v3")
                .addFormDataPart("response_format", "json")
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "audio/transcriptions")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Transcribe error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (response) {
                    String raw = response.body() != null ? response.body().string() : "";

                    if (!response.isSuccessful()) {
                        callback.onError("Transcribe failed (HTTP " + response.code() + "): " + raw);
                        return;
                    }

                    JSONObject obj = new JSONObject(raw);
                    String text = obj.optString("text", "").trim();

                    if (text.isEmpty()) {
                        callback.onError("Transcript is empty");
                    } else {
                        callback.onSuccess(text);
                    }
                } catch (Exception e) {
                    callback.onError("Parse error: " + e.getMessage());
                }
            }
        });
    }

    public static void generateInsight(String transcript, GroqCallback callback) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            callback.onError("GROQ_API_KEY missing");
            return;
        }
        if (transcript == null || transcript.trim().isEmpty()) {
            callback.onError("Transcript is empty");
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("model", "openai/gpt-oss-20b");
            json.put("temperature", 0.3);

            JSONArray messages = new JSONArray();
            JSONObject user = new JSONObject();
            user.put("role", "user");
            user.put("content",
                    "Analyze this diary transcript empathetically:\n\n"
                            + transcript +
                            "\n\nReturn JSON ONLY with this structure (do not wrap in markdown or backticks):\n" +
                            "{\n" +
                            "  \"emotion\": \"main emotion\",\n" +
                            "  \"summary\": \"short emotional summary\",\n" +
                            "  \"topics\": [\"topic1\", \"topic2\"],\n" +
                            "  \"advice\": \"gentle helpful advice\"\n" +
                            "}"
            );

            messages.put(user);
            json.put("messages", messages);

        } catch (Exception e) {
            callback.onError("JSON build error: " + e.getMessage());
            return;
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "chat/completions")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(RequestBody.create(
                        json.toString(),
                        MediaType.parse("application/json")
                ))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Insight error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (response) {
                    String raw = response.body() != null ? response.body().string() : "";

                    if (!response.isSuccessful()) {
                        callback.onError("Insight failed (HTTP " + response.code() + "): " + raw);
                        return;
                    }

                    JSONObject obj = new JSONObject(raw);
                    JSONArray choices = obj.getJSONArray("choices");
                    String content = choices.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                            .trim();

                    // Strip markdown backticks if model returned ```json ... ```
                    if (content.startsWith("```json")) {
                        content = content.substring(7);
                    } else if (content.startsWith("```")) {
                        content = content.substring(3);
                    }
                    if (content.endsWith("```")) {
                        content = content.substring(0, content.length() - 3);
                    }
                    content = content.trim();

                    callback.onSuccess(content);

                } catch (Exception e) {
                    callback.onError("Insight parse error: " + e.getMessage());
                }
            }
        });
    }
}
