package com.example.finalproject.ui.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

import com.example.finalproject.R;
import com.example.finalproject.adapters.CalendarTapeAdapter;
import com.example.finalproject.adapters.TapeShelfAdapter;
import com.example.finalproject.models.CalendarDay;
import com.example.finalproject.models.Tape;
import com.example.finalproject.ui.addtape.AddTapeDialog;
import com.example.finalproject.ui.profile.ProfileActivity;
import com.example.finalproject.ui.tapedetail.TapeDetailActivity;
import com.example.finalproject.utils.LoadingOverlay;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvTapeCalendar, rvTapeShelf;
    private TextView tvMonthYear, tvSelectedDateTitle, tvGreeting;
    private ImageButton btnPrevMonth, btnNextMonth;
    private ImageView ivProfile;

    // mini player
    private TextView tvPlayerTitle;
    private View viewPlayerColor;
    private ImageView btnMiniPlay, btnMiniStop;
    private MediaPlayer miniPlayer = null;
    private String currentAudioUrl = null;

    // adapters
    private CalendarTapeAdapter calendarAdapter;
    private TapeShelfAdapter shelfAdapter;
    private final List<Tape> shelfList = new ArrayList<>();

    // selected date
    private int currentYear, currentMonth, selectedDay;

    private static final String TAG = "HOME";


    // ------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupMiniPlayer();
        setupDate();
        setupCalendarRecycler();
        setupTapeShelfRecycler();
        loadCallName();
        updateCalendar();

        // goto profile
        ivProfile.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class))
        );


// add new tape (ALWAYS TODAY)
        FloatingActionButton fab = findViewById(R.id.fabAddTape);
        fab.setOnClickListener(v -> {

            // SELALU tanggal hari ini
            Calendar today = Calendar.getInstance();
            int yearToday  = today.get(Calendar.YEAR);
            int monthToday = today.get(Calendar.MONTH);
            int dayToday   = today.get(Calendar.DAY_OF_MONTH);

            AddTapeDialog dialog = AddTapeDialog.newInstance(
                    yearToday,
                    monthToday,
                    dayToday
            );

            // CALLBACK tetap memakai tanggal yang sedang dipilih user di kalender
            dialog.setOnTapeAddedListener(() -> {
                loadTapesForDate(currentYear, currentMonth, selectedDay);
                loadMonthTapeIndicators(currentYear, currentMonth);
            });

            dialog.show(getSupportFragmentManager(), "AddTapeDialog");
        });



        btnPrevMonth.setOnClickListener(v -> moveMonth(-1));
        btnNextMonth.setOnClickListener(v -> moveMonth(+1));

        btnMiniPlay.setOnClickListener(v -> playMiniPlayer());
        btnMiniStop.setOnClickListener(v -> stopMiniPlayer());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTapesForDate(currentYear, currentMonth, selectedDay);
        loadMonthTapeIndicators(currentYear, currentMonth);
    }

    // ------------------------------------------------------------
    private void initViews() {
        rvTapeCalendar = findViewById(R.id.rvTapeCalendar);
        rvTapeShelf = findViewById(R.id.rvTapeShelf);

        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvSelectedDateTitle = findViewById(R.id.tvSelectedDateTitle);
        tvGreeting = findViewById(R.id.tvGreeting);

        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        ivProfile = findViewById(R.id.ivProfile);

        // mini player
        tvPlayerTitle = findViewById(R.id.tvMiniPlayerTapeTitle);
        viewPlayerColor = findViewById(R.id.viewMiniTapeColor);
        btnMiniPlay = findViewById(R.id.btnMiniPlay);
        btnMiniStop = findViewById(R.id.btnMiniStop);
    }


    // ------------------------------------------------------------
    // MINI PLAYER LOGIC
    // ------------------------------------------------------------
    private void setupMiniPlayer() {
        tvPlayerTitle.setText("No tape selected");
        viewPlayerColor.setBackgroundColor(0xFFEAEAEA);
    }

    private void updateMiniPlayer(Tape tape) {
        tvPlayerTitle.setText(tape.title);
        viewPlayerColor.setBackgroundColor(tape.color);

        currentAudioUrl = tape.audioUrl;

        if (miniPlayer != null) {
            miniPlayer.release();
            miniPlayer = null;
        }
    }

    private void playMiniPlayer() {
        if (currentAudioUrl == null || currentAudioUrl.isEmpty()) {
            tvPlayerTitle.setText("No audio");
            return;
        }

        try {
            if (miniPlayer == null) {
                miniPlayer = new MediaPlayer();
                miniPlayer.setDataSource(currentAudioUrl);

                miniPlayer.setOnPreparedListener(mp -> {
                    miniPlayer.start();
                    btnMiniPlay.setImageResource(R.drawable.ic_pause);
                    tvPlayerTitle.setText("Playing...");
                });

                miniPlayer.setOnCompletionListener(mp -> stopMiniPlayer());
                miniPlayer.prepareAsync();

            } else {
                miniPlayer.start();
                btnMiniPlay.setImageResource(R.drawable.ic_pause);
                tvPlayerTitle.setText("Playing...");
            }

        } catch (Exception e) {
            tvPlayerTitle.setText("Cannot play");
            Log.e(TAG, "MiniPlayer: " + e.getMessage());
        }
    }

    private void stopMiniPlayer() {
        if (miniPlayer != null && miniPlayer.isPlaying()) {
            miniPlayer.pause();
        }
        btnMiniPlay.setImageResource(R.drawable.ic_play);
        tvPlayerTitle.setText("Paused");
    }


    // ------------------------------------------------------------
    // GREETING
    // ------------------------------------------------------------
    private void loadCallName() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String callName = doc.getString("callName");
                        if (callName != null)
                            tvGreeting.setText("Hello, " + callName + "!");
                    }
                });
    }


    // ------------------------------------------------------------
    // CALENDAR
    // ------------------------------------------------------------
    private void setupDate() {
        Calendar now = Calendar.getInstance();
        currentYear = now.get(Calendar.YEAR);
        currentMonth = now.get(Calendar.MONTH);
        selectedDay = now.get(Calendar.DAY_OF_MONTH);
    }

    private void setupCalendarRecycler() {
        rvTapeCalendar.setLayoutManager(new GridLayoutManager(this, 7));
    }

    private List<CalendarDay> generateMonth(int year, int month) {
        List<CalendarDay> list = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);

        int max = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int offset = cal.get(Calendar.DAY_OF_WEEK) - 1;

        for (int i = 0; i < offset; i++) list.add(new CalendarDay(0, false));
        for (int d = 1; d <= max; d++) list.add(new CalendarDay(d, true));

        return list;
    }

    private void updateCalendar() {
        String monthName = new DateFormatSymbols().getMonths()[currentMonth];
        tvMonthYear.setText(monthName + " " + currentYear);

        tvSelectedDateTitle.setText(
                "Memories on " + selectedDay + " " + monthName + " " + currentYear
        );

        List<CalendarDay> days = generateMonth(currentYear, currentMonth);

        calendarAdapter = new CalendarTapeAdapter(days, selectedDay, clickedDay -> {
            if (clickedDay <= 0) return;

            selectedDay = clickedDay;

            tvSelectedDateTitle.setText(
                    "Memories on " + clickedDay + " " + monthName + " " + currentYear
            );

            loadTapesForDate(currentYear, currentMonth, selectedDay);
        });

        rvTapeCalendar.setAdapter(calendarAdapter);
        loadTapesForDate(currentYear, currentMonth, selectedDay);
        loadMonthTapeIndicators(currentYear, currentMonth);
    }

    private void loadMonthTapeIndicators(int year, int month) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();

        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(year, month, maxDay, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long end = cal.getTimeInMillis();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("tapes")
                .whereGreaterThanOrEqualTo("timestamp", start)
                .whereLessThanOrEqualTo("timestamp", end)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Map<Integer, Integer> dayColors = new HashMap<>();
                    Calendar docCal = Calendar.getInstance();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Long ts = doc.getLong("timestamp");
                        Long colorLong = doc.getLong("color");
                        int color = colorLong != null ? colorLong.intValue() : 0xFFEEC373;

                        if (ts != null) {
                            docCal.setTimeInMillis(ts);
                            if (docCal.get(Calendar.YEAR) == year && docCal.get(Calendar.MONTH) == month) {
                                int day = docCal.get(Calendar.DAY_OF_MONTH);
                                dayColors.put(day, color);
                            }
                        }
                    }

                    if (calendarAdapter != null) {
                        calendarAdapter.setTapeColors(dayColors);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Load month tape indicators failed: " + e.getMessage());
                });
    }

    private void moveMonth(int delta) {
        currentMonth += delta;

        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }

        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }

        selectedDay = 1;
        updateCalendar();
    }


    // ------------------------------------------------------------
    // TAPE LIST
    // ------------------------------------------------------------
    private void setupTapeShelfRecycler() {
        rvTapeShelf.setLayoutManager(new LinearLayoutManager(this));

        shelfAdapter = new TapeShelfAdapter(
                shelfList,
                this::updateMiniPlayer,
                this::openTapeDetail
        );

        rvTapeShelf.setAdapter(shelfAdapter);
    }

    private void openTapeDetail(Tape tape) {
        Intent i = new Intent(this, TapeDetailActivity.class);
        i.putExtra("tapeId", tape.id);
        startActivity(i);
    }

    private void loadTapesForDate(int year, int month, int day) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            shelfList.clear();
            shelfAdapter.notifyDataSetChanged();
            return;
        }

        LoadingOverlay.show(this);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);

        // FIX PENTING: set start hari ke 00:00:00
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();

        // END hari ke 23:59:59
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        long end = cal.getTimeInMillis();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("tapes")
                .whereGreaterThanOrEqualTo("timestamp", start)
                .whereLessThan("timestamp", end)
                .get()
                .addOnSuccessListener(snapshot -> {
                    LoadingOverlay.hide();

                    shelfList.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        String id = doc.getId();
                        String caption = doc.getString("caption");
                        String date = doc.getString("date");
                        String audioUrl = doc.getString("audioUrl");
                        Long colorLong = doc.getLong("color");

                        int color = colorLong != null ? colorLong.intValue() : 0xFFEEC373;

                        Tape t = new Tape(id, caption, date, color);
                        t.audioUrl = audioUrl;

                        shelfList.add(t);
                    }

                    shelfAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    LoadingOverlay.hide();
                    Log.e(TAG, "Load tapes failed: " + e.getMessage());
                });
    }

    private void releaseMiniPlayer() {
        if (miniPlayer != null) {
            try {
                if (miniPlayer.isPlaying()) {
                    miniPlayer.stop();
                }
                miniPlayer.release();
            } catch (Exception ignored) {}
            miniPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopMiniPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMiniPlayer();
    }
}
