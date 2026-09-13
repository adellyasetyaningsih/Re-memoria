package com.example.finalproject.ui.onboarding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class OnboardingPagerAdapter extends FragmentStateAdapter {

    public OnboardingPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new Step1Fragment();
            case 1: return new Step2Fragment();
            case 2: return new Step3Fragment();
            case 3: return new Step4Fragment();
            default: return new Step1Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
