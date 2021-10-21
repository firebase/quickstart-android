package com.google.firebase.quickstart.auth.java;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.quickstart.auth.BuildConfig;
import com.google.firebase.quickstart.auth.R;
import com.google.firebase.quickstart.auth.databinding.FragmentFirebaseUiBinding;

import java.util.Collections;

/**
 * Demonstrate authentication using the FirebaseUI-Android library. This fragment demonstrates
 * using FirebaseUI for basic email/password sign in.
 * <p>
 * For more information, visit https://github.com/firebase/firebaseui-android
 */
public class FirebaseUIFragment extends Fragment {

    private FirebaseAuth mAuth;

    private FragmentFirebaseUiBinding mBinding;

    // Build FirebaseUI sign in intent. For documentation on this operation and all
    // possible customization see: https://github.com/firebase/firebaseui-android
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentFirebaseUiBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mBinding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignIn();
            }
        });

        mBinding.signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI(mAuth.getCurrentUser());
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            // Sign in succeeded
            updateUI(mAuth.getCurrentUser());
        } else {
            // Sign in failed
            Toast.makeText(getContext(), "Sign In Failed", Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
    }

    private void startSignIn() {
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.EmailBuilder().build()))
                .setLogo(R.mipmap.ic_launcher)
                .build();

        signInLauncher.launch(intent);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Signed in
            mBinding.status.setText(getString(R.string.firebaseui_status_fmt, user.getEmail()));
            mBinding.detail.setText(getString(R.string.id_fmt, user.getUid()));

            mBinding.signInButton.setVisibility(View.GONE);
            mBinding.signOutButton.setVisibility(View.VISIBLE);
        } else {
            // Signed out
            mBinding.status.setText(R.string.signed_out);
            mBinding.detail.setText(null);

            mBinding.signInButton.setVisibility(View.VISIBLE);
            mBinding.signOutButton.setVisibility(View.GONE);
        }
    }

    private void signOut() {
        AuthUI.getInstance().signOut(getContext());
        updateUI(null);
    }
}