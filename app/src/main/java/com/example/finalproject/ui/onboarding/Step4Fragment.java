package com.example.finalproject.ui.onboarding;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalproject.R;

public class Step4Fragment extends Fragment {

    private EditText etAbout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.onboarding_step4, container, false);

        etAbout = view.findViewById(R.id.etAbout);
        Button btnFinish = view.findViewById(R.id.btnFinish);

        btnFinish.setOnClickListener(v -> {

            Log.d("CHECK", "FINISH BUTTON CLICKED");   // LOG 1

            String about = etAbout.getText().toString().trim();

            if (about.isEmpty()) {
                Toast.makeText(requireContext(), "Please write something", Toast.LENGTH_SHORT).show();
                return;
            }

            OnboardingData.about = about;

            if (getActivity() instanceof OnboardingActivity) {
                ((OnboardingActivity) getActivity()).finishOnboarding();
            }
        });

        return view;
    }
}
