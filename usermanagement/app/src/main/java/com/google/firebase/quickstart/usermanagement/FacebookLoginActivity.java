package com.google.firebase.quickstart.usermanagement;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class FacebookLoginActivity extends AppCompatActivity {

    private static final String TAG = "FacebookLogin";

    private ProgressDialog mProgressDialog;
    private TextView mStatusTextView;
    private TextView mDetailTextView;

    private FirebaseAuth mAuth;

    private CallbackManager mCallbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_facebook);

        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);
        mDetailTextView = (TextView) findViewById(R.id.detail);

        // Initialize Firebase Auth
        FirebaseApp.initializeApp(this, getString(R.string.google_app_id),
                new FirebaseOptions(getString(R.string.google_api_key)));
        mAuth = FirebaseAuth.getAuth();

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                updateUI(null);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                updateUI(null);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check for existing sign-in
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Log.d(TAG, "Got cached FirebaseUser");
            updateUI(user);
        } else {
            updateUI(null);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        OptionalPendingResult<AuthResult> opr = mAuth.signInWithCredential(credential);
        if (opr.isDone()) {
            handleFirebaseAuthResult(opr.get());
        } else {showProgressDialog();
            opr.setResultCallback(new ResultCallback<AuthResult>() {
                @Override
                public void onResult(@NonNull AuthResult result) {
                    handleFirebaseAuthResult(result);
                    hideProgressDialog();
                }
            });
        }
    }

    private void handleFirebaseAuthResult(AuthResult result) {
        if (result.getStatus().isSuccess()) {
            Log.d(TAG, "handleFirebaseAuthResult:SUCCESS");
            updateUI(result.getUser());
        } else {
            Log.d(TAG, "handleFirebaseAuthResukt:ERROR:" + result.getStatus().toString());
            updateUI(null);
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            mStatusTextView.setText(getString(R.string.facebook_status_fmt, user.getEmail()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUserId()));
        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);
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

}
