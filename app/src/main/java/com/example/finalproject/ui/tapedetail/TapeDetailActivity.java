package com.example.finalproject.ui.tapedetail;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalproject.R;
import com.example.finalproject.models.Tape;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class TapeDetailActivity extends AppCompatActivity {

    private static final String TAG = "TapeDetailActivity";

    // UI basic
    private ImageView btnBack, btnPlayPause;
    private View viewTapeColor;
    private TextView tvTitle, tvDate, tvDuration, tvStatus;

    // Transcript + AI
    private TextView tvTranscript;
    private View cardInsight;
    private TextView tvEmotion;
    private TextView tvSummary;
    private TextView tvTopics;
    private TextView tvAdvice;

    private Button btnDelete;

    private MediaPlayer player;
    private Tape tape;
    private String tapeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tape_detail);

        tapeId = getIntent().getStringExtra("tapeId");

        initViews();
        setupListeners();
        loadTapeData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnDelete = findViewById(R.id.btnDeleteTape);

        viewTapeColor = findViewById(R.id.tapePreviewDetail);

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvDate = findViewById(R.id.tvDetailDate);
        tvDuration = findViewById(R.id.tvDetailDuration);
        tvStatus = findViewById(R.id.tvPlayStatus);

        tvTranscript = findViewById(R.id.tvTranscript);

        cardInsight = findViewById(R.id.cardInsight);
        tvEmotion = findViewById(R.id.tvAiEmotion);
        tvSummary = findViewById(R.id.tvAiSummary);
        tvTopics = findViewById(R.id.tvAiTopics);
        tvAdvice = findViewById(R.id.tvAiAdvice);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPlayPause.setOnClickListener(v -> {
            if (tape == null || tape.audioUrl == null || tape.audioUrl.isEmpty()) {
                tvStatus.setText("No audio");
                return;
            }

            if (player == null) {
                startAudio();
            } else {
                toggleAudio();
            }
        });

        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadTapeData() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null || tapeId == null) {
            finish();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("tapes")
                .document(tapeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String title = doc.getString("caption");
                    String date = doc.getString("date");
                    Long colorLong = doc.getLong("color");
                    Long durationMs = doc.getLong("durationMs");
                    String audioUrl = doc.getString("audioUrl");

                    String transcript = doc.getString("transcript");
                    String insightJson = doc.getString("insight");

                    int color = (colorLong != null) ? colorLong.intValue() : 0xFFEEC373;

                    tape = new Tape(tapeId, title, date, color);
                    tape.audioUrl = audioUrl;
                    tape.durationMs = durationMs != null ? durationMs : 0;

                    tvTitle.setText(tape.title != null ? tape.title : "Untitled");
                    tvDate.setText(tape.date != null ? tape.date : "");
                    viewTapeColor.setBackgroundColor(color);

                    int sec = (int) (tape.durationMs / 1000);
                    tvDuration.setText(String.format(Locale.getDefault(), "Duration: %02d:%02d", sec / 60, sec % 60));

                    if (transcript != null && !transcript.trim().isEmpty()) {
                        tvTranscript.setText(transcript);
                    } else {
                        tvTranscript.setText("No transcript available for this tape.");
                    }

                    if (insightJson != null && !insightJson.trim().isEmpty()) {
                        bindInsight(insightJson);
                    } else {
                        cardInsight.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Load tape error: " + e.getMessage()));
    }

    private void bindInsight(String insightJson) {
        try {
            JSONObject obj = new JSONObject(insightJson);

            String emotion = obj.optString("emotion", "Unknown");
            String summary = obj.optString("summary", "");
            String advice = obj.optString("advice", "");
            JSONArray topicsArr = obj.optJSONArray("topics");

            StringBuilder topicsBuilder = new StringBuilder();
            if (topicsArr != null) {
                for (int i = 0; i < topicsArr.length(); i++) {
                    if (i > 0) topicsBuilder.append(" • ");
                    topicsBuilder.append(topicsArr.optString(i));
                }
            }

            cardInsight.setVisibility(View.VISIBLE);
            tvEmotion.setText(emotion);
            tvSummary.setText(summary.isEmpty() ? "-" : summary);
            tvTopics.setText(topicsBuilder.length() == 0 ? "No specific topics detected." : topicsBuilder.toString());
            tvAdvice.setText(advice.isEmpty() ? "-" : advice);

        } catch (Exception e) {
            Log.e(TAG, "Insight parse error: " + e.getMessage());
            cardInsight.setVisibility(View.GONE);
        }
    }

    private void startAudio() {
        if (tape == null || tape.audioUrl == null || tape.audioUrl.isEmpty()) return;

        releasePlayer();

        try {
            player = new MediaPlayer();
            player.setDataSource(tape.audioUrl);
            player.setOnPreparedListener(mp -> {
                player.start();
                btnPlayPause.setImageResource(R.drawable.ic_pause);
                tvStatus.setText("Playing...");
            });
            player.setOnCompletionListener(mp -> {
                btnPlayPause.setImageResource(R.drawable.ic_play);
                tvStatus.setText("Finished");
            });
            player.prepareAsync();

        } catch (Exception e) {
            Log.e(TAG, "Audio playback error: " + e.getMessage());
            tvStatus.setText("Cannot play audio");
        }
    }

    private void toggleAudio() {
        if (player == null) return;

        if (player.isPlaying()) {
            player.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play);
            tvStatus.setText("Paused");
        } else {
            player.start();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            tvStatus.setText("Playing...");
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Tape?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> deleteTape())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTape() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null || tapeId == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("tapes")
                .document(tapeId)
                .delete()
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Tape deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Delete error: " + e.getMessage()));
    }

    private void releasePlayer() {
        if (player != null) {
            try {
                if (player.isPlaying()) {
                    player.stop();
                }
                player.release();
            } catch (Exception ignored) {}
            player = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }
}
