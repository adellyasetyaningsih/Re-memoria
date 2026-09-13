package com.example.finalproject;

import android.app.Application;

import com.example.finalproject.utils.ThemeHelper;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeHelper.applyTheme(this);
    }
}
