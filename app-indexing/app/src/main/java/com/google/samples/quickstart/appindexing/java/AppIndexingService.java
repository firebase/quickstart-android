package com.google.samples.quickstart.appindexing.java;

import android.app.IntentService;
import android.content.Intent;

import com.google.firebase.appindexing.FirebaseAppIndex;

public class AppIndexingService extends IntentService {

    public AppIndexingService() {
        super("AppIndexingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppIndexingUtil.setStickers(getApplicationContext(), FirebaseAppIndex.getInstance());
    }
}
