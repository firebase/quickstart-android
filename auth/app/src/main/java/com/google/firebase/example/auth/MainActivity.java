package com.google.firebase.example.auth;

import com.google.android.gms.auth.api.signin.EmailSignInOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseError;
import com.google.firebase.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ui.EmailAuthProvider;
import com.google.firebase.auth.ui.FacebookAuthProvider;
import com.google.firebase.auth.ui.GoogleAuthProvider;
import com.google.firebase.auth.ui.SignInUIBuilder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Firebase
        FirebaseApp.initializeApp(this, getString(R.string.google_app_id));

        // Initialize authentication
        mAuth = FirebaseAuth.getAuth();
        mAuth.addAuthResultCallback(new FirebaseAuth.AuthResultCallbacks() {
            @Override
            public void onAuthenticated(@NonNull FirebaseUser firebaseUser) {
                Log.d(TAG, "onAuthenticated:" + firebaseUser);
                mAuth.removeAuthResultCallback(this);
                showSignedInUI(firebaseUser);
            }

            @Override
            public void onAuthenticationError(@NonNull FirebaseError firebaseError) {
                Log.d(TAG, "onAuthenticationError:" + firebaseError.getErrorCode());
                mAuth.removeAuthResultCallback(this);
                showSignedOutUI();
            }
        });

        // Click listeners
        findViewById(R.id.button_sign_in).setOnClickListener(this);
        findViewById(R.id.button_sign_out).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check for signed-in user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            showSignedInUI(currentUser);
        } else {
            showSignedOutUI();
        }
    }

    private void onSignInClicked() {
        // Support Google, Facebook, and Email/Password sign-in
        GoogleAuthProvider googleAuth = GoogleAuthProvider.getDefaultAuthProvider();

        EmailAuthProvider emailAuth = new EmailAuthProvider(
                new EmailSignInOptions(Uri.parse(getString(R.string.server_widget_url))));

        // TODO(samstern): this crashes
        FacebookAuthProvider facebookAuth = new FacebookAuthProvider()
                .addScope("email");

        Intent intent = new SignInUIBuilder(mAuth)
                .setServerClientId(getString(R.string.server_client_id))
                .supportSignIn(googleAuth)
                .supportSignIn(facebookAuth)
                .supportSignIn(emailAuth)
                .build(this);
        startActivity(intent);
    }

    private void onSignOutClicked() {
        mAuth.signOut(this);
        showSignedOutUI();
    }

    private void showSignedInUI(FirebaseUser user) {
        findViewById(R.id.button_sign_in).setVisibility(View.GONE);
        findViewById(R.id.button_sign_out).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.text_status)).setText(
                getString(R.string.signed_in_as, user.getDisplayName(), user.getEmail()));
    }

    private void showSignedOutUI() {
        findViewById(R.id.button_sign_in).setVisibility(View.VISIBLE);
        findViewById(R.id.button_sign_out).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.text_status)).setText(R.string.signed_out);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sign_in:
                onSignInClicked();
                break;
            case R.id.button_sign_out:
                onSignOutClicked();
                break;
        }
    }
}
