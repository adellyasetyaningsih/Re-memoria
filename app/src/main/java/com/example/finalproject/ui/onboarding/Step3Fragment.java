package com.example.finalproject.ui.onboarding;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalproject.R;

import java.util.Locale;

public class Step3Fragment extends Fragment {

    private TextView tvSelectedTime;
    private String selectedTime = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.onboarding_step3, container, false);

        Button btnPickTime = view.findViewById(R.id.btnPickTime);
        Button btnNext = view.findViewById(R.id.btnNext3);
        tvSelectedTime = view.findViewById(R.id.tvSelectedTime);

        btnPickTime.setOnClickListener(v -> openTimePicker());

        btnNext.setOnClickListener(v -> {
            if (selectedTime == null) {
                Toast.makeText(requireContext(), "Please select a time", Toast.LENGTH_SHORT).show();
                return;
            }

            // Simpan
            OnboardingData.reminderTime = selectedTime;

            // Next Page
            ((OnboardingActivity) requireActivity()).nextPage();
        });

        return view;
    }

    private void openTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hour, minute) -> {

                    selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                    tvSelectedTime.setText(selectedTime);

                }, 8, 0, true // default jam 08:00
        );

        dialog.show();
    }
}
