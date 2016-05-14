/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.quickstart.app_indexing;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

// [START import_classes]
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
// [END import_classes]
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private GoogleApiClient mClient;
    private String articleId;
    // Define a title for your current page, shown in autocompletion UI
    private static final String TITLE = "Sample Article";

    // [START handle_intent]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // [START_EXCLUDE]
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // [END_EXCLUDE]
        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        onNewIntent(getIntent());
    }

    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        Uri data = intent.getData();
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            articleId = data.getLastPathSegment();
            TextView linkText = (TextView)findViewById(R.id.link);
            linkText.setText(data.toString());
        }
    }
    // [END handle_intent]

    // [START app_indexing_view]
    @Override
    public void onStart(){
        super.onStart();

        if (articleId != null) {
            // Connect your client
            mClient.connect();

            final Uri BASE_URL = Uri.parse("http://www.example.com/articles/");
            final Uri APP_URI = BASE_URL.buildUpon().appendPath(articleId).build();

            Action viewAction = Action.newAction(Action.TYPE_VIEW, TITLE, APP_URI);

            // Call the App Indexing API view method
            PendingResult<Status> result = AppIndex.AppIndexApi.start(mClient, viewAction);

            result.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.d(TAG, "App Indexing API: Indexed page view successfully.");
                    } else {
                        Log.e(TAG, "App Indexing API: There was an error indexing the page view."
                                + status.toString());
                    }
                }
            });
        }
    }

    @Override
    public void onStop(){
        super.onStop();

        if (articleId != null) {
            final Uri BASE_URL = Uri.parse("http://www.example.com/articles/");
            final Uri APP_URI = BASE_URL.buildUpon().appendPath(articleId).build();

            Action viewAction = Action.newAction(Action.TYPE_VIEW, TITLE, APP_URI);
            PendingResult<Status> result = AppIndex.AppIndexApi.end(mClient, viewAction);

            result.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    if (status.isSuccess()) {
                        Log.d(TAG, "App Indexing API: Indexed recipe view end successfully.");
                    } else {
                        Log.e(TAG, "App Indexing API: There was an error indexing the recipe view."
                                + status.toString());
                    }
                }
            });

            mClient.disconnect();
        }
    }
    // [END app_indexing_view]

}
