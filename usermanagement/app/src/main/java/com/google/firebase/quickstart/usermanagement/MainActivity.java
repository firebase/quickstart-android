/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.quickstart.usermanagement;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private String mCustomToken;
    private TokenBroadcastReceiver mTokenReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Button click listeners
        findViewById(R.id.button_get_custom_token).setOnClickListener(this);
        findViewById(R.id.button_sign_in).setOnClickListener(this);

        // Create token receiver (for demo purposes only)
        mTokenReceiver = new TokenBroadcastReceiver() {
            @Override
            public void onNewToken(String token) {
                Log.d(TAG, "onNewToken:" + token);
                setCustomToken(token);
            }
        };

        // Initialize Firebase
        String apiKey = getString(R.string.api_key);
        FirebaseApp.initializeApp(this, getString(R.string.google_app_id),
                new FirebaseOptions(apiKey));

        // Get instance of FirebaseAuth
        mAuth = FirebaseAuth.getAuth();
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(mTokenReceiver, TokenBroadcastReceiver.getFilter());
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(mTokenReceiver);
    }

    private void getCustomToken() {
        // Get user ID and mint a custom token
        String userId = ((EditText) findViewById(R.id.field_user_id)).getText().toString();
        new GetTokenTask().execute(userId);
    }

    private void startSignIn() {
        // Initiate sign in with custom token
        mAuth.signInWithCustomToken(mCustomToken).setResultCallback(new ResultCallback<AuthResult>() {
            @Override
            public void onResult(@NonNull AuthResult authResult) {
                Log.d(TAG, "signInWithCustomToken:" + authResult.getStatus());

                String message;
                if (authResult.getStatus().isSuccess()) {
                    message = "User ID: " + authResult.getUser().getUserId();
                } else {
                    message = "Error: " + authResult.getStatus().getStatusMessage();
                }

                ((TextView) findViewById(R.id.text_sign_in_status)).setText(message);
            }
        });
    }

    private void setCustomToken(String token) {
        mCustomToken = token;

        String status;
        if (mCustomToken != null) {
            status = "Token: " + mCustomToken;
        } else {
            status = "Token: null";
        }

        // Enable/disable sign-in button and show the token
        findViewById(R.id.button_sign_in).setEnabled((mCustomToken != null));
        ((TextView) findViewById(R.id.text_token_status)).setText(status);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_get_custom_token:
                getCustomToken();
                break;
            case R.id.button_sign_in:
                startSignIn();
                break;
        }
    }

    /**
     * Request custom token for a given user ID. In a real application this would contact your
     * authentication server over the internet, but for the purposes of this sample the
     * MockTokenServer class generates the necessary JSON Web Token.
     *
     * In a real application this would be a network call, in which a case an IntentService would
     * likely be more appropriate than an AsyncTask due to durability.
     */
    class GetTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            MockTokenServer server = new MockTokenServer(MainActivity.this);

            String userId = params[0];
            return server.getCustomToken(userId);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "GetTokenTask:token:" + result);
            setCustomToken(result);
        }
    }
}
