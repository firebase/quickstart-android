package com.google.firebase.quickstart.auth;

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

/**
 * Demonstrate Firebase Authentication using a Facebook access token.
 */
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

        // [START initialize_auth]
        // Initialize Firebase Auth
        FirebaseApp.initializeApp(this, getString(R.string.google_app_id),
                new FirebaseOptions.Builder(getString(R.string.google_api_key)).build());
        mAuth = FirebaseAuth.getAuth();
        // [END initialize_auth]

        // [START initialize_fblogin]
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
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        });
        // [END initialize_fblogin]
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check for existing sign-in
        // TODO(samstern): How to relate this to the Facebook LoginButton state?
        FirebaseUser user = mAuth.getCurrentUser();
        updateUI(user);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        OptionalPendingResult<AuthResult> opr = mAuth.signInWithCredential(credential);
        if (opr.isDone()) {
            handleFirebaseAuthResult(opr.get());
        } else {
            // Show progress dialog
            // [START_EXCLUDE]
            showProgressDialog();
            // [END_EXCLUDE]
            opr.setResultCallback(new ResultCallback<AuthResult>() {
                @Override
                public void onResult(@NonNull AuthResult result) {
                    handleFirebaseAuthResult(result);
                    // Hide progress dialog
                    // [START_EXCLUDE]
                    hideProgressDialog();
                    // [END_EXCLUDE]
                }
            });
        }
    }

    private void handleFirebaseAuthResult(AuthResult result) {
        if (result.getStatus().isSuccess()) {
            Log.d(TAG, "handleFirebaseAuthResult:SUCCESS");
            FirebaseUser user = result.getUser();
            // [START_EXCLUDE]
            updateUI(user);
            // [END_EXCLUDE]
        } else {
            Log.d(TAG, "handleFirebaseAuthResult:ERROR:" + result.getStatus().toString());
            // [START_EXCLUDE]
            updateUI(null);
            // [END_EXCLUDE]
        }
    }
    // [END auth_with_facebook]

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            mStatusTextView.setText(getString(R.string.facebook_status_fmt, user.getEmail()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
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
