package com.google.samples.quickstart.appindexing.java;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import com.google.firebase.appindexing.FirebaseAppIndex;

import java.util.concurrent.ExecutionException;

public class AppIndexingService extends IntentService {

    public AppIndexingService() {
        super("AppIndexingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Task<Void> setStickersTask = AppIndexingUtil.setStickers(getApplicationContext(), FirebaseAppIndex.getInstance());
        if (setStickersTask != null) {
           try {
               Tasks.await(setStickersTask); 
           } catch (ExecutionException e) {
               // setStickersTask failed 
           } catch (InterruptedException e) {
               // this thread was interrupted while waiting for setStickersTask to complete
           }
        }
    }
}
