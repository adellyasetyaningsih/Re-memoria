package com.example.finalproject.ui.onboarding;

import android.graphics.Color;
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
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

public class Step2Fragment extends Fragment {

    private final String[] goals = {
            "Daily Memory Tracking",
            "Mental Health Reflection",
            "Self-Growth Progress",
            "Free Voice Diary",
            "Stress Management",
            "Positivity Building"
    };

    private final List<String> selectedGoals = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.onboarding_step2, container, false);

        FlexboxLayout layoutGoals = v.findViewById(R.id.layoutGoals);
        Button btnNext = v.findViewById(R.id.btnNext2);

        generateGoalChips(layoutGoals);

        btnNext.setOnClickListener(view -> {
            OnboardingData.goals = new ArrayList<>(selectedGoals);
            ((OnboardingActivity) requireActivity()).nextPage();
        });

        return v;
    }

    private void generateGoalChips(FlexboxLayout layout) {
        layout.removeAllViews();

        for (String g : goals) {

            TextView chip = new TextView(getContext());
            chip.setText(g);
            chip.setTextSize(14);
            chip.setTextColor(Color.parseColor("#3C2F2F"));
            chip.setBackground(requireContext().getDrawable(R.drawable.chip_unselected));

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(10, 10, 10, 10);
            chip.setLayoutParams(params);
            chip.setPadding(45, 25, 45, 25);

            chip.setOnClickListener(v -> toggleChip(g, chip));

            layout.addView(chip);
        }
    }

    private void toggleChip(String g, TextView chip) {

        if (selectedGoals.contains(g)) {
            selectedGoals.remove(g);
            chip.setBackground(requireContext().getDrawable(R.drawable.chip_unselected));
        } else {
            if (selectedGoals.size() >= 3) {
                Toast.makeText(getContext(), "Choose max 3 goals", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedGoals.add(g);
            chip.setBackground(requireContext().getDrawable(R.drawable.chip_selected));
        }
    }
}
