/*
 * Copyright (C) The Android Open Source Project
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

package com.google.firebase.quickstart.invites;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Activity for displaying information about a receive App Invite invitation.  This activity
 * displays as a Dialog over the MainActivity and does not cover the full screen.
 */
public class DeepLinkActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = DeepLinkActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deep_link_activity);

        // Button click listener
        findViewById(R.id.button_ok).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check for link in intent
        if (getIntent() != null && getIntent().getData() != null) {
            Uri data = getIntent().getData();

            Log.d(TAG, "data:" + data);
            ((TextView) findViewById(R.id.deep_link_text))
                    .setText(getString(R.string.deep_link_fmt, data.toString()));
        }
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_ok) {
            finish();
        }
    }
}
