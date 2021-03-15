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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.MultiFactorInfo;
import com.google.firebase.auth.PhoneMultiFactorInfo;
import com.google.firebase.quickstart.auth.R;
import com.google.firebase.quickstart.auth.databinding.FragmentMultiFactorBinding;

import java.util.List;

public class MultiFactorFragment extends BaseFragment {

    public static final String RESULT_NEEDS_MFA_SIGN_IN = "RESULT_NEEDS_MFA";

    private static final String TAG = "MultiFactor";

    private FragmentMultiFactorBinding mBinding;

    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentMultiFactorBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setProgressBar(mBinding.progressBar);

        Bundle args = getArguments();
        if (args != null && args.getBoolean(RESULT_NEEDS_MFA_SIGN_IN)) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_mfa_to_mfasignin, args);
        }

        // Buttons
        mBinding.emailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(MultiFactorFragment.this)
                        .navigate(R.id.action_mfa_to_emailpassword);
            }
        });
        mBinding.signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        mBinding.verifyEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailVerification();
            }
        });
        mBinding.enrollMfa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(MultiFactorFragment.this)
                        .navigate(R.id.action_mfa_to_enroll);
            }
        });
        mBinding.unenrollMfa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(MultiFactorFragment.this)
                        .navigate(R.id.action_mfa_to_unenroll);
            }
        });
        mBinding.reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        showDisclaimer();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private void sendEmailVerification() {
        // Disable button
        mBinding.verifyEmailButton.setEnabled(false);

        // Send verification email
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Re-enable button
                        mBinding.verifyEmailButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(),
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(getContext(),
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void reload() {
        mAuth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(mAuth.getCurrentUser());
                    Toast.makeText(getContext(),
                            "Reload successful!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "reload", task.getException());
                    Toast.makeText(getContext(),
                            "Failed to reload user.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressBar();
        if (user != null) {
            mBinding.status.setText(getString(R.string.emailpassword_status_fmt,
                    user.getEmail(), user.isEmailVerified()));
            mBinding.detail.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            List<MultiFactorInfo> secondFactors = user.getMultiFactor().getEnrolledFactors();

            if (secondFactors.isEmpty()) {
                mBinding.unenrollMfa.setVisibility(View.GONE);
            } else {
                mBinding.unenrollMfa.setVisibility(View.VISIBLE);

                StringBuilder sb = new StringBuilder("Second Factors: ");
                String delimiter = ", ";
                for (MultiFactorInfo x : secondFactors) {
                    sb.append(((PhoneMultiFactorInfo) x).getPhoneNumber() + delimiter);
                }
                sb.setLength(sb.length() - delimiter.length());
                mBinding.mfaInfo.setText(sb.toString());
            }

            mBinding.emailSignInButton.setVisibility(View.GONE);
            mBinding.signedInButtons.setVisibility(View.VISIBLE);

            int reloadVisibility = secondFactors.isEmpty() ? View.VISIBLE : View.GONE;
            mBinding.reloadButton.setVisibility(reloadVisibility);

            if (user.isEmailVerified()) {
                mBinding.verifyEmailButton.setVisibility(View.GONE);
                mBinding.enrollMfa.setVisibility(View.VISIBLE);
            } else {
                mBinding.verifyEmailButton.setVisibility(View.VISIBLE);
                mBinding.enrollMfa.setVisibility(View.GONE);
            }
        } else {
            mBinding.status.setText(R.string.multi_factor_signed_out);
            mBinding.detail.setText(null);
            mBinding.mfaInfo.setText(null);

            mBinding.emailSignInButton.setVisibility(View.VISIBLE);
            mBinding.signedInButtons.setVisibility(View.GONE);
        }
    }

    private void showDisclaimer() {
        new AlertDialog.Builder(getContext())
                .setTitle("Warning")
                .setMessage("Multi-factor authentication with SMS is currently only available for " +
                        "Google Cloud Identity Platform projects. For more information see: " +
                        "https://cloud.google.com/identity-platform/docs/android/mfa")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
