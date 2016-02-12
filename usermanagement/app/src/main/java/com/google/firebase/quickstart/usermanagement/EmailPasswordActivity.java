package com.google.firebase.quickstart.usermanagement;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class EmailPasswordActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "EmailPassword";

    private ProgressDialog mProgressDialog;
    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private EditText mEmailField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailpassword);

        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);
        mDetailTextView = (TextView) findViewById(R.id.detail);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        // Buttons
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        // Initialize Firebase Auth
        FirebaseApp.initializeApp(this, getString(R.string.google_app_id),
                new FirebaseOptions(getString(R.string.google_api_key)));
        mAuth = FirebaseAuth.getAuth();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check for existing sign-in
        FirebaseUser user = mAuth.getCurrentUser();
        updateUI(user);
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        showProgressDialog();

        mAuth.createUserWithEmailAndPassword(email, password).setResultCallback(
                new ResultCallback<AuthResult>() {
                    @Override
                    public void onResult(@NonNull AuthResult result) {
                        Log.d(TAG, "create:onResult:" + result.getStatus());
                        handleFirebaseAuthResult(result);
                        hideProgressDialog();
                    }
                });
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        showProgressDialog();

        mAuth.signInWithEmailAndPassword(email, password).setResultCallback(
                new ResultCallback<AuthResult>() {
                    @Override
                    public void onResult(@NonNull AuthResult result) {
                        Log.d(TAG, "signIn:onResult:" + result.getStatus());
                        handleFirebaseAuthResult(result);
                        hideProgressDialog();
                    }
                });
    }

    private void signOut() {
        mAuth.signOut(this);
        updateUI(null);
    }

    private void handleFirebaseAuthResult(AuthResult result) {
        if (result.getStatus().isSuccess()) {
            Log.d(TAG, "handleFirebaseAuthResult:SUCCESS");
            updateUI(result.getUser());
        } else {
            Log.d(TAG, "handleFirebaseAuthResukt:ERROR:" + result.getStatus().toString());
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt, user.getEmail()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUserId()));

            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
            findViewById(R.id.email_password_fields).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
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
            case R.id.email_create_account_button:
                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
                break;
            case R.id.email_sign_in_button:
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }
}
