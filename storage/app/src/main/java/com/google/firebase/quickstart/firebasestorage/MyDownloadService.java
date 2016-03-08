package com.google.firebase.quickstart.firebasestorage;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MyDownloadService extends Service {

    private static final String TAG = "MyDownloadService";

    /** Actions **/
    public static final String ACTION_DOWNLOAD = "action_download";
    public static final String ACTION_COMPLETED = "action_completed";
    public static final String ACTION_ERROR = "action_error";

    /** Extras **/
    public static final String EXTRA_DOWNLOAD_PATH = "extra_download_path";
    public static final String EXTRA_BYTES_DOWNLOADED = "extra_bytes_downloaded";

    private StorageReference mStorage;
    private int mNumTasks = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this, getString(R.string.google_app_id),
                new FirebaseOptions(getString(R.string.google_api_key)));

        // Initialize Storage
        String bucketName = "gs://" + getString(R.string.project_id) + ".storage.firebase.com";
        mStorage = FirebaseStorage.getInstance().getReference(bucketName);
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
            mStorage.getChild(downloadPath).getBytes(Long.MAX_VALUE).setResultCallback(
                    new ResultCallback<StorageReference.ByteResult>() {
                        @Override
                        public void onResult(@NonNull StorageReference.ByteResult result) {
                            Log.d(TAG, "getBytes:onResult:" + result.getStatus());
                            if (result.getStatus().isSuccess()) {
                                // Get downloaded bytes
                                byte[] bytes = result.getBytes();

                                // Send success broadcast with number of bytes downloaded
                                Intent broadcast = new Intent(ACTION_COMPLETED);
                                broadcast.putExtra(EXTRA_DOWNLOAD_PATH, downloadPath);
                                broadcast.putExtra(EXTRA_BYTES_DOWNLOADED, bytes.length);
                                LocalBroadcastManager.getInstance(getApplicationContext())
                                        .sendBroadcast(broadcast);

                                // Mark task completed
                                taskCompleted();
                            } else {
                                // Send failure broadcast
                                Intent broadcast = new Intent(ACTION_ERROR);
                                broadcast.putExtra(EXTRA_DOWNLOAD_PATH, downloadPath);
                                LocalBroadcastManager.getInstance(getApplicationContext())
                                        .sendBroadcast(broadcast);

                                // Mark task completed
                                taskCompleted();
                            }
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
