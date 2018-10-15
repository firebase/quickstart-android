package com.google.samples.quickstart.appindexing.kotlin

import android.app.IntentService
import android.content.Intent
import com.google.firebase.appindexing.FirebaseAppIndex

class AppIndexingService : IntentService("AppIndexingService") {

    override fun onHandleIntent(intent: Intent?) {
        AppIndexingUtil.setStickers(applicationContext, FirebaseAppIndex.getInstance())
    }
}
