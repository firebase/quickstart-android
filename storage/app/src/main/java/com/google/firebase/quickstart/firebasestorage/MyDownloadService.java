package com.google.firebase.quickstart.firebasestorage;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.storage.FirebaseStorage;

public class MyDownloadService extends Service {

    private static final String TAG = "MyDownloadService";

    /** Actions **/
    public static final String ACTION_DOWNLOAD = "action_download";
    public static final String ACTION_COMPLETED = "action_completed";
    public static final String ACTION_ERROR = "action_error";

    /** Extras **/
    public static final String EXTRA_DOWNLOAD_PATH = "extra_download_path";
    public static final String EXTRA_BYTES_DOWNLOADED = "extra_bytes_downloaded";

    private int mNumTasks = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this, getString(R.string.google_app_id),
                new FirebaseOptions(getString(R.string.google_api_key)));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);

        if (ACTION_DOWNLOAD.equals(intent.getAction())) {
            // Get the path to download from the intent
            final String downloadPath = intent.getStringExtra(EXTRA_DOWNLOAD_PATH);

            // Mark task started
            Log.d(TAG, ACTION_DOWNLOAD + ":" + downloadPath);
            taskStarted();

            // Download as bytes
            // TODO(samstern): Dry
            String bucketName = "gs://" + getString(R.string.project_id) + ".storage.firebase.com";
            FirebaseStorage storage = new FirebaseStorage(bucketName);
            storage.getChild(downloadPath).getBytes(new FirebaseStorage.SimpleDownloadCallback() {
                @Override
                protected void onComplete(@NonNull byte[] bytes) {
                    Log.d(TAG, "onComplete:" + downloadPath);

                    // Send success broadcast with number of bytes downloaded (for demonstration)
                    Intent result = new Intent(ACTION_COMPLETED);
                    result.putExtra(EXTRA_DOWNLOAD_PATH, downloadPath);
                    result.putExtra(EXTRA_BYTES_DOWNLOADED, bytes.length);
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(result);

                    // Mark task completed
                    taskCompleted();
                }

                @Override
                protected void onError(@NonNull Exception exception) {
                    Log.d(TAG, "onError:" + downloadPath);

                    // Send failure broadcast
                    Intent result = new Intent(ACTION_ERROR);
                    result.putExtra(EXTRA_DOWNLOAD_PATH, downloadPath);
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(result);

                    // Mark task completed
                    taskCompleted();
                }
            });
        }

        return START_REDELIVER_INTENT;
    }

    private void taskStarted() {
        changeNumberOfTasks(1);
    }

    private void taskCompleted() {
        changeNumberOfTasks(-1);
    }

    private synchronized void changeNumberOfTasks(int delta) {
        Log.d(TAG, "changeNumberOfTasks:" + mNumTasks + ":" + delta);
        mNumTasks += delta;

        // If there are no tasks left, stop the service
        if (mNumTasks <= 0) {
            Log.d(TAG, "stopping");
            stopSelf();
        }
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_COMPLETED);
        filter.addAction(ACTION_ERROR);

        return filter;
    }
}
