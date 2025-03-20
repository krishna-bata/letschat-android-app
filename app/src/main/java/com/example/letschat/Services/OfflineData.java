package com.example.letschat.Services;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class OfflineData extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
