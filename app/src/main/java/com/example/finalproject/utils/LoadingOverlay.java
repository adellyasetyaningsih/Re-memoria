package com.example.finalproject.utils;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.finalproject.R;

import java.lang.ref.WeakReference;

public class LoadingOverlay {

    private static WeakReference<Activity> currentActivityRef = null;
    private static WeakReference<View> currentOverlayRef = null;

    public static synchronized void show(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        View currentView = (currentOverlayRef != null) ? currentOverlayRef.get() : null;
        Activity boundActivity = (currentActivityRef != null) ? currentActivityRef.get() : null;

        if (currentView != null && boundActivity == activity && currentView.getParent() != null) {
            currentView.setVisibility(View.VISIBLE);
            return;
        }

        // Clean up previous overlay if bound to another activity or detached
        hide();

        ViewGroup root = activity.findViewById(android.R.id.content);
        if (root == null) {
            return;
        }

        View newOverlay = LayoutInflater.from(activity).inflate(R.layout.view_loading_overlay, root, false);
        newOverlay.setVisibility(View.VISIBLE);
        root.addView(newOverlay);

        currentActivityRef = new WeakReference<>(activity);
        currentOverlayRef = new WeakReference<>(newOverlay);
    }

    public static synchronized void hide() {
        if (currentOverlayRef != null) {
            View overlayView = currentOverlayRef.get();
            if (overlayView != null) {
                if (overlayView.getParent() instanceof ViewGroup) {
                    ((ViewGroup) overlayView.getParent()).removeView(overlayView);
                } else {
                    overlayView.setVisibility(View.GONE);
                }
            }
            currentOverlayRef.clear();
        }
        if (currentActivityRef != null) {
            currentActivityRef.clear();
        }
    }
}
