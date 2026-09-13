package com.example.finalproject.ui.addtape;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.finalproject.R;
import com.example.finalproject.network.SupabaseUploader;
import com.example.finalproject.ai.GroqClient;
import com.example.finalproject.utils.LoadingOverlay;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class AddTapeDialog extends DialogFragment {

    public interface OnTapeAddedListener {
        void onTapeAdded();
    }

    private OnTapeAddedListener listener;
    public void setOnTapeAddedListener(OnTapeAddedListener l) { listener = l; }

    private static final String TAG = "ADD_TAPE";

    // (KEEPS BUT NOT USED FOR SAVING DATE ANYMORE)
    private int year, month, day;

    public static AddTapeDialog newInstance(int year, int month, int day) {
        AddTapeDialog d = new AddTapeDialog();
        Bundle b = new Bundle();
        b.putInt("year", year);
        b.putInt("month", month);
        b.putInt("day", day);
        d.setArguments(b);
        return d;
    }

    @Override
    public void onCreate(@Nullable Bundle saved) {
        super.onCreate(saved);
        if (getArguments() != null) {
            year = getArguments().getInt("year");
            month = getArguments().getInt("month");
            day = getArguments().getInt("day");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    // ==========================
    // UI
    // ==========================
    private View tapePreview;
    private TextView tvCaptionPreview, tvTimer, tvStatus, tvPreviewDuration;
    private EditText etCaption;
    private Button btnRecord, btnRetake;
    private ImageView btnClose, btnPlayPause;
    private View layoutPreviewPlayer;

    private MediaRecorder recorder;
    private MediaPlayer player;

    private boolean isRecording = false;
    private boolean isPlaying = false;

    private String audioPath = null;
    private long recordingTime = 0L;
    private CountDownTimer timer;

    private int selectedColor = 0xFFEEC373;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.add_tape_dialog, c, false);

        btnClose = v.findViewById(R.id.btnCloseDialog);
        tapePreview = v.findViewById(R.id.tapePreview);
        tvCaptionPreview = v.findViewById(R.id.tvTapeCaptionPreview);
        etCaption = v.findViewById(R.id.etTapeCaption);
        tvTimer = v.findViewById(R.id.tvTimer);
        tvStatus = v.findViewById(R.id.tvRecordStatus);
        btnRecord = v.findViewById(R.id.btnStartRecord);

        layoutPreviewPlayer = v.findViewById(R.id.layoutPreviewPlayer);
        btnPlayPause = v.findViewById(R.id.btnPlayPause);
        tvPreviewDuration = v.findViewById(R.id.tvPreviewDuration);
        btnRetake = v.findViewById(R.id.btnRetake);

        layoutPreviewPlayer.setVisibility(View.GONE);

        btnClose.setOnClickListener(view -> dismiss());

        etCaption.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                tvCaptionPreview.setText(s.toString());
            }
            public void afterTextChanged(android.text.Editable s) {}
        });

        int[] ids = { R.id.color1, R.id.color2, R.id.color3, R.id.color4, R.id.color5, R.id.color6 };
        for (int id : ids) {
            View cc = v.findViewById(id);
            cc.setOnClickListener(vv -> {
                selectedColor = ((android.graphics.drawable.ColorDrawable) cc.getBackground()).getColor();
                tapePreview.setBackgroundColor(selectedColor);
            });
        }

        btnRecord.setOnClickListener(view -> {
            if (layoutPreviewPlayer.getVisibility() == View.VISIBLE && !isRecording) {
                uploadTape();
                return;
            }
            if (isRecording) {
                stopRecording();
                showPreview();
                return;
            }
            if (etCaption.getText().toString().trim().isEmpty()) {
                Toast.makeText(getContext(), "Enter tape title first", Toast.LENGTH_SHORT).show();
                return;
            }
            startRecording();
        });

        btnPlayPause.setOnClickListener(vv -> {
            if (!isPlaying) startPlayback();
            else pausePlayback();
        });

        btnRetake.setOnClickListener(vv -> retakeAudio());

        return v;
    }

    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{ Manifest.permission.RECORD_AUDIO },
                2001);
    }

    // =============================
    // RECORDING
    // =============================
    private void startRecording() {

        if (!hasPermission()) {
            requestPermission();
            return;
        }

        stopPlayer();

        isRecording = true;
        btnRecord.setText("Stop Recording");
        tvStatus.setText("Recording...");

        recordingTime = 0;
        tvTimer.setText("00:00");

        File file = new File(requireContext().getExternalFilesDir(null),
                "tape_" + System.currentTimeMillis() + ".m4a");
        audioPath = file.getAbsolutePath();

        recorder = new MediaRecorder();
        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(128000);
            recorder.setAudioSamplingRate(44100);
            recorder.setOutputFile(audioPath);

            recorder.prepare();
            recorder.start();

        } catch (Exception e) {
            tvStatus.setText("Recording failed");
            Log.e(TAG, e.toString());
            isRecording = false;
            btnRecord.setText("Start Recording");
            return;
        }

        startTimer();
    }

    private void stopRecording() {
        if (!isRecording) return;

        isRecording = false;

        try { recorder.stop(); } catch (Exception ignored) {}
        try { recorder.release(); } catch (Exception ignored) {}
        recorder = null;

        btnRecord.setText("Save Tape");
        tvStatus.setText("Recording stopped");

        stopTimer();
    }

    private void startTimer() {
        timer = new CountDownTimer(3600000, 1000) {
            public void onTick(long m) {
                recordingTime += 1000;
                int mm = (int) (recordingTime / 60000);
                int ss = (int) ((recordingTime / 1000) % 60);
                tvTimer.setText(String.format("%02d:%02d", mm, ss));
            }
            public void onFinish() {}
        }.start();
    }

    private void stopTimer() { if (timer != null) timer.cancel(); }

    // =============================
    // PLAYBACK
    // =============================
    private void startPlayback() {
        if (audioPath == null) return;

        stopPlayer();

        try {
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(audioPath);
            player.prepare();
            player.start();

            isPlaying = true;
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            tvStatus.setText("Playing...");

            player.setOnCompletionListener(mp -> {
                pausePlayback();
                tvStatus.setText("Ready");
            });

        } catch (Exception e) {
            tvStatus.setText("Playback error");
        }
    }

    private void pausePlayback() {
        if (player != null && player.isPlaying()) {
            player.pause();
            isPlaying = false;
            btnPlayPause.setImageResource(R.drawable.ic_play);
        }
    }

    private void stopPlayer() {
        if (player != null) {
            try { player.stop(); } catch (Exception ignored) {}
            try { player.release(); } catch (Exception ignored) {}
            player = null;
        }
        isPlaying = false;
        if (btnPlayPause != null)
            btnPlayPause.setImageResource(R.drawable.ic_play);
    }

    // =============================
    // RETAKE
    // =============================
    private void retakeAudio() {
        stopPlayer();

        if (audioPath != null) {
            new File(audioPath).delete();
        }

        audioPath = null;
        tvTimer.setText("00:00");
        recordingTime = 0;
        tvStatus.setText("Ready");
        btnRecord.setText("Start Recording");

        layoutPreviewPlayer.setVisibility(View.GONE);
    }

    private void showPreview() {
        layoutPreviewPlayer.setVisibility(View.VISIBLE);
        tvPreviewDuration.setText(tvTimer.getText());
    }

    // =============================
    // UPLOAD + TRANSCRIBE + AI
    // =============================
    private void uploadTape() {

        File f = new File(audioPath);
        if (audioPath == null || !f.exists()) {
            Toast.makeText(getContext(), "No audio recorded", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRecord.setText("Uploading...");
        tvStatus.setText("Uploading audio...");

        LoadingOverlay.show(requireActivity());

        SupabaseUploader.uploadAudio(
                FirebaseAuth.getInstance().getUid(),
                f,
                new SupabaseUploader.UploadCallback() {
                    @Override public void onSuccess(String url) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            tvStatus.setText("Transcribing...");
                            startTranscribe(url);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            LoadingOverlay.hide();
                            tvStatus.setText("Upload failed");
                            btnRecord.setText("Save Tape");
                            Toast.makeText(getContext(), "Upload error: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void startTranscribe(String audioUrlOnline) {

        GroqClient.transcribeAudio(audioPath, new GroqClient.GroqCallback() {
            @Override public void onSuccess(String transcript) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    tvStatus.setText("Generating insights...");
                    startInsight(audioUrlOnline, transcript);
                });
            }

            @Override public void onError(String error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    Log.w(TAG, "Transcribe failed: " + error + ", saving audio anyway");
                    Toast.makeText(getContext(), "Transcribe issue. Saving audio...", Toast.LENGTH_SHORT).show();
                    saveToFirestore(audioUrlOnline, "", "");
                });
            }
        });
    }

    private void startInsight(String audioUrlOnline, String transcript) {

        GroqClient.generateInsight(transcript, new GroqClient.GroqCallback() {
            @Override public void onSuccess(String insightJson) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        saveToFirestore(audioUrlOnline, transcript, insightJson)
                );
            }

            @Override public void onError(String error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    Log.w(TAG, "Insight failed: " + error + ", saving without insight");
                    Toast.makeText(getContext(), "Insight issue. Saving tape...", Toast.LENGTH_SHORT).show();
                    saveToFirestore(audioUrlOnline, transcript, "");
                });
            }
        });
    }

    // =============================
    // SAVE TO FIRESTORE **FIXED**
    // =============================
    private void saveToFirestore(String audioUrl, String transcript, String insight) {

        String uid = FirebaseAuth.getInstance().getUid();
        String caption = etCaption.getText().toString().trim();

        // ALWAYS today's date fix ❤️
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        long ts = cal.getTimeInMillis();
        String dateStr = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(cal.getTime());

        Map<String, Object> map = new HashMap<>();
        map.put("caption", caption);
        map.put("color", selectedColor);
        map.put("audioUrl", audioUrl);
        map.put("durationMs", recordingTime);
        map.put("date", dateStr);
        map.put("timestamp", ts);
        map.put("transcript", transcript);
        map.put("insight", insight);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("tapes")
                .add(map)
                .addOnSuccessListener(d -> {
                    LoadingOverlay.hide();
                    tvStatus.setText("Saved!");
                    Toast.makeText(getContext(), "Memory saved with AI ❤️", Toast.LENGTH_SHORT).show();

                    if (listener != null) listener.onTapeAdded();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    LoadingOverlay.hide();
                    tvStatus.setText("Failed to save");
                    Toast.makeText(getContext(), "Firestore error", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimer();
        stopPlayer();
        if (recorder != null) recorder.release();
    }
}
