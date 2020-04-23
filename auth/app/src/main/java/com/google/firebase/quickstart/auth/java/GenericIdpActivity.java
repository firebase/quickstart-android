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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.quickstart.auth.R;
import com.google.firebase.quickstart.auth.databinding.ActivityGenericIdpBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrate Firebase Authentication using a Generic Identity Provider (IDP).
 */
@SuppressWarnings("Convert2Lambda")
public class GenericIdpActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "GenericIdp";

    private static final Map<String,String> PROVIDER_MAP = new HashMap<String, String>() {
        {
            put("Apple", "apple.com");
            put("Microsoft", "microsoft.com");
            put("Yahoo", "yahoo.com");
            put("Twitter", "twitter.com");
        }
    };

    private ActivityGenericIdpBinding mBinding;
    private ArrayAdapter<String> mSpinnerAdapter;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityGenericIdpBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        // Views

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set up button click listeners
        mBinding.genericSignInButton.setOnClickListener(this);
        mBinding.signOutButton.setOnClickListener(this);

        // Spinner
        List<String> providers = new ArrayList<>(PROVIDER_MAP.keySet());
        mSpinnerAdapter = new ArrayAdapter<>(this, R.layout.item_spinner_list, providers);
        mBinding.providerSpinner.setAdapter(mSpinnerAdapter);
        mBinding.providerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mBinding.genericSignInButton.setText(getString(R.string.generic_signin_fmt, mSpinnerAdapter.getItem(position)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        mBinding.providerSpinner.setSelection(0);
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
                    updateUI(authResult.getUser());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "checkPending:onFailure", e);
                }
            });
        } else {
            Log.d(TAG, "checkPending: null");
        }
    }

    private void signIn() {
        // Could add custom scopes here
        ArrayList<String> scopes = new ArrayList<>();

        // Examples of provider ID: apple.com (Apple), microsoft.com (Microsoft), yahoo.com (Yahoo)
        String providerId = getProviderId();

        mAuth.startActivityForSignInWithProvider(this,
                OAuthProvider.newBuilder(providerId, mAuth)
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
                                showToast(getString(R.string.error_sign_in_failed));
                            }
                        });
    }

    private String getProviderId() {
        String providerName = mSpinnerAdapter.getItem(mBinding.providerSpinner.getSelectedItemPosition());
        return PROVIDER_MAP.get(providerName);
    }

    private void updateUI(FirebaseUser user) {
        hideProgressBar();
        if (user != null) {
            mBinding.status.setText(getString(R.string.generic_status_fmt, user.getDisplayName(), user.getEmail()));
            mBinding.detail.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            mBinding.spinnerLayout.setVisibility(View.GONE);
            mBinding.genericSignInButton.setVisibility(View.GONE);
            mBinding.signOutButton.setVisibility(View.VISIBLE);
        } else {
            mBinding.status.setText(R.string.signed_out);
            mBinding.detail.setText(null);

            mBinding.spinnerLayout.setVisibility(View.VISIBLE);
            mBinding.genericSignInButton.setVisibility(View.VISIBLE);
            mBinding.signOutButton.setVisibility(View.GONE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
