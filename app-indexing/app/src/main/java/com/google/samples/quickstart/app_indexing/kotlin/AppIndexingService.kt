package com.google.samples.quickstart.app_indexing.kotlin

import android.app.IntentService
import android.content.Intent
import com.google.firebase.appindexing.FirebaseAppIndex


class AppIndexingService : IntentService("AppIndexingService") {

    override fun onHandleIntent(intent: Intent?) {
        AppIndexingUtil.setStickers(applicationContext, FirebaseAppIndex.getInstance())
    }
}