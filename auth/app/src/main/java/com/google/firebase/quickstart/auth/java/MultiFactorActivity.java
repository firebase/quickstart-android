/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.auth.java;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthMultiFactorException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.MultiFactorInfo;
import com.google.firebase.auth.PhoneMultiFactorInfo;
import com.google.firebase.quickstart.auth.R;

import java.util.List;

public class MultiFactorActivity extends BaseActivity implements
        View.OnClickListener {

    public static final int RESULT_NEEDS_MFA_SIGN_IN = 42;

    private static final String TAG = "MultiFactor";
    private static final int RC_MULTI_FACTOR = 9005;

    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private TextView mMfaInfoTextView;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_factor);
        setProgressBar(R.id.progressBar);

        // Views
        mStatusTextView = findViewById(R.id.status);
        mDetailTextView = findViewById(R.id.detail);
        mMfaInfoTextView = findViewById(R.id.mfaInfo);

        // Buttons
        findViewById(R.id.emailSignInButton).setOnClickListener(this);
        findViewById(R.id.signOutButton).setOnClickListener(this);
        findViewById(R.id.verifyEmailButton).setOnClickListener(this);
        findViewById(R.id.enrollMfa).setOnClickListener(this);
        findViewById(R.id.unenrollMfa).setOnClickListener(this);
        findViewById(R.id.reloadButton).setOnClickListener(this);

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        showDisclaimer();
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    // [END on_start_check_user]


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_MULTI_FACTOR) {
            if (resultCode == RESULT_NEEDS_MFA_SIGN_IN) {
                Intent intent = new Intent(this, MultiFactorSignInActivity.class);
                intent.putExtras(data.getExtras());
                startActivityForResult(intent, RC_MULTI_FACTOR);
            }
        }
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private void sendEmailVerification() {
        // Disable button
        findViewById(R.id.verifyEmailButton).setEnabled(false);

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button
                        findViewById(R.id.verifyEmailButton).setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(MultiFactorActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(MultiFactorActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    private void reload() {
        mAuth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(mAuth.getCurrentUser());
                    Toast.makeText(MultiFactorActivity.this,
                            "Reload successful!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "reload", task.getException());
                    Toast.makeText(MultiFactorActivity.this,
                            "Failed to reload user.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressBar();
        if (user != null) {
            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
                    user.getEmail(), user.isEmailVerified()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            List<MultiFactorInfo> secondFactors = user.getMultiFactor().getEnrolledFactors();

            if (secondFactors.isEmpty()) {
                findViewById(R.id.unenrollMfa).setVisibility(View.GONE);
            } else {
                findViewById(R.id.unenrollMfa).setVisibility(View.VISIBLE);

                StringBuilder sb = new StringBuilder("Second Factors: ");
                String delimiter = ", ";
                for (MultiFactorInfo x : secondFactors) {
                    sb.append(((PhoneMultiFactorInfo) x).getPhoneNumber() + delimiter);
                }
                sb.setLength(sb.length() - delimiter.length());
                mMfaInfoTextView.setText(sb.toString());
            }

            findViewById(R.id.emailPasswordButtons).setVisibility(View.GONE);
            findViewById(R.id.signedInButtons).setVisibility(View.VISIBLE);

            int reloadVisibility = secondFactors.isEmpty() ? View.VISIBLE : View.GONE;
            findViewById(R.id.reloadButton).setVisibility(reloadVisibility);

            if (user.isEmailVerified()) {
                findViewById(R.id.verifyEmailButton).setVisibility(View.GONE);
                findViewById(R.id.enrollMfa).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.verifyEmailButton).setVisibility(View.VISIBLE);
                findViewById(R.id.enrollMfa).setVisibility(View.GONE);
            }
        } else {
            mStatusTextView.setText(R.string.multi_factor_signed_out);
            mDetailTextView.setText(null);
            mMfaInfoTextView.setText(null);

            findViewById(R.id.emailPasswordButtons).setVisibility(View.VISIBLE);
            findViewById(R.id.signedInButtons).setVisibility(View.GONE);
        }
    }

    private void showDisclaimer() {
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Multi-factor authentication with SMS is currently only available for " +
                        "Google Cloud Identity Platform projects. For more information see: " +
                        "https://cloud.google.com/identity-platform/docs/android/mfa")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.emailSignInButton) {
            startActivityForResult(new Intent(this, EmailPasswordActivity.class), RC_MULTI_FACTOR);
        } else if (i == R.id.signOutButton) {
            signOut();
        } else if (i == R.id.verifyEmailButton) {
            sendEmailVerification();
        } else if (i == R.id.enrollMfa) {
            startActivity(new Intent(this, MultiFactorEnrollActivity.class));
        } else if (i == R.id.unenrollMfa) {
            startActivity(new Intent(this, MultiFactorUnenrollActivity.class));
        } else if (i == R.id.reloadButton) {
            reload();
        }
    }
}
