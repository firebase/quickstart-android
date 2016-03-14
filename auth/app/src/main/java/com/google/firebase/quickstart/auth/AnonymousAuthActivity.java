package com.google.firebase.quickstart.auth;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Activity to demonstrate anonymous login and account linking (with an email/password account).
 */
public class AnonymousAuthActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "AnonymousAuth";

    private FirebaseAuth mAuth;

    private ProgressDialog mProgressDialog;
    private EditText mEmailField;
    private EditText mPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_auth);

        // Initialize Firebase Auth
        FirebaseApp.initializeApp(this, getString(R.string.google_app_id),
                new FirebaseOptions.Builder(getString(R.string.google_api_key)).build());
        mAuth = FirebaseAuth.getAuth();

        // Fields
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        // Click listeners
        findViewById(R.id.button_anonymous_sign_in).setOnClickListener(this);
        findViewById(R.id.button_anonymous_sign_out).setOnClickListener(this);
        findViewById(R.id.button_link_account).setOnClickListener(this);
    }

    private void signInAnonymously() {
        showProgressDialog();
        mAuth.signInAnonymously().setResultCallback(new ResultCallback<AuthResult>() {
            @Override
            public void onResult(@NonNull AuthResult result) {
                hideProgressDialog();
                handleFirebaseAuthResult(result);
            }
        });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private void linkAccount(String email, String password) {
        // Create EmailAuthCredential with email and password
        AuthCredential credential = EmailAuthProvider.getEmailAuthCredential(email, password);

        // Link the anonymous user to the email credential
        showProgressDialog();
        mAuth.getCurrentUser().linkWithCredential(credential).setResultCallback(
                new ResultCallback<AuthResult>() {
                    @Override
                    public void onResult(@NonNull AuthResult result) {
                        hideProgressDialog();
                        handleFirebaseAuthResult(result);
                    }
                });
    }

    private void handleFirebaseAuthResult(AuthResult result) {
        if (result.getStatus().isSuccess()) {
            Log.d(TAG, "handleFirebaseAuthResult:SUCCESS");
            updateUI(result.getUser());
        } else {
            Log.d(TAG, "handleFirebaseAuthResult:ERROR:" + result.getStatus().toString());
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
    }

    private void updateUI(FirebaseUser user) {
        TextView idView = (TextView) findViewById(R.id.anonymous_status_id);
        TextView emailView = (TextView) findViewById(R.id.anonymous_status_email);
        boolean isSignedIn = (user != null);

        // Status text
        if (isSignedIn) {
            idView.setText(getString(R.string.id_fmt, user.getUid()));
            emailView.setText(getString(R.string.email_fmt, user.getEmail()));
        } else {
            idView.setText(R.string.signed_out);
            emailView.setText(null);
        }

        // Button visibility
        findViewById(R.id.button_anonymous_sign_in).setEnabled(!isSignedIn);
        findViewById(R.id.button_anonymous_sign_out).setEnabled(isSignedIn);
        findViewById(R.id.button_link_account).setEnabled(isSignedIn);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_anonymous_sign_in:
                signInAnonymously();
                break;
            case R.id.button_anonymous_sign_out:
                signOut();
                break;
            case R.id.button_link_account:
                String email = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();
                linkAccount(email, password);
                break;
        }
    }
}
