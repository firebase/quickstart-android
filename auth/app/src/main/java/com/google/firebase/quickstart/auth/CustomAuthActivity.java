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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Demonstrate Firebase Authentication using a custom minted token. For more information, see:
 * https://firebase.google.com/docs/auth/android/custom-auth
 */
public class CustomAuthActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CustomAuthActivity";

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]

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
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // [START_EXCLUDE]
                updateUI(user);
                // [END_EXCLUDE]
            }
        };
        // [END auth_state_listener]
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        // [START_EXCLUDE]
        registerReceiver(mTokenReceiver, TokenBroadcastReceiver.getFilter());
        // [END_EXCLUDE]
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        // [START_EXCLUDE]
        unregisterReceiver(mTokenReceiver);
        // [END_EXCLUDE]
    }
    // [END on_stop_remove_listener]

    private void startSignIn() {
        // Initiate sign in with custom token
        // [START sign_in_custom]
        mAuth.signInWithCustomToken(mCustomToken)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCustomToken:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCustomToken", task.getException());
                            Toast.makeText(CustomAuthActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // [END sign_in_custom]
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            ((TextView) findViewById(R.id.text_sign_in_status)).setText(
                    "User ID: " + user.getUid());
        } else {
            ((TextView) findViewById(R.id.text_sign_in_status)).setText(
                    "Error: sign in failed.");
        }
    }

    private void setCustomToken(String token) {
        mCustomToken = token;

        String status;
        if (mCustomToken != null) {
            status = "Token:" + mCustomToken;
        } else {
            status = "Token: null";
        }

        // Enable/disable sign-in button and show the token
        findViewById(R.id.button_sign_in).setEnabled((mCustomToken != null));
        ((TextView) findViewById(R.id.text_token_status)).setText(status);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_sign_in) {
            startSignIn();

        }
    }
}
