/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.auth.java;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.quickstart.auth.R;

import java.util.ArrayList;

/**
 * Demonstrate Firebase Authentication using a Generic Identity Provider (IDP).
 */
@SuppressWarnings("Convert2Lambda")
public class GenericIdpActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "GenericIdp";

    private TextView mStatusTextView;
    private TextView mDetailTextView;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_idp);

        // Views
        mStatusTextView = findViewById(R.id.status);
        mDetailTextView = findViewById(R.id.detail);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set up button click listeners
        findViewById(R.id.genericSignInButton).setOnClickListener(this);
        findViewById(R.id.signOutButton).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        // Look for a pending auth result
        Task<AuthResult> pending = mAuth.getPendingAuthResult();
        if (pending != null) {
            pending.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Log.d(TAG, "checkPending:onSuccess:" + authResult);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "checkPending:onFailure", e);
                }
            });
        } else {
            Log.d(TAG, "pending: null");
        }
    }

    private void signIn() {
        // Could add custom scopes here
        ArrayList<String> scopes = new ArrayList<>();

        mAuth.startActivityForSignInWithProvider(this,
                OAuthProvider.newBuilder("microsoft.com", mAuth)
                        .setScopes(scopes)
                        .build())
                .addOnSuccessListener(
                        new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Log.d(TAG, "activitySignIn:onSuccess:" + authResult.getUser());
                                updateUI(authResult.getUser());
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "activitySignIn:onFailure", e);
                            }
                        });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            mStatusTextView.setText(getString(R.string.msft_status_fmt, user.getDisplayName()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.genericSignInButton).setVisibility(View.GONE);
            findViewById(R.id.signOutButton).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.genericSignInButton).setVisibility(View.VISIBLE);
            findViewById(R.id.signOutButton).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.genericSignInButton:
                signIn();
                break;
            case R.id.signOutButton:
                mAuth.signOut();
                updateUI(null);
                break;
        }

    }
}
