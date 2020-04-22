package com.google.firebase.quickstart.auth.java;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthActionCodeException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.quickstart.auth.R;
import com.google.firebase.quickstart.auth.databinding.ActivityPasswordlessBinding;

/**
 * Demonstrate Firebase Authentication without a password, using a link sent to an
 * email address.
 */
public class PasswordlessActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "PasswordlessSignIn";
    private static final String KEY_PENDING_EMAIL = "key_pending_email";

    private FirebaseAuth mAuth;

    private ActivityPasswordlessBinding mBinding;

    private String mPendingEmail;
    private String mEmailLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityPasswordlessBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setProgressBar(mBinding.progressBar);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mBinding.passwordlessSendEmailButton.setOnClickListener(this);
        mBinding.passwordlessSignInButton.setOnClickListener(this);
        mBinding.signOutButton.setOnClickListener(this);

        // Restore the "pending" email address
        if (savedInstanceState != null) {
            mPendingEmail = savedInstanceState.getString(KEY_PENDING_EMAIL, null);
            mBinding.fieldEmail.setText(mPendingEmail);
        }

        // Check if the Intent that started the Activity contains an email sign-in link.
        checkIntent(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI(mAuth.getCurrentUser());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntent(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_PENDING_EMAIL, mPendingEmail);
    }

    /**
     * Check to see if the Intent has an email link, and if so set up the UI accordingly.
     * This can be called from either onCreate or onNewIntent, depending on how the Activity
     * was launched.
     */
    private void checkIntent(@Nullable Intent intent) {
        if (intentHasEmailLink(intent)) {
            mEmailLink = intent.getData().toString();

            mBinding.status.setText(R.string.status_link_found);
            mBinding.passwordlessSendEmailButton.setEnabled(false);
            mBinding.passwordlessSignInButton.setEnabled(true);
        } else {
            mBinding.status.setText(R.string.status_email_not_sent);
            mBinding.passwordlessSendEmailButton.setEnabled(true);
            mBinding.passwordlessSignInButton.setEnabled(false);
        }
    }

    /**
     * Determine if the given Intent contains an email sign-in link.
     */
    private boolean intentHasEmailLink(@Nullable Intent intent) {
        if (intent != null && intent.getData() != null) {
            String intentData = intent.getData().toString();
            if (mAuth.isSignInWithEmailLink(intentData)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Send an email sign-in link to the specified email.
     */
    private void sendSignInLink(String email) {
        ActionCodeSettings settings = ActionCodeSettings.newBuilder()
                .setAndroidPackageName(
                        getPackageName(),
                        false, /* install if not available? */
                        null   /* minimum app version */)
                .setHandleCodeInApp(true)
                .setUrl("https://auth.example.com/emailSignInLink")
                .build();

        hideKeyboard(mBinding.fieldEmail);
        showProgressBar();

        mAuth.sendSignInLinkToEmail(email, settings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideProgressBar();

                        if (task.isSuccessful()) {
                            Log.d(TAG, "Link sent");
                            showSnackbar("Sign-in link sent!");

                            mPendingEmail = email;
                            mBinding.status.setText(R.string.status_email_sent);
                        } else {
                            Exception e = task.getException();
                            Log.w(TAG, "Could not send link", e);
                            showSnackbar("Failed to send link.");

                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                mBinding.fieldEmail.setError("Invalid email address.");
                            }
                        }
                    }
                });
    }

    /**
     * Sign in using an email address and a link, the link is passed to the Activity
     * from the dynamic link contained in the email.
     */
    private void signInWithEmailLink(String email, String link) {
        Log.d(TAG, "signInWithLink:" + link);

        hideKeyboard(mBinding.fieldEmail);
        showProgressBar();

        mAuth.signInWithEmailLink(email, link)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressBar();
                        mPendingEmail = null;

                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmailLink:success");

                            mBinding.fieldEmail.setText(null);
                            updateUI(task.getResult().getUser());
                        } else {
                            Log.w(TAG, "signInWithEmailLink:failure", task.getException());
                            updateUI(null);

                            if (task.getException() instanceof FirebaseAuthActionCodeException) {
                                showSnackbar("Invalid or expired sign-in link.");
                            }
                        }
                    }
                });
    }

    private void onSendLinkClicked() {
        String email = mBinding.fieldEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mBinding.fieldEmail.setError("Email must not be empty.");
            return;
        }

        sendSignInLink(email);
    }

    private void onSignInClicked() {
        String email = mBinding.fieldEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mBinding.fieldEmail.setError("Email must not be empty.");
            return;
        }

        signInWithEmailLink(email, mEmailLink);
    }

    private void onSignOutClicked() {
        mAuth.signOut();

        updateUI(null);
        mBinding.status.setText(R.string.status_email_not_sent);
    }

    private void updateUI(@Nullable FirebaseUser user) {
        if (user != null) {
            mBinding.status.setText(getString(R.string.passwordless_status_fmt,
                    user.getEmail(), user.isEmailVerified()));

            mBinding.passwordlessFields.setVisibility(View.GONE);
            mBinding.passwordlessButtons.setVisibility(View.GONE);
            mBinding.signedInButtons.setVisibility(View.VISIBLE);
        } else {
            mBinding.passwordlessFields.setVisibility(View.VISIBLE);
            mBinding.passwordlessButtons.setVisibility(View.VISIBLE);
            mBinding.signedInButtons.setVisibility(View.GONE);
        }
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.passwordlessSendEmailButton:
                onSendLinkClicked();
                break;
            case R.id.passwordlessSignInButton:
                onSignInClicked();
                break;
            case R.id.signOutButton:
                onSignOutClicked();
                break;
        }
    }
}
