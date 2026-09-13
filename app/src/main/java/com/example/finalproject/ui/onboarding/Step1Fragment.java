package com.example.finalproject.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalproject.R;

public class Step1Fragment extends Fragment {

    private EditText etCallName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.onboarding_step1, container, false);

        etCallName = view.findViewById(R.id.etCallName);
        Button btnNext = view.findViewById(R.id.btnNext1);
        ImageView btnBack = view.findViewById(R.id.btnBackOnboard1);

        // 🔙 BACK BUTTON — kembali ke SignupActivity
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // ➡️ NEXT STEP
        btnNext.setOnClickListener(v -> {
            String name = etCallName.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            OnboardingData.callName = name;

            ((OnboardingActivity) requireActivity()).nextPage();
        });

        return view;
    }
}
