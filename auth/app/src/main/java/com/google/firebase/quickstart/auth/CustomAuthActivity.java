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
package com.google.firebase.quickstart.auth;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Demonstrate Firebase Authentication using a custom minted token. For more information, see:
 * https://developers.google.com/firebase/docs/auth/android/custom-auth
 */
public class CustomAuthActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CustomAuthActivity";

    private FirebaseAuth mAuth;
    private String mCustomToken;
    private TokenBroadcastReceiver mTokenReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);

        // Button click listeners
        findViewById(R.id.button_sign_in).setOnClickListener(this);

        // Create token receiver (for demo purposes only)
        mTokenReceiver = new TokenBroadcastReceiver() {
            @Override
            public void onNewToken(String token) {
                Log.d(TAG, "onNewToken:" + token);
                setCustomToken(token);
            }
        };

        // [START initialize_auth]
        // Initialize Firebase
        FirebaseApp.initializeApp(this, getString(R.string.google_app_id),
                new FirebaseOptions(getString(R.string.google_api_key)));

        // Get instance of FirebaseAuth
        mAuth = FirebaseAuth.getAuth();
        // [END initialize_auth]
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

    private void startSignIn() {
        // Initiate sign in with custom token
        // [START sign_in_custom]
        mAuth.signInWithCustomToken(mCustomToken).setResultCallback(new ResultCallback<AuthResult>() {
            @Override
            public void onResult(@NonNull AuthResult authResult) {
                Log.d(TAG, "signInWithCustomToken:" + authResult.getStatus());
                if (authResult.getStatus().isSuccess()) {
                    // Signed in, display user information.
                    FirebaseUser user = authResult.getUser();

                    // [START_EXCLUDE]
                    ((TextView) findViewById(R.id.text_sign_in_status)).setText(
                            "User ID: " + user.getUid());
                    Toast.makeText(CustomAuthActivity.this, "Signed In", Toast.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                } else {
                    // Sign-in error, display a message.
                    String errorMessage = authResult.getStatus().getStatusMessage();

                    // [START_EXCLUDE]
                    ((TextView) findViewById(R.id.text_sign_in_status)).setText(
                            "Error: " + errorMessage);
                    // [END_EXCLUDE]
                }

            }
        });
        // [END sign_in_custom]
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
            case R.id.button_sign_in:
                startSignIn();
                break;
        }
    }
}
