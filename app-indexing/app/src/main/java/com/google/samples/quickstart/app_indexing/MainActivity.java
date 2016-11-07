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
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
// [START import_classes]
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseAppIndexingInvalidArgumentException;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Actions;
// [END import_classes]

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    // Define a title for your current page, shown in autocompletion UI
    private static final String TITLE = "Sample Article";
    private String articleId;

    // [START handle_intent]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // [START_EXCLUDE]
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // [END_EXCLUDE]
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
            final Uri BASE_URL = Uri.parse("http://www.example.com/articles/");
            final String APP_URI = BASE_URL.buildUpon().appendPath(articleId).build().toString();

            Indexable articleToIndex = new Indexable.Builder()
                    .setName(TITLE)
                    .setUrl(APP_URI)
                    .build();

            Task<Void> task = FirebaseAppIndex.getInstance().update(articleToIndex);

            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "App Indexing API: Successfully added " + TITLE + " to index");
                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e(TAG, "App Indexing API: Failed to add " + TITLE + " to index. " + exception.getMessage());
                }
            });

            try {
                FirebaseUserActions.getInstance().start(Actions.newView(TITLE, APP_URI));
            } catch (FirebaseAppIndexingInvalidArgumentException exception) {
                Log.e(TAG, "App Indexing API: " + exception.getMessage());
            }
        }
    }

    @Override
    public void onStop(){
        super.onStop();

        if (articleId != null) {
            final Uri BASE_URL = Uri.parse("http://www.example.com/articles/");
            final String APP_URI = BASE_URL.buildUpon().appendPath(articleId).build().toString();

            try {
                FirebaseUserActions.getInstance().end(Actions.newView(TITLE, APP_URI));
            } catch (FirebaseAppIndexingInvalidArgumentException exception) {
                Log.e(TAG, "App Indexing API: " + exception.getMessage());
            }
        }
    }
    // [END app_indexing_view]
}
