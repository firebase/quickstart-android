package com.google.firebase.quickstart.database;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.quickstart.database.models.User;

public class SignInActivity extends BaseActivity {

    private static final String TAG = "SignInActivity";

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private EditText mUsernameField;
    private Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mUsernameField = (EditText) findViewById(R.id.field_username);
        mSignUpButton = (Button) findViewById(R.id.button_sign_in);

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check auth on Activity start
        if (mAuth.getCurrentUser() != null) {
            launchMainActivity();
        }
    }

    private void signUp() {
        showProgressDialog();
        mAuth.signInAnonymously()
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "signInAnonymously:onSuccess:" + authResult);
                        hideProgressDialog();

                        // Write new user and go to Main Activity
                        writeNewUser(authResult.getUser(), mUsernameField.getText().toString());
                        launchMainActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Throwable throwable) {
                        Log.w(TAG, "signInAnonymously:onFailure", throwable);
                        hideProgressDialog();

                        Toast.makeText(SignInActivity.this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void launchMainActivity() {
        startActivity(new Intent(SignInActivity.this, MainActivity.class));
        finish();
    }

    // [START basic_write]
    private void writeNewUser(FirebaseUser firebaseUser, String name) {
        String userId = firebaseUser.getUid();
        User user = new User(name);

        mDatabase.child("users").child(userId).setValue(user);
    }
    // [END basic_write]
}
